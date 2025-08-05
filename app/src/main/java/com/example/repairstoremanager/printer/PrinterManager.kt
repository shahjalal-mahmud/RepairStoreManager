package com.example.repairstoremanager.printer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.example.repairstoremanager.printer.model.PrinterDevice
import java.util.*

class PrinterManager(private val context: Context) {

    companion object {
        private const val TAG = "PrinterManager"
    }

    private val printerHelper = PrinterHelper(context)

    @SuppressLint("MissingPermission")
    @RequiresPermission(allOf = [
        android.Manifest.permission.BLUETOOTH_CONNECT,
        android.Manifest.permission.BLUETOOTH_SCAN
    ])
    fun getAvailablePrinters(): List<PrinterDevice> {
        val printers = mutableListOf<PrinterDevice>()

        // Get Bluetooth POS printers
        if (hasBluetoothPermissions()) {
            printers.addAll(getBluetoothPrinters())
        }

        // Get standard printers (API 19+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            printers.addAll(getStandardPrinters())
        }

        return printers
    }

    @SuppressLint("MissingPermission")
    private fun getBluetoothPrinters(): List<PrinterDevice> {
        val printers = mutableListOf<PrinterDevice>()
        val bluetoothAdapter = getBluetoothAdapter() ?: return emptyList()

        if (!bluetoothAdapter.isEnabled) {
            Log.w(TAG, "Bluetooth is disabled")
            return emptyList()
        }

        bluetoothAdapter.bondedDevices?.forEach { device ->
            printers.add(
                PrinterDevice(
                    name = device.name ?: "Unknown Bluetooth Device",
                    address = device.address,
                    type = PrinterType.POS,
                    bluetoothDevice = device
                )
            )
        }

        return printers
    }

    @SuppressLint("ServiceCast")
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun getStandardPrinters(): List<PrinterDevice> {
        val printers = mutableListOf<PrinterDevice>()
        try {
            val printerManager = context.getSystemService(Context.PRINT_SERVICE) as android.print.PrinterManager
            printerManager.printers.forEach { printer ->
                printers.add(
                    PrinterDevice(
                        name = printer.name,
                        address = printer.id.localId ?: "",
                        type = PrinterType.STANDARD,
                        printerInfo = printer
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting standard printers", e)
        }
        return printers
    }

    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    private fun getBluetoothAdapter(): BluetoothAdapter? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
            bluetoothManager.adapter
        } else {
            @Suppress("DEPRECATION")
            BluetoothAdapter.getDefaultAdapter()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    fun connectToPrinter(printer: PrinterDevice): Boolean {
        return printerHelper.connectToPrinter(printer)
    }

    fun printText(text: String): Boolean {
        return printerHelper.printText(text)
    }

    fun disconnect() {
        printerHelper.disconnect()
    }

    fun getCurrentPrinter(): PrinterDevice? {
        return printerHelper.getCurrentPrinter()
    }
}