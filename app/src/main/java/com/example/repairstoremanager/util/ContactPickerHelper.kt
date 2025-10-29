package com.example.repairstoremanager.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

data class ContactInfo(
    val name: String,
    val phoneNumber: String
)

class ContactPickerHelper {

    @SuppressLint("Range")
    fun getContactInfoFromIntent(context: Context, data: Intent?): ContactInfo? {
        if (data == null) return null

        return try {
            val uri = data.data
            val cursor = context.contentResolver.query(
                uri!!,
                null,
                null,
                null,
                null
            )

            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val name = c.getString(
                        c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    ) ?: ""

                    val phoneNumber = c.getString(
                        c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    ) ?: ""

                    ContactInfo(name, phoneNumber)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Composable
fun rememberContactPickerLauncher(
    onContactPicked: (ContactInfo?) -> Unit
): Pair<Boolean, () -> Unit> {
    val context = LocalContext.current
    val contactPickerHelper = remember { ContactPickerHelper() }

    // Contact picker launcher - declare this FIRST
    val contactPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactInfo = contactPickerHelper.getContactInfoFromIntent(
                context,
                result.data
            )
            onContactPicked(contactInfo)
        } else {
            onContactPicked(null)
        }
    }

    // Permission launcher for reading contacts - declare this AFTER contactPickerLauncher
    val contactPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, launch contact picker
            launchContactPicker(contactPickerLauncher)
        } else {
            Toast.makeText(context, "Contact permission is required to pick contacts", Toast.LENGTH_SHORT).show()
        }
    }

    val hasContactPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    val launchContactPicker: () -> Unit = remember {
        {
            if (hasContactPermission) {
                launchContactPicker(contactPickerLauncher)
            } else {
                contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    return Pair(hasContactPermission, launchContactPicker)
}

private fun launchContactPicker(
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    val intent = Intent(Intent.ACTION_PICK).apply {
        type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
    }
    launcher.launch(intent)
}