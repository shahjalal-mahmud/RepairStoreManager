package com.example.repairstoremanager.printer.model

import android.bluetooth.BluetoothDevice
import android.print.PrinterInfo
import com.example.repairstoremanager.printer.PrinterType

data class PrinterDevice(
    val name: String,
    val address: String,
    val type: PrinterType,
    val bluetoothDevice: BluetoothDevice? = null,
    val printerInfo: PrinterInfo? = null
)