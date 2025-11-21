package io.nightfish.lightnovelreader.api.explore

data class ExploreBooksRow(
    val title: String,
    val bookList: List<ExploreDisplayBook>,
    val expandable: Boolean = false,
    val expandedPageDataSourceId: String? = null
)
