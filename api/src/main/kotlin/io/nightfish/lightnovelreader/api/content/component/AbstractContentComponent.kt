package io.nightfish.lightnovelreader.api.content.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

abstract class AbstractContentComponent<Data: AbstractContentComponentData>(
    val data: Data
) {
    abstract val id: String

    @Composable
    abstract fun Content(modifier: Modifier)
}
