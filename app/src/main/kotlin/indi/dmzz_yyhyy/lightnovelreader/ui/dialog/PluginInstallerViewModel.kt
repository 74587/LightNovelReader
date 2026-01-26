package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.fold
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInstallEvent
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInstallPrompt
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInstallSource
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInstallStage
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInstaller
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInfo
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginManager
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.DeleteStepState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.InstallDecision
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.InstallStepState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.MutablePluginInstallerDialogUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginDialogMode
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginInstallInfo
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginInstallerDialogUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.UpdateStepState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.toDecisionType
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginMetadata
import jakarta.inject.Inject
import java.io.File
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup

@HiltViewModel
class PluginInstallerDialogViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val pluginManager: PluginManager,
    private val pluginInstaller: PluginInstaller
) : ViewModel() {
    val uiState: PluginInstallerDialogUiState = MutablePluginInstallerDialogUiState()

    private val _snackbarFlow = MutableSharedFlow<String>()
    val snackbarFlow = _snackbarFlow.asSharedFlow()

    private val json = Json { ignoreUnknownKeys = true }
    private var currentOperation: Job? = null
    private var pendingUserDecision: CompletableDeferred<Boolean>? = null

    fun setSource(source: String) {
        viewModelScope.launch {
            currentOperation?.cancelAndJoin()
            pendingUserDecision?.complete(false)
            pendingUserDecision = null
            when (val request = parseRequest(source)) {
                is PluginInstallerRequest.Install -> startInstallation(request.source)
                is PluginInstallerRequest.Uninstall -> startUninstallation(request.pluginId)
                is PluginInstallerRequest.UpdateCheck -> startUpdateCheck(request.pluginId)
                null -> closeDialog()
            }
        }
    }

    private fun parseRequest(source: String): PluginInstallerRequest? {
        if (source.isBlank()) return null
        if (source.startsWith("uninstall:")) {
            return PluginInstallerRequest.Uninstall(source.removePrefix("uninstall:"))
        }
        if (source.startsWith("content://") || source.startsWith("file://")) {
            return PluginInstallerRequest.Install(PluginInstallSource.UriSource(source.toUri()))
        }
        val file = File(source)
        if (file.exists()) {
            return PluginInstallerRequest.Install(PluginInstallSource.FileSource(file))
        }
        return if (pluginManager.getPluginInfo(source) != null) {
            PluginInstallerRequest.UpdateCheck(source)
        } else {
            null
        }
    }

    private fun startInstallation(source: PluginInstallSource) {
        uiState.mode = PluginDialogMode.Install
        uiState.installStage = PluginInstallStage.Preparing
        uiState.installMessage = context.getString(R.string.plugin_install_preparing)
        uiState.installProgress = null
        uiState.installInfo = null
        uiState.installDecision = null
        uiState.installStep = InstallStepState.Working
        uiState.installCompletedSuccess = false
        uiState.installCompletedMessage = ""

        currentOperation = viewModelScope.launch {
            try {
                val result = pluginInstaller.installFromSource(
                    source = source,
                    onEvent = ::handleInstallEvent,
                    onConfirm = ::awaitUserConfirm
                )
                result.fold(
                    success = {
                        uiState.installStep = InstallStepState.Completed
                        uiState.installProgress = null
                        uiState.installCompletedSuccess = true
                        uiState.installCompletedMessage = context.getString(R.string.plugin_install_completed)
                    },
                    failure = { message ->
                        uiState.installStep = InstallStepState.Completed
                        uiState.installProgress = null
                        uiState.installCompletedSuccess = false
                        uiState.installCompletedMessage = message
                    }
                )
            } catch (_: CancellationException) {
            } catch (t: Throwable) {
                val reason = t.message ?: context.getString(R.string.unspecified)
                uiState.installStep = InstallStepState.Completed
                uiState.installProgress = null
                uiState.installCompletedSuccess = false
                uiState.installCompletedMessage =
                    context.getString(R.string.plugin_install_failed_with_reason, reason)
            }
        }
    }

    private fun handleInstallEvent(event: PluginInstallEvent) {
        when (event) {
            is PluginInstallEvent.StageChanged -> {
                uiState.installStep = InstallStepState.Working
                uiState.installDecision = null
                uiState.installStage = event.stage
                uiState.installProgress = null
                uiState.installMessage = when (event.stage) {
                    PluginInstallStage.Preparing -> context.getString(R.string.plugin_install_preparing)
                    PluginInstallStage.Copying -> context.getString(R.string.plugin_install_reading)
                    PluginInstallStage.Parsing -> context.getString(R.string.plugin_install_parsing)
                    PluginInstallStage.Verifying -> context.getString(R.string.plugin_install_verifying)
                    PluginInstallStage.Installing -> context.getString(R.string.plugin_install_installing)
                }
            }
            is PluginInstallEvent.Progress -> {
                uiState.installProgress = event.progress
            }

            is PluginInstallEvent.Metadata -> {
                val name = event.annotation.name.takeIf { it.isNotBlank() } ?: event.pluginId
                uiState.installInfo = PluginInstallInfo(
                    packageName = event.pluginId,
                    name = name,
                    versionName = event.annotation.versionName
                )
            }
        }
    }

    private suspend fun awaitUserConfirm(prompt: PluginInstallPrompt): Boolean {
        pendingUserDecision = CompletableDeferred()
        uiState.installStep = InstallStepState.AwaitingDecision
        uiState.installDecision = InstallDecision(
            type = prompt.type.toDecisionType(),
            message = prompt.message
        )
        uiState.installProgress = null
        return pendingUserDecision?.await() ?: false
    }

    fun respondUserDecision(continueOperation: Boolean) {
        pendingUserDecision?.complete(continueOperation)
        pendingUserDecision = null
        if (!continueOperation) {
            closeDialog()
            return
        }

        when (uiState.mode) {
            PluginDialogMode.Install -> {
                uiState.installStep = InstallStepState.Working
                uiState.installDecision = null
                uiState.installStage = PluginInstallStage.Verifying
                uiState.installMessage = context.getString(R.string.plugin_install_continue)
                uiState.installProgress = null
            }

            PluginDialogMode.UpdateCheck -> {
                uiState.updateStep = UpdateStepState.Checking
                uiState.updateMessage = context.getString(R.string.plugin_update_preparing)
                uiState.updateDownloadProgress = null
            }
            else -> Unit
        }
    }

    private fun startUninstallation(pluginId: String) {
        val info = pluginManager.getPluginInfo(pluginId) ?: run {
            closeDialog()
            return
        }

        uiState.mode = PluginDialogMode.Uninstall
        uiState.uninstallPluginId = pluginId
        uiState.uninstallPluginName = info.name
        uiState.uninstallStep = DeleteStepState.Working
        uiState.uninstallMessage = context.getString(R.string.plugin_delete_deleting)
        uiState.uninstallCompletedSuccess = false
        uiState.uninstallCompletedMessage = ""

        currentOperation = viewModelScope.launch(Dispatchers.IO) {
            performUninstallation(pluginId)
        }
    }

    private fun performUninstallation(pluginId: String) {
        try {
            pluginManager.deletePlugin(pluginId)
            uiState.uninstallStep = DeleteStepState.Completed
            uiState.uninstallCompletedSuccess = true
            uiState.uninstallCompletedMessage = context.getString(R.string.plugin_delete_completed)
        } catch (t: Throwable) {
            val reason = t.message ?: context.getString(R.string.unspecified)
            uiState.uninstallStep = DeleteStepState.Completed
            uiState.uninstallCompletedSuccess = false
            uiState.uninstallCompletedMessage =
                context.getString(R.string.plugin_delete_failed_with_reason, reason)
        }
    }

    private fun startUpdateCheck(pluginId: String) {
        val info = pluginManager.getPluginInfo(pluginId) ?: run {
            closeDialog()
            return
        }

        uiState.mode = PluginDialogMode.UpdateCheck
        uiState.updatePluginId = pluginId
        uiState.updatePluginName = info.name
        uiState.updateStep = UpdateStepState.Checking
        uiState.updateMessage = context.getString(R.string.plugin_update_checking)
        uiState.updateDownloadProgress = null

        currentOperation = viewModelScope.launch {
            checkUpdateInfo(info)
        }
    }

    private suspend fun checkUpdateInfo(pluginInfo: PluginInfo) {
        val updateUrlDir = pluginInfo.updateUrl?.trimEnd('/') ?: run {
            uiState.updateStep = UpdateStepState.Error
            uiState.updateMessage = context.getString(R.string.plugin_update_url_missing)
            return
        }
        val metadataUrl = "$updateUrlDir/metadata.json"
        val remoteMeta: PluginMetadata = try {
            val body = withContext(Dispatchers.IO) {
                Jsoup.connect(metadataUrl).ignoreContentType(true).timeout(10_000).execute().body()
            }
            json.decodeFromString(PluginMetadata.serializer(), body)
        } catch (t: Throwable) {
            val reason = t.message ?: context.getString(R.string.unspecified)
            uiState.updateStep = UpdateStepState.Error
            uiState.updateMessage =
                context.getString(R.string.plugin_update_check_failed_with_reason, reason)
            return
        }

        if (remoteMeta.version <= pluginInfo.version) {
            uiState.updateStep = UpdateStepState.Latest
            uiState.updateMessage = context.getString(R.string.plugin_update_latest)
            return
        }

        uiState.updateStep = UpdateStepState.Available
        uiState.updateMessage = context.getString(
            R.string.plugin_update_available_with_version,
            remoteMeta.versionName,
            remoteMeta.version
        )

        pendingUserDecision = CompletableDeferred()
        val ok = try {
            pendingUserDecision?.await()
        } catch (_: Throwable) {
            false
        } finally {
            pendingUserDecision = null
        }
        if (ok != true) {
            closeDialog()
            return
        }
        closeDialog()
    }

    fun onCancelOperation() {
        viewModelScope.launch {
            pendingUserDecision?.complete(false)
            pendingUserDecision = null
            currentOperation?.cancelAndJoin()
            closeDialog()
        }
    }

    fun onCloseDialog() {
        viewModelScope.launch {
            pendingUserDecision?.complete(false)
            pendingUserDecision = null
            currentOperation?.cancelAndJoin()
            closeDialog()
        }
    }

    private fun closeDialog() {
        uiState.mode = PluginDialogMode.Hidden
        uiState.installDecision = null
        uiState.installProgress = null
        uiState.updateDownloadProgress = null
        uiState.closeSignal += 1
    }

    private sealed interface PluginInstallerRequest {
        data class Install(val source: PluginInstallSource) : PluginInstallerRequest
        data class Uninstall(val pluginId: String) : PluginInstallerRequest
        data class UpdateCheck(val pluginId: String) : PluginInstallerRequest
    }
}
