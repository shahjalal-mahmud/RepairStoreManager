package com.example.repairstoremanager.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.widget.Toast
import androidx.annotation.RequiresPermission
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

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun sendSms(context: Context, phoneNumber: String, message: String, simSlotIndex: Int = 0) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "SMS permission denied.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
                    ?.createForSubscriptionId(getSubscriptionIdForSimSlot(context, simSlotIndex))
            } else {
                SmsManager.getDefault()
            }

            smsManager?.sendTextMessage(phoneNumber, null, message, null, null)
        } catch (e: SecurityException) {
            Toast.makeText(context, "SMS failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getSubscriptionIdForSimSlot(context: Context, simSlotIndex: Int): Int {
        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList

        return if (subscriptionInfoList != null && subscriptionInfoList.size > simSlotIndex) {
            subscriptionInfoList[simSlotIndex].subscriptionId
        } else {
            SubscriptionManager.getDefaultSubscriptionId() // fallback
        }
    }

}
