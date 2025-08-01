package com.example.repairstoremanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        }
    }
}