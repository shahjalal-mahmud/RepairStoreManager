package com.example.repairstoremanager.util

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.OutputStream
import java.util.*

class POSPrinterHelper {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    fun connectToPrinter(printerName: String = "PT210"): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) return false

        val device: BluetoothDevice? = bluetoothAdapter?.bondedDevices?.firstOrNull {
            it.name.contains(printerName, ignoreCase = true)
        }

        device?.let {
            val uuid = it.uuids?.get(0)?.uuid ?: UUID.randomUUID()
            bluetoothSocket = it.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            return true
        }

        return false
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