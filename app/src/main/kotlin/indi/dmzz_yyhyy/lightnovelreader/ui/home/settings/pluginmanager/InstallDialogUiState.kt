package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import androidx.compose.runtime.Immutable
import io.nightfish.lightnovelreader.api.plugin.Plugin

@Immutable
data class InstallDialogState(
    val visible: Boolean = false,
    val packageName: String = "",
    val phase: String = "",
    val finished: Boolean = false,
    val error: Boolean = false,
    val pluginAnnotation: Plugin? = null,
    val confirm: Confirm = Confirm.None
) {
    @Immutable
    sealed interface Confirm {
        data object None : Confirm
        data object InvalidSig : Confirm
        data object Upgrade : Confirm
    }
}

@Immutable
data class DeleteDialogState(
    val visible: Boolean = false,
    val pluginId: String = "",
    val pluginName: String = "",
    val phase: String = "",
    val finished: Boolean = false
)

@Immutable
data class UpdateDialogState(
    val visible: Boolean = false,
    val pluginId: String = "",
    val pluginName: String = "",
    val isChecking: Boolean = false,
    val isLatest: Boolean = false,
    val hasUpdate: Boolean = false,
    val isError: Boolean = false,
    val updateSuccess: Boolean = false,
    val message: String? = null
)