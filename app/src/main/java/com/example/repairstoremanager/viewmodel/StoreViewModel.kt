package com.example.repairstoremanager.viewmodel

import android.Manifest
import android.content.Context
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.annotation.RequiresPermission
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

    var autoSmsEnabled by mutableStateOf(true)
    var selectedSimSlot by mutableIntStateOf(0)

    val simList = mutableStateListOf<SubscriptionInfo>()

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

    fun changePassword(newPassword: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.changePassword(newPassword)
            onResult(result.exceptionOrNull()?.message)
        }
    }
    fun updateStoreName(newValue: String) {
        storeInfo = storeInfo.copy(storeName = newValue)
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

    fun updateAutoSmsEnabled(enabled: Boolean) {
        autoSmsEnabled = enabled
    }

    // <-- Remove this manual setter to avoid clash
    // fun setSelectedSimSlot(slot: Int) {
    //    selectedSimSlot = slot
    // }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun loadSimList(context: Context) {
        val subscriptionManager =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val sims = subscriptionManager.activeSubscriptionInfoList ?: emptyList()
        simList.clear()
        simList.addAll(sims)
    }
}