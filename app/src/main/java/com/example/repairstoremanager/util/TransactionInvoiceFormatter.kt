package com.example.repairstoremanager.util

import com.example.repairstoremanager.data.model.Transaction
import com.example.repairstoremanager.data.model.StoreInfo
import com.example.repairstoremanager.data.model.Product
import com.example.repairstoremanager.viewmodel.StockViewModel
import java.text.SimpleDateFormat
import java.util.*

fun buildTransactionInvoiceText(
    transaction: Transaction,
    storeInfo: StoreInfo,
    stockViewModel: StockViewModel
): String {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(transaction.timestamp))

    // Calculate warranty information for each product using StockViewModel
    val warrantyInfo = getDetailedWarrantyInfo(transaction, stockViewModel)

    return buildString {
        // Shop Header
        appendLine("     ${storeInfo.storeName.uppercase().take(24)}")
        appendLine("  ${storeInfo.address.take(30)}")
        appendLine("       Call: ${storeInfo.phone.take(15)}")
        appendLine("--------------------------------")

        // Transaction Header
        appendLine("DATE/TIME : ${formattedDate.take(20)}")
        appendLine("CUSTOMER  : ${transaction.customerName.take(20)}")
        appendLine("--------------------------------")

        // Product Details Header
        appendLine("ITEM                QTY   PRICE")
        appendLine("--------------------------------")

        // Product List
        transaction.products.forEach { product ->
            val productName = product.name.take(18).padEnd(18)
            val quantity = product.quantity.toString().padStart(3)
            val price = "৳${"%.0f".format(product.sellingPrice)}".padStart(6)

            appendLine("$productName $quantity  $price")

            // Show model if available
            val productDetails = getProductDetails(product.productId, stockViewModel)
            if (productDetails?.model?.isNotEmpty() == true) {
                appendLine("  Model: ${productDetails.model.take(22)}")
            }
        }

        appendLine("--------------------------------")

        // Totals
        appendLine("SUB-TOTAL : ${"৳${"%.2f".format(transaction.totalAmount)}".padStart(10)}")
        appendLine("--------------------------------")

        // Payment Info
        appendLine("PAYMENT METHOD")
        appendLine("${transaction.paymentType.uppercase().padStart(20)}")
        appendLine("--------------------------------")

        // Warranty & Guarantee Information
        if (warrantyInfo.isNotEmpty()) {
            appendLine("WARRANTY & GUARANTEE")
            appendLine("--------------------------------")
            warrantyInfo.forEach { line ->
                appendLine(line.take(32))
            }
            appendLine("--------------------------------")
        } else {
            appendLine("WARRANTY INFO")
            appendLine("No warranty/guarantee")
            appendLine("--------------------------------")
        }

        // Footer
        appendLine("     THANK YOU FOR YOUR BUSINESS")
        appendLine("   Keep this invoice for warranty")
        appendLine()
        appendLine()
        appendLine()
    }
}

private fun getDetailedWarrantyInfo(transaction: Transaction, stockViewModel: StockViewModel): List<String> {
    val warrantyInfo = mutableListOf<String>()
    var hasAnyWarranty = false

    transaction.products.forEach { product ->
        val productDetails = getProductDetails(product.productId, stockViewModel)
        if (productDetails != null) {
            val productName = productDetails.name.take(12)

            // Handle Warranty
            if (productDetails.hasWarranty && productDetails.warrantyDuration.isNotEmpty()) {
                hasAnyWarranty = true
                val warrantyEndDate = calculateEndDate(transaction.timestamp, productDetails.warrantyDuration, productDetails.warrantyType)
                val warrantyText = "$productName Warranty: ${formatDuration(productDetails.warrantyDuration, productDetails.warrantyType)}"
                warrantyInfo.add(warrantyText)
                warrantyInfo.add("  Valid till: $warrantyEndDate")
            }

            // Handle Guarantee
            if (productDetails.hasGuarantee && productDetails.guaranteeDuration.isNotEmpty()) {
                hasAnyWarranty = true
                val guaranteeEndDate = calculateEndDate(transaction.timestamp, productDetails.guaranteeDuration, productDetails.guaranteeType)
                val guaranteeText = "$productName Guarantee: ${formatDuration(productDetails.guaranteeDuration, productDetails.guaranteeType)}"
                warrantyInfo.add(guaranteeText)
                warrantyInfo.add("  Valid till: $guaranteeEndDate")
            }

            // Add separator between products if both warranty and guarantee exist
            if ((productDetails.hasWarranty || productDetails.hasGuarantee) && product != transaction.products.last()) {
                warrantyInfo.add("----------------")
            }
        }
    }

    return if (hasAnyWarranty) warrantyInfo else emptyList()
}

// Helper function to get product details from StockViewModel
private fun getProductDetails(productId: String, stockViewModel: StockViewModel): Product? {
    return stockViewModel.products.value.find { it.id == productId }
}

private fun calculateEndDate(purchaseTimestamp: Long, duration: String, type: String): String {
    try {
        val durationValue = duration.toIntOrNull() ?: return "Invalid duration"
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = purchaseTimestamp

        when (type.lowercase()) {
            "day", "days" -> calendar.add(Calendar.DAY_OF_MONTH, durationValue)
            "month", "months" -> calendar.add(Calendar.MONTH, durationValue)
            "year", "years" -> calendar.add(Calendar.YEAR, durationValue)
            else -> calendar.add(Calendar.MONTH, durationValue) // Default to months
        }

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    } catch (e: Exception) {
        return "Invalid date"
    }
}

private fun formatDuration(duration: String, type: String): String {
    val durationValue = duration.toIntOrNull() ?: return "Invalid"
    return when (type.lowercase()) {
        "day", "days" -> if (durationValue == 1) "1 Day" else "$durationValue Days"
        "month", "months" -> if (durationValue == 1) "1 Month" else "$durationValue Months"
        "year", "years" -> if (durationValue == 1) "1 Year" else "$durationValue Years"
        else -> "$durationValue ${type.capitalize()}" // Fallback
    }
}