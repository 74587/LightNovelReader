package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import java.time.LocalDate
import java.time.LocalTime

@TypeConverters(
    LocalDateTimeConverter::class
)

@Entity(tableName = "book_records")
data class BookRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @ColumnInfo(name = "book_id")
    val bookId: String,
    @ColumnInfo(name = "sessions")
    val sessions: Int,
    @ColumnInfo(name = "total_time")
    val totalTime: Int,
    @ColumnInfo(name = "first_seen")
    val firstSeen: LocalTime,
    @ColumnInfo(name = "last_seen")
    val lastSeen: LocalTime
)
