package indi.dmzz_yyhyy.lightnovelreader.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Collections
import java.util.zip.ZipInputStream

@Composable
fun withHaptic(action: (() -> Unit)?): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return {
        action?.let {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            it()
        }
    }
}

fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }

fun <T> Flow<T>.throttleLatest(periodMillis: Long): Flow<T> = flow {
    var lastTime = 0L
    var pendingValue: T? = null
    collect { value ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTime >= periodMillis) {
            lastTime = currentTime
            pendingValue = null
            emit(value)
        } else {
            pendingValue = value
        }
    }

    pendingValue?.let { emit(it) }
}

@Composable
fun LazyListState.isScrollingUp(): State<Boolean> {
    return produceState(initialValue = true) {
        var lastIndex = 0
        var lastScroll = Int.MAX_VALUE
        snapshotFlow {
            firstVisibleItemIndex to firstVisibleItemScrollOffset
        }.collect { (currentIndex, currentScroll) ->
            if (currentIndex != lastIndex || currentScroll != lastScroll) {
                value = currentIndex < lastIndex ||
                        (currentIndex == lastIndex && currentScroll < lastScroll)
                lastIndex = currentIndex
                lastScroll = currentScroll
            }
        }
    }
}

fun NavController.popBackStackIfResumed() {
    if (isResumed()) {
        popBackStack()
    }
}

fun NavController.isResumed(): Boolean {
    return this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED
}

fun quickSelect(list: List<Int>, percentile: Double): Int {
    val targetIndex = (list.size * percentile).toInt().coerceIn(list.indices)
    val arr = list.toMutableList()

    var left = 0
    var right = arr.lastIndex

    while (left < right) {
        val pivotIndex = partition(arr, left, right)
        when {
            pivotIndex == targetIndex -> return arr[pivotIndex]
            pivotIndex < targetIndex -> left = pivotIndex + 1
            else -> right = pivotIndex - 1
        }
    }
    return arr[left]
}

private fun partition(arr: MutableList<Int>, left: Int, right: Int): Int {
    val pivot = arr[right]
    var i = left
    for (j in left until right) {
        if (arr[j] <= pivot) {
            Collections.swap(arr, i, j)
            i++
        }
    }
    Collections.swap(arr, i, right)
    return i
}

val homeRoutes = listOf(
    "Reading.Home", "Bookshelf.Home", "Explore.Home", "Settings.Home"
)

fun isInMainNavigation(from: NavDestination, to: NavDestination): Boolean {
    val fromRoute = from.route ?: return false
    val toRoute = to.route ?: return false

    val fromMatch = homeRoutes.any(fromRoute::contains)
    if (!fromMatch) return false

    val toMatch = homeRoutes.any(toRoute::contains)
    return toMatch
}

suspend fun unzipFile(zipFile: File, outFile: File) {
    withContext(Dispatchers.IO) {
        ZipInputStream(zipFile.inputStream().buffered()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    outFile.outputStream().buffered().use { outStream ->
                        val buffer = ByteArray(8 * 1024)
                        var len: Int
                        while (zis.read(buffer).also { len = it } > 0) {
                            outStream.write(buffer, 0, len)
                        }
                    }
                }
                entry = zis.nextEntry
            }
        }
    }
}

