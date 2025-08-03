package com.example.repairstoremanager.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

    // Permission Launcher
    val simPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            storeViewModel.loadSimList(context)
        }
    }

    val hasPhoneStatePermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_PHONE_STATE
    ) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(Unit) {
        if (hasPhoneStatePermission) {
            storeViewModel.loadSimList(context)
        } else {
            simPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            StoreInfoSection(
                viewModel = storeViewModel,
                onLogout = onLogout
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Delivery Reminder",
                    style = MaterialTheme.typography.titleMedium
                )
                ReminderTimePicker()
            }

            // ✅ Auto SMS Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Auto SMS")
                Switch(
                    checked = storeViewModel.autoSmsEnabled,
                    onCheckedChange = { storeViewModel.updateAutoSmsEnabled(it) }
                )
            }

            // ✅ SIM Selection
            if (storeViewModel.simList.isNotEmpty()) {
                Text(
                    text = "Preferred SIM for SMS:",
                    style = MaterialTheme.typography.titleMedium
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
                        Text(text = sim.displayName?.toString() ?: "SIM ${index + 1}")
                    }
                }
            }
        }
    }
}
