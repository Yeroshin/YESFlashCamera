package com.yes.billing.di.component

import com.yes.billing.di.BillingScope
import com.yes.billing.di.module.BillingModule
import com.yes.shared.di.component.SharedComponent
import com.yes.shared.presentation.vm.BaseDependency
import dagger.Component

@Component(
    dependencies = [SharedComponent::class],
    modules = [BillingModule::class]
)
@BillingScope
interface BillingComponent {
    fun getDependency(): BaseDependency
}
