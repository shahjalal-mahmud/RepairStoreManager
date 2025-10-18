package com.example.repairstoremanager.ui.components.common

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.repairstoremanager.util.DateUtils
import java.time.LocalDate
import java.util.Calendar

@Composable
fun AndroidDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Convert LocalDate to Calendar
    val calendar = Calendar.getInstance().apply {
        val date = DateUtils.localDateToDate(initialDate)
        time = date
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            val selectedDate = LocalDate.of(year, month + 1, day)
            onDateSelected(selectedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    datePickerDialog.setOnDismissListener {
        onDismiss()
    }

    datePickerDialog.show()
}