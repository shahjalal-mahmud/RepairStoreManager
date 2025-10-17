package com.example.repairstoremanager.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.repairstoremanager.util.NotificationUtils
import com.google.gson.Gson
import java.util.UUID

class TalikhataReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    companion object {
        const val KEY_NAME = "name"
        const val KEY_AMOUNT = "amount"
        const val KEY_DUEDATE_MS = "due_date_ms"
        const val KEY_IS_PAYABLE_TO_USER = "is_payable_to_user"
        const val KEY_ENTRY_ID = "entry_id"
    }

    override suspend fun doWork(): Result {
        val name = inputData.getString(KEY_NAME) ?: return Result.failure()
        val amount = inputData.getDouble(KEY_AMOUNT, 0.0)
        val isPayableToUser = inputData.getBoolean(KEY_IS_PAYABLE_TO_USER, true)
        val entryId = inputData.getString(KEY_ENTRY_ID) ?: UUID.randomUUID().toString()

        val title = if (isPayableToUser) "Upcoming payment to $name" else "Upcoming payment from $name"
        val message = if (isPayableToUser) {
            "You need to pay ${formatMoney(amount)} to $name tomorrow."
        } else {
            "$name is due to pay you ${formatMoney(amount)} tomorrow."
        }

        // Use your NotificationUtils to show and save notification
        NotificationUtils.showNotification(applicationContext, title, message)

        return Result.success()
    }

    private fun formatMoney(value: Double): String {
        // simple formatting, adjust for locale if needed
        return String.format("%.2f", value)
    }
}