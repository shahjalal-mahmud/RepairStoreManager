package com.example.repairstoremanager.data.repository

import com.example.repairstoremanager.data.model.Customer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CustomerRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private fun getUserId(): String? = auth.currentUser?.uid

    private fun formatPhoneNumber(number: String): String {
        return when {
            number.startsWith("+") -> number
            number.startsWith("0") -> "+88$number"
            else -> number // fallback
        }
    }

    suspend fun addCustomer(customer: Customer): Result<Unit> {
        return try {
            val uid = getUserId() ?: return Result.failure(Exception("User not logged in"))
            val customerId = db.collection("customers").document().id

            val invoiceNumber = getNextInvoiceNumber()

            val formattedCustomer = customer.copy(
                id = customerId,
                shopOwnerId = uid,
                invoiceNumber = invoiceNumber,
                contactNumber = formatPhoneNumber(customer.contactNumber)
            )

            db.collection("customers").document(customerId)
                .set(formattedCustomer)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getNextInvoiceNumber(): String {
        val counterDocRef = db.collection("metadata").document("counters")
        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(counterDocRef)
                val current = snapshot.getLong("lastInvoiceNumber") ?: 0L
                val next = current + 1
                transaction.update(counterDocRef, "lastInvoiceNumber", next)
                "INV-${String.format("%06d", next)}"
            }.await()
        } catch (e: Exception) {
            // Fallback if transaction fails
            val snapshot = counterDocRef.get().await()
            val current = snapshot.getLong("lastInvoiceNumber") ?: 0L
            val next = current + 1
            counterDocRef.update("lastInvoiceNumber", next).await()
            "INV-${String.format("%06d", next)}"
        }
    }

    suspend fun getAllCustomers(): List<Customer> {
        val uid = getUserId() ?: return emptyList()
        val snapshot = db.collection("customers")
            .whereEqualTo("shopOwnerId", uid)
            .get()
            .await()
        return snapshot.toObjects(Customer::class.java)
    }
    suspend fun updateStatus(customerId: String, status: String): Result<Unit> {
        return try {
            db.collection("customers").document(customerId)
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun updateCustomer(customer: Customer): Result<Unit> {
        return try {
            db.collection("customers").document(customer.id)
                .set(customer)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
