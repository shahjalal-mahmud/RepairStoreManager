package com.example.repairstoremanager.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceInfoSection(
    phoneModel: String,
    problem: String,
    deliveryDate: String,
    onPhoneModelChange: (String) -> Unit,
    onProblemChange: (String) -> Unit,
    onDeliveryDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionTitle("📱 Device Info")
        CustomTextField("Phone Model", phoneModel, onValueChange = onPhoneModelChange)
        CustomTextField("Problem Description", problem, onValueChange = onProblemChange)

        OutlinedTextField(
            value = deliveryDate,
            onValueChange = {}, // Read-only
            label = { Text("Expected Delivery Date") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = onDeliveryDateClick) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Date"
                    )
                }
            }
        )
    }
}