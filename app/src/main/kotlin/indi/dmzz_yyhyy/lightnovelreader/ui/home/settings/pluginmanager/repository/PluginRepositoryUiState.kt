package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.repository

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginMetadata

@Stable
interface PluginRepositoryUiState {
    val isLoading: Boolean
    val errorMessage: String?
    val pluginMetadataList: List<PluginMetadata>
    val progressMap: SnapshotStateMap<String, Float?>
    val queue: List<String>
    val currentInstalling: String?
    val indexList: List<RepoIndexEntry>
    val metadataLoadingStates: Map<String, Boolean>
}

class MutablePluginRepositoryUiState : PluginRepositoryUiState {
    override var isLoading: Boolean by mutableStateOf(false)
    override var errorMessage: String? by mutableStateOf(null)
    override var pluginMetadataList: List<PluginMetadata> by mutableStateOf(emptyList())
    override val progressMap: SnapshotStateMap<String, Float?> = mutableStateMapOf()
    override var queue: List<String> by mutableStateOf(emptyList())
    override var currentInstalling: String? by mutableStateOf(null)
    override var indexList: List<RepoIndexEntry> by mutableStateOf(emptyList())
    override val metadataLoadingStates: SnapshotStateMap<String, Boolean> = mutableStateMapOf()
}