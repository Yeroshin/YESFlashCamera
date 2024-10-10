package com.yes.camera.di.module

import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.data.repository.MediaEncoder
import com.yes.camera.domain.usecase.OpenCameraUseCase
import com.yes.camera.domain.usecase.RecordVideoUseCase
import com.yes.camera.domain.usecase.SetCharacteristicsUsCase
import com.yes.camera.presentation.mapper.MapperUI
import com.yes.camera.presentation.vm.CameraViewModel
import com.yes.camera.utils.ShutterSpeedsResourcesProvider
import com.yes.shared.di.module.IoDispatcher
import com.yes.shared.presentation.vm.BaseDependency
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
class CameraModule {
    @Provides
    fun providesMapper(
    ): MapperUI {
        return MapperUI()
    }
    @Provides
    fun providesMediaEncoder(
    ): MediaEncoder {
        return MediaEncoder()
    }
@Singleton
    @Provides
    fun providesCameraRepository(
        context: Context,
        encoder:MediaEncoder
    ): CameraRepository {
        return CameraRepository(
            context,
            context.getSystemService(CAMERA_SERVICE) as CameraManager,
            encoder
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
    fun providesSetCharacteristicsUseCase(
        @IoDispatcher dispatcher: CoroutineDispatcher,
        cameraRepository: CameraRepository
    ): SetCharacteristicsUsCase {
        return SetCharacteristicsUsCase(
            dispatcher,
            cameraRepository
        )
    }
    @Provides
    fun providesSetRecordVideoUseCase(
        @IoDispatcher dispatcher: CoroutineDispatcher,
        cameraRepository: CameraRepository
    ): RecordVideoUseCase {
        return RecordVideoUseCase(
            dispatcher,
            cameraRepository
        )
    }

    @Provides
    fun providesMainViewModelFactory(
        mapper: MapperUI,
        openCameraUseCase: OpenCameraUseCase,
        setCharacteristicsUsCase: SetCharacteristicsUsCase,
        recordVideoUseCase: RecordVideoUseCase
    ): CameraViewModel.Factory {
        return CameraViewModel.Factory(
            mapper,
            openCameraUseCase,
            setCharacteristicsUsCase,
            recordVideoUseCase
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