package io.nightfish.lightnovelreader.plugin.js

import kotlinx.serialization.Serializable

@Serializable
data class PackageInfo(
    val id: Int,
    val name: String,
    val provider: String,
    val webDataSourcePath: String
)