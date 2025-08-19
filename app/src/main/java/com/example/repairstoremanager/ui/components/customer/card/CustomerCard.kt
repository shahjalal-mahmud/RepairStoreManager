package com.example.repairstoremanager.ui.components.customer.card

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.util.MediaStorageHelper
import com.example.repairstoremanager.viewmodel.CustomerViewModel
import com.example.repairstoremanager.viewmodel.StoreViewModel

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun CustomerCard(
    customer: Customer,
    viewModel: CustomerViewModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // State management
    var showMediaGallery by remember { mutableStateOf(false) }
    var showPrintSheet by remember { mutableStateOf(false) }
    var showFullScreenMedia by remember { mutableStateOf(false) }
    var selectedMediaIndex by remember { mutableStateOf(0) }
    var showEditScreen by remember { mutableStateOf(false) }
    var showCallOptions by remember { mutableStateOf(false) }
    var callButtonBounds by remember { mutableStateOf<Rect?>(null) }

    val context = LocalContext.current
    val storeViewModel: StoreViewModel = viewModel()
    val mediaList = rememberMediaList(context, customer.invoiceNumber)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            CustomerCardHeader(customer)
            CustomerCardContent(
                customer = customer,
                mediaList = mediaList,
                showMediaGallery = showMediaGallery,
                onToggleMediaGallery = { showMediaGallery = !showMediaGallery },
                onMediaSelected = { index ->
                    selectedMediaIndex = index
                    showFullScreenMedia = true
                }
            )
            CustomerCardActions(
                customer = customer,
                viewModel = viewModel,
                onPrintClick = { showPrintSheet = true },
                onEditClick = { showEditScreen = true },
                onCallClick = { bounds ->
                    callButtonBounds = bounds
                    showCallOptions = true
                },
                callExpanded = showCallOptions,
                callButtonBounds = callButtonBounds,
                onCallDismiss = { showCallOptions = false }
            )
        }
    }

    // Handle dialogs and sheets
    CustomerCardDialogs(
        customer = customer,
        viewModel = viewModel,
        storeViewModel = storeViewModel,
        mediaList = mediaList,
        showPrintSheet = showPrintSheet,
        showFullScreenMedia = showFullScreenMedia,
        showEditScreen = showEditScreen,
        selectedMediaIndex = selectedMediaIndex,
        onDismissPrint = { showPrintSheet = false },
        onDismissMedia = { showFullScreenMedia = false },
        onDismissEdit = { showEditScreen = false },
    )
}

@Composable
private fun rememberMediaList(context: Context, invoiceNumber: String): List<Uri> {
    return remember(invoiceNumber) {
        try {
            MediaStorageHelper.getMediaForCustomer(context, invoiceNumber)
        } catch (e: Exception) {
            Log.e("CustomerCard", "Error loading media: ${e.message}")
            emptyList()
        }
    }
}