package com.example.repairstoremanager.data.repository

import com.example.repairstoremanager.data.model.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TransactionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun addTransaction(transaction: Transaction): Result<Unit> {
        return try {
            val docRef = db.collection("transactions").document()
            val withId = transaction.copy(id = docRef.id)
            docRef.set(withId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactionsByDate(date: String): List<Transaction> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            db.collection("transactions")
                .whereEqualTo("shopOwnerId", uid)
                .whereEqualTo("date", date)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Transaction::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllTransactions(): List<Transaction> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            db.collection("transactions")
                .whereEqualTo("shopOwnerId", uid)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Transaction::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun peekNextInvoiceNumber(): String {
        val uid = auth.currentUser?.uid ?: return "INV-0001"
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
            "INV-0001"
        }
    }

    fun getUserId(): String {
        return auth.currentUser?.uid ?: ""
    }

    suspend fun updateProductQuantity(productId: String, quantityChange: Int): Result<Unit> {
        return try {
            val productRef = db.collection("products").document(productId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(productRef)
                if (snapshot.exists()) {
                    val currentQuantity = snapshot.getLong("quantity") ?: 0L
                    val newQuantity = (currentQuantity - quantityChange).coerceAtLeast(0L)
                    transaction.update(productRef, "quantity", newQuantity)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}