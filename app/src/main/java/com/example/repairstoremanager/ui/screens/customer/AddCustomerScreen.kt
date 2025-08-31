package com.example.repairstoremanager.ui.screens.customer

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.repairstoremanager.ui.components.customer.add.InvoiceFormSection

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Add New Customer", style = MaterialTheme.typography.titleLarge)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        // Pass the inner padding to InvoiceFormSection for proper spacing
        InvoiceFormSection(modifier = Modifier.padding(paddingValues))
    }
}
