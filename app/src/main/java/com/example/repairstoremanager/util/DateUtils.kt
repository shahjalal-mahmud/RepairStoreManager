package com.example.repairstoremanager.util

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object DateUtils {
    private val displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val storageFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val inputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun formatDateForDisplay(date: LocalDate): String {
        return date.format(displayFormatter)
    }

    fun formatDateForStorage(date: LocalDate): String {
        return date.format(storageFormatter)
    }

    fun parseDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString, storageFormatter)
        } catch (e: Exception) {
            null
        }
    }

    fun localDateToLong(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun longToLocalDate(timestamp: Long): LocalDate {
        return Date(timestamp).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun getCurrentDate(): LocalDate {
        return LocalDate.now()
    }

    // Convert LocalDate to java.util.Date for Android DatePicker
    fun localDateToDate(localDate: LocalDate): Date {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    // Convert java.util.Date to LocalDate
    fun dateToLocalDate(date: Date): LocalDate {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }
}