package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8

import android.util.Log
import cxhttp.CxHttp
import cxhttp.response.Response
import cxhttp.response.bodyOrNull
import indi.dmzz_yyhyy.lightnovelreader.utils.UserAgentGenerator
import indi.dmzz_yyhyy.lightnovelreader.utils.update
import io.nightfish.lightnovelreader.api.util.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.nio.charset.Charset
import java.time.Instant
import kotlin.io.encoding.Base64
import kotlin.random.Random

private val requestLimiter = Semaphore(4)
private val pendingJobs = Channel<Unit>(capacity = 25, onBufferOverflow = BufferOverflow.DROP_OLDEST)

fun wenku8Cookies(): Map<String, String> = mapOf(
    "Hm_lvt_acfbfe93830e0272a88e1cc73d4d6d0f" to "1737964211",
    "PHPSESSID" to "261c62b5dae26868bba643433e859ce6",
    "jieqiUserInfo" to "jieqiUserId%3D1125456%2CjieqiUserName%3Dyyhyy%2CjieqiUserGroup%3D3%2CjieqiUserVip%3D0%2CjieqiUserPassword%3Deb62861281462fd923fb99218735fef0%2CjieqiUserName_un%3Dyyhyy%2CjieqiUserHonor_un%3D%26%23x4E2D%3B%26%23x7EA7%3B%26%23x4F1A%3B%26%23x5458%3B%2CjieqiUserGroupName_un%3D%26%23x666E%3B%26%23x901A%3B%26%23x4F1A%3B%26%23x5458%3B%2CjieqiUserLogin%3D1739294499",
    "jieqiVisitInfo" to "jieqiUserLogin%3D1739294499%2CjieqiUserId%3D1125456",
    "cf_clearance" to "3zr0PrHC91IKoMSddax50XdS4Z_w10P.MHnUWfhwvuE-1739294164-1.2.1.1-KudGwf7eifsQWo9tIfX7Gg9Z_VwgSDRHr2erMBcjfHcOJqyg6zpM.XQYS54P0zx8bgSOrmvyRU5xcR9EuCA9aiNSec_tY.r82Lq6w3O_EEPgZuG1HdqjGCgMH11Mud34v5h3lMSGG3PBLCdXD5GXqDE1mPWDzIWyDbprUKg_YZ09DekRXkpyKwa.rt6Pz8LmBN5aVAkoF06sdPcLoUHqnyKe2584pWQ8nWrsM7frhohd8oAH0u12GPD_z8k_SHhflswjC7...cUz.5Hxonur_829PrCsjt.vJqAal0eqE5AmfBJ3FLWO1I3c0vKsVkSO3rrA8bH0v0yDHfatKKO3ww",
    "HMACCOUNT" to "E7837B0FF79F0590",
    "Hm_lvt_d72896ddbf8d27c750e3b365ea2fc902" to "1739294365,1739294389,1739294442,1739294467",
    "Hm_lpvt_d72896ddbf8d27c750e3b365ea2fc902" to "1739294503"
)

private val cache = Cache(
    timeout = 5 * 60 * 1000
)

private inline fun ifCache(id: String, block: () -> Document?): Document? {
    val cacheData = cache.getCache<Document?>(id.hashCode())
    if (cacheData == null) {
        val data = block.invoke()
        if (data == null) return data
        cache.cache(id.hashCode(), data)
        return data
    }
    return cacheData
}

suspend fun wenku8Api(request: String): Document? = ifCache(request) {
    if (!pendingJobs.trySend(Unit).isSuccess) {
        Log.w("Wenku8API", "request dropped: $request")
    }

    return try {
        requestLimiter.withPermit {
            Log.i("Wenku8API", "request to wenku8 with $request")

            withTimeoutOrNull(15_000L) {
                suspend fun post(): Response {
                    return CxHttp
                        .post(update("eNpb85aBtYRBMaOkpMBKXz-xoECvPDUvu9RCLzk_Vz8xL6UoPzNFryCjAAAfiA5Q").toString()){
                            formBody {
                                append("request", Base64.encode(request.toByteArray()))
                                append("timetoken", Instant.now().toEpochMilli().toString())
                                append("appver", "1.21")
                            }
                            header("user-agent", "wenku8")
                        }
                        .scope(this)
                        .await()
                }
                var retryTime = 5
                var retryDelay = 1500
                var response = post()
                while (!response.isSuccessful && retryTime >= 1) {
                    response = post()
                    retryTime--
                    retryDelay = retryDelay * 2
                }
                delay(Random.Default.nextLong(300, 450))
                val doc = response.bodyOrNull<String>()?.let(Jsoup::parse)
                    ?.outputSettings(
                        Document.OutputSettings()
                            .prettyPrint(false)
                            .syntax(Document.OutputSettings.Syntax.xml)
                    )
                return@withTimeoutOrNull doc
            }.also {
                if (it == null) Log.w("Wenku8API", "request timeout: $request")
            }
        }
    } finally {
        pendingJobs.tryReceive().getOrNull()
    }
}

suspend fun autoReconnectionGetWithWenku8Cookie(url: String): Document? = withContext(Dispatchers.IO) {
    suspend fun get(): Response {
        return CxHttp
            .get(url){
                header("user-agent", UserAgentGenerator.generate())
                header("cookie",wenku8Cookies().map { "${it.key}=${it.value}" }.joinToString(separator = ";"))
            }
            .scope(this)
            .await()
    }
    var retryTime = 5
    var retryDelay = 1500
    var response = get()
    while (!response.isSuccessful && retryTime >= 1) {
        response = get()
        retryTime--
        retryDelay = retryDelay * 2
    }
    val doc = response.body
        ?.bytes()
        ?.toString(charset = Charset.forName("GBK"))
        ?.let(Jsoup::parse)
        ?.outputSettings(
            Document.OutputSettings()
                .prettyPrint(false)
                .syntax(Document.OutputSettings.Syntax.xml)
        )
    return@withContext doc
}
