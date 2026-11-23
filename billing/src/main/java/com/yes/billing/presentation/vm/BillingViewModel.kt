package com.yes.billing.presentation.vm

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yes.billing.domain.usecase.GetSubscriptionStatusUseCase
import com.yes.billing.domain.usecase.LaunchBillingFlowUseCase
import com.yes.billing.domain.usecase.LaunchBillingParams
import com.yes.billing.domain.usecase.QueryProductsUseCase
import com.yes.billing.presentation.contract.BillingContract.*
import com.yes.billing.presentation.mapper.MapperUI
import com.yes.shared.presentation.vm.BaseDependency
import com.yes.shared.presentation.vm.BaseViewModel
import kotlinx.coroutines.flow.combine

class BillingViewModel(
    private val getSubscriptionStatusUseCase: GetSubscriptionStatusUseCase,
    private val queryProductsUseCase: QueryProductsUseCase,
    private val launchBillingFlowUseCase: LaunchBillingFlowUseCase,
    private val mapperUI: MapperUI
) : BaseViewModel<Event, State, Effect>() {

    interface DependencyResolver {
        fun resolveBillingDependency(): BaseDependency
    }

    init {
        withUseCaseScope(
            onError = { setState { copy(billingState = BillingState.Error(it.message ?: "Unknown error")) } },
            block = {
                val statusFlow = getSubscriptionStatusUseCase()
                val productsFlow = queryProductsUseCase(
                    listOf(
                        "yes_camera_subscription_monthly",
                        "yes_camera_subscription_yearly",
                        "yes_camera_lifetime"
                    )
                )

                combine(statusFlow, productsFlow) { status, products ->
                    BillingState.Success(mapperUI.map(status, products))
                }.collect { billingState ->
                    setState { copy(billingState = billingState) }
                }
            }
        )
    }

    override fun createInitialState(): State {
        return State(BillingState.Loading)
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is Event.OnSubscribe -> {
                launchBilling(event.activity, event.productId)
            }
            is Event.OnQueryProducts -> {
                queryProducts(event.productIds)
            }
        }
    }

    private fun launchBilling(activity: Activity, productId: String) {
        withUseCaseScope(
            onError = { setState { copy(billingState = BillingState.Error(it.message ?: "Purchase failed")) } },
            block = {
                val success = launchBillingFlowUseCase(LaunchBillingParams(activity, productId))
                if (!success) {
                    setState { copy(billingState = BillingState.Error("Failed to launch billing flow")) }
                }
            }
        )
    }

    private fun queryProducts(productIds: List<String>) {
        withUseCaseScope(
            onError = { },
            block = {
                queryProductsUseCase(productIds)
            }
        )
    }

    class Factory(
        val getSubscriptionStatusUseCase: GetSubscriptionStatusUseCase,
        val queryProductsUseCase: QueryProductsUseCase,
        val launchBillingFlowUseCase: LaunchBillingFlowUseCase,
        val mapperUI: MapperUI
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return BillingViewModel(
                getSubscriptionStatusUseCase,
                queryProductsUseCase,
                launchBillingFlowUseCase,
                mapperUI
            ) as T
        }
    }
}
