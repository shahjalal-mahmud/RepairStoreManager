package com.example.repairstoremanager.ui.components.customer.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.ui.components.customer.add.PatternLockCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityDropdown(customer: Customer) {
    var securityExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = securityExpanded,
        onExpandedChange = { securityExpanded = !securityExpanded }
    ) {
        OutlinedTextField(
            value = if (customer.securityType == "Password") "Password Protected" else "Pattern Lock",
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = securityExpanded)
            },
            label = { Text("Security") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = securityExpanded,
            onDismissRequest = { securityExpanded = false }
        ) {
            if (customer.securityType == "Password") {
                Text(
                    text = "Password: ${customer.phonePassword}",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Pattern Lock:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    PatternLockCanvas(
                        pattern = customer.pattern,
                        isInteractive = false,
                        isPreview = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }
        }
    }
}