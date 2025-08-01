package com.example.repairstoremanager.worker


import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.repairstoremanager.data.repository.CustomerRepository
import com.example.repairstoremanager.util.NotificationUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeliveryReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val repository = CustomerRepository()
        val customers = repository.getAllCustomers()
        val deliveriesToday = customers.filter { it.deliveryDate == today }

        if (deliveriesToday.isNotEmpty()) {
            val message = buildString {
                append("You have ${deliveriesToday.size} device(s) to deliver today:\n")
                deliveriesToday.forEach {
                    append("- ${it.customerName} (${it.phoneModel})\n")
                }
            }
            NotificationUtils.showNotification(
                applicationContext,
                title = "Today's Deliveries",
                message = message
            )
        }

        return Result.success()
    }
}