package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.advanced.translate

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.settingsTranslateDestination(navController: NavController) {
    composable<Route.Home.Settings.Translate> {
        val viewModel = hiltViewModel<TranslateViewModel>()
        TranslateScreen(
            onClickBack = navController::popBackStackIfResumed,
            settingState = viewModel.settingState,
        )
    }
}

fun NavController.navigateToTranslate() {
    navigate(Route.Home.Settings.Translate)
}