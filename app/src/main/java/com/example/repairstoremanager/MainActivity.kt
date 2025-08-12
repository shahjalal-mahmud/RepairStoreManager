package com.example.repairstoremanager

import android.Manifest
import android.app.AlarmManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.repairstoremanager.ui.navigation.Navigation
import com.example.repairstoremanager.ui.theme.RepairStoreManagerTheme
import com.example.repairstoremanager.util.SmsHelper
import com.example.repairstoremanager.viewmodel.StoreViewModel
import com.example.repairstoremanager.worker.WorkScheduler

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

    private val requestBluetoothPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                Toast.makeText(this, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Bluetooth permissions denied - some features may not work",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private fun checkBluetoothPermissions() {
        val requiredPermissions = mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_SCAN)
            } else {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
                add(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }.toTypedArray()

        val allGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allGranted) {
            requestBluetoothPermissionsLauncher.launch(requiredPermissions)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkExactAlarmPermission() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(this, "Grant exact alarm permission in settings", Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SmsHelper.hasAllSmsPermissions(this)) {
            requestSmsPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_PHONE_NUMBERS
                )
            )
        }

        checkBluetoothPermissions()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkExactAlarmPermission()
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