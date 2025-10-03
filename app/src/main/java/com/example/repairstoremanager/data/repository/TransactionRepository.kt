package com.example.repairstoremanager.data.repository

import android.util.Log
import com.example.repairstoremanager.data.model.PurchaseProduct
import com.example.repairstoremanager.data.model.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun addTransaction(transaction: Transaction): Result<Unit> {
        return try {
            Log.d("TransactionRepo", "Attempting to save transaction: ${transaction.invoiceNumber}")

            val docRef = db.collection("transactions").document()
            val withId = transaction.copy(id = docRef.id)

            docRef.set(withId).await()

            Log.d("TransactionRepo", "Transaction saved successfully: ${transaction.invoiceNumber}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error saving transaction: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getTransactionsByDate(date: String): List<Transaction> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            Log.d("TransactionRepo", "Fetching transactions for date: $date")

            val result = db.collection("transactions")
                .whereEqualTo("shopOwnerId", uid)
                .whereEqualTo("date", date)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Transaction::class.java)

            Log.d("TransactionRepo", "Found ${result.size} transactions for date: $date")
            result
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error fetching transactions by date: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getAllTransactions(): List<Transaction> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            Log.d("TransactionRepo", "Fetching all transactions")

            val result = db.collection("transactions")
                .whereEqualTo("shopOwnerId", uid)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Transaction::class.java)

            Log.d("TransactionRepo", "Found ${result.size} total transactions")
            result
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error fetching all transactions: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun peekNextInvoiceNumber(): String {
        val uid = auth.currentUser?.uid ?: return "INV-0001"
        return try {
            Log.d("TransactionRepo", "Peeking next invoice number")

            val snapshot = db.collection("transactions")
                .whereEqualTo("shopOwnerId", uid)
                .orderBy("invoiceNumber", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Log.d("TransactionRepo", "No transactions found, starting with INV-0001")
                "INV-0001"
            } else {
                val lastInvoice = snapshot.documents.first().getString("invoiceNumber") ?: "INV-0000"
                val number = lastInvoice.substringAfter("INV-").toIntOrNull() ?: 0
                val nextInvoice = "INV-${String.format("%04d", number + 1)}"

                Log.d("TransactionRepo", "Next invoice number: $nextInvoice")
                nextInvoice
            }
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error peeking next invoice: ${e.message}", e)
            "INV-0001"
        }
    }

    fun getUserId(): String {
        return auth.currentUser?.uid ?: ""
    }

    suspend fun updateProductQuantity(productId: String, quantityChange: Int): Result<Unit> {
        return try {
            Log.d("TransactionRepo", "Updating product quantity for ID: $productId, change: $quantityChange")

            val productRef = db.collection("products").document(productId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(productRef)
                if (snapshot.exists()) {
                    val currentQuantity = snapshot.getLong("quantity") ?: 0L
                    val newQuantity = (currentQuantity - quantityChange).coerceAtLeast(0L)

                    transaction.update(productRef, mapOf(
                        "quantity" to newQuantity,
                        "updatedAt" to System.currentTimeMillis()
                    ))

                    Log.d("TransactionRepo", "Product quantity updated: $currentQuantity -> $newQuantity")
                } else {
                    throw IllegalStateException("Product not found")
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error updating product quantity: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun getTransactionsRealTimeListener(onUpdate: (List<Transaction>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return

        Log.d("TransactionRepo", "Setting up real-time listener for transactions")

        db.collection("transactions")
            .whereEqualTo("shopOwnerId", uid)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TransactionRepo", "Real-time listener error: ${error.message}", error)
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val transactions = it.toObjects(Transaction::class.java)
                    Log.d("TransactionRepo", "Real-time update: ${transactions.size} transactions")
                    onUpdate(transactions)
                }
            }
    }
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun getCurrentDateOnly(): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Add this function to get transaction summary
    suspend fun getTransactionSummaryByDate(date: String): TransactionSummary {
        val uid = auth.currentUser?.uid ?: return TransactionSummary()

        return try {
            val transactions = db.collection("transactions")
                .whereEqualTo("shopOwnerId", uid)
                .whereEqualTo("date", date)
                .get()
                .await()
                .toObjects(Transaction::class.java)

            // Calculate summary
            val totalSales = transactions.filter { it.type == "Sale" }.sumOf { it.amount }
            val totalServices = transactions.filter { it.type == "Service" }.sumOf { it.amount }
            val totalExpenses = transactions.filter { it.type == "Expense" }.sumOf { it.amount }

            // Count products sold
            val productsSold = mutableMapOf<String, Int>()
            transactions.forEach { transaction ->
                transaction.products.forEach { product ->
                    productsSold[product.name] = productsSold.getOrDefault(product.name, 0) + product.quantity
                }
            }

            TransactionSummary(
                date = date,
                totalTransactions = transactions.size,
                totalSales = totalSales,
                totalServices = totalServices,
                totalExpenses = totalExpenses,
                productsSold = productsSold
            )
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error getting transaction summary: ${e.message}", e)
            TransactionSummary(date = date)
        }
    }
    suspend fun addPurchaseTransaction(products: List<PurchaseProduct>, supplier: String = ""): Result<Unit> {
        return try {
            val totalCost = products.sumOf { it.totalCost }
            val transaction = Transaction(
                shopOwnerId = getUserId(),
                invoiceNumber = generatePurchaseInvoiceNumber(),
                customerName = supplier,
                type = "Purchase",
                description = "Stock purchase from $supplier",
                amount = totalCost,
                cost = totalCost,
                paymentType = "Cash",
                date = getCurrentDate(),
                status = "Completed"
            )

            // Update stock quantities (increase)
            products.forEach { product ->
                updateStockQuantity(product.productId, product.quantity, true)
            }

            addTransaction(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addServiceTransaction(
        customerName: String,
        serviceDescription: String,
        serviceCharge: Double,
        partsCost: Double = 0.0
    ): Result<Unit> {
        return try {
            val transaction = Transaction(
                shopOwnerId = getUserId(),
                invoiceNumber = generateServiceInvoiceNumber(),
                customerName = customerName,
                type = "Service",
                description = serviceDescription,
                amount = serviceCharge,
                cost = partsCost,
                paymentType = "Cash",
                date = getCurrentDate(),
                status = "Completed"
            )

            addTransaction(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addExpenseTransaction(
        description: String,
        amount: Double,
        category: String
    ): Result<Unit> {
        return try {
            val transaction = Transaction(
                shopOwnerId = getUserId(),
                invoiceNumber = generateExpenseReference(),
                type = "Expense",
                description = description,
                amount = amount,
                cost = amount,
                category = category,
                paymentType = "Cash",
                date = getCurrentDate(),
                status = "Completed"
            )

            addTransaction(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addIncomeTransaction(
        description: String,
        amount: Double,
        category: String = "Other"
    ): Result<Unit> {
        return try {
            val transaction = Transaction(
                shopOwnerId = getUserId(),
                invoiceNumber = generateIncomeReference(),
                type = "Income",
                description = description,
                amount = amount,
                cost = 0.0,
                category = category,
                paymentType = "Cash",
                date = getCurrentDate(),
                status = "Completed"
            )

            addTransaction(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun generatePurchaseInvoiceNumber(): String {
        val lastPurchase = db.collection("transactions")
            .whereEqualTo("shopOwnerId", getUserId())
            .whereEqualTo("type", "Purchase")
            .orderBy("invoiceNumber", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()

        return if (lastPurchase.isEmpty) "PUR-0001" else {
            val last = lastPurchase.documents.first().getString("invoiceNumber") ?: "PUR-0000"
            val number = last.substringAfter("PUR-").toIntOrNull() ?: 0
            "PUR-${String.format("%04d", number + 1)}"
        }
    }

    private suspend fun generateServiceInvoiceNumber(): String {
        val lastService = db.collection("transactions")
            .whereEqualTo("shopOwnerId", getUserId())
            .whereEqualTo("type", "Service")
            .orderBy("invoiceNumber", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()

        return if (lastService.isEmpty) "SRV-0001" else {
            val last = lastService.documents.first().getString("invoiceNumber") ?: "SRV-0000"
            val number = last.substringAfter("SRV-").toIntOrNull() ?: 0
            "SRV-${String.format("%04d", number + 1)}"
        }
    }

    private fun generateExpenseReference(): String {
        return "EXP-${System.currentTimeMillis()}"
    }

    private fun generateIncomeReference(): String {
        return "INC-${System.currentTimeMillis()}"
    }

    private suspend fun updateStockQuantity(productId: String, quantity: Int, isIncrease: Boolean) {
        val productRef = db.collection("products").document(productId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(productRef)
            if (snapshot.exists()) {
                val currentQuantity = snapshot.getLong("quantity") ?: 0L
                val newQuantity = if (isIncrease) currentQuantity + quantity else (currentQuantity - quantity).coerceAtLeast(0L)

                transaction.update(productRef, mapOf(
                    "quantity" to newQuantity,
                    "updatedAt" to System.currentTimeMillis()
                ))
            }
        }.await()
    }
    suspend fun getTransactionsByDateRange(startDate: String, endDate: String): List<Transaction> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            db.collection("transactions")
                .whereEqualTo("shopOwnerId", uid)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", Query.Direction.DESCENDING)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Transaction::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Add this data class for summary
    data class TransactionSummary(
        val date: String = "",
        val totalTransactions: Int = 0,
        val totalSales: Double = 0.0,
        val totalServices: Double = 0.0,
        val totalExpenses: Double = 0.0,
        val productsSold: Map<String, Int> = emptyMap()
    )
}