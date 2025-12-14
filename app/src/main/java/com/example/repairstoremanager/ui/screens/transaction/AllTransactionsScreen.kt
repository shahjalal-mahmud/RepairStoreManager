package com.example.repairstoremanager.ui.screens.transaction

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repairstoremanager.data.model.Transaction
import com.example.repairstoremanager.ui.components.common.DateRangePicker
import com.example.repairstoremanager.ui.components.customer.invoice.TransactionPrintBottomSheet
import com.example.repairstoremanager.viewmodel.StockViewModel
import com.example.repairstoremanager.viewmodel.StoreViewModel
import com.example.repairstoremanager.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsScreen(
    viewModel: TransactionViewModel = viewModel(),
    storeViewModel: StoreViewModel,
    stockViewModel: StockViewModel,
    onAddTransactionClick: () -> Unit = {}
) {
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val dateRange by viewModel.dateRange.collectAsState()
    val isFiltering by viewModel.isFiltering.collectAsState()

    // Load all transactions when screen is first shown
    LaunchedEffect(Unit) {
        if (!isFiltering) {
            viewModel.loadAllTransactions()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddTransactionClick() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("All Transactions")
                        if (isFiltering) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                " (Filtered)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Date Range Filter
            DateRangePicker(
                dateRange = dateRange,
                onDateRangeSelected = { newDateRange ->
                    viewModel.updateDateRange(newDateRange)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = "Filter by Date Range"
            )

            // Summary section
            TransactionSummaryHeader(transactions, isFiltering)

            // Transactions list
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (isFiltering) "Filtering transactions..." else "Loading transactions...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (transactions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "No transactions found",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        if (isFiltering) {
                                            "Try adjusting your date range filter"
                                        } else {
                                            "Add your first transaction using the + button"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (isFiltering) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { viewModel.clearDateRangeFilter() }
                                        ) {
                                            Text("Clear Filter")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            // Transaction count header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${transactions.size} transactions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (isFiltering) {
                                    TextButton(
                                        onClick = { viewModel.clearDateRangeFilter() }
                                    ) {
                                        Text("Clear Filter")
                                    }
                                }
                            }
                        }

                        items(transactions) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                storeViewModel = storeViewModel,
                                stockViewModel = stockViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionSummaryHeader(transactions: List<Transaction>, isFiltered: Boolean = false) {
    val totalAmount = transactions.sumOf { it.totalAmount }
    val totalProfit = transactions.sumOf { it.totalProfit }
    val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (isFiltered) {
                Text(
                    "Filtered Results",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
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
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    storeViewModel: StoreViewModel,
    stockViewModel: StockViewModel
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    var showPrintSheet by remember { mutableStateOf(false) }
    val storeInfo = storeViewModel.storeInfo

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (transaction.type) {
                "Sale" -> Color(0xFFE3F2FD)
                "Purchase" -> Color(0xFFFFF3E0)
                "Expense" -> Color(0xFFFFEBEE)
                "Income" -> Color(0xFFE8F5E9)
                "Service" -> Color(0xFFEDE7F6)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            /** Header Row **/
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = transaction.invoiceNumber,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = transaction.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = { showPrintSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "Print Invoice"
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            /** Customer Name **/
            Text(
                text = transaction.customerName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            /** Amount + Profit **/
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatter.format(transaction.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Profit: ${formatter.format(transaction.totalProfit)}",
                    color = if (transaction.totalProfit >= 0)
                        Color(0xFF2E7D32)
                    else
                        Color(0xFFC62828),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    /** Print Bottom Sheet **/
    if (showPrintSheet) {
        TransactionPrintBottomSheet(
            transaction = transaction,
            storeInfo = storeInfo,
            stockViewModel = stockViewModel,
            onDismiss = { showPrintSheet = false }
        )
    }
}
