package com.example.repairstoremanager.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repairstoremanager.data.model.Product
import com.example.repairstoremanager.data.repository.StockRepository
import com.example.repairstoremanager.util.StockNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StockViewModel : ViewModel() {
    private val repository = StockRepository()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    init {
        fetchProducts()
    }

    fun fetchProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _products.value = repository.getAllProducts()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load products"
                _products.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addProduct(
        product: Product,
        context: Context? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.addProduct(product, context)
            _isLoading.value = false

            if (result.isSuccess) {
                fetchProducts()
                onSuccess()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Failed to add product"
                _error.value = errorMsg
                onError(errorMsg)
            }
        }
    }

    fun updateProduct(
        product: Product,
        context: Context? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.updateProduct(product, context)
            _isLoading.value = false

            if (result.isSuccess) {
                fetchProducts()
                onSuccess()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Failed to update product"
                _error.value = errorMsg
                onError(errorMsg)
            }
        }
    }

    fun deleteProduct(
        productId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.deleteProduct(productId)
            _isLoading.value = false

            if (result.isSuccess) {
                fetchProducts()
                onSuccess()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Failed to delete product"
                _error.value = errorMsg
                onError(errorMsg)
            }
        }
    }

    fun updateQuantity(
        productId: String,
        delta: Long,
        context: Context? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.incrementQuantity(productId, delta, context)
            _isLoading.value = false

            if (result.isSuccess) {
                fetchProducts()
                onSuccess()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Failed to update quantity"
                _error.value = errorMsg
                onError(errorMsg)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (query.isBlank()) {
                    _products.value = repository.getAllProducts()
                } else {
                    _products.value = repository.searchProducts(query)
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to search products"
                _products.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun getProductById(productId: String, onResult: (Product?) -> Unit) {
        viewModelScope.launch {
            try {
                val product = repository.getProductById(productId)
                onResult(product)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load product"
                onResult(null)
            }
        }
    }

    fun checkLowStockOnAppStart(context: Context) {
        viewModelScope.launch {
            try {
                repository.checkLowStockOnAppStart(context)
            } catch (e: Exception) {
                // Log the error but don't show to user as this is a background check
                println("Low stock check failed: ${e.message}")
            }
        }
    }

    // Method to get low stock products
    fun getLowStockProducts(): List<Product> {
        return _products.value.filter { product ->
            product.quantity <= product.alertQuantity && product.alertQuantity > 0
        }
    }

    // Method to check if a specific product is low on stock
    fun isProductLowStock(product: Product): Boolean {
        return product.quantity <= product.alertQuantity && product.alertQuantity > 0
    }

    // Mark all stock notifications as read
    fun markAllStockNotificationsAsRead(context: Context) {
        StockNotificationManager.markAllStockNotificationsAsRead(context)
    }

    // Check if a product has low stock notification
    fun hasLowStockNotification(context: Context, productId: String): Boolean {
        return StockNotificationManager.hasLowStockNotification(context, productId)
    }

    // Force show low stock notification (for testing)
    fun forceLowStockNotification(context: Context) {
        viewModelScope.launch {
            val allProducts = repository.getAllProducts()
            StockNotificationManager.forceShowLowStockNotification(context, allProducts)
        }
    }
}