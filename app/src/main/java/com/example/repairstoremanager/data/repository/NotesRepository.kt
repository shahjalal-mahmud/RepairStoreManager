package com.example.repairstoremanager.data.repository

import com.example.repairstoremanager.data.model.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

class NotesRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun addNote(note: Note): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            val noteWithId = note.copy(shopOwnerId = uid)
            val docRef = db.collection("notes").document()
            val finalNote = noteWithId.copy(id = docRef.id)

            docRef.set(finalNote).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNote(note: Note): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            if (note.shopOwnerId != uid) {
                return Result.failure(Exception("Unauthorized"))
            }

            val updatedNote = note.copy(updatedAt = Date().time)
            db.collection("notes").document(note.id).set(updatedNote).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(noteId: String): Result<Unit> {
        return try {
            db.collection("notes").document(noteId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotes(): List<Note> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            db.collection("notes")
                .whereEqualTo("shopOwnerId", uid)
                .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Note::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getNoteById(noteId: String): Note? {
        return try {
            db.collection("notes").document(noteId).get().await().toObject(Note::class.java)
        } catch (e: Exception) {
            null
        }
    }
}