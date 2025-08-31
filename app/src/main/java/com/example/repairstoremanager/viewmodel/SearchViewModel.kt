package com.example.repairstoremanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.data.repository.SearchRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

class SearchViewModel : ViewModel() {
    private val searchRepository = SearchRepository()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<Customer>>(emptyList())
    val searchResults: StateFlow<List<Customer>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError

    // Debounce and job management
    private var searchJob: Job? = null
    private val debounceTime = 600L // Optimal debounce time

    init {
        setupSearchListener()
    }

    private fun setupSearchListener() {
        viewModelScope.launch {
            _searchQuery.collect { query ->
                if (query.isNotBlank() && query.length >= 2) {
                    performSearchWithDebounce(query)
                } else {
                    clearSearchResults()
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun performSearchWithDebounce(query: String) {
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = null

            try {
                // Debounce delay
                delay(debounceTime)

                // Check if query still valid after debounce
                if (_searchQuery.value.isNotBlank() && _searchQuery.value.length >= 2) {
                    // Use the simple search method for compatibility
                    val results = searchRepository.searchCustomersSimple(query)
                    _searchResults.value = results
                    Log.d("CustomerSearch", "Search completed: ${results.size} results for '$query'")
                }
            } catch (e: Exception) {
                _searchError.value = "Search failed: ${e.message}"
                _searchResults.value = emptyList()
                Log.e("CustomerSearch", "Search error for query: $query", e)
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _searchQuery.value = ""
        clearSearchResults()
    }

    private fun clearSearchResults() {
        _searchResults.value = emptyList()
        _isSearching.value = false
        _searchError.value = null
    }

    fun retrySearch() {
        val currentQuery = _searchQuery.value
        if (currentQuery.isNotBlank()) {
            performSearchWithDebounce(currentQuery)
        }
    }

    // Utility methods
    fun hasSearchResults(): Boolean {
        return _searchResults.value.isNotEmpty() && _searchQuery.value.isNotBlank()
    }

    fun isSearchActive(): Boolean {
        return _searchQuery.value.isNotBlank()
    }
}