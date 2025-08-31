package com.example.repairstoremanager.data.repository

import com.example.repairstoremanager.data.model.Customer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class SearchRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId(): String? = auth.currentUser?.uid

    suspend fun searchCustomersOptimized(query: String): List<Customer> {
        val uid = getUserId() ?: return emptyList()
        val trimmedQuery = query.trim()

        if (trimmedQuery.isBlank() || trimmedQuery.length < 2) {
            return emptyList()
        }

        return try {
            // Execute multiple queries in parallel for better performance
            coroutineScope {
                val nameSearch = async { searchByName(uid, trimmedQuery) }
                val phoneSearch = async { searchByPhone(uid, trimmedQuery) }
                val invoiceSearch = async { searchByInvoice(uid, trimmedQuery) }

                // Wait for all searches to complete
                val nameResults = nameSearch.await()
                val phoneResults = phoneSearch.await()
                val invoiceResults = invoiceSearch.await()

                // Combine and deduplicate results
                (nameResults + phoneResults + invoiceResults)
                    .distinctBy { it.id }
                    .sortedBy { it.customerName } // Optional: sort by name
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun searchByName(uid: String, query: String): List<Customer> {
        return try {
            db.collection("customers")
                .whereEqualTo("shopOwnerId", uid)
                .whereGreaterThanOrEqualTo("customerName", query)
                .whereLessThanOrEqualTo("customerName", query + "\uf8ff")
                .limit(15)
                .get()
                .await()
                .toObjects(Customer::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun searchByPhone(uid: String, query: String): List<Customer> {
        return try {
            db.collection("customers")
                .whereEqualTo("shopOwnerId", uid)
                .whereGreaterThanOrEqualTo("contactNumber", query)
                .whereLessThanOrEqualTo("contactNumber", query + "\uf8ff")
                .limit(15)
                .get()
                .await()
                .toObjects(Customer::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun searchByInvoice(uid: String, query: String): List<Customer> {
        return try {
            // First try exact match
            val exactResults = db.collection("customers")
                .whereEqualTo("shopOwnerId", uid)
                .whereEqualTo("invoiceNumber", query.uppercase())
                .limit(5)
                .get()
                .await()
                .toObjects(Customer::class.java)

            if (exactResults.isNotEmpty()) {
                return exactResults
            }

            // If no exact match, search for prefix
            db.collection("customers")
                .whereEqualTo("shopOwnerId", uid)
                .whereGreaterThanOrEqualTo("invoiceNumber", query)
                .whereLessThanOrEqualTo("invoiceNumber", query + "\uf8ff")
                .limit(10)
                .get()
                .await()
                .toObjects(Customer::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Alternative method for simpler search (if you prefer)
    suspend fun searchCustomersSimple(query: String): List<Customer> {
        val uid = getUserId() ?: return emptyList()
        val trimmedQuery = query.trim()

        if (trimmedQuery.isBlank() || trimmedQuery.length < 2) {
            return emptyList()
        }

        return try {
            // Get all user's customers and filter locally (good for small datasets)
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