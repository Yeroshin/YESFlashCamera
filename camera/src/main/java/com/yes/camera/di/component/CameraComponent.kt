package com.yes.camera.di.component

import com.yes.camera.di.CameraScope
import com.yes.camera.di.module.CameraModule
import com.yes.shared.presentation.vm.BaseDependency
import dagger.Component

@Component(
  //  dependencies = [SharedComponent::class],
    modules = [
        CameraModule::class
    ]
)
@CameraScope
interface CameraComponent {
    fun getDependency(): BaseDependency
}