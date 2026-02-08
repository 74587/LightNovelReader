package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.CountConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.serialier.CountSerializer
import indi.dmzz_yyhyy.lightnovelreader.data.serialier.LocalDateSerializer
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.Count
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
@TypeConverters(
    LocalDateTimeConverter::class,
    CountConverter::class,
    ListConverter::class
)
@Entity(tableName = "reading_statistics")
data class ReadingStatisticsEntity(
    @Serializable(LocalDateSerializer::class)
    @PrimaryKey
    val date: LocalDate,
    @Serializable(CountSerializer::class)
    @ColumnInfo(name = "reading_time_count")
    val readingTimeCount: Count,
    @ColumnInfo(name = "foreground_time")
    val foregroundTime: Int,
    @ColumnInfo(name = "favorite_books")
    val favoriteBooks: List<String>,
    @ColumnInfo(name = "started_books")
    val startedBooks: List<String>,
    @ColumnInfo(name = "finished_books")
    val finishedBooks: List<String>
): Mergeable<ReadingStatisticsEntity> {
    override fun merge(
        new: ReadingStatisticsEntity
    ): ReadingStatisticsEntity =
        ReadingStatisticsEntity(
            date = new.date,
            readingTimeCount = this.readingTimeCount + new.readingTimeCount,
            foregroundTime = this.foregroundTime + new.foregroundTime,
            favoriteBooks = (this.favoriteBooks + new.favoriteBooks).distinct(),
            startedBooks = (this.startedBooks + new.startedBooks).distinct(),
            finishedBooks = (this.finishedBooks + new.finishedBooks).distinct()
        )
}
