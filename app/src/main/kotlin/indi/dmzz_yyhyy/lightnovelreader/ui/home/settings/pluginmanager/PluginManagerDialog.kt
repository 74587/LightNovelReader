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
    uiState: PluginInstallerDialogUiState,
    onClickClose: () -> Unit,
    onConfirmDecision: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Column {
                val titleText = uiState.installInfo?.name?.takeIf { it.isNotEmpty() }
                    ?: stringResource(R.string.plugin_install_preparing)

                Text(text = titleText, style = typography.displayMedium, color = colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))

                uiState.installInfo?.let { info ->
                    var text = info.packageName
                    if (info.versionName.isNotEmpty()) {
                        text += "\n"
                        text += stringResource(R.string.plugin_version_prefix, info.versionName)
                    }
                    Text(text = text, style = typography.bodyMedium)
                }
            }
        },
        text = {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                InstallIndicator(uiState = uiState)
                Spacer(Modifier.width(20.dp))

                val msg = when (uiState.installStep) {
                    InstallStepState.AwaitingDecision ->
                        uiState.installDecision?.message.orEmpty()

                    InstallStepState.Completed -> {
                        val m = uiState.installCompletedMessage
                        if (m.isNotEmpty()) m
                        else if (uiState.installCompletedSuccess) stringResource(R.string.plugin_install_completed)
                        else stringResource(R.string.plugin_install_failed)
                    }

                    InstallStepState.Working ->
                        uiState.installMessage.ifEmpty { stringResource(R.string.plugin_install_preparing) }
                }

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = msg,
                    style = typography.bodyMedium
                )
            }
        },
        confirmButton = {
            when (uiState.installStep) {
                InstallStepState.Completed -> {
                    TextButton(onClick = onClickClose) { Text(text = stringResource(android.R.string.ok)) }
                }

                InstallStepState.AwaitingDecision -> {
                    val type = uiState.installDecision?.type
                    val label = when (type) {
                        InstallDecisionType.InvalidSignature -> R.string.plugin_install_anyway
                        InstallDecisionType.Reinstall,
                        InstallDecisionType.Downgrade,
                        null -> R.string.next
                    }
                    TextButton(onClick = { onConfirmDecision(true) }) {
                        Text(text = stringResource(label))
                    }
                }

                InstallStepState.Working -> {
                    TextButton(onClick = {}, enabled = false) { Text(text = stringResource(R.string.next)) }
                }
            }
        },
        dismissButton = {
            val canShowCancel = uiState.installStep != InstallStepState.Completed &&
                    (uiState.installStep == InstallStepState.AwaitingDecision || uiState.installProgress == null)

            if (canShowCancel) {
                TextButton(
                    onClick = {
                        if (uiState.installStep == InstallStepState.AwaitingDecision) onConfirmDecision(false)
                        else onClickClose()
                    }
                ) { Text(text = stringResource(R.string.abort)) }
            }
        }
    )
}

@Composable
private fun InstallIndicator(
    uiState: PluginInstallerDialogUiState
) {
    val indicatorSize = Modifier.size(36.dp)
    when (uiState.installStep) {
        InstallStepState.AwaitingDecision -> {
            Box(
                modifier = indicatorSize
                    .background(color = colorScheme.error.copy(alpha = 0.9f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.info_24px),
                    contentDescription = stringResource(R.string.confirm),
                    tint = colorScheme.surface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        InstallStepState.Completed -> {
            if (uiState.installCompletedSuccess) DoneIndicator() else ErrorIndicator()
        }

        InstallStepState.Working -> {
            val progress = uiState.installProgress
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
    uiState: PluginInstallerDialogUiState,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = stringResource(R.string.plugin_delete_title, uiState.uninstallPluginName),
                style = typography.displayMedium,
                color = colorScheme.onSurface
            )
        },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (uiState.uninstallStep) {
                    DeleteStepState.Completed -> {
                        if (uiState.uninstallCompletedSuccess) DoneIndicator() else ErrorIndicator()
                    }

                    DeleteStepState.Working -> {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))

                val msg = when (uiState.uninstallStep) {
                    DeleteStepState.Completed -> uiState.uninstallCompletedMessage
                    DeleteStepState.Working -> uiState.uninstallMessage
                }

                Text(text = msg, style = typography.bodyMedium)
            }
        },
        confirmButton = {
            val enabled = uiState.uninstallStep == DeleteStepState.Completed
            TextButton(onClick = onClose, enabled = enabled) {
                Text(text = stringResource(android.R.string.ok))
            }
        }
    )
}

