package indi.dmzz_yyhyy.lightnovelreader.data.explore

import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import io.nightfish.lightnovelreader.api.book.BookInformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExploreRepository @Inject constructor(
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val processingRepository: TextProcessingRepository
) {
    private val searchResultCacheMap = mutableMapOf<String, List<BookInformation>>()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    val searchTypeIdList get() = webBookDataSourceProvider.default.searchTypeIdList
    val searchTypeMap get() = processingRepository.processSearchTypeNameMap { webBookDataSourceProvider.default.searchTypeMap }
    val searchTipMap get() = processingRepository.processSearchTipMap { webBookDataSourceProvider.default.searchTipMap }
    val explorePageIdList get() = webBookDataSourceProvider.default.explorePageIdList
    val explorePageDataSourceMap get() = webBookDataSourceProvider.default.explorePageDataSourceMap
    val exploreExpandedPageDataSourceMap get() = webBookDataSourceProvider.default.exploreExpandedPageDataSourceMap

    fun search(searchType: String, keyword: String): Flow<List<BookInformation>> {
        searchResultCacheMap[searchType + keyword]?.let { searchResult ->
            val flow = MutableStateFlow(emptyList<BookInformation>())
            flow.update { searchResult }
            return flow
        }
        val flow = webBookDataSourceProvider.default.search(searchType, keyword)
        coroutineScope.launch {
            flow.collect {
                if (it.isNotEmpty() && it.last().isEmpty()) {
                    searchResultCacheMap[searchType + keyword] = it
                }
            }
        }

        return flow.map { list ->
            list.map {
                processingRepository.processBookInformation { it }
            }
        }
    }

    fun stopAllSearch() = webBookDataSourceProvider.default.stopAllSearch()
}