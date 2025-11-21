package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    refresh: () -> Unit,
    uiState: ExploreUiState,
    content: @Composable (AnimatedVisibilityScope.() -> Unit)
) {
    val scope = rememberCoroutineScope()
    val rememberPullToRefreshState = rememberPullToRefreshState()
    val snackbarHostState = LocalSnackbarHost.current
    val context = LocalContext.current

    AnimatedVisibility(
        modifier = modifier,
        visible = uiState.isOffLine,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = uiState.isRefreshing,
            state = rememberPullToRefreshState,
            onRefresh = {
                refresh.invoke()
                scope.launch {
                    rememberPullToRefreshState.animateToHidden()
                }
            }
        ) {
            LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                item {
                    EmptyPage(
                        icon = painterResource(R.drawable.link_off_24px),
                        title = stringResource(R.string.offline),
                        description = stringResource(R.string.offline_desc)
                    ) {
                        IconButton({
                            showSnackbar(
                                coroutineScope = scope,
                                hostState = snackbarHostState,
                                message = context.getString(R.string.network_error_message),
                                actionLabel = context.getString(android.R.string.ok),
                                duration = SnackbarDuration.Long
                            ) { }
                        }) {
                            Icon(painterResource(
                                id = R.drawable.help_24px),
                                contentDescription = "help",
                                tint = colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = !uiState.isOffLine,
        enter = fadeIn(),
        exit = fadeOut(),
        content = content
    )
}