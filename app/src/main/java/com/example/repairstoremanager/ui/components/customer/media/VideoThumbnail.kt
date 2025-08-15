package com.example.repairstoremanager.ui.components.customer.media

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun VideoThumbnail(uri: Uri, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(uri) {
        val retriever = MediaMetadataRetriever()
        try {
            // Add alternative data source methods
            if (uri.scheme == "content" || uri.scheme == "file") {
                retriever.setDataSource(context, uri)
            } else {
                retriever.setDataSource(uri.toString())
            }

            val frame = retriever.frameAtTime
            bitmap.value = frame ?: throw Exception("No frame available")
        } catch (e: Exception) {
            Log.e("VideoThumbnail", "Error loading thumbnail: ${e.message}")
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Log.e("VideoThumbnail", "Error releasing retriever", e)
            }
        }
    }

    bitmap.value?.let { bmp ->
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = "Video thumbnail",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } ?: run {
        Box(
            modifier = modifier.background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Video",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}