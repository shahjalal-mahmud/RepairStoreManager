package com.example.repairstoremanager.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repairstoremanager.data.model.Product
import com.example.repairstoremanager.data.model.Transaction
import com.example.repairstoremanager.data.model.TransactionProduct
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

    private val _cartProducts = MutableStateFlow<List<TransactionProduct>>(emptyList())
    val cartProducts: StateFlow<List<TransactionProduct>> = _cartProducts

    private val _selectedDate = MutableStateFlow(getCurrentDate())
    val selectedDate: StateFlow<String> = _selectedDate

    private val _showAllTransactions = MutableStateFlow(false)
    val showAllTransactions: StateFlow<Boolean> = _showAllTransactions

    init {
        Log.d("TransactionVM", "ViewModel initialized")
        fetchNextInvoiceNumber()
        setupRealTimeTransactions()
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        Log.d("TransactionVM", "Adding to cart: ${product.name}, quantity: $quantity")

        val existingProduct = _cartProducts.value.find { it.productId == product.id }

        if (existingProduct != null) {
            updateCartQuantity(product.id, existingProduct.quantity + quantity)
        } else {
            _cartProducts.value = _cartProducts.value + TransactionProduct(
                productId = product.id,
                name = product.name,
                price = product.sellingPrice,
                quantity = quantity
            )
        }
    }

    fun updateCartQuantity(productId: String, quantity: Int) {
        _cartProducts.value = _cartProducts.value.map {
            if (it.productId == productId) it.copy(quantity = quantity.coerceAtLeast(1)) else it
        }
    }

    fun updateCartPrice(productId: String, price: Double) {
        _cartProducts.value = _cartProducts.value.map {
            if (it.productId == productId) it.copy(price = price.coerceAtLeast(0.0)) else it
        }
    }

    fun removeFromCart(productId: String) {
        _cartProducts.value = _cartProducts.value.filter { it.productId != productId }
    }

    fun clearCart() {
        _cartProducts.value = emptyList()
    }

    fun getCartTotal(): Double {
        return _cartProducts.value.sumOf { it.price * it.quantity }
    }

    fun createSaleTransaction(
        customerName: String = "Walk-in Customer",
        customerPhone: String = "",
        paymentType: String = "Cash",
        onResult: (Boolean, String?, String?) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("TransactionVM", "Creating sale transaction")

            try {
                val totalAmount = getCartTotal()
                val transaction = Transaction(
                    shopOwnerId = repository.getUserId(),
                    invoiceNumber = _currentInvoiceNumber.value ?: "INV-0001",
                    customerName = customerName,
                    customerPhone = customerPhone,
                    type = "Sale",
                    description = "Product sale - ${_cartProducts.value.size} items",
                    amount = totalAmount,
                    paymentType = paymentType,
                    products = _cartProducts.value,
                    date = getCurrentDateOnly(),
                    createdAt = System.currentTimeMillis(),
                    status = "Completed"
                )

                Log.d("TransactionVM", "Transaction created: ${transaction.invoiceNumber}")

                // First update all product quantities
                val updateResults = _cartProducts.value.map { product ->
                    repository.updateProductQuantity(product.productId, product.quantity)
                }

                // Check if any stock update failed
                val failedUpdates = updateResults.filter { it.isFailure }
                if (failedUpdates.isNotEmpty()) {
                    val errorMsg = "Failed to update stock for some products"
                    Log.e("TransactionVM", errorMsg)
                    onResult(false, null, errorMsg)
                    return@launch
                }

                // Then save the transaction
                val result = repository.addTransaction(transaction)

                if (result.isSuccess) {
                    Log.d("TransactionVM", "Transaction saved successfully")

                    // Generate next invoice number
                    fetchNextInvoiceNumber()
                    clearCart()

                    // Force refresh transactions
                    if (_showAllTransactions.value) {
                        fetchAllTransactions()
                    } else {
                        fetchTodayTransactions()
                    }

                    onResult(true, transaction.invoiceNumber, null)
                } else {
                    val errorMsg = "Transaction failed: ${result.exceptionOrNull()?.message}"
                    Log.e("TransactionVM", errorMsg)
                    onResult(false, null, errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message}"
                Log.e("TransactionVM", errorMsg, e)
                onResult(false, null, errorMsg)
            } finally {
                _isLoading.value = false
            }
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

    fun fetchTodayTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _transactions.value = repository.getTransactionsByDate(getCurrentDate())
            } catch (e: Exception) {
                _transactions.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchTransactionsByDate(date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _transactions.value = repository.getTransactionsByDate(date)
                _selectedDate.value = date
            } catch (e: Exception) {
                _transactions.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchAllTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _transactions.value = repository.getAllTransactions()
            } catch (e: Exception) {
                _transactions.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleTransactionView(showAll: Boolean) {
        _showAllTransactions.value = showAll
        if (showAll) {
            fetchAllTransactions()
        } else {
            fetchTodayTransactions()
        }
    }

    fun getDailySalesTotal(): Double {
        return _transactions.value.filter { it.type == "Sale" }.sumOf { it.amount }
    }

    fun getDailyTransactionCount(): Int {
        return _transactions.value.size
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    private fun getCurrentDateOnly(): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    private fun setupRealTimeTransactions() {
        repository.getTransactionsRealTimeListener { transactions ->
            Log.d("TransactionVM", "Real-time update received: ${transactions.size} transactions")
            _transactions.value = transactions
        }
    }
}