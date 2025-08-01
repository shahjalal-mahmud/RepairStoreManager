package com.example.repairstoremanager.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.util.POSPrinterHelper
import com.example.repairstoremanager.util.buildInvoiceText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicePrintBottomSheet(
    customer: Customer,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val printer = POSPrinterHelper()
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.75f),
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("🖨️ Print Preview", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(12.dp))
                    InvoicePrintPreview(customer = customer)
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                if (printer.connectToPrinter()) {
                                    val text = buildInvoiceText(customer)
                                    printer.printText(text)
                                    printer.disconnect()
                                    Toast.makeText(context, "Printed successfully!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "Printer not found.", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("🖨️ Print Now", fontSize = 18.sp)
                }
            }
        }
    )
}
