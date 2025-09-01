package com.example.repairstoremanager.ui.components.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.data.model.StoreInfo
import com.example.repairstoremanager.ui.screens.main.ProfileInfoItem

@Composable
fun OwnerDetailsCard(
    storeInfo: StoreInfo,
    isEditing: Boolean = false,
    onOwnerNameChange: (String) -> Unit = {},
    onOwnerPhoneChange: (String) -> Unit = {},
    onOwnerEmailChange: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        if (isEditing) {
            // Editable fields
            OutlinedTextField(
                value = storeInfo.ownerName,
                onValueChange = onOwnerNameChange,
                label = { Text("Owner Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            OutlinedTextField(
                value = storeInfo.ownerPhone,
                onValueChange = onOwnerPhoneChange,
                label = { Text("Owner Phone") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            OutlinedTextField(
                value = storeInfo.ownerEmail,
                onValueChange = onOwnerEmailChange,
                label = { Text("Owner Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else {
            // Read-only display
            ProfileInfoItem(
                icon = Icons.Default.Person,
                title = "Owner Name",
                value = storeInfo.ownerName.ifEmpty { "Not set" }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            ProfileInfoItem(
                icon = Icons.Default.Phone,
                title = "Phone",
                value = storeInfo.ownerPhone.ifEmpty { "Not set" }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            ProfileInfoItem(
                icon = Icons.Default.Email,
                title = "Email",
                value = storeInfo.ownerEmail.ifEmpty { "Not set" }
            )
        }
    }
}