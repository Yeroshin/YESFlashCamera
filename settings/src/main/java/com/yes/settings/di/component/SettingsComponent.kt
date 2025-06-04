package com.yes.settings.di.component

import com.yes.settings.di.SettingsScope
import com.yes.settings.di.module.SettingsModule
import com.yes.shared.di.component.SharedComponent
import com.yes.shared.presentation.vm.BaseDependency
import dagger.Component

@Component(
    dependencies = [SharedComponent::class],
    modules = [
        SettingsModule::class
    ]
)
@SettingsScope
interface SettingsComponent {
    fun getDependency(): BaseDependency
}