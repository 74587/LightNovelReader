package indi.dmzz_yyhyy.lightnovelreader.utils

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalAppTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException

fun loadReaderFontFamilySafe(uri: Uri): FontFamily? {
    return try {
        if (uri == Uri.EMPTY) return null
        val fontFile = File(uri.path ?: return null)
        if (!fontFile.exists()) throw FileNotFoundException()
        FontFamily(Font(fontFile))
    } catch (e: Exception) {
        Log.e("FontLoad", "Failed to load custom font", e)
        null
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun rememberReaderFontFamily(settingState: SettingState): FontFamily {
    val coroutineScope = rememberCoroutineScope()
    val uri = settingState.fontFamilyUri
    val fontFamily = remember(uri) {
        loadReaderFontFamilySafe(uri)
    }

    if (fontFamily == null && uri != Uri.EMPTY) {
        val context = LocalContext.current
        coroutineScope.launch(Dispatchers.IO) {
            settingState.fontFamilyUriUserData.set(Uri.EMPTY)
        }
        LaunchedEffect(uri) {
            settingState.fontFamilyUriUserData.asynchronousSet(Uri.EMPTY)
            Toast.makeText(context, "字体加载失败，已恢复为默认字体", Toast.LENGTH_SHORT).show()
        }
    }

    return fontFamily ?: FontFamily.Default
}

@Composable
fun rememberReaderBackgroundPainter(settingState: SettingState): Painter {
    val context = LocalContext.current
    val isDark = LocalAppTheme.current.isDark

    val backgroundUri = remember(
        isDark,
        settingState.backgroundImageUri,
        settingState.backgroundDarkImageUri
    ) {
        if (isDark) settingState.backgroundDarkImageUri else settingState.backgroundImageUri
    }

    if (backgroundUri == Uri.EMPTY || backgroundUri.toString().isBlank()) {
        return painterResource(id = R.drawable.paper)
    }

    val imageRequest = remember(backgroundUri) {
        ImageRequest.Builder(context)
            .data(backgroundUri)
            .crossfade(true)
            .build()
    }

    val painter = rememberAsyncImagePainter(
        model = imageRequest,
        error = painterResource(id = R.drawable.paper)
    )

    val state = painter.state
    LaunchedEffect(state, backgroundUri) {
        if (state is AsyncImagePainter.State.Error) {
            if (isDark) {
                settingState.backgroundDarkImageUriUserData.asynchronousSet(Uri.EMPTY)
            } else {
                settingState.backgroundImageUriUserData.asynchronousSet(Uri.EMPTY)
            }
            Toast.makeText(context, "背景加载失败，已恢复默认", Toast.LENGTH_SHORT).show()
        }
    }

    return painter
}


@Composable
fun readerTextColor(settingState: SettingState): Color {
    val localTheme = LocalAppTheme.current
    val isDark = localTheme.isDark
    val onSurface = localTheme.colorScheme.onSurface

    val color = remember(isDark, settingState.textColor, settingState.textDarkColor, onSurface) {
        when {
            isDark && settingState.textDarkColor.isUnspecified -> onSurface
            !isDark && settingState.textColor.isUnspecified -> onSurface
            isDark -> settingState.textDarkColor
            else -> settingState.textColor
        }
    }

    return color
}
