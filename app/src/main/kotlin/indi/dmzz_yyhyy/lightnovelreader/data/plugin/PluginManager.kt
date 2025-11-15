package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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

    val pluginsDir: File = appContext.dataDir.resolve("plugins")
    val pluginsTempDir: File = appContext.cacheDir.resolve("plugins_tmp")
    fun getPluginDir(name: String): File = pluginsDir.resolve(name)
    fun getPluginDataDir(pluginDir: File) = pluginDir.resolve("data")
    fun getPluginFile(pluginDir: File): File = pluginDir.resolve("plugin")
    fun getPluginAssetDir(pluginDir: File): File = pluginDir.resolve("asset")
    fun getPluginLibsDir(pluginDir: File): File = pluginDir.resolve("libs")

    fun loadAllPlugins() {
        val tmp = pluginsTempDir
        if (tmp.exists()) tmp.deleteRecursively()
        errorPluginsUserData.get()?.forEach { path ->
            File(path).also {
                it.delete()
                if (it.parentFile?.parentFile == pluginsDir) {
                    it.parentFile!!.deleteRecursively()
                }
            }
            errorPluginsUserData.update {
                it.toMutableList().apply {
                    remove(path)
                }
            }
        }
        defaultWebDataSources.forEach(webBookDataSourceManager::loadWebDataSourceClass)
        pluginsDir
            .also(File::mkdir)
            .listFiles()
            ?.filter { it.isDirectory }
            ?.forEach { dir ->
                val file = getPluginFile(dir)
                PluginAnnotationParser.parsePluginAnnotationFromFile(
                    apkFile = file,
                    workDir = pluginsTempDir,
                    parentClassLoader = this::class.java.classLoader,
                    pm = appContext.packageManager
                )?.let { (pid, ann) ->
                    if (!ApiCompat.isSupported(ann.apiVersion, ApiMetadata.API_VERSION)) {
                        enabledPluginsUserData.update { it.toMutableList().apply { remove(pid) } }
                    }
                }
                loadPlugin(file)
            }
    }

    private fun getPluginId(clazz: Class<*>): String? {
        val annotation = clazz.getAnnotation(Plugin::class.java) ?: return null
        val id = clazz.`package`?.name ?: return null
        val existingSignatures = _allPluginInfo.firstOrNull { it.id == id }?.signatures
        val info = PluginInfo(
            isUpdatable = false,
            id = id,
            name = annotation.name,
            version = annotation.version,
            versionName = annotation.versionName,
            author = annotation.author,
            description = annotation.description,
            updateUrl = annotation.updateUrl,
            signatures = existingSignatures
        )
        _allPluginInfo.removeAll { it.id == info.id }
        _allPluginInfo.add(info)
        return id
    }

    private fun getPluginInstance(clazz: Class<*>, pluginContext: PluginContext): LightNovelReaderPlugin? {
        if (!LightNovelReaderPlugin::class.java.isAssignableFrom(clazz)) return null
        return pluginInjector.providePlugin(clazz, pluginContext)
    }

    fun loadPlugin(plugin: LightNovelReaderPlugin, forceLoad: Boolean = false) {
        val id = getPluginId(plugin.javaClass) ?: return
        if (!enabledPluginsUserData.getOrDefault(emptyList()).contains(id) && !forceLoad) return
        plugin.apply {
            onLoad()
        }
        pluginMap[id] = plugin
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
                loadPlugin(it, forceLoad = forceLoad)
            }
        webBookDataSourceManager.loadWebDataSourcesFromClassLoader(classLoader, pluginInjector, scanPackage)
        if (id != null) {
            pluginClassLoaderMap[id] = classLoader
        }
        return id
    }

    fun loadPlugin(id: String) {
        val path = pluginPathMap[id] ?: return
        loadPlugin(path)
    }

    fun unloadPlugin(id: String) {
        pluginMap[id]?.onUnload()
        pluginClassLoaderMap[id]?.let { webBookDataSourceManager.unloadWebDataSourcesFromClassLoader(it) }
    }

    fun upgradePlugin(id: String, newFile: File): Boolean {
        val oldFile = pluginPathMap[id] ?: return false
        unloadPlugin(id)

        val dir = oldFile.parentFile ?: return false
        val tmp = File(dir, "${oldFile.name}.tmp")
        val bak = File(dir, "${oldFile.name}.bak")

        fun restoreOld(): Boolean {
            loadPlugin(oldFile, forceLoad = true)
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

        _allPluginInfo.removeAll { it.id == id }
        val loadedId = runCatching { loadPlugin(oldFile, forceLoad = true) }.getOrNull()

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