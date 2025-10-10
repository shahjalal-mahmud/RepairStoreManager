package com.example.repairstoremanager.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.repairstoremanager.R
import com.example.repairstoremanager.data.model.Product
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object StockNotificationManager {
    private const val STOCK_CHANNEL_ID = "low_stock_channel" // Different channel for stock notifications
    private const val STOCK_CHANNEL_NAME = "Low Stock Alerts"
    private const val SUMMARY_NOTIFICATION_ID = 1001 // Fixed ID for summary notification
    private const val LAST_NOTIFICATION_TIME_KEY = "last_stock_notification_time"
    private const val NOTIFICATION_COOLDOWN = 6 * 60 * 60 * 1000 // 6 hours cooldown

    /**
     * Check for low stock and show a single summary notification with ALL products
     */
    suspend fun checkLowStockAndNotify(context: Context, products: List<Product>) {
        withContext(Dispatchers.IO) {
            val lowStockProducts = products.filter { product ->
                product.quantity <= product.alertQuantity && product.alertQuantity > 0
            }

            if (lowStockProducts.isNotEmpty()) {
                // Check if we should show notification (cooldown period)
                if (shouldShowNotification(context)) {
                    showLowStockSummaryNotification(context, lowStockProducts)
                    saveIndividualNotifications(context, lowStockProducts)
                    updateLastNotificationTime(context)
                }
            } else {
                // No low stock products, remove any existing summary notification
                clearSummaryNotification(context)
            }
        }
    }

    /**
     * Show a single summary notification for ALL low stock products
     */
    private fun showLowStockSummaryNotification(context: Context, lowStockProducts: List<Product>) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create dedicated notification channel for stock alerts
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                STOCK_CHANNEL_ID,
                STOCK_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Alerts for low stock products"
            channel.enableVibration(true)
            channel.setShowBadge(true)
            notificationManager.createNotificationChannel(channel)
        }

        val (title, message, detailedMessage) = createNotificationContent(lowStockProducts)

        // Build the summary notification
        val builder = NotificationCompat.Builder(context, STOCK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detailedMessage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setGroup("low_stock_group")
            .setGroupSummary(true)

        // Show the notification with a fixed ID so it replaces previous ones
        notificationManager.notify(SUMMARY_NOTIFICATION_ID, builder.build())
    }

    /**
     * Create notification content with ALL low stock products
     */
    private fun createNotificationContent(lowStockProducts: List<Product>): Triple<String, String, String> {
        val title = "📦 Low Stock Alert"

        val message = when (lowStockProducts.size) {
            1 -> "1 product needs restocking"
            else -> "${lowStockProducts.size} products need restocking"
        }

        val detailedMessage = StringBuilder()

        if (lowStockProducts.isEmpty()) {
            detailedMessage.append("No products are low on stock.")
        } else if (lowStockProducts.size == 1) {
            val product = lowStockProducts.first()
            detailedMessage.append("${product.name} is running low:\n")
            detailedMessage.append("• Current Stock: ${product.quantity}\n")
            detailedMessage.append("• Alert Level: ${product.alertQuantity}")
            if (product.supplier.isNotBlank()) {
                detailedMessage.append("\n• Supplier: ${product.supplier}")
            }
        } else {
            detailedMessage.append("The following products are running low:\n\n")

            // Show ALL products sorted by quantity (most critical first)
            lowStockProducts.sortedBy { it.quantity }.forEachIndexed { index, product ->
                detailedMessage.append("${index + 1}. ${product.name}\n")
                detailedMessage.append("   📊 Stock: ${product.quantity} | Alert: ${product.alertQuantity}")
                if (product.supplier.isNotBlank()) {
                    detailedMessage.append(" | Supplier: ${product.supplier}")
                }
                detailedMessage.append("\n\n")
            }

            // Add summary at the end
            val criticalCount = lowStockProducts.count { it.quantity == 0L }
            val lowCount = lowStockProducts.count { it.quantity > 0 && it.quantity <= 3 }

            if (criticalCount > 0 || lowCount > 0) {
                detailedMessage.append("📈 Summary:\n")
                if (criticalCount > 0) {
                    detailedMessage.append("• $criticalCount products are OUT OF STOCK\n")
                }
                if (lowCount > 0) {
                    detailedMessage.append("• $lowCount products have VERY LOW stock\n")
                }
            }
        }

        return Triple(title, message, detailedMessage.toString())
    }

    /**
     * Save individual notifications for in-app notification screen
     */
    private fun saveIndividualNotifications(context: Context, lowStockProducts: List<Product>) {
        val prefs = context.getSharedPreferences(NotificationUtils.PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val existingNotifications = NotificationUtils.getNotifications(context).toMutableList()

        // Remove old unread stock notifications to avoid duplicates
        existingNotifications.removeAll { notification ->
            notification.type.startsWith("stock") && !notification.isRead
        }

        // Create a summary notification for in-app display
        val (title, message, _) = createNotificationContent(lowStockProducts)
        val summaryNotification = AppNotification(
            title = title,
            message = message,
            type = "stock_summary",
            productId = "summary_${System.currentTimeMillis()}"
        )

        // Add individual product notifications for in-app details
        val productNotifications = lowStockProducts.map { product ->
            AppNotification(
                title = "Low Stock: ${product.name}",
                message = "Current: ${product.quantity} | Alert: ${product.alertQuantity}" +
                        if (product.supplier.isNotBlank()) " | Supplier: ${product.supplier}" else "",
                type = "stock_detail",
                productId = product.id
            )
        }

        // Combine all notifications (summary first, then individual products)
        val allNotifications = mutableListOf<AppNotification>().apply {
            add(summaryNotification)
            addAll(productNotifications)
            addAll(existingNotifications) // Keep existing read notifications
        }

        // Save to shared preferences
        val json = gson.toJson(allNotifications)
        prefs.edit().putString(NotificationUtils.KEY_NOTIFICATIONS, json).apply()
    }

    /**
     * Check if we should show notification based on cooldown period
     */
    private fun shouldShowNotification(context: Context): Boolean {
        val prefs = context.getSharedPreferences(NotificationUtils.PREFS_NAME, Context.MODE_PRIVATE)
        val lastNotificationTime = prefs.getLong(LAST_NOTIFICATION_TIME_KEY, 0)
        val currentTime = System.currentTimeMillis()

        // Always show if never shown before or cooldown period has passed
        return lastNotificationTime == 0L || currentTime - lastNotificationTime > NOTIFICATION_COOLDOWN
    }

    /**
     * Update the last notification time
     */
    private fun updateLastNotificationTime(context: Context) {
        val prefs = context.getSharedPreferences(NotificationUtils.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(LAST_NOTIFICATION_TIME_KEY, System.currentTimeMillis()).apply()
    }

    /**
     * Clear the summary notification
     */
    private fun clearSummaryNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(SUMMARY_NOTIFICATION_ID)
    }

    /**
     * Check if a specific product has low stock notification
     */
    fun hasLowStockNotification(context: Context, productId: String): Boolean {
        val notifications = NotificationUtils.getNotifications(context)
        return notifications.any {
            it.type == "stock_detail" && it.productId == productId && !it.isRead
        }
    }

    /**
     * Mark stock notifications as read for a specific product
     */
    fun markStockNotificationAsRead(context: Context, productId: String) {
        val prefs = context.getSharedPreferences(NotificationUtils.PREFS_NAME, Context.MODE_PRIVATE)
        val notifications = NotificationUtils.getNotifications(context).toMutableList()

        notifications.forEachIndexed { index, notification ->
            if (notification.type == "stock_detail" && notification.productId == productId && !notification.isRead) {
                notifications[index] = notification.copy(isRead = true)
            }
        }

        val gson = Gson()
        val json = gson.toJson(notifications)
        prefs.edit().putString(NotificationUtils.KEY_NOTIFICATIONS, json).apply()
    }

    /**
     * Mark all stock notifications as read
     */
    fun markAllStockNotificationsAsRead(context: Context) {
        val prefs = context.getSharedPreferences(NotificationUtils.PREFS_NAME, Context.MODE_PRIVATE)
        val notifications = NotificationUtils.getNotifications(context).toMutableList()

        notifications.forEachIndexed { index, notification ->
            if (notification.type.startsWith("stock") && !notification.isRead) {
                notifications[index] = notification.copy(isRead = true)
            }
        }

        val gson = Gson()
        val json = gson.toJson(notifications)
        prefs.edit().putString(NotificationUtils.KEY_NOTIFICATIONS, json).apply()

        // Also clear the system notification
        clearSummaryNotification(context)
    }

    /**
     * Force show notification (bypass cooldown) - useful for testing
     */
    suspend fun forceShowLowStockNotification(context: Context, products: List<Product>) {
        withContext(Dispatchers.IO) {
            val lowStockProducts = products.filter { product ->
                product.quantity <= product.alertQuantity && product.alertQuantity > 0
            }

            if (lowStockProducts.isNotEmpty()) {
                showLowStockSummaryNotification(context, lowStockProducts)
                saveIndividualNotifications(context, lowStockProducts)
                updateLastNotificationTime(context)
            }
        }
    }

    /**
     * Reset notification cooldown - useful for testing
     */
    fun resetNotificationCooldown(context: Context) {
        val prefs = context.getSharedPreferences(NotificationUtils.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(LAST_NOTIFICATION_TIME_KEY).apply()
    }

    /**
     * Get current low stock count for badge or UI updates
     */
    suspend fun getLowStockCount(context: Context, products: List<Product>): Int {
        return withContext(Dispatchers.IO) {
            products.count { product ->
                product.quantity <= product.alertQuantity && product.alertQuantity > 0
            }
        }
    }
}