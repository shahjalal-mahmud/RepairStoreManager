package com.example.repairstoremanager.data.repository

import android.util.Log
import com.example.repairstoremanager.data.model.DailySummary
import com.example.repairstoremanager.data.model.Transaction
import com.example.repairstoremanager.data.model.TransactionProduct
import com.example.repairstoremanager.util.DateUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

class TransactionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Save transaction and update stock
    suspend fun saveSaleTransaction(transaction: Transaction): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            // Generate invoice number if not provided
            val finalTransaction = if (transaction.invoiceNumber.isBlank()) {
                transaction.copy(invoiceNumber = generateNextInvoiceNumber(uid))
            } else {
                transaction
            }.calculateTotals()

            // Save transaction
            val docRef = db.collection("transactions").document()
            val transactionWithId = finalTransaction.copy(id = docRef.id, shopOwnerId = uid)

            docRef.set(transactionWithId).await()

            // Update stock quantities
            updateStockQuantities(transactionWithId.products)

            Log.d("TransactionRepo", "Transaction saved: ${transactionWithId.invoiceNumber}")
            Result.success(transactionWithId.invoiceNumber)
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error saving transaction: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Get transactions for a specific date
    suspend fun getTransactionsByDate(date: String): List<Transaction> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            db.collection("transactions")
                .whereEqualTo("shopOwnerId", uid)
                .whereEqualTo("date", date)
                .whereEqualTo("type", "Sale")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Transaction::class.java)
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error fetching transactions: ${e.message}", e)
            emptyList()
        }
    }

    // Get all sales transactions (for history)
    suspend fun getAllSalesTransactions(): List<Transaction> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            db.collection("transactions")
                .whereEqualTo("shopOwnerId", uid)
                .whereEqualTo("type", "Sale")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Transaction::class.java)
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error fetching all transactions: ${e.message}", e)
            emptyList()
        }
    }

    // Get daily summary
    suspend fun getDailySummary(date: String): DailySummary {
        val transactions = getTransactionsByDate(date)

        val totalSales = transactions.sumOf { it.totalAmount }
        val totalCost = transactions.sumOf { it.totalCost }
        val totalProfit = transactions.sumOf { it.totalProfit }

        // Count products sold
        val productsSold = mutableMapOf<String, Int>()
        transactions.forEach { transaction ->
            transaction.products.forEach { product ->
                productsSold[product.name] = productsSold.getOrDefault(product.name, 0) + product.quantity
            }
        }

        return DailySummary(
            date = date,
            totalSales = totalSales,
            totalCost = totalCost,
            totalProfit = totalProfit,
            transactionCount = transactions.size,
            productsSold = productsSold
        )
    }

    // Real-time listener for transactions
    fun getTransactionsRealTimeListener(date: String, onUpdate: (List<Transaction>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("transactions")
            .whereEqualTo("shopOwnerId", uid)
            .whereEqualTo("date", date)
            .whereEqualTo("type", "Sale")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TransactionRepo", "Real-time listener error: ${error.message}", error)
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val transactions = it.toObjects(Transaction::class.java)
                    onUpdate(transactions)
                }
            }
    }

    // Generate next invoice number
    private suspend fun generateNextInvoiceNumber(uid: String): String {
        return try {
            val snapshot = db.collection("transactions")
                .whereEqualTo("shopOwnerId", uid)
                .orderBy("invoiceNumber", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                "INV-0001"
            } else {
                val lastInvoice = snapshot.documents.first().getString("invoiceNumber") ?: "INV-0000"
                val number = lastInvoice.substringAfter("INV-").toIntOrNull() ?: 0
                "INV-${String.format("%04d", number + 1)}"
            }
        } catch (e: Exception) {
            "INV-${System.currentTimeMillis() % 10000}"
        }
    }

    // FIXED: Update stock quantities after sale
    private suspend fun updateStockQuantities(products: List<TransactionProduct>) {
        products.forEach { product ->
            try {
                val productRef = db.collection("products").document(product.productId)
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(productRef)
                    if (snapshot.exists()) {
                        val currentQuantity = snapshot.getLong("quantity") ?: 0L
                        val newQuantity = (currentQuantity - product.quantity).coerceAtLeast(0L)

                        // CORRECTED: Use separate update calls or FieldPath
                        transaction.update(productRef, "quantity", newQuantity)
                        transaction.update(productRef, "updatedAt", System.currentTimeMillis())
                    }
                }.await()
            } catch (e: Exception) {
                Log.e("TransactionRepo", "Error updating stock for ${product.name}: ${e.message}")
            }
        }
    }

    // Alternative method using FieldPath (if you prefer)
    private suspend fun updateStockQuantitiesAlternative(products: List<TransactionProduct>) {
        products.forEach { product ->
            try {
                val productRef = db.collection("products").document(product.productId)
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(productRef)
                    if (snapshot.exists()) {
                        val currentQuantity = snapshot.getLong("quantity") ?: 0L
                        val newQuantity = (currentQuantity - product.quantity).coerceAtLeast(0L)

                        // Using FieldPath with varargs
                        transaction.update(
                            productRef,
                            FieldPath.of("quantity"), newQuantity,
                            FieldPath.of("updatedAt"), System.currentTimeMillis()
                        )
                    }
                }.await()
            } catch (e: Exception) {
                Log.e("TransactionRepo", "Error updating stock for ${product.name}: ${e.message}")
            }
        }
    }

    // Another alternative: Update using a map (outside transaction)
    private suspend fun updateStockQuantitiesWithMap(products: List<TransactionProduct>) {
        products.forEach { product ->
            try {
                val productRef = db.collection("products").document(product.productId)

                // Get current quantity first
                val snapshot = productRef.get().await()
                if (snapshot.exists()) {
                    val currentQuantity = snapshot.getLong("quantity") ?: 0L
                    val newQuantity = (currentQuantity - product.quantity).coerceAtLeast(0L)

                    // Update using a map (simpler, but not transactional)
                    val updates = mapOf(
                        "quantity" to newQuantity,
                        "updatedAt" to System.currentTimeMillis()
                    )

                    productRef.update(updates).await()
                }
            } catch (e: Exception) {
                Log.e("TransactionRepo", "Error updating stock for ${product.name}: ${e.message}")
            }
        }
    }

    // Utility function to get current date
    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }
    // Get transactions by date range
    suspend fun getTransactionsByDateRange(startDate: LocalDate, endDate: LocalDate): List<Transaction> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            // Convert LocalDate to timestamps for range query
            val startTimestamp = DateUtils.localDateToLong(startDate)
            val endTimestamp = DateUtils.localDateToLong(endDate.plusDays(1)) // Include the entire end date

            db.collection("transactions")
                .whereEqualTo("shopOwnerId", uid)
                .whereEqualTo("type", "Sale")
                .whereGreaterThanOrEqualTo("timestamp", startTimestamp)
                .whereLessThan("timestamp", endTimestamp)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Transaction::class.java)
                .also {
                    Log.d("TransactionRepo", "Fetched ${it.size} transactions from $startDate to $endDate")
                }
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error fetching transactions by date range: ${e.message}", e)
            emptyList()
        }
    }
}