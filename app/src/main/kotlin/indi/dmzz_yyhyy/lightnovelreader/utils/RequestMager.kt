package indi.dmzz_yyhyy.lightnovelreader.utils

import kotlinx.coroutines.delay
import kotlin.coroutines.cancellation.CancellationException

class RequestMarge {
    data class Result(
        val value: Any
    )

    val resultMap: MutableMap<String, Result?> = mutableMapOf()

    suspend inline fun <reified T: Any> margeRequest(id: Int, block: suspend () -> T): T {
        val key = T::class.hashCode().toString() + id

        if (!resultMap.contains(key)) {
            resultMap[key] = null
            try {
                resultMap[key] = Result(block.invoke())
            } catch (_: CancellationException) {
                resultMap.remove(key)
                return block.invoke()
            }
        }
        while (resultMap.contains(key) && resultMap[key] == null) {
            println("wait for marge")
            delay(16) // FIXME: ?
        }
        val value = resultMap[key]?.let { it.value as T} ?: block.invoke()
        resultMap.remove(key)
        return value
    }
}