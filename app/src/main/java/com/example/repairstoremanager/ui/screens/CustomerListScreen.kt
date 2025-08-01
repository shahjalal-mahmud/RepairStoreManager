package com.example.repairstoremanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repairstoremanager.ui.components.CustomerCard
import com.example.repairstoremanager.ui.components.CustomerSearchFilterSortBar
import com.example.repairstoremanager.viewmodel.CustomerViewModel

@Composable
fun CustomerListScreen(viewModel: CustomerViewModel = viewModel()) {
    val customerList by viewModel.customers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedSort by remember { mutableStateOf("None") }

    // 1️⃣ Filtered and sorted list
    val filteredList = customerList
        .filter {
            (searchQuery.isBlank() || it.customerName.contains(searchQuery, true)
                    || it.phoneModel.contains(searchQuery, true)
                    || it.contactNumber.contains(searchQuery, true))
        }
        .filter {
            selectedFilter == "All" || it.status == selectedFilter
        }
        .let { list ->
            when (selectedSort) {
                "Name (A–Z)" -> list.sortedBy { it.customerName }
                "Total Amount (High → Low)" -> list.sortedByDescending { it.totalAmount.toDoubleOrNull() ?: 0.0 }
                "Total Amount (Low → High)" -> list.sortedBy { it.totalAmount.toDoubleOrNull() ?: 0.0 }
                "Newest First" -> list.sortedByDescending { it.createdAt } // Ensure `createdAt` exists
                "Oldest First" -> list.sortedBy { it.createdAt }
                else -> list
            }
        }

    LaunchedEffect(Unit) {
        viewModel.fetchCustomers()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        CustomerSearchFilterSortBar(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            selectedFilter = selectedFilter,
            onFilterChange = { selectedFilter = it },
            selectedSort = selectedSort,
            onSortChange = { selectedSort = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredList) { customer ->
                CustomerCard(customer = customer, viewModel = viewModel)
            }
        }
    }
}
