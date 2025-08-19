package com.example.repairstoremanager.ui.components.customer.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.ui.components.customer.add.PatternLockCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityDropdown(customer: Customer) {
    var expanded by remember { mutableStateOf(false) }
    val headerText = if (customer.securityType == "Password") "Password Lock" else "Pattern Lock"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp) // Reduced vertical padding
    ) {
        // Clickable header to toggle expansion
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 6.dp), // Reduced vertical padding
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = headerText, // Dynamic header text
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Expanded content
        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp) // Reduced bottom padding
            ) {
                if (customer.securityType == "Password") {
                    Text(
                        text = "Password: ${customer.phonePassword}",
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp), // Reduced padding
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp) // Reduced spacing
                    ) {
                        PatternLockCanvas(
                            pattern = customer.pattern,
                            isInteractive = false,
                            isPreview = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .padding(bottom = 4.dp) // Reduced padding
                        )
                    }
                }
            }
        }
    }
}