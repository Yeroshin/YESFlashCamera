package com.yes.billing.domain.model

data class ProductInfo(
    val productId: String,
    val title: String,
    val description: String,
    val price: String,
    val formattedPrice: String,
    val productType: String
)
