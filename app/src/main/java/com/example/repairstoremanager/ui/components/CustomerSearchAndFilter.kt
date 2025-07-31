package com.example.repairstoremanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CustomerSearchAndFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit
) {
    val filterOptions = listOf("All", "Pending", "Repaired", "Delivered", "Cancelled")
    var filterExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = { Text("🔍 Search by name, phone or model") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Box {
            OutlinedButton(
                onClick = { filterExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Filter: $selectedFilter ⬇️")
            }

            DropdownMenu(
                expanded = filterExpanded,
                onDismissRequest = { filterExpanded = false }
            ) {
                filterOptions.forEach { filter ->
                    DropdownMenuItem(
                        text = { Text(filter) },
                        onClick = {
                            onFilterChange(filter)
                            filterExpanded = false
                        }
                    )
                }
            }
        }
    }
}