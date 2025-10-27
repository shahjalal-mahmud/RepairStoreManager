package com.example.repairstoremanager.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repairstoremanager.data.model.DailySummary
import com.example.repairstoremanager.data.model.Product
import com.example.repairstoremanager.data.model.Transaction
import com.example.repairstoremanager.data.model.TransactionProduct
import com.example.repairstoremanager.data.repository.TransactionRepository
import com.example.repairstoremanager.ui.components.common.DateRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionViewModel : ViewModel() {
    private val repository = TransactionRepository()

    // State flows
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _dailySummary = MutableStateFlow(DailySummary())
    val dailySummary: StateFlow<DailySummary> = _dailySummary

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _cartProducts = MutableStateFlow<List<TransactionProduct>>(emptyList())
    val cartProducts: StateFlow<List<TransactionProduct>> = _cartProducts

    private val _selectedDate = MutableStateFlow(repository.getCurrentDate())
    val selectedDate: StateFlow<String> = _selectedDate

    private val _showAllTransactions = MutableStateFlow(false)
    val showAllTransactions: StateFlow<Boolean> = _showAllTransactions

    private val _dateRange = MutableStateFlow(DateRange())
    val dateRange: StateFlow<DateRange> = _dateRange

    private val _isFiltering = MutableStateFlow(false)
    val isFiltering: StateFlow<Boolean> = _isFiltering

    init {
        loadTodayTransactions()
        setupRealTimeListener()
    }

    // Cart management
    fun addToCart(product: Product, sellingPrice: Double? = null) {
        val finalSellingPrice = sellingPrice ?: product.sellingPrice

        val existingProduct = _cartProducts.value.find { it.productId == product.id }

        if (existingProduct != null) {
            updateCartQuantity(product.id, existingProduct.quantity + 1)
        } else {
            val transactionProduct = TransactionProduct(
                productId = product.id,
                name = product.name,
                buyingPrice = product.buyingPrice,
                sellingPrice = finalSellingPrice,
                quantity = 1
            )

            _cartProducts.value = _cartProducts.value + transactionProduct
        }
    }

    fun updateCartQuantity(productId: String, quantity: Int) {
        _cartProducts.value = _cartProducts.value.map { product ->
            if (product.productId == productId) {
                product.copy(quantity = quantity.coerceAtLeast(1))
            } else {
                product
            }
        }
    }

    fun updateCartPrice(productId: String, sellingPrice: Double) {
        _cartProducts.value = _cartProducts.value.map { product ->
            if (product.productId == productId) {
                product.copy(sellingPrice = sellingPrice.coerceAtLeast(0.0))
            } else {
                product
            }
        }
    }

    fun removeFromCart(productId: String) {
        _cartProducts.value = _cartProducts.value.filter { it.productId != productId }
    }

    fun clearCart() {
        _cartProducts.value = emptyList()
    }

    fun getCartTotal(): Double {
        return _cartProducts.value.sumOf { it.sellingPrice * it.quantity }
    }

    fun getCartProfit(): Double {
        return _cartProducts.value.sumOf {
            (it.sellingPrice - it.buyingPrice) * it.quantity
        }
    }

    // Transaction operations
    fun createSaleTransaction(
        customerName: String = "Walk-in Customer",
        paymentType: String = "Cash",
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val transaction = Transaction(
                    customerName = customerName,
                    products = _cartProducts.value,
                    paymentType = paymentType,
                    date = repository.getCurrentDate()
                ).calculateTotals()

                val result = repository.saveSaleTransaction(transaction)

                if (result.isSuccess) {
                    clearCart()
                    refreshTransactions()
                    onResult(true, result.getOrNull())
                } else {
                    onResult(false, result.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Data loading
    fun loadTodayTransactions() {
        _selectedDate.value = repository.getCurrentDate()
        loadTransactionsByDate(_selectedDate.value)
    }

    fun loadTransactionsByDate(date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _transactions.value = repository.getTransactionsByDate(date)
                _dailySummary.value = repository.getDailySummary(date)
                _selectedDate.value = date
            } catch (e: Exception) {
                _transactions.value = emptyList()
                _dailySummary.value = DailySummary(date = date)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _transactions.value = repository.getAllSalesTransactions()
                _showAllTransactions.value = true
            } catch (e: Exception) {
                _transactions.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleView(showAll: Boolean) {
        _showAllTransactions.value = showAll
        if (showAll) {
            loadAllTransactions()
        } else {
            loadTodayTransactions()
        }
    }

    private fun setupRealTimeListener() {
        repository.getTransactionsRealTimeListener(_selectedDate.value) { transactions ->
            _transactions.value = transactions

            // Recalculate summary when real-time update comes
            viewModelScope.launch {
                if (!_showAllTransactions.value) {
                    _dailySummary.value = repository.getDailySummary(_selectedDate.value)
                }
            }
        }
    }

    private fun refreshTransactions() {
        if (_showAllTransactions.value) {
            loadAllTransactions()
        } else {
            loadTransactionsByDate(_selectedDate.value)
        }
    }

    // Date formatting
    fun formatDateForDisplay(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
            val parsedDate = inputFormat.parse(date)
            outputFormat.format(parsedDate ?: Date())
        } catch (e: Exception) {
            date
        }
    }
    fun loadTransactionsByDateRange(dateRange: DateRange) {
        viewModelScope.launch {
            _isLoading.value = true
            _dateRange.value = dateRange
            _isFiltering.value = dateRange.isComplete

            try {
                val transactions = if (dateRange.isComplete) {
                    // Use date range filter
                    repository.getTransactionsByDateRange(dateRange.startDate!!, dateRange.endDate!!)
                } else {
                    // Load all transactions
                    repository.getAllSalesTransactions()
                }

                _transactions.value = transactions
                _showAllTransactions.value = !dateRange.isComplete

                Log.d("TransactionVM", "Loaded ${transactions.size} transactions with date range: ${dateRange.getDisplayText()}")

            } catch (e: Exception) {
                Log.e("TransactionVM", "Error loading transactions by date range: ${e.message}", e)
                _transactions.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Update date range
    fun updateDateRange(dateRange: DateRange) {
        _dateRange.value = dateRange
        if (dateRange.isComplete) {
            loadTransactionsByDateRange(dateRange)
        } else if (dateRange.isEmpty) {
            // If cleared, load all transactions
            loadAllTransactions()
        }
    }

    // Clear date range filter
    fun clearDateRangeFilter() {
        _dateRange.value = DateRange()
        _isFiltering.value = false
        loadAllTransactions()
    }
    fun getTransactionForPrinting(invoiceNumber: String): Transaction? {
        return _transactions.value.find { it.invoiceNumber == invoiceNumber }
    }
}