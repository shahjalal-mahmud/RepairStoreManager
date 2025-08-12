package com.example.repairstoremanager.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

object MessageHelper {

    // Method for sending SMS via intent
    fun sendSmsViaIntent(context: Context, phoneNumber: String, message: String) {
        val formattedNumber = formatPhoneNumber(phoneNumber)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "smsto:$formattedNumber".toUri()
            putExtra("sms_body", message)
        }
        context.startActivity(intent)
    }

    // Method for sending WhatsApp message
    fun sendWhatsAppMessage(context: Context, phoneNumber: String, message: String) {
        try {
            val formattedNumber = formatPhoneNumberForWhatsApp(phoneNumber)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = "https://wa.me/$formattedNumber?text=${Uri.encode(message)}".toUri()
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // WhatsApp not installed, open in browser
            val formattedNumber = formatPhoneNumberForWhatsApp(phoneNumber)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data =
                    "https://web.whatsapp.com/send?phone=$formattedNumber&text=${Uri.encode(message)}".toUri()
            }
            context.startActivity(intent)
        }
    }
    private fun formatPhoneNumber(number: String): String {
        val clean = number.trim().replace(" ", "")
        return when {
            clean.startsWith("+880") -> clean
            clean.startsWith("0") -> "+88$clean"
            else -> "+880$clean"
        }
    }
    private fun formatPhoneNumberForWhatsApp(number: String): String {
        // WhatsApp needs number without '+' prefix
        return formatPhoneNumber(number).removePrefix("+")
    }
}