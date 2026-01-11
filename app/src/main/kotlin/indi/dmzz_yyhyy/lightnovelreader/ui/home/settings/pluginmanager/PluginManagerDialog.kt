package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.utils.ApkSignatureInfo
import indi.dmzz_yyhyy.lightnovelreader.utils.ApkSignatureScheme

@Composable
fun InstallProgressDialog(
    state: InstallDialogState,
    onClickClose: () -> Unit,
    onConfirmDecision: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Column {
                val titleText = state.info?.name?.takeIf { it.isNotEmpty() }
                    ?: stringResource(R.string.plugin_install_preparing)

                Text(text = titleText, style = typography.displayMedium, color = colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))

                state.info?.let { info ->
                    var text = info.packageName
                    if (info.versionName.isNotEmpty()) {
                        text += "\n"
                        text += stringResource(R.string.plugin_version_prefix, info.versionName)
                    }
                    Text(
                        text = text,
                        style = typography.bodyMedium
                    )
                }
            }
        },
        text = {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                InstallIndicator(state = state)
                Spacer(Modifier.width(20.dp))
                val step = state.step
                val msg = when (step) {
                    is InstallStep.AwaitingDecision -> step.decision.message
                    is InstallStep.Completed -> {
                        if (step.message.isNotEmpty()) step.message
                        else if (step.success) stringResource(R.string.plugin_install_completed)
                        else stringResource(R.string.plugin_install_failed)
                    }
                    is InstallStep.Working -> step.message.ifEmpty {
                        stringResource(R.string.plugin_install_preparing)
                    }
                }

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = msg,
                    style = typography.bodyMedium
                )
            }
        },
        confirmButton = {
            when (val step = state.step) {
                is InstallStep.Completed -> {
                    TextButton(onClick = onClickClose) { Text(text = stringResource(android.R.string.ok)) }
                }

                is InstallStep.AwaitingDecision -> {
                    val label = when (step.decision.type) {
                        InstallDecisionType.InvalidSignature -> R.string.plugin_install_anyway
                        InstallDecisionType.Reinstall,
                        InstallDecisionType.Downgrade -> R.string.next
                    }
                    TextButton(onClick = { onConfirmDecision(true) }) {
                        Text(text = stringResource(label))
                    }
                }

                is InstallStep.Working -> {
                    TextButton(onClick = {}, enabled = false) { Text(text = stringResource(R.string.next)) }
                }
            }
        },
        dismissButton = {
            val canShowCancel = state.step !is InstallStep.Completed &&
                    (state.step is InstallStep.AwaitingDecision || state.progress == null)

            if (canShowCancel) {
                TextButton(
                    onClick = {
                        if (state.step is InstallStep.AwaitingDecision) onConfirmDecision(false)
                        else onClickClose()
                    }
                ) { Text(text = stringResource(R.string.abort)) }
            }
        }
    )
}

@Composable
private fun InstallIndicator(
    state: InstallDialogState
) {
    val indicatorSize = Modifier.size(36.dp)
    val step = state.step
    when (step) {
        is InstallStep.AwaitingDecision -> {
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

        is InstallStep.Completed -> {
            if (step.success) DoneIndicator() else ErrorIndicator()
        }

        is InstallStep.Working -> {
            val progress = state.progress
            if (progress != null) {
                val anim by animateFloatAsState(
                    targetValue = progress.coerceIn(0f, 1f),
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    label = "install_progress"
                )
                CircularProgressIndicator(progress = { anim }, modifier = indicatorSize)
            } else {
                CircularProgressIndicator(modifier = indicatorSize)
            }
        }
    }
}

@Composable
fun DeleteProgressDialog(
    state: DeleteDialogState,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = stringResource(R.string.plugin_delete_title, state.pluginName),
                style = typography.displayMedium,
                color = colorScheme.onSurface
            )
        },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (val step = state.step) {
                    is DeleteStep.Completed -> {
                        if (step.success) DoneIndicator() else ErrorIndicator()
                    }
                    is DeleteStep.Working -> {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                val msg = when (val step = state.step) {
                    is DeleteStep.Completed -> step.message
                    is DeleteStep.Working -> step.message
                }
                Text(text = msg, style = typography.bodyMedium)
            }
        },
        confirmButton = {
            val enabled = state.step is DeleteStep.Completed
            TextButton(onClick = onClose, enabled = enabled) {
                Text(text = stringResource(android.R.string.ok))
            }
        }
    )
}

