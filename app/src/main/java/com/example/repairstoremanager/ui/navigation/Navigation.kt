package com.example.repairstoremanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.repairstoremanager.ui.screens.AddCustomerScreen
import com.example.repairstoremanager.ui.screens.CustomerListScreen
import com.example.repairstoremanager.ui.screens.DashboardScreen
import com.example.repairstoremanager.ui.screens.LoginScreen
import com.example.repairstoremanager.ui.screens.ProfileScreen
import com.example.repairstoremanager.viewmodel.LoginViewModel
import com.example.repairstoremanager.viewmodel.StoreViewModel

@Composable
fun Navigation(
    navController: NavHostController,
    storeViewModel: StoreViewModel
) {
    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            val loginViewModel = remember { LoginViewModel(storeViewModel) } // ✅ Inject dependency
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(BottomNavItem.Dashboard.route) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable(BottomNavItem.Dashboard.route) {
            MainScaffold(navController) { DashboardScreen() }
        }

        composable(BottomNavItem.AddCustomer.route) {
            MainScaffold(navController) { AddCustomerScreen() }
        }

        composable(BottomNavItem.CustomerList.route) {
            MainScaffold(navController) { CustomerListScreen() }
        }

        composable(BottomNavItem.Profile.route) {
            MainScaffold(navController) {
                ProfileScreen(
                    navController = navController,
                    storeViewModel = storeViewModel,
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true } // ✅ Clear backstack on logout
                        }
                    }
                )
            }
        }
    }
}
