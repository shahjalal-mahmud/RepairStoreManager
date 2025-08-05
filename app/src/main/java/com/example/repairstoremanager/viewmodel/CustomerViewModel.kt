package com.example.repairstoremanager.viewmodel

import android.content.Context
import android.widget.Toast
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

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError

    fun fetchCustomers() {
        viewModelScope.launch {
            _isLoading.value = true
            _hasError.value = false
            try {
                _customers.value = repository.getAllCustomers()
            } catch (e: Exception) {
                _hasError.value = true
                _customers.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addCustomer(
        customer: Customer,
        context: Context,
        simSlotIndex: Int = 0,
        autoSmsEnabled: Boolean = true,
        onSuccess: (Customer) -> Unit,  // Changed to accept Customer parameter
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val timestamp = if (customer.createdAt == 0L) System.currentTimeMillis() else customer.createdAt
            val customerWithTimestamp = customer.copy(createdAt = timestamp)
            val result = repository.addCustomer(customerWithTimestamp)
            if (result.isSuccess) {
                fetchCustomers()
                // Get the last added customer (which will have the invoice number)
                val savedCustomer = _customers.value.lastOrNull {
                    it.customerName == customer.customerName &&
                            it.contactNumber == customer.contactNumber &&
                            it.date == customer.date
                }
                onSuccess(savedCustomer ?: customer) // Return the saved customer or fallback to original

                if (autoSmsEnabled) {
                    val message = "প্রিয় ${customer.customerName}, আপনার ডিভাইসটি মেরামতের জন্য গ্রহণ করা হয়েছে। " +
                            "প্রত্যাশিত ডেলিভারি তারিখ: ${customer.deliveryDate}। স্ট্যাটাস: Pending।\n\n📌 নোট: অনুগ্রহ করে ২ মাসের মধ্যে আপনার ডিভাইস সংগ্রহ করুন। অন্যথায়, আমরা ডিভাইসের কোনো গ্যারান্টি দিতে পারব না।"
                    SmsHelper.sendSms(context, customer.contactNumber, message, simSlotIndex)
                }
            } else {
                onError(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun updateCustomerStatus(
        customerId: String,
        newStatus: String,
        customer: Customer,
        context: Context,
        simSlotIndex: Int = 0,
        autoSmsEnabled: Boolean = true
    ) {
        viewModelScope.launch {
            repository.updateStatus(customerId, newStatus)
            fetchCustomers()

            if (autoSmsEnabled) {
                val note = "\n\n📌 নোট: অনুগ্রহ করে ২ মাসের মধ্যে আপনার ডিভাইস সংগ্রহ করুন। অন্যথায়, আমরা ডিভাইসের কোনো গ্যারান্টি দিতে পারব না।"

                val message = when (newStatus) {
                    "Repaired" -> "প্রিয় ${customer.customerName}, আপনার ডিভাইসটি মেরামত করা হয়েছে। অনুগ্রহ করে ডিভাইসটি সংগ্রহ করুন।$note"
                    "Delivered" -> "প্রিয় ${customer.customerName}, আপনার ডিভাইসটি ডেলিভারি দেওয়া হয়েছে। আমাদের সেবার জন্য ধন্যবাদ!"
                    "Cancelled" -> "প্রিয় ${customer.customerName}, আপনার রিপেয়ার অনুরোধ বাতিল করা হয়েছে। ভবিষ্যতে আবার যোগাযোগ করুন।"
                    "Pending" -> "প্রিয় ${customer.customerName}, আপনার ডিভাইসটি রিপেয়ারের জন্য গ্রহণ করা হয়েছে। বর্তমান অবস্থা: Pending।$note"
                    else -> "প্রিয় ${customer.customerName}, আপনার ডিভাইসের স্ট্যাটাস এখন: $newStatus।$note"
                }

                SmsHelper.sendSms(context, customer.contactNumber, message, simSlotIndex)
            }
        }
    }
    fun updateCustomer(
        updatedCustomer: Customer,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.updateCustomer(updatedCustomer)
            if (result.isSuccess) {
                fetchCustomers()
                Toast.makeText(context, "Customer updated successfully", Toast.LENGTH_SHORT).show()
                onSuccess()
            } else {
                Toast.makeText(context, "Failed to update customer", Toast.LENGTH_SHORT).show()
                onError(result.exceptionOrNull()?.message ?: "Unknown error")
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
