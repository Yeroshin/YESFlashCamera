package com.yes.billing.di.module;

import com.yes.billing.presentation.vm.BillingViewModel;
import com.yes.shared.presentation.vm.BaseDependency;
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
public final class BillingModule_ProvidesDependencyFactory implements Factory<BaseDependency> {
  private final BillingModule module;

  private final Provider<BillingViewModel.Factory> factoryProvider;

  public BillingModule_ProvidesDependencyFactory(BillingModule module,
      Provider<BillingViewModel.Factory> factoryProvider) {
    this.module = module;
    this.factoryProvider = factoryProvider;
  }

  @Override
  public BaseDependency get() {
    return providesDependency(module, factoryProvider.get());
  }

  public static BillingModule_ProvidesDependencyFactory create(BillingModule module,
      Provider<BillingViewModel.Factory> factoryProvider) {
    return new BillingModule_ProvidesDependencyFactory(module, factoryProvider);
  }

  public static BaseDependency providesDependency(BillingModule instance,
      BillingViewModel.Factory factory) {
    return Preconditions.checkNotNullFromProvides(instance.providesDependency(factory));
  }
}
