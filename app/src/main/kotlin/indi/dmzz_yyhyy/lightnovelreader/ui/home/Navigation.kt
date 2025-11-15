package indi.dmzz_yyhyy.lightnovelreader.ui.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.bookshelfNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.explorationNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.readingNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.settingsNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.fadeEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.fadeExit
import indi.dmzz_yyhyy.lightnovelreader.utils.fadePopEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.fadePopExit
import indi.dmzz_yyhyy.lightnovelreader.utils.isInMainNavigation

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.homeNavigation(sharedTransitionScope: SharedTransitionScope) {
    navigation<Route.Main>(
        startDestination = Route.Main.Reading,
        enterTransition = {
            if (isInMainNavigation(initialState.destination, targetState.destination)) fadeEnter()
            else null
        },
        exitTransition = {
            fadeExit()
        },
        popEnterTransition = {
            if (isInMainNavigation(initialState.destination, targetState.destination)) fadePopEnter()
            else null
        },
        popExitTransition = {
            if (isInMainNavigation(initialState.destination, targetState.destination)) fadePopExit()
            else null
        }
    ) {
        readingNavigation(sharedTransitionScope)
        explorationNavigation(sharedTransitionScope)
        bookshelfNavigation(sharedTransitionScope)
        settingsNavigation(sharedTransitionScope)
    }
}

@Suppress("unused")
fun NavController.navigateToHomeNavigation() {
    navigate(Route.Main)
}

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun HomeNavigateBar(
    selectedRoute: Any?,
    controller: NavController,
) {
    fun <T: Any> NavController.navigateSingleTopTo(route: T) {
        navigate(route) {
            launchSingleTop = true
            restoreState = true
            val start = graph.findStartDestination().id
            popUpTo(start) { saveState = true }
        }
    }

    val isReading = selectedRoute is Route.Main.Reading
    val isBookshelf = selectedRoute is Route.Main.Bookshelf
    val isExploration = selectedRoute is Route.Main.Exploration
    val isSettings = selectedRoute is Route.Main.Settings

    val avdReading = AnimatedImageVector.animatedVectorResource(R.drawable.animated_book)
    val avdShelf = AnimatedImageVector.animatedVectorResource(R.drawable.animated_bookshelf)
    val avdExplore = AnimatedImageVector.animatedVectorResource(R.drawable.animated_exploration)
    val avdSettings = AnimatedImageVector.animatedVectorResource(R.drawable.animated_settings)

    NavigationBar {
        NavigationBarItem(
            selected = isReading,
            onClick = { controller.navigateSingleTopTo(Route.Main.Reading) },
            icon = { Icon(painter = rememberAnimatedVectorPainter(avdReading, isReading), null) },
            label = { Text(stringResource(R.string.nav_reading), maxLines = 1) }
        )
        NavigationBarItem(
            selected = isBookshelf,
            onClick = { controller.navigateSingleTopTo(Route.Main.Bookshelf) },
            icon = { Icon(painter = rememberAnimatedVectorPainter(avdShelf, isBookshelf), null) },
            label = { Text(stringResource(R.string.nav_bookshelf), maxLines = 1) }
        )
        NavigationBarItem(
            selected = isExploration,
            onClick = { controller.navigateSingleTopTo(Route.Main.Exploration) },
            icon = { Icon(painter = rememberAnimatedVectorPainter(avdExplore, isExploration), null) },
            label = { Text(stringResource(R.string.nav_explore), maxLines = 1) }
        )
        NavigationBarItem(
            selected = isSettings,
            onClick = { controller.navigateSingleTopTo(Route.Main.Settings) },
            icon = { Icon(painter = rememberAnimatedVectorPainter(avdSettings, isSettings), null) },
            label = { Text(stringResource(R.string.nav_settings), maxLines = 1) }
        )
    }
}
