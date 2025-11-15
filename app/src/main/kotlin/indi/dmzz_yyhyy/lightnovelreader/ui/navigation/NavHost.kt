package indi.dmzz_yyhyy.lightnovelreader.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import indi.dmzz_yyhyy.lightnovelreader.ui.book.bookNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.addBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.markAllChaptersAsReadDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.pluginInstallerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.updatesAvailableDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.downloadmanager.downloadManager
import indi.dmzz_yyhyy.lightnovelreader.ui.home.homeNavigation
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.expandEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandExit
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopExit
import io.nightfish.lightnovelreader.api.ui.LocalNavController

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LightNovelReaderNavHost(
    navController: NavHostController,
    onBuildNavHost: NavGraphBuilder.() -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    CompositionLocalProvider(
        LocalNavController provides navController,
        LocalSnackbarHost provides snackbarHostState
    ) {
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = Route.Main,
                enterTransition = { expandEnter() },
                exitTransition = { expandExit() },
                popEnterTransition = { expandPopEnter() },
                popExitTransition = { expandPopExit() }
            ) {
                homeNavigation(this@SharedTransitionLayout)
                bookNavigation()
                updatesAvailableDialog()
                addBookToBookshelfDialog()
                downloadManager()
                pluginInstallerDialog()
                markAllChaptersAsReadDialog()
                onBuildNavHost.invoke(this)
            }
        }
    }
}