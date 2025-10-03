package com.example.repairstoremanager.data.model

data class TransactionProduct(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val total: Double = price * quantity
)

data class Transaction(
    val id: String = "",
    val shopOwnerId: String = "",
    val invoiceNumber: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val type: String = "", // "Sale", "Purchase", "Service", "Expense", "Income"
    val description: String = "",
    val amount: Double = 0.0,
    val cost: Double = 0.0, // Add cost for profit calculation
    val profit: Double = amount - cost, // Auto-calculated profit
    val advanced: Double = 0.0,
    val due: Double = 0.0,
    val paymentType: String = "Cash",
    val products: List<TransactionProduct> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val date: String = "",
    val status: String = "Completed", // "Completed", "Pending", "Cancelled"
    val category: String = "" // For expenses: "Rent", "Salary", "Utilities", etc.
)

// Add this for purchase transactions
data class PurchaseProduct(
    val productId: String = "",
    val name: String = "",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val quantity: Int = 1,
    val totalCost: Double = purchasePrice * quantity
)