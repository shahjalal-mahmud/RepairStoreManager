package com.example.repairstoremanager.util

import com.example.repairstoremanager.data.model.Customer

fun buildInvoiceText(customer: Customer): String {
    val due = (customer.totalAmount.toIntOrNull() ?: 0) - (customer.advanced.toIntOrNull() ?: 0)

    return """
        Repair Store Manager
        ------------------------------
        Invoice No: ${customer.invoiceNumber}
        Date: ${customer.date}
        
        Name: ${customer.customerName}
        Phone: ${customer.contactNumber}
        
        Model: ${customer.phoneModel}
        Problem: ${customer.problem}
        Delivery: ${customer.deliveryDate}
        
        Total: ৳${customer.totalAmount}
        Advance: ৳${customer.advanced}
        Due: ৳$due

        Accessories:
        ${if (customer.battery) "✔ Battery" else ""}
        ${if (customer.sim) "✔ SIM" else ""}
        ${if (customer.memory) "✔ Memory" else ""}
        ${if (customer.simTray) "✔ SIM Tray" else ""}
        ${if (customer.backCover) "✔ Back Cover" else ""}
        
        ${if (customer.deadPermission) "☠ Permission for Dead Risk" else ""}
        
        ------------------------------
        Thank you!
        
    """.trimIndent()
}