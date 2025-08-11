package com.example.repairstoremanager.ui.components.customer.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SaveCustomerButton(
    isLoading: Boolean,
    onSaveClick: () -> Unit,
    onSaveAndPrintClick: () -> Unit,
    onPreviewClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSaved: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onSaveClick,
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
                Text("💾 Save", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSaveAndPrintClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isLoading
        ) {
            Text("🖨️ Save & Print", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}