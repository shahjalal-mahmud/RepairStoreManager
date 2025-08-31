package com.example.repairstoremanager.viewmodel

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
    val currentInvoiceNumber: StateFlow<String?> = _currentInvoiceNumber

    private val _cartProducts = MutableStateFlow<List<TransactionProduct>>(emptyList())
    val cartProducts: StateFlow<List<TransactionProduct>> = _cartProducts

    private val _selectedDate = MutableStateFlow(getCurrentDate())
    val selectedDate: StateFlow<String> = _selectedDate

    init {
        fetchNextInvoiceNumber()
        fetchTodayTransactions()
    }

    fun addToCart(product: Product, quantity: Int = 1) {
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
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val totalAmount = getCartTotal()
                val transaction = Transaction(
                    shopOwnerId = repository.getUserId(),
                    invoiceNumber = _currentInvoiceNumber.value ?: "",
                    customerName = customerName,
                    customerPhone = customerPhone,
                    type = "Sale",
                    description = "Product sale - ${_cartProducts.value.size} items",
                    amount = totalAmount,
                    paymentType = paymentType,
                    products = _cartProducts.value,
                    date = getCurrentDateTime(),
                    status = "Completed"
                )

                // Save transaction
                val result = repository.addTransaction(transaction)

                if (result.isSuccess) {
                    // Update stock quantities
                    _cartProducts.value.forEach { product ->
                        repository.updateProductQuantity(product.productId, product.quantity)
                    }

                    // Generate next invoice number
                    fetchNextInvoiceNumber()
                    clearCart()
                    fetchTodayTransactions()

                    onResult(true, transaction.invoiceNumber)
                } else {
                    onResult(false, null)
                }
            } catch (e: Exception) {
                onResult(false, null)
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

    fun getDailySalesTotal(): Double {
        return _transactions.value.filter { it.type == "Sale" }.sumOf { it.amount }
    }

    fun getDailyTransactionCount(): Int {
        return _transactions.value.size
    }

    fun getTopSellingProducts(): Map<String, Int> {
        val productSales = mutableMapOf<String, Int>()
        _transactions.value.forEach { transaction ->
            transaction.products.forEach { product ->
                productSales[product.name] = (productSales[product.name] ?: 0) + product.quantity
            }
        }
        return productSales.toList().sortedByDescending { it.second }.take(5).toMap()
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    private fun getCurrentDateTime(): String {
        return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
    }
}