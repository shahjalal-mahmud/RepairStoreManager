package com.example.repairstoremanager.ui.components.customer.add

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.util.rememberContactPickerLauncher
import com.example.repairstoremanager.util.rememberContactSaverLauncher

@Composable
fun CustomerInfoSection(
    customerName: String,
    contactNumber: String,
    isFormValid: Boolean,
    saveToContacts: Boolean,
    onCustomerNameChange: (String) -> Unit,
    onContactNumberChange: (String) -> Unit,
    onSaveToContactsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // Remove this local state - use the one passed from parent
    // var saveToContacts by remember { mutableStateOf(false) }

    val (hasContactPermission, launchContactPicker) = rememberContactPickerLauncher { contactInfo ->
        contactInfo?.let { contact ->
            onCustomerNameChange(contact.name)
            onContactNumberChange(contact.phoneNumber)
            Toast.makeText(context, "Contact imported: ${contact.name}", Toast.LENGTH_SHORT).show()
        }
    }

    // Contact saver launcher
    val (hasWriteContactPermission, saveContact) = rememberContactSaverLauncher { success ->
        // Handle save result if needed
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Customer Information",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            OutlinedIconButton(
                onClick = launchContactPicker,
                enabled = hasContactPermission,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Contacts,
                    contentDescription = "Pick from Contacts",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "* Provide at least one field",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(Modifier.height(10.dp))

        // Customer Name Field
        OutlinedTextField(
            value = customerName,
            onValueChange = onCustomerNameChange,
            label = { Text("Customer Name") },
            leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            isError = customerName.isEmpty() && contactNumber.isEmpty(),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(Modifier.height(10.dp))

        // Contact Number Field
        OutlinedTextField(
            value = contactNumber,
            onValueChange = onContactNumberChange,
            label = { Text("Contact Number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            isError = customerName.isEmpty() && contactNumber.isEmpty(),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(Modifier.height(16.dp))

        // Save to Contacts Toggle Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContactPhone,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Save to Contacts",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = "Save customer to phone contacts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = saveToContacts,
                        onCheckedChange = { newValue ->
                            onSaveToContactsChange(newValue) // Call the parent's callback
                            // Auto-save when toggle is enabled and we have data
                            if (newValue && (customerName.isNotBlank() || contactNumber.isNotBlank())) {
                                saveContact(customerName, contactNumber)
                            }
                        }
                    )
                }

                // Permission hint for write contacts
                if (!hasWriteContactPermission && saveToContacts) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Contact write permission required to save contacts",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                }

                // Info text when toggle is on
                if (saveToContacts) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "✓ Customer will be saved to your phone contacts",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // Original permission hint for reading contacts
        if (!hasContactPermission) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Contact permission required to pick from contacts",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.error
                )
            )
        }

        if (!isFormValid) Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun CustomTextField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    isError: Boolean = false,
    onNext: (() -> Unit)? = null,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNext?.invoke() }
        ),
        isError = isError // Apply error state
    )
}