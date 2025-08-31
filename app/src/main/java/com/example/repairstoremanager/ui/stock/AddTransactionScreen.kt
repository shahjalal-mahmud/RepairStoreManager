package com.example.repairstoremanager.ui.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.repairstoremanager.data.model.Product
import com.example.repairstoremanager.data.model.TransactionProduct
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

@Composable
fun InvoiceHeader(invoiceNumber: String, onNewInvoice: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Invoice Number", style = MaterialTheme.typography.labelMedium)
                Text(invoiceNumber, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Button(onClick = onNewInvoice, shape = MaterialTheme.shapes.medium) {
                Text("New Invoice")
            }
        }
    }
}

@Composable
fun CustomerInfoSection(
    customerName: String,
    customerPhone: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Customer Information", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customerName,
                onValueChange = onNameChange,
                label = { Text("Customer Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Customer") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customerPhone,
                onValueChange = onPhoneChange,
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") }
            )
        }
    }
}

@Composable
fun EmptyCartPlaceholder(onAddProducts: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.LightGray.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ShoppingCart, contentDescription = "Empty Cart", modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("No products added", style = MaterialTheme.typography.bodyMedium)
            Text("Tap 'Add Products' to start", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun CartItemsList(
    products: List<TransactionProduct>,
    onUpdateQuantity: (String, Int) -> Unit,
    onUpdatePrice: (String, Double) -> Unit,
    onRemoveItem: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products) { product ->
            CartItemCard(
                product = product,
                onQuantityChange = { onUpdateQuantity(product.productId, it) },
                onPriceChange = { onUpdatePrice(product.productId, it) },
                onRemove = { onRemoveItem(product.productId) }
            )
        }
    }
}

@Composable
fun CartItemCard(
    product: TransactionProduct,
    onQuantityChange: (Int) -> Unit,
    onPriceChange: (Double) -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quantity Control
                QuantitySelector(
                    quantity = product.quantity,
                    onQuantityChange = onQuantityChange,
                    modifier = Modifier.weight(1f)
                )

                // Price Input
                PriceInput(
                    price = product.price,
                    onPriceChange = onPriceChange,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Total: $${"%.2f".format(product.price * product.quantity)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun QuantitySelector(quantity: Int, onQuantityChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onQuantityChange(quantity - 1) },
            enabled = quantity > 1
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease")
        }

        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        IconButton(onClick = { onQuantityChange(quantity + 1) }) {
            Icon(Icons.Default.Add, contentDescription = "Increase")
        }
    }
}

@Composable
fun PriceInput(price: Double, onPriceChange: (Double) -> Unit, modifier: Modifier = Modifier) {
    var priceText by remember { mutableStateOf(price.toString()) }

    OutlinedTextField(
        value = priceText,
        onValueChange = {
            priceText = it
            it.toDoubleOrNull()?.let { newPrice -> onPriceChange(newPrice) }
        },
        label = { Text("Price") },
        modifier = modifier,
        prefix = { Text("$") }
    )
}

@Composable
fun SalesBottomBar(total: Double, itemCount: Int, onCheckout: () -> Unit, isLoading: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total Items: $itemCount", style = MaterialTheme.typography.bodySmall)
                Text(
                    "$${"%.2f".format(total)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onCheckout,
                enabled = itemCount > 0 && !isLoading,
                modifier = Modifier.height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Checkout")
                }
            }
        }
    }
}

@Composable
fun ProductPickerDialog(
    stockViewModel: StockViewModel,
    onProductSelected: (Product) -> Unit,
    onDismiss: () -> Unit
) {
    val products by stockViewModel.products.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Product", style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search products...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    val filteredProducts = if (searchQuery.isBlank()) {
                        products
                    } else {
                        products.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    }

                    items(filteredProducts) { product ->
                        ProductPickerItem(
                            product = product,
                            onClick = { onProductSelected(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductPickerItem(product: Product, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text("Stock: ${product.quantity}", style = MaterialTheme.typography.bodySmall)
            }
            Text("$${product.sellingPrice}", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun PaymentDialog(
    total: Double,
    customerName: String,
    paymentType: String,
    onPaymentTypeChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Confirm Payment") },
        text = {
            Column {
                Text("Customer: ${customerName.ifBlank { "Walk-in Customer" }}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Total Amount: $${"%.2f".format(total)}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                PaymentTypeSelector(
                    selectedType = paymentType,
                    onTypeSelected = onPaymentTypeChange
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentTypeSelector(selectedType: String, onTypeSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val paymentTypes = listOf("Cash", "Card", "Mobile Banking", "Bank Transfer")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedType,
            onValueChange = {},
            label = { Text("Payment Method") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            paymentTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}