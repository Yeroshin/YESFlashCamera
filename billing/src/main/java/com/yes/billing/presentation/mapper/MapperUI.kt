package com.yes.billing.presentation.mapper

import com.yes.billing.domain.model.ProductInfo
import com.yes.billing.domain.model.SubscriptionStatus
import com.yes.billing.presentation.model.BillingUI
import com.yes.billing.presentation.model.ProductUI

class MapperUI {
    fun map(status: SubscriptionStatus, products: List<ProductInfo>): BillingUI {
        val productList = if (products.isEmpty()) {
            listOf(
                ProductUI(
                    productId = "yes_camera_subscription_monthly",
                    title = "Ежемесячная подписка",
                    description = "Сохранение снимков в режиме внешней вспышки на 1 месяц",
                    price = "299 ₽ / месяц"
                ),
                ProductUI(
                    productId = "yes_camera_subscription_yearly",
                    title = "Годовая подписка (Выгода 40%)",
                    description = "Сохранение снимков в режиме внешней вспышки на 1 год",
                    price = "1990 ₽ / год"
                ),
                ProductUI(
                    productId = "yes_camera_lifetime",
                    title = "Навсегда (Lifetime)",
                    description = "Пожизненное сохранение снимков в режиме внешней вспышки",
                    price = "3990 ₽"
                )
            )
        } else {
            products.map {
                val defaultTitle = when {
                    it.productId.contains("lifetime") -> "Навсегда (Lifetime)"
                    it.productId.contains("year") -> "Годовая подписка"
                    else -> "Ежемесячная подписка"
                }
                ProductUI(
                    productId = it.productId,
                    title = it.title.ifEmpty { defaultTitle },
                    description = it.description.ifEmpty { "Сохранение снимков в режиме внешней вспышки" },
                    price = it.price.ifEmpty { "299 ₽" }
                )
            }
        }

        return BillingUI(
            isActiveSubscription = status.isActive,
            activeProductId = status.productId,
            products = productList
        )
    }
}
