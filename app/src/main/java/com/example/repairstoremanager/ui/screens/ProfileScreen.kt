package com.example.repairstoremanager.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.repairstoremanager.ui.components.ReminderTimePicker
import com.example.repairstoremanager.ui.components.StoreInfoSection
import com.example.repairstoremanager.viewmodel.StoreViewModel

@Composable
fun ProfileScreen(
    navController: NavHostController,
    storeViewModel: StoreViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    val simPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) storeViewModel.loadSimList(context)
    }

    val hasPhoneStatePermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_PHONE_STATE
    ) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(Unit) {
        if (hasPhoneStatePermission) {
            storeViewModel.loadSimList(context)
        } else {
            simPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Store Info
        StoreInfoSection(
            viewModel = storeViewModel,
            onLogout = onLogout
        )

        // Delivery Reminder Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Delivery Reminder",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                ReminderTimePicker()
            }
        }

        // Auto SMS Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Enable Auto SMS", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = storeViewModel.autoSmsEnabled,
                    onCheckedChange = { storeViewModel.updateAutoSmsEnabled(it) }
                )
            }
        }

        // SIM Selection
        if (storeViewModel.simList.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Preferred SIM for SMS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    storeViewModel.simList.forEachIndexed { index, sim ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = storeViewModel.selectedSimSlot == index,
                                onClick = { storeViewModel.selectedSimSlot = index }
                            )
                            Text(
                                text = sim.displayName?.toString() ?: "SIM ${index + 1}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
