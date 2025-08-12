package com.example.repairstoremanager.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.work.*
import java.util.*

object WorkScheduler {

    fun scheduleDailyReminder(context: Context, hour: Int = 9, minute: Int = 0) {
        cancelReminder(context) // Cancel previous

        val tz = TimeZone.getTimeZone("Asia/Dhaka")
        val now = Calendar.getInstance(tz)
        val scheduled = Calendar.getInstance(tz).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // On Android 12+, check exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val canScheduleExact = alarmManager.canScheduleExactAlarms()
            if (!canScheduleExact) {
                // Redirect user to grant exact alarm permission
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)

                // Fallback: use WorkManager
                enqueueWorker(context, scheduled.timeInMillis)
                return
            }
        }

        // Schedule exact alarm
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            scheduled.timeInMillis,
            pendingIntent
        )
    }

    private fun enqueueWorker(context: Context, triggerTimeMillis: Long) {
        val delay = triggerTimeMillis - System.currentTimeMillis()
        val request = OneTimeWorkRequestBuilder<DeliveryReminderWorker>()
            .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    fun triggerWorkerImmediately(context: Context) {
        val request = OneTimeWorkRequestBuilder<DeliveryReminderWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }

    fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("delivery_reminder")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}