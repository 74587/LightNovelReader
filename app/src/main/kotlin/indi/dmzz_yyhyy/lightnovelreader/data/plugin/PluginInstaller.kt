package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.utils.ApkSignatureInfo
import indi.dmzz_yyhyy.lightnovelreader.utils.PluginAnnotationParser
import indi.dmzz_yyhyy.lightnovelreader.utils.getApkSignatures
import indi.dmzz_yyhyy.lightnovelreader.utils.isSignatureMatch
import io.nightfish.lightnovelreader.api.plugin.Plugin
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

enum class PluginInstallerOperation {
    INSTALL, UNINSTALL, UPGRADE
}

@Singleton
class PluginInstaller @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val pluginManager: PluginManager,
    userDataRepository: UserDataRepository
) {
    data class InstallCallbacks(
        val onPhase: (String) -> Unit = {},
        val onProgress: (Float?) -> Unit = {},
        val onConfirm: suspend (String) -> Boolean = { _ -> true },
        val onError: (String) -> Unit = {}
    )

    private val enabledPluginUserData =
        userDataRepository.stringListUserData(UserDataPath.Plugin.EnabledPlugins.path)

    suspend fun installFromTempFile(
        tempFile: File,
        callbacks: InstallCallbacks = InstallCallbacks(),
        preParsed: Pair<String, Plugin>? = null,
        preSignatures: List<ApkSignatureInfo>? = null
    ): Boolean {
        callbacks.onPhase("解析插件信息中")
        callbacks.onProgress(null)
        tempFile.setReadOnly()

        val parsed = preParsed ?: PluginAnnotationParser.parsePluginAnnotationFromFile(
            apkFile = tempFile,
            workDir = pluginManager.pluginsTempDir,
            parentClassLoader = this::class.java.classLoader,
            pm = context.packageManager
        )
        if (parsed == null) {
            val msg = "无效的插件：未包含有效 @Plugin 注解\n请重新选择一个 .lnrp 插件安装文件。"
            callbacks.onError(msg)
            tempFile.delete()
            return false
        }

        val (pluginId, annotation) = parsed
        when (val check = performInstallChecks(pluginId, annotation, preSignatures, tempFile)) {
            is InstallCheckResult.Failed -> {
                callbacks.onError(check.reason)
                tempFile.delete()
                return false
            }
            is InstallCheckResult.NeedsUserConfirm -> {
                val goOn = callbacks.onConfirm(check.message)
                if (!goOn) {
                    tempFile.delete()
                    return false
                }
            }
            InstallCheckResult.Ok -> Unit
        }

        callbacks.onPhase("安装中")
        callbacks.onProgress(null)

        val success = performFinalInstallStep(
            tempFile = tempFile,
            pluginId = pluginId,
        )

        if (!success) callbacks.onError("安装失败")
        return success
    }

    private sealed class InstallCheckResult {
        data object Ok : InstallCheckResult()
        data class Failed(val reason: String) : InstallCheckResult()
        data class NeedsUserConfirm(val message: String) : InstallCheckResult()
    }

    private suspend fun performInstallChecks(
        pluginId: String,
        annotation: Plugin,
        preSignaturesNewApk: List<ApkSignatureInfo>?,
        tempFile: File
    ): InstallCheckResult = withContext(Dispatchers.IO) {
        val tempSignatures = preSignaturesNewApk ?: getApkSignatures(tempFile)

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
            val msg = if (annotation.version == ev)
                "已安装相同版本（$evName），是否重新安装？"
            else
                "当前已安装更高版本（$evName），是否降级安装？"
            return@withContext InstallCheckResult.NeedsUserConfirm(msg)
        }

        InstallCheckResult.Ok
    }

    private fun performFinalInstallStep(
        tempFile: File,
        pluginId: String,
    ): Boolean {
        val pluginsRoot = pluginManager.pluginsDir
        val pluginDir = pluginsRoot.resolve(pluginId).apply { mkdirs() }

        val pluginFile = pluginManager.getPluginFile(pluginDir)
        val assetDir = pluginManager.getPluginAssetDir(pluginDir).apply { mkdirs() }
        val libsDir = pluginManager.getPluginLibsDir(pluginDir).apply { mkdirs() }

        val isInstalled = pluginFile.exists()
        if (isInstalled) {
            return pluginManager.upgradePlugin(pluginId, tempFile)
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
        }
        return loaded
    }



    suspend fun copyUriToFileWithProgress(
        context: Context,
        uri: Uri,
        destFile: File,
        progressCb: (Float?) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val cr = context.contentResolver
            val total = cr.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L

            cr.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
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
            } ?: return@withContext false

            if (total > 0) progressCb(1f) else progressCb(null)
            true
        } catch (_: Throwable) {
            false
        }
    }
}