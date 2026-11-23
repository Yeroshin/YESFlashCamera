package com.yes.billing.domain.usecase

import android.app.Activity
import com.yes.billing.data.repository.BillingRepository
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher

data class LaunchBillingParams(
    val activity: Activity,
    val productId: String
)

class LaunchBillingFlowUseCase(
    dispatcher: CoroutineDispatcher,
    private val billingRepository: BillingRepository
) : UseCase<LaunchBillingParams, Boolean>(dispatcher) {
    override suspend fun run(params: LaunchBillingParams): Boolean {
        return billingRepository.launchBillingFlow(params.activity, params.productId)
    }
}
