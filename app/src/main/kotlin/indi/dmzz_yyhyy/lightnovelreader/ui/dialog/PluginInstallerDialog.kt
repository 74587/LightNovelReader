package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.DeleteProgressDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.InstallProgressDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginDialogState
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

        val uiState by viewModel.uiState.collectAsState()

        when (val state = uiState) {
            is PluginDialogState.Install -> {
                InstallProgressDialog(
                    state = state.state,
                    onClickClose = { viewModel.onCancelOperation() },
                    onConfirmDecision = { confirm -> viewModel.respondUserDecision(confirm) }
                )
            }
            is PluginDialogState.Uninstall -> {
                DeleteProgressDialog(
                    state = state.state,
                    onClose = { viewModel.onCloseDialog() }
                )
            }
            is PluginDialogState.UpdateCheck -> {
                UpdateCheckDialog(
                    state = state.state,
                    onClose = { viewModel.onCloseDialog() },
                    onConfirmUpdate = { _ -> viewModel.respondUserDecision(true) }
                )
            }
            PluginDialogState.Hidden -> Unit
        }
    }
}

fun NavController.navigateToPluginInstallerDialog(string: String) {
    navigate(Route.PluginInstallerDialog(string))
}
