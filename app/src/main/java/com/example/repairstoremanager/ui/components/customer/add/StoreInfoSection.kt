package com.example.repairstoremanager.ui.components.customer.add

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.repairstoremanager.ui.components.profile.Base64Image
import com.example.repairstoremanager.viewmodel.StoreViewModel

@Composable
fun StoreInfoSection(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
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
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Store Logo with edit icon overlay
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
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        tonalElevation = 4.dp,
                        modifier = Modifier
                            .size(28.dp)
                            .offset((-8).dp, (-8).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }

            Text(
                text = "Store Information",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            // Editable fields with consistent spacing
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                EditableTextField("Store Name", info.storeName, isEditing) { viewModel.updateStoreName(it) }
                EditableTextField("Owner Name", info.ownerName, isEditing) { viewModel.updateOwnerName(it) }
                EditableTextField("Address", info.address, isEditing) { viewModel.updateAddress(it) }
                EditableTextField("Phone Number", info.phone, isEditing) { viewModel.updatePhone(it) }
                EditableTextField("Email", info.email, isEditing) { viewModel.updateEmail(it) }
                EditableTextField("Working Hours", info.workingHours, isEditing) { viewModel.updateWorkingHours(it) }
            }

            // Action Buttons Row with spacing and consistent size
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        if (isEditing) viewModel.updateStoreInfo()
                        viewModel.toggleEditMode()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isEditing) "Save" else "Edit Info")
                }

                OutlinedButton(
                    onClick = { viewModel.logout { onLogout() } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Logout")
                }
            }

            // Change Password button centered below
            TextButton(
                onClick = { showDialog = true },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Change Password")
            }

            // Status message text
            viewModel.message?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
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
