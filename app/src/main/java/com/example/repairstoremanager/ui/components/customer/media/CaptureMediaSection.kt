package com.example.repairstoremanager.ui.components.customer.media

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.repairstoremanager.util.MediaStorageHelper

@Composable
fun CaptureMediaSection(
    customerId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val images = remember { mutableStateListOf<Uri>() }
    val videos = remember { mutableStateListOf<Uri>() }

    // Multiple photo capture
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            Toast.makeText(context, "Photo saved", Toast.LENGTH_SHORT).show()
        } else {
            images.removeLastOrNull() // Remove if capture failed
            Toast.makeText(context, "Photo capture failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Video capture
    val takeVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) {
            Toast.makeText(context, "Video saved", Toast.LENGTH_SHORT).show()
        } else {
            videos.removeLastOrNull() // Remove if capture failed
            Toast.makeText(context, "Video capture failed", Toast.LENGTH_SHORT).show()
        }
    }

    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Request permission
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingAction?.invoke()
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            val uri = MediaStorageHelper.createImageUri(context, customerId)
            if (uri != null) {
                images.add(uri)
                takePictureLauncher.launch(uri)
            }
        } else {
            pendingAction = { checkPermissionAndTakePhoto() }
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun checkPermissionAndTakeVideo() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            val uri = MediaStorageHelper.createVideoUri(context, customerId)
            if (uri != null) {
                videos.add(uri)
                takeVideoLauncher.launch(uri)
            }
        } else {
            pendingAction = { checkPermissionAndTakeVideo() }
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { checkPermissionAndTakePhoto() },
                modifier = Modifier.weight(1f)
            ) {
                Text("📷 Take Photo")
            }
            Button(
                onClick = { checkPermissionAndTakeVideo() },
                modifier = Modifier.weight(1f)
            ) {
                Text("🎥 Take Video")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Show captured images with cancel icons
        if (images.isNotEmpty() || videos.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(images) { uri ->
                    Box(modifier = Modifier.size(100.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { images.remove(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .background(Color.Red.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove image",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                items(videos) { uri ->
                    Box(modifier = Modifier.size(100.dp)) {
                        // Show video thumbnail
                        VideoThumbnail(uri = uri)
                        IconButton(
                            onClick = { videos.remove(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .background(Color.Red.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove video",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Play video",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(32.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoThumbnail(
    uri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap = remember(uri) {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        try {
            mediaMetadataRetriever.setDataSource(context, uri)
            mediaMetadataRetriever.frameAtTime
        } catch (e: Exception) {
            null
        } finally {
            mediaMetadataRetriever.release()
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Video thumbnail",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Videocam,
                contentDescription = "Video",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}