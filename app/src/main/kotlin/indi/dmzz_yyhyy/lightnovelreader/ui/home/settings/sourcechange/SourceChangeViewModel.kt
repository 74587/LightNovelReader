package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.sourcechange

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceManager
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import indi.dmzz_yyhyy.lightnovelreader.data.work.ExportDataWork
import indi.dmzz_yyhyy.lightnovelreader.data.work.ImportDataWork
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ExportContext
import indi.dmzz_yyhyy.lightnovelreader.ui.components.MutableExportContext
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.system.exitProcess

@HiltViewModel
class SourceChangeViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val userDataRepository: UserDataRepository,
    private val workManager: WorkManager,
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val localBookDataSource: LocalBookDataSource,
    private val bookshelfRepository: BookshelfRepository,
    private val statsRepository: StatsRepository,
    webBookDataSourceManager: WebBookDataSourceManager
) : ViewModel() {

    private val _uiState = MutableSourceChangeUiState().apply {
        currentSourceId = webBookDataSourceProvider.default.id
        webDataSourceItems = webBookDataSourceManager.webDataSourceItems
    }
    val uiState: SourceChangeUiState = _uiState


    private val currentSourceId: Int
        get() = _uiState.currentSourceId

    fun changeWebSource(webDataSourceId: Int, fileDir: File) {
        if (webDataSourceId == _uiState.currentSourceId) return
        if (_uiState.isProcessing) return

        _uiState.isProcessing = true

        viewModelScope.launch(Dispatchers.IO) {
            val oldUri = File(fileDir, "${currentSourceId}.data.lnr").toUri()
            val exportRequest = exportToFile(
                uri = oldUri,
                exportContext = MutableExportContext().apply { settings = false }
            )

            try {
                val exportInfo = wait(exportRequest)
                when (exportInfo?.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        performDataClearAndImport(webDataSourceId, fileDir, oldUri)
                    }

                    WorkInfo.State.FAILED,
                    WorkInfo.State.CANCELLED -> {
                        Log.e("SourceChange", "Export failed, aborting")
                        _uiState.isProcessing = false
                    }

                    else -> {
                        _uiState.isProcessing = false
                    }
                }
            } catch (e: Exception) {
                Log.e("SourceChange", "Export failed", e)
                _uiState.isProcessing = false
            }
        }
    }

    private fun exportToFile(uri: Uri, exportContext: ExportContext): OneTimeWorkRequest {
        val workRequest = OneTimeWorkRequestBuilder<ExportDataWork>()
            .setInputData(
                workDataOf(
                    "uri" to uri.toString(),
                    "exportBookshelf" to exportContext.bookshelf,
                    "exportReadingData" to exportContext.readingData,
                    "exportSetting" to exportContext.settings,
                    "exportBookmark" to exportContext.bookmark,
                )
            )
            .build()
        workManager.enqueueUniqueWork(
            uri.toString(),
            ExistingWorkPolicy.KEEP,
            workRequest
        )
        return workRequest
    }

    private fun importFromFile(uri: Uri, ignoreDataIdCheck: Boolean): OneTimeWorkRequest {
        val workRequest = OneTimeWorkRequestBuilder<ImportDataWork>()
            .setInputData(
                workDataOf(
                    "uri" to uri.toString(),
                    "ignoreDataIdCheck" to ignoreDataIdCheck
                )
            )
            .build()
        workManager.enqueueUniqueWork(
            uri.toString(),
            ExistingWorkPolicy.KEEP,
            workRequest
        )
        return workRequest
    }

    private suspend fun wait(request: OneTimeWorkRequest): WorkInfo? {
        return workManager
            .getWorkInfoByIdFlow(request.id)
            .first { it?.state?.isFinished == true }
    }

    private suspend fun performDataClearAndImport(
        newSourceId: Int,
        fileDir: File,
        fallbackUri: Uri
    ) {
        val oldSourceId = currentSourceId
        try {
            localBookDataSource.clear()
            bookshelfRepository.clear()
            userDataRepository.remove(UserDataPath.ReadingBooks.path)
            userDataRepository.remove(UserDataPath.Search.History.path)
            userDataRepository.intUserData(UserDataPath.Settings.Data.WebDataSourceId.path)
                .set(newSourceId)
            statsRepository.clear()

            val newFile = File(fileDir, "$newSourceId.data.lnr")
            if (!newFile.exists()) {
                Log.i("SourceChange", "No data file for new source, just restart")
                _uiState.currentSourceId = newSourceId
                _uiState.isProcessing = false
                restartApp(appContext)
                return
            }

            val importRequest = importFromFile(newFile.toUri(), ignoreDataIdCheck = true)
            val importInfo = wait(importRequest)

            when (importInfo?.state) {
                WorkInfo.State.SUCCEEDED -> {
                    Log.i("SourceChange", "All operations completed, restarting")
                    _uiState.currentSourceId = newSourceId
                    _uiState.isProcessing = false
                    restartApp(appContext)
                }

                WorkInfo.State.FAILED,
                WorkInfo.State.CANCELLED -> {
                    Log.e("SourceChange", "Import failed. Attempting rollback")
                    restoreFallbackData(fallbackUri, oldSourceId)
                }

                else -> {
                    Log.e("SourceChange", "Import finished with unexpected state")
                    restoreFallbackData(fallbackUri, oldSourceId)
                }
            }
        } catch (e: Exception) {
            Log.e("SourceChange", "Error during import", e)
            restoreFallbackData(fallbackUri, oldSourceId)
        }
    }

    private suspend fun restoreFallbackData(uri: Uri, fallbackSourceId: Int) {
        try {
            userDataRepository.intUserData(UserDataPath.Settings.Data.WebDataSourceId.path)
                .set(fallbackSourceId)

            val restoreRequest = importFromFile(uri, ignoreDataIdCheck = true)
            val restoreInfo = wait(restoreRequest)

            when (restoreInfo?.state) {
                WorkInfo.State.SUCCEEDED -> {
                    Log.i("SourceChange", "Rollback succeeded, restarting")
                    restartApp(appContext)
                }

                WorkInfo.State.FAILED,
                WorkInfo.State.CANCELLED -> {
                    Log.e("SourceChange", "Rollback failed.")
                    _uiState.isProcessing = false
                }

                else -> _uiState.isProcessing = false
            }
        } catch (e: Exception) {
            Log.e("SourceChange", "Rollback import failed with exception", e)
            _uiState.isProcessing = false
        }
    }

    private fun restartApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)

        exitProcess(0)
    }

}