package indi.dmzz_yyhyy.lightnovelreader.data.book

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.json.AppUserDataContent
import indi.dmzz_yyhyy.lightnovelreader.data.json.BookUserData
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.work.CacheBookWork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(
    private val webBookDataSource: WebBookDataSource,
    private val localBookDataSource: LocalBookDataSource,
    private val bookshelfRepository: BookshelfRepository,
    private val textProcessingRepository: TextProcessingRepository,
    private val workManager: WorkManager
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private val bookCacheProgressFlowMap: MutableMap<Int, SharedFlow<Int>> = mutableMapOf()

    suspend fun getBookInformation(id: Int): Flow<BookInformation> {
        val bookInformation: MutableStateFlow<BookInformation> =
            MutableStateFlow(localBookDataSource.getBookInformation(id) ?: BookInformation.empty())
        coroutineScope.launch {
            webBookDataSource.getBookInformation(id)?.let { information ->
                localBookDataSource.updateBookInformation(information)
                localBookDataSource.getBookInformation(id)?.let { newInfo ->
                    bookInformation.update { newInfo }
                    bookshelfRepository.getBookshelfBookMetadata(information.id)?.let { bookshelfBookMetadata ->
                        if (bookshelfBookMetadata.lastUpdate.isBefore(information.lastUpdated))
                            bookshelfBookMetadata.bookShelfIds.forEach {
                                bookshelfRepository.updateBookshelfBookMetadataLastUpdateTime(information.id, information.lastUpdated)
                                bookshelfRepository.addUpdatedBooksIntoBookShelf(it, id)
                            }
                    }
                }
            }
        }
        return bookInformation
    }

    suspend fun getBookVolumes(id: Int): Flow<BookVolumes> {
        val bookVolumes: MutableStateFlow<BookVolumes> =
            MutableStateFlow(localBookDataSource.getBookVolumes(id) ?: BookVolumes.empty())
        coroutineScope.launch {
            webBookDataSource.getBookVolumes(id)?.let { newBookVolumes ->
                localBookDataSource.updateBookVolumes(id, newBookVolumes)
                bookVolumes.update {
                    newBookVolumes
                }
            }
        }
        return bookVolumes
    }

    suspend fun getChapterContent(chapterId: Int, bookId: Int): Flow<ChapterContent> {
        val chapterContent: MutableStateFlow<ChapterContent> =
            MutableStateFlow(localBookDataSource.getChapterContent(chapterId) ?: ChapterContent.empty())
        coroutineScope.launch {
            webBookDataSource.getChapterContent(
                chapterId = chapterId,
                bookId = bookId
            )?.let { content ->
                localBookDataSource.updateChapterContent(content)
                localBookDataSource.getChapterContent(chapterId)?.let { newContent ->
                    chapterContent.update {
                        newContent.copy(
                            lastChapter = if (newContent.lastChapter == -1) it.lastChapter else newContent.lastChapter,
                            nextChapter = if (newContent.nextChapter == -1) it.nextChapter else newContent.nextChapter
                        )
                    }
                }
            }
        }
        return textProcessingRepository.processChapterContent(chapterContent)
    }

    fun getUserReadingData(bookId: Int): Flow<UserReadingData> =
        localBookDataSource.getUserReadingData(bookId).map { it }

    fun getAllUserReadingData(): List<UserReadingData> =
        localBookDataSource.getAllUserReadingData()

    fun updateUserReadingData(id: Int, update: (UserReadingData) -> UserReadingData) {
        localBookDataSource.updateUserReadingData(id, update)
    }

    fun isCacheBookWorkFlow(workId: UUID) = workManager.getWorkInfoByIdFlow(workId)

    fun importUserReadingData(data: AppUserDataContent): Boolean {
        val userReadingDataList: List<BookUserData> = data.bookUserData ?: return false
        userReadingDataList.forEach { bookUserData ->
            localBookDataSource.updateUserReadingData(bookUserData.id) {
                UserReadingData(
                    id = bookUserData.id,
                    lastReadTime = if (bookUserData.lastReadTime.isAfter(it.lastReadTime)) bookUserData.lastReadTime else it.lastReadTime,
                    totalReadTime = if (bookUserData.totalReadTime > it.totalReadTime) bookUserData.totalReadTime else it.totalReadTime,
                    readingProgress = if (bookUserData.readingProgress > it.readingProgress) bookUserData.readingProgress else it.readingProgress,
                    lastReadChapterId = bookUserData.lastReadChapterId,
                    lastReadChapterTitle = bookUserData.lastReadChapterTitle,
                    lastReadChapterProgress = bookUserData.lastReadChapterProgress,
                    readCompletedChapterIds = (bookUserData.readCompletedChapterIds + it.readCompletedChapterIds).distinct()
                )
            }
        }
        return true
    }

    fun cacheBook(bookId: Int): OneTimeWorkRequest {
        val workRequest = OneTimeWorkRequestBuilder<CacheBookWork>()
            .setInputData(workDataOf(
                "bookId" to bookId
            ))
            .build()
        workManager.enqueueUniqueWork(
            bookId.toString(),
            ExistingWorkPolicy.KEEP,
            workRequest
        )
        return workRequest
    }

    suspend fun getIsBookCached(bookId: Int): Boolean {
        localBookDataSource.getBookVolumes(bookId)?.let { bookVolumes ->
            if (bookVolumes.volumes.isEmpty())
                return false
            bookVolumes.volumes.forEach { bookVolume ->
                bookVolume.chapters.forEach {
                    if (!localBookDataSource.isChapterContentExists(it.id))
                        return false
                }
            }
        } ?: return false
        return true
    }

    fun addCacheBookProgressFlow(bookId: Int, progressFlow: SharedFlow<Int>) = bookCacheProgressFlowMap.put(bookId, progressFlow)
    fun getCacheBookProgressFlow(bookId: Int) = bookCacheProgressFlowMap[bookId]
    fun clearCacheBookProgressFlow(bookId: Int) = bookCacheProgressFlowMap.remove(bookId)
}