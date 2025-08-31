package com.example.repairstoremanager.data.model

data class TransactionProduct(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1
)

data class Transaction(
    val id: String = "",
    val shopOwnerId: String = "",
    val invoiceNumber: String = "",
    val customerId: String = "",
    val type: String = "", // "Service", "ProductSale", "Expense"
    val description: String = "",
    val amount: Double = 0.0,
    val advanced: Double = 0.0,
    val due: Double = 0.0,
    val paymentType: String = "Cash",
    val products: List<TransactionProduct> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val date: String = "",
)
