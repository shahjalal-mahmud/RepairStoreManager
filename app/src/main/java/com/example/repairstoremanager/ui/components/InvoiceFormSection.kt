package com.example.repairstoremanager.ui.components

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    val viewModel: CustomerViewModel = viewModel()
    val context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📅 Date: $date", style = MaterialTheme.typography.bodySmall)
                Text("📄 Invoice No: $invoiceNumber", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("👤 Customer Info")
        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("Customer Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = contactNumber,
            onValueChange = { contactNumber = it },
            label = { Text("Contact Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("📱 Device Info")
        OutlinedTextField(
            value = phoneModel,
            onValueChange = { phoneModel = it },
            label = { Text("Phone Model") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = problem,
            onValueChange = { problem = it },
            label = { Text("Problem Description") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = deliveryDate,
            onValueChange = { deliveryDate = it },
            label = { Text("Expected Delivery Date") },
            placeholder = { Text("dd-mm-yyyy") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("💳 Payment Info")
        OutlinedTextField(
            value = totalAmount,
            onValueChange = { totalAmount = it },
            label = { Text("Total Amount (৳)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = advanced,
            onValueChange = { advanced = it },
            label = { Text("Advanced Paid (৳)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("🔐 Security Type")
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = securityType == "Password", onClick = { securityType = "Password" })
            Text("Password", modifier = Modifier.padding(end = 16.dp))
            RadioButton(selected = securityType == "Pattern", onClick = { securityType = "Pattern" })
            Text("Pattern")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (securityType == "Password") {
            PasswordField(
                value = phonePassword,
                onValueChange = { phonePassword = it },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            PatternLockCanvas(
                onPatternComplete = { drawnPattern -> pattern = drawnPattern },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            if (pattern.isNotEmpty()) {
                Text(
                    text = "Pattern: ${pattern.joinToString(" → ")}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("📦 Accessories & Consent")
        AccessoriesRow("Battery", battery) { battery = it }
        AccessoriesRow("SIM", sim) { sim = it }
        AccessoriesRow("Memory", memory) { memory = it }
        AccessoriesRow("SIM Tray", simTray) { simTray = it }
        AccessoriesRow("Back Cover", backCover) { backCover = it }
        AccessoriesRow("Dead Permission", deadPermission) { deadPermission = it }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val customer = Customer(
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
                        Toast.makeText(context, "Customer saved!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("💾 Save Invoice", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
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
