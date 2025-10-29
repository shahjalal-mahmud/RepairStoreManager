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
import android.content.ContentProviderOperation
import android.content.ContentResolver

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

class ContactSaverHelper {

    fun saveContactToDevice(
        contentResolver: ContentResolver,
        name: String,
        phoneNumber: String
    ): Boolean {
        return try {
            val operations = ArrayList<ContentProviderOperation>()

            // Create raw contact
            operations.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build()
            )

            // Add name
            operations.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build()
            )

            // Add phone number
            if (phoneNumber.isNotBlank()) {
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                        .withValue(
                            ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                        )
                        .build()
                )
            }

            // Apply batch operations
            contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

@Composable
fun rememberContactSaverLauncher(
    onContactSaved: (Boolean) -> Unit
): Pair<Boolean, (String, String) -> Unit> {
    val context = LocalContext.current

    // Permission launcher for writing contacts
    val writeContactPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Contact write permission denied", Toast.LENGTH_SHORT).show()
            onContactSaved(false)
        }
    }

    val hasWriteContactPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    val saveContact: (String, String) -> Unit = remember {
        { name, phoneNumber ->
            if (hasWriteContactPermission) {
                val contactSaverHelper = ContactSaverHelper()
                val success = contactSaverHelper.saveContactToDevice(
                    context.contentResolver,
                    name,
                    phoneNumber
                )
                if (success) {
                    Toast.makeText(context, "Contact saved successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to save contact", Toast.LENGTH_SHORT).show()
                }
                onContactSaved(success)
            } else {
                writeContactPermissionLauncher.launch(Manifest.permission.WRITE_CONTACTS)
            }
        }
    }

    return Pair(hasWriteContactPermission, saveContact)
}