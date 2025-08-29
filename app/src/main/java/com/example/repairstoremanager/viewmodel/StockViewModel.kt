package com.example.repairstoremanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repairstoremanager.data.model.Product
import com.example.repairstoremanager.data.repository.StockRepository
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
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.addProduct(product)
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
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.updateProduct(product)
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
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.incrementQuantity(productId, delta)

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

}