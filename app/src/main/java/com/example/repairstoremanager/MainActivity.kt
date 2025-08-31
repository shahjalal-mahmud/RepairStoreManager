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
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.repairstoremanager.ui.navigation.Navigation
import com.example.repairstoremanager.ui.theme.RepairStoreManagerTheme
import com.example.repairstoremanager.viewmodel.CustomerViewModel
import com.example.repairstoremanager.viewmodel.SearchViewModel
import com.example.repairstoremanager.viewmodel.StockViewModel
import com.example.repairstoremanager.viewmodel.StoreViewModel
import com.example.repairstoremanager.viewmodel.TransactionViewModel
import com.example.repairstoremanager.worker.WorkScheduler

class MainActivity : ComponentActivity() {

    // Launcher for Bluetooth permissions
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

    // Launcher for media permissions (photos + videos)
    private val requestMediaPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                Toast.makeText(this, "Media permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Media permissions denied - cannot display or play media files",
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

    /**
     * Requests correct media permissions for Android 12, 13, and 14+
     */
    private fun checkMediaPermissions() {
        val requiredPermissions = when {
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 -> { // Android 12 and below
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else -> { // Android 13+ (includes 14+ with Selected Photos Access)
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            }
        }

        val allGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allGranted) {
            requestMediaPermissionsLauncher.launch(requiredPermissions)
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

        // Request Bluetooth permissions
        checkBluetoothPermissions()

        // Request media (photo/video) permissions
        checkMediaPermissions()

        // Check exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkExactAlarmPermission()
        }

        // Schedule daily reminder
        WorkScheduler.scheduleDailyReminder(applicationContext, hour = 9, minute = 0)

        setContent {
            RepairStoreManagerTheme {
                val navController = rememberNavController()
                val storeViewModel: StoreViewModel = viewModel()
                val stockViewModel = remember { StockViewModel() }
                val transactionViewModel = remember { TransactionViewModel() }
                val customerViewModel = remember { CustomerViewModel() }
                val searchViewModel = remember { SearchViewModel() }
                Navigation(
                    navController = navController,
                    storeViewModel = storeViewModel,
                    stockViewModel = stockViewModel,
                    transactionViewModel = transactionViewModel,
                    customerViewModel = customerViewModel,
                    searchViewModel = searchViewModel
                )
            }
        }
    }
}
