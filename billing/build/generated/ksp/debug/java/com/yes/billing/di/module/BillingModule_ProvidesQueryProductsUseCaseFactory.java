package com.yes.billing.di.module;

import com.yes.billing.data.repository.BillingRepository;
import com.yes.billing.domain.usecase.QueryProductsUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import kotlinx.coroutines.CoroutineDispatcher;

@ScopeMetadata
@QualifierMetadata("com.yes.shared.di.module.IoDispatcher")
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
public final class BillingModule_ProvidesQueryProductsUseCaseFactory implements Factory<QueryProductsUseCase> {
  private final BillingModule module;

  private final Provider<CoroutineDispatcher> dispatcherProvider;

  private final Provider<BillingRepository> billingRepositoryProvider;

  public BillingModule_ProvidesQueryProductsUseCaseFactory(BillingModule module,
      Provider<CoroutineDispatcher> dispatcherProvider,
      Provider<BillingRepository> billingRepositoryProvider) {
    this.module = module;
    this.dispatcherProvider = dispatcherProvider;
    this.billingRepositoryProvider = billingRepositoryProvider;
  }

  @Override
  public QueryProductsUseCase get() {
    return providesQueryProductsUseCase(module, dispatcherProvider.get(), billingRepositoryProvider.get());
  }

  public static BillingModule_ProvidesQueryProductsUseCaseFactory create(BillingModule module,
      Provider<CoroutineDispatcher> dispatcherProvider,
      Provider<BillingRepository> billingRepositoryProvider) {
    return new BillingModule_ProvidesQueryProductsUseCaseFactory(module, dispatcherProvider, billingRepositoryProvider);
  }

  public static QueryProductsUseCase providesQueryProductsUseCase(BillingModule instance,
      CoroutineDispatcher dispatcher, BillingRepository billingRepository) {
    return Preconditions.checkNotNullFromProvides(instance.providesQueryProductsUseCase(dispatcher, billingRepository));
  }
}
