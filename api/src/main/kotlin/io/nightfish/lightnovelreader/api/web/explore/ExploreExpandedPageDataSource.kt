package io.nightfish.lightnovelreader.api.web.explore

import io.nightfish.lightnovelreader.api.web.SearchResult
import io.nightfish.lightnovelreader.api.web.explore.filter.Filter
import kotlinx.coroutines.flow.Flow

interface ExploreExpandedPageDataSource {
    val title: String
    val filters: List<Filter<*>>
    fun loadMore()

    /**
     * 探索扩展页的数据流, 需要提供SearchResult
     */
    fun getResultFlow(): Flow<SearchResult>
}