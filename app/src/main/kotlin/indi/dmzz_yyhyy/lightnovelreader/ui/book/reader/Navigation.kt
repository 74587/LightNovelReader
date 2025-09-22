package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.imageview.ImageViewerScreen
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ColorPickerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme.navigateToSettingsThemeDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.ImageUtils.saveBitmapAsPng
import indi.dmzz_yyhyy.lightnovelreader.utils.ImageUtils.urlToBitmap
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun NavGraphBuilder.bookReaderDestination() {
    composable<Route.Book.Reader> { navBackStackEntry ->
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<ReaderViewModel>(navController.getBackStackEntry<Route.Book>())
        ReaderScreen(
            readingScreenUiState = viewModel.uiState,
            settingState = viewModel.settingState,
            onClickBackButton = navController::popBackStackIfResumed,
            updateTotalReadingTime = viewModel::updateTotalReadingTime,
            accumulateReadTime = viewModel::accumulateReadingTime,
            onClickPrevChapter = viewModel::prevChapter,
            onClickNextChapter = viewModel::nextChapter,
            onChangeChapter = viewModel::changeChapter,
            onClickThemeSettings = navController::navigateToSettingsThemeDestination,
            onZoomImage = navController::navigateToImageViewerDialog
        )
    }
    colorPickerDialog()
    imageViewerDialog()
}

fun NavController.navigateToBookReaderDestination(bookId: Int, chapterId: Int, context: Context) {
    if (this.currentBackStackEntry?.destination?.route?.contains("indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route.Book.Detail") != true) {
        return
    }
    val entry = this.getBackStackEntry<Route.Book>()
    val viewModel = ViewModelProvider.create(
        entry,
        HiltViewModelFactory(
            context = context,
            delegateFactory = entry.defaultViewModelProviderFactory
        ),
    )[ReaderViewModel::class.java]
    viewModel.bookId = bookId
    viewModel.changeChapter(chapterId)
    this.navigate(Route.Book.Reader)
}

private fun NavGraphBuilder.colorPickerDialog() {
    dialog<Route.Book.ColorPickerDialog> { entry ->
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<ColorPickerDialogViewModel>()
        val route = entry.toRoute<Route.Book.ColorPickerDialog>()
        val selectedColor by viewModel.init(route.colorUserDataPath).collectAsState(Color.Unspecified)
        ColorPickerDialog(
            onDismissRequest = { navController.popBackStack() },
            onConfirmation = {
                viewModel.changeBackgroundColor(it)
                navController.popBackStack()
            },
            selectedColor = selectedColor ?: Color.Unspecified,
            colors = route.colors.map { Color(if (it < 0) return@map Color.Unspecified else it) }
        )
    }
}

fun NavController.navigateToColorPickerDialog(colorUserDataPath: String, colors: List<Long>) {
    if (!this.isResumed()) return
    navigate(Route.Book.ColorPickerDialog(colorUserDataPath, colors.toLongArray()))
}

private fun NavGraphBuilder.imageViewerDialog() {
    dialog<Route.Book.ImageViewerDialog>(
        dialogProperties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) { entry ->
        val navController = LocalNavController.current
        val route = entry.toRoute<Route.Book.ImageViewerDialog>()

        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        ImageViewerScreen(
            imageUrl = route.imageUrl,
            onDismissRequest = { navController.popBackStack() },
            onClickSave = {
                urlToBitmap(
                    scope = coroutineScope,
                    imageURL = route.imageUrl,
                    context = context,
                    onSuccess = { bitmap ->
                        coroutineScope.launch {
                            val savedName = saveBitmapAsPng(context, bitmap)
                            withContext(Dispatchers.Main) {
                                if (savedName != null) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.saved_to_pictures_dir, savedName),
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(context, context.getString(R.string.save_failed), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    onError = { error ->
                        Log.d("ImageViewer", "Failed to save image: ${error.message}")
                        Toast.makeText(context, context.getString(R.string.save_failed), Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
}

fun NavController.navigateToImageViewerDialog(
    imageUrl: String
) {
    navigate(
        Route.Book.ImageViewerDialog(
            imageUrl = imageUrl
        )
    )
}