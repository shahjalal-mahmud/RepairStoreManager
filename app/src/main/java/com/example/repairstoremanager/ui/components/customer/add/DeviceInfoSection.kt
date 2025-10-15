package com.example.repairstoremanager.ui.components.customer.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.ui.components.customer.common.SectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceInfoSection(
    phoneModel: String,
    problem: String,
    deliveryDate: String,
    onPhoneModelChange: (String) -> Unit,
    onProblemChange: (String) -> Unit,
    onAddPhoneModel: (String) -> Unit,
    onAddProblem: (String) -> Unit,
    onDeliveryDateClick: () -> Unit,
    commonPhoneModels: Set<String>,
    userPhoneModels: Set<String>,
    commonProblems: Set<String>,
    userProblems: Set<String>,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier) {
        SectionTitle("📱 Device Info")

        SuggestionTextField(
            value = phoneModel,
            onValueChange = onPhoneModelChange,
            label = "Phone Model",
            commonItems = commonPhoneModels,
            userItems = userPhoneModels,
            onAddUserItem = onAddPhoneModel,
            imeAction = ImeAction.Next,
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            modifier = Modifier.padding(vertical = 4.dp)
        )

        SuggestionTextField(
            value = problem,
            onValueChange = onProblemChange,
            label = "Problem Description",
            commonItems = commonProblems,
            userItems = userProblems,
            onAddUserItem = onAddProblem,
            imeAction = ImeAction.Next,
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            modifier = Modifier.padding(vertical = 4.dp)
        )

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
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
    }
}