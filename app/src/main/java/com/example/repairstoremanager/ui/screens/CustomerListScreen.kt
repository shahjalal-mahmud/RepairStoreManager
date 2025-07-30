package com.example.repairstoremanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repairstoremanager.viewmodel.CustomerViewModel
import com.example.repairstoremanager.data.model.Customer

@Composable
fun CustomerListScreen(viewModel: CustomerViewModel = viewModel()) {
    val customerList by viewModel.customers.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCustomers()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(customerList) { customer ->
            CustomerCard(customer)
        }
    }
}

@Composable
fun CustomerCard(customer: Customer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("👤 ${customer.customerName}", style = MaterialTheme.typography.titleMedium)
            Text("📱 ${customer.phoneModel} | Problem: ${customer.problem}")
            Text("📞 ${customer.contactNumber}")
            Text("💳 Paid: ${customer.advanced} / Total: ${customer.totalAmount}")
            Text("📦 Accessories: " + listOfNotNull(
                if (customer.battery) "Battery" else null,
                if (customer.sim) "SIM" else null,
                if (customer.memory) "Memory" else null,
                if (customer.simTray) "SIM Tray" else null,
                if (customer.backCover) "Back Cover" else null
            ).joinToString(", "))
        }
    }
}
