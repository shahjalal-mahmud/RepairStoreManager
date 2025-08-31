package com.example.repairstoremanager.ui.screens.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.repairstoremanager.data.model.Transaction
import com.example.repairstoremanager.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    transactionViewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    val transactions by transactionViewModel.transactions.collectAsState()
    val isLoading by transactionViewModel.isLoading.collectAsState()
    val selectedDate by transactionViewModel.selectedDate.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Date Selector and Summary
            TransactionHeader(
                selectedDate = selectedDate,
                totalSales = transactionViewModel.getDailySalesTotal(),
                transactionCount = transactionViewModel.getDailyTransactionCount(),
                onDateSelect = { showDatePicker = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions found for $selectedDate", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                TransactionList(transactions = transactions)
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDateSelected = { date ->
                    transactionViewModel.fetchTransactionsByDate(date)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@Composable
fun TransactionHeader(
    selectedDate: String,
    totalSales: Double,
    transactionCount: Int,
    onDateSelect: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Date Selector
            Button(
                onClick = onDateSelect,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                Spacer(modifier = Modifier.width(8.dp))
                Text(selectedDate)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                SummaryItem("Transactions", transactionCount.toString())
                SummaryItem("Total Sales", "$${"%.2f".format(totalSales)}")
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun TransactionList(transactions: List<Transaction>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(transactions) { transaction ->
            TransactionCard(transaction = transaction)
        }
    }
}

@Composable
fun TransactionCard(transaction: Transaction) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(transaction.invoiceNumber, fontWeight = FontWeight.Bold)
                Text("$${"%.2f".format(transaction.amount)}", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Customer: ${transaction.customerName}")
            Text("Payment: ${transaction.paymentType}")

            if (transaction.products.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Items:", style = MaterialTheme.typography.labelMedium)
                transaction.products.forEach { product ->
                    Text("  • ${product.name} x${product.quantity} - $${product.total}")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                transaction.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun DatePickerDialog(onDateSelected: (String) -> Unit, onDismiss: () -> Unit) {
    // Simplified date picker - in real app, use proper date picker
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Date", style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                // Simple date buttons - in real app, use proper calendar
                val dates = listOf(
                    "Today" to getFormattedDate(0),
                    "Yesterday" to getFormattedDate(-1),
                    "2 days ago" to getFormattedDate(-2)
                )

                dates.forEach { (label, date) ->
                    Button(
                        onClick = {
                            onDateSelected(date)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("$label ($date)")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

private fun getFormattedDate(daysOffset: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, daysOffset)
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
}