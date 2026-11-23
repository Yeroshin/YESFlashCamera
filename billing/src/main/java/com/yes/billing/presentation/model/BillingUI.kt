package com.yes.billing.presentation.model

data class BillingUI(
    val isActiveSubscription: Boolean,
    val activeProductId: String?,
    val products: List<ProductUI>
)
