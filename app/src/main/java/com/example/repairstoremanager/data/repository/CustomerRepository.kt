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
            val formattedCustomer = customer.copy(
                id = customerId,
                shopOwnerId = uid,
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

}
