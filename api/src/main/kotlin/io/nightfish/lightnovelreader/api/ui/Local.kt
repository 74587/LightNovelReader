package io.nightfish.lightnovelreader.api.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.navigation.NavController

val LocalNavController = compositionLocalOf<NavController> {
    error("CompositionLocal LocalNavController not present")
}

val LocalReaderStyle = compositionLocalOf {
    ReaderStyle(
        fontSize = 15f,
        fontLineHeight = 7f,
        fontWeight = 500f,
        textColor = Color.Unspecified,
        textDarkColor = Color.Unspecified,
    )
}

val LocalTextLocaleList = compositionLocalOf {
    LocaleList(Locale.current)
}

fun appLocaleToTextLocaleList(appLocale: String): LocaleList {
    val parts = appLocale.split("-")
    val language = parts.getOrNull(0).orEmpty()
    val region = parts.getOrNull(1).orEmpty()

    val tag = buildString {
        append(language.ifBlank { "en" })
        if (region.isNotBlank()) {
            append("-")
            append(region)
        }
    }

    return LocaleList(Locale(tag))
}