@Composable
fun UpdateCheckDialog(
    state: UpdateDialogState,
    onClose: () -> Unit,
    onConfirmUpdate: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Column {
                Text(
                    text = stringResource(R.string.plugin_update_check_title),
                    style = typography.displayMedium,
                    color = colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(text = state.pluginName, style = typography.bodyLarge)
            }
        },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val downloadProgress = state.downloadProgress
                val indicatorModifier = Modifier.size(36.dp)
                when {
                    downloadProgress != null -> {
                        val anim by animateFloatAsState(
                            targetValue = downloadProgress.coerceIn(0f, 1f),
                            animationSpec = tween(220, easing = FastOutSlowInEasing),
                            label = "update_progress"
                        )
                        CircularProgressIndicator(progress = { anim }, modifier = indicatorModifier)
                    }
                    state.step is UpdateStep.Latest -> DoneIndicator()
                    state.step is UpdateStep.Completed -> DoneIndicator()
                    state.step is UpdateStep.Available -> HasUpdateIndicator()
                    state.step is UpdateStep.Error -> ErrorIndicator()
                    state.step is UpdateStep.Checking -> CircularProgressIndicator(modifier = indicatorModifier)
                    state.step is UpdateStep.Downloading -> CircularProgressIndicator(modifier = indicatorModifier)
                    state.step is UpdateStep.Installing -> CircularProgressIndicator(modifier = indicatorModifier)
                    else -> CircularProgressIndicator(modifier = indicatorModifier)
                }

                Spacer(Modifier.width(16.dp))

                val msg = when {
                    downloadProgress != null -> {
                        val percent = if (downloadProgress < 0.75f) {
                            (downloadProgress / 0.75f * 100).toInt().coerceAtMost(100)
                        } else {
                            (((downloadProgress - 0.75f) / 0.25f) * 100).toInt().coerceAtMost(100)
                        }

                        if (downloadProgress < 0.75f) {
                            stringResource(R.string.plugin_update_downloading_percent, percent)
                        } else {
                            stringResource(R.string.plugin_update_extracting_percent, percent)
                        }
                    }

                    else -> when (val step = state.step) {
                        is UpdateStep.Checking -> step.message.ifEmpty { stringResource(R.string.plugin_update_checking) }
                        is UpdateStep.Latest -> step.message.ifEmpty { stringResource(R.string.plugin_update_latest) }
                        is UpdateStep.Available -> step.message
                        is UpdateStep.Error -> step.message.ifEmpty { stringResource(R.string.plugin_error_generic) }
                        is UpdateStep.Downloading -> step.message
                        is UpdateStep.Installing -> step.message
                        is UpdateStep.Completed -> step.message
                    }
                }

                Text(text = msg, style = typography.labelMedium)
            }
        },
        confirmButton = {
            Row(Modifier.animateContentSize()) {
                val step = state.step
                if (state.downloadProgress != null) return@AlertDialog
                if (step is UpdateStep.Available) {
                    TextButton(onClick = { onConfirmUpdate(state.pluginId) }) {
                        Text(text = stringResource(R.string.plugin_update_download_install))
                    }
                } else {
                    val enabled = step !is UpdateStep.Checking
                    TextButton(onClick = onClose, enabled = enabled) {
                        Text(text = stringResource(android.R.string.ok))
                    }
                }
            }
        },
        dismissButton = {
            val step = state.step
            TextButton(
                onClick = onClose,
                enabled = (state.downloadProgress != null ||
                    step is UpdateStep.Available ||
                    step is UpdateStep.Checking ||
                    step is UpdateStep.Error)
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
            Text(
                text = stringResource(R.string.plugin_signature_about_title),
                style = typography.displayMedium,
                color = colorScheme.onSurface
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.plugin_signature_about_body),
                    style = typography.bodyMedium
                )
                Text(
                    modifier = Modifier.padding(top = 20.dp, bottom = 14.dp),
                    text = stringResource(R.string.plugin_signature_dev_advice_title),
                    style = typography.titleSmall,
                    color = colorScheme.onSurface
                )
                Text(text = stringResource(R.string.plugin_signature_dev_advice_body))
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
fun PluginErrorDialog(
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(
                text = "插件已被禁用",
                style = typography.titleLarge,
                color = colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = "在加载该插件过程中发生错误，导致应用异常终止。\n\n出于稳定性考虑，该插件已被自动禁用。您可在插件管理界面手动重新启用。",
            )
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
    val list = signatureInfo.orEmpty()
    val dateFormatter = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.plugin_signature_info_title),
                    style = typography.titleLarge,
                    color = colorScheme.onSurface
                )
                if (!list.isEmpty()) {
                    Text(
                        text = "包含 ${list.size} 个签名",
                        style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            if (list.isEmpty()) {
                Text(
                    text = stringResource(R.string.plugin_signature_info_empty),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(list, key = { index, _ -> index }) { index, sig ->
                        Surface(
                            color = colorScheme.surfaceContainerLow,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.plugin_signature_index, index + 1),
                                            style = typography.titleMedium,
                                            color = colorScheme.onSurface
                                        )
                                        Spacer(Modifier.weight(1f))
                                        val schemesText = sig.schemes
                                            .map { it.name }
                                            .sorted()
                                            .joinToString(" · ")
                                            .ifEmpty { ApkSignatureScheme.UNKNOWN.name }
                                        Text(
                                            text = schemesText,
                                            style = typography.labelMedium,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "Valid ${dateFormatter.format(sig.notBefore)} → ${dateFormatter.format(sig.notAfter)}",
                                        style = typography.bodySmall,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }

                                HorizontalDivider(color = colorScheme.outlineVariant)

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "Subject",
                                            style = typography.labelMedium,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                        SelectionContainer {
                                            Text(
                                                text = sig.subject,
                                                style = typography.bodyMedium,
                                                color = colorScheme.onSurface,
                                                maxLines = 3,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "Issuer",
                                            style = typography.labelMedium,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                        SelectionContainer {
                                            Text(
                                                text = sig.issuer,
                                                style = typography.bodyMedium,
                                                color = colorScheme.onSurface,
                                                maxLines = 3,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = "Public key",
                                                style = typography.labelMedium,
                                                color = colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${sig.publicKeyAlgorithm} · ${sig.publicKeyLength} bit",
                                                style = typography.bodyMedium,
                                                color = colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = colorScheme.outlineVariant)

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "SHA-1",
                                            style = typography.labelMedium,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                        SelectionContainer {
                                            Text(
                                                text = sig.sha1,
                                                style = typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                                color = colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
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
        Icon(
            painter = painterResource(R.drawable.done_outline_24px),
            contentDescription = "done",
            tint = colorScheme.surface,
            modifier = Modifier.size(20.dp)
        )
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
        Icon(
            painter = painterResource(R.drawable.downloading_24px),
            contentDescription = "downloading",
            tint = colorScheme.surface,
            modifier = Modifier.size(20.dp)
        )
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
        Icon(
            painter = painterResource(R.drawable.close_24px),
            contentDescription = "close",
            tint = colorScheme.surface,
            modifier = Modifier.size(20.dp)
        )
    }
}
