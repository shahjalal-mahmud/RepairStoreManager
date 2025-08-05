package com.example.repairstoremanager.ui.components

import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.viewmodel.StoreViewModel
import com.example.repairstoremanager.worker.WorkScheduler

@Composable
fun ReminderTimePicker(
    storeViewModel: StoreViewModel,
    context: Context = LocalContext.current,
    modifier: Modifier = Modifier
) {
    val storeInfo = storeViewModel.storeInfo
    val hour = storeInfo.reminderHour ?: 9
    val minute = storeInfo.reminderMinute ?: 0
    var selectedHour by remember { mutableStateOf(hour) }
    var selectedMinute by remember { mutableStateOf(minute) }
    var isReminderEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        WorkScheduler.cancelReminder(context) // Avoid duplicates
        WorkScheduler.scheduleDailyReminder(context, hour, minute)
    }

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
                { _, pickedHour, pickedMinute ->
                    selectedHour = pickedHour
                    selectedMinute = pickedMinute

                    WorkScheduler.cancelReminder(context)
                    WorkScheduler.scheduleDailyReminder(context, pickedHour, pickedMinute)

                    storeViewModel.updateReminderTime(pickedHour, pickedMinute)

                    isReminderEnabled = true
                    Toast.makeText(
                        context,
                        "Reminder set for %02d:%02d".format(pickedHour, pickedMinute),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                selectedHour,
                selectedMinute,
                false
            )
            timePickerDialog.show()
        }) {
            Text("Set Reminder")
        }

        Button(
            onClick = {
                WorkScheduler.cancelReminder(context)
                isReminderEnabled = false
                storeViewModel.updateReminderTime(null, null)
                Toast.makeText(context, "Reminder cancelled", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Text("Cancel")
        }
    }
}
