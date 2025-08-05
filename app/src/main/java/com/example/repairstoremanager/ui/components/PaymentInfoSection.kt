package com.example.repairstoremanager.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun PaymentInfoSection(
    totalAmount: String,
    advanced: String,
    onTotalAmountChange: (String) -> Unit,
    onAdvancedChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionTitle("💳 Payment Info")
        CustomTextField(
            "Total Amount (৳)",
            totalAmount,
            KeyboardType.Number,
            onTotalAmountChange
        )
        CustomTextField(
            "Advanced Paid (৳)",
            advanced,
            KeyboardType.Number,
            onAdvancedChange
        )
    }
}