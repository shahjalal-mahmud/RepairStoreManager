package com.example.repairstoremanager.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repairstoremanager.data.model.Customer

@Composable
fun InvoicePrintPreview(customer: Customer) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Repair Store Manager", fontSize = 20.sp)
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            Text("Invoice No: ${customer.invoiceNumber}")
            Text("Date: ${customer.date}")
            Spacer(Modifier.height(8.dp))
            Text("Customer: ${customer.customerName}")
            Text("Phone: ${customer.contactNumber}")
            Text("Model: ${customer.phoneModel}")
            Text("Problem: ${customer.problem}")
            Text("Delivery: ${customer.deliveryDate}")
            Spacer(Modifier.height(8.dp))
            Text("Total: ৳${customer.totalAmount}")
            Text("Advance: ৳${customer.advanced}")
            Text("Due: ৳${
                (customer.totalAmount.toIntOrNull() ?: 0) -
                        (customer.advanced.toIntOrNull() ?: 0)
            }")
            Spacer(Modifier.height(8.dp))
            Text("Accessories:")
            if (customer.battery) Text("✔ Battery")
            if (customer.sim) Text("✔ SIM")
            if (customer.memory) Text("✔ Memory")
            if (customer.simTray) Text("✔ SIM Tray")
            if (customer.backCover) Text("✔ Back Cover")
            if (customer.deadPermission) Text("☠ Permission for Dead Risk")
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )
            Text("Thank you!", fontSize = 18.sp)
        }
    }
}
