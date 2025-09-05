package com.example.repairstoremanager.data.model

import java.util.Date

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val createdAt: Long = Date().time,
    val updatedAt: Long = Date().time,
    val shopOwnerId: String = "",
    val isPinned: Boolean = false,
    val color: Int = 0 // Optional: for note coloring
)