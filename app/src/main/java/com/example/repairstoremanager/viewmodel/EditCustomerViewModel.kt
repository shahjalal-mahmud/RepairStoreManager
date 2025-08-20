package com.example.repairstoremanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.data.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditCustomerViewModel : ViewModel() {
    private val repository = CustomerRepository()

    private val _customer = MutableStateFlow(Customer())
    val customer: StateFlow<Customer> = _customer.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    fun loadCustomer(customerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val customers = repository.getAllCustomers()
                _customer.value = customers.find { it.id == customerId } ?: Customer()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load customer: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCustomer(
        updatedCustomer: Customer,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isSuccess.value = false

            try {
                val result = repository.updateCustomer(updatedCustomer)
                if (result.isSuccess) {
                    _customer.value = updatedCustomer
                    _isSuccess.value = true
                    onSuccess()
                } else {
                    _errorMessage.value = "Failed to update customer"
                    onError(_errorMessage.value ?: "Unknown error")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                onError(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _errorMessage.value = null
        _isSuccess.value = false
    }

    fun updateField(field: String, value: Any) {
        val currentCustomer = _customer.value
        val updatedCustomer = when (field) {
            "customerName" -> currentCustomer.copy(customerName = value as String)
            "contactNumber" -> currentCustomer.copy(contactNumber = value as String)
            "phoneModel" -> currentCustomer.copy(phoneModel = value as String)
            "problem" -> currentCustomer.copy(problem = value as String)
            "deliveryDate" -> currentCustomer.copy(deliveryDate = value as String)
            "totalAmount" -> currentCustomer.copy(totalAmount = value as String)
            "advanced" -> currentCustomer.copy(advanced = value as String)
            "battery" -> currentCustomer.copy(battery = value as Boolean)
            "sim" -> currentCustomer.copy(sim = value as Boolean)
            "memory" -> currentCustomer.copy(memory = value as Boolean)
            "simTray" -> currentCustomer.copy(simTray = value as Boolean)
            "backCover" -> currentCustomer.copy(backCover = value as Boolean)
            "deadPermission" -> currentCustomer.copy(deadPermission = value as Boolean)
            else -> currentCustomer
        }
        _customer.value = updatedCustomer
    }
}