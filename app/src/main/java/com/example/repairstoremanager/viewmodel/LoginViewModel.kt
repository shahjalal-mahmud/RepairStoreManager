package com.example.repairstoremanager.viewmodel

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repairstoremanager.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
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

class ForgotPasswordViewModel : ViewModel() {
    var email by mutableStateOf("")
    var message by mutableStateOf<String?>(null)
    var error by mutableStateOf<String?>(null)

    private val auth = FirebaseAuth.getInstance()

    fun sendResetLink(context: Context) {
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

