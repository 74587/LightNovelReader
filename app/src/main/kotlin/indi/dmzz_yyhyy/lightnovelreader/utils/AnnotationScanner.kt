package indi.dmzz_yyhyy.lightnovelreader.utils

import android.annotation.SuppressLint
import android.util.Log
import dalvik.system.DexClassLoader
import dalvik.system.DexFile
import java.lang.reflect.Field

@Suppress("DEPRECATION")
object AnnotationScanner {
    private const val TAG = "AnnotationScanner"

    /**
     * 扫描DexClassLoader中带有指定注解的所有类
     *
     * @param classLoader 要扫描的DexClassLoader
     * @param annotationClass 要查找的注解类型
     * @return 带有指定注解的类列表
     */
    @SuppressLint("NewApi")
    fun findAnnotatedClasses(
        classLoader: DexClassLoader,
        annotationClass: Class<out Annotation?>,
        scanPackage: String = "",
    ): MutableList<Class<*>> {
        val result: MutableList<Class<*>> = ArrayList()

        try {
            val pathListField = findField(classLoader, "pathList")
            val pathList = pathListField.get(classLoader) ?: return mutableListOf()

            val dexElementsField = findField(pathList, "dexElements")
            @Suppress("UNCHECKED_CAST") val dexElements = dexElementsField.get(pathList) as? Array<Any> ?: return mutableListOf()

            for (dexElement in dexElements) {
                val dexFileField = runCatching { findField(dexElement, "dexFile") }.getOrNull() ?: continue
                val dexFile = dexFileField.get(dexElement) as? DexFile ?: continue
                val classNames = runCatching { dexFile.entries() }.getOrNull() ?: continue

                while (classNames.hasMoreElements()) {
                    val className = classNames.nextElement()
                    if (className.isNullOrEmpty() || !className.contains(".") ||
                        (scanPackage.isNotEmpty() && !className.startsWith(scanPackage))) continue
                    runCatching {
                        val clazz = classLoader.loadClass(className)
                        if (clazz.isAnnotationPresent(annotationClass)) {
                            result.add(clazz)
                            Log.d(TAG, "Found annotated class: $className")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning annotations", e)
        }

        return result
    }

    @Throws(NoSuchFieldException::class)
    private fun findField(instance: Any, name: String): Field {
        var clazz: Class<*>? = instance.javaClass
        while (clazz != null) {
            try {
                val field = clazz.getDeclaredField(name)
                field.isAccessible = true
                return field
            } catch (_: NoSuchFieldException) {
                clazz = clazz.superclass
            }
        }
        throw NoSuchFieldException("Field " + name + " not found in " + instance.javaClass)
    }
}
