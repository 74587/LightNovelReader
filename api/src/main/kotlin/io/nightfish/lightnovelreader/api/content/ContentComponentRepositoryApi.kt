package io.nightfish.lightnovelreader.api.content

import io.nightfish.lightnovelreader.api.content.component.AbstractContentComponent
import io.nightfish.lightnovelreader.api.content.component.AbstractContentComponentData
import io.nightfish.lightnovelreader.api.content.component.ComponentDataJsonElementSerializer
import kotlin.reflect.KClass

interface ContentComponentRepositoryApi {
    interface RegisterBuilder {
        fun component(value: KClass<out AbstractContentComponent<out AbstractContentComponentData>>): RegisterBuilder

        fun data(value: KClass<out AbstractContentComponentData>): RegisterBuilder

        fun serializer(value: ComponentDataJsonElementSerializer<out AbstractContentComponentData>): RegisterBuilder

        fun register()
    }

    interface Registrar {
        fun id(id: String): RegisterBuilder
    }

    val registrar: Registrar
}