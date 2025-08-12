package com.example.repairstoremanager.ui.components.customer.add

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun SuggestionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    commonItems: Set<String>,
    userItems: Set<String>,
    onAddUserItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdown by remember { mutableStateOf(false) }
    var newItem by remember { mutableStateOf("") }
    val allItems = remember { (commonItems + userItems).toList() }
    val focusManager = LocalFocusManager.current

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                showDropdown = false
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Row {
                    if (value.isNotBlank()) {
                        IconButton(onClick = { onValueChange("") }) {
                            Icon(Icons.Default.Close, "Clear")
                        }
                    }
                    IconButton(onClick = { showDropdown = true }) {
                        Icon(Icons.Default.Add, "Add from list")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (value.isNotBlank()) {
                        onAddUserItem(value)
                        focusManager.clearFocus()
                    }
                }
            )
        )

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            // Common items section
            DropdownMenuItem(
                text = { Text("Common Items", fontWeight = FontWeight.Bold) },
                onClick = {}
            )
            commonItems.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onValueChange(if (value.isEmpty()) item else "$value, $item")
                        showDropdown = false
                    }
                )
            }

            // User items section
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            DropdownMenuItem(
                text = { Text("Your Items", fontWeight = FontWeight.Bold) },
                onClick = {}
            )
            userItems.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onValueChange(if (value.isEmpty()) item else "$value, $item")
                        showDropdown = false
                    }
                )
            }

            // Add new item section
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newItem,
                    onValueChange = { newItem = it },
                    label = { Text("Add new item") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (newItem.isNotBlank()) {
                            onAddUserItem(newItem)
                            onValueChange(if (value.isEmpty()) newItem else "$value, $newItem")
                            newItem = ""
                            showDropdown = false
                        }
                    }
                ) {
                    Icon(Icons.Default.Check, "Add")
                }
            }
        }
    }
}