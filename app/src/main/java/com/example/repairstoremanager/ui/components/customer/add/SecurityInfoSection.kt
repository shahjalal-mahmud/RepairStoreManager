package com.example.repairstoremanager.ui.components.customer.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.ui.components.customer.common.SectionTitle

@Composable
fun SecurityInfoSection(
    securityType: String,
    phonePassword: String,
    pattern: List<Int>,
    hasDrawnPattern: Boolean,
    patternResetKey: Int,
    onSecurityTypeChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPatternComplete: (List<Int>) -> Unit,
    onResetPattern: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionTitle("🔐 Security Type")
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = securityType == "Password",
                onClick = { onSecurityTypeChange("Password") }
            )
            Text("Password", Modifier.padding(end = 16.dp))
            RadioButton(
                selected = securityType == "Pattern",
                onClick = { onSecurityTypeChange("Pattern") }
            )
            Text("Pattern")
        }

        Spacer(Modifier.height(12.dp))

        when (securityType) {
            "Password" -> PasswordField(
                value = phonePassword,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth()
            )
            "Pattern" -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Draw your pattern (minimum 4 dots)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        PatternLockCanvas(
                            isInteractive = !hasDrawnPattern,
                            pattern = pattern,
                            resetKey = patternResetKey,
                            onPatternComplete = { drawn ->
                                if (drawn.size >= 4) {
                                    onPatternComplete(drawn)
                                }
                            }
                        )
                    }

                    if (hasDrawnPattern) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(onClick = onResetPattern) {
                                Text("🔄 Try Again")
                            }
                        }
                    }
                }
            }
        }
    }
}