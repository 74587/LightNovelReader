package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import dev.jeziellago.compose.markdowntext.MarkdownText
import indi.dmzz_yyhyy.lightnovelreader.BuildConfig
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.update.Release
import io.nightfish.lightnovelreader.api.ui.theme.AppTypography
import io.nightfish.lightnovelreader.api.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

fun NavGraphBuilder.updatesAvailableDialog() {
    dialog<Route.UpdatesAvailableDialog> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<UpdatesAvailableDialogViewModel>()
        UpdatesAvailableDialog(
            onDismissRequest = { navController.popBackStack() },
            onConfirmation = { viewModel.downloadUpdate() },
            release = viewModel.release
        )
    }
}

@Composable
fun UpdatesAvailableDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    release: Release?
) {
    val context = LocalContext.current
    AlertDialog(
        title = {
            Text(
                text = stringResource(R.string.dialog_updates_available),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                release?.versionName?.let {
                    Text(
                        text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) → ${release.versionName} (${release.version})"
                    )
                }
                release?.releaseNotes?.let {
                    LazyColumn(
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .wrapContentHeight()
                            .heightIn(max = 350.dp)
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.changelog),
                                style = AppTypography.titleMedium,
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        item {
                            MarkdownText(it)
                        }
                    }
                }
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(R.string.install_update))
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismissRequest
                ) {
                    Text(text = stringResource(R.string.decline))
                }
                TextButton(
                    onClick = {
                        release?.downloadUrl?.let { url ->
                            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                            context.startActivity(intent, null)
                        }
                        onDismissRequest()
                    }
                ) {
                    Text(text = stringResource(R.string.manual_download))
                }
            }
        }
    )
}

fun NavController.navigateUpdatesAvailableDialog() {
    navigate(Route.UpdatesAvailableDialog)
}
