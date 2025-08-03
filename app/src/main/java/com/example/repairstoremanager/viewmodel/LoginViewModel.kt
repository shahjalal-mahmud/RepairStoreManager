package com.example.repairstoremanager.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repairstoremanager.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val storeViewModel: StoreViewModel) : ViewModel() {
    private val repository = AuthRepository()

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var loginSuccess by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    val isUserLoggedIn: Boolean
        get() = repository.isUserLoggedIn()

    fun login() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = repository.loginUser(email.trim(), password)
            isLoading = false
            if (result.isSuccess) {
                storeViewModel.loadStoreInfo()
                loginSuccess = true
            } else {
                errorMessage = result.exceptionOrNull()?.localizedMessage
            }
        }
    }
}
