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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.bookshelfNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.explorationNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.homeReadingDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.settingsNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.homeNavigation(navController: NavController, sharedTransitionScope: SharedTransitionScope) {
    navigation<Route.Home>(
        startDestination = Route.Home.Reading
    ) {
        homeReadingDestination(navController, sharedTransitionScope)
        explorationNavigation(navController, sharedTransitionScope)
        bookshelfNavigation(navController, sharedTransitionScope)
        settingsNavigation(navController, sharedTransitionScope)
    }
}

@Suppress("unused")
fun NavController.navigateToHomeNavigation() {
    navigate(Route.Home)
}

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun HomeNavigateBar(
    modifier: Modifier = Modifier,
    selectedRoute: Any,
    controller: NavController,
) {
    fun <T : Any> coverNavigate(route: T) {
        controller.popBackStack()
        controller.navigate(route)
    }

    NavigationBar(modifier) {
        NavigationBarItem(
            selected = selectedRoute is Route.Home.Reading,
            onClick = {
                if (selectedRoute !is Route.Home.Reading) coverNavigate(Route.Home.Reading)
            },
            icon = {
                Icon(
                    painter = rememberAnimatedVectorPainter(
                        AnimatedImageVector.animatedVectorResource(R.drawable.animated_book),
                        selectedRoute is Route.Home.Reading
                    ),
                    contentDescription = null
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_reading),
                    maxLines = 1
                )
            }
        )
        NavigationBarItem(
            selected = selectedRoute is Route.Home.Bookshelf,
            onClick = {
                if (selectedRoute !is Route.Home.Bookshelf) coverNavigate(Route.Home.Bookshelf)
            },
            icon = {
                Icon(
                    painter = rememberAnimatedVectorPainter(
                        AnimatedImageVector.animatedVectorResource(R.drawable.animated_bookshelf),
                        selectedRoute is Route.Home.Bookshelf
                    ),
                    contentDescription = null
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_bookshelf),
                    maxLines = 1
                )
            }
        )
        NavigationBarItem(
            selected = selectedRoute is Route.Home.Exploration,
            onClick = {
                if (selectedRoute !is Route.Home.Exploration) coverNavigate(Route.Home.Exploration)
            },
            icon = {
                Icon(
                    painter = rememberAnimatedVectorPainter(
                        AnimatedImageVector.animatedVectorResource(R.drawable.animated_exploration),
                        selectedRoute is Route.Home.Exploration
                    ),
                    contentDescription = null
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_explore),
                    maxLines = 1
                )
            }
        )
        NavigationBarItem(
            selected = selectedRoute is Route.Home.Settings,
            onClick = {
                if (selectedRoute !is Route.Home.Settings) coverNavigate(Route.Home.Settings)
            },
            icon = {
                Icon(
                    painter = rememberAnimatedVectorPainter(
                        AnimatedImageVector.animatedVectorResource(R.drawable.animated_settings),
                        selectedRoute is Route.Home.Settings
                    ),
                    contentDescription = null
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_settings),
                    maxLines = 1
                )
            }
        )
    }
}
