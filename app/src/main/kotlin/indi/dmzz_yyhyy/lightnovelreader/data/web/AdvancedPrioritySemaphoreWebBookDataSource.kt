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
        try {
            advancedPrioritySemaphore.acquire(priority)
            return webBookDataSource.getBookInformation(id)
        } finally {
            advancedPrioritySemaphore.release()
        }
    }

    override suspend fun getBookVolumes(id: String): BookVolumes {
        try {
            advancedPrioritySemaphore.acquire(priority)
            return webBookDataSource.getBookVolumes(id)
        } finally {
            advancedPrioritySemaphore.release()
        }
    }

    override suspend fun getChapterContent(chapterId: String, bookId: String): ChapterContent {
        try {
            advancedPrioritySemaphore.acquire(priority)
            return webBookDataSource.getChapterContent(chapterId, bookId)
        } finally {
            advancedPrioritySemaphore.release()
        }
    }
}