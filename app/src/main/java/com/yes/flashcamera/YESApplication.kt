package com.yes.flashcamera

import android.app.Application
import com.yes.camera.di.component.DaggerCameraComponent
import com.yes.camera.di.module.CameraModule
import com.yes.camera.presentation.vm.CameraViewModel
import com.yes.shared.di.component.DaggerSharedComponent
import com.yes.shared.di.module.SharedModule
import com.yes.shared.presentation.vm.BaseDependency

class YESApplication: Application(),CameraViewModel.DependencyResolver {
    private val sharedModule by lazy {
        SharedModule(this)
    }
    private val sharedComponent by lazy {
        DaggerSharedComponent.builder()
            .sharedModule(sharedModule)
            .build()
    }
    override fun resolveCameraDependency(): BaseDependency {
        return DaggerCameraComponent.builder()
            .cameraModule(CameraModule())
            .sharedComponent(sharedComponent)
            .build().getDependency()
    }

}