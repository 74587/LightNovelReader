package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.utils.ApkSignatureInfo
import io.nightfish.lightnovelreader.api.ui.theme.AppTypography

@Composable
fun InstallProgressDialog(
    state: InstallDialogState,
    progress: Float?,
    onClickClose: () -> Unit,
    onConfirmDecision: (Boolean) -> Unit
) {
    if (!state.visible) return

    AlertDialog(
        onDismissRequest = { },
        title = {
            Column {
                val titleText = state.pluginAnnotation?.name?.takeIf { it.isNotEmpty() } ?: "正在准备"
                Text(text = titleText, style = AppTypography.titleLarge, color = colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                if (state.packageName.isNotEmpty()) {
                    Text(
                        text = buildString {
                            append(state.packageName)
                            val ver = state.pluginAnnotation?.versionName.orEmpty()
                            if (ver.isNotEmpty()) append("\n版本 $ver")
                        },
                        style = AppTypography.bodyMedium
                    )
                }
            }
        },
        text = {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                InstallIndicator(state = state, progress = progress)
                Spacer(Modifier.width(20.dp))
                val msg = when {
                    state.error -> state.phase.ifEmpty { "安装失败" }
                    state.finished -> "插件安装完成"
                    else -> state.phase
                }
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = msg,
                    style = AppTypography.bodyMedium
                )
            }
        },
        confirmButton = {
            when {
                state.error || state.finished -> {
                    TextButton(onClick = onClickClose) { Text(text = stringResource(android.R.string.ok)) }
                }
                state.confirm == InstallDialogState.Confirm.InvalidSig -> {
                    TextButton(onClick = { onConfirmDecision(true) }) { Text("仍要安装") }
                }
                state.confirm == InstallDialogState.Confirm.Upgrade -> {
                    TextButton(onClick = { onConfirmDecision(true) }) { Text(text = stringResource(R.string.next)) }
                }
                else -> {
                    TextButton(onClick = {}, enabled = false) { Text(text = stringResource(R.string.next)) }
                }
            }
        },
        dismissButton = {
            val canShowCancel = !state.finished &&
                    !state.error &&
                    (state.confirm != InstallDialogState.Confirm.None || progress == null)
            if (canShowCancel) {
                TextButton(
                    onClick = {
                        if (state.confirm != InstallDialogState.Confirm.None) onConfirmDecision(false)
                        else onClickClose()
                    }
                ) { Text(text = stringResource(R.string.abort)) }
            }
        }
    )
}

@Composable
private fun InstallIndicator(
    state: InstallDialogState,
    progress: Float?
) {
    val indicatorSize = Modifier.size(36.dp)
    when {
        state.confirm != InstallDialogState.Confirm.None -> {
            Box(
                modifier = indicatorSize
                    .background(color = colorScheme.error.copy(alpha = 0.9f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.info_24px),
                    contentDescription = "confirm",
                    tint = colorScheme.surface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        state.error -> {
            ErrorIndicator()
        }
        progress != null -> {
            val anim by animateFloatAsState(
                targetValue = progress.coerceIn(0f, 1f),
                animationSpec = tween(220, easing = FastOutSlowInEasing),
                label = "install_progress"
            )
            CircularProgressIndicator(progress = { anim }, modifier = indicatorSize)
        }
        state.finished -> {
            DoneIndicator()
        }
        else -> {
            CircularProgressIndicator(modifier = indicatorSize)
        }
    }
}

@Composable
fun DeleteProgressDialog(
    state: DeleteDialogState,
    onClose: () -> Unit
) {
    if (!state.visible) return

    AlertDialog(
        onDismissRequest = { },
        title = { Text(text = "删除 ${state.pluginName}", style = AppTypography.titleLarge, color = colorScheme.onSurface) },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.finished) DoneIndicator() else CircularProgressIndicator(modifier = Modifier.size(36.dp))
                Spacer(Modifier.width(16.dp))
                Text(text = state.phase, style = AppTypography.bodyMedium)
            }
        },
        confirmButton = {
            TextButton(onClick = onClose, enabled = state.finished) {
                Text(text = stringResource(android.R.string.ok))
            }
        }
    )
}

