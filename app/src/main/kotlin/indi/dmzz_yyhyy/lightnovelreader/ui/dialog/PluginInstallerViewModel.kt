package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInfo
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInstaller
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInstallerOperation
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginManager
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.DeleteDialogState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.InstallDialogState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginMetadata
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.UpdateDialogState
import indi.dmzz_yyhyy.lightnovelreader.utils.PluginAnnotationParser
import indi.dmzz_yyhyy.lightnovelreader.utils.getApkSignatures
import io.nightfish.lightnovelreader.api.plugin.Plugin
import jakarta.inject.Inject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

@OptIn(FlowPreview::class)
@HiltViewModel
class PluginInstallerDialogViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val pluginManager: PluginManager,
    private val pluginInstaller: PluginInstaller
) : ViewModel() {
    var dialogType by mutableStateOf<PluginInstallerOperation?>(null)
        private set

    var installState by mutableStateOf(InstallDialogState())
        private set
    var deleteState by mutableStateOf(DeleteDialogState())
        private set
    var updateState by mutableStateOf(UpdateDialogState())
        private set

    private val _installProgress = MutableStateFlow<Float?>(null)
    val installProgress = _installProgress.asStateFlow()
    private val _snackbarFlow = MutableSharedFlow<String>()
    val snackbarFlow = _snackbarFlow.asSharedFlow()
    private val _closeDialogFlow = MutableSharedFlow<Unit>()
    val closeDialogFlow = _closeDialogFlow.asSharedFlow()

    private val json = Json { ignoreUnknownKeys = true }
    private var currentOperation: Job? = null
    private var pendingUserDecision: CompletableDeferred<Boolean>? = null

    private var currentSource: String = ""
    private var tempFile: File? = null
    private var parsedPluginInfo: Pair<String, Plugin>? = null

    init {
        viewModelScope.launch {
            installProgress.sample(150).distinctUntilChanged().collect()
        }
    }

    fun setSource(source: String) {
        viewModelScope.launch {
            currentOperation?.cancelAndJoin()
            currentSource = source
            when (determineOperationType()) {
                PluginInstallerOperation.INSTALL -> startInstallation()
                PluginInstallerOperation.UNINSTALL -> startUninstallation()
                PluginInstallerOperation.UPGRADE -> startUpgrade()
                else -> closeDialog()
            }
        }
    }

    private fun determineOperationType(): PluginInstallerOperation? {
        if (currentSource.startsWith("uninstall:")) {
            dialogType = PluginInstallerOperation.UNINSTALL
            return dialogType
        }
        if (currentSource.startsWith("content://") || currentSource.startsWith("file://")) {
            dialogType = PluginInstallerOperation.INSTALL
            return dialogType
        }
        val exists = pluginManager.getPluginInfo(currentSource) != null
        dialogType = if (exists) PluginInstallerOperation.UPGRADE else null
        return dialogType
    }

    private fun startInstallation() {
        dialogType = PluginInstallerOperation.INSTALL
        installState = InstallDialogState(visible = true, phase = "正在准备安装...")
        currentOperation = viewModelScope.launch {
            try {
                processInstallation()
            } catch (_: CancellationException) {
            } catch (t: Throwable) {
                t.printStackTrace()
                installState = installState.copy(error = true, phase = "安装失败：${t.message ?: "未知错误"}")
                closeDialog()
            }
        }
    }

    private suspend fun copyFromUri(uri: Uri): File = withContext(Dispatchers.IO) {
        val cacheDir = context.cacheDir.resolve("plugin_install").apply {
            mkdirs()
            listFiles()?.forEach { it.delete() }
        }
        val out = cacheDir.resolve(System.currentTimeMillis().toString())
        val ok = pluginInstaller.copyUriToFileWithProgress(context, uri, out) { prog ->
            _installProgress.value = prog ?: 0f
        }
        if (!ok) throw IOException("读取源文件失败")
        out.setReadOnly()
        out
    }

    private suspend fun processInstallation() {
        val file = if (currentSource.contains("://")) copyFromUri(currentSource.toUri()) else File(currentSource)
        tempFile = file
        installState = installState.copy(phase = "解析插件信息中", error = false, finished = false)
        println("processInstallation -> ${file.absolutePath}")

        val parsed = parsePluginAnnotationFromFile(file)
        parsedPluginInfo = parsed
        if (parsed == null) {
            installState = installState.copy(error = true, phase = "无效的插件：未包含有效 @Plugin 注解")
            runCatching { file.delete() }
            return
        }

        val (pluginId, annotation) = parsed
        installState = installState.copy(packageName = pluginId, pluginAnnotation = annotation)

        val preSig = runCatching { getApkSignatures(file) }.getOrNull()

        installState = installState.copy(phase = "正在安装插件...")
        val ok = pluginInstaller.installFromTempFile(
            tempFile = file,
            callbacks = PluginInstaller.InstallCallbacks(
                onPhase = { phase -> installState = installState.copy(phase = phase) },
                onProgress = { p -> _installProgress.value = p },
                onConfirm = { msg ->
                    pendingUserDecision = CompletableDeferred()
                    installState = installState.copy(confirm = InstallDialogState.Confirm.Upgrade, phase = msg)
                    pendingUserDecision?.await() ?: false
                },
                onError = { reason ->
                    installState = installState.copy(error = true, phase = reason)
                }
            ),
            preParsed = parsed,
            preSignatures = preSig
        )

        runCatching { if (file.exists()) file.delete() }
        installState = if (ok) installState.copy(finished = true, phase = "安装完成")
        else installState.copy(error = true)
    }

    fun respondUserDecision(continueOperation: Boolean) {
        pendingUserDecision?.complete(continueOperation)
        pendingUserDecision = null
        installState = installState.copy(confirm = InstallDialogState.Confirm.None)

        if (!continueOperation) {
            closeDialog()
        }
    }

    private fun startUninstallation() {
        val pluginId = currentSource.removePrefix("uninstall:")
        val info = pluginManager.getPluginInfo(pluginId)
        if (info == null) { println("no plugins found for: $info"); closeDialog(); return }
        dialogType = PluginInstallerOperation.UNINSTALL
        deleteState = DeleteDialogState(visible = true, pluginId = pluginId, pluginName = info.name, phase = "正在删除...")
        currentOperation = viewModelScope.launch { performUninstallation(pluginId) }
    }

    private fun performUninstallation(pluginId: String) {
        try {
            pluginManager.deletePlugin(pluginId)
            deleteState = deleteState.copy(phase = "删除完成", finished = true)
        } catch (t: Throwable) {
            deleteState = deleteState.copy(phase = "删除失败：${t.message ?: "未知错误"}", finished = true)
        }
    }

    private fun startUpgrade() {
        val info = pluginManager.getPluginInfo(currentSource) ?: return
        dialogType = PluginInstallerOperation.UPGRADE
        updateState = UpdateDialogState(visible = true, pluginId = currentSource, pluginName = info.name, isChecking = true, message = "正在检查更新...")
        currentOperation = viewModelScope.launch { checkUpdateInfo(info) }
    }

    private suspend fun checkUpdateInfo(pluginInfo: PluginInfo) {
        val updateUrlDir = pluginInfo.updateUrl?.trimEnd('/') ?: run {
            updateState = updateState.copy(isChecking = false, isError = true, message = "未配置更新地址"); return
        }
        val metadataUrl = "$updateUrlDir/metadata.json"
        val remoteMeta: PluginMetadata = try {
            val body = withContext(Dispatchers.IO) {
                Jsoup.connect(metadataUrl).ignoreContentType(true).timeout(10_000).execute().body()
            }
            json.decodeFromString(PluginMetadata.serializer(), body)
        } catch (t: Throwable) {
            t.printStackTrace()
            updateState = updateState.copy(isChecking = false, isError = true, message = "检查失败：${t.message}")
            return
        }

        if (remoteMeta.version <= pluginInfo.version) {
            updateState = updateState.copy(isChecking = false, isLatest = true, message = "已是最新版本")
            return
        }

        updateState = updateState.copy(
            isChecking = false,
            hasUpdate = true,
            message = "检测到新版本：${remoteMeta.versionName}（${remoteMeta.version}）"
        )

        pendingUserDecision = CompletableDeferred()
        val ok = try { pendingUserDecision?.await() } catch (_: Throwable) { false } finally {
            pendingUserDecision = null
            updateState = updateState.copy(hasUpdate = false)
        }
        if (ok != true) { println("cancel upgrade chk: ${pluginInfo.name}"); closeDialog(); return }
        closeDialog()
    }

    fun onCancelOperation() {
        viewModelScope.launch {
            currentOperation?.cancelAndJoin()
            tempFile?.let { runCatching { if (it.exists()) it.delete() } }
            tempFile = null
            closeDialog()
        }
    }

    fun onCloseDialog() {
        viewModelScope.launch {
            currentOperation?.cancelAndJoin()
            tempFile?.delete()
            closeDialog()
        }
    }

    private fun closeDialog() {
        viewModelScope.launch(Dispatchers.Main) { _closeDialogFlow.emit(Unit) }
    }

    private fun parsePluginAnnotationFromFile(tempFile: File): Pair<String, Plugin>? {
        return PluginAnnotationParser.parsePluginAnnotationFromFile(
            apkFile = tempFile,
            workDir = pluginManager.pluginsTempDir,
            parentClassLoader = this::class.java.classLoader,
            pm = context.packageManager
        )
    }

}