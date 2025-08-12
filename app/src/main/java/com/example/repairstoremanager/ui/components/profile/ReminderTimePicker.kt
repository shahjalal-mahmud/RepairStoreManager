package com.example.repairstoremanager.ui.components.profile

import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.repairstoremanager.viewmodel.StoreViewModel
import com.example.repairstoremanager.worker.WorkScheduler

@Composable
fun ReminderTimePicker(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current
) {
    val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
    var selectedHour by remember { mutableStateOf(prefs.getInt("hour", 9)) }
    var selectedMinute by remember { mutableStateOf(prefs.getInt("minute", 0)) }
    var isReminderEnabled by remember { mutableStateOf(prefs.contains("hour")) }

    LaunchedEffect(Unit) {
        if (isReminderEnabled) {
            WorkScheduler.scheduleDailyReminder(context, selectedHour, selectedMinute)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Delivery Reminder Time",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            Text(
                if (isReminderEnabled)
                    "Reminder set for: %02d:%02d".format(selectedHour, selectedMinute)
                else
                    "No reminder set",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                Button(
                    onClick = {
                        val timePickerDialog = TimePickerDialog(
                            context,
                            { _, pickedHour, pickedMinute ->
                                selectedHour = pickedHour
                                selectedMinute = pickedMinute
                                storeViewModel.updateReminderTime(context, pickedHour, pickedMinute)
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
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Set Reminder")
                }

                Button(
                    onClick = {
                        storeViewModel.cancelReminderTime(context)
                        isReminderEnabled = false
                        Toast.makeText(context, "Reminder cancelled", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

