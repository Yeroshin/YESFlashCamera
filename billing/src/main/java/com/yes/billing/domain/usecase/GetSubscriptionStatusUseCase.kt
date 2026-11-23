package com.yes.billing.domain.usecase

import com.yes.billing.data.repository.BillingRepository
import com.yes.billing.domain.model.SubscriptionStatus
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow

class GetSubscriptionStatusUseCase(
    dispatcher: CoroutineDispatcher,
    private val billingRepository: BillingRepository
) : UseCase<Unit, StateFlow<SubscriptionStatus>>(dispatcher) {
    override suspend fun run(): StateFlow<SubscriptionStatus> {
        return billingRepository.subscriptionStatus
    }
}
