package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInfo
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginManager
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginSource
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PluginManagerViewModel @Inject constructor(
    val pluginManager: PluginManager,
    val userDataRepository: UserDataRepository,
) : ViewModel() {

    private val enabledPluginUserData =
        userDataRepository.stringListUserData(UserDataPath.Plugin.EnabledPlugins.path)
    val enabledPluginFlow = enabledPluginUserData.getFlowWithDefault(emptyList())
    private val enabledPluginPackagesUserData =
        userDataRepository.stringListUserData(UserDataPath.Plugin.EnabledPluginPackages.path)
    val enabledPluginPackagesFlow = enabledPluginPackagesUserData.getFlowWithDefault(emptyList())

    val errorPluginUserData =
        userDataRepository.stringListUserData(UserDataPath.Plugin.ErrorPlugins.path)
    val errorPluginIdsFlow = errorPluginUserData.getFlowWithDefault(emptyList())
        .map { entries ->
            entries.mapNotNull { entry ->
                when {
                    entry.startsWith("id:") -> entry.removePrefix("id:")
                    entry.contains('/') || entry.contains('\\') -> null
                    entry.isNotBlank() -> entry
                    else -> null
                }
            }.toSet()
        }

    val pluginList = pluginManager.allPluginInfo
    val scannedPluginApps = pluginManager.scannedPluginApps

    private val _snackbarFlow = MutableSharedFlow<String>()
    val snackbarFlow = _snackbarFlow.asSharedFlow()

    fun onClickEnabledSwitch(pluginInfo: PluginInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            when (pluginInfo.source) {
                PluginSource.InstalledApp -> {
                    val packageName = pluginInfo.packageName ?: return@launch
                    val current = enabledPluginPackagesUserData.getOrDefault(emptyList())
                    val list = current.toMutableList()
                    val enable = !current.contains(packageName)
                    if (enable) {
                        list.add(packageName)
                        enabledPluginPackagesUserData.set(list)
                        pluginManager.clearPluginError(pluginInfo.id)
                        runCatching { pluginManager.loadPlugin(pluginInfo.id) }
                    } else {
                        list.remove(packageName)
                        enabledPluginPackagesUserData.set(list)
                        runCatching { pluginManager.unloadPlugin(pluginInfo.id) }
                    }
                }
                PluginSource.LocalPackage -> {
                    val id = pluginInfo.id
                    val current = enabledPluginUserData.getOrDefault(emptyList())
                    val list = current.toMutableList()
                    val enable = !current.contains(id)
                    if (enable) {
                        list.add(id)
                        enabledPluginUserData.set(list)
                        pluginManager.clearPluginError(id)
                        runCatching { pluginManager.loadPlugin(id) }
                    } else {
                        list.remove(id)
                        enabledPluginUserData.set(list)
                        runCatching { pluginManager.unloadPlugin(id) }
                    }
                }
            }
        }
    }

    fun showSnackbar(message: String) {
        viewModelScope.launch(Dispatchers.Main) { _snackbarFlow.emit(message) }
    }

    @Composable
    fun PluginContent(id: String, paddingValues: PaddingValues) {
        pluginManager.PluginContent(id, paddingValues)
    }
}
