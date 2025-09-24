package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsClickableEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsMenuEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsSwitchEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import indi.dmzz_yyhyy.lightnovelreader.utils.uriLauncher
import kotlinx.coroutines.launch

@Composable
fun DataSettingsList(
    onClickChangeSource: () -> Unit,
    onClickExportUserData: () -> Unit,
    onClickLogcat: () -> Unit,
    settingState: SettingState,
    importData: (Uri) -> OneTimeWorkRequest,
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHost.current
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)
    val scope = rememberCoroutineScope()
    val importDataLauncher = uriLauncher {
        scope.launch {
            workManager.getWorkInfoByIdFlow(importData(it).id).collect {
                when (it?.state) {
                    WorkInfo.State.FAILED -> {
                        Toast.makeText(context, context.getString(R.string.data_import_failed), Toast.LENGTH_SHORT).show()
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        Toast.makeText(context, context.getString(R.string.data_import_success), Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }
    /*
    var displayExportDialog by remember { mutableStateOf(false) }
    var displaySourceChangeDialog by remember { mutableStateOf(false) }
    dialog {
        var selectedWebDataSourceId by remember { mutableStateOf(webDataSourceId) }
        if (displaySourceChangeDialog) {
            SourceChangeDialog(
                onDismissRequest = {
                    displaySourceChangeDialog = false
                    selectedWebDataSourceId = webDataSourceId
                },
                onConfirmation = {
                    displaySourceChangeDialog = false
                    changeWebDataSource(selectedWebDataSourceId, context)
                },
                webDataSourceItems = listOf(wenku8ApiWebDataSourceItem, zaiComicWebDataSourceItem),
                selectedWebDataSourceId = selectedWebDataSourceId,
                onClickItem = {
                    selectedWebDataSourceId = it
                }
            )
        }
        if (displayExportDialog) {
            ExportUserDataDialog(
                onDismissRequest = { displayExportDialog = false },
                onClickSaveAndSend = {
                    displayExportDialog = false
                    exportAndSendToFile(exportContext, context)
                },
                onClickSaveToFile = {
                    displayExportDialog = false
                    exportContext = it
                    createDataFile("LightNovelReaderData", saveDataToFileLauncher)
                }
            )
        }
    }*/
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.output_24px),
        title = stringResource(R.string.settings_snap_data),
        description = stringResource(R.string.settings_snap_data_desc),
        onClick = onClickExportUserData
    )
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.input_24px),
        title = stringResource(R.string.settings_import_data),
        description = stringResource(R.string.settings_import_data_desc),
        onClick = { selectDataFile(importDataLauncher) }
    )
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.public_24px),
        title = stringResource(R.string.settings_select_data_source),
        description = stringResource(R.string.settings_select_data_source_desc),
        onClick = onClickChangeSource
    )
    SettingsSwitchEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.wifi_proxy_24px),
        title = stringResource(R.string.settings_auto_proxy),
        description = stringResource(R.string.settings_auto_proxy_desc),
        checked = settingState.isUseProxy,
        booleanUserData = settingState.isUseProxyUserData
    )
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.bug_report_24px),
        title = stringResource(R.string.settings_app_logs),
        description = stringResource(R.string.settings_app_logs_desc),
        onClick = onClickLogcat
    )
    SettingsMenuEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.bug_report_24px),
        title = stringResource(R.string.settings_app_log_level),
        description = stringResource(R.string.settings_app_log_level_desc),
        options = MenuOptions.LogLevelOptions,
        selectedOptionKey = settingState.logLevelKey,
        onOptionChange = { option ->
            settingState.logLevelKeyUserData.asynchronousSet(option)
            showSnackbar(
                coroutineScope = coroutineScope,
                hostState = snackbarHostState,
                message = context.getString(R.string.restart_to_apply_changes)
            ) { }
        }
    )
}

fun selectDataFile(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val initUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Documents")
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initUri)
    }
    launcher.launch(Intent.createChooser(intent, "选择数据文件"))
}