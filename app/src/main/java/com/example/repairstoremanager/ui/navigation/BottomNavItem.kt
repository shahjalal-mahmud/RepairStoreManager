package com.example.repairstoremanager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : BottomNavItem("dashboard", "Dashboard", Icons.Filled.Home)
    object AddCustomer : BottomNavItem("add_customer", "Add", Icons.Filled.Add)
    object CustomerList : BottomNavItem("customer_list", "Customers",
        Icons.AutoMirrored.Filled.List
    )
    object Stock : BottomNavItem("stock_list", "Stock", Icons.Filled.Inventory)
    object Profile : BottomNavItem("profile", "Profile", Icons.Filled.Person)
}