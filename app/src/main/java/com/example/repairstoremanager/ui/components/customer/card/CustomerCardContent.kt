package com.example.repairstoremanager.ui.components.customer.card

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.ui.components.customer.common.AccessoryBadges
import com.example.repairstoremanager.ui.components.customer.common.statusToColor
import com.example.repairstoremanager.ui.components.customer.media.MediaGallery
import com.example.repairstoremanager.ui.components.customer.media.MediaGalleryToggle
import com.example.repairstoremanager.ui.components.customer.media.MediaThumbnail

@Composable
fun CustomerCardContent(
    customer: Customer,
    mediaList: List<Uri>,
    showMediaGallery: Boolean,
    onToggleMediaGallery: () -> Unit,
    onMediaSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val firstMedia = mediaList.firstOrNull()
    val statusColor = statusToColor(customer.status)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Media and details row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (firstMedia != null) {
                MediaThumbnail(
                    uri = firstMedia,
                    onClick = { onMediaSelected(0) }
                )
            }

            CustomerDetails(customer)
        }

        AccessoryBadges(customer)
        SecurityDropdown(customer)

        if (mediaList.size > 1) {
            MediaGalleryToggle(
                showMediaGallery = showMediaGallery,
                mediaCount = mediaList.size,
                onToggle = onToggleMediaGallery
            )

            if (showMediaGallery) {
                MediaGallery(mediaList = mediaList, onMediaSelected = onMediaSelected)
            }
        }
    }
}

@Composable
private fun CustomerDetails(
    customer: Customer,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier, // weight comes from parent
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = customer.customerName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = customer.phoneModel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = customer.problem,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        PaymentInfo(customer)
        ContactInfo(customer)

        if (customer.status in listOf("Pending", "Repaired")) {
            Text(
                text = "Delivery: ${customer.deliveryDate}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PaymentInfo(customer: Customer) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "₹${customer.totalAmount}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Paid: ₹${customer.advanced}",
            style = MaterialTheme.typography.bodySmall,
            color = if (customer.advanced == customer.totalAmount) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
        )
    }
}

@Composable
private fun ContactInfo(customer: Customer) {
    val context = LocalContext.current
    Text(
        text = customer.contactNumber,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clickable {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = "tel:${customer.contactNumber}".toUri()
            }
            context.startActivity(intent)
        }
    )
}