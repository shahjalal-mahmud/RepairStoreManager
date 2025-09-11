package com.example.repairstoremanager.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
    val color: Int = COLOR_DEFAULT, // For note coloring
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

        // Get appropriate text color based on background color
        fun getTextColorForBackground(backgroundColor: Color): Color {
            return if (backgroundColor.luminance() > 0.5) {
                Color.Black // Use black text for light backgrounds
            } else {
                Color.White // Use white text for dark backgrounds
            }
        }

        // Get background color with dark mode support
        fun getBackgroundColor(colorValue: Int, isDarkTheme: Boolean = false): Color {
            return when (colorValue) {
                COLOR_BLUE -> if (isDarkTheme) Color(0xFF0D47A1) else Color(0xFFE3F2FD)
                COLOR_GREEN -> if (isDarkTheme) Color(0xFF1B5E20) else Color(0xFFE8F5E9)
                COLOR_YELLOW -> if (isDarkTheme) Color(0xFFF57F17) else Color(0xFFFFF9C4)
                COLOR_RED -> if (isDarkTheme) Color(0xFFB71C1C) else Color(0xFFFFEBEE)
                COLOR_PURPLE -> if (isDarkTheme) Color(0xFF4A148C) else Color(0xFFF3E5F5)
                else -> if (isDarkTheme) Color(0xFF2D2D2D) else Color.White
            }
        }
    }
}