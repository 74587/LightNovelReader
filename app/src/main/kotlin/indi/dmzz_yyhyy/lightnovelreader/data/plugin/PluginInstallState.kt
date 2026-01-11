package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import android.net.Uri
import io.nightfish.lightnovelreader.api.plugin.Plugin
import java.io.File

sealed interface PluginInstallSource {
    data class UriSource(val uri: Uri) : PluginInstallSource
    data class FileSource(val file: File) : PluginInstallSource
}

sealed interface PluginInstallEvent {
    data object Preparing : PluginInstallEvent
    data class Copying(val progress: Float?) : PluginInstallEvent
    data object Parsing : PluginInstallEvent
    data object Verifying : PluginInstallEvent
    data class Metadata(val pluginId: String, val annotation: Plugin) : PluginInstallEvent
    data object Installing : PluginInstallEvent
    data class Success(val message: String) : PluginInstallEvent
    data class Failure(val message: String) : PluginInstallEvent
}

data class PluginInstallPrompt(
    val type: PluginInstallPromptType,
    val message: String
)

enum class PluginInstallPromptType {
    Reinstall,
    Downgrade
}
