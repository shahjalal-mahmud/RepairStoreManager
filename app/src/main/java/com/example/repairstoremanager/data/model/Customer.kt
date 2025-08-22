package com.example.repairstoremanager.data.model

data class Customer(
    val id: String = "", // UUID or Firebase auto-id
    val shopOwnerId: String = "", // for future multiple owners
    val invoiceNumber: String = "",
    val date: String = "",
    val customerName: String = "",
    val contactNumber: String = "",
    val phoneModel: String = "",
    val problem: String = "",
    val deliveryDate: String = "",
    val totalAmount: String = "",
    val advanced: String = "",
    val securityType: String = "",
    val phonePassword: String = "",
    val pattern: List<Int> = emptyList(),
    val battery: Boolean = false,
    val sim: Boolean = false,
    val memory: Boolean = false,
    val simTray: Boolean = false,
    val backCover: Boolean = false,
    val deadPermission: Boolean = false,
    val status: String = "Pending",
    val createdAt: Long = 0L,
    val drawerNumber: String = "", // New field for box/drawer number
    val extraDetails: String = ""  // New field for additional details
)