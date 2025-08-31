package com.example.repairstoremanager.data.repository

import com.example.repairstoremanager.data.model.Customer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class CustomerRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    fun getUserId(): String? = auth.currentUser?.uid

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
        val uid = getUserId() ?: throw Exception("User not logged in")
        val counterDocRef = db.collection("metadata").document("counters")

        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(counterDocRef)
                val counters = snapshot.get("invoiceCounters") as? Map<String, Long> ?: mutableMapOf()
                val current = counters[uid] ?: 0L
                val next = current + 1

                // Update the specific user's counter
                val updatedCounters = counters.toMutableMap().apply {
                    this[uid] = next
                }

                transaction.update(counterDocRef, "invoiceCounters", updatedCounters)
                "INV-${String.format("%06d", next)}"
            }.await()
        } catch (e: Exception) {
            // Fallback if transaction fails
            val snapshot = counterDocRef.get().await()
            val counters = snapshot.get("invoiceCounters") as? Map<String, Long> ?: mutableMapOf()
            val current = counters[uid] ?: 0L
            val next = current + 1

            counterDocRef.update("invoiceCounters.${uid}", next).await()
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
    suspend fun peekNextInvoiceNumber(): String {
        val uid = getUserId() ?: throw Exception("User not logged in")
        val counterDocRef = db.collection("metadata").document("counters")

        return try {
            val snapshot = counterDocRef.get().await()
            val counters = snapshot.get("invoiceCounters") as? Map<String, Long> ?: mutableMapOf()
            val current = counters[uid] ?: 0L
            "INV-${String.format("%06d", current + 1)}"
        } catch (e: Exception) {
            "INV-000001" // Fallback
        }
    }
    suspend fun getCustomerByInvoice(invoice: String): Customer? {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("customers")
                .whereEqualTo("invoiceNumber", invoice)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot.toObjects(Customer::class.java).firstOrNull()
            } else null
        } catch (e: Exception) {
            null
        }
    }
    suspend fun searchCustomers(query: String): List<Customer> {
        val uid = getUserId() ?: return emptyList()
        val trimmedQuery = query.trim()

        if (trimmedQuery.isBlank() || trimmedQuery.length < 2) {
            return emptyList()
        }

        return try {
            // Use the simple search approach for compatibility
            val allCustomers = db.collection("customers")
                .whereEqualTo("shopOwnerId", uid)
                .get()
                .await()
                .toObjects(Customer::class.java)

            // Filter with case-insensitive search
            allCustomers.filter { customer ->
                customer.customerName.contains(trimmedQuery, ignoreCase = true) ||
                        customer.contactNumber.contains(trimmedQuery, ignoreCase = true) ||
                        customer.invoiceNumber?.contains(trimmedQuery, ignoreCase = true) == true ||
                        // For invoice numbers without "INV-" prefix
                        (trimmedQuery.all { it.isDigit() } &&
                                customer.invoiceNumber?.replace("INV-", "", ignoreCase = true)?.startsWith(trimmedQuery, ignoreCase = true) == true)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}