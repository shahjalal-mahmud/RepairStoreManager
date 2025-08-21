package com.example.repairstoremanager.ui.components.customer.card

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.ui.components.customer.invoice.InvoicePrintBottomSheet
import com.example.repairstoremanager.ui.components.customer.media.CustomerMediaViewer
import com.example.repairstoremanager.viewmodel.CustomerViewModel
import com.example.repairstoremanager.viewmodel.StoreViewModel

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun CustomerCardDialogs(
    customer: Customer,
    viewModel: CustomerViewModel,
    storeViewModel: StoreViewModel,
    mediaList: List<Uri>,
    showPrintSheet: Boolean,
    showFullScreenMedia: Boolean,
    selectedMediaIndex: Int,
    onDismissPrint: () -> Unit,
    onDismissMedia: () -> Unit,
    navController: NavHostController
)  {
    val context = LocalContext.current

    if (showPrintSheet) {
        InvoicePrintBottomSheet(
            customer = customer,
            storeInfo = storeViewModel.storeInfo,
            onDismiss = onDismissPrint
        )
    }

    if (showFullScreenMedia) {
        CustomerMediaViewer(
            context = context,
            customerId = customer.invoiceNumber,
            initialIndex = selectedMediaIndex,
            onClose = onDismissMedia
        )
    }
}