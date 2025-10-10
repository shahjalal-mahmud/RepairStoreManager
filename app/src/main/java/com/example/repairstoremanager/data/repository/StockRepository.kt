package com.example.repairstoremanager.data.repository

import android.content.Context
import com.example.repairstoremanager.data.model.Product
import com.example.repairstoremanager.util.StockNotificationManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StockRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getUserId(): String? = auth.currentUser?.uid

    suspend fun addProduct(product: Product, context: Context? = null): Result<Unit> {
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

            // Check for low stock after adding - check ALL products for summary
            context?.let {
                val allProducts = getAllProducts()
                StockNotificationManager.checkLowStockAndNotify(it, allProducts)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: Product, context: Context? = null): Result<Unit> {
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

            // Check for low stock after updating - check ALL products for summary
            context?.let {
                val allProducts = getAllProducts()
                StockNotificationManager.checkLowStockAndNotify(it, allProducts)
            }

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

    suspend fun incrementQuantity(productId: String, delta: Long, context: Context? = null): Result<Unit> {
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

            // Check for low stock after quantity update - check ALL products for summary
            context?.let {
                val allProducts = getAllProducts()
                StockNotificationManager.checkLowStockAndNotify(it, allProducts)
            }

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

    suspend fun checkLowStockOnAppStart(context: Context) {
        try {
            val allProducts = getAllProducts()
            // ✅ Use the enhanced summary notification system
            StockNotificationManager.checkLowStockAndNotify(context, allProducts)
        } catch (e: Exception) {
            // Log error but don't crash the app
            println("Low stock check on app start failed: ${e.message}")
        }
    }

    // Method to get low stock products specifically
    suspend fun getLowStockProducts(): List<Product> {
        val allProducts = getAllProducts()
        return allProducts.filter { product ->
            product.quantity <= product.alertQuantity && product.alertQuantity > 0
        }
    }
}