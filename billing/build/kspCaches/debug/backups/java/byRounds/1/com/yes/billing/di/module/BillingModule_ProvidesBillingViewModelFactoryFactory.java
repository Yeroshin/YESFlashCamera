package com.yes.billing.di.module;

import com.yes.billing.domain.usecase.GetSubscriptionStatusUseCase;
import com.yes.billing.domain.usecase.LaunchBillingFlowUseCase;
import com.yes.billing.domain.usecase.QueryProductsUseCase;
import com.yes.billing.presentation.mapper.MapperUI;
import com.yes.billing.presentation.vm.BillingViewModel;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class BillingModule_ProvidesBillingViewModelFactoryFactory implements Factory<BillingViewModel.Factory> {
  private final BillingModule module;

  private final Provider<GetSubscriptionStatusUseCase> getSubscriptionStatusUseCaseProvider;

  private final Provider<QueryProductsUseCase> queryProductsUseCaseProvider;

  private final Provider<LaunchBillingFlowUseCase> launchBillingFlowUseCaseProvider;

  private final Provider<MapperUI> mapperUIProvider;

  public BillingModule_ProvidesBillingViewModelFactoryFactory(BillingModule module,
      Provider<GetSubscriptionStatusUseCase> getSubscriptionStatusUseCaseProvider,
      Provider<QueryProductsUseCase> queryProductsUseCaseProvider,
      Provider<LaunchBillingFlowUseCase> launchBillingFlowUseCaseProvider,
      Provider<MapperUI> mapperUIProvider) {
    this.module = module;
    this.getSubscriptionStatusUseCaseProvider = getSubscriptionStatusUseCaseProvider;
    this.queryProductsUseCaseProvider = queryProductsUseCaseProvider;
    this.launchBillingFlowUseCaseProvider = launchBillingFlowUseCaseProvider;
    this.mapperUIProvider = mapperUIProvider;
  }

  @Override
  public BillingViewModel.Factory get() {
    return providesBillingViewModelFactory(module, getSubscriptionStatusUseCaseProvider.get(), queryProductsUseCaseProvider.get(), launchBillingFlowUseCaseProvider.get(), mapperUIProvider.get());
  }

  public static BillingModule_ProvidesBillingViewModelFactoryFactory create(BillingModule module,
      Provider<GetSubscriptionStatusUseCase> getSubscriptionStatusUseCaseProvider,
      Provider<QueryProductsUseCase> queryProductsUseCaseProvider,
      Provider<LaunchBillingFlowUseCase> launchBillingFlowUseCaseProvider,
      Provider<MapperUI> mapperUIProvider) {
    return new BillingModule_ProvidesBillingViewModelFactoryFactory(module, getSubscriptionStatusUseCaseProvider, queryProductsUseCaseProvider, launchBillingFlowUseCaseProvider, mapperUIProvider);
  }

  public static BillingViewModel.Factory providesBillingViewModelFactory(BillingModule instance,
      GetSubscriptionStatusUseCase getSubscriptionStatusUseCase,
      QueryProductsUseCase queryProductsUseCase, LaunchBillingFlowUseCase launchBillingFlowUseCase,
      MapperUI mapperUI) {
    return Preconditions.checkNotNullFromProvides(instance.providesBillingViewModelFactory(getSubscriptionStatusUseCase, queryProductsUseCase, launchBillingFlowUseCase, mapperUI));
  }
}
