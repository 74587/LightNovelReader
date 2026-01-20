package io.nightfish.lightnovelreader.api.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

class LocalString {
    val stringGetter: @Composable () -> String
    constructor(string: String) {
        stringGetter = { string }
    }
    constructor(@StringRes id: Int) {
        stringGetter = {
            stringResource(id)
        }
    }

    @Composable
    fun resolve() = stringGetter()
}

fun String.local() = LocalString(this)