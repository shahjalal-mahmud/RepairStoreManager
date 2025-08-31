package com.example.repairstoremanager.ui.components.stock

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductTypeDropdown(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val productTypes = listOf("Standard", "Service", "Part", "Accessory", "Consumable")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedType,
            onValueChange = {},
            label = { Text("Product Type *") },
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
            productTypes.forEach { type ->
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
fun CategorySection(
    category: String,
    subCategory: String,
    onCategorySelected: (String) -> Unit,
    onSubCategorySelected: (String) -> Unit
) {
    val categories = listOf("Phone", "Charger", "Battery", "Screen", "Accessory", "Tool", "Other")
    val subCategories = remember(category) {
        when (category) {
            "Phone" -> listOf("Smartphone", "Feature Phone", "Tablet", "Other")
            "Charger" -> listOf("Wall Charger", "Car Charger", "Wireless Charger", "Cable", "Other")
            "Battery" -> listOf("Phone Battery", "Power Bank", "Other")
            "Screen" -> listOf("LCD", "OLED", "Touch Screen", "Other")
            "Accessory" -> listOf("Case", "Protector", "Headphones", "Other")
            "Tool" -> listOf("Screwdriver", "Pry Tool", "Tweezers", "Other")
            else -> listOf("Other")
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Category Dropdown
        CategoryDropdown(
            selectedValue = category,
            options = categories,
            label = "Category",
            onValueSelected = onCategorySelected,
            modifier = Modifier.weight(1f)
        )

        // Subcategory Dropdown
        CategoryDropdown(
            selectedValue = subCategory,
            options = subCategories,
            label = "Subcategory",
            onValueSelected = onSubCategorySelected,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selectedValue: String,
    options: List<String>,
    label: String,
    onValueSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            label = { Text(label) },
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
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

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
fun SupplierSection(
    supplier: String,
    unit: String,
    onSupplierChange: (String) -> Unit,
    onUnitChange: (String) -> Unit
) {
    val units = listOf("Piece", "Set", "Pack", "Meter", "Roll", "Other")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = supplier,
            onValueChange = onSupplierChange,
            label = { Text("Supplier") },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium
        )

        UnitDropdown(
            selectedUnit = unit,
            onUnitSelected = onUnitChange,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDropdown(
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val units = listOf("Piece", "Set", "Pack", "Meter", "Roll", "Other")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedUnit,
            onValueChange = {},
            label = { Text("Unit") },
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
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PricingSection(
    cost: String,
    buyingPrice: String,
    sellingPrice: String,
    onCostChange: (String) -> Unit,
    onBuyingPriceChange: (String) -> Unit,
    onSellingPriceChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Pricing Information",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = cost,
                onValueChange = onCostChange,
                label = { Text("Cost Price") },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = buyingPrice,
                onValueChange = onBuyingPriceChange,
                label = { Text("Buying Price") },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            )
        }

        OutlinedTextField(
            value = sellingPrice,
            onValueChange = onSellingPriceChange,
            label = { Text("Selling Price *") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
    }
}

@Composable
fun WarrantySection(
    hasWarranty: Boolean,
    warrantyDuration: String,
    warrantyType: String,
    onWarrantyToggle: (Boolean) -> Unit,
    onWarrantyDurationChange: (String) -> Unit,
    onWarrantyTypeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = hasWarranty,
                onCheckedChange = onWarrantyToggle
            )
            Text(
                "Has Warranty/Guarantee",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (hasWarranty) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = warrantyDuration,
                    onValueChange = onWarrantyDurationChange,
                    label = { Text("Duration") },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                )

                WarrantyTypeDropdown(
                    selectedType = warrantyType,
                    onTypeSelected = onWarrantyTypeChange,
                    modifier = Modifier.weight(1f)
                )
            }
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
    val warrantyTypes = listOf("month", "months", "year", "years", "day", "days")

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