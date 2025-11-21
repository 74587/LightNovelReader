package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.CountConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.Count
import java.time.LocalDate

@Dao
@TypeConverters(
    LocalDateTimeConverter::class,
    CountConverter::class,
    ListConverter::class
)
interface ReadingStatisticsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReadingStatistics(statistics: ReadingStatisticsEntity)

    @Query("SELECT * FROM reading_statistics WHERE date = :date LIMIT 1")
    suspend fun getReadingStatisticsForDate(date: LocalDate): ReadingStatisticsEntity?

    @Query("SELECT * FROM reading_statistics")
    fun getAllReadingStatistics(): List<ReadingStatisticsEntity>

    @Delete
    suspend fun deleteReadingStatistics(statistics: ReadingStatisticsEntity)

    @Query("SELECT * FROM reading_statistics WHERE date = :date LIMIT 1")
    suspend fun getReadingTimeCountForDate(date: LocalDate): Count? =
        getReadingStatisticsForDate(date)?.readingTimeCount

    @Query("SELECT * FROM reading_statistics WHERE date BETWEEN :start AND :end")
    suspend fun getReadingStatisticsBetweenDates(start: LocalDate, end: LocalDate): List<ReadingStatisticsEntity>

    @Query("SELECT * FROM reading_statistics WHERE date = :date LIMIT 1")
    suspend fun getFavoriteBooksForDate(date: LocalDate): List<String>? =
        getReadingStatisticsForDate(date)?.favoriteBooks

    @Query("SELECT * FROM reading_statistics WHERE date = :date LIMIT 1")
    suspend fun getStartedBooksForDate(date: LocalDate): List<String>? =
        getReadingStatisticsForDate(date)?.startedBooks

    @Query("SELECT * FROM reading_statistics WHERE date = :date LIMIT 1")
    suspend fun getFinishedBooksForDate(date: LocalDate): List<String>? =
        getReadingStatisticsForDate(date)?.finishedBooks

    @Query("SELECT * FROM reading_statistics WHERE date = :date LIMIT 1")
    suspend fun getForegroundTimeForDate(date: LocalDate): Int? =
        getReadingStatisticsForDate(date)?.foregroundTime

    @Query("DELETE FROM reading_statistics")
    fun clearStatistics()

    @Transaction
    fun clear() {
        clearStatistics()
    }
}