package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInstallerOperation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.DeleteProgressDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.InstallProgressDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.UpdateCheckDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import io.nightfish.lightnovelreader.api.ui.LocalNavController

fun NavGraphBuilder.pluginInstallerDialog() {
    dialog<Route.PluginInstallerDialog> { entry ->

        val snackbarHostState = LocalSnackbarHost.current
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<PluginInstallerDialogViewModel>()

        val route = entry.toRoute<Route.PluginInstallerDialog>()
        val source = route.source

        LaunchedEffect(source) {
            if (source.isNotBlank()) viewModel.setSource(source)
        }

        LaunchedEffect(viewModel) {
            viewModel.closeDialogFlow.collect { navController.popBackStack() }
        }
        LaunchedEffect(viewModel) {
            viewModel.snackbarFlow.collect { message ->
                snackbarHostState.showSnackbar(message, withDismissAction = true)
            }
        }

        val progress by viewModel.installProgress.collectAsState(initial = null)

        when (viewModel.dialogType) {
            PluginInstallerOperation.INSTALL -> {
                InstallProgressDialog(
                    state = viewModel.installState,
                    progress = progress,
                    onClickClose = { viewModel.onCancelOperation() },
                    onConfirmDecision = { confirm -> viewModel.respondUserDecision(confirm) }
                )
            }
            PluginInstallerOperation.UNINSTALL -> {
                DeleteProgressDialog(
                    state = viewModel.deleteState,
                    onClose = { viewModel.onCloseDialog() }
                )
            }
            PluginInstallerOperation.UPGRADE -> {
                UpdateCheckDialog(
                    state = viewModel.updateState,
                    downloadProgress = null,
                    onClose = { viewModel.onCloseDialog() },
                    onConfirmUpdate = { _ -> viewModel.respondUserDecision(true) }
                )
            }
            else -> Unit
        }
    }
}

fun NavController.navigateToPluginInstallerDialog(string: String) {
    navigate(Route.PluginInstallerDialog(string))
}