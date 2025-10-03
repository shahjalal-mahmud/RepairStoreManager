package com.example.repairstoremanager.ui.screens.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.data.model.Product
import com.example.repairstoremanager.data.model.PurchaseProduct
import com.example.repairstoremanager.viewmodel.StockViewModel

@Composable
fun PurchaseProductsList(
    products: List<PurchaseProduct>,
    onUpdateQuantity: (String, Int) -> Unit,
    onUpdatePrice: (String, Double, Double) -> Unit,
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(product.name, style = MaterialTheme.typography.titleMedium)
                        IconButton(
                            onClick = { onRemoveItem(product.productId) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Quantity
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Quantity", style = MaterialTheme.typography.labelSmall)
                            OutlinedTextField(
                                value = product.quantity.toString(),
                                onValueChange = {
                                    val newQuantity = it.toIntOrNull() ?: 1
                                    onUpdateQuantity(product.productId, newQuantity)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Purchase Price
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Cost Price", style = MaterialTheme.typography.labelSmall)
                            OutlinedTextField(
                                value = product.purchasePrice.toString(),
                                onValueChange = {
                                    val newPrice = it.toDoubleOrNull() ?: 0.0
                                    onUpdatePrice(product.productId, newPrice, product.sellingPrice)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Selling Price
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Selling Price", style = MaterialTheme.typography.labelSmall)
                            OutlinedTextField(
                                value = product.sellingPrice.toString(),
                                onValueChange = {
                                    val newPrice = it.toDoubleOrNull() ?: 0.0
                                    onUpdatePrice(product.productId, product.purchasePrice, newPrice)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total: ₹${product.totalCost}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

// PurchaseProductPickerDialog.kt
@Composable
fun PurchaseProductPickerDialog(
    stockViewModel: StockViewModel,
    onProductSelected: (Product) -> Unit,
    onDismiss: () -> Unit
) {
    val products by stockViewModel.products.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Product for Purchase") },
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
                            Text("Current Stock: ${product.quantity}", style = MaterialTheme.typography.bodySmall)
                            Text("Cost: ₹${product.buyingPrice}", style = MaterialTheme.typography.bodySmall)
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