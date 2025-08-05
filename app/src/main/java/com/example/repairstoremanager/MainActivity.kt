package com.example.repairstoremanager

import android.Manifest
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
    // Add these permission constants at the top of MainActivity
    private val BLUETOOTH_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    private fun hasBluetoothPermissions(): Boolean {
        return BLUETOOTH_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestBluetoothPermissions() {
        if (!hasBluetoothPermissions()) {
            requestBluetoothPermissionsLauncher.launch(BLUETOOTH_PERMISSIONS)
        }
    }

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

    private val requestBluetoothPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Bluetooth permissions denied - some features may not work", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkBluetoothPermissions() {
        val requiredPermissions = mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_SCAN)
            } else {
                // For Android 10 and 11, we need location permissions for Bluetooth
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check and request all necessary permissions
        if (!SmsHelper.hasAllSmsPermissions(this)) {
            requestSmsPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_PHONE_NUMBERS
                )
            )
        }

        // Check Bluetooth permissions
        checkBluetoothPermissions()

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