package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.utils.AdvancedPrioritySemaphore
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.search.SearchProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdvancedPrioritySemaphoreWebBookDataSource(
    val webBookDataSource: WebBookDataSource,
    val advancedPrioritySemaphore: AdvancedPrioritySemaphore,
    val priority: Int
): WebBookDataSource {
    override val id get() = webBookDataSource.id
    override suspend fun isOffLine() = webBookDataSource.isOffLine()
    override val offLine get() = webBookDataSource.offLine
    override val isOffLineFlow get() = webBookDataSource.isOffLineFlow
    override val explorePageProvider: ExplorePageProvider get() = webBookDataSource.explorePageProvider
    override val searchProvider: SearchProvider get() = webBookDataSource.searchProvider

    override suspend fun getBookInformation(id: String): BookInformation = withContext(Dispatchers.IO) {
        try {
            advancedPrioritySemaphore.acquire(priority)
            return@withContext webBookDataSource.getBookInformation(id).copy()
        } finally {
            advancedPrioritySemaphore.release()
        }
    }

    override suspend fun getBookVolumes(id: String): BookVolumes = withContext(Dispatchers.IO) {
        try {
            advancedPrioritySemaphore.acquire(priority)
            return@withContext webBookDataSource.getBookVolumes(id).copy()
        } finally {
            advancedPrioritySemaphore.release()
        }
    }

    override suspend fun getChapterContent(chapterId: String, bookId: String): ChapterContent = withContext(Dispatchers.IO) {
            try {
                advancedPrioritySemaphore.acquire(priority)
                return@withContext webBookDataSource.getChapterContent(chapterId, bookId).copy()
            } finally {
                advancedPrioritySemaphore.release()
            }
        }
}