package com.example.repairstoremanager.printer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.repairstoremanager.R
import com.example.repairstoremanager.printer.model.PrinterDevice

@Composable
fun PrinterSelectionDialog(
    printers: List<PrinterDevice>,
    onPrinterSelected: (PrinterDevice) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.select_printer),
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(printers) { printer ->
                        ListItem(
                            headlineContent = { Text(printer.name) },
                            supportingContent = {
                                Text(
                                    when (printer.type) {
                                        PrinterType.POS -> stringResource(R.string.pos_printer)
                                        PrinterType.STANDARD -> stringResource(R.string.standard_printer)
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPrinterSelected(printer) }
                        )
                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}