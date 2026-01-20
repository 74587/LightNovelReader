package io.nightfish.lightnovelreader.api.web.explore.filter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.nightfish.lightnovelreader.api.util.LocalString

abstract class SwitchFilter(
    private var title: LocalString,
    default: Boolean
): Filter<Boolean>(default) {
    override fun getTitle(): LocalString = title
}