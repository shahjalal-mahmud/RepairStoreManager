package com.example.repairstoremanager.ui.stock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.viewmodel.TransactionViewModel

@Composable
fun TransactionScreen(
    onClose: () -> Unit,
    viewModel: TransactionViewModel
) {
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchTodayTransactions()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Today's Transactions", style = MaterialTheme.typography.headlineSmall)

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn {
                items(transactions) { txn ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        elevation = CardDefaults.cardElevation()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Type: ${txn.type}")
                            Text("Description: ${txn.description}")
                            Text("Amount: ৳${txn.amount}")
                            Text("Payment: ${txn.paymentType}")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary
            val summary = viewModel.calculateSummary()
            Text("Summary", style = MaterialTheme.typography.titleMedium)
            summary.forEach { (type, total) ->
                Text("$type: ৳$total")
            }
            Text("Total Income: ৳${viewModel.getTotalIncome()}")
        }
    }
}