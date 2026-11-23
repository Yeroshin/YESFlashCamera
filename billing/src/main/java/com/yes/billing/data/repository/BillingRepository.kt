package com.yes.billing.data.repository

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.yes.billing.domain.model.ProductInfo
import com.yes.billing.domain.model.SubscriptionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingRepository(
    private val context: Context
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _subscriptionStatus = MutableStateFlow(SubscriptionStatus(isActive = false))
    val subscriptionStatus: StateFlow<SubscriptionStatus> = _subscriptionStatus.asStateFlow()

    private val _products = MutableStateFlow<List<ProductInfo>>(emptyList())
    val products: StateFlow<List<ProductInfo>> = _products.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<Boolean>()
    val purchaseEvents: SharedFlow<Boolean> = _purchaseEvents.asSharedFlow()

    private var cachedProductDetails: List<ProductDetails> = emptyList()

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .build()

    init {
        startConnection()
    }

    fun startConnection() {
        if (billingClient.isReady) {
            queryActiveSubscriptions()
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryActiveSubscriptions()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry connection or handle disconnect
            }
        })
    }

    fun queryActiveSubscriptions() {
        val paramsSubs = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(paramsSubs) { billingResultSubs, purchasesListSubs ->
            val paramsInApp = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            billingClient.queryPurchasesAsync(paramsInApp) { billingResultInApp, purchasesListInApp ->
                val allPurchases = mutableListOf<Purchase>()
                if (billingResultSubs.responseCode == BillingClient.BillingResponseCode.OK) {
                    allPurchases.addAll(purchasesListSubs)
                }
                if (billingResultInApp.responseCode == BillingClient.BillingResponseCode.OK) {
                    allPurchases.addAll(purchasesListInApp)
                }
                handlePurchases(allPurchases)
            }
        }
    }

    fun queryProductDetails(productIds: List<String>) {
        val productList = productIds.map { id ->
            val productType = if (id.contains("lifetime")) {
                BillingClient.ProductType.INAPP
            } else {
                BillingClient.ProductType.SUBS
            }
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(productType)
                .build()
        }

        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                cachedProductDetails = productDetailsList
                val mapped = productDetailsList.map { details ->
                    val price = if (details.productType == BillingClient.ProductType.SUBS) {
                        val subDetails = details.subscriptionOfferDetails?.firstOrNull()
                        subDetails?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                    } else {
                        val oneTimeDetails = details.oneTimePurchaseOfferDetails
                        oneTimeDetails?.formattedPrice
                    } ?: ""

                    ProductInfo(
                        productId = details.productId,
                        title = details.title,
                        description = details.description,
                        price = price,
                        formattedPrice = price,
                        productType = details.productType
                    )
                }
                _products.value = mapped
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productId: String): Boolean {
        val productDetails = cachedProductDetails.find { it.productId == productId } ?: return false

        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        if (productDetails.productType == BillingClient.ProductType.SUBS) {
            val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            if (offerToken != null) {
                productDetailsParamsBuilder.setOfferToken(offerToken)
            }
        }

        val productDetailsParamsList = listOf(productDetailsParamsBuilder.build())

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
        return billingResult.responseCode == BillingClient.BillingResponseCode.OK
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
            scope.launch {
                _purchaseEvents.emit(true)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            scope.launch {
                _purchaseEvents.emit(false)
            }
        } else {
            scope.launch {
                _purchaseEvents.emit(false)
            }
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        var hasActiveSub = false
        var activeProductId: String? = null
        var activeToken: String? = null

        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                hasActiveSub = true
                activeProductId = purchase.products.firstOrNull()
                activeToken = purchase.purchaseToken
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase)
                }
            }
        }
        _subscriptionStatus.value = SubscriptionStatus(
            isActive = hasActiveSub,
            productId = activeProductId,
            purchaseToken = activeToken
        )
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { _ -> }
    }
}
