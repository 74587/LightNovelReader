package io.nightfish.lightnovelreader.api.book

import androidx.navigation.NavController
import io.nightfish.lightnovelreader.api.web.WebDataSourcePriority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface BookRepositoryApi {
    fun getStateBookInformation(
        id: String,
        coroutineScope: CoroutineScope,
        priority: WebDataSourcePriority = WebDataSourcePriority.Default
    ): BookInformation
    fun getBookInformationFlow(
        id: String,
        priority: WebDataSourcePriority = WebDataSourcePriority.Default
    ): Flow<BookInformation>
    fun getBookVolumesFlow(
        id: String,
        priority: WebDataSourcePriority = WebDataSourcePriority.Default
    ): Flow<BookVolumes>
    fun getStateChapterContent(
        chapterId: String,
        bookId: String,
        coroutineScope: CoroutineScope,
        priority: WebDataSourcePriority = WebDataSourcePriority.Default
    ): ChapterContent

    suspend fun getChapterContent(
        chapterId: String,
        bookId: String,
        priority: WebDataSourcePriority = WebDataSourcePriority.Default
    ): ChapterContent
    fun getChapterContentFlow(
        chapterId: String,
        bookId: String,
        priority: WebDataSourcePriority = WebDataSourcePriority.Default
    ): Flow<ChapterContent>

    fun getStateUserReadingData(bookId: String, coroutineScope: CoroutineScope): UserReadingData
    fun getUserReadingData(bookId: String): UserReadingData
    fun getUserReadingDataFlow(bookId: String): Flow<UserReadingData>
    fun getAllUserReadingData(): List<UserReadingData>
    fun updateUserReadingData(id: String, update: (MutableUserReadingData) -> UserReadingData)
    suspend fun getIsBookCached(bookId: String): Boolean
    fun progressBookTagClick(tag: String, navController: NavController)
}