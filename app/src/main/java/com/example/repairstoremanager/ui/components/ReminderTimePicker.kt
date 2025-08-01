package com.example.repairstoremanager.ui.components

import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.worker.WorkScheduler
import java.util.*

@Composable
fun ReminderTimePicker(
    context: Context = LocalContext.current,
    modifier: Modifier = Modifier
) {
    val calendar = remember { Calendar.getInstance() }
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }
    var isReminderEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Delivery Reminder Time", style = MaterialTheme.typography.titleMedium)

        if (isReminderEnabled) {
            Text("Reminder set for: %02d:%02d".format(selectedHour, selectedMinute))
        } else {
            Text("No reminder set")
        }

        Button(onClick = {
            val timePickerDialog = TimePickerDialog(
                context,
                { _, hour, minute ->
                    selectedHour = hour
                    selectedMinute = minute
                    WorkScheduler.cancelReminder(context)
                    WorkScheduler.scheduleDailyReminder(context, hour, minute)
                    isReminderEnabled = true
                    Toast.makeText(
                        context,
                        "Reminder set for %02d:%02d".format(hour, minute),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                selectedHour,
                selectedMinute,
                false // 12-hour clock (use true for 24-hour)
            )
            timePickerDialog.show()
        }) {
            Text("Set Reminder")
        }

        Button(
            onClick = {
                WorkScheduler.cancelReminder(context)
                isReminderEnabled = false
                Toast.makeText(context, "Reminder cancelled", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Text("Cancel")
        }
    }
}
