package io.nightfish.lightnovelreader.api.content.builder

import android.net.Uri
import io.nightfish.lightnovelreader.api.content.component.ImageComponentData

fun ContentBuilder.image(uri: Uri): ContentBuilder = component(ImageComponentData(uri))