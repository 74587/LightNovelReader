package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.utils.AdvancedPrioritySemaphore
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.search.SearchProvider

class AdvancedPrioritySemaphoreWebBookDataSource(
    val webBookDataSource: WebBookDataSource,
    val advancedPrioritySemaphore: AdvancedPrioritySemaphore,
    val priority: Int
): WebBookDataSource {
    override val id = webBookDataSource.id
    override suspend fun isOffLine() = webBookDataSource.isOffLine()
    override val offLine = webBookDataSource.offLine
    override val isOffLineFlow = webBookDataSource.isOffLineFlow
    override val searchProvider: SearchProvider = webBookDataSource.searchProvider
    override val explorePageProvider: ExplorePageProvider = webBookDataSource.explorePageProvider

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