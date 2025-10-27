package com.example.repairstoremanager.ui.components.customer.invoice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.repairstoremanager.data.model.StoreInfo
import com.example.repairstoremanager.data.model.Transaction
import com.example.repairstoremanager.util.POSPrinterHelper
import com.example.repairstoremanager.util.buildTransactionInvoiceText
import com.example.repairstoremanager.viewmodel.StockViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionPrintBottomSheet(
    transaction: Transaction,
    storeInfo: StoreInfo,
    stockViewModel: StockViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val printer = POSPrinterHelper(context)
    val coroutineScope = rememberCoroutineScope()

    // Bluetooth permission launcher for multiple permissions
    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            startPrinting(printer, transaction, storeInfo, stockViewModel, context, onDismiss, coroutineScope)
        } else {
            Toast.makeText(
                context,
                "Bluetooth permissions required for printing",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Check if we need to request permissions
    val needBluetoothPermissions = checkBluetoothPermissions(context)

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
                TransactionPrintPreview(transaction = transaction, storeInfo = storeInfo, stockViewModel = stockViewModel)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (needBluetoothPermissions) {
                        requestBluetoothPermissions(bluetoothPermissionLauncher)
                    } else {
                        startPrinting(printer, transaction, storeInfo, stockViewModel, context, onDismiss, coroutineScope)
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

@Composable
fun TransactionPrintPreview(
    transaction: Transaction,
    storeInfo: StoreInfo,
    stockViewModel: StockViewModel
) {
    val invoiceText = buildTransactionInvoiceText(transaction, storeInfo, stockViewModel)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Invoice Preview",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = invoiceText,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun checkBluetoothPermissions(context: Context): Boolean {
    val requiredPermissions = mutableListOf<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            // For Android 10 and 11, we need location permissions
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    return requiredPermissions.any {
        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
    }
}

private fun requestBluetoothPermissions(
    launcher: ActivityResultLauncher<Array<String>>
) {
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
    launcher.launch(permissions)
}

private fun startPrinting(
    printer: POSPrinterHelper,
    transaction: Transaction,
    storeInfo: StoreInfo,
    stockViewModel: StockViewModel,
    context: Context,
    onDismiss: () -> Unit,
    coroutineScope: CoroutineScope
) {
    coroutineScope.launch {
        try {
            if (printer.connectToPrinter()) {
                val invoiceText = buildTransactionInvoiceText(transaction, storeInfo, stockViewModel)
                if (printer.printText(invoiceText)) {
                    Toast.makeText(
                        context,
                        "Printed successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    onDismiss()
                } else {
                    Toast.makeText(
                        context,
                        "Printing failed - no data sent",
                        Toast.LENGTH_LONG
                    ).show()
                }
                printer.disconnect()
            } else {
                Toast.makeText(
                    context,
                    "Printer connection failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(
                context,
                "Permission denied: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Print error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}