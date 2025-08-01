package com.example.repairstoremanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.data.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CustomerViewModel : ViewModel() {
    private val repository = CustomerRepository()

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers

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
            _customers.value = repository.getAllCustomers()
        }
    }

    fun updateCustomerStatus(customerId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateStatus(customerId, newStatus)
            fetchCustomers()
        }
    }

}
