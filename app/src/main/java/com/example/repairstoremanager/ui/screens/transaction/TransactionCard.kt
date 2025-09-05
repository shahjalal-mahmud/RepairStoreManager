package com.example.repairstoremanager.ui.screens.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TransactionList(transactions: List<com.example.repairstoremanager.data.model.Transaction>, showAll: Boolean) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(transactions) { transaction ->
            TransactionCard(transaction = transaction, showDate = showAll)
        }
    }
}
@Composable
fun TransactionCard(transaction: com.example.repairstoremanager.data.model.Transaction, showDate: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(transaction.invoiceNumber, fontWeight = FontWeight.Bold)
                    if (showDate) {
                        Text(
                            transaction.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                Text("₹${"%.2f".format(transaction.amount)}", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Customer: ${transaction.customerName}")
            Text("Payment: ${transaction.paymentType}")

            if (transaction.products.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Items:", style = MaterialTheme.typography.labelMedium)
                transaction.products.forEach { product ->
                    Text("  • ${product.name} x${product.quantity} - ₹${"%.2f".format(product.total)}")
                }
            }

            if (!showDate) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    transaction.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}