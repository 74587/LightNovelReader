package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import androidx.compose.runtime.Immutable

@Immutable
sealed interface PluginDialogState {
    data object Hidden : PluginDialogState
    data class Install(val state: InstallDialogState) : PluginDialogState
    data class Uninstall(val state: DeleteDialogState) : PluginDialogState
    data class UpdateCheck(val state: UpdateDialogState) : PluginDialogState
}

@Immutable
data class InstallDialogState(
    val info: PluginInstallInfo? = null,
    val step: InstallStep = InstallStep.Working(InstallStage.Preparing, ""),
    val progress: Float? = null
)

@Immutable
data class PluginInstallInfo(
    val packageName: String,
    val name: String,
    val versionName: String
)

@Immutable
sealed interface InstallStep {
    data class Working(val stage: InstallStage, val message: String) : InstallStep
    data class AwaitingDecision(val decision: InstallDecision) : InstallStep
    data class Completed(val success: Boolean, val message: String) : InstallStep
}

enum class InstallStage {
    Preparing,
    Copying,
    Parsing,
    Verifying,
    Installing
}

@Immutable
data class InstallDecision(
    val type: InstallDecisionType,
    val message: String
)

enum class InstallDecisionType {
    Reinstall,
    Downgrade,
    InvalidSignature
}

@Immutable
data class DeleteDialogState(
    val pluginId: String = "",
    val pluginName: String = "",
    val step: DeleteStep = DeleteStep.Working("")
)

@Immutable
sealed interface DeleteStep {
    data class Working(val message: String) : DeleteStep
    data class Completed(val success: Boolean, val message: String) : DeleteStep
}

@Immutable
data class UpdateDialogState(
    val pluginId: String = "",
    val pluginName: String = "",
    val step: UpdateStep = UpdateStep.Checking(""),
    val downloadProgress: Float? = null
)

@Immutable
sealed interface UpdateStep {
    data class Checking(val message: String) : UpdateStep
    data class Latest(val message: String) : UpdateStep
    data class Available(val message: String) : UpdateStep
    data class Error(val message: String) : UpdateStep
    data class Downloading(val message: String) : UpdateStep
    data class Installing(val message: String) : UpdateStep
    data class Completed(val message: String) : UpdateStep
}
