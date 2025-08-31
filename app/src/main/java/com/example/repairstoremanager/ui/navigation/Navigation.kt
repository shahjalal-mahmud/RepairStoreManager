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
import com.example.repairstoremanager.ui.screens.EditCustomerScreen
import com.example.repairstoremanager.ui.screens.ForgotPasswordScreen
import com.example.repairstoremanager.ui.screens.LoginScreen
import com.example.repairstoremanager.ui.screens.ProfileScreen
import com.example.repairstoremanager.ui.screens.QuickInvoiceScreen
import com.example.repairstoremanager.ui.stock.AddProductScreen
import com.example.repairstoremanager.ui.stock.AddTransactionScreen
import com.example.repairstoremanager.ui.stock.EditProductScreen
import com.example.repairstoremanager.ui.stock.StockListScreen
import com.example.repairstoremanager.ui.stock.TransactionScreen
import com.example.repairstoremanager.viewmodel.CustomerViewModel
import com.example.repairstoremanager.viewmodel.EditCustomerViewModel
import com.example.repairstoremanager.viewmodel.LoginViewModel
import com.example.repairstoremanager.viewmodel.StockViewModel
import com.example.repairstoremanager.viewmodel.StoreViewModel
import com.example.repairstoremanager.viewmodel.TransactionViewModel

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun Navigation(
    navController: NavHostController,
    storeViewModel: StoreViewModel,
    stockViewModel: StockViewModel,
    transactionViewModel: TransactionViewModel,
    customerViewModel: CustomerViewModel
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
            MainScaffold(navController) {
                DashboardScreen(
                    onNavigateToQuickInvoice = {
                        navController.navigate("sales")
                    },
                    navController = navController
                )
            }
        }

        composable(BottomNavItem.AddCustomer.route) {
            MainScaffold(navController) { AddCustomerScreen() }
        }

        composable(BottomNavItem.CustomerList.route) {
            MainScaffold(navController) { CustomerListScreen(navController) }
        }

        composable(BottomNavItem.Stock.route) {
            MainScaffold(navController) { StockListScreen(navController, stockViewModel)}
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

        composable("quick_invoice") {
            MainScaffold(navController) {
                QuickInvoiceScreen(
                    onClose = { navController.popBackStack() }
                )
            }
        }

        // ✅ Sales and Transactions routes - UPDATED
        composable("sales") {
            MainScaffold(navController) {
                AddTransactionScreen(
                    transactionViewModel = transactionViewModel,
                    stockViewModel = stockViewModel,
                    onNavigateToTransactions = { navController.navigate("transactions") }
                )
            }
        }

        composable("transactions") {
            MainScaffold(navController) {
                TransactionScreen(
                    transactionViewModel = transactionViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable("edit_customer/{customerId}") { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
            val editCustomerViewModel = remember { EditCustomerViewModel() }
            MainScaffold(navController) {
                EditCustomerScreen(
                    customerId = customerId,
                    navController = navController,
                    viewModel = editCustomerViewModel
                )
            }
        }

        // ✅ Stock management routes
        composable("stock_list") {
            MainScaffold(navController) {
                StockListScreen(
                    navController = navController,
                    viewModel = stockViewModel
                )
            }
        }

        composable("add_product") {
            MainScaffold(navController) {
                AddProductScreen(
                    navController = navController,
                    viewModel = stockViewModel
                )
            }
        }

        composable("edit_product/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            MainScaffold(navController) {
                EditProductScreen(
                    navController = navController,
                    viewModel = stockViewModel,
                    productId = productId
                )
            }
        }
    }
}