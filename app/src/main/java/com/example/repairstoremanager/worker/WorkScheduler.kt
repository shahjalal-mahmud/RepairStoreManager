package com.example.repairstoremanager.worker

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WorkScheduler {

    fun scheduleDailyReminder(context: Context, hour: Int = 9, minute: Int = 0) {
        val currentTime = Calendar.getInstance()
        val scheduledTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(currentTime)) add(Calendar.DAY_OF_YEAR, 1)
        }

        val delay = scheduledTime.timeInMillis - currentTime.timeInMillis

        val request = OneTimeWorkRequestBuilder<DeliveryReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("delivery_reminder_test")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "delivery_reminder_test_work",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("delivery_reminder_test_work")
    }
}
