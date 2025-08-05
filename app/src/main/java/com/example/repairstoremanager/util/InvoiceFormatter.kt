package com.example.repairstoremanager.util

import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.data.model.StoreInfo

fun buildInvoiceText(customer: Customer, storeInfo: StoreInfo): String {
    val due = (customer.totalAmount.toIntOrNull() ?: 0) - (customer.advanced.toIntOrNull() ?: 0)
    val accessories = listOf(
        "Batt" to customer.battery,
        "SIM" to customer.sim,
        "Mem" to customer.memory,
        "Tray" to customer.simTray,
        "Cover" to customer.backCover
    ).filter { it.second }.map { it.first }

    return buildString {
        appendLine("     ${storeInfo.storeName.uppercase().take(24)}")
        appendLine("     ${storeInfo.address.take(30)}")
        appendLine("     Call: ${storeInfo.phone.take(15)}")
        appendLine("--------------------------------")
        appendLine("INVOICE: ${customer.invoiceNumber.padEnd(10)}")  
        appendLine("DATE   : ${customer.date}")
        appendLine("STATUS : ${customer.status.take(10).uppercase()}")
        appendLine("--------------------------------")
        appendLine("CUSTOMER INFO")
        appendLine("Name   : ${customer.customerName.take(20)}")
        appendLine("Phone  : ${customer.contactNumber.take(14)}")
        appendLine("--------------------------------")
        appendLine("DEVICE INFO")
        appendLine("Model  : ${customer.phoneModel.take(20)}")
        appendLine("Problem: ${customer.problem.take(20)}")
        appendLine("Delivery: ${customer.deliveryDate}")
        appendLine("--------------------------------")
        appendLine("PAYMENT INFO")
        appendLine("Total  : ${customer.totalAmount.padStart(10)}")
        appendLine("Advance: ${customer.advanced.padStart(10)}")
        appendLine("Due    : ${due.toString().padStart(10)}")
        appendLine("--------------------------------")
        if (accessories.isNotEmpty()) {
            appendLine("Accessories: ${accessories.joinToString(",")}")
        }
        if (customer.deadPermission) {
            appendLine("Note: DEAD APPROVED")
        }
        appendLine("--------------------------------")
        appendLine("     THANK YOU FOR CHOOSING US")
        appendLine(" Bring this invoice for delivery ")
        appendLine()
        appendLine()
        appendLine()
    }
}
