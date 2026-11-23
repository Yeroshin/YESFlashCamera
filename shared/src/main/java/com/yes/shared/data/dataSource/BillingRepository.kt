package com.yes.shared.data.dataSource

import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams

class BillingRepository (

    private val context: Context,
    private val onSubscriptionStateChanged: (Boolean) -> Unit
    ) : PurchasesUpdatedListener {

        private val billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .enablePrepaidPlans()
                    .build()
            )
            .build()

        // 1. Запуск подключения при старте приложения
        fun startConnection() {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        // Сразу проверяем активные подписки
                        queryActiveSubscriptions()
                    }
                }

                override fun onBillingServiceDisconnected() {
                    // Здесь стоит добавить логику переподключения с задержкой
                }
            })
        }

        // 2. Проверка активных подписок (вызывать при старте и в onResume)
        fun queryActiveSubscriptions() {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    handlePurchases(purchasesList)
                }
            }
        }

        // 3. Обработка результата новой покупки
        override fun onPurchasesUpdated(
            billingResult: BillingResult,
            purchases: MutableList<Purchase>?
        ) {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                handlePurchases(purchases)
            }
        }

        private fun handlePurchases(purchases: List<Purchase>) {
            var hasActiveSub = false

            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    hasActiveSub = true
                    // Обязательно подтверждаем подписку, иначе Google вернёт деньги
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }
                }
            }
            onSubscriptionStateChanged(hasActiveSub)
        }

        private fun acknowledgePurchase(purchase: Purchase) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient.acknowledgePurchase(params) { /* Проверка результата */ }
        }
    }

