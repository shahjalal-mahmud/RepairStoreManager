package com.example.repairstoremanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.data.model.Customer

@Composable
fun InvoicePrintPreview(customer: Customer) {
    val due = (customer.totalAmount.toIntOrNull() ?: 0) - (customer.advanced.toIntOrNull() ?: 0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "TECH CARE CENTER",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = "Device Repair Service",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            // Invoice Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Invoice #: ${customer.invoiceNumber}",
                        style = MaterialTheme.typography.bodySmall)
                    Text("Date: ${customer.date}",
                        style = MaterialTheme.typography.bodySmall)
                }
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(customer.status.uppercase())
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Customer Info
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("CUSTOMER DETAILS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                Text(
                    customer.customerName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ))
                Text("📱 ${customer.contactNumber}")
            }

            Spacer(Modifier.height(16.dp))

            // Device Info
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("DEVICE DETAILS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                InfoRow("Model", customer.phoneModel)
                InfoRow("Problem", customer.problem)
                InfoRow("Expected Delivery", customer.deliveryDate)
            }

            Spacer(Modifier.height(16.dp))

            // Payment Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("PAYMENT SUMMARY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                PaymentRow("Total", "৳${customer.totalAmount}")
                PaymentRow("Advance Paid", "৳${customer.advanced}")
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
                PaymentRow("Balance Due", "৳$due", isTotal = true)
            }

            Spacer(Modifier.height(16.dp))

            // Accessories
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ACCESSORIES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(Modifier.height(8.dp))

                val accessories = listOf(
                    "Battery" to customer.battery,
                    "SIM Card" to customer.sim,
                    "Memory Card" to customer.memory,
                    "SIM Tray" to customer.simTray,
                    "Back Cover" to customer.backCover
                )

                accessories.filter { it.second }.forEach { (name, _) ->
                    AccessoryItem(name)
                }

                if (customer.deadPermission) {
                    AccessoryItem("Dead Repair Permission", icon = "⚠️")
                }
            }

            Spacer(Modifier.height(24.dp))

            // Footer
            Text(
                text = "Thank you for your business!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Bring this invoice when collecting your device",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.SemiBold
        ))
    }
}

@Composable
private fun PaymentRow(label: String, value: String, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = if (isTotal) {
            MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            MaterialTheme.typography.bodyMedium
        })
    }
}

@Composable
private fun AccessoryItem(name: String, icon: String = "✔") {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(icon, modifier = Modifier.padding(end = 8.dp))
        Text(name, style = MaterialTheme.typography.bodyMedium)
    }
}