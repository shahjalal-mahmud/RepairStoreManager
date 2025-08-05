package com.example.repairstoremanager.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.repairstoremanager.data.repository.AuthRepository
import com.example.repairstoremanager.ui.screens.AddCustomerScreen
import com.example.repairstoremanager.ui.screens.CustomerListScreen
import com.example.repairstoremanager.ui.screens.DashboardScreen
import com.example.repairstoremanager.ui.screens.ForgotPasswordScreen
import com.example.repairstoremanager.ui.screens.LoginScreen
import com.example.repairstoremanager.ui.screens.ProfileScreen
import com.example.repairstoremanager.viewmodel.LoginViewModel
import com.example.repairstoremanager.viewmodel.StoreViewModel

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun Navigation(
    navController: NavHostController,
    storeViewModel: StoreViewModel
) {
    val authRepository = remember { AuthRepository() }

    // ✅ Decide start destination based on login status
    val startDestination = if (authRepository.isUserLoggedIn()) {
        BottomNavItem.Dashboard.route
    } else {
        "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable("login") {
            val loginViewModel = remember { LoginViewModel(storeViewModel) }
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(BottomNavItem.Dashboard.route) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate("forgot_password")
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
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
        composable("forgot_password") {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() }
            )
        }

    }
}