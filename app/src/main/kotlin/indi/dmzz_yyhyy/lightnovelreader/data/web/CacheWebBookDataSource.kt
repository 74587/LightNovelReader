package indi.dmzz_yyhyy.lightnovelreader.data.web

import io.nightfish.lightnovelreader.api.book.CanBeEmpty
import io.nightfish.lightnovelreader.api.util.Cache
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.search.SearchProvider

class CacheWebBookDataSource(
    override val cache: Cache,
    val webBookDataSource: WebBookDataSource
): WebBookDataSource {
    private inline fun <reified T: CanBeEmpty> ifCache(id: String, block: () -> T): T {
        val cacheData = cache.getCache<T>(id.hashCode())
        if (cacheData == null) {
            val data = block.invoke()
            if (data.isEmpty()) return data
            cache.cache(id.hashCode(), data)
            return data
        }
        return cacheData
    }
    override val id = webBookDataSource.id

    override suspend fun isOffLine() = webBookDataSource.isOffLine()

    override val offLine = webBookDataSource.offLine
    override val isOffLineFlow = webBookDataSource.isOffLineFlow
    override val explorePageProvider: ExplorePageProvider = webBookDataSource.explorePageProvider
    override val searchProvider: SearchProvider = webBookDataSource.searchProvider

    override suspend fun getBookInformation(id: String) = ifCache(id) {
        webBookDataSource.getBookInformation(id)
    }

    override suspend fun getBookVolumes(id: String) = ifCache(id) {
        webBookDataSource.getBookVolumes(id)
    }

    override suspend fun getChapterContent(chapterId: String, bookId: String) = ifCache(chapterId + bookId) {
        webBookDataSource.getChapterContent(chapterId, bookId)
    }
}