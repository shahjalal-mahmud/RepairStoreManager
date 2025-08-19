package com.example.repairstoremanager.ui.components.customer.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun StatusDropdown(
    selectedStatus: String,
    options: List<String>,
    onStatusChange: (String) -> Unit,
    statusColor: Color,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var buttonWidth by remember { mutableStateOf(0) }

    Box(modifier = modifier) {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(containerColor = statusColor),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp
            ),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { buttonWidth = it.width }
        ) {
            Text(
                text = selectedStatus,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(with(LocalDensity.current) { buttonWidth.toDp() })
        ) {
            options.forEach { status ->
                DropdownMenuItem(
                    text = {
                        Text(
                            status,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
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
        "Delivered" -> MaterialTheme.colorScheme.primary.copy(green = 0.8f)
        "Cancelled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }
}