package com.example.repairstoremanager.worker

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.repairstoremanager.data.repository.CustomerRepository
import com.example.repairstoremanager.util.NotificationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DeliveryReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable(applicationContext)) {
            // Skip if no internet
            return@withContext Result.retry()
        }

        val tz = TimeZone.getTimeZone("Asia/Dhaka")
        val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply {
            timeZone = tz
        }.format(Date())

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

        Result.success()
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
