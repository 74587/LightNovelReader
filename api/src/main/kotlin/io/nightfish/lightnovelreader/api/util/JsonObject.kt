package io.nightfish.lightnovelreader.api.util

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

fun JsonObject.Companion.empty() = buildJsonObject {  }