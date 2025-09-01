package com.example.repairstoremanager.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.data.repository.CustomerRepository
import com.example.repairstoremanager.data.repository.GoogleContactsRepository
import com.example.repairstoremanager.util.GoogleAuthHelper
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CustomerViewModel(private val context: Context) : ViewModel() {
    private val repository = CustomerRepository()
    private val googleContactsRepo = GoogleContactsRepository(context)
    private val googleAuthHelper = GoogleAuthHelper(context)

    private val _gmailContacts = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val gmailContacts: StateFlow<List<Pair<String, String>>> = _gmailContacts

    private val _isGmailConnected = MutableStateFlow(false)
    val isGmailConnected: StateFlow<Boolean> = _isGmailConnected

    private val _saveToGmailResult = MutableStateFlow<Boolean?>(null)
    val saveToGmailResult: StateFlow<Boolean?> = _saveToGmailResult

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError

    private val _phoneModelHistory = MutableStateFlow<Set<String>>(emptySet())
    private val _problemHistory = MutableStateFlow<Set<String>>(emptySet())

    private val _userPhoneModels = MutableStateFlow<Set<String>>(emptySet())
    val userPhoneModels: StateFlow<Set<String>> = _userPhoneModels

    private val _userProblems = MutableStateFlow<Set<String>>(emptySet())
    val userProblems: StateFlow<Set<String>> = _userProblems

    private val _currentInvoiceNumber = MutableStateFlow<String?>(null)
    val currentInvoiceNumber: StateFlow<String?> = _currentInvoiceNumber

    // Add this function to check if user is already signed in
    fun checkGmailConnection() {
        val account = googleAuthHelper.getCurrentAccount()
        _isGmailConnected.value = account != null
        account?.let { fetchGmailContacts(it) }
    }

    fun fetchGmailContacts(account: GoogleSignInAccount) {
        viewModelScope.launch {
            try {
                val contacts = googleContactsRepo.getContactsFromGmail(account)
                _gmailContacts.value = contacts
                _isGmailConnected.value = true
            } catch (e: Exception) {
                _gmailContacts.value = emptyList()
                _isGmailConnected.value = false
                Log.e("CustomerViewModel", "Error fetching Gmail contacts", e)
            }
        }
    }

    fun saveContactToGmail(account: GoogleSignInAccount, name: String, phone: String) {
        viewModelScope.launch {
            try {
                val success = googleContactsRepo.saveContactToGmail(account, name, phone)
                _saveToGmailResult.value = success
                if (success) {
                    // Refresh contacts after saving
                    fetchGmailContacts(account)
                }
            } catch (e: Exception) {
                _saveToGmailResult.value = false
                Log.e("CustomerViewModel", "Error saving contact to Gmail", e)
            }
        }
    }

    fun clearSaveResult() {
        _saveToGmailResult.value = null
    }

    fun getGoogleAuthHelper(): GoogleAuthHelper {
        return googleAuthHelper
    }

    fun fetchCustomers() {
        viewModelScope.launch {
            _isLoading.value = true
            _hasError.value = false
            try {
                val customers = repository.getAllCustomers()
                _customers.value = customers

                // Populate history sets
                _phoneModelHistory.value = customers.map { it.phoneModel }.toSet()
                _problemHistory.value = customers.map { it.problem }.toSet()
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
        onSuccess: (Customer) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val timestamp = if (customer.createdAt == 0L) System.currentTimeMillis() else customer.createdAt
            val customerWithTimestamp = customer.copy(
                createdAt = timestamp,
                invoiceNumber = currentInvoiceNumber.value ?: ""
            )
            val result = repository.addCustomer(customerWithTimestamp)
            if (result.isSuccess) {
                fetchCustomers()
                fetchNextInvoiceNumber()
                onSuccess(customerWithTimestamp.copy(
                    id = _customers.value.lastOrNull()?.id ?: "",
                    shopOwnerId = repository.getUserId() ?: ""
                ))
            } else {
                onError(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun updateCustomerStatus(customerId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateStatus(customerId, newStatus)
            fetchCustomers()
        }
    }

    fun getStatusMessage(customer: Customer): String {
        val total = customer.totalAmount.ifEmpty { "0" }.toIntOrNull() ?: 0
        val advanced = customer.advanced.ifEmpty { "0" }.toIntOrNull() ?: 0
        val due = total - advanced

        val note = "\nনোট: মেরামতের পর ৩০ দিনের মধ্যে ডিভাইস সংগ্রহ করুন, অন্যথায় দোকান কর্তৃপক্ষ দায়ী থাকবে না。"

        return when (customer.status) {
            "Repaired" -> "প্রিয় ${customer.customerName}, আপনার ডিভাইসটি মেরামত সম্পন্ন হয়েছে। " +
                    "অনুগ্রহ করে যত দ্রুত সম্ভব সংগ্রহ করুন। " +
                    "মোট: ৳$total, অগ্রিম: ৳$advanced, বাকি: ৳$due। " +
                    "Invoice: ${customer.invoiceNumber}.$note"

            "Pending" -> "প্রিয় ${customer.customerName}, আপনার ডিভাইসটি রিপেয়ারের জন্য গ্রহণ করা হয়েছে। " +
                    "মোট: ৳$total, অগ্রিম: ৳$advanced, বাকি: ৳$due। " +
                    "Invoice: ${customer.invoiceNumber}.$note"

            "Delivered" -> "প্রিয় ${customer.customerName}, আপনার ডিভাইসটি সফলভাবে ডেলিভারি করা হয়েছে। " +
                    "Invoice: ${customer.invoiceNumber}. ধন্যবাদ。"

            "Cancelled" -> "প্রিয় ${customer.customerName}, আপনার রিপেয়ার অনুরোধ বাতিল হয়েছে। " +
                    "ভবিষ্যতে আবার যোগাযোগ করুন。"

            else -> "প্রিয় ${customer.customerName}, আপনার ডিভাইসের বর্তমান স্ট্যাটাস: ${customer.status}.$note"
        }
    }

    fun fetchNextInvoiceNumber() {
        viewModelScope.launch {
            try {
                _currentInvoiceNumber.value = repository.peekNextInvoiceNumber()
            } catch (e: Exception) {
                _currentInvoiceNumber.value = null
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

    fun addUserPhoneModel(model: String) {
        _userPhoneModels.value = _userPhoneModels.value + model
    }

    fun addUserProblem(problem: String) {
        _userProblems.value = _userProblems.value + problem
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