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

    fun logout() {
        auth.signOut()
    }

    // Make changePassword suspend because updatePassword is async
    suspend fun changePassword(newPassword: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
        return try {
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
