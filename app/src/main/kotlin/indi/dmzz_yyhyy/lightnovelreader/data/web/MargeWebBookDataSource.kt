package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.utils.RequestMarge
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import kotlinx.coroutines.flow.Flow

class MargeWebBookDataSource(
    val webBookDataSource: WebBookDataSource,
    val requestMarge: RequestMarge = RequestMarge()
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
    override fun search(searchType: String, keyword: String): Flow<BookInformation> = webBookDataSource.search(searchType, keyword)
    override fun stopAllSearch() = webBookDataSource.stopAllSearch()

    override suspend fun getBookInformation(id: String) = requestMarge.margeRequest(id.hashCode()) {
        webBookDataSource.getBookInformation(id)
    }

    override suspend fun getBookVolumes(id: String) = requestMarge.margeRequest(id.hashCode()) {
        webBookDataSource.getBookVolumes(id)
    }

    override suspend fun getChapterContent(chapterId: String, bookId: String) = requestMarge.margeRequest((chapterId + bookId).hashCode()) {
        webBookDataSource.getChapterContent(chapterId, bookId)
    }
}