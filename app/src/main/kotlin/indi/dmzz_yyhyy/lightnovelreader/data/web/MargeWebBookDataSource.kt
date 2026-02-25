package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.utils.RequestMarge
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.search.SearchProvider

class MargeWebBookDataSource(
    val webBookDataSource: WebBookDataSource,
    val requestMarge: RequestMarge = RequestMarge()
): WebBookDataSource {
    override val id get() = webBookDataSource.id
    override suspend fun isOffLine() = webBookDataSource.isOffLine()
    override val offLine get() = webBookDataSource.offLine
    override val isOffLineFlow get() = webBookDataSource.isOffLineFlow
    override val explorePageProvider: ExplorePageProvider get() = webBookDataSource.explorePageProvider
    override val searchProvider: SearchProvider get() = webBookDataSource.searchProvider

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