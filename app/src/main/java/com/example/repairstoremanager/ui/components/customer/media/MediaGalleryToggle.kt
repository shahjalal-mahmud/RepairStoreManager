package com.example.repairstoremanager.ui.components.customer.media

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.repairstoremanager.R

@Composable
fun MediaGalleryToggle(
    showMediaGallery: Boolean,
    mediaCount: Int,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onToggle,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Text(
            text = if (showMediaGallery) {
                stringResource(R.string.hide_media_count, mediaCount)
            } else {
                stringResource(R.string.show_media_count, mediaCount)
            },
            style = MaterialTheme.typography.labelMedium
        )
    }
}