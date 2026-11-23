package com.yes.billing.di.module;

import android.content.Context;
import com.yes.billing.data.repository.BillingRepository;
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
public final class BillingModule_ProvidesBillingRepositoryFactory implements Factory<BillingRepository> {
  private final BillingModule module;

  private final Provider<Context> contextProvider;

  public BillingModule_ProvidesBillingRepositoryFactory(BillingModule module,
      Provider<Context> contextProvider) {
    this.module = module;
    this.contextProvider = contextProvider;
  }

  @Override
  public BillingRepository get() {
    return providesBillingRepository(module, contextProvider.get());
  }

  public static BillingModule_ProvidesBillingRepositoryFactory create(BillingModule module,
      Provider<Context> contextProvider) {
    return new BillingModule_ProvidesBillingRepositoryFactory(module, contextProvider);
  }

  public static BillingRepository providesBillingRepository(BillingModule instance,
      Context context) {
    return Preconditions.checkNotNullFromProvides(instance.providesBillingRepository(context));
  }
}
