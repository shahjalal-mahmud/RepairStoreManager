package com.example.repairstoremanager.ui.components.customer.list

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.ui.components.customer.common.AccessoriesBadges
import com.example.repairstoremanager.ui.components.customer.common.AccessoryCheckboxes
import com.example.repairstoremanager.ui.components.customer.invoice.InvoicePrintBottomSheet
import com.example.repairstoremanager.ui.components.customer.add.PatternLockCanvas
import com.example.repairstoremanager.viewmodel.CustomerViewModel
import com.example.repairstoremanager.viewmodel.StoreViewModel

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun CustomerCard(customer: Customer, viewModel: CustomerViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val storeViewModel: StoreViewModel = viewModel()

    // Editable Fields
    var name by remember { mutableStateOf(customer.customerName) }
    var phone by remember { mutableStateOf(customer.contactNumber) }
    var model by remember { mutableStateOf(customer.phoneModel) }
    var problem by remember { mutableStateOf(customer.problem) }
    var delivery by remember { mutableStateOf(customer.deliveryDate) }
    var total by remember { mutableStateOf(customer.totalAmount) }
    var paid by remember { mutableStateOf(customer.advanced) }

    var battery by remember { mutableStateOf(customer.battery) }
    var sim by remember { mutableStateOf(customer.sim) }
    var memory by remember { mutableStateOf(customer.memory) }
    var simTray by remember { mutableStateOf(customer.simTray) }
    var backCover by remember { mutableStateOf(customer.backCover) }
    var deadPermission by remember { mutableStateOf(customer.deadPermission) }

    val statusOptions = listOf("Pending", "Repaired", "Delivered", "Cancelled")
    val selectedStatus = customer.status
    val statusColor = statusToColor(selectedStatus)

    var showPrintSheet by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isEditing) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = "\uD83D\uDC64 ${customer.customerName}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                }

                StatusDropdown(
                    selectedStatus = selectedStatus,
                    options = statusOptions,
                    onStatusChange = { newStatus ->
                        viewModel.updateCustomerStatus(
                            customerId = customer.id,
                            newStatus = newStatus,
                            customer = customer,
                            context = context,
                            simSlotIndex = storeViewModel.selectedSimSlot,
                            autoSmsEnabled = storeViewModel.autoSmsEnabled
                        )
                    },
                    statusColor = statusColor
                )
            }

            // Editable Fields
            if (isEditing) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Phone Model") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = problem, onValueChange = { problem = it }, label = { Text("Problem") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Contact") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = paid, onValueChange = { paid = it }, label = { Text("Paid Amount") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = total, onValueChange = { total = it }, label = { Text("Total Amount") }, modifier = Modifier.fillMaxWidth())
                    if (selectedStatus in listOf("Pending", "Repaired")) {
                        OutlinedTextField(value = delivery, onValueChange = { delivery = it }, label = { Text("Delivery Date") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("\uD83D\uDCF1 ${customer.phoneModel}", style = MaterialTheme.typography.bodyMedium)
                    Text("\uD83D\uDEE0️ Problem: ${customer.problem}", style = MaterialTheme.typography.bodySmall)
                    Text("\uD83D\uDCDE Contact: ${customer.contactNumber}", style = MaterialTheme.typography.bodySmall)
                    Text("\uD83D\uDCB3 Paid: ${customer.advanced} / Total: ${customer.totalAmount}", style = MaterialTheme.typography.bodySmall)
                    if (selectedStatus in listOf("Pending", "Repaired")) {
                        Text("\uD83D\uDCE6 Delivery: ${customer.deliveryDate}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Text("\uD83D\uDD52 Created: ${customer.date}", style = MaterialTheme.typography.labelSmall)
            Text("📄 Invoice No: ${customer.invoiceNumber}", style = MaterialTheme.typography.bodySmall)

            // Accessories
            if (isEditing) {
                AccessoryCheckboxes(
                    battery, { battery = it },
                    sim, { sim = it },
                    memory, { memory = it },
                    simTray, { simTray = it },
                    backCover, { backCover = it },
                    deadPermission, { deadPermission = it }
                )
            } else {
                AccessoriesBadges(
                    battery = customer.battery,
                    sim = customer.sim,
                    memory = customer.memory,
                    simTray = customer.simTray,
                    backCover = customer.backCover,
                    deadPermission = customer.deadPermission
                )
            }

            // Security Info Toggle
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "\uD83D\uDD12 Hide Security Info" else "\uD83D\uDD13 Show Security Info")
            }

            if (expanded) {
                if (customer.securityType == "Password") {
                    Text("\uD83D\uDD11 Password: ${customer.phonePassword}", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("\uD83D\uDD10 Pattern:", style = MaterialTheme.typography.bodySmall)
                    PatternLockCanvas(
                        pattern = customer.pattern,
                        isInteractive = false,
                        isPreview = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = DividerDefaults.color
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                if (isEditing) {
                    TextButton(
                        onClick = {
                            val updated = customer.copy(
                                customerName = name,
                                contactNumber = phone,
                                phoneModel = model,
                                problem = problem,
                                deliveryDate = delivery,
                                advanced = paid,
                                totalAmount = total,
                                battery = battery,
                                sim = sim,
                                memory = memory,
                                simTray = simTray,
                                backCover = backCover,
                                deadPermission = deadPermission
                            )
                            viewModel.updateCustomer(
                                updatedCustomer = updated,
                                context = context,
                                onSuccess = { isEditing = false },
                                onError = {}
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("💾 Save")
                    }
                    TextButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("❌ Cancel")
                    }
                } else {
                    TextButton(onClick = { isEditing = true }, modifier = Modifier.weight(1f)) {
                        Text("✏️ Edit")
                    }
                    TextButton(onClick = { showPrintSheet = true }, modifier = Modifier.weight(1f)) {
                        Text("🖨 Print")
                    }
                }
            }

            if (showPrintSheet) {
                InvoicePrintBottomSheet(
                    customer = customer,
                    storeInfo = storeViewModel.storeInfo,
                    onDismiss = { showPrintSheet = false }
                )
            }
        }
    }
}

@Composable
fun StatusDropdown(
    selectedStatus: String,
    options: List<String>,
    onStatusChange: (String) -> Unit,
    statusColor: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.textButtonColors(containerColor = statusColor),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.defaultMinSize(minHeight = 32.dp)
        ) {
            Text(
                text = selectedStatus,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status) },
                    onClick = {
                        onStatusChange(status)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun statusToColor(status: String): Color {
    return when (status) {
        "Pending" -> MaterialTheme.colorScheme.outline
        "Repaired" -> MaterialTheme.colorScheme.primary
        "Delivered" -> MaterialTheme.colorScheme.primary.copy(green = 0.8f)
        "Cancelled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }
}