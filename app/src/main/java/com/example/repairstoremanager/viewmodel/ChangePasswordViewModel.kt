package com.example.repairstoremanager.viewmodel


import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repairstoremanager.data.repository.AuthRepository
import kotlinx.coroutines.launch

class ChangePasswordViewModel : ViewModel() {
    private val repository = AuthRepository()

    var currentPassword by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var successMessage by mutableStateOf<String?>(null)
    var errorMessage by mutableStateOf<String?>(null)

    fun changePassword() {
        // Reset messages
        successMessage = null
        errorMessage = null

        // Validation
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            errorMessage = "Please fill in all fields"
            return
        }

        if (newPassword.length < 6) {
            errorMessage = "New password must be at least 6 characters"
            return
        }

        if (newPassword != confirmPassword) {
            errorMessage = "New passwords do not match"
            return
        }

        isLoading = true

        viewModelScope.launch {
            val result = repository.changePassword(currentPassword, newPassword)
            isLoading = false

            if (result.isSuccess) {
                successMessage = "Password changed successfully!"
                // Clear fields on success
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "Failed to change password"
            }
        }
    }

    fun clearMessages() {
        successMessage = null
        errorMessage = null
    }
}