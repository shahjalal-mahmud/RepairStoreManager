package com.example.repairstoremanager.util


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object SmsHelper {

    const val SMS_PERMISSION_REQUEST_CODE = 101

    fun hasSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestSmsPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.SEND_SMS),
            SMS_PERMISSION_REQUEST_CODE
        )
    }

    fun sendSms(
        context: Context,
        phoneNumber: String,
        message: String,
        onResult: (Boolean) -> Unit = {}
    ) {
        if (!hasSmsPermission(context)) {
            Toast.makeText(context, "SMS permission not granted", Toast.LENGTH_SHORT).show()
            onResult(false)
            return
        }

        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            onResult(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to send SMS: ${e.message}", Toast.LENGTH_LONG).show()
            onResult(false)
        }
    }
}