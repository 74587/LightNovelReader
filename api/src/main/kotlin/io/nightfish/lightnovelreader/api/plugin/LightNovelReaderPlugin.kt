package io.nightfish.lightnovelreader.api.plugin

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder

interface LightNovelReaderPlugin {
    fun onLoad() { }
    fun onUnload() { }
    /**
     * 当软件初始化导航时调用
     * 仅当软件首次启动时有效
     */
    fun NavGraphBuilder.onBuildNavHost() {}
    @Composable
    fun PageContent(paddingValues: PaddingValues) {
    }
}