@Composable
fun UpdateCheckDialog(
    uiState: PluginInstallerDialogUiState,
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
                Text(text = uiState.updatePluginName, style = typography.bodyLarge)
            }
        },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val downloadProgress = uiState.updateDownloadProgress
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

                    uiState.updateStep == UpdateStepState.Latest -> DoneIndicator()
                    uiState.updateStep == UpdateStepState.Completed -> DoneIndicator()
                    uiState.updateStep == UpdateStepState.Available -> HasUpdateIndicator()
                    uiState.updateStep == UpdateStepState.Error -> ErrorIndicator()
                    uiState.updateStep == UpdateStepState.Checking -> CircularProgressIndicator(modifier = indicatorModifier)
                    uiState.updateStep == UpdateStepState.Downloading -> CircularProgressIndicator(modifier = indicatorModifier)
                    uiState.updateStep == UpdateStepState.Installing -> CircularProgressIndicator(modifier = indicatorModifier)
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

                    else -> uiState.updateMessage.ifEmpty {
                        when (uiState.updateStep) {
                            UpdateStepState.Checking -> stringResource(R.string.plugin_update_checking)
                            UpdateStepState.Latest -> stringResource(R.string.plugin_update_latest)
                            UpdateStepState.Error -> stringResource(R.string.plugin_error_generic)
                            else -> ""
                        }
                    }
                }

                Text(text = msg, style = typography.labelMedium)
            }
        },
        confirmButton = {
            Row(Modifier.animateContentSize()) {
                val step = uiState.updateStep
                if (uiState.updateDownloadProgress != null) return@AlertDialog

                if (step == UpdateStepState.Available) {
                    TextButton(onClick = { onConfirmUpdate(uiState.updatePluginId) }) {
                        Text(text = stringResource(R.string.plugin_update_download_install))
                    }
                } else {
                    val enabled = step != UpdateStepState.Checking
                    TextButton(onClick = onClose, enabled = enabled) {
                        Text(text = stringResource(android.R.string.ok))
                    }
                }
            }
        },
        dismissButton = {
            val step = uiState.updateStep
            TextButton(
                onClick = onClose,
                enabled = (uiState.updateDownloadProgress != null ||
                        step == UpdateStepState.Available ||
                        step == UpdateStepState.Checking ||
                        step == UpdateStepState.Error)
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
                text = stringResource(R.string.plugin_disabled_title),
                style = typography.titleLarge,
                color = colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = stringResource(R.string.plugin_disabled_body),
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
                        text = stringResource(R.string.plugin_signature_count, list.size),
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
                                            text = "#${index + 1}",
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
                                        text = stringResource(
                                            R.string.plugin_signature_validity,
                                            dateFormatter.format(sig.notBefore),
                                            dateFormatter.format(sig.notAfter)
                                        ),
                                        style = typography.bodySmall,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }

                                HorizontalDivider(color = colorScheme.outlineVariant)

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = stringResource(R.string.plugin_signature_subject),
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
                                            text = stringResource(R.string.plugin_signature_issuer),
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
                                                text = stringResource(R.string.plugin_signature_public_key_label),
                                                style = typography.labelMedium,
                                                color = colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = stringResource(
                                                    R.string.plugin_signature_public_key_value,
                                                    sig.publicKeyAlgorithm,
                                                    sig.publicKeyLength
                                                ),
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
                                            text = stringResource(R.string.plugin_signature_sha1_label),
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
            contentDescription = "error",
            tint = colorScheme.surface,
            modifier = Modifier.size(20.dp)
        )
    }
}
