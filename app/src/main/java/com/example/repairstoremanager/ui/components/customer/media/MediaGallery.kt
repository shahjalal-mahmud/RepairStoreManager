package com.example.repairstoremanager.ui.components.customer.media

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun MediaGallery(
    mediaList: List<Uri>,
    onMediaSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        itemsIndexed(mediaList) { index, mediaUri ->
            MediaThumbnail(
                uri = mediaUri,
                onClick = { onMediaSelected(index) },
                modifier = Modifier.size(100.dp)
            )
        }
    }
}

@Composable
fun MediaThumbnail(
    uri: Uri,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isVideo = isVideoUri(context, uri)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        if (isVideo) {
            VideoThumbnail(
                uri = uri,
                modifier = Modifier.fillMaxSize()
            )
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Play video",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            )
        } else {
            AsyncImage(
                model = uri,
                contentDescription = "Customer device media",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

private fun isVideoUri(context: Context, uri: Uri): Boolean {
    val type = context.contentResolver.getType(uri) ?: return false
    return type.startsWith("video/")
}