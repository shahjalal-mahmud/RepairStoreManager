package com.example.repairstoremanager.ui.components.customer.list

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.ui.components.customer.add.PatternLockCanvas
import com.example.repairstoremanager.ui.components.customer.common.StatusDropdown
import com.example.repairstoremanager.ui.components.customer.common.statusToColor
import com.example.repairstoremanager.ui.components.customer.invoice.InvoicePrintBottomSheet
import com.example.repairstoremanager.ui.components.customer.media.CustomerMediaViewer
import com.example.repairstoremanager.ui.components.customer.media.VideoThumbnail
import com.example.repairstoremanager.ui.screens.EditCustomerScreen
import com.example.repairstoremanager.util.MediaStorageHelper
import com.example.repairstoremanager.util.MessageHelper
import com.example.repairstoremanager.viewmodel.CustomerViewModel
import com.example.repairstoremanager.viewmodel.StoreViewModel
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun CustomerCards(customer: Customer, viewModel: CustomerViewModel) {
    var showMediaGallery by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val storeViewModel: StoreViewModel = viewModel()
    var showCallOptions by remember { mutableStateOf(false) }

    val statusOptions = listOf("Pending", "Repaired", "Delivered", "Cancelled")
    val selectedStatus = customer.status
    val statusColor = statusToColor(selectedStatus)

    var showPrintSheet by remember { mutableStateOf(false) }
    var showFullScreenMedia by remember { mutableStateOf(false) }
    var selectedMediaIndex by remember { mutableStateOf(0) }
    var showEditScreen by remember { mutableStateOf(false) }

    val mediaList by remember(customer.invoiceNumber) {
        derivedStateOf {
            try {
                MediaStorageHelper.getMediaForCustomer(context, customer.invoiceNumber)
            } catch (e: Exception) {
                Log.e("CustomerCard", "Error loading media: ${e.message}")
                emptyList()
            }
        }
    }

    val firstMedia = mediaList.firstOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Invoice and Date Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = customer.invoiceNumber,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Text(
                    text = customer.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Media Thumbnail and Details Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Media Thumbnail
                    if (firstMedia != null) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showFullScreenMedia = true }
                        ) {
                            if (isVideoUri(context, firstMedia)) {
                                VideoThumbnail(
                                    uri = firstMedia,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Play video",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                )
                            } else {
                                AsyncImage(
                                    model = firstMedia,
                                    contentDescription = "Customer device media",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    // Customer Details
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = customer.customerName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
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

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "₹${customer.totalAmount}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
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

                        if (selectedStatus in listOf("Pending", "Repaired")) {
                            Text(
                                text = "Delivery: ${customer.deliveryDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Accessories Badges
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (customer.battery) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            content = {
                                Text("Battery", style = MaterialTheme.typography.labelSmall)
                            }
                        )
                    }
                    if (customer.sim) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            content = {
                                Text("SIM", style = MaterialTheme.typography.labelSmall)
                            }
                        )
                    }
                    if (customer.memory) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            content = {
                                Text("Memory", style = MaterialTheme.typography.labelSmall)
                            }
                        )
                    }
                    if (customer.simTray) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            content = {
                                Text("SIM Tray", style = MaterialTheme.typography.labelSmall)
                            }
                        )
                    }
                    if (customer.backCover) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            content = {
                                Text("Back Cover", style = MaterialTheme.typography.labelSmall)
                            }
                        )
                    }
                    if (customer.deadPermission) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            content = {
                                Text("Dead Permission", style = MaterialTheme.typography.labelSmall)
                            }
                        )
                    }
                }

                // Security Dropdown
                var securityExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = securityExpanded,
                    onExpandedChange = { securityExpanded = !securityExpanded }
                ) {
                    OutlinedTextField(
                        value = if (customer.securityType == "Password") "Password Protected" else "Pattern Lock",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = securityExpanded)
                        },
                        label = { Text("Security") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = securityExpanded,
                        onDismissRequest = { securityExpanded = false }
                    ) {
                        if (customer.securityType == "Password") {
                            Text(
                                text = "Password: ${customer.phonePassword}",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Pattern Lock:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                PatternLockCanvas(
                                    pattern = customer.pattern,
                                    isInteractive = false,
                                    isPreview = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                )
                            }
                        }
                    }
                }

                // Media Gallery Button
                if (mediaList.size > 1) {
                    Button(
                        onClick = { showMediaGallery = !showMediaGallery },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text(
                            text = if (showMediaGallery) "Hide Media (${mediaList.size})" else "Show All Media (${mediaList.size})",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // Show media gallery if expanded
                if (showMediaGallery && mediaList.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(mediaList.size) { index ->
                            val mediaUri = mediaList[index]
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedMediaIndex = index
                                        showFullScreenMedia = true
                                    }
                            ) {
                                if (isVideoUri(context, mediaUri)) {
                                    VideoThumbnail(
                                        uri = mediaUri,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Play video",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(24.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    )
                                } else {
                                    AsyncImage(
                                        model = mediaUri,
                                        contentDescription = "Customer device media",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Print Button
                    Button(
                        onClick = { showPrintSheet = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Print",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Print",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Edit Button
                    Button(
                        onClick = { showEditScreen = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Call Dropdown Button
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        Button(
                            onClick = { showCallOptions = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Contact",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Contact",
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        DropdownMenu(
                            expanded = showCallOptions,
                            onDismissRequest = { showCallOptions = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Call") },
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = "tel:${customer.contactNumber}".toUri()
                                    }
                                    context.startActivity(intent)
                                    showCallOptions = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Call, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("SMS") },
                                onClick = {
                                    val message = viewModel.getStatusMessage(customer)
                                    MessageHelper.sendSmsViaIntent(context, customer.contactNumber, message)
                                    showCallOptions = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Sms, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("WhatsApp") },
                                onClick = {
                                    val message = viewModel.getStatusMessage(customer)
                                    MessageHelper.sendWhatsAppMessage(context, customer.contactNumber, message)
                                    showCallOptions = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Chat, contentDescription = null)
                                }
                            )
                        }
                    }

                    // Status Dropdown
                    StatusDropdown(
                        selectedStatus = selectedStatus,
                        options = statusOptions,
                        onStatusChange = { newStatus ->
                            viewModel.updateCustomerStatus(
                                customerId = customer.id,
                                newStatus = newStatus,
                            )
                        },
                        statusColor = statusColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (showPrintSheet) {
        InvoicePrintBottomSheet(
            customer = customer,
            storeInfo = storeViewModel.storeInfo,
            onDismiss = { showPrintSheet = false }
        )
    }

    if (showFullScreenMedia) {
        CustomerMediaViewer(
            context = LocalContext.current,
            customerId = customer.invoiceNumber,
            initialIndex = selectedMediaIndex,
            onClose = { showFullScreenMedia = false }
        )
    }
    if (showEditScreen) {
        Dialog(onDismissRequest = { showEditScreen = false }) {
            EditCustomerScreen(
                customer = customer,
                onSave = { updatedCustomer ->
                    // Handle the updated customer if needed
                    showEditScreen = false
                },
                onCancel = { showEditScreen = false },
                viewModel = viewModel
            )
        }
    }
}
private fun isVideoUri(context: Context, uri: Uri): Boolean {
    val type = context.contentResolver.getType(uri) ?: return false
    return type.startsWith("video/")
}