package com.example.repairstoremanager.ui.components.customer.card

import android.content.Context
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.net.toUri
import com.example.repairstoremanager.R
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.util.MessageHelper
import com.example.repairstoremanager.viewmodel.CustomerViewModel

@Composable
fun CallDropdownMenu(
    customer: Customer,
    viewModel: CustomerViewModel,
    context: Context,
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    stringResource(R.string.call),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = "tel:${customer.contactNumber}".toUri()
                }
                context.startActivity(intent)
                onDismiss()
            },
            leadingIcon = {
                Icon(Icons.Default.Call, contentDescription = null)
            }
        )

        DropdownMenuItem(
            text = {
                Text(
                    stringResource(R.string.sms),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            onClick = {
                val message = viewModel.getStatusMessage(customer)
                MessageHelper.sendSmsViaIntent(context, customer.contactNumber, message)
                onDismiss()
            },
            leadingIcon = {
                Icon(Icons.Default.Sms, contentDescription = null)
            }
        )

        DropdownMenuItem(
            text = {
                Text(
                    stringResource(R.string.whatsapp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            onClick = {
                val message = viewModel.getStatusMessage(customer)
                MessageHelper.sendWhatsAppMessage(context, customer.contactNumber, message)
                onDismiss()
            },
            leadingIcon = {
                Icon(Icons.Default.Chat, contentDescription = null)
            }
        )
    }
}