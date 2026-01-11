package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInstallEvent
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInstallPrompt
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInstallPromptType
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInstallSource
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInstaller
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInfo
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginManager
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.DeleteDialogState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.DeleteStep
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.InstallDecision
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.InstallDecisionType
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.InstallDialogState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.InstallStage
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.InstallStep
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginDialogState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginInstallInfo
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginMetadata
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.UpdateDialogState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.UpdateStep
import jakarta.inject.Inject
import java.io.File
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup

@HiltViewModel
class PluginInstallerDialogViewModel @Inject constructor(
    private val pluginManager: PluginManager,
    private val pluginInstaller: PluginInstaller
) : ViewModel() {
    private val _uiState = MutableStateFlow<PluginDialogState>(PluginDialogState.Hidden)
    val uiState = _uiState.asStateFlow()

    private val _snackbarFlow = MutableSharedFlow<String>()
    val snackbarFlow = _snackbarFlow.asSharedFlow()
    private val _closeDialogFlow = MutableSharedFlow<Unit>()
    val closeDialogFlow = _closeDialogFlow.asSharedFlow()

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
        _uiState.value = PluginDialogState.Install(
            InstallDialogState(step = InstallStep.Working(InstallStage.Preparing, "正在准备安装..."))
        )
        currentOperation = viewModelScope.launch {
            try {
                pluginInstaller.installFromSource(
                    source = source,
                    callbacks = PluginInstaller.InstallCallbacks(
                        onEvent = ::handleInstallEvent,
                        onConfirm = ::awaitUserConfirm
                    )
                )
            } catch (_: CancellationException) {
            } catch (t: Throwable) {
                updateInstallState {
                    it.copy(step = InstallStep.Completed(false, "安装失败：${t.message ?: "未知错误"}"))
                }
            }
        }
    }

    private fun handleInstallEvent(event: PluginInstallEvent) {
        when (event) {
            PluginInstallEvent.Preparing -> {
                updateInstallState {
                    it.copy(step = InstallStep.Working(InstallStage.Preparing, "正在准备安装..."), progress = null)
                }
            }
            is PluginInstallEvent.Copying -> {
                updateInstallState {
                    it.copy(step = InstallStep.Working(InstallStage.Copying, "正在读取插件..."), progress = event.progress)
                }
            }
            PluginInstallEvent.Parsing -> {
                updateInstallState {
                    it.copy(step = InstallStep.Working(InstallStage.Parsing, "解析插件信息中"), progress = null)
                }
            }
            PluginInstallEvent.Verifying -> {
                updateInstallState {
                    it.copy(step = InstallStep.Working(InstallStage.Verifying, "正在校验插件..."), progress = null)
                }
            }
            is PluginInstallEvent.Metadata -> {
                val name = event.annotation.name.takeIf { it.isNotBlank() } ?: event.pluginId
                updateInstallState {
                    it.copy(
                        info = PluginInstallInfo(
                            packageName = event.pluginId,
                            name = name,
                            versionName = event.annotation.versionName
                        ),
                        progress = null
                    )
                }
            }
            PluginInstallEvent.Installing -> {
                updateInstallState {
                    it.copy(step = InstallStep.Working(InstallStage.Installing, "正在安装插件..."), progress = null)
                }
            }
            is PluginInstallEvent.Success -> {
                updateInstallState {
                    it.copy(step = InstallStep.Completed(true, event.message), progress = null)
                }
            }
            is PluginInstallEvent.Failure -> {
                updateInstallState {
                    it.copy(step = InstallStep.Completed(false, event.message), progress = null)
                }
            }
        }
    }

    private suspend fun awaitUserConfirm(prompt: PluginInstallPrompt): Boolean {
        pendingUserDecision = CompletableDeferred()
        updateInstallState {
            it.copy(
                step = InstallStep.AwaitingDecision(
                    InstallDecision(
                        type = prompt.type.toDecisionType(),
                        message = prompt.message
                    )
                ),
                progress = null
            )
        }
        return pendingUserDecision?.await() ?: false
    }

    fun respondUserDecision(continueOperation: Boolean) {
        pendingUserDecision?.complete(continueOperation)
        pendingUserDecision = null
        if (!continueOperation) {
            closeDialog()
            return
        }
        when (_uiState.value) {
            is PluginDialogState.Install -> {
                updateInstallState {
                    it.copy(step = InstallStep.Working(InstallStage.Verifying, "继续安装..."), progress = null)
                }
            }
            is PluginDialogState.UpdateCheck -> {
                updateUpdateState {
                    it.copy(step = UpdateStep.Checking("准备更新..."))
                }
            }
            else -> Unit
        }
    }

    private fun startUninstallation(pluginId: String) {
        val info = pluginManager.getPluginInfo(pluginId)
        if (info == null) {
            closeDialog()
            return
        }
        _uiState.value = PluginDialogState.Uninstall(
            DeleteDialogState(
                pluginId = pluginId,
                pluginName = info.name,
                step = DeleteStep.Working("正在删除...")
            )
        )
        currentOperation = viewModelScope.launch(Dispatchers.IO) {
            performUninstallation(pluginId)
        }
    }

    private fun performUninstallation(pluginId: String) {
        try {
            pluginManager.deletePlugin(pluginId)
            updateDeleteState {
                it.copy(step = DeleteStep.Completed(true, "删除完成"))
            }
        } catch (t: Throwable) {
            updateDeleteState {
                it.copy(step = DeleteStep.Completed(false, "删除失败：${t.message ?: "未知错误"}"))
            }
        }
    }

    private fun startUpdateCheck(pluginId: String) {
        val info = pluginManager.getPluginInfo(pluginId)
        if (info == null) {
            closeDialog()
            return
        }
        _uiState.value = PluginDialogState.UpdateCheck(
            UpdateDialogState(
                pluginId = pluginId,
                pluginName = info.name,
                step = UpdateStep.Checking("正在检查更新...")
            )
        )
        currentOperation = viewModelScope.launch { checkUpdateInfo(info) }
    }

    private suspend fun checkUpdateInfo(pluginInfo: PluginInfo) {
        val updateUrlDir = pluginInfo.updateUrl?.trimEnd('/') ?: run {
            updateUpdateState { it.copy(step = UpdateStep.Error("未配置更新地址")) }
            return
        }
        val metadataUrl = "$updateUrlDir/metadata.json"
        val remoteMeta: PluginMetadata = try {
            val body = withContext(Dispatchers.IO) {
                Jsoup.connect(metadataUrl).ignoreContentType(true).timeout(10_000).execute().body()
            }
            json.decodeFromString(PluginMetadata.serializer(), body)
        } catch (t: Throwable) {
            updateUpdateState { it.copy(step = UpdateStep.Error("检查失败：${t.message}")) }
            return
        }

        if (remoteMeta.version <= pluginInfo.version) {
            updateUpdateState { it.copy(step = UpdateStep.Latest("已是最新版本")) }
            return
        }

        updateUpdateState {
            it.copy(step = UpdateStep.Available("检测到新版本：${remoteMeta.versionName}（${remoteMeta.version}）"))
        }

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
        _uiState.value = PluginDialogState.Hidden
        viewModelScope.launch(Dispatchers.Main) { _closeDialogFlow.emit(Unit) }
    }

    private fun updateInstallState(block: (InstallDialogState) -> InstallDialogState) {
        _uiState.update { state ->
            if (state is PluginDialogState.Install) {
                PluginDialogState.Install(block(state.state))
            } else {
                state
            }
        }
    }

    private fun updateDeleteState(block: (DeleteDialogState) -> DeleteDialogState) {
        _uiState.update { state ->
            if (state is PluginDialogState.Uninstall) {
                PluginDialogState.Uninstall(block(state.state))
            } else {
                state
            }
        }
    }

    private fun updateUpdateState(block: (UpdateDialogState) -> UpdateDialogState) {
        _uiState.update { state ->
            if (state is PluginDialogState.UpdateCheck) {
                PluginDialogState.UpdateCheck(block(state.state))
            } else {
                state
            }
        }
    }

    private fun PluginInstallPromptType.toDecisionType(): InstallDecisionType = when (this) {
        PluginInstallPromptType.Reinstall -> InstallDecisionType.Reinstall
        PluginInstallPromptType.Downgrade -> InstallDecisionType.Downgrade
    }

    private sealed interface PluginInstallerRequest {
        data class Install(val source: PluginInstallSource) : PluginInstallerRequest
        data class Uninstall(val pluginId: String) : PluginInstallerRequest
        data class UpdateCheck(val pluginId: String) : PluginInstallerRequest
    }
}
