package com.example.repairstoremanager.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repairstoremanager.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repository = AuthRepository()

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var loginSuccess by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    val isUserLoggedIn: Boolean
        get() = repository.isUserLoggedIn()

    fun login(storeViewModel: StoreViewModel) {
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

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.logout()
                onLogoutComplete()
            } catch (e: Exception) {
                errorMessage = "Logout failed: ${e.message}"
                onLogoutComplete() // still navigate back
            }
        }
    }
}

class ForgotPasswordViewModel : ViewModel() {
    var email by mutableStateOf("")
    var message by mutableStateOf<String?>(null)
    var error by mutableStateOf<String?>(null)

    private val auth = FirebaseAuth.getInstance()

    fun sendResetLink() {
        message = null
        error = null
        auth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener {
                message = "Reset link sent to your email."
            }
            .addOnFailureListener {
                error = it.localizedMessage
            }
    }
}
