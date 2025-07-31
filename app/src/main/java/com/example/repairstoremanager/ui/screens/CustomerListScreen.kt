package com.example.repairstoremanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repairstoremanager.viewmodel.CustomerViewModel
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.ui.components.CustomerSearchAndFilterBar
import com.example.repairstoremanager.ui.components.CustomerSearchFilterSortBar
import com.example.repairstoremanager.ui.components.PatternLockCanvas

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
                "Total Amount (High → Low)" -> list.sortedByDescending { it.totalAmount }
                "Total Amount (Low → High)" -> list.sortedBy { it.totalAmount }
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
    var selectedStatus by remember { mutableStateOf(customer.status) }

    val statusOptions = listOf("Pending", "Repaired", "Delivered", "Cancelled")
    val statusColor = statusToColor(selectedStatus)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Customer name + status button row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "👤 ${customer.customerName}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                StatusDropdown(
                    selectedStatus = selectedStatus,
                    options = statusOptions,
                    onStatusChange = { newStatus ->
                        selectedStatus = newStatus
                        viewModel.updateCustomerStatus(customer.id, newStatus)
                    },
                    statusColor = statusColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("📱 ${customer.phoneModel} | Problem: ${customer.problem}")
            Text("📞 ${customer.contactNumber}")
            Text("💳 Paid: ${customer.advanced} / Total: ${customer.totalAmount}")

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "🔒 Hide Security Info" else "🔓 Show Security Info")
            }

            if (expanded) {
                if (customer.securityType == "Password") {
                    Text("🔑 Password: ${customer.phonePassword}")
                } else {
                    Text("🔐 Pattern:")
                    PatternLockCanvas(
                        pattern = customer.pattern,
                        isInteractive = false,
                        isPreview = true,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DropdownMenuBox(
    selectedStatus: String,
    options: List<String>,
    onStatusChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text("📋 $selectedStatus ⬇️")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
