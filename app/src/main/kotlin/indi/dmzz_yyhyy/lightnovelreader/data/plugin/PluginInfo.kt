package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import indi.dmzz_yyhyy.lightnovelreader.utils.ApkSignatureInfo

enum class PluginSource {
    LocalPackage,
    InstalledApp
}

data class PluginInfo(
    val isUpdatable: Boolean,
    val id: String,
    val name: String,
    val version: Int,
    val versionName: String,
    val author: String,
    val description: String,
    val updateUrl: String?,
    val signatures: List<ApkSignatureInfo>?,
    val packageName: String? = null,
    val source: PluginSource = PluginSource.LocalPackage
) {
    override fun equals(other: Any?) = other is PluginInfo && other.id == id
    override fun hashCode() = id.hashCode()
}

data class PluginAppInfo(
    val packageName: String,
    val name: String,
    val versionName: String
)
