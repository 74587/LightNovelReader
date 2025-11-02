package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings

import android.net.Uri
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.work.OneTimeWorkRequest
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTypography
import indi.dmzz_yyhyy.lightnovelreader.ui.SharedContentKey
import indi.dmzz_yyhyy.lightnovelreader.ui.home.HomeNavigateBar
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list.AboutSettingsList
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list.DataSettingsList
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list.DisplaySettingsList
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list.ReadingSettingsList
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list.UpdatesSettingsList
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SettingsScreen(
    controller: NavController,
    selectedRoute: Any,
    settingState: SettingState,
    updatePhase: String,
    checkUpdate: () -> Unit,
    importData: (Uri) -> OneTimeWorkRequest,
    onClickLogcat: () -> Unit,
    onClickChangeSource: () -> Unit,
    onClickExportUserData: () -> Unit,
    onClickDebugMode: () -> Unit,
    onClickThemeSettings: () -> Unit,
    onClickTextFormatting: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    with(sharedTransitionScope) {
        Scaffold(
            topBar = { TopBar(scrollBehavior) },
            bottomBar = {
                HomeNavigateBar(
                    Modifier.sharedElement(
                        sharedTransitionScope.rememberSharedContentState(SharedContentKey.HomeNavigateBar),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                    selectedRoute,
                    controller
                )
            },
            snackbarHost = {
                SnackbarHost(LocalSnackbarHost.current)
            }
        ) {
            LazyColumn (
                Modifier.padding(it)
            ) {
                item {
                    SettingsCategory(
                        title = stringResource(R.string.app_updates)
                    ) {
                        UpdatesSettingsList(
                            updatePhase = updatePhase,
                            settingState = settingState,
                            checkUpdate = checkUpdate,
                        )
                    }
                }

                item {
                    SettingsCategory(
                        title = stringResource(R.string.reading_settings),
                    ) {
                        ReadingSettingsList(
                            settingState = settingState,
                            onClickTheme = onClickThemeSettings,
                            onClickTextFormatting = onClickTextFormatting
                        )
                    }
                }
                item {
                    SettingsCategory(
                        title = stringResource(R.string.display_settings),
                    ) {
                        DisplaySettingsList(
                            settingState = settingState
                        )
                    }
                }
                item {
                    SettingsCategory(
                        title = stringResource(R.string.data_settings),
                    ) {
                        DataSettingsList(
                            onClickChangeSource = onClickChangeSource,
                            onClickExportUserData = onClickExportUserData,
                            settingState = settingState,
                            importData = importData,
                            onClickLogcat = onClickLogcat
                        )
                    }
                }
                item {
                    SettingsCategory(
                        title = stringResource(R.string.about_settings),
                    ) {
                        AboutSettingsList(
                            onClickDebugMode = onClickDebugMode
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.nav_settings),
                style = AppTypography.titleTopBar
            )
        },
        navigationIcon = {
            Box(Modifier.size(48.dp)) {
                Icon(
                    modifier = Modifier.align(Alignment.Center),
                    painter = painterResource(id = R.drawable.outline_settings_24px),
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun SettingsCategory(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    title?.let {
        Text(
            modifier = Modifier.padding(horizontal = 24.dp)
                .padding(vertical = 12.dp),
            text = it,
            color = colorScheme.onSurfaceVariant,
            style = AppTypography.titleSmall,
            fontWeight = FontWeight.W600
        )
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(16.dp)),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        content()
    }
}
