package com.example.repairstoremanager.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import com.example.repairstoremanager.ui.components.customer.add.QuickInvoiceForm

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun QuickInvoiceScreen(onClose: () -> Unit) {
    QuickInvoiceForm(
        onClose = onClose,
        onSaveSuccess = {
            // Optional: Navigate back or show success message
            onClose()
        }
    )
}