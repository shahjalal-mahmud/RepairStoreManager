package com.example.repairstoremanager.ui.components.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
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
fun ShopDetailsCard(
    storeInfo: StoreInfo,
    isEditing: Boolean = false,
    onStoreNameChange: (String) -> Unit = {},
    onAddressChange: (String) -> Unit = {},
    onPhoneChange: (String) -> Unit = {},
    onEmailChange: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        if (isEditing) {
            // Editable fields
            OutlinedTextField(
                value = storeInfo.storeName,
                onValueChange = onStoreNameChange,
                label = { Text("Shop Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            OutlinedTextField(
                value = storeInfo.address,
                onValueChange = onAddressChange,
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            OutlinedTextField(
                value = storeInfo.phone,
                onValueChange = onPhoneChange,
                label = { Text("Contact Phone") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            OutlinedTextField(
                value = storeInfo.email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else {
            // Read-only display
            ProfileInfoItem(
                icon = Icons.Default.Store,
                title = "Shop Name",
                value = storeInfo.storeName.ifEmpty { "Not set" }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            ProfileInfoItem(
                icon = Icons.Default.LocationOn,
                title = "Address",
                value = storeInfo.address.ifEmpty { "Not set" }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            ProfileInfoItem(
                icon = Icons.Default.Phone,
                title = "Contact",
                value = storeInfo.phone.ifEmpty { "Not set" }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            ProfileInfoItem(
                icon = Icons.Default.Email,
                title = "Email",
                value = storeInfo.email.ifEmpty { "Not set" }
            )
        }
    }
}