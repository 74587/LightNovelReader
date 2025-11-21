package io.nightfish.lightnovelreader.api.content.component

import android.content.Context
import kotlinx.serialization.json.JsonElement
import org.dom4j.Element

abstract class AbstractContentComponentData {
    abstract val id: String
    abstract fun toJsonElement(): JsonElement
    abstract fun toHtmlElement(context: Context): Element
}
