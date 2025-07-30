package com.example.repairstoremanager.ui.components

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.viewmodel.CustomerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SimpleDateFormat")
@Composable
fun InvoiceFormSection() {
    val scrollState = rememberScrollState()
    val date = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a").format(Date()) }
    val invoiceNumber = "INV-${System.currentTimeMillis().toString().takeLast(5)}"

    val context = LocalContext.current
    val viewModel: CustomerViewModel = viewModel()

    // Form state
    var customerName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var phoneModel by remember { mutableStateOf("") }
    var problem by remember { mutableStateOf("") }
    var deliveryDate by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var advanced by remember { mutableStateOf("") }

    var securityType by remember { mutableStateOf("Password") }
    var phonePassword by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf(listOf<Int>()) }

    var battery by remember { mutableStateOf(false) }
    var sim by remember { mutableStateOf(false) }
    var memory by remember { mutableStateOf(false) }
    var simTray by remember { mutableStateOf(false) }
    var backCover by remember { mutableStateOf(false) }
    var deadPermission by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var resetTrigger by remember { mutableIntStateOf(0) }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (resetTrigger % 2 == 0) 1f else 0f,
        animationSpec = tween(300)
    )

    fun clearForm() {
        customerName = ""
        contactNumber = ""
        phoneModel = ""
        problem = ""
        deliveryDate = ""
        totalAmount = ""
        advanced = ""
        securityType = "Password"
        phonePassword = ""
        pattern = emptyList()
        battery = false
        sim = false
        memory = false
        simTray = false
        backCover = false
        deadPermission = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .alpha(animatedAlpha)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("📅 Date: $date", style = MaterialTheme.typography.labelMedium)
                Text("📄 Invoice No: $invoiceNumber", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(Modifier.height(20.dp))

        SectionTitle("👤 Customer Info")
        CustomTextField("Customer Name", customerName) { customerName = it }
        CustomTextField("Contact Number", contactNumber, KeyboardType.Phone) { contactNumber = it }

        Spacer(Modifier.height(20.dp))

        SectionTitle("📱 Device Info")
        CustomTextField("Phone Model", phoneModel) { phoneModel = it }
        CustomTextField("Problem Description", problem) { problem = it }
        CustomTextField("Expected Delivery Date", deliveryDate) { deliveryDate = it }

        Spacer(Modifier.height(20.dp))

        SectionTitle("💳 Payment Info")
        CustomTextField("Total Amount (৳)", totalAmount, KeyboardType.Number) { totalAmount = it }
        CustomTextField("Advanced Paid (৳)", advanced, KeyboardType.Number) { advanced = it }

        Spacer(Modifier.height(20.dp))

        SectionTitle("🔐 Security Type")
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = securityType == "Password", onClick = { securityType = "Password" })
            Text("Password", Modifier.padding(end = 16.dp))
            RadioButton(selected = securityType == "Pattern", onClick = { securityType = "Pattern" })
            Text("Pattern")
        }

        Spacer(Modifier.height(12.dp))

        if (securityType == "Password") {
            PasswordField(
                value = phonePassword,
                onValueChange = { phonePassword = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (securityType == "Pattern") {
            Column {
                Text(
                    "Draw your pattern (minimum 4 dots)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                PatternLockCanvas(
                    onPatternComplete = { drawn -> pattern = drawn },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
        }

        if (securityType == "Pattern" && pattern.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Your pattern preview:", style = MaterialTheme.typography.labelMedium)
            PatternLockCanvas(
                pattern = pattern,
                isInteractive = false,
                isPreview = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Text(
                "Pattern: ${pattern.joinToString(" → ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(20.dp))

        SectionTitle("📦 Accessories & Consent")
        AccessoriesRow("Battery", battery) { battery = it }
        AccessoriesRow("SIM", sim) { sim = it }
        AccessoriesRow("Memory", memory) { memory = it }
        AccessoriesRow("SIM Tray", simTray) { simTray = it }
        AccessoriesRow("Back Cover", backCover) { backCover = it }
        AccessoriesRow("Dead Permission", deadPermission) { deadPermission = it }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                isLoading = true
                val customer = Customer(
                    id = "", // or generate UUID.randomUUID().toString()
                    shopOwnerId = "", // fill if needed
                    invoiceNumber = invoiceNumber,
                    date = date,
                    customerName = customerName,
                    contactNumber = contactNumber,
                    phoneModel = phoneModel,
                    problem = problem,
                    deliveryDate = deliveryDate,
                    totalAmount = totalAmount,
                    advanced = advanced,
                    securityType = securityType,
                    phonePassword = phonePassword,
                    pattern = pattern,
                    battery = battery,
                    sim = sim,
                    memory = memory,
                    simTray = simTray,
                    backCover = backCover,
                    deadPermission = deadPermission
                )
                viewModel.addCustomer(
                    customer,
                    onSuccess = {
                        isLoading = false
                        Toast.makeText(context, "Customer saved!", Toast.LENGTH_SHORT).show()
                        resetTrigger++
                        clearForm()
                        resetTrigger++
                    },
                    onError = {
                        isLoading = false
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("💾 Save Invoice", fontSize = 18.sp)
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun AccessoriesRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 4.dp, bottom = 4.dp)
            .fillMaxWidth()
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}
@Composable
fun CustomTextField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}
