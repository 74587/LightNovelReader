package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import indi.dmzz_yyhyy.lightnovelreader.utils.ApkSignatureInfo

data class PluginInfo(
    val isUpdatable: Boolean,
    val id: String,
    val name: String,
    val version: Int,
    val versionName: String,
    val author: String,
    val description: String,
    val updateUrl: String?,
    val signatures: List<ApkSignatureInfo>?
) {
    override fun equals(other: Any?) = other is PluginInfo && other.id == id
    override fun hashCode() = id.hashCode()
}
