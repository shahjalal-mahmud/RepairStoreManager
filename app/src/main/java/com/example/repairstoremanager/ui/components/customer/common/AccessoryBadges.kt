package com.example.repairstoremanager.ui.components.customer.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.data.model.Customer

@Composable
fun AccessoryBadges(customer: Customer) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (customer.battery) BadgeItem("Battery")
        if (customer.sim) BadgeItem("SIM")
        if (customer.memory) BadgeItem("Memory")
        if (customer.simTray) BadgeItem("SIM Tray")
        if (customer.backCover) BadgeItem("Back Cover")
        if (customer.deadPermission) BadgeItem("Dead Permission")
    }
}

@Composable
private fun BadgeItem(text: String) {
    Badge(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        content = { Text(text, style = MaterialTheme.typography.labelSmall) }
    )
}