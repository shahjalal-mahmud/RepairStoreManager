package com.example.repairstoremanager.ui.screens.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RequestPage
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // App Settings Section
            SettingsSection(title = "App Settings") {
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Google Drive Backup",
                    subtitle = "Store data, photos and videos in cloud",
                    hasSwitch = true,
                    initialSwitchState = false
                )
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Message,
                    title = "Auto Messaging",
                    subtitle = "Enable automatic SMS notifications",
                    hasSwitch = true,
                    initialSwitchState = false
                )
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Update Password",
                    subtitle = "Change your login password"
                )
            }

            // Reports & Analytics Section
            SettingsSection(title = "Reports & Analytics") {
                SettingsItem(
                    icon = Icons.Default.Analytics,
                    title = "Analytics Orders Report",
                    subtitle = "View detailed order analytics"
                )
                SettingsItem(
                    icon = Icons.Default.Receipt,
                    title = "Purchasing History",
                    subtitle = "Track all your purchases"
                )
                SettingsItem(
                    icon = Icons.Default.History,
                    title = "Deleted Order History",
                    subtitle = "Recover deleted orders"
                )
            }

            // Support Section
            SettingsSection(title = "Support & Information") {
                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "Terms and Conditions",
                    subtitle = "Read our terms of service"
                )
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    title = "How to use this app",
                    subtitle = "User guide and tutorials"
                )
                SettingsItem(
                    icon = Icons.Default.RequestPage,
                    title = "Request a new feature",
                    subtitle = "Suggest improvements"
                )
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = "Rate us",
                    subtitle = "Share your experience"
                )
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About us",
                    subtitle = "Learn about our company"
                )
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.ContactSupport,
                    title = "Contact us",
                    subtitle = "Get in touch with support"
                )
            }

            // Additional Features
            SettingsSection(title = "Additional Features") {
                SettingsItem(
                    icon = Icons.Default.MonetizationOn,
                    title = "Financial Reports",
                    subtitle = "Revenue, profit, and expense reports"
                )
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "Customer Management",
                    subtitle = "Advanced customer analytics"
                )
                SettingsItem(
                    icon = Icons.Default.ShoppingCart,
                    title = "Inventory Alerts",
                    subtitle = "Low stock notifications"
                )
            }

            // Account Section
            SettingsSection(title = "Account") {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    title = "Logout",
                    subtitle = "Sign out from your account",
                    isDestructive = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            content()
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    hasSwitch: Boolean = false,
    initialSwitchState: Boolean = false,
    isDestructive: Boolean = false,
    onClick: () -> Unit = {}
) {
    val switchState = remember { mutableStateOf(initialSwitchState) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 16.dp),
                tint = if (isDestructive) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )

            // Text Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    ),
                    color = if (isDestructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Switch or Arrow
            if (hasSwitch) {
                Switch(
                    checked = switchState.value,
                    onCheckedChange = { switchState.value = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    }

    // Divider between items (except for the last one)
    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp, end = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    )
}