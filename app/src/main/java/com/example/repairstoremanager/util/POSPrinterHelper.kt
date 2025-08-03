package com.example.repairstoremanager.util

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import androidx.annotation.RequiresPermission
import java.io.OutputStream
import java.util.UUID

class POSPrinterHelper {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    fun connectToPrinter(printerName: String = "PT-210_00D1"): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) return false

        val device = bluetoothAdapter?.bondedDevices?.firstOrNull {
            it.name.contains(printerName, ignoreCase = true)
        } ?: return false

        return try {
            // Common Serial Port UUID
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)

            bluetoothAdapter?.cancelDiscovery() // Very important
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun disconnect() {
        outputStream?.close()
        bluetoothSocket?.close()
    }

    fun printText(text: String) {
        outputStream?.write(text.toByteArray())
        outputStream?.flush()
    }
}