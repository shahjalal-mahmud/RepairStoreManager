package com.example.repairstoremanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.ui.components.common.DatePickerDialog
import com.example.repairstoremanager.ui.components.customer.common.AccessoriesSection
import com.example.repairstoremanager.ui.components.customer.edit.ActionButtons
import com.example.repairstoremanager.ui.components.customer.edit.CustomerInfoSection
import com.example.repairstoremanager.ui.components.customer.edit.FinancialSection
import com.example.repairstoremanager.ui.components.customer.edit.PhoneDetailsSection
import com.example.repairstoremanager.ui.components.customer.edit.ReadOnlyInfoCard
import com.example.repairstoremanager.viewmodel.EditCustomerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCustomerScreen(
    customerId: String,
    navController: NavController,
    viewModel: EditCustomerViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val customer by viewModel.customer.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isSuccess by viewModel.isSuccess.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(customerId) {
        if (customer.id.isEmpty() || customer.id != customerId) {
            viewModel.loadCustomer(customerId)
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message = message)
            }
        }
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            // Show Toast message
            android.widget.Toast.makeText(
                context,
                "Customer updated successfully!",
                android.widget.Toast.LENGTH_SHORT
            ).show()

            // Navigate back immediately
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Customer Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (isLoading && customer.id.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading customer details...")
            }
        } else if (customer.id.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Error, contentDescription = "Error", tint = Color.Red)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Customer not found", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            EditCustomerContent(
                customer = customer,
                isLoading = isLoading,
                onUpdateField = viewModel::updateField,
                onSave = { updatedCustomer ->
                    viewModel.updateCustomer(
                        updatedCustomer = updatedCustomer,
                        onSuccess = {
                            // This callback is no longer needed since we're using LaunchedEffect
                        },
                        onError = {}
                    )
                },
                onShowDatePicker = { showDatePicker = true },
                modifier = Modifier.padding(innerPadding)
            )
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDateSelected = { date ->
                    viewModel.updateField("deliveryDate", date)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@Composable
fun EditCustomerContent(
    customer: Customer,
    isLoading: Boolean,
    onUpdateField: (String, Any) -> Unit,
    onSave: (Customer) -> Unit,
    onShowDatePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Read-only information card
        ReadOnlyInfoCard(customer)

        // Editable fields
        CustomerInfoSection(customer, onUpdateField)

        // Phone details section
        PhoneDetailsSection(customer, onUpdateField)

        // Financial section
        FinancialSection(customer, onUpdateField, onShowDatePicker)

        // Accessories section
        AccessoriesSection(
            battery = customer.battery,
            sim = customer.sim,
            memory = customer.memory,
            simTray = customer.simTray,
            backCover = customer.backCover,
            deadPermission = customer.deadPermission,
            onBatteryChange = { onUpdateField("battery", it) },
            onSimChange = { onUpdateField("sim", it) },
            onMemoryChange = { onUpdateField("memory", it) },
            onSimTrayChange = { onUpdateField("simTray", it) },
            onBackCoverChange = { onUpdateField("backCover", it) },
            onDeadPermissionChange = { onUpdateField("deadPermission", it) }
        )

        // Action buttons
        ActionButtons(
            isLoading = isLoading,
            onSave = { onSave(customer) },
            onCancel = { /* Handled by navigation */ }
        )
    }
}
