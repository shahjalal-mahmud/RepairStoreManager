package com.example.repairstoremanager.ui.screens.transaction

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.viewmodel.StockViewModel
import com.example.repairstoremanager.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionViewModel: TransactionViewModel,
    stockViewModel: StockViewModel,
    onNavigateToTransactions: () -> Unit
) {
    val cartProducts by transactionViewModel.cartProducts.collectAsState()
    val invoiceNumber by transactionViewModel.currentInvoiceNumber.collectAsState()
    val isLoading by transactionViewModel.isLoading.collectAsState()

    var showProductPicker by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf("Cash") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Point of Sale") },
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
                onCheckout = { showPaymentDialog = true },
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
            // Invoice Header
            InvoiceHeader(
                invoiceNumber = invoiceNumber ?: "Loading...",
                onNewInvoice = { transactionViewModel.fetchNextInvoiceNumber() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Customer Info
            CustomerInfoSection(
                customerName = customerName,
                customerPhone = customerPhone,
                onNameChange = { customerName = it },
                onPhoneChange = { customerPhone = it }
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

        // Payment Dialog
        if (showPaymentDialog) {
            PaymentDialog(
                total = transactionViewModel.getCartTotal(),
                customerName = customerName,
                paymentType = paymentType,
                onPaymentTypeChange = { paymentType = it },
                onConfirm = {
                    transactionViewModel.createSaleTransaction(
                        customerName = customerName,
                        customerPhone = customerPhone,
                        paymentType = paymentType
                    ) { success, invoice ->
                        showPaymentDialog = false
                        if (success) {
                            // Show success message
                            customerName = ""
                            customerPhone = ""
                        }
                    }
                },
                onCancel = { showPaymentDialog = false }
            )
        }
    }
}