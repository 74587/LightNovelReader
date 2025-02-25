package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.advanced.debug

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.settingsDebugDestination(navController: NavController) {
    composable<Route.Home.Settings.Debug> {
        val viewModel = hiltViewModel<DebugScreenViewModel>()
        DebugScreen(
            onClickBack = navController::popBackStackIfResumed,
            onClickQuery = viewModel::runSQLCommand,
            result = viewModel.result
        )
    }
}

fun NavController.navigateToDebug() {
    navigate(Route.Home.Settings.Debug)
}