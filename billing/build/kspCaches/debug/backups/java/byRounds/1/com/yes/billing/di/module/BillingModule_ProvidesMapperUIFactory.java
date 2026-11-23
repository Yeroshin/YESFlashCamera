package com.yes.billing.di.module;

import com.yes.billing.presentation.mapper.MapperUI;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class BillingModule_ProvidesMapperUIFactory implements Factory<MapperUI> {
  private final BillingModule module;

  public BillingModule_ProvidesMapperUIFactory(BillingModule module) {
    this.module = module;
  }

  @Override
  public MapperUI get() {
    return providesMapperUI(module);
  }

  public static BillingModule_ProvidesMapperUIFactory create(BillingModule module) {
    return new BillingModule_ProvidesMapperUIFactory(module);
  }

  public static MapperUI providesMapperUI(BillingModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.providesMapperUI());
  }
}
