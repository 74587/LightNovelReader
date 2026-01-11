package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.utils.PluginAnnotationParser
import indi.dmzz_yyhyy.lightnovelreader.utils.getApkSignatures
import indi.dmzz_yyhyy.lightnovelreader.utils.isSignatureMatch
import io.nightfish.lightnovelreader.api.ApiCompat
import io.nightfish.lightnovelreader.api.ApiMetadata
import io.nightfish.lightnovelreader.api.plugin.Plugin
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class PluginInstaller @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val pluginManager: PluginManager,
    userDataRepository: UserDataRepository
) {
    data class InstallCallbacks(
        val onEvent: (PluginInstallEvent) -> Unit = {},
        val onConfirm: suspend (PluginInstallPrompt) -> Boolean = { _ -> true }
    )

    private val enabledPluginUserData =
        userDataRepository.stringListUserData(UserDataPath.Plugin.EnabledPlugins.path)

    suspend fun installFromSource(
        source: PluginInstallSource,
        callbacks: InstallCallbacks = InstallCallbacks()
    ): Boolean {
        callbacks.onEvent(PluginInstallEvent.Preparing)
        var tempFile: File? = null
        var shouldDeleteTemp = false
        try {
            val installFile = when (source) {
                is PluginInstallSource.UriSource -> {
                    callbacks.onEvent(PluginInstallEvent.Copying(null))
                    val copied = copyUriToTempFile(source.uri) { progress ->
                        callbacks.onEvent(PluginInstallEvent.Copying(progress))
                    }
                    tempFile = copied
                    shouldDeleteTemp = true
                    copied
                }
                is PluginInstallSource.FileSource -> {
                    callbacks.onEvent(PluginInstallEvent.Copying(null))
                    val copied = copyFileToTempFile(source.file) { progress ->
                        callbacks.onEvent(PluginInstallEvent.Copying(progress))
                    }
                    tempFile = copied
                    shouldDeleteTemp = true
                    copied
                }
            }

            if (!installFile.exists()) {
                callbacks.onEvent(PluginInstallEvent.Failure("读取源文件失败"))
                return false
            }

            callbacks.onEvent(PluginInstallEvent.Parsing)
            val parsed = PluginAnnotationParser.parsePluginAnnotationFromFile(
                apkFile = installFile,
                workDir = pluginManager.pluginsTempDir,
                parentClassLoader = this::class.java.classLoader,
                pm = context.packageManager
            )
            if (parsed == null) {
                val msg = "无效的插件：未包含有效 @Plugin 注解\n请重新选择一个 .lnrp 插件安装文件。"
                callbacks.onEvent(PluginInstallEvent.Failure(msg))
                return false
            }

            val (pluginId, annotation) = parsed
            callbacks.onEvent(PluginInstallEvent.Metadata(pluginId, annotation))
            val apiVersion = runCatching { annotation.apiVersion }.getOrNull()
            if (apiVersion == null) {
                callbacks.onEvent(PluginInstallEvent.Failure("无效的插件：缺少 apiVersion"))
                return false
            }
            if (!ApiCompat.isSupported(apiVersion, ApiMetadata.API_VERSION)) {
                callbacks.onEvent(PluginInstallEvent.Failure("插件版本不兼容"))
                return false
            }
            pluginManager.updatePluginInfoFromAnnotation(pluginId, annotation)
            callbacks.onEvent(PluginInstallEvent.Verifying)

            when (val check = performInstallChecks(pluginId, annotation, installFile)) {
                is InstallCheckResult.Failed -> {
                    callbacks.onEvent(PluginInstallEvent.Failure(check.reason))
                    return false
                }
                is InstallCheckResult.NeedsUserConfirm -> {
                    val goOn = callbacks.onConfirm(check.prompt)
                    if (!goOn) {
                        return false
                    }
                }
                InstallCheckResult.Ok -> Unit
            }

            callbacks.onEvent(PluginInstallEvent.Installing)
            val success = performFinalInstallStep(
                tempFile = installFile,
                pluginId = pluginId,
                annotation = annotation
            )

            if (success) {
                callbacks.onEvent(PluginInstallEvent.Success("安装完成"))
            } else {
                callbacks.onEvent(PluginInstallEvent.Failure("安装失败"))
            }
            return success
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            callbacks.onEvent(PluginInstallEvent.Failure("安装失败：${t.message ?: "未知错误"}"))
            return false
        } finally {
            if (shouldDeleteTemp) {
                runCatching { tempFile?.delete() }
            }
        }
    }

    private sealed class InstallCheckResult {
        data object Ok : InstallCheckResult()
        data class Failed(val reason: String) : InstallCheckResult()
        data class NeedsUserConfirm(val prompt: PluginInstallPrompt) : InstallCheckResult()
    }

    private suspend fun performInstallChecks(
        pluginId: String,
        annotation: Plugin,
        tempFile: File
    ): InstallCheckResult = withContext(Dispatchers.IO) {
        val tempSignatures = getApkSignatures(tempFile)

        val pluginDir = context.dataDir.resolve("plugin").resolve(pluginId).apply { mkdirs() }
        val pluginFile = pluginManager.getPluginFile(pluginDir)
        val isInstalled = pluginFile.exists()

        if (!isInstalled) return@withContext InstallCheckResult.Ok

        val existingPath = pluginManager.getPluginFile(pluginId)
        val existingSignatures =
            existingPath?.let { runCatching { getApkSignatures(it) }.getOrNull() }
                ?: pluginManager.getPluginInfo(pluginId)?.signatures

        if (!isSignatureMatch(existingSignatures, tempSignatures)) {
            return@withContext InstallCheckResult.Failed("安装失败：检测到不同签名，请先卸载已安装版本后再安装此插件。")
        }
        val existingInfo = pluginManager.getPluginInfo(pluginId)
        val ev = existingInfo?.version
        val evName = existingInfo?.versionName.orEmpty()
        if (ev != null && annotation.version <= ev) {
            val (promptType, msg) = if (annotation.version == ev)
                PluginInstallPromptType.Reinstall to "已安装相同版本（$evName），是否重新安装？"
            else
                PluginInstallPromptType.Downgrade to "当前已安装更高版本（$evName），是否降级安装？"
            return@withContext InstallCheckResult.NeedsUserConfirm(
                PluginInstallPrompt(type = promptType, message = msg)
            )
        }

        InstallCheckResult.Ok
    }

    private fun performFinalInstallStep(
        tempFile: File,
        pluginId: String,
        annotation: Plugin
    ): Boolean {
        val pluginsRoot = pluginManager.pluginsDir
        val pluginDir = pluginsRoot.resolve(pluginId).apply { mkdirs() }

        val pluginFile = pluginManager.getPluginFile(pluginDir)
        val assetDir = pluginManager.getPluginAssetDir(pluginDir).apply { mkdirs() }
        val libsDir = pluginManager.getPluginLibsDir(pluginDir).apply { mkdirs() }

        val isInstalled = pluginFile.exists()
        if (isInstalled) {
            val wasEnabled = enabledPluginUserData.getOrDefault(emptyList()).contains(pluginId)
            if (wasEnabled) {
                enabledPluginUserData.update { it.toMutableList().apply { remove(pluginId) } }
                runCatching { pluginManager.unloadPlugin(pluginId) }
            }

            val upgraded = pluginManager.upgradePlugin(pluginId, tempFile, forceLoad = wasEnabled)
            if (wasEnabled) {
                enabledPluginUserData.update { it.toMutableList().apply { if (!contains(pluginId)) add(pluginId) } }
            }
            if (upgraded) {
                pluginManager.updatePluginInfoFromAnnotation(pluginId, annotation)
                pluginManager.writePluginMetadata(pluginId, annotation)
            }
            return upgraded
        }

        val replaced = try {
            tempFile.copyTo(pluginFile, overwrite = true)
            tempFile.delete()
            pluginFile.setReadOnly()
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }

        if (!replaced) return false

        try {
            pluginManager.writePluginMetadata(pluginId, annotation)
            val zipFile = java.util.zip.ZipFile(pluginFile)
            val entries = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.name.startsWith("assets/") && !entry.isDirectory) {
                    zipFile.getInputStream(entry).use { input ->
                        val outFile = assetDir.resolve(entry.name.removePrefix("assets/"))
                        outFile.parentFile?.mkdirs()
                        outFile.outputStream().use { output -> input.copyTo(output) }
                    }
                }
            }
            zipFile.close()

            pluginManager.extractLibFromApk(pluginFile, libsDir)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val loaded = pluginManager.loadPlugin(pluginFile, forceLoad = true) != null
        if (loaded) {
            enabledPluginUserData.update {
                it.toMutableList().apply { if (!contains(pluginId)) add(pluginId) }
            }
        } else {
            pluginManager.markPluginError(pluginId)
        }
        return loaded
    }



    private suspend fun copyUriToTempFile(
        uri: Uri,
        progressCb: (Float?) -> Unit
    ): File {
        val cr = context.contentResolver
        val total = cr.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
        return copySourceToTempFile(total, { cr.openInputStream(uri) }, progressCb)
    }

    private suspend fun copyFileToTempFile(
        file: File,
        progressCb: (Float?) -> Unit
    ): File {
        val total = file.length()
        return copySourceToTempFile(total, { file.inputStream() }, progressCb)
    }

    private suspend fun copySourceToTempFile(
        total: Long,
        openInput: () -> InputStream?,
        progressCb: (Float?) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val cacheDir = context.cacheDir.resolve("plugin_install").apply {
            mkdirs()
            listFiles()?.forEach { it.delete() }
        }
        val out = cacheDir.resolve(System.currentTimeMillis().toString())
        return@withContext try {
            openInput()?.use { input ->
                out.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var copied = 0L
                    var read: Int

                    val minIntervalMs = 150L
                    var lastEmit = 0L
                    val step = if (total > 0) maxOf(total / 200, 128 * 1024) else 256 * 1024
                    var nextStep = step

                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        copied += read

                        val now = System.currentTimeMillis()
                        if (copied >= nextStep || now - lastEmit >= minIntervalMs) {
                            lastEmit = now
                            if (total > 0) {
                                progressCb((copied.toDouble() / total).coerceIn(0.0, 1.0).toFloat())
                                nextStep = ((copied / step) + 1) * step
                            } else {
                                progressCb(null)
                            }
                        }
                    }
                    output.fd.sync()
                }
            } ?: throw IOException("读取源文件失败")

            if (total > 0) progressCb(1f) else progressCb(null)
            out.setReadOnly()
            out
        } catch (t: Throwable) {
            runCatching { out.delete() }
            throw t
        }
    }
}
