package com.yes.camera.di.module

import com.yes.camera.presentation.vm.CameraViewModel
import com.yes.shared.presentation.vm.BaseDependency
import dagger.Module
import dagger.Provides

@Module
class CameraModule {
    @Provides
    fun providesMainViewModelFactory(

    ): CameraViewModel.Factory {
        return CameraViewModel.Factory(


        )
    }

    @Provides
    fun providesDependency(
        factory: CameraViewModel.Factory,
    ): BaseDependency {
        return BaseDependency(
            factory
        )
    }
}