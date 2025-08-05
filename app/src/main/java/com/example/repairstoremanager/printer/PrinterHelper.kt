package com.example.repairstoremanager.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Build
import android.print.PrintManager
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.repairstoremanager.printer.model.PrinterDevice
import java.io.IOException
import java.io.OutputStream
import java.lang.reflect.Method
import java.util.UUID

class PrinterHelper(private val context: Context) {

    companion object {
        private const val TAG = "PrinterHelper"
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var currentPrinter: PrinterDevice? = null

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

    @SuppressLint("MissingPermission")
    @RequiresPermission(allOf = [
        android.Manifest.permission.BLUETOOTH_CONNECT,
        android.Manifest.permission.BLUETOOTH_SCAN
    ])
    fun connectToPrinter(printerDevice: PrinterDevice): Boolean {
        return when (printerDevice.type) {
            PrinterType.POS -> connectToPOSPrinter(printerDevice)
            PrinterType.STANDARD -> connectToStandardPrinter(printerDevice)
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToPOSPrinter(printerDevice: PrinterDevice): Boolean {
        return try {
            bluetoothAdapter = getBluetoothAdapter()

            if (bluetoothAdapter == null) {
                Log.e(TAG, "Bluetooth not available")
                return false
            }

            if (!bluetoothAdapter!!.isEnabled) {
                Log.e(TAG, "Bluetooth is disabled")
                return false
            }

            val device = printerDevice.bluetoothDevice ?: run {
                Log.e(TAG, "Bluetooth device not found")
                return false
            }

            // Try standard connection first
            if (tryStandardConnection(device)) {
                currentPrinter = printerDevice
                true
            } else {
                // Fallback to alternative method
                tryAlternativeConnection(device).also { success ->
                    if (success) currentPrinter = printerDevice
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed", e)
            false
        }
    }

    private fun connectToStandardPrinter(printerDevice: PrinterDevice): Boolean {
        // Standard printers don't need explicit connection
        currentPrinter = printerDevice
        return true
    }

    @SuppressLint("MissingPermission")
    private fun tryStandardConnection(device: BluetoothDevice): Boolean {
        return try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothAdapter?.cancelDiscovery()
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            true
        } catch (e: IOException) {
            Log.e(TAG, "Standard connection failed", e)
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
        currentPrinter ?: return false

        return when (currentPrinter!!.type) {
            PrinterType.POS -> printToPOS(text)
            PrinterType.STANDARD -> printToStandardPrinter(text)
        }
    }

    private fun printToPOS(text: String): Boolean {
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

    private fun printToStandardPrinter(text: String): Boolean {
        // Implement standard Android printing
        // This is a simplified version - you might want to create a proper PrintDocumentAdapter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "${context.packageName} - Document"

            // In a real app, you would create a proper PrintDocumentAdapter
            // Here we just show the print dialog
            printManager.print(jobName, null, null)
            return true
        }
        return false
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
            currentPrinter = null
        }
    }

    fun getCurrentPrinter(): PrinterDevice? = currentPrinter
}