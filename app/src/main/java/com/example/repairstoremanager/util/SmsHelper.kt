package com.example.repairstoremanager.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object SmsHelper {

    const val SMS_PERMISSION_REQUEST_CODE = 101

    fun hasAllSmsPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    }

    fun requestSmsPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE),
            SMS_PERMISSION_REQUEST_CODE
        )
    }

    // For automatic SMS sending (if you still want to keep this)
    fun sendSms(context: Context, phoneNumber: String, message: String, simSlotIndex: Int = 0) {
        if (!hasAllSmsPermissions(context)) {
            Toast.makeText(context, "SMS permissions missing", Toast.LENGTH_SHORT).show()
            return
        }

        val formattedNumber = formatPhoneNumber(phoneNumber)

        try {
            val subId = getSubscriptionIdForSimSlot(context, simSlotIndex)
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                context.getSystemService(SmsManager::class.java)?.createForSubscriptionId(subId)
            } else {
                SmsManager.getDefault()
            }

            smsManager?.sendTextMessage(formattedNumber, null, message, null, null)
        } catch (e: Exception) {
            Toast.makeText(context, "SMS failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // New method for sending SMS via intent
    fun sendSmsViaIntent(context: Context, phoneNumber: String, message: String) {
        val formattedNumber = formatPhoneNumber(phoneNumber)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("smsto:$formattedNumber")
            putExtra("sms_body", message)
        }
        context.startActivity(intent)
    }

    private fun formatPhoneNumber(number: String): String {
        val clean = number.trim().replace(" ", "")
        return when {
            clean.startsWith("+880") -> clean
            clean.startsWith("0") -> "+88$clean"
            else -> "+880$clean"
        }
    }

    private fun getSubscriptionIdForSimSlot(context: Context, simSlotIndex: Int): Int {
        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
        return if (subscriptionInfoList != null && subscriptionInfoList.size > simSlotIndex) {
            subscriptionInfoList[simSlotIndex].subscriptionId
        } else {
            SubscriptionManager.getDefaultSubscriptionId()
        }
    }
}