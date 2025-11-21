package indi.dmzz_yyhyy.lightnovelreader.data.json

import com.google.gson.annotations.SerializedName
import io.nightfish.lightnovelreader.api.book.UserReadingData
import java.time.LocalDateTime

data class BookUserData(
    @SerializedName("id")
    val id: String,
    @SerializedName("last_read_time")
    val lastReadTime: LocalDateTime,
    @SerializedName("total_read_time")
    val totalReadTime: Int,
    @SerializedName("reading_progress")
    val readingProgress: Float,
    @SerializedName("last_read_chapter_id")
    val lastReadChapterId: String,
    @SerializedName("last_read_chapter_title")
    val lastReadChapterTitle: String,
    @SerializedName("last_read_chapter_progress")
    val lastReadChapterProgress: Float,
    @SerializedName("read_completed_chapter_ids")
    val readCompletedChapterIds: List<String>,
)

fun UserReadingData.toJsonData() =
    BookUserData(
        id = this.id,
        lastReadTime = this.lastReadTime,
        totalReadTime = this.totalReadTime,
        readingProgress = this.readingProgress,
        lastReadChapterId = this.lastReadChapterId,
        lastReadChapterTitle = this.lastReadChapterTitle,
        lastReadChapterProgress = this.lastReadChapterProgress,
        readCompletedChapterIds = this.readCompletedChapterIds,
    )