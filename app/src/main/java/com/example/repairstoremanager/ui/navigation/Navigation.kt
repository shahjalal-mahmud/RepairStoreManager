package com.example.repairstoremanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.repairstoremanager.ui.screens.*
import com.example.repairstoremanager.viewmodel.LoginViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    loginViewModel: LoginViewModel
) {
    NavHost(navController = navController, startDestination = "dashboard") {

        composable("login") {
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
            MainScaffold(navController) { ProfileScreen() }
        }
    }
}