package io.nightfish.lightnovelreader.api.book

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDateTime

@Stable
interface UserReadingData: CanBeEmpty {
    val id: String
    val lastReadTime: LocalDateTime
    val totalReadTime: Int
    val readingProgress: Float
    val lastReadChapterId: String
    val lastReadChapterTitle: String
    val lastReadChapterProgress: Float
    val readCompletedChapterIds: List<String>

    override fun isEmpty(): Boolean = id.isEmpty()

    companion object {
        fun empty(): UserReadingData = MutableUserReadingData(
            "",
            LocalDateTime.MIN,
            -1,
            0.0f,
            "",
            "",
            0.0f,
            readCompletedChapterIds = emptyList()
        )
    }

    fun toMutable(): MutableUserReadingData {
        if (this is MutableUserReadingData)
            return this
        return MutableUserReadingData(id, lastReadTime, totalReadTime, readingProgress, lastReadChapterId, lastReadChapterTitle, lastReadChapterProgress, readCompletedChapterIds)
    }
}

class MutableUserReadingData(
    id: String,
    lastReadTime: LocalDateTime,
    totalReadTime: Int,
    readingProgress: Float,
    lastReadChapterId: String,
    lastReadChapterTitle: String,
    lastReadChapterProgress: Float,
    readCompletedChapterIds: List<String>
): UserReadingData {
    override var id by mutableStateOf(id)
    override var lastReadTime by mutableStateOf(lastReadTime)
    override var totalReadTime by mutableIntStateOf(totalReadTime)
    override var readingProgress by mutableFloatStateOf(readingProgress)
    override var lastReadChapterId by mutableStateOf(lastReadChapterId)
    override var lastReadChapterTitle by mutableStateOf(lastReadChapterTitle)
    override var lastReadChapterProgress by mutableFloatStateOf(lastReadChapterProgress)
    override var readCompletedChapterIds = mutableStateListOf<String>().apply { addAll(readCompletedChapterIds) }
    
    companion object {
        fun empty(): MutableUserReadingData = MutableUserReadingData(
            "",
            LocalDateTime.MIN,
            -1,
            0.0f,
            "",
            "",
            0.0f,
            readCompletedChapterIds = emptyList()
        )
    }
    
    fun update(userReadingData: UserReadingData) {
        this.id = userReadingData.id
        this.lastReadTime = userReadingData.lastReadTime
        this.totalReadTime = userReadingData.totalReadTime
        this.readingProgress = userReadingData.readingProgress
        this.lastReadChapterId = userReadingData.lastReadChapterId
        this.lastReadChapterTitle = userReadingData.lastReadChapterTitle
        this.lastReadChapterProgress = userReadingData.lastReadChapterProgress
        this.readCompletedChapterIds.clear()
        this.readCompletedChapterIds.addAll(userReadingData.readCompletedChapterIds)
    }
}
