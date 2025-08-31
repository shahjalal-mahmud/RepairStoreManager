package com.example.repairstoremanager.data.repository

import com.example.repairstoremanager.data.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StockRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getUserId(): String? = auth.currentUser?.uid

    suspend fun addProduct(product: Product): Result<Unit> {
        return try {
            val uid = getUserId() ?: return Result.failure(IllegalStateException("User not logged in"))
            val docRef = db.collection("products").document()
            val now = System.currentTimeMillis()

            val payload = product.copy(
                id = docRef.id,
                shopOwnerId = uid,
                createdAt = now,
                updatedAt = now
            )

            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            if (product.id.isBlank()) return Result.failure(IllegalArgumentException("Missing product id"))
            val uid = getUserId() ?: return Result.failure(IllegalStateException("User not logged in"))

            val payload = product.copy(
                shopOwnerId = uid,
                updatedAt = System.currentTimeMillis()
            )

            db.collection("products").document(product.id)
                .set(payload)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllProducts(): List<Product> {
        val uid = getUserId() ?: return emptyList()
        val snapshot = db.collection("products")
            .whereEqualTo("shopOwnerId", uid)
            .orderBy("name")
            .get()
            .await()
        return snapshot.toObjects(Product::class.java)
    }

    suspend fun getProductById(productId: String): Product? {
        return try {
            val snapshot = db.collection("products").document(productId).get().await()
            snapshot.toObject(Product::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun incrementQuantity(productId: String, delta: Long): Result<Unit> {
        return try {
            val uid = getUserId() ?: return Result.failure(IllegalStateException("User not logged in"))
            val doc = db.collection("products").document(productId)

            db.runTransaction { txn ->
                val snap = txn.get(doc)
                if (!snap.exists()) throw IllegalStateException("Product not found")
                val owner = snap.getString("shopOwnerId") ?: ""
                if (owner != uid) throw SecurityException("Unauthorized")

                val currentQty = snap.getLong("quantity") ?: 0L
                val next = (currentQty + delta).coerceAtLeast(0L)

                txn.update(doc, mapOf(
                    "quantity" to next,
                    "updatedAt" to System.currentTimeMillis()
                ))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            db.collection("products").document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchProducts(query: String): List<Product> {
        val uid = getUserId() ?: return emptyList()
        val snapshot = db.collection("products")
            .whereEqualTo("shopOwnerId", uid)
            .orderBy("name")
            .get()
            .await()

        return snapshot.toObjects(Product::class.java)
            .filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        product.category.contains(query, ignoreCase = true) ||
                        product.type.contains(query, ignoreCase = true)
            }
    }
}