package com.example.repairstoremanager.ui.screens.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
            Row(
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
@Composable
fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Select Date",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Simple date buttons - in real app, use proper calendar
                val dates = listOf(
                    "Today" to getFormattedDate(0),
                    "Yesterday" to getFormattedDate(-1),
                    "2 days ago" to getFormattedDate(-2),
                    "3 days ago" to getFormattedDate(-3),
                    "4 days ago" to getFormattedDate(-4),
                    "5 days ago" to getFormattedDate(-5),
                    "6 days ago" to getFormattedDate(-6)
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

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Cancel")
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

// Extension function for formatting doubles
fun Double.format(decimals: Int): String {
    return "%.${decimals}f".format(this)
}