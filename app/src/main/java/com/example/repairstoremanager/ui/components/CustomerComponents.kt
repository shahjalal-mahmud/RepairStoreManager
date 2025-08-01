package com.example.repairstoremanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.viewmodel.CustomerViewModel

@Composable
fun CustomerCard(customer: Customer, viewModel: CustomerViewModel) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "\uD83D\uDC64 ${customer.customerName}",
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

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("\uD83D\uDCF1 ${customer.phoneModel}", style = MaterialTheme.typography.bodyMedium)
                Text("\uD83D\uDEE0️ Problem: ${customer.problem}", style = MaterialTheme.typography.bodySmall)
                Text("\uD83D\uDCDE Contact: ${customer.contactNumber}", style = MaterialTheme.typography.bodySmall)
                Text("\uD83D\uDCB3 Paid: ${customer.advanced} / Total: ${customer.totalAmount}", style = MaterialTheme.typography.bodySmall)
                if (customer.status == "Pending" || customer.status == "Repaired") {
                    Text("\uD83D\uDCE6 Delivery Date: ${customer.deliveryDate}", style = MaterialTheme.typography.bodySmall)
                }
                Text("\uD83D\uDD52 Created: ${customer.date}", style = MaterialTheme.typography.labelSmall)
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

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "\uD83D\uDD12 Hide Security Info" else "\uD83D\uDD13 Show Security Info")
            }

            if (expanded) {
                Spacer(Modifier.height(4.dp))
                if (customer.securityType == "Password") {
                    Text("\uD83D\uDD11 Password: ${customer.phonePassword}", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("\uD83D\uDD10 Pattern:")
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
fun CustomerListSection(
    title: String,
    customers: List<Customer>,
    viewModel: CustomerViewModel
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    if (customers.isEmpty()) {
        Text("No devices scheduled.", style = MaterialTheme.typography.bodyMedium)
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(customers) { customer ->
                CustomerCard(customer = customer, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun statusToColor(status: String): Color {
    return when (status) {
        "Pending" -> MaterialTheme.colorScheme.outline
        "Repaired" -> MaterialTheme.colorScheme.primary
        "Delivered" -> MaterialTheme.colorScheme.primary.copy(green = 0.8f)
        "Cancelled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }
}
