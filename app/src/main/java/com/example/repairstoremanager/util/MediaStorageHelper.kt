package com.example.repairstoremanager.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object MediaStorageHelper {

    // Create a new MediaStore entry for an image
    fun createImageUri(context: Context, customerId: String): Uri? {
        return try {
            val contentValues = ContentValues().apply {
                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    "IMG_${customerId}_${System.currentTimeMillis()}.jpg"
                )
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/RepairStoreManager/$customerId"
                )
            }
            context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
        } catch (e: Exception) {
            Log.e("MediaStorageHelper", "Error creating image URI", e)
            null
        }
    }

    // Create a new MediaStore entry for a video
    fun createVideoUri(context: Context, customerId: String): Uri? {
        return try {
            val contentValues = ContentValues().apply {
                put(
                    MediaStore.Video.Media.DISPLAY_NAME,
                    "VID_${customerId}_${System.currentTimeMillis()}.mp4"
                )
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(
                    MediaStore.Video.Media.RELATIVE_PATH,
                    "Movies/RepairStoreManager/$customerId"
                )
            }
            context.contentResolver.insert(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
        } catch (e: Exception) {
            Log.e("MediaStorageHelper", "Error creating video URI", e)
            null
        }
    }

    // Retrieve all media for a customer (images + videos)
    fun getMediaForCustomer(context: Context, customerId: String): List<Uri> {
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
                val id = cursor.getLong(idColumn)
                val date = cursor.getLong(dateColumn)
                val uri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )
                mediaItems.add(uri to date)
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
                val id = cursor.getLong(idColumn)
                val date = cursor.getLong(dateColumn)
                val uri = Uri.withAppendedPath(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )
                mediaItems.add(uri to date)
            }
        }

        // Sort by date (newest first)
        return mediaItems
            .sortedByDescending { it.second }
            .map { it.first }
    }
    fun saveImageFromUri(context: Context, sourceUri: Uri, fileName: String): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val file = File(context.filesDir, fileName) // app internal storage
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            Uri.fromFile(file) // permanent Uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}