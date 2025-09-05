package com.example.repairstoremanager.data.model

import java.util.Date
import java.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val createdAt: Long = Date().time,
    val updatedAt: Long = Date().time,
    val shopOwnerId: String = "",
    val isPinned: Boolean = false,
    val color: Int = 0, // For note coloring
    val tags: List<String> = emptyList() // For categorization
) {
    companion object {
        // Predefined colors for notes
        const val COLOR_DEFAULT = 0
        const val COLOR_BLUE = 1
        const val COLOR_GREEN = 2
        const val COLOR_YELLOW = 3
        const val COLOR_RED = 4
        const val COLOR_PURPLE = 5
    }
}