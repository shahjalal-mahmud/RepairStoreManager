package com.example.repairstoremanager.util

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.OutputStream
import java.lang.reflect.Method
import java.util.UUID

class POSPrinterHelper(private val context: Context) {

    companion object {
        private const val TAG = "POSPrinterHelper"
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    private fun getBluetoothAdapter(): BluetoothAdapter? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                bluetoothManager.adapter
            } else {
                @Suppress("DEPRECATION")
                BluetoothAdapter.getDefaultAdapter()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Bluetooth adapter", e)
            null
        }
    }

    @RequiresPermission(allOf = [
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN
    ])
    fun connectToPrinter(printerName: String = "PT-210_00D1"): Boolean {
        return try {
            // Check Bluetooth support
            if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
                Log.e(TAG, "Device doesn't support Bluetooth")
                return false
            }

            bluetoothAdapter = getBluetoothAdapter()

            if (bluetoothAdapter == null) {
                Log.e(TAG, "Bluetooth not available")
                return false
            }

            if (!bluetoothAdapter!!.isEnabled) {
                Log.e(TAG, "Bluetooth is disabled")
                return false
            }

            val device = findPrinterDevice(printerName) ?: run {
                Log.e(TAG, "Printer device not found")
                return false
            }

            // Try standard connection first
            if (tryStandardConnection(device)) {
                true
            } else {
                // Fallback to alternative method
                tryAlternativeConnection(device)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Bluetooth permission denied", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed", e)
            false
        }
    }

    private fun findPrinterDevice(printerName: String): BluetoothDevice? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "BLUETOOTH_CONNECT permission required")
            null
        } else {
            bluetoothAdapter?.bondedDevices?.firstOrNull { device ->
                device.name.equals(printerName, ignoreCase = true) ||
                        device.name.contains(printerName, ignoreCase = true)
            }
        }
    }

    @RequiresPermission(allOf = [
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN
    ])
    private fun tryStandardConnection(device: BluetoothDevice): Boolean {
        // Add explicit permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "BLUETOOTH_CONNECT permission not granted")
                return false
            }
        } else {
            // For Android 10-11, we need location permission
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "ACCESS_FINE_LOCATION permission not granted")
                return false
            }
        }

        return try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothAdapter?.cancelDiscovery()
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            true
        } catch (e: IOException) {
            Log.e(TAG, "Standard connection failed", e)
            false
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException - permission denied", e)
            false
        }
    }

    private fun tryAlternativeConnection(device: BluetoothDevice): Boolean {
        return try {
            val method: Method = device.javaClass.getMethod(
                "createInsecureRfcommSocketToServiceRecord",
                UUID::class.java
            )
            bluetoothSocket = method.invoke(device, SPP_UUID) as BluetoothSocket
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            true
        } catch (e: Exception) {
            Log.e(TAG, "Alternative connection failed", e)
            false
        }
    }

    fun printText(text: String): Boolean {
        return try {
            outputStream?.write(text.toByteArray())
            outputStream?.flush()
            true
        } catch (e: IOException) {
            Log.e(TAG, "Print failed", e)
            false
        } catch (e: NullPointerException) {
            Log.e(TAG, "Output stream not initialized", e)
            false
        }
    }

    fun disconnect() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error disconnecting", e)
        } finally {
            outputStream = null
            bluetoothSocket = null
        }
    }
}