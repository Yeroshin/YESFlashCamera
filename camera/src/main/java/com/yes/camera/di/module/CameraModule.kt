package com.yes.camera.di.module

import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.hardware.camera2.CameraManager
import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.data.repository.MediaEncoder
import com.yes.camera.domain.usecase.OpenCameraUseCase
import com.yes.camera.domain.usecase.RecordVideoUseCase
import com.yes.camera.domain.usecase.SetInputCharacteristicsUseCase
import com.yes.camera.domain.usecase.SubscribeHistogramUseCase
import com.yes.camera.presentation.mapper.MapperUI
import com.yes.camera.presentation.vm.CameraViewModel
import com.yes.camera.utils.AndroidResourceProvider
import com.yes.camera.utils.ResourceProvider
import com.yes.shared.di.module.IoDispatcher
import com.yes.shared.presentation.vm.BaseDependency
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
class CameraModule {
    @Provides
    fun providesAndroidResourceProvider(
        context: Context,
    ): ResourceProvider {
        return AndroidResourceProvider(
            context
        )
    }
    @Provides
    fun providesMapper(
        resources: ResourceProvider
    ): MapperUI {
        return MapperUI(
            resources
        )
    }
    @Singleton
    @Provides
    fun providesMediaEncoder(
    ): MediaEncoder {
        return MediaEncoder()
    }

    @Provides
    fun providesSubscribeHistogramUseCase(
        @IoDispatcher dispatcher: CoroutineDispatcher,
        cameraRepository: CameraRepository
    ): SubscribeHistogramUseCase {
        return SubscribeHistogramUseCase(
            dispatcher,
            cameraRepository
        )
    }
    @Singleton
    @Provides
    fun providesCameraRepository(
        context: Context,
        encoder: MediaEncoder
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
    ): SetInputCharacteristicsUseCase {
        return SetInputCharacteristicsUseCase(
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
        setInputCharacteristicsUseCase: SetInputCharacteristicsUseCase,
        recordVideoUseCase: RecordVideoUseCase,
        subscribeHistogramUseCase:SubscribeHistogramUseCase,
    ): CameraViewModel.Factory {
        return CameraViewModel.Factory(
            mapper,
            openCameraUseCase,
            setInputCharacteristicsUseCase,
            recordVideoUseCase,
            subscribeHistogramUseCase
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