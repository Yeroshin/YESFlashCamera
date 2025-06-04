package com.yes.flashcamera

import android.app.Application
import com.yes.camera.di.component.DaggerCameraComponent
import com.yes.camera.di.module.CameraModule
import com.yes.camera.presentation.vm.CameraViewModel
import com.yes.flashcamera.presentation.MainActivity
import com.yes.settings.di.component.DaggerSettingsComponent
import com.yes.settings.di.module.SettingsModule
import com.yes.settings.presentation.wm.SettingsViewModel
import com.yes.shared.di.component.DaggerSharedComponent
import com.yes.shared.di.module.SharedModule
import com.yes.shared.presentation.vm.BaseDependency

class YESApplication:
    Application(),
    CameraViewModel.DependencyResolver,
SettingsViewModel.DependencyResolver {
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

    override fun resolveSettingsDependency(): BaseDependency {
        return DaggerSettingsComponent.builder()
            .settingsModule(SettingsModule())
            .sharedComponent(sharedComponent)
            .build().getDependency()
    }

}