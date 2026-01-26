package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import android.content.Context
import android.content.Intent
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
import io.nightfish.lightnovelreader.api.plugin.PluginConstants
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
    private val enabledPluginPackagesUserData =
        userDataRepository.stringListUserData(UserDataPath.Plugin.EnabledPluginPackages.path)
    private val errorPluginsUserData =
        userDataRepository.stringListUserData(UserDataPath.Plugin.ErrorPlugins.path)

    private val defaultWebDataSources = listOf(Wenku8Api)
    private val computeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }
    private val installedPluginIds = mutableSetOf<String>()
    private val _scannedPluginApps = mutableStateListOf<PluginAppInfo>()
    val scannedPluginApps: SnapshotStateList<PluginAppInfo> = _scannedPluginApps

    val pluginsDir: File = appContext.dataDir.resolve("plugins")
    val pluginsTempDir: File = appContext.cacheDir.resolve("plugins_tmp")
    fun getPluginDir(name: String): File = pluginsDir.resolve(name)
    fun getPluginDataDir(pluginDir: File) = pluginDir.resolve("data")
    fun getPluginFile(pluginDir: File): File = pluginDir.resolve("plugin")
    fun getPluginAssetDir(pluginDir: File): File = pluginDir.resolve("asset")
    fun getPluginLibsDir(pluginDir: File): File = pluginDir.resolve("libs")
    private fun getPluginMetadataFile(pluginDir: File): File = pluginDir.resolve("metadata.json")

    fun loadAllPlugins() {
        if (pluginsTempDir.exists()) pluginsTempDir.deleteRecursively()

        normalizeErrorRecords()

        defaultWebDataSources.forEach(webBookDataSourceManager::loadWebDataSourceClass)
        installedPluginIds.clear()
        _scannedPluginApps.clear()

        val enabledPlugins = enabledPluginsUserData.getOrDefault(emptyList()).toSet()

        loadAppPlugins()

        pluginsDir.also(File::mkdir)
            .listFiles()
            ?.filter { it.isDirectory }
            ?.forEach { dir -> loadLocalPluginDir(dir, enabledPlugins) }
    }

    fun refreshAppPlugins() {
        if (pluginsTempDir.exists()) pluginsTempDir.deleteRecursively()

        val previousInstalled = _allPluginInfo.filter { it.source == PluginSource.InstalledApp }

        installedPluginIds.clear()
        _scannedPluginApps.clear()
        loadAppPlugins()

        val activePackages = _scannedPluginApps.map { it.packageName }.toSet()
        val removed = previousInstalled.filter { info ->
            val pkg = info.packageName
            pkg == null || !activePackages.contains(pkg)
        }

        if (removed.isEmpty()) return

        removed.forEach { info ->
            unloadPlugin(info.id)
            pluginMap.remove(info.id)
            pluginClassLoaderMap.remove(info.id)
        }
        _allPluginInfo.removeAll { info ->
            info.source == PluginSource.InstalledApp && removed.any { it.id == info.id }
        }
        errorPluginsUserData.update { current -> current.filterNot { id -> removed.any { it.id == id } } }
    }

    private fun normalizeErrorRecords() {
        val errorIds = errorPluginsUserData.get().orEmpty().toSet()
        if (errorIds.isEmpty()) return

        enabledPluginsUserData.update { it.toMutableList().apply { removeAll(errorIds) } }
        errorPluginsUserData.update { errorIds.toList() }
    }

    private fun loadLocalPluginDir(dir: File, enabledPlugins: Set<String>) {
        val dirPluginId = dir.name
        val pluginFile = getPluginFile(dir)
        if (!pluginFile.exists()) return

        val meta = resolvePluginMetadata(dir, pluginFile, dirPluginId) ?: return
        val (pluginId, apiVersion) = meta

        if (installedPluginIds.contains(pluginId)) {
            enabledPluginsUserData.update { it.toMutableList().apply { remove(pluginId) } }
            markPluginError(pluginId)
            return
        }

        pluginPathMap[pluginId] = pluginFile

        if (!isPluginApiCompatible(pluginId, apiVersion)) return
        if (!enabledPlugins.contains(pluginId)) return

        val loadedId = runCatching { loadPlugin(pluginFile) }.getOrNull()
        if (loadedId != pluginId) {
            enabledPluginsUserData.update { it.toMutableList().apply { remove(pluginId) } }
            markPluginError(pluginId)
        }
    }

    private fun resolvePluginMetadata(
        dir: File,
        pluginFile: File,
        dirPluginId: String
    ): Pair<String, Int?>? {
        readPluginMetadataCache(dir)?.let { cache ->
            val isCompatible = ApiCompat.isSupported(cache.apiVersion, ApiMetadata.API_VERSION)
            val info = PluginInfo(
                isUpdatable = false,
                id = cache.id,
                name = cache.name,
                version = cache.version,
                versionName = cache.versionName,
                author = cache.author,
                description = cache.description,
                updateUrl = cache.updateUrl,
                signatures = null,
                apiVersion = cache.apiVersion,
                isApiCompatible = isCompatible,
                source = PluginSource.LocalPackage
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
                signatures = null,
                apiVersion = null,
                isApiCompatible = false,
                source = PluginSource.LocalPackage
            )
            upsertPluginInfo(fallback)
            pluginPathMap[dirPluginId] = pluginFile
            markPluginError(dirPluginId)
            return null
        }

        val (pluginId, annotation) = parsed
        updatePluginInfo(pluginId, annotation)
        writePluginMetadata(pluginId, annotation)
        return pluginId to getApiVersion(annotation)
    }

    private fun isPluginApiCompatible(pluginId: String, apiVersion: Int?): Boolean {
        if (apiVersion == null) return false
        if (!ApiCompat.isSupported(apiVersion, ApiMetadata.API_VERSION)) {
            enabledPluginsUserData.update { it.toMutableList().apply { remove(pluginId) } }
            return false
        }
        return true
    }

    private fun loadAppPlugins() {
        val intent = Intent(PluginConstants.DISCOVERY_ACTION)
        val receivers = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.packageManager.queryBroadcastReceivers(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            appContext.packageManager.queryBroadcastReceivers(intent, PackageManager.MATCH_ALL)
        }

        val enabledPackages = enabledPluginPackagesUserData.getOrDefault(emptyList()).toMutableSet()
        val discoveredPackages = mutableSetOf<String>()

        receivers.forEach { resolveInfo ->
            val appInfo = resolveInfo.activityInfo?.applicationInfo ?: return@forEach
            if (appInfo.packageName == appContext.packageName) return@forEach

            val packageName = appInfo.packageName
            val apkPath = appInfo.sourceDir ?: return@forEach
            val apkFile = File(apkPath)
            discoveredPackages.add(packageName)

            val appLabel = runCatching { appInfo.loadLabel(appContext.packageManager).toString() }
                .getOrDefault(packageName)

            val versionName = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    appContext.packageManager.getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(0L)
                    ).versionName
                } else {
                    @Suppress("DEPRECATION")
                    appContext.packageManager.getPackageInfo(packageName, 0).versionName
                }
            }.getOrNull().orEmpty()

            _scannedPluginApps.removeAll { it.packageName == packageName }
            _scannedPluginApps.add(
                PluginAppInfo(
                    packageName = packageName,
                    name = appLabel,
                    versionName = versionName
                )
            )

            val parsed = PluginAnnotationParser.parsePluginAnnotationFromFile(
                apkFile = apkFile,
                workDir = pluginsTempDir,
                parentClassLoader = this::class.java.classLoader,
                pm = appContext.packageManager
            ) ?: return@forEach

            val (pluginId, annotation) = parsed
            updatePluginInfo(
                pluginId,
                annotation,
                signatures = runCatching { getApkSignatures(apkFile) }.getOrNull(),
                source = PluginSource.InstalledApp,
                packageName = packageName
            )

            if (!ApiCompat.isSupported(annotation.apiVersion, ApiMetadata.API_VERSION)) {
                enabledPackages.remove(packageName)
                return@forEach
            }

            installedPluginIds.add(pluginId)

            if (!enabledPackages.contains(packageName)) return@forEach

            val pluginDir = getPluginDir(pluginId).apply { mkdirs() }
            val loadedId = loadPluginFromApk(
                path = apkFile,
                pluginDir = pluginDir,
                forceLoad = false,
                source = PluginSource.InstalledApp,
                packageName = packageName
            )
            if (loadedId == null) {
                enabledPackages.remove(packageName)
                markPluginError(pluginId)
            }
        }

        enabledPackages.retainAll(discoveredPackages)
        enabledPluginPackagesUserData.update { enabledPackages.toList() }
    }

    private fun getPluginId(
        clazz: Class<*>,
        source: PluginSource = PluginSource.LocalPackage,
        packageName: String? = null
    ): String? {
        val annotation = clazz.getAnnotation(Plugin::class.java) ?: return null
        val id = clazz.`package`?.name ?: return null
        updatePluginInfo(id, annotation, source = source, packageName = packageName)
        return id
    }

    private fun updatePluginInfo(
        pluginId: String,
        annotation: Plugin,
        signatures: List<ApkSignatureInfo>? = null,
        source: PluginSource = PluginSource.LocalPackage,
        packageName: String? = null
    ) {
        val apiVersion = runCatching { annotation.apiVersion }.getOrNull()
        val isApiCompatible = apiVersion?.let { ApiCompat.isSupported(it, ApiMetadata.API_VERSION) } ?: false
        val existingSignatures =
            signatures ?: _allPluginInfo.firstOrNull { it.id == pluginId }?.signatures
        val existingInfo = _allPluginInfo.firstOrNull { it.id == pluginId }

        val resolvedSource = existingInfo?.source ?: source
        val resolvedPackageName = if (resolvedSource == PluginSource.InstalledApp) {
            packageName ?: existingInfo?.packageName
        } else {
            existingInfo?.packageName
        }

        val info = PluginInfo(
            isUpdatable = false,
            id = pluginId,
            name = annotation.name,
            version = annotation.version,
            versionName = annotation.versionName,
            author = annotation.author,
            description = annotation.description,
            updateUrl = annotation.updateUrl,
            signatures = existingSignatures,
            apiVersion = apiVersion,
            isApiCompatible = isApiCompatible,
            packageName = resolvedPackageName,
            source = resolvedSource
        )
        upsertPluginInfo(info)
    }

    fun updatePluginInfoFromAnnotation(pluginId: String, annotation: Plugin) {
        updatePluginInfo(pluginId, annotation, source = PluginSource.LocalPackage)
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

        val apiVersion = getApiVersion(annotation) ?: return
        val cache = PluginInfoCache(
            id = pluginId,
            name = annotation.name,
            version = annotation.version,
            versionName = annotation.versionName,
            author = annotation.author,
            description = annotation.description,
            updateUrl = annotation.updateUrl,
            apiVersion = apiVersion
        )
        getPluginMetadataFile(pluginDir).writeText(
            json.encodeToString(PluginInfoCache.serializer(), cache)
        )
    }

    private fun readPluginMetadataCache(pluginDir: File): PluginInfoCache? {
        val file = getPluginMetadataFile(pluginDir)
        if (!file.exists()) return null
        return runCatching {
            json.decodeFromString(PluginInfoCache.serializer(), file.readText())
        }.getOrNull()
    }

    fun clearPluginError(pluginId: String) {
        errorPluginsUserData.update { it.filterNot { id -> id == pluginId } }
    }

    @Serializable
    private data class PluginInfoCache(
        val id: String,
        val name: String,
        val version: Int,
        val versionName: String,
        val author: String,
        val description: String,
        val updateUrl: String?,
        val apiVersion: Int
    )

    private fun getPluginInstance(clazz: Class<*>, pluginContext: PluginContext): LightNovelReaderPlugin? {
        if (!LightNovelReaderPlugin::class.java.isAssignableFrom(clazz)) return null
        return pluginInjector.providePlugin(clazz, pluginContext)
    }

    fun loadPlugin(plugin: LightNovelReaderPlugin, forceLoad: Boolean = false): Boolean {
        val id = getPluginId(plugin.javaClass) ?: return false

        if (!forceLoad) {
            val info = _allPluginInfo.firstOrNull { it.id == id }
            val enabled = when (info?.source) {
                PluginSource.InstalledApp -> {
                    val pkg = info.packageName
                    pkg != null && enabledPluginPackagesUserData.getOrDefault(emptyList()).contains(pkg)
                }

                PluginSource.LocalPackage, null ->
                    enabledPluginsUserData.getOrDefault(emptyList()).contains(id)
            }
            if (!enabled) return false
        }

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
        val pluginDir = path.parentFile ?: return null
        return loadPluginFromApk(
            path = path,
            pluginDir = pluginDir,
            forceLoad = forceLoad,
            source = PluginSource.LocalPackage,
            packageName = null
        )
    }

    private fun loadPluginFromApk(
        path: File,
        pluginDir: File,
        forceLoad: Boolean,
        source: PluginSource,
        packageName: String?
    ): String? {
        runCatching { path.setReadOnly() }

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
        val pluginFile = if (source == PluginSource.InstalledApp) path else getPluginFile(pluginDir)

        val id = loadPlugin(
            classLoader = classLoader,
            pluginContext = PluginContext(
                dataDir = getPluginDataDir(pluginDir),
                pluginFile = pluginFile,
                assetDir = getPluginAssetDir(pluginDir)
            ),
            scanPackage = scanPackage,
            forceLoad = forceLoad,
            source = source,
            packageName = packageName ?: scanPackage
        )

        if (id != null) {
            clearPluginError(id)
            if (source == PluginSource.LocalPackage) {
                pluginPathMap[id] = path
            }
            computeScope.launch {
                val sig = try {
                    getApkSignatures(path)
                } catch (_: Throwable) {
                    null
                }
                val info = _allPluginInfo.firstOrNull { it.id == id }
                if (info != null) {
                    _allPluginInfo.removeAll { it.id == id }
                    _allPluginInfo.add(info.copy(signatures = sig))
                }
            }
        }
        return id
    }

    fun loadPlugin(
        classLoader: DexClassLoader,
        pluginContext: PluginContext,
        scanPackage: String = "",
        forceLoad: Boolean = false,
        source: PluginSource = PluginSource.LocalPackage,
        packageName: String? = null
    ): String? {
        var id: String? = null
        try {
            AnnotationScanner.findAnnotatedClasses(classLoader, Plugin::class.java, scanPackage)
                .asSequence()
                .filter {
                    id = getPluginId(it, source = source, packageName = packageName)
                    id != null
                }
                .map { getPluginInstance(it, pluginContext) }
                .filterIsInstance<LightNovelReaderPlugin>()
                .firstOrNull()
                ?.let {
                    val ok = loadPlugin(it, forceLoad = forceLoad)
                    if (!ok) id = null
                }
        } catch (e: Throwable) {
            val targetId = id
            if (targetId != null) {
                Log.e("PluginManager", "Error loading $targetId:\n$e")
                markPluginError(targetId)
            }
            return null
        }
        webBookDataSourceManager.loadWebDataSourcesFromClassLoader(classLoader, pluginInjector, scanPackage)
        if (id != null) {
            pluginClassLoaderMap[id!!] = classLoader
        }
        return id
    }

    fun markPluginError(pluginId: String) {
        val info = _allPluginInfo.firstOrNull { it.id == pluginId }

        if (info?.source == PluginSource.InstalledApp) {
            info.packageName?.let { pkg ->
                enabledPluginPackagesUserData.update { it.toMutableList().apply { remove(pkg) } }
            }
        } else {
            enabledPluginsUserData.update { it.toMutableList().apply { remove(pluginId) } }
        }
        errorPluginsUserData.update { current ->
            val list = current.toMutableList()
            if (!list.contains(pluginId)) list.add(pluginId)
            list
        }
        unloadPlugin(pluginId)
    }

    fun loadPlugin(id: String): Boolean {
        val info = _allPluginInfo.firstOrNull { it.id == id }
        if (info?.source == PluginSource.InstalledApp) {
            val packageName = info.packageName ?: return false
            val appInfo = runCatching {
                appContext.packageManager.getApplicationInfo(packageName, 0)
            }.getOrNull() ?: return false

            val apkPath = appInfo.sourceDir ?: return false
            val pluginDir = getPluginDir(id).apply { mkdirs() }

            return loadPluginFromApk(
                path = File(apkPath),
                pluginDir = pluginDir,
                forceLoad = false,
                source = PluginSource.InstalledApp,
                packageName = packageName
            ) == id
        }

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
        val info = _allPluginInfo.firstOrNull { it.id == id }
        if (info?.source == PluginSource.InstalledApp) {
            unloadPlugin(id)
            info.packageName?.let { packageName ->
                enabledPluginPackagesUserData.update { it.toMutableList().apply { remove(packageName) } }
            }
            return
        }

        val path = pluginPathMap[id] ?: return
        unloadPlugin(id)
        path.delete()
        if (path.parentFile?.parentFile == pluginsDir) {
            path.parentFile!!.deleteRecursively()
        }
        enabledPluginsUserData.update { it.toMutableList().apply { remove(id) } }
        errorPluginsUserData.update { it.toMutableList().apply { remove(id) } }

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
        } catch (_: Exception) {
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
