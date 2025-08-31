package com.example.repairstoremanager.ui.stock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
        Color(0xFFFFEBEE) // Light red for low stock
    } else if (product.hasWarranty) {
        Color(0xFFE8F5E8) // Light green for products with warranty
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        onClick = onItemClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header with name and type
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
                    modifier = Modifier.weight(1f)
                )

                Text(
                    product.type,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Category and model
            if (product.category.isNotBlank() || product.model.isNotBlank()) {
                Text(
                    "${product.category}${if (product.subCategory.isNotBlank()) " • ${product.subCategory}" else ""}${if (product.model.isNotBlank()) " • ${product.model}" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
            }

            // Warranty information
            if (product.hasWarranty) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            "Warranty: ${product.getWarrantyDisplay()}",
                            color = Color(0xFF2E7D32),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFFC8E6C9)
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Stock and pricing info
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity with alert indicator
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                "Qty: ${product.quantity}",
                                color = if (product.quantity <= product.alertQuantity && product.alertQuantity > 0) {
                                    Color(0xFFD32F2F)
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    )

                    if (product.quantity <= product.alertQuantity && product.alertQuantity > 0) {
                        Badge(
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }
                }

                Text(
                    "$${product.sellingPrice}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Supplier info if available
            if (product.supplier.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Supplier: ${product.supplier}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Product details if available
            if (product.details.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    product.details,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}