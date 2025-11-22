package io.nightfish.lightnovelreader.api

import java.io.File

class PluginContext(
    val dataDir: File,
    val pluginFile: File,
    private val assetDir: File
) {
    fun getAsset(path: String) = assetDir.resolve(path)
}