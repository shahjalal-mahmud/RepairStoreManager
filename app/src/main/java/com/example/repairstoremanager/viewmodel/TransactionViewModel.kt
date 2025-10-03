package com.example.repairstoremanager.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repairstoremanager.data.model.Product
import com.example.repairstoremanager.data.model.PurchaseProduct
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
    private val DATE_FORMAT = "dd MMM yyyy"

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

    private val _transactionSummary = MutableStateFlow(TransactionRepository.TransactionSummary())
    val transactionSummary: StateFlow<TransactionRepository.TransactionSummary> = _transactionSummary

    init {
        Log.d("TransactionVM", "ViewModel initialized")
        fetchNextInvoiceNumber()
        setupRealTimeTransactions()
        fetchTodayTransactions()
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
    }

    private fun getCurrentDateOnly(): String {
        return SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
    }

    fun formatDateForDisplay(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
            val parsedDate = inputFormat.parse(date)
            outputFormat.format(parsedDate ?: Date())
        } catch (e: Exception) {
            date
        }
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
                val today = getCurrentDate()
                _transactions.value = repository.getTransactionsByDate(today)
                _selectedDate.value = today
                _transactionSummary.value = repository.getTransactionSummaryByDate(today)
            } catch (e: Exception) {
                _transactions.value = emptyList()
                _transactionSummary.value = TransactionRepository.TransactionSummary(date = getCurrentDate())
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
                _transactionSummary.value = repository.getTransactionSummaryByDate(date)
            } catch (e: Exception) {
                _transactions.value = emptyList()
                _transactionSummary.value = TransactionRepository.TransactionSummary(date = date)
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
                // For all transactions view, we don't show the summary
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

    private fun setupRealTimeTransactions() {
        repository.getTransactionsRealTimeListener { allTransactions ->
            Log.d("TransactionVM", "Real-time update received: ${allTransactions.size} transactions")

            if (_showAllTransactions.value) {
                // Show all transactions
                _transactions.value = allTransactions
            } else {
                // Filter by selected date for daily view
                val filtered = allTransactions.filter { it.date == _selectedDate.value }
                _transactions.value = filtered

                // Also update summary with the filtered transactions
                viewModelScope.launch {
                    // Calculate summary from filtered transactions instead of fetching again
                    val summary = calculateSummaryFromTransactions(filtered, _selectedDate.value)
                    _transactionSummary.value = summary
                }
            }
        }
    }

    // Add this helper function to TransactionViewModel
    private fun calculateSummaryFromTransactions(transactions: List<Transaction>, date: String): TransactionRepository.TransactionSummary {
        // Calculate summary from the provided transactions
        val totalSales = transactions.filter { it.type == "Sale" }.sumOf { it.amount }
        val totalServices = transactions.filter { it.type == "Service" }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.type == "Expense" }.sumOf { it.amount }

        // Count products sold
        val productsSold = mutableMapOf<String, Int>()
        transactions.forEach { transaction ->
            transaction.products.forEach { product ->
                productsSold[product.name] = productsSold.getOrDefault(product.name, 0) + product.quantity
            }
        }

        return TransactionRepository.TransactionSummary(
            date = date,
            totalTransactions = transactions.size,
            totalSales = totalSales,
            totalServices = totalServices,
            totalExpenses = totalExpenses,
            productsSold = productsSold
        )
    }
    fun createPurchaseTransaction(
        products: List<PurchaseProduct>,
        supplier: String = "",
        onResult: (Boolean, String?, String?) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.addPurchaseTransaction(products, supplier)
                if (result.isSuccess) {
                    refreshTransactions()
                    onResult(true, "Purchase recorded", null)
                } else {
                    onResult(false, null, result.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                onResult(false, null, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createServiceTransaction(
        customerName: String,
        serviceDescription: String,
        serviceCharge: Double,
        partsCost: Double = 0.0,
        onResult: (Boolean, String?, String?) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.addServiceTransaction(
                    customerName,
                    serviceDescription,
                    serviceCharge,
                    partsCost
                )
                if (result.isSuccess) {
                    refreshTransactions()
                    onResult(true, "Service recorded", null)
                } else {
                    onResult(false, null, result.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                onResult(false, null, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createExpenseTransaction(
        description: String,
        amount: Double,
        category: String,
        onResult: (Boolean, String?, String?) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.addExpenseTransaction(description, amount, category)
                if (result.isSuccess) {
                    refreshTransactions()
                    onResult(true, "Expense recorded", null)
                } else {
                    onResult(false, null, result.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                onResult(false, null, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createIncomeTransaction(
        description: String,
        amount: Double,
        category: String = "Other",
        onResult: (Boolean, String?, String?) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.addIncomeTransaction(description, amount, category)
                if (result.isSuccess) {
                    refreshTransactions()
                    onResult(true, "Income recorded", null)
                } else {
                    onResult(false, null, result.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                onResult(false, null, e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun refreshTransactions() {
        if (_showAllTransactions.value) {
            fetchAllTransactions()
        } else {
            fetchTransactionsByDate(_selectedDate.value)
        }
    }

    // Enhanced summary calculation
    suspend fun getFinancialSummary(startDate: String, endDate: String): FinancialSummary {
        return try {
            val transactions = repository.getTransactionsByDateRange(startDate, endDate)

            val totalRevenue = transactions
                .filter { it.type == "Sale" || it.type == "Service" || it.type == "Income" }
                .sumOf { it.amount }

            val totalCost = transactions
                .filter { it.type == "Sale" || it.type == "Purchase" || it.type == "Expense" }
                .sumOf { it.cost }

            val totalProfit = totalRevenue - totalCost

            val salesByCategory = transactions
                .filter { it.type == "Sale" }
                .groupBy { it.category }
                .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }

            FinancialSummary(
                startDate = startDate,
                endDate = endDate,
                totalRevenue = totalRevenue,
                totalCost = totalCost,
                totalProfit = totalProfit,
                salesByCategory = salesByCategory,
                transactionCount = transactions.size
            )
        } catch (e: Exception) {
            FinancialSummary(startDate = startDate, endDate = endDate)
        }
    }

    data class FinancialSummary(
        val startDate: String = "",
        val endDate: String = "",
        val totalRevenue: Double = 0.0,
        val totalCost: Double = 0.0,
        val totalProfit: Double = 0.0,
        val salesByCategory: Map<String, Double> = emptyMap(),
        val transactionCount: Int = 0
    )
}