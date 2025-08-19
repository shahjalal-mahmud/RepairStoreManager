package com.example.repairstoremanager.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
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
import com.example.repairstoremanager.ui.components.customer.card.CustomerCard
import com.example.repairstoremanager.ui.components.customer.list.CustomerSearchFilterSortBar
import com.example.repairstoremanager.viewmodel.CustomerViewModel

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun CustomerListScreen(viewModel: CustomerViewModel = viewModel()) {
    val customerList by viewModel.customers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedSort by remember { mutableStateOf("Newest First") }

    val filteredList = customerList
        .filter {
            (searchQuery.isBlank() ||
                    it.customerName.contains(searchQuery, true) ||
                    it.phoneModel.contains(searchQuery, true) ||
                    it.contactNumber.contains(searchQuery, true) ||
                    it.invoiceNumber?.contains(searchQuery, true) == true) // Add invoice number search
        }
        .filter {
            selectedFilter == "All" || it.status == selectedFilter
        }
        .let { list ->
            when (selectedSort) {
                "Name (A–Z)" -> list.sortedBy { it.customerName }
                "Total Amount (High → Low)" -> list.sortedByDescending { it.totalAmount.toDoubleOrNull() ?: 0.0 }
                "Total Amount (Low → High)" -> list.sortedBy { it.totalAmount.toDoubleOrNull() ?: 0.0 }
                "Newest First" -> list.sortedByDescending { it.createdAt }
                "Oldest First" -> list.sortedBy { it.createdAt }
                else -> list
            }
        }

    LaunchedEffect(Unit) {
        viewModel.fetchCustomers()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stickyHeader {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                CustomerSearchFilterSortBar(
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    selectedFilter = selectedFilter,
                    onFilterChange = { selectedFilter = it },
                    selectedSort = selectedSort,
                    onSortChange = { selectedSort = it }
                )
            }
        }

        items(filteredList) { customer ->
            CustomerCard(customer = customer, viewModel = viewModel)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}