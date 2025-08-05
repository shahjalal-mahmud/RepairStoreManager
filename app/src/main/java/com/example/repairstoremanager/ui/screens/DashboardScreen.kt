package com.example.repairstoremanager.ui.screens

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.ui.components.CustomerCard
import com.example.repairstoremanager.viewmodel.CustomerViewModel

@SuppressLint("UnusedBoxWithConstraintsScope")
@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: CustomerViewModel = viewModel()) {
    val scrollState = rememberScrollState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasError by viewModel.hasError.collectAsState(false)

    LaunchedEffect(Unit) {
        viewModel.fetchCustomers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Dashboard", style = MaterialTheme.typography.titleLarge)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                hasError -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Something went wrong.", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { viewModel.fetchCustomers() }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(scrollState)
                    ) {
                        // 🔢 Metrics Row
                        DashboardMetricsRow(viewModel)

                        Spacer(Modifier.height(24.dp))

                        // 📦 Deliveries
                        DashboardDeliverySection("Today’s Deliveries", viewModel.todayDeliveryList, viewModel)

                        Spacer(Modifier.height(16.dp))

                        DashboardDeliverySection("Tomorrow’s Deliveries", viewModel.tomorrowDeliveryList, viewModel)
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DashboardMetricsRow(viewModel: CustomerViewModel) {
    BoxWithConstraints {
        val isCompact = maxWidth < 600.dp
        if (isCompact) {
            // 📱 Small screens - vertical layout
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardMetricCard("Total Customers", viewModel.totalCustomersCount)
                DashboardMetricCard("Today's Invoices", viewModel.todaysInvoicesCount)
                DashboardMetricCard("Pending Devices", viewModel.pendingDevicesCount)
            }
        } else {
            // 💻 Large screens - horizontal layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardMetricCard("Total Customers", viewModel.totalCustomersCount, Modifier.weight(1f))
                DashboardMetricCard("Today's Invoices", viewModel.todaysInvoicesCount, Modifier.weight(1f))
                DashboardMetricCard("Pending Devices", viewModel.pendingDevicesCount, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DashboardMetricCard(title: String, count: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(100.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun DashboardDeliverySection(
    title: String,
    customers: List<Customer>,
    viewModel: CustomerViewModel = viewModel()
) {
    val count = customers.size
    Text(
        text = "$title ($count)",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    if (customers.isEmpty()) {
        Text(
            text = "No devices scheduled.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            customers.forEach { customer ->
                CustomerCard(customer = customer, viewModel = viewModel)
            }
        }
    }
}
