package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTypography
import kotlinx.coroutines.launch

@Composable
fun ZoomableImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    onZoomEnd: () -> Unit,
    maxScale: Float = 3f,
    minScale: Float = 1f,
    placeholderHeight: Dp = 200.dp
) {
    var retryKey by remember { mutableIntStateOf(0) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var lastError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    Box(modifier = modifier) {
        key(retryKey) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .listener(
                        onSuccess = { _, _ ->
                            lastError = null
                        },
                        onError = { _, result ->
                            lastError = result.throwable.message
                        }
                    )
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                loading = {
                    Box(
                        modifier = Modifier
                            .height(placeholderHeight)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                },
                error = {
                    Column(
                        modifier = Modifier
                            .height(placeholderHeight)
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("图片加载失败", style = AppTypography.labelMedium)
                        lastError?.let {
                            Text(
                                text = it,
                                style = AppTypography.bodySmall,
                                color = colorScheme.error
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            retryKey++
                            lastError = null
                            offset = Offset.Zero
                            coroutineScope.launch { scale.snapTo(1f) }
                        }) {
                            Text("重试")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = placeholderHeight)
                    .align(Alignment.Center)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val pointers = event.changes.count { it.pressed }
                                if (pointers >= 2) {
                                    val zoom = event.calculateZoom()
                                    val pan = event.calculatePan()
                                    val newScale =
                                        (scale.value * zoom).coerceIn(minScale, maxScale)
                                    coroutineScope.launch { scale.snapTo(newScale) }
                                    if (newScale > 1f) offset += pan
                                    event.changes.forEach { it.consume() }
                                } else if (pointers == 0 && scale.value != 1f) {
                                    onZoomEnd()
                                    coroutineScope.launch {
                                        scale.animateTo(
                                            targetValue = 1f,
                                            animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                        )
                                        offset = Offset.Zero
                                    }
                                }
                            }
                        }
                    }
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .padding(bottom = 8.dp)
            )
        }
    }
}
