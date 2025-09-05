package com.example.repairstoremanager.data.repository

import android.util.Log
import com.example.repairstoremanager.data.model.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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