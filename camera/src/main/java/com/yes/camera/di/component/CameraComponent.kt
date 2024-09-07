package com.yes.camera.di.component

import com.yes.camera.di.CameraScope
import com.yes.camera.di.module.CameraModule
import com.yes.shared.di.component.SharedComponent
import com.yes.shared.presentation.vm.BaseDependency
import dagger.Component
import javax.inject.Singleton

@Component(
    dependencies = [SharedComponent::class],
    modules = [
        CameraModule::class
    ]
)
@Singleton
interface CameraComponent {
    fun getDependency(): BaseDependency
}