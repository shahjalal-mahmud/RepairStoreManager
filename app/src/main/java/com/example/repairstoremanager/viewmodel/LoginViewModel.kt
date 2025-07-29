package com.example.repairstoremanager.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repairstoremanager.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var loginSuccess by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val repository = AuthRepository()

    fun login() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = repository.loginUser(email.trim(), password)
            isLoading = false
            if (result.isSuccess) {
                loginSuccess = true
            } else {
                errorMessage = result.exceptionOrNull()?.localizedMessage
            }
        }
    }
}