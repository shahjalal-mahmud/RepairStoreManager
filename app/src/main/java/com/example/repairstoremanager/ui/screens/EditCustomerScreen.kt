package com.example.repairstoremanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.viewmodel.CustomerViewModel

@Composable
fun EditCustomerScreen(
    customer: Customer,
    onSave: (Customer) -> Unit,
    onCancel: () -> Unit,
    viewModel: CustomerViewModel
) {
    var editedCustomer by remember { mutableStateOf(customer.copy()) }
    val context = LocalContext.current // Get context during composition

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Edit Customer Details",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = editedCustomer.customerName,
            onValueChange = { editedCustomer = editedCustomer.copy(customerName = it) },
            label = { Text("Customer Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = editedCustomer.contactNumber,
            onValueChange = { editedCustomer = editedCustomer.copy(contactNumber = it) },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth()
        )

        // Add other fields similarly (phone model, problem, etc.)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    viewModel.updateCustomer(
                        updatedCustomer = editedCustomer,
                        context = context, // Use the context we got during composition
                        onSuccess = { onSave(editedCustomer) },
                        onError = {}
                    )
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text("Save")
            }
        }
    }
}