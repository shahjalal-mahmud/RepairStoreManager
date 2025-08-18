package com.example.repairstoremanager.ui.components.customer.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.ui.components.customer.common.StatusDropdown
import com.example.repairstoremanager.ui.components.customer.common.statusToColor
import com.example.repairstoremanager.viewmodel.CustomerViewModel

@Composable
fun CustomerCardActions(
    customer: Customer,
    viewModel: CustomerViewModel,
    onPrintClick: () -> Unit,
    onEditClick: () -> Unit,
    onCallClick: () -> Unit,
) {
    val statusOptions = listOf("Pending", "Repaired", "Delivered", "Cancelled")
    val statusColor = statusToColor(customer.status)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PrintButton(
            onClick = onPrintClick,
            modifier = Modifier.weight(1f)
        )
        EditButton(
            onClick = onEditClick,
            modifier = Modifier.weight(1f)
        )
        CallDropdownButton(
            onClick = onCallClick,
            modifier = Modifier.weight(1f)
        )
        StatusDropdown(
            selectedStatus = customer.status,
            options = statusOptions,
            onStatusChange = { newStatus ->
                viewModel.updateCustomerStatus(
                    customerId = customer.id,
                    newStatus = newStatus,
                )
            },
            statusColor = statusColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PrintButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier, // weight comes from parent
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Icon(Icons.Default.Print, contentDescription = "Print", modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Print", style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun EditButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier, // weight comes from parent
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Edit", style = MaterialTheme.typography.labelMedium)
    }
}