@Composable
fun UpdateCheckDialog(
    state: UpdateDialogState,
    downloadProgress: Float?,
    onClose: () -> Unit,
    onConfirmUpdate: (String) -> Unit
) {
    if (!state.visible) return

    AlertDialog(
        onDismissRequest = { },
        title = {
            Column {
                Text(text = "检查更新", style = AppTypography.titleLarge, color = colorScheme.onSurface)
                Spacer(Modifier.height(6.dp))
                Text(text = state.pluginName, style = AppTypography.bodyLarge)
            }
        },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val indicatorModifier = Modifier.size(36.dp)
                when {
                    state.updateSuccess -> DoneIndicator()
                    state.isChecking -> CircularProgressIndicator(modifier = indicatorModifier)
                    downloadProgress != null -> {
                        val anim by animateFloatAsState(
                            targetValue = downloadProgress.coerceIn(0f, 1f),
                            animationSpec = tween(220, easing = FastOutSlowInEasing),
                            label = "update_progress"
                        )
                        CircularProgressIndicator(progress = { anim }, modifier = indicatorModifier)
                    }
                    state.isLatest -> DoneIndicator()
                    state.hasUpdate -> HasUpdateIndicator()
                    state.isError -> ErrorIndicator()
                    else -> CircularProgressIndicator(modifier = indicatorModifier)
                }

                Spacer(Modifier.width(16.dp))

                val msg = state.message ?: when {
                    state.isChecking -> "正在检查更新…"
                    state.isLatest -> "已是最新版本"
                    state.isError -> "错误"
                    downloadProgress != null -> {
                        if (downloadProgress < 0.75f)
                            "下载中 ${(downloadProgress / 0.75f * 100).toInt().coerceAtMost(100)}%"
                        else
                            "解压中 ${(((downloadProgress - 0.75f) / 0.25f) * 100).toInt().coerceAtMost(100)}%"
                    }
                    else -> ""
                }
                Text(text = msg, style = AppTypography.labelMedium)
            }
        },
        confirmButton = {
            Row(Modifier.animateContentSize()) {
                if (state.isError || downloadProgress != null) return@AlertDialog
                if (state.hasUpdate) {
                    TextButton(onClick = { onConfirmUpdate(state.pluginId) }) { Text(text = "下载并安装") }
                } else {
                    TextButton(onClick = onClose, enabled = (!state.isChecking || state.updateSuccess)) {
                        Text(text = stringResource(android.R.string.ok))
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onClose,
                enabled = (downloadProgress != null || state.hasUpdate || state.isChecking || state.isError)
            ) { Text(text = stringResource(android.R.string.cancel)) }
        }
    )
}

@Composable
fun PluginNoSignatureDialog(
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(text = "关于插件签名", style = AppTypography.titleLarge, color = colorScheme.onSurface)
        },
        text = {
            Column {
                Text(
                    text = "插件签名可被用于确认插件文件的完整性与来源。\n允许安装未签名的插件，但请务必确认文件来自可信的渠道。",
                    style = AppTypography.bodyMedium
                )
                Text(
                    modifier = Modifier.padding(top = 20.dp, bottom = 14.dp),
                    text = "对插件开发者的建议",
                    style = AppTypography.titleSmall,
                    color = colorScheme.onSurface
                )
                Text("建议为插件生成并使用固定的签名证书，以便在版本更新时维持一致性，并确保插件在分发过程中不被篡改。")
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) {
                Text(text = stringResource(android.R.string.ok))
            }
        }
    )
}

@Composable
fun PluginSignatureDialog(
    signatureInfo: List<ApkSignatureInfo>?,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onClose() },
        title = {
            Text(
                text = "签名信息",
                style = AppTypography.titleLarge,
                color = colorScheme.onSurface
            )
        },
        text = {
            if (signatureInfo.isNullOrEmpty()) {
                Text("无签名信息")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    signatureInfo.forEachIndexed { index, sig ->
                        Column {
                            Text(
                                text = "#${index + 1}",
                                style = AppTypography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("所有者: ${sig.subject}")
                            Text("公钥: ${sig.publicKeyAlgorithm}, ${sig.publicKeyLength}-bit")
                            Text("SHA256: ${sig.sha256}")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) {
                Text(text = stringResource(android.R.string.ok))
            }
        }
    )
}


@Composable
private fun DoneIndicator() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(color = colorScheme.primary, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(R.drawable.done_outline_24px), contentDescription = "done", tint = colorScheme.surface, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun HasUpdateIndicator() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(color = colorScheme.primary, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(R.drawable.downloading_24px), contentDescription = "downloading", tint = colorScheme.surface, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ErrorIndicator() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(color = colorScheme.error, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(R.drawable.close_24px), contentDescription = "close", tint = colorScheme.surface, modifier = Modifier.size(20.dp))
    }
}