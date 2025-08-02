package com.example.repairstoremanager

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.repairstoremanager.ui.navigation.Navigation
import com.example.repairstoremanager.ui.theme.RepairStoreManagerTheme
import com.example.repairstoremanager.util.SmsHelper
import com.example.repairstoremanager.viewmodel.StoreViewModel
import com.example.repairstoremanager.worker.WorkScheduler

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request SMS permission at launch
        if (!SmsHelper.hasSmsPermission(this)) {
            SmsHelper.requestSmsPermission(this)
        }

        // Schedule daily delivery reminder notification (9:00 AM default)
        WorkScheduler.scheduleDailyReminder(applicationContext, hour = 9, minute = 0)

        setContent {
            RepairStoreManagerTheme {
                val navController = rememberNavController()
                val storeViewModel: StoreViewModel = viewModel()
                Navigation(navController = navController, storeViewModel = storeViewModel)
            }
        }
    }

    // Handle SMS permission result
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == SmsHelper.SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "SMS permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
