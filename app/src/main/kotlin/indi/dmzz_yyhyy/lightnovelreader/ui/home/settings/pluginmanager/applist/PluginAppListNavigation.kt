package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.applist

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginManagerViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import io.nightfish.lightnovelreader.api.ui.LocalNavController

fun NavGraphBuilder.settingsPluginAppListDestination() {
    composable<Route.Main.Settings.PluginManager.AppList> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<PluginManagerViewModel>()

        PluginAppListScreen(
            appPluginList = viewModel.scannedPluginApps,
            onClickBack = navController::popBackStackIfResumed
        )
    }
}

fun NavController.navigateToSettingsPluginAppListDestination() {
    navigate(Route.Main.Settings.PluginManager.AppList)
}
