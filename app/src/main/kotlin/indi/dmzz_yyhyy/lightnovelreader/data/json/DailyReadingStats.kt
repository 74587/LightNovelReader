package indi.dmzz_yyhyy.lightnovelreader.data.json

import com.google.gson.annotations.SerializedName
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.Count
import java.time.LocalDate
import java.time.LocalTime

data class DailyReadingStats(
    @SerializedName("date")
    val date: LocalDate,
    @SerializedName("reading_time_count")
    val readingTimeCount: Count,
    @SerializedName("foreground_time")
    val foregroundTime: Int,
    @SerializedName("favorite_books")
    val favoriteBooks: List<String>,
    @SerializedName("started_books")
    val startedBooks: List<String>,
    @SerializedName("finished_books")
    val finishedBooks: List<String>,
    @SerializedName("book_records")
    val bookRecords: List<BookRecordData>
)

data class BookRecordData(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("date")
    val date: LocalDate,
    @SerializedName("book_id")
    val bookId: String,
    @SerializedName("sessions")
    val sessions: Int,
    @SerializedName("total_time")
    val totalTime: Int,
    @SerializedName("first_seen")
    val firstSeen: LocalTime,
    @SerializedName("last_seen")
    val lastSeen: LocalTime
)

fun ReadingStatisticsEntity.toDailyStatsData(bookRecords: List<BookRecordEntity>): DailyReadingStats {
        return DailyReadingStats(
        date = this.date,
        readingTimeCount = this.readingTimeCount,
        foregroundTime = this.foregroundTime,
        favoriteBooks = this.favoriteBooks,
        startedBooks = this.startedBooks,
        finishedBooks = this.finishedBooks,
        bookRecords = bookRecords.map {
            BookRecordData(
                id = it.id,
                date = it.date,
                bookId = it.bookId,
                sessions = it.sessions,
                totalTime = it.totalTime,
                firstSeen = it.firstSeen,
                lastSeen = it.lastSeen
            )
        }
    )
}

fun DailyReadingStats.toEntity(): ReadingStatisticsEntity {
    return ReadingStatisticsEntity(
        date = this.date,
        readingTimeCount = this.readingTimeCount,
        foregroundTime = this.foregroundTime,
        favoriteBooks = this.favoriteBooks,
        startedBooks = this.startedBooks,
        finishedBooks = this.finishedBooks
    )
}

fun BookRecordData.toEntity(): BookRecordEntity {
    return BookRecordEntity(
        id = this.id,
        date = this.date,
        bookId = this.bookId,
        sessions = this.sessions,
        totalTime = this.totalTime,
        firstSeen = this.firstSeen,
        lastSeen = this.lastSeen
    )
}
