package com.example.repairstoremanager.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repairstoremanager.data.model.StoreInfo
import com.example.repairstoremanager.data.repository.StoreRepository
import kotlinx.coroutines.launch

class StoreViewModel : ViewModel() {
    private val repository = StoreRepository()

    var storeInfo by mutableStateOf(StoreInfo())
        private set

    var isEditMode by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)

    init {
        loadStoreInfo()
    }

    fun loadStoreInfo() {
        viewModelScope.launch {
            storeInfo = repository.getStoreInfo()
        }
    }

    fun toggleEditMode() {
        isEditMode = !isEditMode
    }

    fun updateStoreInfo() {
        viewModelScope.launch {
            val result = repository.updateStoreInfo(storeInfo)
            message = if (result.isSuccess) "Updated Successfully" else result.exceptionOrNull()?.message
            if (result.isSuccess) {
                isEditMode = false
            }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        repository.logout()
        onLogoutComplete()
    }

    // suspend function for password change
    fun changePassword(newPassword: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.changePassword(newPassword)
            onResult(result.exceptionOrNull()?.message)
        }
    }

    fun updateOwnerName(newValue: String) {
        storeInfo = storeInfo.copy(ownerName = newValue)
    }

    fun updateAddress(newValue: String) {
        storeInfo = storeInfo.copy(address = newValue)
    }

    fun updatePhone(newValue: String) {
        storeInfo = storeInfo.copy(phone = newValue)
    }

    fun updateEmail(newValue: String) {
        storeInfo = storeInfo.copy(email = newValue)
    }

    fun updateWorkingHours(newValue: String) {
        storeInfo = storeInfo.copy(workingHours = newValue)
    }
}