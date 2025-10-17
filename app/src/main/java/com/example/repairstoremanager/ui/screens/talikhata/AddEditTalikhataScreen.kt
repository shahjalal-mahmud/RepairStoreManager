package com.example.repairstoremanager.ui.screens.talikhata

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.data.model.TalikhataEntry
import com.google.firebase.Timestamp
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTalikhataScreen(
    entry: TalikhataEntry? = null,
    onSave: (TalikhataEntry) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(entry?.name ?: "") }
    var phone by remember { mutableStateOf(entry?.phone ?: "") }
    var amount by remember { mutableStateOf(entry?.amount?.toString() ?: "") }
    var dueDate by remember { mutableStateOf(entry?.dueDate?.toDate() ?: Date()) }
    var payableToUser by remember {
        mutableStateOf(entry?.payableToUser ?: true)
    }

    var showDatePicker by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (entry == null) "Add Entry" else "Edit Entry",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (validateInput(name, phone, amount)) {
                        val talikhataEntry = TalikhataEntry(
                            id = entry?.id ?: "",
                            name = name.trim(),
                            phone = phone.trim(),
                            amount = amount.toDouble(),
                            dueDate = Timestamp(dueDate),
                            payableToUser = payableToUser,
                            reminderScheduled = entry?.reminderScheduled ?: false,
                            createdAt = entry?.createdAt ?: Timestamp.now()
                        )
                        onSave(talikhataEntry)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Name Field
            SmartTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name *",
                placeholder = "Enter person's name",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { /* Focus moves to next field automatically */ }),
                leadingIcon = Icons.Outlined.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Field
            SmartTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Phone Number",
                placeholder = "Enter phone number",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { /* Focus moves to next field automatically */ }),
                leadingIcon = Icons.Outlined.Phone
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Field
            SmartTextField(
                value = amount,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                        amount = it
                    }
                },
                label = "Amount *",
                placeholder = "0.00",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                leadingIcon = Icons.Outlined.AttachMoney,
                trailingIcon = {
                    Text(
                        "BDT",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Payment Type Selection
            Text(
                text = "Payment Type *",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PaymentTypeChip(
                    selected = payableToUser,
                    onClick = { payableToUser = true },
                    text = "You will pay",
                    icon = Icons.Outlined.ArrowUpward,
                    color = MaterialTheme.colorScheme.error
                )

                PaymentTypeChip(
                    selected = !payableToUser,
                    onClick = { payableToUser = false },
                    text = "You will receive",
                    icon = Icons.Outlined.ArrowDownward,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Due Date Selection
            Text(
                text = "Due Date *",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDateForDisplay(dueDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Select date",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Info text
            Text(
                text = "You'll receive a reminder 1 day before the due date",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate.time
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            dueDate = Date(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SmartTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(
                    width = 2.dp,
                    color = when {
                        isFocused -> MaterialTheme.colorScheme.primary
                        value.isNotEmpty() -> MaterialTheme.colorScheme.outline
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    },
                    shape = MaterialTheme.shapes.small
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (isFocused) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { isFocused = it.isFocused },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                )

                trailingIcon?.invoke()
            }
        }
    }
}

@Composable
private fun PaymentTypeChip(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier
            .height(80.dp)
            .clickable { onClick() },
        elevation = if (selected) CardDefaults.cardElevation(4.dp)
        else CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) color.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = if (selected) BorderStroke(2.dp, color) else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatDateForDisplay(date: Date): String {
    val formatter = java.text.SimpleDateFormat("EEEE, MMMM dd, yyyy", java.util.Locale.getDefault())
    return formatter.format(date)
}

private fun validateInput(name: String, phone: String, amount: String): Boolean {
    return name.isNotBlank() && amount.isNotBlank() && amount.toDoubleOrNull() != null
}