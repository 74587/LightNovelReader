package io.nightfish.lightnovelreader.api.content.builder

import io.nightfish.lightnovelreader.api.content.component.SimpleTextComponentData

fun ContentBuilder.simpleText(text: String): ContentBuilder = component(SimpleTextComponentData(text))