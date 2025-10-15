package com.example.repairstoremanager.ui.components.customer.add

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.repairstoremanager.ui.components.customer.common.SectionTitle

@Composable
fun PaymentInfoSection(
    totalAmount: String,
    advanced: String,
    onTotalAmountChange: (String) -> Unit,
    onAdvancedChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    Column(modifier = modifier) {
        SectionTitle("💳 Payment Info")

        CustomTextField(
            label = "Total Amount (৳)",
            value = totalAmount,
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onValueChange = onTotalAmountChange
        )

        CustomTextField(
            label = "Advanced Paid (৳)",
            value = advanced,
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onValueChange = onAdvancedChange
        )
    }
}
