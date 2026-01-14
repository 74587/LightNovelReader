package indi.dmzz_yyhyy.lightnovelreader.data.explore

import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import io.nightfish.lightnovelreader.api.book.BookInformation
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExploreRepository @Inject constructor(
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val processingRepository: TextProcessingRepository
) {
    val searchTypeIdList get() = webBookDataSourceProvider.default.searchTypeIdList
    val searchTypeMap get() = processingRepository.processSearchTypeNameMap { webBookDataSourceProvider.default.searchTypeMap }
    val searchTipMap get() = processingRepository.processSearchTipMap { webBookDataSourceProvider.default.searchTipMap }
    val explorePageIdList get() = webBookDataSourceProvider.default.explorePageIdList
    val explorePageDataSourceMap get() = webBookDataSourceProvider.default.explorePageDataSourceMap
    val exploreExpandedPageDataSourceMap get() = webBookDataSourceProvider.default.exploreExpandedPageDataSourceMap

    fun search(searchType: String, keyword: String): Flow<BookInformation> =
        webBookDataSourceProvider.default.search(searchType, keyword)

    fun stopAllSearch() = webBookDataSourceProvider.default.stopAllSearch()
}