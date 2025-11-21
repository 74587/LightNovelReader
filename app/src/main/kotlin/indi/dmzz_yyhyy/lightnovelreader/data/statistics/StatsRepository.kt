package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import indi.dmzz_yyhyy.lightnovelreader.data.json.AppUserDataContent
import indi.dmzz_yyhyy.lightnovelreader.data.json.DailyReadingStats
import indi.dmzz_yyhyy.lightnovelreader.data.json.toDailyStatsData
import indi.dmzz_yyhyy.lightnovelreader.data.json.toEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookRecordDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.ReadingStatisticsDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

data class ReadingStatsUpdate(
    val bookId: String,
    val secondDelta: Int = 0,
    val sessionDelta: Int = 0,
    val localTime: LocalTime = LocalTime.now(),
    val startedBooks: List<String> = emptyList(),
    val favoriteBooks: List<String> = emptyList()
)

@Singleton
class StatsRepository @Inject constructor(
    private val readingStatisticsDao: ReadingStatisticsDao,
    private val bookRecordDao: BookRecordDao
) {
    private val bookReadTimeBuffer = mutableMapOf<String, Pair<LocalTime, Int>>()

    suspend fun accumulateBookReadTime(bookId: String, seconds: Int) {
        if (seconds < 0) {
            bookReadTimeBuffer.keys.toList().forEach { _ ->
                clearBookReadTimeBuffer(bookId)
                bookReadTimeBuffer.remove(bookId)
            }
            return
        }
        val current = bookReadTimeBuffer[bookId] ?: Pair(LocalTime.now(), 0)
        val newTotal = current.second + seconds
        bookReadTimeBuffer[bookId] = current.copy(second = newTotal)

        if (newTotal >= 60 || Duration.between(current.first, LocalTime.now()).seconds >= 60) {
            clearBookReadTimeBuffer(bookId)
        }
    }

    private suspend fun clearBookReadTimeBuffer(bookId: String) {
        val (startTime, totalSeconds) = bookReadTimeBuffer[bookId] ?: return

        updateReadingStatistics(
            ReadingStatsUpdate(
                bookId = bookId,
                secondDelta = totalSeconds,
                localTime = startTime,
                sessionDelta = 0
            )
        )

        bookReadTimeBuffer.clear()
    }

    fun getAllReadingStats(): List<DailyReadingStats> {
        val statsMap = readingStatisticsDao.getAllReadingStatistics().associateBy { it.date }
        val recordsMap = bookRecordDao.getAllBookRecords().groupBy { it.date }
        val allDates = (statsMap.keys + recordsMap.keys).distinct().sorted()

        return allDates.mapNotNull { date ->
            val statistics = statsMap[date]
            val records = recordsMap[date] ?: emptyList()
            statistics?.toDailyStatsData(records)
        }
    }

    fun importReadingStats(data: AppUserDataContent) {
        data.readingStatsData?.forEach { dailyStats ->
            val statsEntity = dailyStats.toEntity()
            val recordEntities = dailyStats.bookRecords.map { it.toEntity() }

            readingStatisticsDao.insertReadingStatistics(statsEntity)
            recordEntities.forEach {
                bookRecordDao.insertBookRecord(it)
            }
        }
    }

    suspend fun getReadingStatistics(start: LocalDate, end: LocalDate? = null): Map<LocalDate, ReadingStatisticsEntity> {
        return if (end == null) {
            val entity = readingStatisticsDao.getReadingStatisticsForDate(start) ?: createStatsEntity(start)
            mapOf(start to entity)
        } else {
            val allDates = generateSequence(start) { it.plusDays(1) }
                .takeWhile { !it.isAfter(end) }
                .toList()

            val fetchedStats = readingStatisticsDao.getReadingStatisticsBetweenDates(allDates.first(), allDates.last())
                .groupBy { it.date }
                .mapValues { it.value.first() }

            fetchedStats.ifEmpty {
                allDates.associateWith { createStatsEntity(it) }
            }
        }
    }

    suspend fun getBookRecords(
        start: LocalDate,
        end: LocalDate? = null
    ): Map<LocalDate, List<BookRecordEntity>> {
        val raw = if (end == null) {
            val records = bookRecordDao.getBookRecordsForDate(start)
            mapOf(start to records)
        } else {
            bookRecordDao.getBookRecordsBetweenDates(start, end).groupBy { it.date }
        }
        val bookRecordsList = raw.mapValues { (_, records) ->
            records.filter { it.id != -721 } // We exclude total records with id -721 on fetch
        }.filterValues { it.isNotEmpty() }

        return bookRecordsList
    }

    /**
     * 具有特殊 id 的总阅读记录 Entity，用于总体统计
     */
    private fun createTotalRecordEntity(): BookRecordEntity = BookRecordEntity(
        id = -721,
        date = LocalDate.now(),
        bookId = "-721",
        sessions = 0,
        totalTime = 0,
        firstSeen = LocalTime.now(),
        lastSeen = LocalTime.now()
    )

    private fun createRecordEntity(bookId: String): BookRecordEntity = BookRecordEntity(
        id = null,
        date = LocalDate.now(),
        bookId = bookId,
        sessions = 0,
        totalTime = 0,
        firstSeen = LocalTime.now(),
        lastSeen = LocalTime.now()
    )

    fun createStatsEntity(date: LocalDate) = ReadingStatisticsEntity(
        date = date,
        readingTimeCount = Count(),
        foregroundTime = 0,
        favoriteBooks = emptyList(),
        startedBooks = emptyList(),
        finishedBooks = emptyList()
    )

    suspend fun getTotalBookRecord(): BookRecordEntity {
        return bookRecordDao.getTotalRecord() ?: createTotalRecordEntity()
    }

    private suspend fun updateTotalRecord(update: ReadingStatsUpdate) {
        val totalRecord = getTotalBookRecord()
        val updatedTotal = totalRecord.copy(
            sessions = totalRecord.sessions + update.sessionDelta,
            totalTime = totalRecord.totalTime + update.secondDelta,
            lastSeen = LocalTime.now()
        )

        bookRecordDao.insertBookRecord(updatedTotal)
    }

    suspend fun updateReadingStatistics(update: ReadingStatsUpdate) {
        val today = LocalDate.now()
        val statsEntity = getReadingStatistics(today)[today] ?: createStatsEntity(today)

        val updatedStats = statsEntity.copy(
            readingTimeCount = updateCount(statsEntity.readingTimeCount, update)
        )

        val existingBookRecord = bookRecordDao.getBookRecordByIdAndDate(update.bookId, today) ?: createRecordEntity(update.bookId)

        val newBookRecord = existingBookRecord.copy(
            totalTime = existingBookRecord.totalTime + update.secondDelta,
            sessions = existingBookRecord.sessions + update.sessionDelta,
            lastSeen = update.localTime
        )

        updateTotalRecord(update)

        readingStatisticsDao.insertReadingStatistics(updatedStats)
        bookRecordDao.insertBookRecord(newBookRecord)

        bookReadTimeBuffer.clear()
    }

    suspend fun updateBookStatus(
        bookId: String,
        isFavorite: Boolean = false,
        isFirstReading: Boolean = false,
        isFinishedReading: Boolean = false
    ) {
        val date = LocalDate.now()
        val currentEntity = readingStatisticsDao.getReadingStatisticsForDate(date) ?: createStatsEntity(date)

        val updatedEntity = currentEntity.copy(
            favoriteBooks = updateList(currentEntity.favoriteBooks, bookId, isFavorite),
            startedBooks = updateList(currentEntity.startedBooks, bookId, isFirstReading),
            finishedBooks = updateList(currentEntity.finishedBooks, bookId, isFinishedReading)
        )

        readingStatisticsDao.insertReadingStatistics(updatedEntity)
    }

    private fun updateCount(count: Count, update: ReadingStatsUpdate): Count {
        val minutesDelta = update.secondDelta / 60
        if (minutesDelta > 0) {
            val hour = update.localTime.hour
            val totalMinutes = count.getMinute(hour) + minutesDelta
            count.setMinute(hour, totalMinutes.coerceAtMost(60))
        }
        return count
    }

    fun updateList(list: List<String>, item: String, add: Boolean): List<String> =
        if (add) {
            if (item !in list) list + item else list
        } else list - item

    fun clear() {
        readingStatisticsDao.clear()
        bookRecordDao.clear()
    }
}