package com.yes.billing.di.module

import android.content.Context
import com.yes.billing.data.repository.BillingRepository
import com.yes.billing.domain.usecase.GetSubscriptionStatusUseCase
import com.yes.billing.domain.usecase.LaunchBillingFlowUseCase
import com.yes.billing.domain.usecase.QueryProductsUseCase
import com.yes.billing.presentation.mapper.MapperUI
import com.yes.billing.presentation.vm.BillingViewModel
import com.yes.shared.di.module.IoDispatcher
import com.yes.shared.presentation.vm.BaseDependency
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher

@Module
class BillingModule {

    @Provides
    fun providesMapperUI(): MapperUI {
        return MapperUI()
    }

    @Provides
    fun providesBillingRepository(
        context: Context
    ): BillingRepository {
        return BillingRepository(context)
    }

    @Provides
    fun providesGetSubscriptionStatusUseCase(
        @IoDispatcher dispatcher: CoroutineDispatcher,
        billingRepository: BillingRepository
    ): GetSubscriptionStatusUseCase {
        return GetSubscriptionStatusUseCase(dispatcher, billingRepository)
    }

    @Provides
    fun providesQueryProductsUseCase(
        @IoDispatcher dispatcher: CoroutineDispatcher,
        billingRepository: BillingRepository
    ): QueryProductsUseCase {
        return QueryProductsUseCase(dispatcher, billingRepository)
    }

    @Provides
    fun providesLaunchBillingFlowUseCase(
        @IoDispatcher dispatcher: CoroutineDispatcher,
        billingRepository: BillingRepository
    ): LaunchBillingFlowUseCase {
        return LaunchBillingFlowUseCase(dispatcher, billingRepository)
    }

    @Provides
    fun providesBillingViewModelFactory(
        getSubscriptionStatusUseCase: GetSubscriptionStatusUseCase,
        queryProductsUseCase: QueryProductsUseCase,
        launchBillingFlowUseCase: LaunchBillingFlowUseCase,
        mapperUI: MapperUI
    ): BillingViewModel.Factory {
        return BillingViewModel.Factory(
            getSubscriptionStatusUseCase,
            queryProductsUseCase,
            launchBillingFlowUseCase,
            mapperUI
        )
    }

    @Provides
    fun providesDependency(
        factory: BillingViewModel.Factory
    ): BaseDependency {
        return BaseDependency(factory)
    }
}
