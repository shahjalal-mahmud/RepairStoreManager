package com.example.repairstoremanager.ui.screens.stock

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.repairstoremanager.data.model.Product
import com.example.repairstoremanager.ui.components.stock.CategorySection
import com.example.repairstoremanager.ui.components.stock.DropdownMenuBox
import com.example.repairstoremanager.ui.components.stock.PricingSection
import com.example.repairstoremanager.ui.components.stock.ProductTypeDropdown
import com.example.repairstoremanager.ui.components.stock.QuantitySection
import com.example.repairstoremanager.ui.components.stock.SupplierSection
import com.example.repairstoremanager.ui.components.stock.WarrantySection
import com.example.repairstoremanager.util.MediaStorageHelper
import com.example.repairstoremanager.viewmodel.StockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    viewModel: StockViewModel
) {
    val context = LocalContext.current

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

    var hasGuarantee by remember { mutableStateOf(false) }
    var guaranteeDuration by remember { mutableStateOf("") }
    var guaranteeType by remember { mutableStateOf("month") }

    var submitting by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingAction?.invoke()
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkPermissionAndExecute(action: () -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            action()
        } else {
            pendingAction = action
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ---- Image Pickers ----
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Copy image into app storage
            val savedUri = MediaStorageHelper.saveImageFromUri(context, it, "product_${System.currentTimeMillis()}.jpg")
            savedUri?.let { newUri ->
                imageUrl = newUri.toString() // use permanent Uri
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { imageUrl = it.toString() }
        }
    }

    fun pickFromGallery() {
        galleryLauncher.launch("image/*")
    }

    fun takePhoto() {
        checkPermissionAndExecute {
            val uri = MediaStorageHelper.createImageUri(context, "product")
            if (uri != null) {
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(context, "Failed to create image file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---- Screen UI ----
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
                        painter = rememberAsyncImagePainter(Uri.parse(imageUrl)),
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

                // Mini FAB → choose option
                DropdownMenuBox(
                    onGalleryClick = { pickFromGallery() },
                    onCameraClick = { takePhoto() }
                )
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

            // Guarantee Section
            WarrantySection(
                hasWarranty = hasGuarantee,
                warrantyDuration = guaranteeDuration,
                warrantyType = guaranteeType,
                onWarrantyToggle = { hasGuarantee = it },
                onWarrantyDurationChange = { guaranteeDuration = it },
                onWarrantyTypeChange = { guaranteeType = it },
                title = "Guarantee"
            )

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
                    if (productName.isBlank() || quantity.isBlank()) return@Button

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
                        imageUrl = imageUrl.trim(), // Save as local file URI
                        hasWarranty = hasWarranty,
                        warrantyDuration = warrantyDuration.trim(),
                        warrantyType = warrantyType.trim(),
                        hasGuarantee = hasGuarantee,
                        guaranteeDuration = guaranteeDuration.trim(),
                        guaranteeType = guaranteeType.trim()
                    )

                    viewModel.addProduct(
                        product = product,
                        onSuccess = {
                            submitting = false
                            navController.popBackStack()
                        },
                        onError = { _ -> submitting = false }
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
