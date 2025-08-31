package com.example.repairstoremanager.ui.stock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.data.repository.CustomerRepository
import com.example.repairstoremanager.viewmodel.CustomerViewModel
import com.example.repairstoremanager.viewmodel.StockViewModel
import com.example.repairstoremanager.viewmodel.TransactionViewModel

@Composable
fun AddTransactionScreen(
    transactionViewModel: TransactionViewModel,
    customerViewModel: CustomerViewModel,
    stockViewModel: StockViewModel
) {
    val invoiceNumber by transactionViewModel.currentInvoiceNumber.collectAsState()
    val customer by transactionViewModel.selectedCustomer.collectAsState()
    val products by transactionViewModel.products.collectAsState()

    var inputInvoice by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf("Cash") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Add Transaction", style = MaterialTheme.typography.headlineSmall)

        // Invoice box
        OutlinedTextField(
            value = inputInvoice,
            onValueChange = { inputInvoice = it },
            label = { Text("Invoice Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = {
                transactionViewModel.searchCustomerByInvoice(inputInvoice, CustomerRepository())
            }) {
                Text("Search Invoice")
            }
            Button(onClick = {
                transactionViewModel.fetchNextInvoiceNumber()
                inputInvoice = invoiceNumber ?: ""
            }) {
                Text("New Invoice")
            }
        }

        Spacer(Modifier.height(12.dp))

        // Show customer payment info
        customer?.let {
            Card(Modifier.fillMaxWidth().padding(4.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Customer: ${it.customerName}")
                    Text("Total: ৳${it.totalAmount}")
                    Text("Advanced: ৳${it.advanced}")
                    val due = (it.totalAmount.toDoubleOrNull() ?: 0.0) -
                            (it.advanced.toDoubleOrNull() ?: 0.0)
                    Text("Due: ৳$due")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Add products
        Text("Add Products", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(products) { p ->
                Row(
                    Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("${p.name} x${p.quantity}")
                        Text("৳${p.price * p.quantity}")
                    }
                    IconButton(onClick = { transactionViewModel.removeProduct(p.productId) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                    }
                }
            }
        }

        Button(onClick = {
            // Example: Show product picker dialog (from stockViewModel.products)
        }) {
            Text("Add Product")
        }

        Spacer(Modifier.height(16.dp))

        // Payment type
        DropdownMenuDemo(paymentType) { paymentType = it }

        Spacer(Modifier.height(16.dp))

        // Save transaction
        Button(
            onClick = {
                transactionViewModel.saveTransaction(customer, paymentType) { success ->
                    if (success) {
//                        Toast.makeText(LocalContext.current, "Transaction Saved", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Transaction")
        }
    }
}

@Composable
fun DropdownMenuDemo(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) { Text(selected) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf("Cash", "Card", "Mobile Banking").forEach {
                DropdownMenuItem(onClick = {
                    onSelect(it)
                    expanded = false
                }, text = { Text(it) })
            }
        }
    }
}
