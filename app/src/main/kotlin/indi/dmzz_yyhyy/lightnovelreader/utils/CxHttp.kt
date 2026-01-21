package indi.dmzz_yyhyy.lightnovelreader.utils

import cxhttp.CxHttp
import cxhttp.request.Request
import cxhttp.response.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

suspend fun autoReconnectionGet(url: String,  block: suspend Request.() -> Unit = {}): Document? = withContext(Dispatchers.IO) {
    suspend fun get(): Response {
        return CxHttp
            .get(url, block)
            .scope(this)
            .await()
    }
    var retryTime = 5
    var retryDelay = 1500L
    var response = get()
    while (!response.isSuccessful && retryTime >= 1) {
        response = get()
        retryTime--
        delay(retryDelay)
        retryDelay *= 2
    }
    val doc = response.body
        ?.bytes()
        ?.toString()
        ?.let(Jsoup::parse)
        ?.outputSettings(
            Document.OutputSettings()
                .prettyPrint(false)
                .syntax(Document.OutputSettings.Syntax.xml)
        )
    return@withContext doc
}