package com.yes.billing.di.module;

import com.yes.billing.data.repository.BillingRepository;
import com.yes.billing.domain.usecase.LaunchBillingFlowUseCase;
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
public final class BillingModule_ProvidesLaunchBillingFlowUseCaseFactory implements Factory<LaunchBillingFlowUseCase> {
  private final BillingModule module;

  private final Provider<CoroutineDispatcher> dispatcherProvider;

  private final Provider<BillingRepository> billingRepositoryProvider;

  public BillingModule_ProvidesLaunchBillingFlowUseCaseFactory(BillingModule module,
      Provider<CoroutineDispatcher> dispatcherProvider,
      Provider<BillingRepository> billingRepositoryProvider) {
    this.module = module;
    this.dispatcherProvider = dispatcherProvider;
    this.billingRepositoryProvider = billingRepositoryProvider;
  }

  @Override
  public LaunchBillingFlowUseCase get() {
    return providesLaunchBillingFlowUseCase(module, dispatcherProvider.get(), billingRepositoryProvider.get());
  }

  public static BillingModule_ProvidesLaunchBillingFlowUseCaseFactory create(BillingModule module,
      Provider<CoroutineDispatcher> dispatcherProvider,
      Provider<BillingRepository> billingRepositoryProvider) {
    return new BillingModule_ProvidesLaunchBillingFlowUseCaseFactory(module, dispatcherProvider, billingRepositoryProvider);
  }

  public static LaunchBillingFlowUseCase providesLaunchBillingFlowUseCase(BillingModule instance,
      CoroutineDispatcher dispatcher, BillingRepository billingRepository) {
    return Preconditions.checkNotNullFromProvides(instance.providesLaunchBillingFlowUseCase(dispatcher, billingRepository));
  }
}
