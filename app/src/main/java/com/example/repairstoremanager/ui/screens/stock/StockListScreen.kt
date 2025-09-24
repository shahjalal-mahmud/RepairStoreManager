package com.example.repairstoremanager.ui.screens.stock

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.repairstoremanager.data.model.Product
import com.example.repairstoremanager.viewmodel.StockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockListScreen(
    navController: NavController,
    viewModel: StockViewModel
) {
    val products by viewModel.products.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showSearchBar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchProducts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearchBar) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.setSearchQuery(it) },
                            onSearch = { viewModel.searchProducts(it) },
                            onActiveChange = { showSearchBar = it }
                        )
                    } else {
                        Text("Stock Management", fontWeight = FontWeight.SemiBold)
                    }
                },
                actions = {
                    if (!showSearchBar) {
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_product") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = error ?: "Error occurred")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.fetchProducts() }) {
                            Text("Retry")
                        }
                    }
                }
                products.isEmpty() -> {
                    Text(
                        text = "No products found.\nTap + to add your first product.",
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(products) { product ->
                            ProductCard(product = product, onItemClick = {
                                navController.navigate("edit_product/${product.id}")
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit
) {
    TextField(
        value = query,
        onValueChange = {
            onQueryChange(it)
            onSearch(it)
        },
        placeholder = { Text("Search products...") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = {
                    onQueryChange("")
                    onSearch("")
                    onActiveChange(false)
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        }
    )
}

@Composable
fun ProductCard(product: Product, onItemClick: () -> Unit) {
    val backgroundColor = if (product.quantity <= product.alertQuantity && product.alertQuantity > 0) {
        if (isSystemInDarkTheme()) Color(0x33000000) else Color(0x1AFF5252)
    } else if (product.hasWarranty) {
        if (isSystemInDarkTheme()) Color(0x1A4CAF50) else Color(0x1A66BB6A)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textColor = if (backgroundColor == MaterialTheme.colorScheme.surface) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        onClick = onItemClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ Product Image
            if (product.imageUrl.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(product.imageUrl),
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
            }

            // ✅ Product Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )

                    if (product.type.isNotBlank()) {
                        Text(
                            product.type,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Category + Model
                if (product.category.isNotBlank() || product.model.isNotBlank()) {
                    Text(
                        "${product.category}${if (product.subCategory.isNotBlank()) " • ${product.subCategory}" else ""}${if (product.model.isNotBlank()) " • ${product.model}" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(6.dp))

                // Quantity & Price
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Qty: ${product.quantity}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (product.quantity <= product.alertQuantity && product.alertQuantity > 0) {
                            if (isSystemInDarkTheme()) Color(0xFFEF5350) else Color(0xFFD32F2F)
                        } else {
                            textColor
                        }
                    )
                    Text(
                        "$${product.sellingPrice}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}