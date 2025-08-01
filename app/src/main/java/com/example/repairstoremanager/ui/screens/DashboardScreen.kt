package com.example.repairstoremanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.ui.components.CustomerCard
import com.example.repairstoremanager.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: CustomerViewModel = viewModel()) {
    val scrollState = rememberScrollState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCustomers()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dashboard", style = MaterialTheme.typography.headlineSmall) })
        }
    ) { padding ->
        if (isLoading) {
            // 👇 Centered loading screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                // Metrics Cards
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

                Spacer(Modifier.height(24.dp))

                // Today’s Deliveries
                DashboardDeliverySection("Today’s Deliveries", viewModel.todayDeliveryList, viewModel)

                Spacer(Modifier.height(16.dp))

                // Tomorrow’s Deliveries
                DashboardDeliverySection("Tomorrow’s Deliveries", viewModel.tomorrowDeliveryList, viewModel)
            }
        }
    }
}

@Composable
fun DashboardMetricCard(title: String, count: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.labelMedium)
            Text(text = count.toString(), style = MaterialTheme.typography.headlineSmall)
        }
    }
}

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
        Text("No devices scheduled.", style = MaterialTheme.typography.bodyMedium)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            customers.forEach { customer ->
                CustomerCard(customer = customer, viewModel = viewModel)
            }
        }
    }
}