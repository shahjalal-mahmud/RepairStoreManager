package com.example.repairstoremanager.ui.stock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.repairstoremanager.data.model.Product
import com.example.repairstoremanager.viewmodel.StockViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    viewModel: StockViewModel
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

    // Use a coroutine scope for snackbar operations
    val scope = rememberCoroutineScope()

    fun parseDouble(s: String) = s.toDoubleOrNull() ?: 0.0
    fun parseLong(s: String) = s.toLongOrNull() ?: 0L

    // Handle snackbar messages as state
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Show snackbar when errorMessage changes
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            errorMessage = null // Reset after showing
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Add Product") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name *") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type *") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category *") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = subCategory, onValueChange = { subCategory = it }, label = { Text("Sub Category") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = buyingPrice, onValueChange = { buyingPrice = it },
                label = { Text("Buying Price *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = sellingPrice, onValueChange = { sellingPrice = it },
                label = { Text("Selling Price *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = cost, onValueChange = { cost = it },
                label = { Text("Cost (optional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = quantity, onValueChange = { quantity = it },
                label = { Text("Quantity *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = details, onValueChange = { details = it }, label = { Text("Details") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isBlank() || type.isBlank() || category.isBlank() || sellingPrice.isBlank() || buyingPrice.isBlank() || quantity.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please fill all required * fields")
                        }
                        return@Button
                    }
                    submitting = true
                    val product = Product(
                        name = name.trim(),
                        type = type.trim(),
                        category = category.trim(),
                        subCategory = subCategory.trim(),
                        buyingPrice = parseDouble(buyingPrice),
                        sellingPrice = parseDouble(sellingPrice),
                        cost = parseDouble(cost),
                        quantity = parseLong(quantity),
                        details = details.trim()
                    )
                    viewModel.addProduct(
                        product = product,
                        onSuccess = {
                            submitting = false
                            navController.popBackStack()
                        },
                        onError = { msg ->
                            submitting = false
                            errorMessage = msg // Set the error message state
                        }
                    )
                },
                enabled = !submitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (submitting) "Saving..." else "Save")
            }
        }
    }
}