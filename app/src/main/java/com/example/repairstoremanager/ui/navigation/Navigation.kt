package com.example.repairstoremanager.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.repairstoremanager.data.repository.AuthRepository
import com.example.repairstoremanager.ui.screens.customer.AddCustomerScreen
import com.example.repairstoremanager.ui.screens.delivery.AllDeliveriesScreen
import com.example.repairstoremanager.ui.screens.customer.CustomerListScreen
import com.example.repairstoremanager.ui.screens.main.DashboardScreen
import com.example.repairstoremanager.ui.screens.customer.EditCustomerScreen
import com.example.repairstoremanager.ui.screens.auth.ForgotPasswordScreen
import com.example.repairstoremanager.ui.screens.auth.LoginScreen
import com.example.repairstoremanager.ui.screens.common.NotificationsScreen
import com.example.repairstoremanager.ui.screens.main.ProfileScreen
import com.example.repairstoremanager.ui.screens.common.QuickInvoiceScreen
import com.example.repairstoremanager.ui.screens.common.SearchScreen
import com.example.repairstoremanager.ui.screens.common.SettingsScreen
import com.example.repairstoremanager.ui.screens.delivery.TodayDeliveriesScreen
import com.example.repairstoremanager.ui.screens.delivery.TomorrowDeliveriesScreen
import com.example.repairstoremanager.ui.screens.stock.AddProductScreen
import com.example.repairstoremanager.ui.screens.transaction.AddTransactionScreen
import com.example.repairstoremanager.ui.screens.stock.EditProductScreen
import com.example.repairstoremanager.ui.screens.stock.StockListScreen
import com.example.repairstoremanager.ui.screens.transaction.TransactionScreen
import com.example.repairstoremanager.viewmodel.CustomerViewModel
import com.example.repairstoremanager.viewmodel.EditCustomerViewModel
import com.example.repairstoremanager.viewmodel.LoginViewModel
import com.example.repairstoremanager.viewmodel.SearchViewModel
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
    customerViewModel: CustomerViewModel,
    searchViewModel: SearchViewModel
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
                    viewModel = customerViewModel,
                    storeViewModel = storeViewModel,
                    navController = navController,
                    onNavigateToQuickInvoice = {
                        navController.navigate("sales")
                    },
                    onNavigateToProfile = {
                        navController.navigate(BottomNavItem.Profile.route)
                    },
                    onNavigateToNotifications = {
                        navController.navigate("notifications")
                    },
                    onNavigateToSearch = {
                        navController.navigate("search")
                    },
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    }
                )
            }
        }

        composable("notifications") {
            MainScaffold(navController) {
                NotificationsScreen(navController)
            }
        }

        composable("search") {
            MainScaffold(navController) {
                SearchScreen(navController, customerViewModel, searchViewModel)
            }
        }

        composable("settings") {
            MainScaffold(navController) {
                SettingsScreen(navController)
            }
        }

        composable("today_deliveries") {
            MainScaffold(navController) {
                TodayDeliveriesScreen(navController)
            }
        }

        composable("tomorrow_deliveries") {
            MainScaffold(navController) {
                TomorrowDeliveriesScreen(navController)
            }
        }

        composable("all_deliveries") {
            MainScaffold(navController) {
                AllDeliveriesScreen(navController)
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