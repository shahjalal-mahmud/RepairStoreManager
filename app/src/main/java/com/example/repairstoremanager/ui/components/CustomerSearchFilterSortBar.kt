package com.example.repairstoremanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CustomerSearchFilterSortBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    selectedSort: String,
    onSortChange: (String) -> Unit
) {
    val filterOptions = listOf("All", "Pending", "Repaired", "Delivered", "Cancelled")
    val sortOptions = listOf(
        "None",
        "Name (A–Z)",
        "Total Amount (High → Low)",
        "Total Amount (Low → High)",
        "Newest First",
        "Oldest First"
    )

    var filterExpanded by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = { Text("🔍 Search by name, phone or model") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(onClick = { filterExpanded = true }) {
                    Text("Filter: $selectedFilter ⬇️")
                }

                DropdownMenu(
                    expanded = filterExpanded,
                    onDismissRequest = { filterExpanded = false }
                ) {
                    filterOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onFilterChange(option)
                                filterExpanded = false
                            }
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(onClick = { sortExpanded = true }) {
                    Text("Sort: $selectedSort ⬇️")
                }

                DropdownMenu(
                    expanded = sortExpanded,
                    onDismissRequest = { sortExpanded = false }
                ) {
                    sortOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onSortChange(option)
                                sortExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}