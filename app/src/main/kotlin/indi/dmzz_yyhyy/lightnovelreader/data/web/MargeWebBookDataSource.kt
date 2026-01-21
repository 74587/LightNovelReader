package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.utils.RequestMarge
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.search.SearchProvider

class MargeWebBookDataSource(
    val webBookDataSource: WebBookDataSource,
    val requestMarge: RequestMarge = RequestMarge()
): WebBookDataSource {
    override val id = webBookDataSource.id
    override suspend fun isOffLine() = webBookDataSource.isOffLine()
    override val offLine = webBookDataSource.offLine
    override val isOffLineFlow = webBookDataSource.isOffLineFlow
    override val explorePageProvider: ExplorePageProvider = webBookDataSource.explorePageProvider
    override val searchProvider: SearchProvider = webBookDataSource.searchProvider

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