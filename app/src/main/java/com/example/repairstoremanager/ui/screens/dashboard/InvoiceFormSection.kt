package com.example.repairstoremanager.ui.screens.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.ui.components.PasswordField
import com.example.repairstoremanager.ui.components.PatternLockCanvas
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SimpleDateFormat")
@Composable
fun InvoiceFormSection() {
    val date = remember {
        SimpleDateFormat("dd MMM yyyy, hh:mm a").format(Date())
    }
    val invoiceNumber = "INV-${System.currentTimeMillis().toString().takeLast(5)}"

    var customerName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var phoneModel by remember { mutableStateOf("") }
    var problem by remember { mutableStateOf("") }
    var deliveryDate by remember { mutableStateOf("") }
    var advanced by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("") }
    var securityType by remember { mutableStateOf("Password") }
    var phonePassword by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf(listOf<Int>()) }

    var battery by remember { mutableStateOf(false) }
    var sim by remember { mutableStateOf(false) }
    var memory by remember { mutableStateOf(false) }
    var simTray by remember { mutableStateOf(false) }
    var backCover by remember { mutableStateOf(false) }
    var deadPermission by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("🗓️ Date & Time: $date")
        Text("📄 Invoice No: $invoiceNumber", modifier = Modifier.padding(bottom = 8.dp))

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
            label = { Text("Delivery Date") },
            placeholder = { Text("dd-mm-yyyy") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = advanced,
                onValueChange = { advanced = it },
                label = { Text("Advanced") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = due,
                onValueChange = { due = it },
                label = { Text("Due") },
                modifier = Modifier.weight(1f)
            )
        }

        Text("🔐 Select Unlock Type:")
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = securityType == "Password",
                onClick = { securityType = "Password" }
            )
            Text("Password")

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(
                selected = securityType == "Pattern",
                onClick = { securityType = "Pattern" }
            )
            Text("Pattern")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (securityType == "Password") {
            PasswordField(
                value = phonePassword,
                onValueChange = { phonePassword = it },
                modifier = Modifier.fillMaxWidth()
            )
        }else {
            PatternLockCanvas(
                onPatternComplete = { drawnPattern ->
                    pattern = drawnPattern
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (pattern.isNotEmpty()) {
                Text("Pattern: ${pattern.joinToString(" -> ")}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Accessories / Consent:")
        Row {
            Checkbox(checked = battery, onCheckedChange = { battery = it })
            Text("Battery", modifier = Modifier.padding(top = 12.dp))

            Checkbox(checked = sim, onCheckedChange = { sim = it })
            Text("SIM", modifier = Modifier.padding(top = 12.dp))
        }

        Row {
            Checkbox(checked = memory, onCheckedChange = { memory = it })
            Text("Memory")

            Checkbox(checked = simTray, onCheckedChange = { simTray = it })
            Text("SIM Tray")
        }

        Row {
            Checkbox(checked = backCover, onCheckedChange = { backCover = it })
            Text("Back Cover")

            Checkbox(checked = deadPermission, onCheckedChange = { deadPermission = it })
            Text("Dead Permission")
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { /* Save to Firestore later */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Save Invoice")
        }
    }
}