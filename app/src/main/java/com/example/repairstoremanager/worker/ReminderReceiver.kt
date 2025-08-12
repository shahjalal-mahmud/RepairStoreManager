package com.example.repairstoremanager.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        WorkScheduler.triggerWorkerImmediately(context)
        // Reschedule for next day
        val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        val hour = prefs.getInt("hour", 9)
        val minute = prefs.getInt("minute", 0)
        WorkScheduler.scheduleDailyReminder(context, hour, minute)
    }
}