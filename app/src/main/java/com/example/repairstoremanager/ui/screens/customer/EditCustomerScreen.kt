package com.example.repairstoremanager.ui.screens.customer

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.repairstoremanager.ui.components.common.DatePickerDialog
import com.example.repairstoremanager.ui.components.customer.edit.EditCustomerContent
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
            Toast.makeText(
                context,
                "Customer updated successfully!",
                Toast.LENGTH_SHORT
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
                onCancel = { navController.popBackStack() }, // Add cancel callback
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