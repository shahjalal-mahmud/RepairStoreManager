package com.example.repairstoremanager.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object MediaStorageHelper {

    // For camera capture (using FileProvider)
    fun createImageCaptureUri(context: Context, customerId: String): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.getExternalFilesDir("images")
            val file = File.createTempFile(
                "IMG_${customerId}_${timeStamp}_",
                ".jpg",
                storageDir
            )
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } catch (e: Exception) {
            Log.e("MediaStorageHelper", "Error creating image capture URI", e)
            null
        }
    }

    // For gallery/media store
    fun createImageUri(context: Context, customerId: String): Uri? {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${customerId}_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/RepairStoreManager/$customerId")
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        } catch (e: Exception) {
            Log.e("MediaStorageHelper", "Error creating image URI", e)
            null
        }
    }

    // For video capture (using FileProvider)
    fun createVideoCaptureUri(context: Context, customerId: String): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?:
            context.getExternalFilesDir("videos") ?:
            context.filesDir

            val videoFile = File.createTempFile(
                "VID_${customerId}_${timeStamp}_",
                ".mp4",
                storageDir
            )

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                videoFile
            )
        } catch (e: Exception) {
            Log.e("MediaStorageHelper", "Error creating video capture URI", e)
            null
        }
    }

    // For video media store
    fun createVideoUri(context: Context, customerId: String): Uri? {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, "VID_${customerId}_${System.currentTimeMillis()}.mp4")
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/RepairStoreManager/$customerId")
            }
            context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
        } catch (e: Exception) {
            Log.e("MediaStorageHelper", "Error creating video URI", e)
            null
        }
    }

    fun getMediaForCustomer(context: Context, customerId: String): List<Uri> {
        val mediaList = mutableListOf<Uri>()
        val mediaItems = mutableListOf<Pair<Uri, Long>>()

        // Query images
        val imageProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED
        )
        val imageSelection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        val imageSelectionArgs = arrayOf("%RepairStoreManager/$customerId%")

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            imageProjection,
            imageSelection,
            imageSelectionArgs,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                try {
                    val id = cursor.getLong(idColumn)
                    val date = cursor.getLong(dateColumn)
                    val uri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )
                    mediaItems.add(uri to date)
                } catch (e: Exception) {
                    Log.e("MediaStorageHelper", "Error processing image: ${e.message}")
                }
            }
        }

        // Query videos
        val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATE_ADDED
        )
        val videoSelection = "${MediaStore.Video.Media.RELATIVE_PATH} LIKE ?"
        val videoSelectionArgs = arrayOf("%RepairStoreManager/$customerId%")

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            videoProjection,
            videoSelection,
            videoSelectionArgs,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                try {
                    val id = cursor.getLong(idColumn)
                    val date = cursor.getLong(dateColumn)
                    val uri = Uri.withAppendedPath(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )
                    mediaItems.add(uri to date)
                } catch (e: Exception) {
                    Log.e("MediaStorageHelper", "Error processing video: ${e.message}")
                }
            }
        }

        // Sort by date (newest first) and extract URIs
        return mediaItems
            .sortedByDescending { it.second }
            .map { it.first }
    }
}