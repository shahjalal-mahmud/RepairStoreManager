package com.example.repairstoremanager.ui.components.customer.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.ui.components.customer.common.AccessoriesSection

@Composable
fun EditCustomerContent(
    customer: Customer,
    isLoading: Boolean,
    onUpdateField: (String, Any) -> Unit,
    onSave: (Customer) -> Unit,
    onShowDatePicker: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Track original customer to detect changes
    val originalCustomer = remember { customer.copy() }
    var hasChanges by remember { mutableStateOf(false) }

    // Create a function to handle field updates with change detection
    fun handleFieldUpdate(field: String, value: Any) {
        onUpdateField(field, value)

        // Create the updated customer for comparison
        val updatedCustomer = updateCustomerField(customer, field, value)
        hasChanges = hasCustomerChanged(originalCustomer, updatedCustomer)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Read-only information card
        ReadOnlyInfoCard(customer)

        // Editable fields
        CustomerInfoSection(
            customer = customer,
            onUpdateField = { field, value -> handleFieldUpdate(field, value) }
        )

        // Phone details section
        PhoneDetailsSection(
            customer = customer,
            onUpdateField = { field, value -> handleFieldUpdate(field, value) }
        )

        // Financial section
        FinancialSection(
            customer = customer,
            onUpdateField = { field, value -> handleFieldUpdate(field, value) },
            onShowDatePicker = onShowDatePicker
        )

        // Accessories section
        AccessoriesSection(
            battery = customer.battery,
            sim = customer.sim,
            memory = customer.memory,
            simTray = customer.simTray,
            backCover = customer.backCover,
            deadPermission = customer.deadPermission,
            onBatteryChange = { handleFieldUpdate("battery", it) },
            onSimChange = { handleFieldUpdate("sim", it) },
            onMemoryChange = { handleFieldUpdate("memory", it) },
            onSimTrayChange = { handleFieldUpdate("simTray", it) },
            onBackCoverChange = { handleFieldUpdate("backCover", it) },
            onDeadPermissionChange = { handleFieldUpdate("deadPermission", it) }
        )

        // Action buttons
        ActionButtons(
            isLoading = isLoading,
            hasChanges = hasChanges,
            onSave = { onSave(customer) },
            onCancel = onCancel
        )
    }
}

// Helper function to check if customer has changed
fun hasCustomerChanged(original: Customer, current: Customer): Boolean {
    return original.customerName != current.customerName ||
            original.contactNumber != current.contactNumber ||
            original.phoneModel != current.phoneModel ||
            original.problem != current.problem ||
            original.totalAmount != current.totalAmount ||
            original.advanced != current.advanced ||
            original.deliveryDate != current.deliveryDate ||
            original.battery != current.battery ||
            original.sim != current.sim ||
            original.memory != current.memory ||
            original.simTray != current.simTray ||
            original.backCover != current.backCover ||
            original.deadPermission != current.deadPermission ||
            original.status != current.status
}
fun updateCustomerField(customer: Customer, field: String, value: Any): Customer {
    return when (field) {
        "customerName" -> customer.copy(customerName = value as String)
        "contactNumber" -> customer.copy(contactNumber = value as String)
        "phoneModel" -> customer.copy(phoneModel = value as String)
        "problem" -> customer.copy(problem = value as String)
        "totalAmount" -> customer.copy(totalAmount = value as String)
        "advanced" -> customer.copy(advanced = value as String)
        "deliveryDate" -> customer.copy(deliveryDate = value as String)
        "battery" -> customer.copy(battery = value as Boolean)
        "sim" -> customer.copy(sim = value as Boolean)
        "memory" -> customer.copy(memory = value as Boolean)
        "simTray" -> customer.copy(simTray = value as Boolean)
        "backCover" -> customer.copy(backCover = value as Boolean)
        "deadPermission" -> customer.copy(deadPermission = value as Boolean)
        "status" -> customer.copy(status = value as String)
        else -> customer
    }
}