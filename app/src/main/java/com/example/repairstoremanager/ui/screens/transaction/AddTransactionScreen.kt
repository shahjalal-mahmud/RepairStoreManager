package com.example.repairstoremanager.ui.screens.transaction

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.data.model.Transaction
import com.example.repairstoremanager.ui.components.customer.invoice.TransactionPrintBottomSheet
import com.example.repairstoremanager.util.POSPrinterHelper
import com.example.repairstoremanager.viewmodel.StockViewModel
import com.example.repairstoremanager.viewmodel.StoreViewModel
import com.example.repairstoremanager.viewmodel.TransactionViewModel

@RequiresApi(Build.VERSION_CODES.S)
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionViewModel: TransactionViewModel,
    stockViewModel: StockViewModel,
    storeViewModel: StoreViewModel,
    onNavigateToTransactions: () -> Unit
) {
    val cartProducts by transactionViewModel.cartProducts.collectAsState()
    val isLoading by transactionViewModel.isLoading.collectAsState()
    val storeInfo = storeViewModel.storeInfo
    val context = LocalContext.current

    var showProductPicker by remember { mutableStateOf(false) }
    var showPrintDialog by remember { mutableStateOf(false) }
    var customerName by remember { mutableStateOf("Walk-in Customer") }
    var showPrintSheet by remember { mutableStateOf(false) }
    var currentTransaction by remember { mutableStateOf<Transaction?>(null) }

    // POS Printer function
    val printToPosPrinter = { invoiceText: String ->
        val printerHelper = POSPrinterHelper(context)
        try {
            // Connect to printer (you might want to handle this connection more efficiently)
            val connected = printerHelper.connectToPrinter()
            if (connected) {
                val success = printerHelper.printText(invoiceText)
                if (success) {
                    // Show success message if needed
                    android.widget.Toast.makeText(context, "Invoice printed successfully", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, "Print failed", android.widget.Toast.LENGTH_SHORT).show()
                }
            } else {
                android.widget.Toast.makeText(context, "Could not connect to printer", android.widget.Toast.LENGTH_SHORT).show()
            }
        } finally {
            printerHelper.disconnect()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales Counter") },
                actions = {
                    IconButton(onClick = { onNavigateToTransactions() }) {
                        Icon(Icons.Default.Receipt, contentDescription = "Transactions")
                    }
                }
            )
        },
        bottomBar = {
            SalesBottomBar(
                total = transactionViewModel.getCartTotal(),
                itemCount = cartProducts.size,
                onCompleteSale = { showPrintDialog = true },
                isLoading = isLoading
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Customer Name Input (Optional)
            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("Customer Name") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Walk-in Customer") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Cart Items
            Text(
                "Cart Items (${cartProducts.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (cartProducts.isEmpty()) {
                EmptyCartPlaceholder(onAddProducts = { showProductPicker = true })
            } else {
                CartItemsList(
                    products = cartProducts,
                    onUpdateQuantity = { productId, quantity ->
                        transactionViewModel.updateCartQuantity(productId, quantity)
                    },
                    onUpdatePrice = { productId, price ->
                        transactionViewModel.updateCartPrice(productId, price)
                    },
                    onRemoveItem = { productId ->
                        transactionViewModel.removeFromCart(productId)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Product Button
            Button(
                onClick = { showProductPicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Products")
            }
        }

        // Product Picker Dialog
        if (showProductPicker) {
            ProductPickerDialog(
                stockViewModel = stockViewModel,
                onProductSelected = { product ->
                    transactionViewModel.addToCart(product)
                    showProductPicker = false
                },
                onDismiss = { showProductPicker = false }
            )
        }

        // Print Confirmation Dialog
        if (showPrintDialog) {
            PrintConfirmationDialog(
                total = transactionViewModel.getCartTotal(),
                onPrintAndSave = {
                    // Create transaction and show print sheet
                    transactionViewModel.createSaleTransaction(
                        customerName = customerName,
                        paymentType = "Cash"
                    ) { success, invoiceNumber ->
                        showPrintDialog = false
                        if (success) {
                            // Get the saved transaction and show print sheet
                            val transaction = transactionViewModel.getTransactionForPrinting(invoiceNumber ?: "")
                            currentTransaction = transaction
                            showPrintSheet = true
                            customerName = "Walk-in Customer"
                        } else {
                            // Show error message
                            android.widget.Toast.makeText(context, "Failed to save transaction", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onSaveOnly = {
                    // Create transaction without printing
                    transactionViewModel.createSaleTransaction(
                        customerName = customerName,
                        paymentType = "Cash"
                    ) { success, invoiceNumber ->
                        showPrintDialog = false
                        if (success) {
                            // Show success message
                            android.widget.Toast.makeText(context, "Transaction saved successfully", android.widget.Toast.LENGTH_SHORT).show()
                            customerName = "Walk-in Customer"
                        } else {
                            // Show error message
                            android.widget.Toast.makeText(context, "Failed to save transaction", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onCancel = { showPrintDialog = false },
                isLoading = isLoading
            )
        }

        // Print Bottom Sheet
        if (showPrintSheet && currentTransaction != null) {
            TransactionPrintBottomSheet(
                transaction = currentTransaction!!,
                storeInfo = storeInfo,
                stockViewModel = stockViewModel,
                onDismiss = { showPrintSheet = false }
            )
        }
    }
}