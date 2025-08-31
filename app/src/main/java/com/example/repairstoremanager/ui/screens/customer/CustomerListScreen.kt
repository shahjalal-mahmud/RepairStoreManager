package com.example.repairstoremanager.ui.screens.customer

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.repairstoremanager.ui.components.common.ExpandableSearchBar
import com.example.repairstoremanager.ui.components.common.FilterChipGroup
import com.example.repairstoremanager.ui.components.common.SortChipGroup
import com.example.repairstoremanager.ui.components.customer.card.CustomerCard
import com.example.repairstoremanager.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun CustomerListScreen(
    navController: NavHostController,
    viewModel: CustomerViewModel = viewModel()
) {
    val customerList by viewModel.customers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedSort by remember { mutableStateOf("Newest First") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()
    val isScrolled = remember { derivedStateOf { lazyListState.firstVisibleItemIndex > 0 } }

    val sortOptions = listOf(
        "Newest First",
        "Oldest First",
        "Name (A–Z)",
        "Name (Z–A)",
        "Total Amount (High → Low)",
        "Total Amount (Low → High)"
    )

    val filteredList = customerList
        .filter {
            searchQuery.isBlank() ||
                    it.customerName.contains(searchQuery, true) ||
                    it.phoneModel.contains(searchQuery, true) ||
                    it.contactNumber.contains(searchQuery, true) ||
                    it.invoiceNumber?.contains(searchQuery, true) == true
        }
        .filter { selectedFilter == "All" || it.status == selectedFilter }
        .let { list ->
            when (selectedSort) {
                "Name (A–Z)" -> list.sortedBy { it.customerName }
                "Name (Z–A)" -> list.sortedByDescending { it.customerName }
                "Total Amount (High → Low)" -> list.sortedByDescending { it.totalAmount.toDoubleOrNull() ?: 0.0 }
                "Total Amount (Low → High)" -> list.sortedBy { it.totalAmount.toDoubleOrNull() ?: 0.0 }
                "Newest First" -> list.sortedByDescending { it.createdAt }
                "Oldest First" -> list.sortedBy { it.createdAt }
                else -> list
            }
        }

    LaunchedEffect(Unit) { viewModel.fetchCustomers() }

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = if (isScrolled.value) 8.dp else 0.dp,
                shadowElevation = if (isScrolled.value) 8.dp else 0.dp,
                modifier = Modifier.statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .fillMaxWidth()
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Customers (${filteredList.size})",
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    if (isSearchExpanded) {
                                        searchQuery = ""
                                        isSearchExpanded = false
                                    } else {
                                        isSearchExpanded = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = if (isSearchExpanded) "Close search" else "Search"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    if (isSearchExpanded) {
                        ExpandableSearchBar(
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            onClose = { isSearchExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // 👇 Compact applied chips shown ONLY in sticky top bar
                    if (selectedFilter != "All" || selectedSort != "Newest First") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (selectedFilter != "All") {
                                AssistChip(
                                    onClick = { selectedFilter = "All" },
                                    label = { Text("Filter: $selectedFilter") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Close, contentDescription = "Clear filter")
                                    }
                                )
                            }
                            if (selectedSort != "Newest First") {
                                AssistChip(
                                    onClick = { selectedSort = "Newest First" },
                                    label = { Text("Sort: $selectedSort") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Close, contentDescription = "Clear sort")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Full filter & sort chips SCROLL with content
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        SortChipGroup(
                            selectedSort = selectedSort,
                            onSortChange = { selectedSort = it },
                            sortOptions = sortOptions
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FilterChipGroup(
                            selectedFilter = selectedFilter,
                            onFilterChange = { selectedFilter = it },
                            filterOptions = listOf("All", "Pending", "Repaired", "Delivered", "Cancelled")
                        )
                    }
                }

                items(filteredList) { customer ->
                    CustomerCard(
                        customer = customer,
                        viewModel = viewModel,
                        navController = navController,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }

            if (filteredList.isEmpty() && !isLoading) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "No customers",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No customers found" else "No customers yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (searchQuery.isNotBlank()) {
                        Text(
                            text = "Try adjusting your search or filter",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}