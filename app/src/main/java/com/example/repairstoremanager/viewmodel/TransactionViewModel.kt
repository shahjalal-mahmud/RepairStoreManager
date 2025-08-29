package com.example.repairstoremanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.data.model.Product
import com.example.repairstoremanager.data.model.Transaction
import com.example.repairstoremanager.data.model.TransactionProduct
import com.example.repairstoremanager.data.repository.CustomerRepository
import com.example.repairstoremanager.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionViewModel : ViewModel() {
    private val repository = TransactionRepository()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _currentInvoiceNumber = MutableStateFlow<String?>(null)
    val currentInvoiceNumber: StateFlow<String?> = _currentInvoiceNumber

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer

    private val _products = MutableStateFlow<List<TransactionProduct>>(emptyList())
    val products: StateFlow<List<TransactionProduct>> = _products


    fun addTransaction(transaction: Transaction, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.addTransaction(transaction)
            onResult(result.isSuccess)
            fetchTodayTransactions()
        }
    }

    fun fetchTodayTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            _transactions.value = repository.getTransactionsByDate(today)
            _isLoading.value = false
        }
    }

    fun calculateSummary(): Map<String, Double> {
        val summary = mutableMapOf<String, Double>()
        _transactions.value.forEach {
            summary[it.type] = (summary[it.type] ?: 0.0) + it.amount
        }
        return summary
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

    fun searchCustomerByInvoice(invoice: String, customerRepo: CustomerRepository) {
        viewModelScope.launch {
            val customer = customerRepo.getCustomerByInvoice(invoice)
            _selectedCustomer.value = customer
        }
    }
    fun addProduct(product: Product) {
        _products.value = _products.value + TransactionProduct(
            productId = product.id,
            name = product.name,
            price = product.sellingPrice,
            quantity = 1
        )
    }

    fun updateProductQuantity(productId: String, quantity: Int) {
        _products.value = _products.value.map {
            if (it.productId == productId) it.copy(quantity = quantity) else it
        }
    }

    fun removeProduct(productId: String) {
        _products.value = _products.value.filter { it.productId != productId }
    }

    fun saveTransaction(
        customer: Customer?,
        paymentType: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val totalProducts = _products.value.sumOf { it.price * it.quantity }
            val totalService = customer?.totalAmount?.toDoubleOrNull() ?: 0.0
            val advanced = customer?.advanced?.toDoubleOrNull() ?: 0.0
            val total = totalService + totalProducts
            val due = total - advanced

            val transaction = Transaction(
                shopOwnerId = repository.getUserId(),
                invoiceNumber = currentInvoiceNumber.value ?: "",
                customerId = customer?.id ?: "",
                type = "Service+Product",
                description = "Invoice for ${customer?.customerName ?: "New Customer"}",
                amount = total,
                advanced = advanced,
                due = due,
                paymentType = paymentType,
                products = _products.value,
                date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            )

            val result = repository.addTransaction(transaction)
            onResult(result.isSuccess)
        }
    }


    fun getTotalIncome(): Double = _transactions.value.sumOf { it.amount }
}