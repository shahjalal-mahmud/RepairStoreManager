package com.example.repairstoremanager.ui.screens.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.repairstoremanager.ui.components.profile.AdditionalFeaturesCard
import com.example.repairstoremanager.ui.components.profile.OwnerDetailsCard
import com.example.repairstoremanager.ui.components.profile.QuickAccessGrid
import com.example.repairstoremanager.ui.components.profile.ShopDetailsCard
import com.example.repairstoremanager.ui.components.profile.UserProfileHeader
import com.example.repairstoremanager.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    storeViewModel: StoreViewModel = viewModel()
) {
    val context = LocalContext.current

    // Permission launcher to read phone state for SIM info
    val simPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) storeViewModel.loadSimList(context)
    }

    val hasPhoneStatePermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_PHONE_STATE
    ) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(Unit) {
        // Load store info from Firebase
        storeViewModel.loadStoreInfo()

        if (hasPhoneStatePermission) {
            storeViewModel.loadSimList(context)
        } else {
            simPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Profile",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (storeViewModel.isEditMode) {
                        // Save button when in edit mode
                        IconButton(onClick = {
                            storeViewModel.saveAllChanges()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save Changes",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        // Cancel button when in edit mode
                        IconButton(onClick = {
                            storeViewModel.cancelEdit()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Edit",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        // Edit button when not in edit mode
                        IconButton(onClick = {
                            storeViewModel.toggleEditMode()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Shop Profile Header with logo and shop name
            item {
                UserProfileHeader(viewModel = storeViewModel)
            }

            // Shop Details Section (with inline editing)
            item {
                ProfileSection(title = "Shop Details", icon = Icons.Default.Store) {
                    ShopDetailsCard(
                        storeInfo = storeViewModel.storeInfo,
                        isEditing = storeViewModel.isEditMode,
                        onStoreNameChange = { storeViewModel.updateStoreName(it) },
                        onAddressChange = { storeViewModel.updateAddress(it) },
                        onPhoneChange = { storeViewModel.updatePhone(it) },
                        onEmailChange = { storeViewModel.updateEmail(it) }
                    )
                }
            }

            // Owner Details (with inline editing)
            item {
                ProfileSection(title = "Owner Information", icon = Icons.Default.Person) {
                    OwnerDetailsCard(
                        storeInfo = storeViewModel.storeInfo,
                        isEditing = storeViewModel.isEditMode,
                        onOwnerNameChange = { storeViewModel.updateOwnerName(it) },
                        onOwnerPhoneChange = { storeViewModel.updateOwnerPhone(it) },
                        onOwnerEmailChange = { storeViewModel.updateOwnerEmail(it) }
                    )
                }
            }

            // Staff Management
//            item {
//                ProfileSection(title = "Staff Members", icon = Icons.Default.Work) {
//                    StaffManagementCard(navController)
//                }
//            }

            // Quick Access
            item {
                ProfileSection(title = "Quick Access", icon = Icons.Default.Security) {
                    QuickAccessGrid(navController)
                }
            }

            // Additional Features
            item {
                ProfileSection(title = "Additional Features", icon = Icons.Default.Star) {
                    AdditionalFeaturesCard(navController)
                }
            }

            // Show status message if any
            storeViewModel.message?.let { message ->
                item {
                    Text(
                        text = message,
                        color = if (message.contains("success", ignoreCase = true)) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
// Helper Components
@Composable
fun ProfileSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        content()
    }
}

@Composable
fun ProfileInfoItem(
    icon: ImageVector,
    title: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = valueColor
            )
        }
    }
}