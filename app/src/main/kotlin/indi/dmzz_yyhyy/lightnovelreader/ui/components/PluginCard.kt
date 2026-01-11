package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInfo
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.horizontalPadding
import kotlinx.coroutines.delay

@Composable
fun PluginCard(
    modifier: Modifier = Modifier,
    enabledPluginList: List<String>,
    isErrorDisabled: Boolean,
    pluginInfo: PluginInfo,
    onClickDetail: (String) -> Unit,
    onClickSwitch: (String) -> Unit,
    onClickDelete: (String) -> Unit,
    onClickKeyAlert: () -> Unit,
    onClickErrorAlert: () -> Unit,
    onClickCheckUpdate: (String) -> Unit,
    onClickOptimizePlugin: (String) -> Unit,
    onClickShowSignatures: (String) -> Unit
) {
    val enabled = enabledPluginList.contains(pluginInfo.id)
    val disabledByError = isErrorDisabled && !enabled

    var switchEnabled by remember { mutableStateOf(true) }
    var menuExpanded by remember { mutableStateOf(false) }

    val containerColor by animateColorAsState(
        targetValue = when {
            disabledByError -> colorScheme.errorContainer.copy(alpha = 0.22f)
            enabled -> colorScheme.surfaceContainerLow
            else -> colorScheme.surfaceContainer
        },
        label = "cardColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = horizontalPadding)
            .clickable { onClickDetail(pluginInfo.id) },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    color = if (enabled) colorScheme.primaryContainer else colorScheme.surfaceContainerHighest,
                    contentColor = if (enabled) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Box(
                        modifier = Modifier.size(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.extension_24px),
                            contentDescription = "plugin"
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = pluginInfo.name,
                            style = typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!pluginInfo.signatures.isNullOrEmpty()) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = colorScheme.primaryContainer,
                                contentColor = colorScheme.onPrimaryContainer,
                                shape = RoundedCornerShape(50.dp),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.key_24px),
                                    contentDescription = "signed",
                                    modifier = Modifier.padding(2.dp).size(16.dp)
                                )
                            }
                        }
                        /* TODO: has datasource indicator */
                       /* if (false) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = colorScheme.primaryContainer,
                                contentColor = colorScheme.onPrimaryContainer,
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.electrical_services_24px),
                                    contentDescription = "provides data source",
                                    modifier = Modifier.padding(2.dp).size(16.dp)
                                )
                            }
                        }*/
                    }

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = "${pluginInfo.versionName} · ${stringResource(R.string.plugin_by_author, pluginInfo.author)}",
                        style = typography.labelMedium,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = pluginInfo.description,
                        style = typography.bodyMedium,
                        color = colorScheme.onSurface,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            painter = painterResource(R.drawable.more_vert_24px),
                            contentDescription = "menu"
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.plugin_optimize),
                                    style = typography.bodyLarge
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onClickOptimizePlugin(pluginInfo.id)
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.plugin_signature_info_title),
                                    style = typography.bodyLarge
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onClickShowSignatures(pluginInfo.id)
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "删除插件",
                                    style = typography.bodyLarge
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onClickDelete(pluginInfo.id)
                            }
                        )
                    }

                    Switch(
                        checked = enabled,
                        enabled = !disabledByError && switchEnabled,
                        onCheckedChange = {
                            if (!disabledByError && switchEnabled) {
                                onClickSwitch(pluginInfo.id)
                                switchEnabled = false
                            }
                        }
                    )

                    LaunchedEffect(switchEnabled) {
                        if (!switchEnabled) {
                            delay(900)
                            switchEnabled = true
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pluginInfo.signatures == null) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = onClickKeyAlert) {
                        Icon(
                            painter = painterResource(R.drawable.key_off_24px),
                            contentDescription = "invalid_signature",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = "签名无效")
                    }
                }
                if (disabledByError) {
                    OutlinedButton(
                        onClick = onClickErrorAlert,
                        colors = ButtonDefaults.outlinedButtonColors().copy(
                            containerColor = colorScheme.errorContainer.copy(alpha = 0.35f),
                            contentColor = colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.release_alert_24px),
                            contentDescription = "error",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = "错误")
                    }
                }
            }
        }
    }
}
