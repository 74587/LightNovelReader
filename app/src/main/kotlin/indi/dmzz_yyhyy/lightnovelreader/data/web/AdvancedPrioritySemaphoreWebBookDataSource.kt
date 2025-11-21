package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.utils.AdvancedPrioritySemaphore
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import kotlinx.coroutines.flow.Flow

class AdvancedPrioritySemaphoreWebBookDataSource(
    val webBookDataSource: WebBookDataSource,
    val advancedPrioritySemaphore: AdvancedPrioritySemaphore,
    val priority: Int
): WebBookDataSource {
    override val id = webBookDataSource.id
    override suspend fun isOffLine() = webBookDataSource.isOffLine()
    override val offLine = webBookDataSource.offLine
    override val isOffLineFlow = webBookDataSource.isOffLineFlow
    override val explorePageIdList = webBookDataSource.explorePageIdList
    override val explorePageDataSourceMap = webBookDataSource.explorePageDataSourceMap
    override val exploreExpandedPageDataSourceMap = webBookDataSource.exploreExpandedPageDataSourceMap
    override val searchTypeMap = webBookDataSource.searchTypeMap
    override val searchTipMap = webBookDataSource.searchTipMap
    override val searchTypeIdList = webBookDataSource.searchTypeIdList
    override fun search(searchType: String, keyword: String): Flow<List<BookInformation>> = webBookDataSource.search(searchType, keyword)
    override fun stopAllSearch() = webBookDataSource.stopAllSearch()

    override suspend fun getBookInformation(id: String): BookInformation {
        advancedPrioritySemaphore.acquire(priority)
        val result = webBookDataSource.getBookInformation(id)
        advancedPrioritySemaphore.release()
        return result
    }

    override suspend fun getBookVolumes(id: String): BookVolumes {
        advancedPrioritySemaphore.acquire(priority)
        val result = webBookDataSource.getBookVolumes(id)
        advancedPrioritySemaphore.release()
        return result
    }

    override suspend fun getChapterContent(chapterId: String, bookId: String): ChapterContent {
        advancedPrioritySemaphore.acquire(priority)
        val result = webBookDataSource.getChapterContent(chapterId, bookId)
        advancedPrioritySemaphore.release()
        return result
    }
}