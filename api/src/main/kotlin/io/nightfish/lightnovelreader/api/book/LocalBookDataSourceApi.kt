package io.nightfish.lightnovelreader.api.book

import kotlinx.coroutines.flow.Flow

interface LocalBookDataSourceApi {
    suspend fun getBookInformation(id: String): BookInformation?
    fun updateBookInformation(info: BookInformation)
    suspend fun getBookVolumes(id: String): BookVolumes?
    fun updateBookVolumes(bookId: String, bookVolumes: BookVolumes)
    suspend fun getChapterContent(id: String): MutableChapterContent?
    fun updateChapterContent(chapterContent: ChapterContent)
    fun getUserReadingData(id: String): MutableUserReadingData
    fun getUserReadingDataFlow(id: String): Flow<MutableUserReadingData>
    fun updateUserReadingData(id: String, update: (MutableUserReadingData) -> UserReadingData)
    fun getAllUserReadingData(): List<UserReadingData>
    suspend fun isChapterContentExists(id: String): Boolean
    fun clear()
}