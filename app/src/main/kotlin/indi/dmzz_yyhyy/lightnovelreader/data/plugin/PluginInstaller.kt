package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import android.content.Context
import android.net.Uri
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.fold
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.R
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
    userDataRepository: indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
) {
    private val enabledPluginUserData =
        userDataRepository.stringListUserData(UserDataPath.Plugin.EnabledPlugins.path)

    suspend fun installFromSource(
        source: PluginInstallSource,
        onEvent: (PluginInstallEvent) -> Unit = {},
        onConfirm: suspend (PluginInstallPrompt) -> Boolean = { true }
    ): Result<Unit, String> {
        onEvent(PluginInstallEvent.StageChanged(PluginInstallStage.Preparing))
        var tempFile: File? = null
        var shouldDeleteTemp = false
        try {
            val installFile = when (source) {
                is PluginInstallSource.UriSource -> {
                    onEvent(PluginInstallEvent.StageChanged(PluginInstallStage.Copying))
                    val copied = copyUriToTempFile(source.uri) { progress ->
                        onEvent(PluginInstallEvent.Progress(progress))
                    }
                    tempFile = copied
                    shouldDeleteTemp = true
                    copied
                }

                is PluginInstallSource.FileSource -> {
                    onEvent(PluginInstallEvent.StageChanged(PluginInstallStage.Copying))
                    val copied = copyFileToTempFile(source.file) { progress ->
                        onEvent(PluginInstallEvent.Progress(progress))
                    }
                    tempFile = copied
                    shouldDeleteTemp = true
                    copied
                }
            }

            if (!installFile.exists()) {
                return Err(context.getString(R.string.plugin_install_read_failed))
            }

            onEvent(PluginInstallEvent.StageChanged(PluginInstallStage.Parsing))
            val parsed = PluginAnnotationParser.parsePluginAnnotationFromFile(
                apkFile = installFile,
                workDir = pluginManager.pluginsTempDir,
                parentClassLoader = this::class.java.classLoader,
                pm = context.packageManager
            )
            if (parsed == null) {
                return Err(context.getString(R.string.plugin_install_invalid_missing_annotation))
            }

            val (pluginId, annotation) = parsed
            onEvent(PluginInstallEvent.Metadata(pluginId, annotation))

            val apiVersion = runCatching { annotation.apiVersion }.getOrNull()
                ?: return Err(context.getString(R.string.plugin_install_invalid_missing_api_version))

            if (!ApiCompat.isSupported(apiVersion, ApiMetadata.API_VERSION)) {
                return Err(context.getString(R.string.plugin_install_version_incompatible))
            }

            onEvent(PluginInstallEvent.StageChanged(PluginInstallStage.Verifying))
            val prompt = performInstallChecks(pluginId, installFile).fold(
                success = { it },
                failure = { reason -> return Err(reason) }
            )
            if (prompt != null && !onConfirm(prompt)) {
                return Err(context.getString(R.string.plugin_install_cancelled))
            }

            onEvent(PluginInstallEvent.StageChanged(PluginInstallStage.Installing))
            val success = withContext(Dispatchers.IO) {
                performFinalInstallStep(
                    tempFile = installFile,
                    pluginId = pluginId,
                    annotation = annotation
                )
            }

            return if (success) Ok(Unit) else Err(context.getString(R.string.plugin_install_failed))
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            val reason = t.message ?: context.getString(R.string.unspecified)
            return Err(context.getString(R.string.plugin_install_failed_with_reason, reason))
        } finally {
            if (shouldDeleteTemp) {
                runCatching { tempFile?.delete() }
            }
        }
    }

    private suspend fun performInstallChecks(
        pluginId: String,
        tempFile: File
    ): Result<PluginInstallPrompt?, String> = withContext(Dispatchers.IO) {
        val tempSignatures = getApkSignatures(tempFile)
        val existingInfo = pluginManager.getPluginInfo(pluginId)
        if (existingInfo?.source == PluginSource.InstalledApp) {
            return@withContext Err(context.getString(R.string.plugin_install_conflict_app_plugin))
        }

        val pluginDir = pluginManager.pluginsDir.resolve(pluginId).apply { mkdirs() }
        val pluginFile = pluginManager.getPluginFile(pluginDir)
        val isInstalled = pluginFile.exists()
        if (!isInstalled) return@withContext Ok(null)

        val existingPath = pluginManager.getPluginFile(pluginId)
        val existingSignatures =
            existingPath?.let { runCatching { getApkSignatures(it) }.getOrNull() }
                ?: pluginManager.getPluginInfo(pluginId)?.signatures

        if (!isSignatureMatch(existingSignatures, tempSignatures)) {
            return@withContext Err(context.getString(R.string.plugin_install_signature_mismatch))
        }

        return@withContext Ok(
            PluginInstallPrompt(
                type = PluginInstallPromptType.Reinstall,
                message = context.getString(R.string.plugin_install_overwrite_prompt)
            )
        )
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
                enabledPluginUserData.update {
                    it.toMutableList().apply { if (!contains(pluginId)) add(pluginId) }
                }
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
        } catch (_: Throwable) {
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
        } catch (_: Exception) {
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

        try {
            openInput()?.use { input ->
                out.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var copied = 0L

                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break

                        output.write(buffer, 0, read)
                        copied += read

                        if (total > 0) {
                            progressCb((copied.toFloat() / total).coerceIn(0f, 1f))
                        } else {
                            progressCb(null)
                        }
                    }
                    output.fd.sync()
                }
            } ?: throw IOException(context.getString(R.string.plugin_install_read_failed))

            if (total > 0) progressCb(1f)
            out.setReadOnly()
            out
        } catch (t: Throwable) {
            runCatching { out.delete() }
            throw t
        }
    }
}
