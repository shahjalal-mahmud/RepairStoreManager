package com.example.repairstoremanager.ui.components.customer.media

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.repairstoremanager.util.MediaStorageHelper

@Composable
fun CaptureMediaSection(
    customerId: String,
    modifier: Modifier = Modifier,
    clearSignal: Int = 0,
    onMediaCaptured: (List<Uri>, List<Uri>) -> Unit
) {
    val context = LocalContext.current
    val images = remember { mutableStateListOf<Uri>() }
    val videos = remember { mutableStateListOf<Uri>() }
    var showMediaPickerDialog by remember { mutableStateOf(false) }
    var showVideoPickerDialog by remember { mutableStateOf(false) }

    // Notify parent when media changes
    LaunchedEffect(images, videos) {
        onMediaCaptured(images.toList(), videos.toList())
    }

    // Clear when clearSignal changes
    LaunchedEffect(clearSignal) {
        images.clear()
        videos.clear()
        onMediaCaptured(emptyList(), emptyList())
    }

    // Multiple photo selection from gallery
    val pickMultiplePhotosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                val newUri = copyImageToAppStorage(context, uri, customerId)
                newUri?.let { images.add(it) }
            }
        }
    }

    // Multiple video selection from gallery
    val pickMultipleVideosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                val newUri = copyVideoToAppStorage(context, uri, customerId)
                newUri?.let { videos.add(it) }
            }
        }
    }

    // Camera launcher for photos
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let { uri ->
                images.add(uri)
                tempImageUri = null
            }
        }
    }

    // Video capture launcher
    val takeVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success: Boolean ->
        if (success) {
            tempVideoUri?.let { uri ->
                if (videos.size < 5) {
                    videos.add(uri)
                } else {
                    Toast.makeText(context, "Maximum 5 videos allowed", Toast.LENGTH_SHORT).show()
                }
                tempVideoUri = null
            }
        } else {
            tempVideoUri = null
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
            Toast.makeText(context, "Permission required", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkPermissionAndExecute(action: () -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            action()
        } else {
            pendingAction = action
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun showMediaPicker() {
        showMediaPickerDialog = true
    }

    fun showVideoPicker() {
        showVideoPickerDialog = true
    }

    fun takeMultiplePhotosFromGallery() {
        pickMultiplePhotosLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    fun takeMultipleVideosFromGallery() {
        pickMultipleVideosLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        )
    }

    fun takePhotoWithCamera() {
        checkPermissionAndExecute {
            val uri = MediaStorageHelper.createImageUri(context, customerId)
            if (uri != null) {
                tempImageUri = uri
                takePhotoLauncher.launch(uri)
            }
        }
    }

    fun takeVideoWithCamera() {
        if (videos.size >= 5) {
            Toast.makeText(context, "Maximum 5 videos allowed", Toast.LENGTH_SHORT).show()
            return
        }

        checkPermissionAndExecute {
            val uri = MediaStorageHelper.createVideoUri(context, customerId)
            if (uri != null) {
                tempVideoUri = uri
                takeVideoLauncher.launch(uri)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { showMediaPicker() },
                modifier = Modifier.weight(1f)
            ) {
                Text("📷 Add Photos")
            }
            Button(
                onClick = { showVideoPicker() },
                modifier = Modifier.weight(1f)
            ) {
                Text("🎥 Add Videos")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Show captured images & videos
        if (images.isNotEmpty() || videos.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Images
                items(images, key = { it.toString() }) { uri ->
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Captured image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { images.remove(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(20.dp)
                                .background(Color.Red.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove image",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // Videos
                items(videos, key = { it.toString() }) { uri ->
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        VideoThumbnail(
                            uri = uri,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { videos.remove(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(20.dp)
                                .background(Color.Red.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove video",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Video",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(24.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        )
                    }
                }
            }
        }
    }

    if (showMediaPickerDialog) {
        AlertDialog(
            onDismissRequest = { showMediaPickerDialog = false },
            title = { Text("Add Photos") },
            text = {
                Column {
                    Button(
                        onClick = {
                            showMediaPickerDialog = false
                            takePhotoWithCamera()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Take Photo")
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            showMediaPickerDialog = false
                            takeMultiplePhotosFromGallery()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choose from Gallery")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showMediaPickerDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showVideoPickerDialog) {
        AlertDialog(
            onDismissRequest = { showVideoPickerDialog = false },
            title = { Text("Add Videos") },
            text = {
                Column {
                    Button(
                        onClick = {
                            showVideoPickerDialog = false
                            takeVideoWithCamera()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Take Video")
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            showVideoPickerDialog = false
                            takeMultipleVideosFromGallery()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choose from Gallery")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showVideoPickerDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun copyImageToAppStorage(context: Context, sourceUri: Uri, customerId: String): Uri? {
    return try {
        val destinationUri = MediaStorageHelper.createImageUri(context, customerId)
            ?: return null

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                input.copyTo(output)
            }
        }
        destinationUri
    } catch (e: Exception) {
        Log.e("CaptureMedia", "Error copying image: ${e.message}")
        null
    }
}

private fun copyVideoToAppStorage(context: Context, sourceUri: Uri, customerId: String): Uri? {
    return try {
        val destinationUri = MediaStorageHelper.createVideoUri(context, customerId)
            ?: return null

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                input.copyTo(output)
            }
        }
        destinationUri
    } catch (e: Exception) {
        Log.e("CaptureMedia", "Error copying video: ${e.message}")
        null
    }
}

private var tempImageUri: Uri? = null
private var tempVideoUri: Uri? = null