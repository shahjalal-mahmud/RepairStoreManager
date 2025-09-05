package com.example.repairstoremanager.ui.screens.transaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "Select Date",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Date categories with dividers
                val dateCategories = listOf(
                    "Recent" to listOf(
                        "Today" to getFormattedDate(0),
                        "Yesterday" to getFormattedDate(-1),
                        "2 days ago" to getFormattedDate(-2),
                        "3 days ago" to getFormattedDate(-3)
                    ),
                    "This Week" to listOf(
                        "4 days ago" to getFormattedDate(-4),
                        "5 days ago" to getFormattedDate(-5),
                        "6 days ago" to getFormattedDate(-6),
                        "1 week ago" to getFormattedDate(-7)
                    ),
                    "This Month" to listOf(
                        "2 weeks ago" to getFormattedDate(-14),
                        "3 weeks ago" to getFormattedDate(-21),
                        "1 month ago" to getFormattedDate(-30)
                    ),
                    "Older" to listOf(
                        "2 months ago" to getFormattedDate(-60),
                        "3 months ago" to getFormattedDate(-90),
                        "6 months ago" to getFormattedDate(-180),
                        "1 year ago" to getFormattedDate(-365)
                    )
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dateCategories.forEach { (category, dates) ->
                        stickyHeader {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }

                        items(dates) { (label, date) ->
                            DateOptionButton(
                                label = label,
                                date = date,
                                onClick = { onDateSelected(date) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom date input (optional future enhancement)

                // Cancel button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun DateOptionButton(
    label: String,
    date: String,
    onClick: () -> Unit
) {
    val isToday = label == "Today"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isToday) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        border = if (isToday) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = getDayOfWeek(date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun getDayOfWeek(dateString: String): String {
    return try {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = format.parse(dateString)
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        dayFormat.format(date ?: Date())
    } catch (e: Exception) {
        ""
    }
}

private fun getFormattedDate(daysOffset: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, daysOffset)
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
}

// Extension function for formatting doubles
fun Double.format(decimals: Int): String {
    return "%.${decimals}f".format(this)
}