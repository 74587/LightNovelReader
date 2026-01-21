package io.nightfish.lightnovelreader.api.web.explore

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import kotlinx.coroutines.CoroutineScope

sealed interface ExplorePageProvider {
    /**
     * 软件类默认实现的探索页面
     */
    interface DefaultExplorePageProvider: ExplorePageProvider {
        val explorePageIdList: List<String>
        val exploreTapPageDataSourceMap: Map<String, ExploreTapPageDataSource>
        val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource>
    }

    /**
     * 自定义探索页面内容
     */
    interface CustomExplorePageProvider<T>: ExplorePageProvider {
        val uiState: T

        fun init(viewModelScope: CoroutineScope)

        @Composable
        fun Content(nestedScrollConnection: NestedScrollConnection)
    }
}