package com.example.repairstoremanager.ui.screens.transaction

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Today
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

    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
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
                    totalSales = transactionViewModel.getDailySalesTotal(),
                    transactionCount = transactionViewModel.getDailyTransactionCount(),
                    onDateSelect = { showDatePicker = true },
                    showAll = showAllTransactions
                )
                Spacer(modifier = Modifier.height(16.dp))
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
                        if (showAllTransactions) "No transactions found" else "No transactions found for $selectedDate",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
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