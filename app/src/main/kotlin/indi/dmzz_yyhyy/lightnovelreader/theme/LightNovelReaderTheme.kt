package indi.dmzz_yyhyy.lightnovelreader.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalAppTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalDarkColorScheme
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalLightColorScheme
import indi.dmzz_yyhyy.lightnovelreader.utils.LocaleUtil

data class AppTheme(
    val isDark: Boolean,
    val colorScheme: ColorScheme
)

@Composable
fun LightNovelReaderTheme(
    darkMode: String,
    isDynamicColor: Boolean = true,
    lightThemeName: String,
    darkThemeName: String,
    appLocale: String,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val isSystemInDarkTheme = isSystemInDarkTheme()

    val isDark = remember(darkMode) {
        when (darkMode) {
            "Enabled" -> true
            "Disabled" -> false
            else -> isSystemInDarkTheme
        }
    }

    val lightColorScheme = remember(lightThemeName, isDynamicColor) {
        if (isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            dynamicLightColorScheme(context)
        else when (lightThemeName) {
            "light_default" -> DefaultLightColorScheme
            else -> DefaultLightColorScheme
        }
    }

    val darkColorScheme = remember(darkThemeName, isDynamicColor) {
        if (isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            dynamicDarkColorScheme(context)
        else when (darkThemeName) {
            "dark_obsidian" -> DarkObsidianColorScheme
            "dark_default" -> DefaultDarkColorScheme
            else -> DefaultDarkColorScheme
        }
    }

    val colorScheme = if (isDark) darkColorScheme else lightColorScheme

    val appTheme = remember(isDark, colorScheme) {
        AppTheme(isDark = isDark, colorScheme = colorScheme)
    }

    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.setBackgroundDrawable(colorScheme.background.toArgb().toDrawable())

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
        } else {
            window.decorView.setOnApplyWindowInsetsListener { v, insets ->
                v.setPadding(0, 0, 0, 0)
                insets
            }
        }

        WindowInsetsControllerCompat(window, view).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
    }

    LaunchedEffect(appLocale) {
        val parts = appLocale.split("-")
        val language = parts.getOrNull(0) ?: "en"
        val variant = parts.getOrNull(1) ?: ""
        LocaleUtil.set(language = language, variant = variant)
    }

    CompositionLocalProvider(
        LocalAppTheme provides appTheme,
        LocalLightColorScheme provides lightColorScheme,
        LocalDarkColorScheme provides darkColorScheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}