package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity

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