package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.Wenku8Api
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.CanBeEmpty
import io.nightfish.lightnovelreader.api.util.Cache
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import kotlinx.coroutines.flow.Flow

class CacheWebBookDataSource(
    override val cache: Cache,
    val webBookDataSource: WebBookDataSource
): WebBookDataSource {
    private inline fun <reified T: CanBeEmpty> ifCache(id: String, block: () -> T): T {
        val cacheData = Wenku8Api.cache.getCache<T>(id.hashCode())
        if (cacheData == null) {
            val data = block.invoke()
            if (data.isEmpty()) return data
            Wenku8Api.cache.cache(id.hashCode(), data)
            return data
        }
        return cacheData
    }
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