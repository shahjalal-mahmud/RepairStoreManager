package com.example.repairstoremanager.data.model

data class Product(
    val id: String = "",
    val shopOwnerId: String = "",
    val type: String = "",
    val name: String = "",
    val category: String = "",
    val subCategory: String = "",
    val model: String = "",
    val cost: Double = 0.0,
    val buyingPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val quantity: Long = 0L,
    val alertQuantity: Long = 0L,
    val supplier: String = "",
    val unit: String = "",
    val details: String = "",
    val imageUrl: String = "",
    val hasWarranty: Boolean = false,
    val warrantyDuration: String = "",
    val warrantyType: String = "", // "month", "year", etc.
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    fun getWarrantyDisplay(): String {
        return if (hasWarranty && warrantyDuration.isNotBlank()) {
            "$warrantyDuration ${warrantyType.takeIf { it.isNotBlank() } ?: "month"}"
        } else {
            "No warranty"
        }
    }
}