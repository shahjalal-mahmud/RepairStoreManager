package com.example.repairstoremanager.ui.screens.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.data.model.Product
import com.example.repairstoremanager.data.model.PurchaseProduct
import com.example.repairstoremanager.viewmodel.StockViewModel
import com.example.repairstoremanager.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionTypeScreen(
    transactionViewModel: TransactionViewModel,
    stockViewModel: StockViewModel,
    onBack: () -> Unit
) {
    var selectedTransactionType by remember { mutableStateOf("Sale") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Transaction Type Selector
            TransactionTypeSelector(
                selectedType = selectedTransactionType,
                onTypeSelected = { selectedTransactionType = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic Form based on selected type
            when (selectedTransactionType) {
                "Sale" -> SaleTransactionForm(
                    transactionViewModel = transactionViewModel,
                    stockViewModel = stockViewModel,
                    onSuccess = { message ->
                        successMessage = message
                        showSuccessDialog = true
                    }
                )
                "Purchase" -> PurchaseTransactionForm(
                    transactionViewModel = transactionViewModel,
                    stockViewModel = stockViewModel,
                    onSuccess = { message ->
                        successMessage = message
                        showSuccessDialog = true
                    }
                )
                "Service" -> ServiceTransactionForm(
                    transactionViewModel = transactionViewModel,
                    onSuccess = { message ->
                        successMessage = message
                        showSuccessDialog = true
                    }
                )
                "Expense" -> ExpenseTransactionForm(
                    transactionViewModel = transactionViewModel,
                    onSuccess = { message ->
                        successMessage = message
                        showSuccessDialog = true
                    }
                )
                "Income" -> IncomeTransactionForm(
                    transactionViewModel = transactionViewModel,
                    onSuccess = { message ->
                        successMessage = message
                        showSuccessDialog = true
                    }
                )
            }
        }

        // Success Dialog
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                title = { Text("Success") },
                text = { Text(successMessage) },
                confirmButton = {
                    TextButton(onClick = { showSuccessDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun TransactionTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    val transactionTypes = listOf("Sale", "Purchase", "Service", "Expense", "Income")

    Column {
        Text(
            "Transaction Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Use LazyRow for better handling of multiple chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transactionTypes) { type ->
                val isSelected = selectedType == type
                FilterChip(
                    selected = isSelected,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null
                )
            }
        }
    }
}

@Composable
fun SaleTransactionForm(
    transactionViewModel: TransactionViewModel,
    stockViewModel: StockViewModel,
    onSuccess: (String) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf("Cash") }
    var showProductPicker by remember { mutableStateOf(false) }
    val cartProducts by transactionViewModel.cartProducts.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Sale Transaction",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Customer Information
        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("Customer Name") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Walk-in Customer") }
        )

        OutlinedTextField(
            value = customerPhone,
            onValueChange = { customerPhone = it },
            label = { Text("Customer Phone") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Optional") }
        )

        // Payment Type
        Text("Payment Method", style = MaterialTheme.typography.titleMedium)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("Cash", "Card", "UPI", "Bank Transfer")) { method ->
                FilterChip(
                    selected = paymentType == method,
                    onClick = { paymentType = method },
                    label = { Text(method) }
                )
            }
        }

        // Products Section
        Text("Products", style = MaterialTheme.typography.titleMedium)

        if (cartProducts.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No products added", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            // You'll need to implement CartItemsList or use a simple list
            SimpleCartItemsList(
                products = cartProducts,
                onRemoveItem = { productId ->
                    transactionViewModel.removeFromCart(productId)
                }
            )

            // Total Amount - Fixed formatting
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Amount:", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "₹${"%.2f".format(transactionViewModel.getCartTotal())}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showProductPicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Products")
            }

            Button(
                onClick = {
                    transactionViewModel.createSaleTransaction(
                        customerName = if (customerName.isBlank()) "Walk-in Customer" else customerName,
                        customerPhone = customerPhone,
                        paymentType = paymentType
                    ) { success, invoice, errorMessage ->
                        if (success) {
                            onSuccess("Sale transaction completed successfully!\nInvoice: $invoice")
                            customerName = ""
                            customerPhone = ""
                        } else {
                            // Handle error
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = cartProducts.isNotEmpty()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete Sale")
            }
        }
    }

    // Product Picker Dialog - Simple implementation
    if (showProductPicker) {
        SimpleProductPickerDialog(
            stockViewModel = stockViewModel,
            onProductSelected = { product ->
                transactionViewModel.addToCart(product)
                showProductPicker = false
            },
            onDismiss = { showProductPicker = false }
        )
    }
}

@Composable
fun PurchaseTransactionForm(
    transactionViewModel: TransactionViewModel,
    stockViewModel: StockViewModel,
    onSuccess: (String) -> Unit
) {
    var supplierName by remember { mutableStateOf("") }
    var showProductPicker by remember { mutableStateOf(false) }
    var purchaseProducts by remember { mutableStateOf<List<PurchaseProduct>>(emptyList()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Purchase Transaction",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = supplierName,
            onValueChange = { supplierName = it },
            label = { Text("Supplier Name") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter supplier name") }
        )

        // Purchase Products Section
        Text("Products to Purchase", style = MaterialTheme.typography.titleMedium)

        if (purchaseProducts.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No products added for purchase", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            PurchaseProductsList(
                products = purchaseProducts,
                onUpdateQuantity = { productId, quantity ->
                    purchaseProducts = purchaseProducts.map {
                        if (it.productId == productId) it.copy(quantity = quantity) else it
                    }
                },
                onUpdatePrice = { productId, purchasePrice, sellingPrice ->
                    purchaseProducts = purchaseProducts.map {
                        if (it.productId == productId) it.copy(
                            purchasePrice = purchasePrice,
                            sellingPrice = sellingPrice
                        ) else it
                    }
                },
                onRemoveItem = { productId ->
                    purchaseProducts = purchaseProducts.filter { it.productId != productId }
                }
            )

            // Total Cost - Fixed formatting
            val totalCost = purchaseProducts.sumOf { it.totalCost }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Cost:", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "₹${"%.2f".format(totalCost)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showProductPicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Products")
            }

            Button(
                onClick = {
                    transactionViewModel.createPurchaseTransaction(
                        products = purchaseProducts,
                        supplier = supplierName
                    ) { success, message, errorMessage ->
                        if (success) {
                            onSuccess("Purchase transaction completed successfully!")
                            supplierName = ""
                            purchaseProducts = emptyList()
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = purchaseProducts.isNotEmpty() && supplierName.isNotBlank()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Purchase")
            }
        }
    }

    // Product Picker Dialog for Purchase
    if (showProductPicker) {
        PurchaseProductPickerDialog(
            stockViewModel = stockViewModel,
            onProductSelected = { product ->
                val purchaseProduct = PurchaseProduct(
                    productId = product.id,
                    name = product.name,
                    purchasePrice = product.buyingPrice,
                    sellingPrice = product.sellingPrice,
                    quantity = 1
                )
                purchaseProducts = purchaseProducts + purchaseProduct
                showProductPicker = false
            },
            onDismiss = { showProductPicker = false }
        )
    }
}

@Composable
fun ServiceTransactionForm(
    transactionViewModel: TransactionViewModel,
    onSuccess: (String) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var serviceDescription by remember { mutableStateOf("") }
    var serviceCharge by remember { mutableStateOf("") }
    var partsCost by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Service Transaction",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("Customer Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = serviceDescription,
            onValueChange = { serviceDescription = it },
            label = { Text("Service Description") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g., Phone screen replacement, TV repair") },
            maxLines = 3
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = serviceCharge,
                onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*$"))) serviceCharge = it },
                label = { Text("Service Charge (₹)") },
                modifier = Modifier.weight(1f),
                placeholder = { Text("0.00") }
            )

            OutlinedTextField(
                value = partsCost,
                onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*$"))) partsCost = it },
                label = { Text("Parts Cost (₹)") },
                modifier = Modifier.weight(1f),
                placeholder = { Text("0.00") }
            )
        }

        // Calculate total - Fixed formatting
        val totalAmount = (serviceCharge.toDoubleOrNull() ?: 0.0) + (partsCost.toDoubleOrNull() ?: 0.0)
        if (totalAmount > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Amount:", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "₹${"%.2f".format(totalAmount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Button(
            onClick = {
                transactionViewModel.createServiceTransaction(
                    customerName = customerName,
                    serviceDescription = serviceDescription,
                    serviceCharge = serviceCharge.toDoubleOrNull() ?: 0.0,
                    partsCost = partsCost.toDoubleOrNull() ?: 0.0
                ) { success, message, errorMessage ->
                    if (success) {
                        onSuccess("Service transaction recorded successfully!")
                        customerName = ""
                        serviceDescription = ""
                        serviceCharge = ""
                        partsCost = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = customerName.isNotBlank() && serviceDescription.isNotBlank() && serviceCharge.isNotBlank()
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Record Service")
        }
    }
}

@Composable
fun ExpenseTransactionForm(
    transactionViewModel: TransactionViewModel,
    onSuccess: (String) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Rent") }

    val expenseCategories = listOf("Rent", "Salary", "Utilities", "Supplies", "Maintenance", "Other")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Expense Transaction",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Category Selection - Fixed grid implementation
        Text("Expense Category", style = MaterialTheme.typography.titleMedium)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(120.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(expenseCategories) { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { category = cat },
                    label = { Text(cat) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Expense Description") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g., Monthly rent payment, Employee salary") }
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*$"))) amount = it },
            label = { Text("Amount (₹)") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("0.00") }
        )

        Button(
            onClick = {
                transactionViewModel.createExpenseTransaction(
                    description = description,
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    category = category
                ) { success, message, errorMessage ->
                    if (success) {
                        onSuccess("Expense recorded successfully!")
                        description = ""
                        amount = ""
                        category = "Rent"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = description.isNotBlank() && amount.isNotBlank()
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Record Expense")
        }
    }
}

@Composable
fun IncomeTransactionForm(
    transactionViewModel: TransactionViewModel,
    onSuccess: (String) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Other") }

    val incomeCategories = listOf("Investment", "Refund", "Commission", "Other")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Income Transaction",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Category Selection
        Text("Income Category", style = MaterialTheme.typography.titleMedium)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(incomeCategories) { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { category = cat },
                    label = { Text(cat) }
                )
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Income Description") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g., Investment return, Commission payment") }
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*$"))) amount = it },
            label = { Text("Amount (₹)") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("0.00") }
        )

        Button(
            onClick = {
                transactionViewModel.createIncomeTransaction(
                    description = description,
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    category = category
                ) { success, message, errorMessage ->
                    if (success) {
                        onSuccess("Income recorded successfully!")
                        description = ""
                        amount = ""
                        category = "Other"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = description.isNotBlank() && amount.isNotBlank()
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Record Income")
        }
    }
}

// Simple Cart Items List (replace with your actual implementation)
@Composable
fun SimpleCartItemsList(
    products: List<com.example.repairstoremanager.data.model.TransactionProduct>,
    onRemoveItem: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products) { product ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(product.name, style = MaterialTheme.typography.titleMedium)
                        Text("Qty: ${product.quantity} × ₹${"%.2f".format(product.price)} = ₹${"%.2f".format(product.total)}")
                    }
                    IconButton(onClick = { onRemoveItem(product.productId) }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove")
                    }
                }
            }
        }
    }
}

// Simple Product Picker Dialog
@Composable
fun SimpleProductPickerDialog(
    stockViewModel: StockViewModel,
    onProductSelected: (Product) -> Unit,
    onDismiss: () -> Unit
) {
    val products by stockViewModel.products.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Product") },
        text = {
            LazyColumn(modifier = Modifier.height(300.dp)) {
                items(products) { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        onClick = { onProductSelected(product) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(product.name, style = MaterialTheme.typography.titleMedium)
                            Text("Stock: ${product.quantity}", style = MaterialTheme.typography.bodySmall)
                            Text("Price: ₹${"%.2f".format(product.sellingPrice)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}