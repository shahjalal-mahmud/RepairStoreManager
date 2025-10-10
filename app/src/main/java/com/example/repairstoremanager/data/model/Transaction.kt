package com.example.repairstoremanager.data.model

data class TransactionProduct(
    val productId: String = "",
    val name: String = "",
    val buyingPrice: Double = 0.0,  // Added buying price for profit calculation
    val sellingPrice: Double = 0.0, // Renamed from 'price' for clarity
    val quantity: Int = 1,
    val totalSellingPrice: Double = sellingPrice * quantity,
    val totalCost: Double = buyingPrice * quantity,
    val profit: Double = totalSellingPrice - totalCost
)

data class Transaction(
    val id: String = "",
    val shopOwnerId: String = "",
    val invoiceNumber: String = "",
    val customerName: String = "Walk-in Customer",
    val type: String = "Sale", // Simplified - focus on sales
    val products: List<TransactionProduct> = emptyList(),
    val totalAmount: Double = 0.0,
    val totalCost: Double = 0.0,
    val totalProfit: Double = 0.0,
    val paymentType: String = "Cash",
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = "" // Format: "dd MMM yyyy"
) {
    // Helper function to calculate totals
    fun calculateTotals(): Transaction {
        val totalAmount = products.sumOf { it.totalSellingPrice }
        val totalCost = products.sumOf { it.totalCost }
        val totalProfit = products.sumOf { it.profit }

        return this.copy(
            totalAmount = totalAmount,
            totalCost = totalCost,
            totalProfit = totalProfit
        )
    }
}

// Simple daily summary data class
data class DailySummary(
    val date: String = "",
    val totalSales: Double = 0.0,
    val totalCost: Double = 0.0,
    val totalProfit: Double = 0.0,
    val transactionCount: Int = 0,
    val productsSold: Map<String, Int> = emptyMap() // Product name to quantity
)