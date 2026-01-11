package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation.NavGraphBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import dalvik.system.DexClassLoader
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceManager
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.Wenku8Api
import indi.dmzz_yyhyy.lightnovelreader.utils.AnnotationScanner
import indi.dmzz_yyhyy.lightnovelreader.utils.ApkSignatureInfo
import indi.dmzz_yyhyy.lightnovelreader.utils.PluginAnnotationParser
import indi.dmzz_yyhyy.lightnovelreader.utils.getApkSignatures
import io.nightfish.lightnovelreader.api.ApiCompat
import io.nightfish.lightnovelreader.api.ApiMetadata
import io.nightfish.lightnovelreader.api.PluginContext
import io.nightfish.lightnovelreader.api.plugin.LightNovelReaderPlugin
import io.nightfish.lightnovelreader.api.plugin.Plugin
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Singleton
class PluginManager @Inject constructor(
    @field:ApplicationContext private val appContext: Context,
    private val webBookDataSourceManager: WebBookDataSourceManager,
    private val pluginInjector: PluginInjector,
    userDataRepository: UserDataRepository
) {
    private val _allPluginInfo = mutableStateListOf<PluginInfo>()
    private val pluginPathMap = mutableMapOf<String, File>()
    private val pluginMap = mutableMapOf<String, LightNovelReaderPlugin>()
    private val pluginClassLoaderMap = mutableMapOf<String, DexClassLoader>()
    val allPluginInfo: SnapshotStateList<PluginInfo> = _allPluginInfo

    private val enabledPluginsUserData =
        userDataRepository.stringListUserData(UserDataPath.Plugin.EnabledPlugins.path)
    private val errorPluginsUserData =
        userDataRepository.stringListUserData(UserDataPath.Plugin.ErrorPlugins.path)

    private val defaultWebDataSources = listOf(Wenku8Api)
    private val defaultPlugins = listOf<Class<*>>()
    private val computeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    val pluginsDir: File = appContext.dataDir.resolve("plugins")
    val pluginsTempDir: File = appContext.cacheDir.resolve("plugins_tmp")
    fun getPluginDir(name: String): File = pluginsDir.resolve(name)
    fun getPluginDataDir(pluginDir: File) = pluginDir.resolve("data")
    fun getPluginFile(pluginDir: File): File = pluginDir.resolve("plugin")
    fun getPluginAssetDir(pluginDir: File): File = pluginDir.resolve("asset")
    fun getPluginLibsDir(pluginDir: File): File = pluginDir.resolve("libs")
    private fun getPluginMetadataFile(pluginDir: File): File = pluginDir.resolve("metadata.json")

    fun loadAllPlugins() {
        clearTempDir()
        normalizeErrorEntries()
        defaultWebDataSources.forEach(webBookDataSourceManager::loadWebDataSourceClass)
        val enabledPlugins = enabledPluginsUserData.getOrDefault(emptyList()).toSet()
        pluginsDir.also(File::mkdir)
            .listFiles()
            ?.filter { it.isDirectory }
            ?.forEach { dir -> processPluginDir(dir, enabledPlugins) }
    }

    private fun clearTempDir() {
        if (pluginsTempDir.exists()) pluginsTempDir.deleteRecursively()
    }

    private fun normalizeErrorEntries() {
        val parsedErrors = parseErrorEntries(errorPluginsUserData.get().orEmpty())
        if (parsedErrors.paths.isNotEmpty()) {
            parsedErrors.paths.forEach { path ->
                File(path).also {
                    it.delete()
                    if (it.parentFile?.parentFile == pluginsDir) {
                        it.parentFile!!.deleteRecursively()
                    }
                }
            }
        }
        if (parsedErrors.pluginIds.isNotEmpty()) {
            enabledPluginsUserData.update { it.toMutableList().apply { removeAll(parsedErrors.pluginIds) } }
        }
        errorPluginsUserData.update { parsedErrors.pluginIds.map { id -> "id:$id" } }
    }

    private fun processPluginDir(dir: File, enabledPlugins: Set<String>) {
        val dirPluginId = dir.name
        val pluginFile = getPluginFile(dir)
        if (enabledPlugins.contains(dirPluginId)) markPluginLoading(dirPluginId)

        val meta = resolvePluginMetadata(dir, pluginFile, dirPluginId) ?: return
        val (pluginId, apiVersion) = meta
        updateLoadingMarker(dirPluginId, pluginId, enabledPlugins)
        pluginPathMap[pluginId] = pluginFile

        if (!isApiSupported(pluginId, apiVersion)) return
        if (!enabledPlugins.contains(pluginId)) {
            clearPluginLoading(pluginId)
            return
        }
        loadEnabledPlugin(pluginId, pluginFile)
    }

    private fun resolvePluginMetadata(dir: File, pluginFile: File, dirPluginId: String): Pair<String, Int?>? {
        readPluginMetadata(dir)?.let { cache ->
            val info = PluginInfo(
                isUpdatable = false,
                id = cache.id,
                name = cache.name,
                version = cache.version,
                versionName = cache.versionName,
                author = cache.author,
                description = cache.description,
                updateUrl = cache.updateUrl,
                signatures = null
            )
            upsertPluginInfo(info)
            return cache.id to cache.apiVersion
        }

        val parsed = PluginAnnotationParser.parsePluginAnnotationFromFile(
            apkFile = pluginFile,
            workDir = pluginsTempDir,
            parentClassLoader = this::class.java.classLoader,
            pm = appContext.packageManager
        ) ?: run {
            val fallback = PluginInfo(
                isUpdatable = false,
                id = dirPluginId,
                name = dirPluginId,
                version = 0,
                versionName = "",
                author = "",
                description = "",
                updateUrl = "",
                signatures = null
            )
            upsertPluginInfo(fallback)
            pluginPathMap[dirPluginId] = pluginFile
            clearPluginLoading(dirPluginId)
            markPluginError(dirPluginId)
            return null
        }

        val (pluginId, annotation) = parsed
        updatePluginInfo(pluginId, annotation)
        writePluginMetadata(pluginId, annotation)
        return pluginId to getApiVersion(annotation)
    }

    private fun updateLoadingMarker(dirPluginId: String, pluginId: String, enabledPlugins: Set<String>) {
        if (pluginId == dirPluginId) return
        clearPluginLoading(dirPluginId)
        if (enabledPlugins.contains(pluginId)) markPluginLoading(pluginId)
    }

    private fun isApiSupported(pluginId: String, apiVersion: Int?): Boolean {
        if (apiVersion == null) {
            clearPluginLoading(pluginId)
            markPluginError(pluginId)
            return false
        }
        if (!ApiCompat.isSupported(apiVersion, ApiMetadata.API_VERSION)) {
            enabledPluginsUserData.update { it.toMutableList().apply { remove(pluginId) } }
            clearPluginLoading(pluginId)
            return false
        }
        return true
    }

    private fun loadEnabledPlugin(pluginId: String, pluginFile: File) {
        val loadedId = runCatching { loadPlugin(pluginFile) }.getOrNull()
        if (loadedId == pluginId) {
            clearPluginLoading(pluginId)
        } else {
            enabledPluginsUserData.update { it.toMutableList().apply { remove(pluginId) } }
        }
    }

    private fun getPluginId(clazz: Class<*>): String? {
        val annotation = clazz.getAnnotation(Plugin::class.java) ?: return null
        val id = clazz.`package`?.name ?: return null
        updatePluginInfo(id, annotation)
        return id
    }

    private fun updatePluginInfo(pluginId: String, annotation: Plugin, signatures: List<ApkSignatureInfo>? = null) {
        val existingSignatures = signatures ?: _allPluginInfo.firstOrNull { it.id == pluginId }?.signatures
        val info = PluginInfo(
            isUpdatable = false,
            id = pluginId,
            name = annotation.name,
            version = annotation.version,
            versionName = annotation.versionName,
            author = annotation.author,
            description = annotation.description,
            updateUrl = annotation.updateUrl,
            signatures = existingSignatures
        )
        upsertPluginInfo(info)
    }

    fun updatePluginInfoFromAnnotation(pluginId: String, annotation: Plugin) {
        updatePluginInfo(pluginId, annotation)
    }

    private fun getApiVersion(annotation: Plugin): Int? =
        runCatching { annotation.apiVersion }.getOrElse { null }

    private fun upsertPluginInfo(info: PluginInfo) {
        _allPluginInfo.removeAll { it.id == info.id }
        _allPluginInfo.add(info)
    }

    fun writePluginMetadata(pluginId: String, annotation: Plugin) {
        val pluginDir = pluginPathMap[pluginId]?.parentFile ?: pluginsDir.resolve(pluginId)
        pluginDir.mkdirs()
        val cache = PluginInfoCache(
            id = pluginId,
            name = annotation.name,
            version = annotation.version,
            versionName = annotation.versionName,
            author = annotation.author,
            description = annotation.description,
            updateUrl = annotation.updateUrl,
            apiVersion = getApiVersion(annotation) ?: return
        )
        getPluginMetadataFile(pluginDir).writeText(
            json.encodeToString(PluginInfoCache.serializer(), cache)
        )
    }

    private fun readPluginMetadata(pluginDir: File): PluginInfoCache? {
        val file = getPluginMetadataFile(pluginDir)
        if (!file.exists()) return null
        return runCatching {
            json.decodeFromString(PluginInfoCache.serializer(), file.readText())
        }.getOrNull()
    }

    private fun markPluginLoading(pluginId: String) {
        errorPluginsUserData.update { current ->
            val list = current.toMutableList()
            val key = "id:$pluginId"
            if (!list.contains(key)) list.add(key)
            list
        }
    }

    private fun clearPluginLoading(pluginId: String) {
        errorPluginsUserData.update { current ->
            current.filterNot { it == "id:$pluginId" }
        }
    }

    private data class ErrorEntries(
        val pluginIds: Set<String>,
        val paths: List<String>
    )

    private fun parseErrorEntries(entries: List<String>): ErrorEntries {
        val pluginIds = mutableSetOf<String>()
        val paths = mutableListOf<String>()
        entries.forEach { entry ->
            when {
                entry.startsWith("id:") -> pluginIds.add(entry.removePrefix("id:"))
                entry.startsWith("path:") -> paths.add(entry.removePrefix("path:"))
                entry.contains(File.separatorChar) -> paths.add(entry)
                entry.isNotBlank() -> pluginIds.add(entry)
            }
        }
        return ErrorEntries(pluginIds, paths)
    }

    @Serializable
    private data class PluginInfoCache(
        val id: String,
        val name: String,
        val version: Int,
        val versionName: String,
        val author: String,
        val description: String,
        val updateUrl: String,
        val apiVersion: Int
    )

    private fun getPluginInstance(clazz: Class<*>, pluginContext: PluginContext): LightNovelReaderPlugin? {
        if (!LightNovelReaderPlugin::class.java.isAssignableFrom(clazz)) return null
        return pluginInjector.providePlugin(clazz, pluginContext)
    }

    fun loadPlugin(plugin: LightNovelReaderPlugin, forceLoad: Boolean = false): Boolean {
        val id = getPluginId(plugin.javaClass) ?: return false
        if (!enabledPluginsUserData.getOrDefault(emptyList()).contains(id) && !forceLoad) return false
        return try {
            plugin.onLoad()
            pluginMap[id] = plugin
            true
        } catch (_: Throwable) {
            markPluginError(id)
            false
        }
    }

    fun loadPlugin(path: File, forceLoad: Boolean = false): String? {
        path.setReadOnly()
        val pluginDir = path.parentFile ?: return null

        val packageInfo = appContext.packageManager
            .getPackageArchiveInfo(path.path, PackageManager.GET_PERMISSIONS)
            ?.also {
                val assetDir = getPluginAssetDir(pluginDir).apply {
                    if (exists()) deleteRecursively()
                    mkdirs()
                }
                ZipFile(path).use { zip ->
                    val entries = zip.entries()
                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        if (!entry.isDirectory && entry.name.startsWith("assets/")) {
                            zip.getInputStream(entry).buffered().use { input ->
                                val out = assetDir.resolve(entry.name.removePrefix("assets/"))
                                out.parentFile?.mkdirs()
                                out.outputStream().buffered().use { input.copyTo(it) }
                            }
                        }
                    }
                }
                extractLibFromApk(path, getPluginLibsDir(pluginDir).apply { mkdirs() })
            }

        val classLoader = DexClassLoader(
            path.absolutePath,
            null,
            getPluginLibsDir(pluginDir).absolutePath,
            this.javaClass.classLoader
        )
        val scanPackage = packageInfo?.packageName ?: ""

        val id = loadPlugin(
            classLoader = classLoader,
            pluginContext = PluginContext(
                dataDir = getPluginDataDir(pluginDir),
                pluginFile = getPluginFile(pluginDir),
                assetDir = getPluginAssetDir(pluginDir)
            ),
            scanPackage = scanPackage,
            forceLoad = forceLoad
        )
        if (id != null) {
            clearPluginLoading(id)
            pluginPathMap[id] = path
            computeScope.launch {
                val sig = try { getApkSignatures(path) } catch (_: Throwable) { null }
                val info = _allPluginInfo.firstOrNull { it.id == id }
                if (info != null) {
                    _allPluginInfo.removeAll { it.id == id }
                    _allPluginInfo.add(info.copy(signatures = sig))
                }
            }
        }
        return id
    }

    fun loadPlugin(classLoader: DexClassLoader, pluginContext: PluginContext, scanPackage: String = "", forceLoad: Boolean = false): String? {
        var id: String? = null
        try {
            AnnotationScanner.findAnnotatedClasses(classLoader, Plugin::class.java, scanPackage)
                .filter {
                    id = getPluginId(it)
                    id != null
                }
                .map { getPluginInstance(it, pluginContext) }
                .filter { it is LightNovelReaderPlugin }
                .map { it as LightNovelReaderPlugin }
                .firstOrNull()
                ?.let {
                    val ok = loadPlugin(it, forceLoad = forceLoad)
                    if (!ok) id = null
                }
        } catch (e: Throwable) {
            if (id != null) {
                Log.e("PluginManager", "Error loading $id:\n$e")
                markPluginError(id)
            }
            return null
        }
        webBookDataSourceManager.loadWebDataSourcesFromClassLoader(classLoader, pluginInjector, scanPackage)
        if (id != null) {
            pluginClassLoaderMap[id] = classLoader
        }
        return id
    }

    fun markPluginError(pluginId: String) {
        enabledPluginsUserData.update { it.toMutableList().apply { remove(pluginId) } }
        errorPluginsUserData.update { current ->
            val list = current.toMutableList()
            val key = "id:$pluginId"
            if (!list.contains(key)) list.add(key)
            list
        }
        unloadPlugin(pluginId)
    }

    fun loadPlugin(id: String): Boolean {
        val path = pluginPathMap[id] ?: return false
        return loadPlugin(path) == id
    }

    fun unloadPlugin(id: String) {
        pluginMap[id]?.onUnload()
        pluginClassLoaderMap[id]?.let { webBookDataSourceManager.unloadWebDataSourcesFromClassLoader(it) }
    }

    fun upgradePlugin(id: String, newFile: File, forceLoad: Boolean = true): Boolean {
        val oldFile = pluginPathMap[id] ?: return false
        unloadPlugin(id)

        val dir = oldFile.parentFile ?: return false
        val tmp = File(dir, "${oldFile.name}.tmp")
        val bak = File(dir, "${oldFile.name}.bak")

        fun restoreOld(): Boolean {
            loadPlugin(oldFile, forceLoad = forceLoad)
            return false
        }

        if (tmp.exists()) tmp.delete()
        runCatching {
            newFile.copyTo(tmp, overwrite = true)
            newFile.delete()
            tmp.setReadOnly()
        }.getOrElse {
            tmp.delete()
            return restoreOld()
        }

        if (bak.exists()) bak.delete()
        if (!oldFile.renameTo(bak)) {
            tmp.delete()
            return restoreOld()
        }

        if (!tmp.renameTo(oldFile)) {
            bak.renameTo(oldFile)
            tmp.delete()
            return restoreOld()
        }

        if (forceLoad) {
            _allPluginInfo.removeAll { it.id == id }
        }
        val loadedId = if (forceLoad) {
            runCatching { loadPlugin(oldFile, forceLoad = true) }.getOrNull()
        } else {
            id
        }

        return if (loadedId == id) {
            bak.delete()
            pluginPathMap[id] = oldFile
            true
        } else {
            oldFile.delete()
            bak.renameTo(oldFile)
            tmp.delete()
            loadPlugin(oldFile, forceLoad = true)
            false
        }
    }

    fun deletePlugin(id: String) {
        val path = pluginPathMap[id] ?: return
        unloadPlugin(id)
        path.delete()
        if (path.parentFile?.parentFile == pluginsDir) {
            path.parentFile!!.deleteRecursively()
        }
        enabledPluginsUserData.update { it.toMutableList().apply { remove(id) } }
        _allPluginInfo.removeAll { it.id == id }
        pluginPathMap.remove(id)
        pluginMap.remove(id)
        pluginClassLoaderMap.remove(id)
    }

    fun extractLibFromApk(apk: File, targetDir: File) {
        try {
            val tempDir = targetDir.resolve("temp").also { it.mkdir() }
            val packageInfo = appContext.packageManager.getPackageArchiveInfo(apk.path, 0)
            packageInfo?.applicationInfo?.let {
                ZipFile(apk.path).use { zip ->
                    val entries = zip.entries()
                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        if (
                            entry.name.startsWith("lib/") &&
                            !entry.isDirectory &&
                            !entry.name.endsWith("libandroidx.graphics.path.so")
                        ) {
                            val out = tempDir.resolve(entry.name.removePrefix("lib/"))
                            out.parentFile?.mkdirs()
                            zip.getInputStream(entry).buffered().use { input ->
                                out.outputStream().buffered().use { input.copyTo(it) }
                            }
                        }
                    }
                }
            }
            val abiList = Build.SUPPORTED_ABIS
            for (abi in abiList.reversed()) {
                val abiDir = tempDir.resolve(abi)
                if (!abiDir.exists()) continue
                abiDir.listFiles()?.forEach { file ->
                    val outputFile = targetDir.resolve(file.name)
                    outputFile.parentFile?.mkdirs()
                    if (!outputFile.exists()) outputFile.createNewFile()
                    outputFile.outputStream().buffered().use {
                        file.inputStream().buffered().copyTo(it)
                    }
                }
            }
            tempDir.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Composable
    fun PluginContent(pluginId: String, paddingValues: PaddingValues) {
        pluginMap[pluginId]?.PageContent(paddingValues)
    }

    fun getPluginInfo(id: String): PluginInfo? =
        _allPluginInfo.firstOrNull { it.id == id }

    fun getPluginFile(id: String): File? =
        pluginPathMap[id]

    fun NavGraphBuilder.onBuildNavHost() {
        pluginMap.values.forEach {
            with(it) {
                onBuildNavHost()
            }
        }
    }
}
