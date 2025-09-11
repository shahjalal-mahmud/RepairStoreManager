package com.example.repairstoremanager.data.repository

import com.example.repairstoremanager.data.model.StoreInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId(): String = auth.currentUser?.uid ?: ""

    suspend fun getStoreInfo(): StoreInfo {
        val uid = getUserId()
        if (uid.isEmpty()) return StoreInfo()
        val snapshot = db.collection("stores").document(uid).get().await()
        return snapshot.toObject(StoreInfo::class.java) ?: StoreInfo()
    }

    suspend fun updateStoreInfo(info: StoreInfo): Result<Unit> {
        val uid = getUserId()
        if (uid.isEmpty()) return Result.failure(Exception("User not logged in"))
        return try {
            db.collection("stores").document(uid).set(info).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOwnerDetails(
        ownerName: String,
        ownerEmail: String,
        ownerPhone: String
    ): Result<Unit> {
        val uid = getUserId()
        if (uid.isEmpty()) return Result.failure(Exception("User not logged in"))
        return try {
            val updates = mapOf(
                "ownerName" to ownerName,
                "ownerEmail" to ownerEmail,
                "ownerPhone" to ownerPhone
            )
            db.collection("stores").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}