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
    val firstMedia = mediaList.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp) // Reduced overall spacing
    ) {
        // Top Row: Media thumbnail and device/problem details
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

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Customer and device info
                Text(
                    text = customer.customerName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = customer.phoneModel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = customer.problem,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Contact and delivery information
        ContactAndDeliveryInfo(customer)

        // Payment information
        PaymentDetails(customer)

        // Accessories and Security with minimal gap
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp) // Reduced gap between these two
        ) {
            AccessoryBadges(customer)
            SecurityDropdown(customer)
            AdditionalDetailsSection(customer = customer)
        }

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
private fun PaymentDetails(customer: Customer) {
    // Safely parse amounts with default fallback values
    val advanced = customer.advanced?.toIntOrNull() ?: 0
    val totalAmount = customer.totalAmount?.toIntOrNull() ?: 0

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Total: ৳$totalAmount",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Paid: ৳$advanced",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (advanced >= totalAmount) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )

                if (advanced < totalAmount) {
                    Text(
                        text = "Due: ৳${totalAmount - advanced}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
@Composable
private fun ContactAndDeliveryInfo(customer: Customer) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Phone number (clickable)
        Column(
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = "tel:${customer.contactNumber}".toUri()
                }
                context.startActivity(intent)
            }
        ) {
            Text(
                text = "Contact",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = customer.contactNumber,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Delivery date
        if (customer.status in listOf("Pending", "Repaired")) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Delivery Date",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = customer.deliveryDate,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}