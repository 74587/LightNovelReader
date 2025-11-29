package indi.dmzz_yyhyy.lightnovelreader

import cxhttp.CxHttp
import cxhttp.CxHttpHelper
import cxhttp.converter.GsonConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

suspend fun main() {
    CxHttpHelper.init(scope=MainScope(), debugLog=true, converter = GsonConverter())
    val coroutineScope = CoroutineScope(Dispatchers.IO)
    val job = coroutineScope.launch {
        CxHttp
            .get("https://www.baidu.com")
            .await()
            .body
            ?.string()
            .let(::println)
    }
    println("jj")
    delay(200)
    job.cancel()
    println("jjboy")
}