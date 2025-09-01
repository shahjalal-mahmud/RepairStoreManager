package com.example.repairstoremanager.viewmodel

import android.Manifest
import android.content.Context
import android.net.Uri
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repairstoremanager.data.model.StoreInfo
import com.example.repairstoremanager.data.repository.StoreRepository
import com.example.repairstoremanager.worker.WorkScheduler
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.util.Base64

class StoreViewModel : ViewModel() {

    private val repository = StoreRepository()

    var storeInfo by mutableStateOf(StoreInfo())
        private set

    var isEditMode by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)

    var autoSmsEnabled by mutableStateOf(true)
    var selectedSimSlot by mutableIntStateOf(0)

    val simList = mutableStateListOf<SubscriptionInfo>()
    var logoUri by mutableStateOf<Uri?>(null)

    fun onLogoPicked(uri: Uri, context: Context) {
        logoUri = uri
        uploadLogoAsBase64(uri, context)
    }

    private fun uploadLogoAsBase64(uri: Uri, context: Context) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val imageBytes = inputStream?.readBytes()
            inputStream?.close()

            if (imageBytes != null) {
                val base64String = Base64.getEncoder().encodeToString(imageBytes)
                val userId = Firebase.auth.currentUser?.uid ?: return

                Firebase.firestore.collection("stores")
                    .document(userId)
                    .update("logoBase64", base64String)
                    .addOnSuccessListener {
                        storeInfo = storeInfo.copy(logoBase64 = base64String)
                        message = "Logo updated"
                    }
                    .addOnFailureListener {
                        message = "Failed to update logo"
                    }
            } else {
                message = "Failed to read image"
            }
        } catch (e: Exception) {
            message = "Image error: ${e.message}"
        }
    }

    init {
        loadStoreInfo()
    }
    fun saveAllChanges() {
        viewModelScope.launch {
            // Update store info
            val storeResult = repository.updateStoreInfo(storeInfo)

            // Update owner details specifically
            val ownerResult = repository.updateOwnerDetails(
                storeInfo.ownerName,
                storeInfo.ownerEmail,
                storeInfo.ownerPhone
            )

            message = if (storeResult.isSuccess && ownerResult.isSuccess) {
                isEditMode = false
                "All changes saved successfully"
            } else {
                val errors = listOf(
                    storeResult.exceptionOrNull()?.message,
                    ownerResult.exceptionOrNull()?.message
                ).filterNotNull().joinToString(", ")
                "Failed to save changes: $errors"
            }
        }
    }

    fun cancelEdit() {
        // Reload original data from Firebase
        loadStoreInfo()
        isEditMode = false
        message = "Changes cancelled"
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

    fun updateOwnerDetails() {
        viewModelScope.launch {
            val result = repository.updateOwnerDetails(
                storeInfo.ownerName,
                storeInfo.ownerEmail,
                storeInfo.ownerPhone
            )
            message = if (result.isSuccess) "Owner details updated" else result.exceptionOrNull()?.message
        }
    }

    fun updateReminderTime(context: Context, hour: Int, minute: Int) {
        val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("hour", hour).putInt("minute", minute).apply()

        WorkScheduler.scheduleDailyReminder(context, hour, minute)
        WorkScheduler.triggerWorkerImmediately(context)
    }

    fun cancelReminderTime(context: Context) {
        val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("hour").remove("minute").apply()
        WorkScheduler.cancelReminder(context)
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

    fun updateOwnerEmail(newValue: String) {
        storeInfo = storeInfo.copy(ownerEmail = newValue)
    }

    fun updateOwnerPhone(newValue: String) {
        storeInfo = storeInfo.copy(ownerPhone = newValue)
    }

    fun updateWorkingHours(newValue: String) {
        storeInfo = storeInfo.copy(workingHours = newValue)
    }

    fun updateSubscriptionPlan(newValue: String) {
        storeInfo = storeInfo.copy(subscriptionPlan = newValue)
    }

    fun updateAutoSmsEnabled(enabled: Boolean) {
        autoSmsEnabled = enabled
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun loadSimList(context: Context) {
        val subscriptionManager =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val sims = subscriptionManager.activeSubscriptionInfoList ?: emptyList()
        simList.clear()
        simList.addAll(sims)
    }
}