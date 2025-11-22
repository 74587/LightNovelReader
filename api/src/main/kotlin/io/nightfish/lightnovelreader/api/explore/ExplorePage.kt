package io.nightfish.lightnovelreader.api.explore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class ExplorePage(
    val title: String,
    val rows: Flow<List<ExploreBooksRow>>
) {
    companion object {
        fun empty() = ExplorePage(
            "",
            flowOf(emptyList())
        )
    }
}
