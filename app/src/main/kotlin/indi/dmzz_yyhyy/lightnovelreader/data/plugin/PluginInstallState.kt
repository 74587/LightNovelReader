package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import android.net.Uri
import io.nightfish.lightnovelreader.api.plugin.Plugin
import java.io.File

sealed interface PluginInstallSource {
    data class UriSource(val uri: Uri) : PluginInstallSource
    data class FileSource(val file: File) : PluginInstallSource
}

enum class PluginInstallStage {
    Preparing,
    Copying,
    Parsing,
    Verifying,
    Installing
}

sealed interface PluginInstallEvent {
    data class StageChanged(val stage: PluginInstallStage) : PluginInstallEvent
    data class Progress(val progress: Float?) : PluginInstallEvent
    data class Metadata(val pluginId: String, val annotation: Plugin) : PluginInstallEvent
}

data class PluginInstallPrompt(
    val type: PluginInstallPromptType,
    val message: String
)

enum class PluginInstallPromptType {
    Reinstall
}
