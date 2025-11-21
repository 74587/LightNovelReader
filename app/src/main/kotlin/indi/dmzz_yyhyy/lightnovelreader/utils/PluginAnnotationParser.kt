package indi.dmzz_yyhyy.lightnovelreader.utils

import android.content.pm.PackageManager
import dalvik.system.DexClassLoader
import io.nightfish.lightnovelreader.api.plugin.Plugin
import java.io.File

object PluginAnnotationParser {
    /**
     * 解析指定 APK 中标注 @Plugin 的类
     *
     * @return `(pluginId, annotation)`
     *
     * 如果未找到有效的 @Plugin 注解返回 null
     */

    fun parsePluginAnnotationFromFile(
        apkFile: File,
        workDir: File,
        parentClassLoader: ClassLoader? = ClassLoader.getSystemClassLoader(),
        pm: PackageManager
    ): Pair<String, Plugin>? {
        var useFile: File? = null
        return try {
            useFile = workDir.resolve("parse_${System.currentTimeMillis()}_${apkFile.name}")
            apkFile.copyTo(useFile, overwrite = true)
            useFile.setReadOnly()

            val odexDir = workDir.resolve("odex").apply { mkdirs() }
            val loader = DexClassLoader(useFile.absolutePath, odexDir.absolutePath, null, parentClassLoader)

            val pkgInfo = pm.getPackageArchiveInfo(useFile.absolutePath, 0)
            val scanPackage = pkgInfo?.packageName ?: ""
            val pluginClass = AnnotationScanner.findAnnotatedClasses(loader, Plugin::class.java, scanPackage).firstOrNull()
                ?: return null
            val annotation = pluginClass.getAnnotation(Plugin::class.java) ?: return null
            val id = pluginClass.`package`?.name ?: return null

            id to annotation
        } catch (_: Throwable) {
            null
        } finally {
            useFile?.delete()
        }
    }
}
