package com.example.repairstoremanager.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.repairstoremanager.R
import com.google.gson.Gson
import java.util.*

object NotificationUtils {
    private const val CHANNEL_ID = "delivery_reminder_channel"
    private const val CHANNEL_NAME = "Delivery Reminder"
    private const val PREFS_NAME = "notification_prefs"
    private const val KEY_NOTIFICATIONS = "notifications_list"

    fun showNotification(context: Context, title: String, message: String) {
        // Save notification to storage
        val notification = AppNotification(title = title, message = message)
        saveNotification(context, notification)

        // Show system notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel for Android 8+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun saveNotification(context: Context, notification: AppNotification) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val notifications = getNotifications(context).toMutableList()
        notifications.add(0, notification) // Add to beginning for newest first

        val json = gson.toJson(notifications)
        prefs.edit().putString(KEY_NOTIFICATIONS, json).apply()
    }

    fun getNotifications(context: Context): List<AppNotification> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_NOTIFICATIONS, "[]") ?: "[]"
        val gson = Gson()
        return try {
            gson.fromJson(json, Array<AppNotification>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun markAsRead(context: Context, notificationId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val notifications = getNotifications(context).toMutableList()
        val index = notifications.indexOfFirst { it.id == notificationId }
        if (index != -1) {
            notifications[index] = notifications[index].copy(isRead = true)
            val gson = Gson()
            val json = gson.toJson(notifications)
            prefs.edit().putString(KEY_NOTIFICATIONS, json).apply()
        }
    }

    fun markAllAsRead(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val notifications = getNotifications(context).map { it.copy(isRead = true) }

        val gson = Gson()
        val json = gson.toJson(notifications)
        prefs.edit().putString(KEY_NOTIFICATIONS, json).apply()
    }

    fun getUnreadCount(context: Context): Int {
        return getNotifications(context).count { !it.isRead }
    }

    fun clearAllNotifications(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_NOTIFICATIONS).apply()
    }
}

// Keep this data class separate from the object
data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "delivery"
)