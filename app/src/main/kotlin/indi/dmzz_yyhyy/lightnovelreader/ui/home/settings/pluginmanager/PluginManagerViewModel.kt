package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginManager
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.FileInputStream

@HiltViewModel
class PluginManagerViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    val pluginManager: PluginManager,
    val userDataRepository: UserDataRepository,
) : ViewModel() {

    private val enabledPluginUserData =
        userDataRepository.stringListUserData(UserDataPath.Plugin.EnabledPlugins.path)
    val enabledPluginFlow = enabledPluginUserData.getFlowWithDefault(emptyList())

    val errorPluginUserData =
        userDataRepository.stringListUserData(UserDataPath.Plugin.ErrorPlugins.path)

    val pluginList = pluginManager.allPluginInfo

    private val _snackbarFlow = MutableSharedFlow<String>()
    val snackbarFlow = _snackbarFlow.asSharedFlow()

    fun onClickEnabledSwitch(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            enabledPluginUserData.update { current ->
                val list = current.toMutableList()
                if (current.contains(id)) {
                    list.remove(id)
                    runCatching { pluginManager.unloadPlugin(id) }
                } else {
                    list.add(id)
                    runCatching { pluginManager.loadPlugin(id) }
                }
                list
            }
        }
    }

    fun installPlugin(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val pluginDir = pluginManager.getPluginDir(uri.hashCode().toString())
                .also { it.mkdirs() }
            val pluginFile = pluginManager.getPluginFile(pluginDir)
                .also { it.parentFile?.mkdirs() }

            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                FileInputStream(pfd.fileDescriptor).use { inStream ->
                    pluginFile.outputStream().use { out -> inStream.copyTo(out) }
                }
            }

            errorPluginUserData.update { it + pluginFile.path }

            val id = pluginManager.loadPlugin(pluginFile, forceLoad = true)
            if (id == null) {
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "插件加载失败，请检查文件合法性", Toast.LENGTH_SHORT).show()
                }
                pluginFile.delete()
                return@launch
            }

            errorPluginUserData.update { it.toMutableList().apply { remove(pluginFile.path) } }
            enabledPluginUserData.update { it.toMutableList().apply { if (!contains(id)) add(id) } }
        }
    }

    fun deletePlugin(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            pluginManager.deletePlugin(id)
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
