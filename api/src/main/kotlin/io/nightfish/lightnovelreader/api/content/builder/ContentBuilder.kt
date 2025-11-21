package io.nightfish.lightnovelreader.api.content.builder

import io.nightfish.lightnovelreader.api.content.component.AbstractContentComponentData
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

class ContentBuilder {
    val components = mutableListOf<AbstractContentComponentData>()
    fun component(abstractContentComponentData: AbstractContentComponentData): ContentBuilder {
        components.add(abstractContentComponentData)
        return this
    }

    fun build() =
        buildJsonObject {
            putJsonArray("components") {
                for (component in components) {
                    addJsonObject {
                        put("id", component.id)
                        put("data", component.toJsonElement())
                    }
                }
            }
        }
}