package com.example.repairstoremanager.ui.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.util.DateUtils
import java.time.LocalDate

data class DateRange(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
) {
    val isEmpty: Boolean
        get() = startDate == null && endDate == null

    val isComplete: Boolean
        get() = startDate != null && endDate != null

    fun isValid(): Boolean {
        return if (isComplete) {
            !startDate!!.isAfter(endDate!!)
        } else {
            true
        }
    }

    fun getDisplayText(): String {
        return if (isEmpty) {
            "Select Date Range"
        } else if (startDate != null && endDate != null) {
            "${DateUtils.formatDateForDisplay(startDate)} - ${DateUtils.formatDateForDisplay(endDate)}"
        } else if (startDate != null) {
            "From ${DateUtils.formatDateForDisplay(startDate)}"
        } else {
            "To ${DateUtils.formatDateForDisplay(endDate!!)}"
        }
    }
}

@Composable
fun DateRangePicker(
    dateRange: DateRange,
    onDateRangeSelected: (DateRange) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Date Range"
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Date range display row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            // When clicking the main area, show start date picker
                            showStartDatePicker = true
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select date range",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = dateRange.getDisplayText(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Clear button
                if (!dateRange.isEmpty) {
                    IconButton(
                        onClick = {
                            onDateRangeSelected(DateRange())
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear date range",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Date selection buttons - Always show them
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Start Date Button
                DateSelectionButton(
                    label = "Start Date",
                    date = dateRange.startDate,
                    isSelected = showStartDatePicker,
                    onClick = {
                        showStartDatePicker = true
                        showEndDatePicker = false
                    }
                )

                // End Date Button
                DateSelectionButton(
                    label = "End Date",
                    date = dateRange.endDate,
                    isSelected = showEndDatePicker,
                    onClick = {
                        showEndDatePicker = true
                        showStartDatePicker = false
                    }
                )
            }
        }
    }

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        AndroidDatePickerDialog(
            initialDate = dateRange.startDate ?: LocalDate.now(),
            onDateSelected = { selectedDate ->
                val newRange = dateRange.copy(startDate = selectedDate)
                if (newRange.isValid()) {
                    onDateRangeSelected(newRange)
                }
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    // End Date Picker Dialog
    if (showEndDatePicker) {
        AndroidDatePickerDialog(
            initialDate = dateRange.endDate ?: (dateRange.startDate ?: LocalDate.now()),
            onDateSelected = { selectedDate ->
                val newRange = dateRange.copy(endDate = selectedDate)
                if (newRange.isValid()) {
                    onDateRangeSelected(newRange)
                }
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@Composable
private fun DateSelectionButton(
    label: String,
    date: LocalDate?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = date?.let { DateUtils.formatDateForDisplay(it) } ?: "Select",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (date != null) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}