package com.yes.flashcamera

import android.app.Application
import com.yes.camera.di.component.DaggerCameraComponent
import com.yes.camera.di.module.CameraModule
import com.yes.camera.presentation.vm.CameraViewModel
import com.yes.shared.presentation.vm.BaseDependency

class YESApplication: Application(),CameraViewModel.DependencyResolver {
    override fun resolveCameraDependency(): BaseDependency {
        return DaggerCameraComponent.builder()
            .cameraModule(CameraModule())
            .build().getDependency()
    }

}