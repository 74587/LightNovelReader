package indi.dmzz_yyhyy.lightnovelreader.ui.components

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import indi.dmzz_yyhyy.lightnovelreader.ui.home.HomeNavigateBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun LnrNavigationBar(
    showBottomBar: Boolean,
    selectedRoute: Any?,
    navController: NavController
) {
    val navHeight =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val showFake =
        !showBottomBar &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.P &&
                navHeight > 0.dp

    AnimatedVisibility(
        visible = showBottomBar,
        enter = expandVertically(tween(300)),
        exit = shrinkVertically(tween(300))
    ) {
        HomeNavigateBar(
            selectedRoute = selectedRoute,
            controller = navController
        )
    }

    AnimatedVisibility(
        visible = showFake,
        enter = expandVertically(tween(300)),
        exit = shrinkVertically(tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(navHeight)
                .background(
                    colorScheme.surface.copy(alpha = 0.75f)
                )
        )
    }
}