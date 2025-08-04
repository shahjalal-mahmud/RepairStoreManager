package com.example.repairstoremanager

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.repairstoremanager.ui.navigation.Navigation
import com.example.repairstoremanager.ui.theme.RepairStoreManagerTheme
import com.example.repairstoremanager.viewmodel.StoreViewModel
import com.example.repairstoremanager.worker.WorkScheduler
import com.example.repairstoremanager.util.SmsHelper

class MainActivity : ComponentActivity() {

    private val requestSmsPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.SEND_SMS] == true &&
                    permissions[Manifest.permission.READ_PHONE_STATE] == true
            Toast.makeText(
                this,
                if (granted) "SMS permissions granted!" else "SMS permissions denied.",
                Toast.LENGTH_SHORT
            ).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SmsHelper.hasAllSmsPermissions(this)) {
            requestSmsPermissionsLauncher.launch(
                arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE)
            )
        }

        WorkScheduler.scheduleDailyReminder(applicationContext, hour = 9, minute = 0)

        setContent {
            RepairStoreManagerTheme {
                val navController = rememberNavController()
                val storeViewModel: StoreViewModel = viewModel()
                Navigation(navController = navController, storeViewModel = storeViewModel)
            }
        }
    }
}
