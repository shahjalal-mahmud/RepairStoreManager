package com.example.repairstoremanager.ui.components.customer.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun CustomerInfoSection(
    customerName: String,
    contactNumber: String,
    onCustomerNameChange: (String) -> Unit,
    onContactNumberChange: (String) -> Unit,
    gmailContacts: List<Pair<String, String>> = emptyList(),
    onContactSelected: (Pair<String, String>) -> Unit = { _ -> },
    isGmailConnected: Boolean = false,
    onSaveToGmail: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("👤 Customer Info", style = MaterialTheme.typography.titleMedium)

            // Gmail Contacts Dropdown
            if (isGmailConnected && gmailContacts.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                GmailContactsDropdown(
                    contacts = gmailContacts,
                    onContactSelected = onContactSelected
                )
            }

            Spacer(Modifier.height(8.dp))

            CustomTextField(
                label = "Customer Name",
                value = customerName,
                onValueChange = onCustomerNameChange,
                keyboardType = KeyboardType.Text
            )

            CustomTextField(
                label = "Contact Number",
                value = contactNumber,
                onValueChange = onContactNumberChange,
                keyboardType = KeyboardType.Phone
            )

            // Save to Gmail button
            if (isGmailConnected && customerName.isNotBlank() && contactNumber.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onSaveToGmail,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Contacts,
                        contentDescription = "Save to Gmail"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Save to Gmail Contacts")
                }
            }

            // Connect to Gmail button if not connected
            if (!isGmailConnected) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { /* Handle Gmail connection */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Contacts,
                        contentDescription = "Connect Gmail"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Connect Gmail to Import Contacts")
                }
            }
        }
    }
}

@Composable
fun GmailContactsDropdown(
    contacts: List<Pair<String, String>>,
    onContactSelected: (Pair<String, String>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("Select from Gmail contacts") }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedText, maxLines = 1)
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            contacts.forEach { (name, phone) ->
                DropdownMenuItem(
                    text = { Text("$name - $phone") },
                    onClick = {
                        onContactSelected(name to phone)
                        selectedText = "$name - $phone"
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CustomTextField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}