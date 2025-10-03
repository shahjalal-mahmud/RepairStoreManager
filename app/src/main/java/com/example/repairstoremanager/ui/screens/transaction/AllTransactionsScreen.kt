package com.example.repairstoremanager.ui.screens.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repairstoremanager.data.model.Transaction
import com.example.repairstoremanager.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsScreen(
    viewModel: TransactionViewModel = viewModel(),
    onAddTransactionClick: () -> Unit = {}
) {
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddTransactionClick() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("All Transactions") },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Summary section
            TransactionSummaryHeader(transactions)

            // Transactions list
            if (isLoading) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { txn ->
                        TransactionItem(txn)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionSummaryHeader(transactions: List<Transaction>) {
    val totalAmount = transactions.sumOf { it.amount }
    val totalProfit = transactions.sumOf { it.profit }
    val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Total Amount", fontWeight = FontWeight.SemiBold)
            Text(formatter.format(totalAmount), style = MaterialTheme.typography.bodyLarge)
        }
        Column {
            Text("Profit", fontWeight = FontWeight.SemiBold)
            Text(
                formatter.format(totalProfit),
                style = MaterialTheme.typography.bodyLarge,
                color = if (totalProfit >= 0) Color(0xFF2E7D32) else Color.Red
            )
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (transaction.type) {
                "Sale" -> Color(0xFFE3F2FD)
                "Purchase" -> Color(0xFFFFF3E0)
                "Expense" -> Color(0xFFFFEBEE)
                "Income" -> Color(0xFFE8F5E9)
                "Service" -> Color(0xFFEDE7F6)
                else -> Color.White
            }
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(transaction.type, fontWeight = FontWeight.Bold)
                Text(transaction.date, style = MaterialTheme.typography.bodySmall)
            }
            Text(transaction.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatter.format(transaction.amount), fontWeight = FontWeight.Bold)
            }
        }
    }
}