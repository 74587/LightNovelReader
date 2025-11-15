package indi.dmzz_yyhyy.lightnovelreader.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTheme

val LocalNavController = compositionLocalOf<NavController> {
    error("CompositionLocal LocalNavController not present")
}

val LocalAppTheme = staticCompositionLocalOf<AppTheme> {
    error("No AppTheme provided")
}

val LocalLightColorScheme = staticCompositionLocalOf<ColorScheme> {
    error("No Light ColorScheme provided")
}

val LocalDarkColorScheme = staticCompositionLocalOf<ColorScheme> {
    error("No Dark ColorScheme provided")
}

val LocalScaffoldPadding = compositionLocalOf { PaddingValues() }