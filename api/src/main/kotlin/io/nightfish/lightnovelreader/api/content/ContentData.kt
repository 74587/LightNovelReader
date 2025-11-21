package io.nightfish.lightnovelreader.api.content

import io.nightfish.lightnovelreader.api.content.component.AbstractContentComponent

data class ContentData (
    val components: List<AbstractContentComponent<*>>
) {
    companion object {
        fun empty() = ContentData(emptyList())
    }
}