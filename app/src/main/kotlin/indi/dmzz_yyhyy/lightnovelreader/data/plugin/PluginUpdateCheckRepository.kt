package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import android.util.Log
import indi.dmzz_yyhyy.lightnovelreader.utils.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class PluginUpdateInfo(
    val pluginId: String,
    val versionName: String,
    val storePlugin: StorePlugin
)

@Singleton
class PluginUpdateCheckRepository @Inject constructor(
    private val pluginManager: PluginManager
) {
    companion object {
        private const val TAG = "PluginUpdateCheck"
    }

    private val _updates = MutableStateFlow<Map<String, PluginUpdateInfo>>(emptyMap())
    val updates: StateFlow<Map<String, PluginUpdateInfo>> = _updates

    private val json = Json { ignoreUnknownKeys = true }
    private val base = "eNpb85aBtYRBK6OkpKDYSl-_IKc0PTOvWC8vsSgzO18vvyhdP7EgEyZsn5liCwDAsBIV"
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        pluginManager.addOnInitializedCallback {
            coroutineScope.launch { checkAll() }
        }
    }

    private suspend fun checkAll() {
        Log.d(TAG, "START checking updates")
        val plugins = pluginManager.allPluginList.toList()
        val result = mutableMapOf<String, PluginUpdateInfo>()
        plugins.forEach { meta ->
            if (meta.source == PluginSource.InstalledApp) return@forEach
            val pluginId = meta.packageName
            Log.d(TAG, "Checking updates for plugin $pluginId")
            try {
                val url = "${update(base)}$pluginId"
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    requestMethod = "GET"
                }
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                val storePlugin = json.decodeFromJsonElement(
                    StorePlugin.serializer(),
                    json.parseToJsonElement(body).jsonObject["plugin"]!!
                )
                val remoteVersionCode = storePlugin.release.versionCode ?: return@forEach
                if (remoteVersionCode > meta.version) {
                    Log.d(TAG, "Updates available for $pluginId: ${storePlugin.release.versionName} ($remoteVersionCode)")

                    result[meta.packageName] = PluginUpdateInfo(
                        pluginId = pluginId,
                        versionName = storePlugin.release.versionName,
                        storePlugin = storePlugin
                    )
                } else {
                    Log.d(TAG, "Up to date for $pluginId: ${storePlugin.release.versionName} ($remoteVersionCode)")
                }
            } catch (_: Exception) {
            }
        }
        _updates.emit(result)
    }
}
