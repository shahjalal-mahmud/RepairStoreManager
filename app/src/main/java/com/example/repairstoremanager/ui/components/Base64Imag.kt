package com.example.repairstoremanager.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.io.ByteArrayInputStream

@Composable
fun Base64Image(base64String: String?, modifier: Modifier = Modifier) {
    val imageBitmap: ImageBitmap? = remember(base64String) {
        try {
            base64String?.let {
                val imageBytes = Base64.decode(it, Base64.DEFAULT)
                val inputStream = ByteArrayInputStream(imageBytes)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                bitmap?.asImageBitmap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    imageBitmap?.let {
        Image(
            bitmap = it,
            contentDescription = "Store Logo",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}
