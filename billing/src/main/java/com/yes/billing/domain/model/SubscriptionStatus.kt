package com.yes.billing.domain.model

data class SubscriptionStatus(
    val isActive: Boolean,
    val productId: String? = null,
    val purchaseToken: String? = null,
    val expiryTimeMillis: Long = 0L
)
