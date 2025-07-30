package com.example.repairstoremanager.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.repairstoremanager.ui.components.StoreInfoSection
import com.example.repairstoremanager.viewmodel.StoreViewModel

@Composable
fun ProfileScreen(
    navController: NavHostController,
    storeViewModel: StoreViewModel,
    onLogout: () -> Unit
) {
    StoreInfoSection(
        viewModel = storeViewModel,
        onLogout = onLogout
    )
}