package com.example.repairstoremanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.ui.components.AccessoriesBadges
import com.example.repairstoremanager.ui.components.CustomerSearchFilterSortBar
import com.example.repairstoremanager.ui.components.PatternLockCanvas
import com.example.repairstoremanager.viewmodel.CustomerViewModel

@Composable
fun CustomerListScreen(viewModel: CustomerViewModel = viewModel()) {
    val customerList by viewModel.customers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedSort by remember { mutableStateOf("None") }

    // 1️⃣ Filtered and sorted list
    val filteredList = customerList
        .filter {
            (searchQuery.isBlank() || it.customerName.contains(searchQuery, true)
                    || it.phoneModel.contains(searchQuery, true)
                    || it.contactNumber.contains(searchQuery, true))
        }
        .filter {
            selectedFilter == "All" || it.status == selectedFilter
        }
        .let { list ->
            when (selectedSort) {
                "Name (A–Z)" -> list.sortedBy { it.customerName }
                "Total Amount (High → Low)" -> list.sortedByDescending { it.totalAmount.toDoubleOrNull() ?: 0.0 }
                "Total Amount (Low → High)" -> list.sortedBy { it.totalAmount.toDoubleOrNull() ?: 0.0 }
                "Newest First" -> list.sortedByDescending { it.createdAt } // Ensure `createdAt` exists
                "Oldest First" -> list.sortedBy { it.createdAt }
                else -> list
            }
        }

    LaunchedEffect(Unit) {
        viewModel.fetchCustomers()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        CustomerSearchFilterSortBar(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            selectedFilter = selectedFilter,
            onFilterChange = { selectedFilter = it },
            selectedSort = selectedSort,
            onSortChange = { selectedSort = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredList) { customer ->
                CustomerCard(customer = customer, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CustomerCard(customer: Customer, viewModel: CustomerViewModel = viewModel()) {
    var expanded by remember { mutableStateOf(false) }
    val selectedStatus = customer.status
    val statusOptions = listOf("Pending", "Repaired", "Delivered", "Cancelled")
    val statusColor = statusToColor(selectedStatus)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header: Name and Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "👤 ${customer.customerName}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                StatusDropdown(
                    selectedStatus = selectedStatus,
                    options = statusOptions,
                    onStatusChange = { newStatus ->
                        viewModel.updateCustomerStatus(customer.id, newStatus)
                    },
                    statusColor = statusColor
                )
            }

            Spacer(Modifier.height(8.dp))

            // Info Section
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("📱 ${customer.phoneModel}", style = MaterialTheme.typography.bodyMedium)
                Text("🛠️ Problem: ${customer.problem}", style = MaterialTheme.typography.bodySmall)
                Text("📞 Contact: ${customer.contactNumber}", style = MaterialTheme.typography.bodySmall)
                Text("💳 Paid: ${customer.advanced} / Total: ${customer.totalAmount}", style = MaterialTheme.typography.bodySmall)

                if (customer.status == "Pending" || customer.status == "Repaired") {
                    Text("📦 Delivery Date: ${customer.deliveryDate}", style = MaterialTheme.typography.bodySmall)
                }

                Text("🕓 Created: ${customer.date}", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(8.dp))

            AccessoriesBadges(
                battery = customer.battery,
                sim = customer.sim,
                memory = customer.memory,
                simTray = customer.simTray,
                backCover = customer.backCover,
                deadPermission = customer.deadPermission
            )

            // Security Info Toggle
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "🔒 Hide Security Info" else "🔓 Show Security Info")
            }

            if (expanded) {
                Spacer(Modifier.height(4.dp))
                if (customer.securityType == "Password") {
                    Text("🔑 Password: ${customer.phonePassword}", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("🔐 Pattern:")
                    PatternLockCanvas(
                        pattern = customer.pattern,
                        isInteractive = false,
                        isPreview = true,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusDropdown(
    selectedStatus: String,
    options: List<String>,
    onStatusChange: (String) -> Unit,
    statusColor: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.textButtonColors(containerColor = statusColor),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.defaultMinSize(minHeight = 32.dp)
        ) {
            Text(
                text = selectedStatus,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status) },
                    onClick = {
                        onStatusChange(status)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun statusToColor(status: String): Color {
    return when (status) {
        "Pending" -> MaterialTheme.colorScheme.outline
        "Repaired" -> MaterialTheme.colorScheme.primary
        "Delivered" -> MaterialTheme.colorScheme.primary.copy(green = 0.8f) // nice green
        "Cancelled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }
}
