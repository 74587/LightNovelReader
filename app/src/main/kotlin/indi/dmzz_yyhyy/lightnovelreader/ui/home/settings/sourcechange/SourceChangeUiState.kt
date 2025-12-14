package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.sourcechange

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.nightfish.lightnovelreader.api.web.WebDataSourceItem

interface SourceChangeUiState {
    val currentSourceId: Int
    val webDataSourceItems: List<WebDataSourceItem>
    val isProcessing: Boolean
    val lastError: String?
}

class MutableSourceChangeUiState : SourceChangeUiState {
    override var currentSourceId: Int by mutableIntStateOf(0)
    override var webDataSourceItems: List<WebDataSourceItem> by mutableStateOf(emptyList())
    override var isProcessing: Boolean by mutableStateOf(false)
    override var lastError: String? by mutableStateOf(null)
}
