package indi.dmzz_yyhyy.lightnovelreader.data.update

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.BuildConfig
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateCheckRepository @Inject constructor(
    @param:ApplicationContext @field:ApplicationContext private val context: Context,
    private val userDataRepository: UserDataRepository
) {
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.US)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var checkJob: Job? = null
    var release: Release? = null
        private set
    private val mutableAvailable: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val availableFlow: Flow<Boolean> = mutableAvailable
    private val _updatePhase = MutableStateFlow("未检查")
    val updatePhase: Flow<String> = _updatePhase

    init {
        coroutineScope.launch {
            if (userDataRepository.booleanUserData(UserDataPath.Settings.App.AutoCheckUpdate.path).getOrDefault(true))
                check()
        }
    }

    fun resetAvailable() {
        coroutineScope.launch {
            mutableAvailable.update { false }
        }
    }

    fun check() {
        if (checkJob != null && checkJob!!.isActive) return
        checkJob = coroutineScope.launch {
            val updateChannelKey = userDataRepository.stringUserData(UserDataPath.Settings.App.UpdateChannel.path).get() ?: MenuOptions.UpdateChannelOptions.DEVELOPMENT
            val distributionPlatform = userDataRepository.stringUserData(UserDataPath.Settings.App.DistributionPlatform.path).get() ?: MenuOptions.UpdatePlatformOptions.GitHub
            Log.i("UpdateChecker", "Checking for updates from $distributionPlatform/$updateChannelKey")
            _updatePhase.update { "已请求更新，等待 $distributionPlatform 应答" }
            try {
                release =
                    MenuOptions.UpdatePlatformOptions
                        .getOptionWithValue(distributionPlatform).value
                        .getOptionWithValue(updateChannelKey).value
                        .parser(_updatePhase)
            } catch (e: Exception) {
                Log.e("UpdateChecker", "failed to get release")
                e.printStackTrace()
                _updatePhase.emit("${dateFormat.format(Date())} | 失败: ${e.javaClass.simpleName}\n${e.message}")
            }
            if (release != null) {
                if (release!!.version > BuildConfig.VERSION_CODE) {
                    Log.i("UpdateChecker", "Updates available: ${release!!.versionName}")
                    _updatePhase.emit("${dateFormat.format(Date())} | 有可用更新: ${release!!.versionName}")
                } else {
                    Log.i("UpdateChecker", "App is up to date (${release!!.versionName})")
                    _updatePhase.emit("${dateFormat.format(Date())} | 已是最新 (远程: ${release!!.versionName})")
                }
            }
            mutableAvailable.emit(release != null && release!!.version > BuildConfig.VERSION_CODE)
        }
    }

    fun downloadUpdate() {
        val release = release
        if (release == null) {
            Log.e("UpdateChecker", "Didn't find the release because release is null!")
            return
        }

        val cacheDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val apkFile = cacheDir.resolve("LightNovelReader-update.apk").apply {
            if (exists()) delete()
        }
        val tempFile = cacheDir.resolve("LightNovelReader-update.tmp").apply {
            if (exists()) delete()
        }

        coroutineScope.launch {
            try {
                _updatePhase.emit("下载更新中…")

                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder()
                    .url(release.downloadUrl)
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("UpdateChecker", "Failed to download update: ${response.code}")
                        _updatePhase.emit("下载失败 (${response.code})")
                        return@use
                    }

                    response.body.let { body ->
                        val total = body.contentLength()
                        val input = body.byteStream()
                        FileOutputStream(tempFile).use { output ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            var bytesCopied = 0L
                            var bytes = input.read(buffer)

                            while (bytes >= 0) {
                                output.write(buffer, 0, bytes)
                                bytesCopied += bytes
                                bytes = input.read(buffer)

                                val progress = (bytesCopied * 100 / total).toInt()
                                _updatePhase.emit("下载中... $progress%")
                            }
                        }
                    }
                }

                release.downloadFileProgress?.let { transform ->
                    _updatePhase.emit("合并更新文件…")
                    transform(tempFile, apkFile)
                } ?: tempFile.renameTo(apkFile)

                if (apkFile.exists() && apkFile.length() > 0L) {
                    _updatePhase.emit("下载完成")
                    withContext(Dispatchers.Main) {
                        installApk(apkFile)
                    }
                } else {
                    Log.e("UpdateChecker", "Downloaded file is empty")
                    _updatePhase.emit("下载失败 (空文件)")
                }
            } catch (e: Exception) {
                Log.e("UpdateChecker", "Download failed", e)
                _updatePhase.emit("下载失败 (${e.localizedMessage})")
            }
        }
    }

    private fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            setDataAndType(uri, "application/vnd.android.package-archive")
        }
        context.startActivity(intent)
    }
}
