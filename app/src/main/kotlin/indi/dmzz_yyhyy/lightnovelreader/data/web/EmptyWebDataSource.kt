package indi.dmzz_yyhyy.lightnovelreader.data.web

import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object EmptyWebDataSource: WebBookDataSource {
    override val id: Int = -1
    override suspend fun isOffLine(): Boolean = true

    override val offLine: Boolean = true
    override val isOffLineFlow: Flow<Boolean> = flow { true }
    override val explorePageIdList: List<String> = emptyList()
    override val explorePageDataSourceMap: Map<String, ExplorePageDataSource> = emptyMap()
    override val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource> = emptyMap()
    override val searchTypeMap: Map<String, String> = emptyMap()
    override val searchTipMap: Map<String, String> = emptyMap()
    override val searchTypeIdList: List<String> = emptyList()
    override suspend fun getBookInformation(id: String): BookInformation = BookInformation.empty()

    override suspend fun getBookVolumes(id: String): BookVolumes = BookVolumes.empty("")

    override suspend fun getChapterContent(chapterId: String, bookId: String): ChapterContent = ChapterContent.empty()

    override fun search(searchType: String, keyword: String): Flow<List<BookInformation>> = flow { emptyList<BookInformation>() }

    override fun stopAllSearch() {}
}