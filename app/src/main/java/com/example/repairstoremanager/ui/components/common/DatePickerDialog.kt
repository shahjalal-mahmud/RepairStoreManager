package com.example.repairstoremanager.ui.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Delivery Date",
                    style = MaterialTheme.typography.titleMedium
                )

                // Simple date picker - you might want to use a proper date picker library
                // For now, this is a placeholder
                OutlinedTextField(
                    value = selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    onValueChange = { /* Read-only for now */ },
                    label = { Text("Selected Date") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                            onDateSelected(formattedDate)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Select")
                    }
                }
            }
        }
    }
}