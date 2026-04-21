package indi.dmzz_yyhyy.lightnovelreader.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.rememberNavController
import android.content.Intent
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.UpdatesAvailableDialogViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToPluginInstallerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateUpdatesAvailableDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.LightNovelReaderNavHost
import io.nightfish.lightnovelreader.api.ui.ReaderStyle
import kotlinx.coroutines.flow.Flow

@Composable
fun LightNovelReaderApp(
    onBuildNavHost: NavGraphBuilder.() -> Unit,
    readerStyle: ReaderStyle,
    imageHeaderGetter: () -> Map<String, String>,
    intentFlow: Flow<Intent>,
) {
    val navController = rememberNavController()
    val updatesAvailableDialogViewModel = hiltViewModel<UpdatesAvailableDialogViewModel>()
    val available by updatesAvailableDialogViewModel.availableFlow.collectAsState(false)
    LaunchedEffect(available) {
        if (available) {
            updatesAvailableDialogViewModel.resetAvailable()
            navController.navigateUpdatesAvailableDialog()
        }
    }
    LaunchedEffect(Unit) {
        intentFlow.collect { intent ->
            if (intent.action == Intent.ACTION_VIEW) {
                intent.data?.toString()?.let { uriString ->
                    navController.navigateToPluginInstallerDialog(uriString)
                }
            }
        }
    }
    LightNovelReaderNavHost(navController, onBuildNavHost, readerStyle, imageHeaderGetter)
}
