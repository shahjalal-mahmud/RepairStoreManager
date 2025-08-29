package com.example.repairstoremanager.ui.stock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.repairstoremanager.data.model.Product
import com.example.repairstoremanager.viewmodel.StockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    navController: NavController,
    viewModel: StockViewModel,
    productId: String? = null
) {
    var type by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var subCategory by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var buyingPrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }

    var submitting by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // If editing, load product data
    if (productId != null) {
        LaunchedEffect(productId) {
            viewModel.getProductById(productId) { product ->
                product?.let {
                    name = it.name
                    type = it.type
                    category = it.category
                    subCategory = it.subCategory
                    buyingPrice = it.buyingPrice.toString()
                    sellingPrice = it.sellingPrice.toString()
                    cost = it.cost.toString()
                    quantity = it.quantity.toString()
                    details = it.details
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productId == null) "Add Product" else "Edit Product") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                label = { Text("Type *") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category *") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = subCategory,
                onValueChange = { subCategory = it },
                label = { Text("Sub Category") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = buyingPrice,
                onValueChange = { buyingPrice = it },
                label = { Text("Buying Price *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = sellingPrice,
                onValueChange = { sellingPrice = it },
                label = { Text("Selling Price *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = cost,
                onValueChange = { cost = it },
                label = { Text("Cost (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = details,
                onValueChange = { details = it },
                label = { Text("Details") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isBlank() || type.isBlank() || category.isBlank() ||
                        sellingPrice.isBlank() || buyingPrice.isBlank() || quantity.isBlank()) {
                        // Show error message
                        return@Button
                    }

                    submitting = true
                    val product = Product(
                        name = name.trim(),
                        type = type.trim(),
                        category = category.trim(),
                        subCategory = subCategory.trim(),
                        buyingPrice = buyingPrice.toDoubleOrNull() ?: 0.0,
                        sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                        cost = cost.toDoubleOrNull() ?: 0.0,
                        quantity = quantity.toLongOrNull() ?: 0L,
                        details = details.trim()
                    )

                    if (productId == null) {
                        viewModel.addProduct(
                            product = product,
                            onSuccess = {
                                submitting = false
                                navController.popBackStack()
                            },
                            onError = { msg ->
                                submitting = false
                                // Show error
                            }
                        )
                    } else {
                        // Update existing product
                        val updatedProduct = product.copy(id = productId)
                        viewModel.updateProduct(
                            product = updatedProduct,
                            onSuccess = {
                                submitting = false
                                navController.popBackStack()
                            },
                            onError = { msg ->
                                submitting = false
                                // Show error
                            }
                        )
                    }
                },
                enabled = !submitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (submitting) "Saving..." else "Save")
            }
        }
    }
}