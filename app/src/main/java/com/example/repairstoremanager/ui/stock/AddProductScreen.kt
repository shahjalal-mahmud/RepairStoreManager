package com.example.repairstoremanager.ui.stock

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.repairstoremanager.data.model.Product
import com.example.repairstoremanager.ui.components.stock.CategorySection
import com.example.repairstoremanager.ui.components.stock.PricingSection
import com.example.repairstoremanager.ui.components.stock.ProductTypeDropdown
import com.example.repairstoremanager.ui.components.stock.QuantitySection
import com.example.repairstoremanager.ui.components.stock.SupplierSection
import com.example.repairstoremanager.ui.components.stock.WarrantySection
import com.example.repairstoremanager.viewmodel.StockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    viewModel: StockViewModel
) {
    var productType by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var subCategory by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var alertQuantity by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var buyingPrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var hasWarranty by remember { mutableStateOf(false) }
    var warrantyDuration by remember { mutableStateOf("") }
    var warrantyType by remember { mutableStateOf("month") }

    var submitting by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Product", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Product Image
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                if (imageUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Product Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Add Photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                FloatingActionButton(
                    onClick = { /* TODO: Implement image picker */ },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Add Photo",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Product Type Dropdown
            ProductTypeDropdown(
                selectedType = productType,
                onTypeSelected = { productType = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Product Name
            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Product Name *") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category and Subcategory
            CategorySection(
                category = category,
                subCategory = subCategory,
                onCategorySelected = { category = it },
                onSubCategorySelected = { subCategory = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Model
            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Model") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quantity Section
            QuantitySection(
                quantity = quantity,
                alertQuantity = alertQuantity,
                onQuantityChange = { quantity = it },
                onAlertQuantityChange = { alertQuantity = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Supplier and Unit
            SupplierSection(
                supplier = supplier,
                unit = unit,
                onSupplierChange = { supplier = it },
                onUnitChange = { unit = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pricing Section
            PricingSection(
                cost = cost,
                buyingPrice = buyingPrice,
                sellingPrice = sellingPrice,
                onCostChange = { cost = it },
                onBuyingPriceChange = { buyingPrice = it },
                onSellingPriceChange = { sellingPrice = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Warranty Section
            WarrantySection(
                hasWarranty = hasWarranty,
                warrantyDuration = warrantyDuration,
                warrantyType = warrantyType,
                onWarrantyToggle = { hasWarranty = it },
                onWarrantyDurationChange = { warrantyDuration = it },
                onWarrantyTypeChange = { warrantyType = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Product Details
            OutlinedTextField(
                value = details,
                onValueChange = { details = it },
                label = { Text("Product Details") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = MaterialTheme.shapes.medium,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    if (productName.isBlank() || quantity.isBlank()) {
                        // Show error message
                        return@Button
                    }

                    submitting = true
                    val product = Product(
                        name = productName.trim(),
                        type = productType.trim(),
                        category = category.trim(),
                        subCategory = subCategory.trim(),
                        model = model.trim(),
                        cost = cost.toDoubleOrNull() ?: 0.0,
                        buyingPrice = buyingPrice.toDoubleOrNull() ?: 0.0,
                        sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                        quantity = quantity.toLongOrNull() ?: 0L,
                        alertQuantity = alertQuantity.toLongOrNull() ?: 0L,
                        supplier = supplier.trim(),
                        unit = unit.trim(),
                        details = details.trim(),
                        imageUrl = imageUrl.trim(),
                        hasWarranty = hasWarranty,
                        warrantyDuration = warrantyDuration.trim(),
                        warrantyType = warrantyType.trim()
                    )

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
                },
                enabled = !submitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    if (submitting) "Adding Product..." else "Add Product",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}