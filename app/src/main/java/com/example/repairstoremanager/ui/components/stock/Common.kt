package com.example.repairstoremanager.ui.components.stock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuantitySection(
    quantity: String,
    alertQuantity: String,
    onQuantityChange: (String) -> Unit,
    onAlertQuantityChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = quantity,
            onValueChange = onQuantityChange,
            label = { Text("Quantity *") },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = alertQuantity,
            onValueChange = onAlertQuantityChange,
            label = { Text("Alert Quantity") },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium
        )
    }
}

@Composable
fun PricingSection(
    buyingPrice: String,
    sellingPrice: String,
    onBuyingPriceChange: (String) -> Unit,
    onSellingPriceChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ Both fields in a single Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = buyingPrice,
                onValueChange = onBuyingPriceChange,
                label = { Text("Buying Price") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = sellingPrice,
                onValueChange = onSellingPriceChange,
                label = { Text("Selling Price *") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
        }
    }
}

@Composable
fun WarrantySection(
    hasWarranty: Boolean,
    warrantyDuration: String,
    warrantyType: String,
    onWarrantyToggle: (Boolean) -> Unit,
    onWarrantyDurationChange: (String) -> Unit,
    onWarrantyTypeChange: (String) -> Unit,
    title: String = "Warranty" // 👈 add default title
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = hasWarranty,
                onCheckedChange = onWarrantyToggle
            )
            Text(title) // 👈 dynamic label
        }

        if (hasWarranty) {
            OutlinedTextField(
                value = warrantyDuration,
                onValueChange = onWarrantyDurationChange,
                label = { Text("$title Duration") }, // 👈 dynamic
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            WarrantyTypeDropdown(
                selectedType = warrantyType,
                onTypeSelected = onWarrantyTypeChange
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarrantyTypeDropdown(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val warrantyTypes = listOf("months", "years", "days")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedType,
            onValueChange = {},
            label = { Text("Warranty Type") },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = MaterialTheme.shapes.medium
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            warrantyTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DropdownMenuBox(
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { expanded = true },
            modifier = Modifier.size(36.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                Icons.Default.AddPhotoAlternate,
                contentDescription = "Add Photo",
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Choose from Gallery") },
                onClick = {
                    expanded = false
                    onGalleryClick()
                }
            )
            DropdownMenuItem(
                text = { Text("Take Photo") },
                onClick = {
                    expanded = false
                    onCameraClick()
                }
            )
        }
    }
}