package com.example.repairstoremanager.ui.components.customer.card

import androidx.compose.ui.geometry.Rect // ✅ use Compose Rect, not android.graphics.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.ui.components.customer.common.StatusDropdown
import com.example.repairstoremanager.ui.components.customer.common.statusToColor
import com.example.repairstoremanager.viewmodel.CustomerViewModel

@Composable
fun CustomerCardActions(
    customer: Customer,
    viewModel: CustomerViewModel,
    navController: NavHostController,
    onPrintClick: () -> Unit,
    onCallClick: (Rect) -> Unit,
    callExpanded: Boolean,
    callButtonBounds: Rect?,
    onCallDismiss: () -> Unit
){
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
            onClick = {
                navController.navigate("edit_customer/${customer.id}") // Use navigation
            },
            modifier = Modifier.weight(1f)
        )

        // Contact Button with bounds tracking
        Box(modifier = Modifier.weight(1f)) {
            var buttonBounds by remember { mutableStateOf<Rect?>(null) }
            val density = LocalDensity.current

            CallDropdownButton(
                onClick = { buttonBounds?.let { onCallClick(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        buttonBounds = coordinates.boundsInRoot()
                    }
            )

            CallDropdownMenu(
                customer = customer,
                viewModel = viewModel,
                context = LocalContext.current,
                expanded = callExpanded,
                onDismiss = onCallDismiss,
                modifier = Modifier.width(
                    with(density) { buttonBounds?.width?.toDp() ?: 0.dp } // ✅ convert properly
                )
            )
        }

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
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Print,
                contentDescription = "Print",
                modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Print", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun EditButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Edit",
                modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Edit", style = MaterialTheme.typography.labelMedium)
        }
    }
}