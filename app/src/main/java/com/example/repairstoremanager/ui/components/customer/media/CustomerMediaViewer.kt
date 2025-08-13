package com.example.repairstoremanager.ui.components.customer.media

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.repairstoremanager.util.MediaStorageHelper

@Composable
fun CustomerMediaViewer(
    context: Context,
    customerId: String,
    onClose: () -> Unit
) {
    val mediaList = remember { MediaStorageHelper.getMediaForCustomer(context, customerId) }
    var currentIndex by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Device Media") },
        text = {
            Column {
                // Media display area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (mediaList.isNotEmpty()) {
                        val currentUri = mediaList[currentIndex]
                        if (currentUri.toString().contains(".mp4")) {
                            // Show video with play button
                            VideoThumbnail(uri = currentUri)
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Play video",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            )
                        } else {
                            AsyncImage(
                                model = currentUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        Text("No media found")
                    }
                }

                // Navigation buttons
                if (mediaList.size > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                currentIndex = (currentIndex - 1).mod(mediaList.size)
                            },
                            enabled = mediaList.isNotEmpty()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous")
                        }

                        Text("${currentIndex + 1}/${mediaList.size}")

                        IconButton(
                            onClick = {
                                currentIndex = (currentIndex + 1).mod(mediaList.size)
                            },
                            enabled = mediaList.isNotEmpty()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onClose) { Text("Close") }
        }
    )
}