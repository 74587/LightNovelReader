package indi.dmzz_yyhyy.lightnovelreader.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import indi.dmzz_yyhyy.lightnovelreader.ui.book.bookNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.addBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.updatesAvailableDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.homeNavigation

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LightNovelReaderNavHost(
    navController: NavHostController
) {
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = Route.Home,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            homeNavigation(navController, this@SharedTransitionLayout)
            bookNavigation(navController)
            updatesAvailableDialog(navController)
            addBookToBookshelfDialog(navController)
        }
    }
}