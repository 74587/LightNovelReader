package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import android.content.Intent
import android.os.Build
import android.provider.DocumentsContract
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToPluginInstallerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.detail.navigateToSettingsPluginManagerDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.detail.settingsPluginManagerDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.repository.navigateToSettingsPluginRepositoryDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.repository.settingsPluginRepositoryDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import indi.dmzz_yyhyy.lightnovelreader.utils.uriLauncher
import io.nightfish.lightnovelreader.api.ui.LocalNavController

fun NavGraphBuilder.settingsPluginManagerNavigation() {
    navigation<Route.Main.Settings.PluginManager>(
        startDestination = Route.Main.Settings.PluginManager.Home
    ) {
        settingsPluginManagerHomeDestination()
        settingsPluginManagerDetailDestination()
        settingsPluginRepositoryDestination()
    }
}

fun NavGraphBuilder.settingsPluginManagerHomeDestination() {
    composable<Route.Main.Settings.PluginManager.Home> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<PluginManagerViewModel>()
        val launcher = uriLauncher { uri ->
            navController.navigateToPluginInstallerDialog(uri.toString())
        }
        val enabledPluginList by viewModel.enabledPluginFlow.collectAsState(emptyList())
        var showPluginNoSignatureDialog by remember { mutableStateOf(false) }
        var showPluginSignatureDialog: String? by remember { mutableStateOf(null) }
        val snackbarHostState = LocalSnackbarHost.current
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(viewModel.snackbarFlow) {
            viewModel.snackbarFlow.collect { message ->
                snackbarHostState.showSnackbar(message, withDismissAction = true)
            }
        }

        PluginManagerScreen(
            enabledPluginList = enabledPluginList,
            onClickBack = navController::popBackStackIfResumed,
            onClickDetail = navController::navigateToSettingsPluginManagerDetailDestination,
            onClickSwitch = viewModel::onClickEnabledSwitch,
            onClickDelete = { id ->
                navController.navigateToPluginInstallerDialog("uninstall:$id")
            },
            pluginInfoList = viewModel.pluginList,
            onClickCheckUpdate = { TODO() },
            onClickKeyAlert = {
                showSnackbar(
                    coroutineScope = coroutineScope,
                    hostState = snackbarHostState,
                    message = "插件未被签名。",
                    actionLabel = "了解更多"
                ) {
                    when (it) {
                        SnackbarResult.Dismissed -> { }
                        SnackbarResult.ActionPerformed -> { showPluginNoSignatureDialog = true }
                    }
                }
            },
            onClickPluginRepo = navController::navigateToSettingsPluginRepositoryDestination,
            onClickInstall = {
                val initUri = DocumentsContract.buildDocumentUri(
                    "com.android.externalstorage.documents",
                    "primary:Documents"
                )
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        putExtra(DocumentsContract.EXTRA_INITIAL_URI, initUri)
                }
                launcher.launch(Intent.createChooser(intent, "选择插件"))
            },
            onClickOptimize = { TODO() },
            onClickShowSignatures = { id ->
                showPluginSignatureDialog = id
            }
        )

        if (showPluginNoSignatureDialog) {
            PluginNoSignatureDialog(onClose = { showPluginNoSignatureDialog = false })
        }

        showPluginSignatureDialog?.let { pluginIdToShow ->
            if (pluginIdToShow.isNotEmpty()) {
                PluginSignatureDialog(
                    onClose = { showPluginSignatureDialog = null },
                    signatureInfo = viewModel.pluginList.first { it.id == pluginIdToShow }.signatures
                )
            }
        }
    }
}

fun NavController.navigateToSettingsPluginManagerHomeDestination() {
    navigate(Route.Main.Settings.PluginManager.Home)
}
