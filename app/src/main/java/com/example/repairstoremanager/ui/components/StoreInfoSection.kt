// ui/components/StoreInfoSection.kt
package com.example.repairstoremanager.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.repairstoremanager.R
import com.example.repairstoremanager.viewmodel.StoreViewModel

@Composable
fun StoreInfoSection(viewModel: StoreViewModel, onLogout: () -> Unit) {
    val info = viewModel.storeInfo
    val isEditing = viewModel.isEditMode

    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Logo (replace with image picker logic later)
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Image(
                painter = rememberAsyncImagePainter(info.logoUrl.ifEmpty { R.drawable.om_icon }),
                contentDescription = "Store Logo",
                modifier = Modifier.fillMaxSize()
            )
        }

        // Editable Fields
        EditableTextField("Owner", info.ownerName, isEditing) { viewModel.updateOwnerName(it) }
        EditableTextField("Address", info.address, isEditing) { viewModel.updateAddress(it) }
        EditableTextField("Phone", info.phone, isEditing) { viewModel.updatePhone(it) }
        EditableTextField("Email", info.email, isEditing) { viewModel.updateEmail(it) }
        EditableTextField("Working Hours", info.workingHours, isEditing) { viewModel.updateWorkingHours(it) }

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = {
                if (isEditing) viewModel.updateStoreInfo()
                else viewModel.toggleEditMode()
            }) {
                Text(if (isEditing) "Save" else "Update Info")
            }

            Button(onClick = {
                viewModel.logout {
                    onLogout()
                }
            }) {
                Text("Logout")
            }
        }

        Button(onClick = { showDialog = true }) {
            Text("Change Password")
        }

        if (showDialog) {
            ChangePasswordDialog(
                onDismiss = { showDialog = false },
                onSubmit = { newPassword ->
                    viewModel.changePassword(newPassword) { error ->
                        showDialog = false
                        viewModel.message = error ?: "Password changed successfully"
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        viewModel.message?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun EditableTextField(label: String, value: String, isEditable: Boolean, onValueChange: (String) -> Unit) {
    if (isEditable) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        Text("• $label: $value")
    }
}

@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onSubmit(newPassword) }) {
                Text("Change")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Change Password") },
        text = {
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") }
            )
        }
    )
}