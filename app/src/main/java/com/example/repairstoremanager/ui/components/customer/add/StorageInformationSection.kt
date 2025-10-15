package com.example.repairstoremanager.ui.components.customer.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun StorageInformationSection(
    drawerNumber: String,
    extraDetails: String,
    onDrawerNumberChange: (String) -> Unit,
    onExtraDetailsChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text("Storage Information", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(8.dp))

        // ✅ Drawer / Box Number Field
        OutlinedTextField(
            value = drawerNumber,
            onValueChange = onDrawerNumberChange,
            label = { Text("Box/Drawer Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        // ✅ Additional Details Field
        OutlinedTextField(
            value = extraDetails,
            onValueChange = onExtraDetailsChange,
            label = { Text("Additional Details") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() } // ✅ close keyboard
            ),
            singleLine = false,
            maxLines = 4
        )
    }
}