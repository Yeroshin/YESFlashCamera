package com.yes.billing.domain.usecase

import com.yes.billing.data.repository.BillingRepository
import com.yes.billing.domain.model.ProductInfo
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow

class QueryProductsUseCase(
    dispatcher: CoroutineDispatcher,
    private val billingRepository: BillingRepository
) : UseCase<List<String>, StateFlow<List<ProductInfo>>>(dispatcher) {
    override suspend fun run(params: List<String>): StateFlow<List<ProductInfo>> {
        billingRepository.queryProductDetails(params)
        return billingRepository.products
    }
}
