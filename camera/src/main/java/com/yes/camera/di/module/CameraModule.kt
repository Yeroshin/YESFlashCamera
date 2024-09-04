package com.yes.camera.di.module

import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.domain.usecase.OpenCameraUseCase
import com.yes.camera.presentation.mapper.MapperUI
import com.yes.camera.presentation.vm.CameraViewModel
import com.yes.shared.di.module.IoDispatcher
import com.yes.shared.presentation.vm.BaseDependency
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher

@Module
class CameraModule {
    @Provides
    fun providesMapper(): MapperUI {
        return MapperUI()
    }

    @Provides
    fun providesCameraRepository(
        context: Context
    ): CameraRepository {
        val mBackgroundThread = HandlerThread("CameraThread").apply { start() }
        return CameraRepository(
            context.getSystemService(CAMERA_SERVICE) as CameraManager,
            Handler(mBackgroundThread.looper),
        )
    }

    @Provides
    fun providesOpenCameraUseCase(
        @IoDispatcher dispatcher: CoroutineDispatcher,
        cameraRepository: CameraRepository
    ): OpenCameraUseCase {
        return OpenCameraUseCase(
            dispatcher,
            cameraRepository
        )
    }

    @Provides
    fun providesMainViewModelFactory(
        mapper: MapperUI,
        openCameraUseCase: OpenCameraUseCase
    ): CameraViewModel.Factory {
        return CameraViewModel.Factory(
            mapper,
            openCameraUseCase
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