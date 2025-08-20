package com.example.repairstoremanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.ui.components.common.DatePickerDialog
import com.example.repairstoremanager.ui.components.customer.common.AccessoriesSection
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
            scope.launch {
                snackbarHostState.showSnackbar(message = "Customer updated successfully!")
                // Navigate back after a short delay
                kotlinx.coroutines.delay(1500)
                navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Customer Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                        onSuccess = {},
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

@Composable
fun ReadOnlyInfoCard(customer: Customer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Read-only Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            InfoRow("Invoice Number", customer.invoiceNumber)
            InfoRow("Date", customer.date)
            InfoRow("Status", customer.status)
            InfoRow("Created", formatDate(customer.createdAt))
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$label:", fontWeight = FontWeight.Medium)
        Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CustomerInfoSection(customer: Customer, onUpdateField: (String, Any) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Customer Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = customer.customerName,
            onValueChange = { onUpdateField("customerName", it) },
            label = { Text("Customer Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = customer.contactNumber,
            onValueChange = { onUpdateField("contactNumber", it) },
            label = { Text("Contact Number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PhoneDetailsSection(customer: Customer, onUpdateField: (String, Any) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Phone Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = customer.phoneModel,
            onValueChange = { onUpdateField("phoneModel", it) },
            label = { Text("Phone Model") },
            leadingIcon = { Icon(Icons.Default.Smartphone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = customer.problem,
            onValueChange = { onUpdateField("problem", it) },
            label = { Text("Problem Description") },
            singleLine = false,
            minLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )
    }
}

@Composable
fun FinancialSection(
    customer: Customer,
    onUpdateField: (String, Any) -> Unit,
    onShowDatePicker: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Financial Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = customer.totalAmount,
                onValueChange = { onUpdateField("totalAmount", it) },
                label = { Text("Total Amount") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = customer.advanced,
                onValueChange = { onUpdateField("advanced", it) },
                label = { Text("Advanced") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = customer.deliveryDate,
            onValueChange = { onUpdateField("deliveryDate", it) },
            label = { Text("Delivery Date") },
            leadingIcon = {
                IconButton(onClick = onShowDatePicker) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Pick date")
                }
            },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ActionButtons(
    isLoading: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            enabled = !isLoading
        ) {
            Text("Cancel")
        }

        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(16.dp)
                        .height(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Saving...")
            } else {
                Icon(Icons.Default.CheckCircle, contentDescription = "Save", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Changes")
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return try {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
        sdf.format(java.util.Date(timestamp))
    } catch (e: Exception) {
        "Unknown date"
    }
}