package com.example.repairstoremanager.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.repairstoremanager.viewmodel.StoreViewModel

@Composable
fun StoreInfoSection(viewModel: StoreViewModel, onLogout: () -> Unit) {
    val info = viewModel.storeInfo
    val isEditing = viewModel.isEditMode
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onLogoPicked(it, context) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Store Logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable(enabled = isEditing) { launcher.launch("image/*") },
                contentAlignment = Alignment.BottomEnd
            ) {
                if (viewModel.logoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(viewModel.logoUri),
                        contentDescription = "Store Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Base64Image(info.logoBase64, modifier = Modifier.fillMaxSize())
                }
                if (isEditing) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .offset((-8).dp, (-8).dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background)
                    )
                }
            }

            // Title
            Text(
                text = "Store Information",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)
            )

            // Fields
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EditableTextField("Store Name", info.storeName, isEditing) { viewModel.updateStoreName(it) }
                EditableTextField("Owner Name", info.ownerName, isEditing) { viewModel.updateOwnerName(it) }
                EditableTextField("Address", info.address, isEditing) { viewModel.updateAddress(it) }
                EditableTextField("Phone Number", info.phone, isEditing) { viewModel.updatePhone(it) }
                EditableTextField("Email", info.email, isEditing) { viewModel.updateEmail(it) }
                EditableTextField("Working Hours", info.workingHours, isEditing) { viewModel.updateWorkingHours(it) }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    if (isEditing) viewModel.updateStoreInfo()
                    viewModel.toggleEditMode()
                }) {
                    Text(if (isEditing) "Save" else "Edit Info")
                }

                OutlinedButton(onClick = {
                    viewModel.logout { onLogout() }
                }) {
                    Text("Logout")
                }
            }

            TextButton(onClick = { showDialog = true }) {
                Text("Change Password")
            }

            viewModel.message?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }
        }
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
}

@Composable
fun EditableTextField(label: String, value: String, isEditable: Boolean, onValueChange: (String) -> Unit) {
    if (isEditable) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    } else {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "$label: ",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(130.dp)
            )
            Text(text = value)
        }
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
