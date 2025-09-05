package com.example.repairstoremanager.ui.screens.transaction

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    transactionViewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    val transactions by transactionViewModel.transactions.collectAsState()
    val isLoading by transactionViewModel.isLoading.collectAsState()
    val selectedDate by transactionViewModel.selectedDate.collectAsState()
    val showAllTransactions by transactionViewModel.showAllTransactions.collectAsState()
    val transactionSummary by transactionViewModel.transactionSummary.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions Records") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        transactionViewModel.toggleTransactionView(!showAllTransactions)
                    }) {
                        Icon(
                            if (showAllTransactions) Icons.Default.Today else Icons.Default.History,
                            contentDescription = if (showAllTransactions) "Show Today" else "Show All"
                        )
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
            if (!showAllTransactions) {
                // Date Selector and Summary for daily view
                TransactionHeader(
                    selectedDate = selectedDate,
                    onDateSelect = { showDatePicker = true },
                    showAll = showAllTransactions
                )

                // Transaction Summary
                TransactionSummaryCard(summary = transactionSummary)

                Spacer(modifier = Modifier.height(16.dp))

                // ADD THIS SECTION TO SHOW TRANSACTIONS HEADER
                Text(
                    "Transactions for ${transactionViewModel.formatDateForDisplay(selectedDate)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else {
                // Header for all transactions view
                Text(
                    "All Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (showAllTransactions) "No transactions found"
                        else "No transactions found for ${transactionViewModel.formatDateForDisplay(selectedDate)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                // THIS WILL NOW SHOW THE TRANSACTIONS AFTER THE SUMMARY
                TransactionList(transactions = transactions, showAll = showAllTransactions)
            }
        }

        if (showDatePicker && !showAllTransactions) {
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
fun TransactionSummaryCard(summary: com.example.repairstoremanager.data.repository.TransactionRepository.TransactionSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Daily Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Summary items
            SummaryItem("Total Transactions", summary.totalTransactions.toString())
            SummaryItem("Total Sales", "₹${summary.totalSales.format(2)}")
            SummaryItem("Total Services", "₹${summary.totalServices.format(2)}")
            SummaryItem("Total Expenses", "₹${summary.totalExpenses.format(2)}")

            if (summary.productsSold.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Products Sold:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                summary.productsSold.forEach { (product, quantity) ->
                    Text(
                        "• $product: $quantity",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}