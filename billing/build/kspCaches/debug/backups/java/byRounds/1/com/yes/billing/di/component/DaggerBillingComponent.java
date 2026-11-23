package com.yes.billing.di.component;

import com.yes.billing.data.repository.BillingRepository;
import com.yes.billing.di.module.BillingModule;
import com.yes.billing.di.module.BillingModule_ProvidesBillingRepositoryFactory;
import com.yes.billing.di.module.BillingModule_ProvidesBillingViewModelFactoryFactory;
import com.yes.billing.di.module.BillingModule_ProvidesDependencyFactory;
import com.yes.billing.di.module.BillingModule_ProvidesGetSubscriptionStatusUseCaseFactory;
import com.yes.billing.di.module.BillingModule_ProvidesLaunchBillingFlowUseCaseFactory;
import com.yes.billing.di.module.BillingModule_ProvidesMapperUIFactory;
import com.yes.billing.di.module.BillingModule_ProvidesQueryProductsUseCaseFactory;
import com.yes.billing.domain.usecase.GetSubscriptionStatusUseCase;
import com.yes.billing.domain.usecase.LaunchBillingFlowUseCase;
import com.yes.billing.domain.usecase.QueryProductsUseCase;
import com.yes.billing.presentation.vm.BillingViewModel;
import com.yes.shared.di.component.SharedComponent;
import com.yes.shared.presentation.vm.BaseDependency;
import dagger.internal.DaggerGenerated;
import dagger.internal.Preconditions;
import javax.annotation.processing.Generated;

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
public final class DaggerBillingComponent {
  private DaggerBillingComponent() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private BillingModule billingModule;

    private SharedComponent sharedComponent;

    private Builder() {
    }

    public Builder billingModule(BillingModule billingModule) {
      this.billingModule = Preconditions.checkNotNull(billingModule);
      return this;
    }

    public Builder sharedComponent(SharedComponent sharedComponent) {
      this.sharedComponent = Preconditions.checkNotNull(sharedComponent);
      return this;
    }

    public BillingComponent build() {
      if (billingModule == null) {
        this.billingModule = new BillingModule();
      }
      Preconditions.checkBuilderRequirement(sharedComponent, SharedComponent.class);
      return new BillingComponentImpl(billingModule, sharedComponent);
    }
  }

  private static final class BillingComponentImpl implements BillingComponent {
    private final BillingModule billingModule;

    private final SharedComponent sharedComponent;

    private final BillingComponentImpl billingComponentImpl = this;

    private BillingComponentImpl(BillingModule billingModuleParam,
        SharedComponent sharedComponentParam) {
      this.billingModule = billingModuleParam;
      this.sharedComponent = sharedComponentParam;

    }

    private BillingRepository billingRepository() {
      return BillingModule_ProvidesBillingRepositoryFactory.providesBillingRepository(billingModule, Preconditions.checkNotNullFromComponent(sharedComponent.providesContext()));
    }

    private GetSubscriptionStatusUseCase getSubscriptionStatusUseCase() {
      return BillingModule_ProvidesGetSubscriptionStatusUseCaseFactory.providesGetSubscriptionStatusUseCase(billingModule, Preconditions.checkNotNullFromComponent(sharedComponent.providesIoDispatcher()), billingRepository());
    }

    private QueryProductsUseCase queryProductsUseCase() {
      return BillingModule_ProvidesQueryProductsUseCaseFactory.providesQueryProductsUseCase(billingModule, Preconditions.checkNotNullFromComponent(sharedComponent.providesIoDispatcher()), billingRepository());
    }

    private LaunchBillingFlowUseCase launchBillingFlowUseCase() {
      return BillingModule_ProvidesLaunchBillingFlowUseCaseFactory.providesLaunchBillingFlowUseCase(billingModule, Preconditions.checkNotNullFromComponent(sharedComponent.providesIoDispatcher()), billingRepository());
    }

    private BillingViewModel.Factory billingViewModelFactory() {
      return BillingModule_ProvidesBillingViewModelFactoryFactory.providesBillingViewModelFactory(billingModule, getSubscriptionStatusUseCase(), queryProductsUseCase(), launchBillingFlowUseCase(), BillingModule_ProvidesMapperUIFactory.providesMapperUI(billingModule));
    }

    @Override
    public BaseDependency getDependency() {
      return BillingModule_ProvidesDependencyFactory.providesDependency(billingModule, billingViewModelFactory());
    }
  }
}
