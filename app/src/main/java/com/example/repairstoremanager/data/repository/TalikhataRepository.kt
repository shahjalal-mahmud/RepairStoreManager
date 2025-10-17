package com.example.repairstoremanager.data.repository

import com.example.repairstoremanager.data.model.TalikhataEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TalikhataRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun collectionPath(): String {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        return "users/$uid/talikhata"
    }

    fun observeAll() = callbackFlow<List<TalikhataEntry>> {
        val col = firestore.collection(collectionPath()).orderBy("dueDate", Query.Direction.ASCENDING)
        val registration = col.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // send empty on error
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { it.toObject(TalikhataEntry::class.java)?.copy(id = it.id) } ?: emptyList()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    suspend fun addEntry(entry: TalikhataEntry): String {
        val ref = firestore.collection(collectionPath()).add(entry).await()
        return ref.id
    }

    suspend fun updateEntry(id: String, entry: TalikhataEntry) {
        firestore.collection(collectionPath()).document(id).set(entry).await()
    }

    suspend fun patchReminderScheduled(id: String, scheduled: Boolean) {
        firestore.collection(collectionPath()).document(id).update("reminderScheduled", scheduled).await()
    }

    suspend fun deleteEntry(id: String) {
        firestore.collection(collectionPath()).document(id).delete().await()
    }

    suspend fun getEntry(id: String): TalikhataEntry? {
        val doc = firestore.collection(collectionPath()).document(id).get().await()
        return doc.toObject(TalikhataEntry::class.java)?.copy(id = doc.id)
    }
}
