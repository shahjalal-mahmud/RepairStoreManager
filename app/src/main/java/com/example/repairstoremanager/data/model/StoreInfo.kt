package com.example.repairstoremanager.data.model

data class StoreInfo(
    val storeName: String = "",
    val logoUrl: String = "",
    val logoBase64: String = "",
    val ownerName: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val workingHours: String = "",
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
)