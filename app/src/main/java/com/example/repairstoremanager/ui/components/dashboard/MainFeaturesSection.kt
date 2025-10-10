package com.example.repairstoremanager.ui.components.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.automirrored.outlined.Note
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun MainFeaturesSection(navController: NavHostController) {
    var showDeliveryDialog by remember { mutableStateOf(false) }

    val features = listOf(
        FeatureItem("Delivery Options", Icons.Outlined.DeliveryDining, "delivery_options"),
        FeatureItem("Add Customer", Icons.Outlined.AddCircle, "add_customer"),
        FeatureItem("Customer List", Icons.AutoMirrored.Outlined.ListAlt, "customer_list"),
        FeatureItem("Stock Management", Icons.Outlined.Inventory, "stock_list"),
        FeatureItem("Daily Records", Icons.Outlined.Receipt, "records"),
        FeatureItem("Sales", Icons.Outlined.Payment, "sales"),
        FeatureItem("Quick Invoice", Icons.Outlined.PointOfSale, "quick_invoice"),
        FeatureItem("Notes", Icons.AutoMirrored.Outlined.Note, "notes"),
        FeatureItem("All Transactions", Icons.Outlined.Payment, "transactions"),
    )

    // Delivery options for the dialog
    val deliveryOptions = listOf(
        FeatureItem("Today's Deliveries", Icons.Outlined.CalendarToday, "today_deliveries"),
        FeatureItem("Tomorrow's Delivery", Icons.Outlined.Schedule, "tomorrow_deliveries"),
        FeatureItem("All Deliveries", Icons.Outlined.LocalShipping, "all_deliveries"),
        FeatureItem("Expired Deliveries", Icons.Outlined.Warning, "expired_deliveries"),
        FeatureItem("Ready for Delivery", Icons.Outlined.AssignmentTurnedIn, "ready_for_delivery")
    )

    // Delivery Options Dialog
    if (showDeliveryDialog) {
        DeliveryOptionsDialog(
            options = deliveryOptions,
            onDismiss = { showDeliveryDialog = false },
            onOptionSelected = { route ->
                showDeliveryDialog = false
                navController.navigate(route)
            }
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(features) { feature ->
            FeatureCard(
                feature = feature,
                onClick = {
                    if (feature.route == "delivery_options") {
                        showDeliveryDialog = true
                    } else {
                        navController.navigate(feature.route)
                    }
                }
            )
        }
    }
}
data class FeatureItem(val title: String, val icon: ImageVector, val route: String)

@Composable
fun FeatureCard(feature: FeatureItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = feature.title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = feature.title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}