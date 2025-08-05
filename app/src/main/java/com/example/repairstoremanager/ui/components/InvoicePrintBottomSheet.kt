package com.example.repairstoremanager.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.data.model.StoreInfo
import com.example.repairstoremanager.util.POSPrinterHelper
import com.example.repairstoremanager.util.buildInvoiceText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicePrintBottomSheet(
    customer: Customer,
    storeInfo: StoreInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val printer = POSPrinterHelper()
    val coroutineScope = rememberCoroutineScope()

    // Bluetooth permission launcher
    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startPrinting(printer, customer, storeInfo, context, onDismiss, coroutineScope)
        } else {
            Toast.makeText(
                context,
                "Bluetooth permission required for printing",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Check if we need to request permission
    val needBluetoothPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "🖨️ Print Preview",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                InvoicePrintPreview(customer = customer)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (needBluetoothPermission) {
                        bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                    } else {
                        startPrinting(printer, customer, storeInfo, context, onDismiss, coroutineScope)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 8.dp)
            ) {
                Text("🖨️ Print Now", fontSize = 18.sp)
            }
        }
    }
}

private fun startPrinting(
    printer: POSPrinterHelper,
    customer: Customer,
    storeInfo: StoreInfo, // ✅ CORRECT TYPE
    context: Context,
    onDismiss: () -> Unit,
    coroutineScope: CoroutineScope
) {
    coroutineScope.launch {
        try {
            if (printer.connectToPrinter()) {
                val invoiceText = buildInvoiceText(customer, storeInfo)
                printer.printText(invoiceText) // <- Fixed: use invoiceText
                printer.disconnect()
                Toast.makeText(
                    context,
                    "Printed successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                onDismiss()
            } else {
                Toast.makeText(
                    context,
                    "Printer not found",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Print error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
