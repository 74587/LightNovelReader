package indi.dmzz_yyhyy.lightnovelreader.utils

import kotlinx.coroutines.delay
import kotlin.coroutines.cancellation.CancellationException

class RequestLimit(
    val maxRequestCount: Int = 3,
    val requestDelay: Long = 500
) {
    private var requestCount = 0

    suspend fun <T>limit(block: suspend () -> T): T? {
        while (requestCount >= maxRequestCount) {
            delay(1)
            println("blocking!")
        }
        requestCount++
        return try {
            println(requestCount)
            val value = block.invoke()
            delay(requestDelay)
            value
        } catch (_: CancellationException) {
            null
        } finally {
            requestCount--
            println(requestCount)
        }
    }
}
