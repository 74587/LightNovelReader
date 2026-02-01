package io.nightfish.lightnovelreader.api.book

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
    val chapterReadingProgressMap: Map<String, Float>

    override fun isEmpty(): Boolean = id.isEmpty()

    companion object {
        fun empty(): UserReadingData = MutableUserReadingData(
            "",
            LocalDateTime.MIN,
            -1,
            0.0f,
            "",
            "",
            emptyMap()
        )
    }

    fun toMutable(): MutableUserReadingData {
        if (this is MutableUserReadingData)
            return this
        return MutableUserReadingData(id, lastReadTime, totalReadTime, readingProgress, lastReadChapterId, lastReadChapterTitle, chapterReadingProgressMap)
    }
}

class MutableUserReadingData(
    id: String,
    lastReadTime: LocalDateTime,
    totalReadTime: Int,
    readingProgress: Float,
    lastReadChapterId: String,
    lastReadChapterTitle: String,
    chapterReadingProgressMap: Map<String, Float>
): UserReadingData {
    override var id by mutableStateOf(id)
    override var lastReadTime by mutableStateOf(lastReadTime)
    override var totalReadTime by mutableIntStateOf(totalReadTime)
    override var readingProgress by mutableFloatStateOf(readingProgress)
    override var lastReadChapterId by mutableStateOf(lastReadChapterId)
    override var lastReadChapterTitle by mutableStateOf(lastReadChapterTitle)
    override val chapterReadingProgressMap = mutableStateMapOf(*chapterReadingProgressMap.map { Pair(it.key, it.value) }.toTypedArray())
    
    companion object {
        fun empty(): MutableUserReadingData = MutableUserReadingData(
            "",
            LocalDateTime.MIN,
            -1,
            0.0f,
            "",
            "",
            emptyMap()
        )
    }
    
    fun update(userReadingData: UserReadingData) {
        this.id = userReadingData.id
        this.lastReadTime = userReadingData.lastReadTime
        this.totalReadTime = userReadingData.totalReadTime
        this.readingProgress = userReadingData.readingProgress
        this.lastReadChapterId = userReadingData.lastReadChapterId
        this.lastReadChapterTitle = userReadingData.lastReadChapterTitle
        this.chapterReadingProgressMap.clear()
        this.chapterReadingProgressMap.putAll(userReadingData.chapterReadingProgressMap)
    }

    fun updateChapterReadingProgress(chapterId: String, progress: Float) {
        val progress = progress.coerceAtLeast(this.chapterReadingProgressMap[chapterId] ?: 0f)
        this.chapterReadingProgressMap[chapterId] = progress
    }
}
