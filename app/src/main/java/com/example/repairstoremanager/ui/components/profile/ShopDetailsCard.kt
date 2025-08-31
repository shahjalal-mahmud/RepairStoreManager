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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.ui.screens.main.ProfileInfoItem

@Composable
fun ShopDetailsCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        ProfileInfoItem(
            icon = Icons.Default.Store,
            title = "Shop Name",
            value = "Tech Repair Hub"
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )

        ProfileInfoItem(
            icon = Icons.Default.LocationOn,
            title = "Address",
            value = "123 Main St, City, State"
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )

        ProfileInfoItem(
            icon = Icons.Default.Phone,
            title = "Contact",
            value = "+1 (555) 123-4567"
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )

        ProfileInfoItem(
            icon = Icons.Default.Email,
            title = "Email",
            value = "contact@techrepairhub.com"
        )
    }
}