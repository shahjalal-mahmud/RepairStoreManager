package com.example.repairstoremanager.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class TalikhataEntry(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val amount: Double = 0.0,
    // store as Firestore Timestamp for easy queries
    val dueDate: Timestamp = Timestamp.now(),
    val isPayableToUser: Boolean = true, // true = user should pay (liability), false = user will receive (asset)
    val reminderScheduled: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)
