package indi.dmzz_yyhyy.lightnovelreader.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalScaffoldPadding
import indi.dmzz_yyhyy.lightnovelreader.ui.book.bookNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.components.LnrSnackbar
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.addBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.markAllChaptersAsReadDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.updatesAvailableDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.downloadmanager.downloadManager
import indi.dmzz_yyhyy.lightnovelreader.ui.home.HomeNavigateBar
import indi.dmzz_yyhyy.lightnovelreader.ui.home.homeNavigation
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalClaimSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.currentMainRoute
import indi.dmzz_yyhyy.lightnovelreader.utils.expandEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandExit
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopExit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun LightNovelReaderNavHost(
    navController: NavHostController
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var claimCount by remember { mutableIntStateOf(0) }
    val claim: (Boolean) -> Unit = remember {
        { take -> claimCount += if (take) 1 else -1 }
    }


    CompositionLocalProvider(
        LocalNavController provides navController,
        LocalSnackbarHost provides snackbarHostState,
        LocalClaimSnackbarHost provides claim,
    ) {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDest = backStackEntry?.destination
        val selectedRoute = currentDest.currentMainRoute()
        val showBottomBar = selectedRoute != null

        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = expandVertically(tween(300)),
                    exit = shrinkVertically(tween(300))
                ) {
                    Box {
                        HomeNavigateBar(
                            selectedRoute = selectedRoute,
                            controller = navController
                        )
                    }
                }
            },
            snackbarHost = {
                if (claimCount == 0) {
                    SnackbarHost(snackbarHostState) { data -> LnrSnackbar(data) }
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            CompositionLocalProvider(
                LocalScaffoldPadding provides innerPadding
            ) {
                SharedTransitionLayout {
                    NavHost(
                        modifier = Modifier.fillMaxSize(),
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
                        markAllChaptersAsReadDialog()
                    }
                }
            }
        }
    }
}

