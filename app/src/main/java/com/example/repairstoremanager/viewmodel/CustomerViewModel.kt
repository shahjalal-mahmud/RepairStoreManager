package com.example.repairstoremanager.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.data.repository.CustomerRepository
import com.example.repairstoremanager.util.SmsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CustomerViewModel : ViewModel() {
    private val repository = CustomerRepository()

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun addCustomer(customer: Customer, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val timestamp = if (customer.createdAt == 0L) System.currentTimeMillis() else customer.createdAt
            val customerWithTimestamp = customer.copy(createdAt = timestamp)
            val result = repository.addCustomer(customerWithTimestamp)
            if (result.isSuccess) {
                fetchCustomers()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun fetchCustomers() {
        viewModelScope.launch {
            _isLoading.value = true
            _customers.value = repository.getAllCustomers()
            _isLoading.value = false
        }
    }

    fun updateCustomerStatus(customerId: String, newStatus: String, customer: Customer, context: Context) {
        viewModelScope.launch {
            repository.updateStatus(customerId, newStatus)
            fetchCustomers()

            val message = when (newStatus) {
                "Repaired" -> "✅ Hello ${customer.customerName}, your device has been repaired. Please collect it."
                "Delivered" -> "🙏 Hello ${customer.customerName}, your device has been delivered. Thank you for visiting!"
                "Cancelled" -> "❌ Hello ${customer.customerName}, your repair request has been cancelled. Let us know if we can help again."
                else -> null
            }

            message?.let {
                SmsHelper.sendSms(context, customer.contactNumber, it)
            }
        }
    }

    private fun getToday(): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    private fun getTomorrow(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, 1)
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.time)
    }

    val totalCustomersCount: Int
        get() = customers.value.size

    val todaysInvoicesCount: Int
        get() = customers.value.count {
            it.date.startsWith(getToday())
        }

    val pendingDevicesCount: Int
        get() = customers.value.count { it.status.equals("Pending", ignoreCase = true) }

    val todayDeliveryList: List<Customer>
        get() = customers.value.filter { it.deliveryDate == getToday() }

    val tomorrowDeliveryList: List<Customer>
        get() = customers.value.filter { it.deliveryDate == getTomorrow() }

}
