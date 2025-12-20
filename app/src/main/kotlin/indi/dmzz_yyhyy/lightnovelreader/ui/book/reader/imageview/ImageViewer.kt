package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.imageview

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.request.ImageRequest
import indi.dmzz_yyhyy.lightnovelreader.R
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

@Composable
fun ImageViewerScreen(
    imageUri: Uri,
    onDismissRequest: () -> Unit,
    onClickSave: () -> Unit,
    onLongClickSave: () -> Unit,
    header: Map<String, String> = emptyMap()
) {
    val zoomableState = rememberZoomableState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
    ) {
        ZoomableAsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .zoomable(state = zoomableState),
            model = ImageRequest.Builder(LocalContext.current)
                .also { builder ->
                    header.forEach {
                        builder.addHeader(it.key, it.value)
                    }
                }
                .data(imageUri)
                .crossfade(true)
                .build(),
            contentDescription = null
        )

        IconButton(
            onClick = onDismissRequest,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .systemBarsPadding()
                .background(Color.White, CircleShape)
        ) {
            Icon(
                painter = painterResource(R.drawable.close_24px),
                contentDescription = "close",
                tint = Color.Black
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(horizontal = 20.dp)
                .padding(bottom = 46.dp)
                .systemBarsPadding()
                .size(64.dp)
                .combinedClickable(
                    onClick = onClickSave,
                    onLongClick = onLongClickSave
                )
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.save_24px),
                contentDescription = "save",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
