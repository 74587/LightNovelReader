package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInfo
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import io.nightfish.lightnovelreader.api.ui.theme.AppTypography
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginManagerScreen(
    enabledPluginList: List<String>,
    onClickInstall: () -> Unit,
    onClickBack: () -> Unit,
    onClickDetail: (String) -> Unit,
    onClickDelete: (String) -> Unit,
    onClickSwitch: (String) -> Unit,
    onClickKeyAlert: () -> Unit,
    onClickPluginRepo: () -> Unit,
    onClickCheckUpdate: (String) -> Unit,
    pluginInfoList: List<PluginInfo>,
    onClickOptimize: (String) -> Unit,
    onClickShowSignatures: (String) -> Unit
) {
    val enterAlwaysScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            TopBar(
                onClickBack = onClickBack,
                scrollBehavior = enterAlwaysScrollBehavior,
                onClickPluginRepo = onClickPluginRepo
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.padding(end = 12.dp, bottom = 24.dp),
                onClick = onClickInstall,
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.archive_24px),
                        contentDescription = "install"
                    )
                },
                text = {
                    Text("Install plugin")
                }
            )
        },
        snackbarHost = {
            SnackbarHost(LocalSnackbarHost.current)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            ThirdPartyPluginTips()

            Spacer(Modifier.height(8.dp))

            if (pluginInfoList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyPage(
                        icon = painterResource(R.drawable.deployed_code_update_24px),
                        title = "没有插件",
                        description = "安装受支持的 .lnrp 格式插件",
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(pluginInfoList) { plugin ->
                        PluginCard(
                            modifier = Modifier.animateItem(),
                            pluginInfo = plugin,
                            onClickDetail = onClickDetail,
                            enabledPluginList = enabledPluginList,
                            onClickSwitch = onClickSwitch,
                            onClickDelete = onClickDelete,
                            onClickCheckUpdate = onClickCheckUpdate,
                            onClickKeyAlert = onClickKeyAlert,
                            onClickOptimizePlugin = onClickOptimize,
                            onClickShowSignatures = onClickShowSignatures
                        )
                    }
                    item {
                        Spacer(Modifier.height(98.dp))
                    }
                }
            }
        }
    }

}

@Composable
private fun ThirdPartyPluginTips() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.info_24px),
                contentDescription = "warning"
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("来自第三方的插件可以提供额外的数据源或功能。安装插件前，请确保其来源可信。\nLightNovelReader 不对插件可用性负责。", style = AppTypography.bodyMedium)
            }
        }
    }
}

@Composable
private fun PluginCard(
    modifier: Modifier = Modifier,
    enabledPluginList: List<String>,
    pluginInfo: PluginInfo,
    onClickDetail: (String) -> Unit,
    onClickSwitch: (String) -> Unit,
    onClickDelete: (String) -> Unit,
    onClickKeyAlert: () -> Unit,
    onClickCheckUpdate: (String) -> Unit,
    onClickOptimizePlugin: (String) -> Unit,
    onClickShowSignatures: (String) -> Unit
) {
    var switchEnabled by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 8.dp)
                .clickable { showMenu = true },
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(pluginInfo.name, style = AppTypography.titleMedium)
                        Text(pluginInfo.versionName, style = AppTypography.labelMedium, color = colorScheme.secondary)
                        Text("by ${pluginInfo.author}", style = AppTypography.labelMedium, color = colorScheme.secondary)
                    }
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = enabledPluginList.contains(pluginInfo.id),
                        enabled = switchEnabled,
                        onCheckedChange = { checked ->
                            if (switchEnabled) {
                                onClickSwitch(pluginInfo.id)
                                switchEnabled = false
                            }
                        }
                    )
                    LaunchedEffect(switchEnabled) {
                        if (!switchEnabled) {
                            delay(1000)
                            switchEnabled = true
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    /*if (!pluginInfo.updateUrl.isNullOrEmpty()) {
                        Button(onClick = { onClickCheckUpdate(pluginInfo.id) }) {
                            Icon(painterResource(R.drawable.deployed_code_update_24px), null)
                            Spacer(Modifier.width(12.dp))
                            Text("更新")
                        }
                    }*/
                    Spacer(Modifier.weight(1f))
                    if (pluginInfo.signatures == null) {
                        FilledTonalIconButton(onClick = { onClickKeyAlert() }) {
                            Icon(painterResource(R.drawable.key_off_24px), contentDescription = "invalid_signature")
                        }
                    }
                    FilledTonalIconButton(onClick = { onClickDelete(pluginInfo.id) }) {
                        Icon(painterResource(R.drawable.delete_forever_24px), contentDescription = "remove")
                    }
                    FilledTonalButton(onClick = { onClickDetail(pluginInfo.id) }) {
                        Text("详情")
                    }
                }
            }
        }

        DropdownMenu(
            modifier = Modifier.padding(horizontal = 12.dp),
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                enabled = false,
                text = { Text("优化") },
                onClick = {
                    showMenu = false
                    onClickOptimizePlugin(pluginInfo.id)
                }
            )
            DropdownMenuItem(
                text = { Text("签名信息") },
                onClick = {
                    showMenu = false
                    onClickShowSignatures(pluginInfo.id)
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onClickBack: () -> Unit,
    onClickPluginRepo: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "扩展插件",
                style = AppTypography.titleTopBar,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back_24px),
                    contentDescription = "back"
                )
            }
        },
        actions = {
            TextButton(
                onClick = onClickPluginRepo
            ) {
                Text("插件仓库")
            }
        },
        scrollBehavior = scrollBehavior
    )
}