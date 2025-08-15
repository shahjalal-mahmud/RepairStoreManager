package com.example.repairstoremanager.ui.components.customer.media

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.repairstoremanager.util.MediaStorageHelper

private fun isVideoUri(context: Context, uri: Uri): Boolean {
    val type = context.contentResolver.getType(uri) ?: return false
    return type.startsWith("video/")
}

@Composable
fun CustomerMediaViewer(
    context: Context,
    customerId: String,
    initialIndex: Int = 0,
    onClose: () -> Unit
) {
    val mediaList = remember {
        try {
            MediaStorageHelper.getMediaForCustomer(context, customerId)
        } catch (e: Exception) {
            Log.e("CustomerMediaViewer", "Error loading media: ${e.message}")
            emptyList()
        }
    }

    var currentIndex by remember { mutableStateOf(initialIndex) }

    if (mediaList.isEmpty()) {
        AlertDialog(
            onDismissRequest = onClose,
            title = { Text("No Media") },
            text = { Text("This customer has no media attachments") },
            confirmButton = {
                Button(onClick = onClose) { Text("OK") }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text("Media Viewer") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Media display area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    val currentUri = mediaList[currentIndex]
                    if (isVideoUri(context, currentUri)) {
                        VideoPlayer(uri = currentUri)
                    } else {
                        AsyncImage(
                            model = currentUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Navigation controls
                if (mediaList.size > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                currentIndex = (currentIndex - 1).mod(mediaList.size)
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            "${currentIndex + 1}/${mediaList.size}",
                            color = Color.White
                        )

                        IconButton(
                            onClick = {
                                currentIndex = (currentIndex + 1).mod(mediaList.size)
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onClose) {
                Text("Close")
            }
        }
    )
}
