package com.example.repairstoremanager.data.repository

import com.example.repairstoremanager.data.model.Customer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CustomerRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private fun getUserId(): String? = auth.currentUser?.uid

    suspend fun addCustomer(customer: Customer): Result<Unit> {
        return try {
            val uid = getUserId() ?: return Result.failure(Exception("User not logged in"))
            val customerId = db.collection("customers").document().id
            db.collection("customers").document(customerId)
                .set(customer.copy(id = customerId, shopOwnerId = uid))
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
}
