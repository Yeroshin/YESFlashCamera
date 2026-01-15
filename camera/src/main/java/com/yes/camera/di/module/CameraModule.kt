package com.yes.camera.di.module

import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.data.repository.MediaEncoder
import com.yes.camera.data.repository.SettingsRepository
import com.yes.camera.di.CameraScope
import com.yes.camera.domain.usecase.CloseCameraUseCase
import com.yes.camera.domain.usecase.OpenCameraUseCase
import com.yes.camera.domain.usecase.RecordVideoUseCase
import com.yes.camera.domain.usecase.SetInputCharacteristicsUseCase
import com.yes.camera.domain.usecase.SubscribeCameraSettingsUseCase
import com.yes.camera.presentation.mapper.MapperUI
import com.yes.camera.presentation.vm.CameraViewModel
import com.yes.camera.utils.AndroidResourceProvider
import com.yes.camera.utils.ResourceProvider
import com.yes.shared.data.dataSource.SettingsDataSource
import com.yes.shared.di.module.CameraDispatcher
import com.yes.shared.di.module.IoDispatcher
import com.yes.shared.presentation.vm.BaseDependency
import com.yes.shared.utils.CameraThreadManager
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher

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
    @CameraScope
    @Provides
    fun providesMediaEncoder(
    ): MediaEncoder {
        return MediaEncoder()
    }

    @Provides
    fun providesSubscribeHistogramUseCase(
        @CameraDispatcher dispatcher: CoroutineDispatcher,
        cameraRepository: CameraRepository,
        settingsRepository: SettingsRepository
    ): SubscribeCameraSettingsUseCase {
        return SubscribeCameraSettingsUseCase(
            dispatcher,
            cameraRepository,
            settingsRepository
        )
    }
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @CameraScope
    @Provides
    fun providesCameraRepository(
        cameraThreadManager:CameraThreadManager,
        context: Context,
        encoder: MediaEncoder
    ): CameraRepository {
        return CameraRepository(
            cameraThreadManager,
            context,
            context.getSystemService(CAMERA_SERVICE) as CameraManager,
            encoder
        )
    }

    @Provides
    fun providesOpenCameraUseCase(
        @CameraDispatcher dispatcher: CoroutineDispatcher,
        cameraRepository: CameraRepository,
        settingsRepository: SettingsRepository
    ): OpenCameraUseCase {
        return OpenCameraUseCase(
            dispatcher,
            cameraRepository,
            settingsRepository
        )
    }
    @Provides
    fun providesCloseCameraUseCase(
        @CameraDispatcher dispatcher: CoroutineDispatcher,
        cameraRepository: CameraRepository,
    ): CloseCameraUseCase {
        return CloseCameraUseCase(
            dispatcher,
            cameraRepository,
        )
    }

    @Provides
    fun providesSettingsRepository(
        settingsDataSource: SettingsDataSource
    ): SettingsRepository {
        return SettingsRepository(
            settingsDataSource
        )
    }

    @Provides
    fun providesSetCharacteristicsUseCase(
        @CameraDispatcher dispatcher: CoroutineDispatcher,
        cameraRepository: CameraRepository,
        settingsRepository: SettingsRepository
    ): SetInputCharacteristicsUseCase {
        return SetInputCharacteristicsUseCase(
            dispatcher,
            cameraRepository,
            settingsRepository
        )
    }

    @Provides
    fun providesSetRecordVideoUseCase(
        @CameraDispatcher dispatcher: CoroutineDispatcher,
        cameraRepository: CameraRepository
    ): RecordVideoUseCase {
        return RecordVideoUseCase(
            dispatcher,
            cameraRepository
        )
    }


    @Provides
    fun providesCameraViewModelFactory(
        mapper: MapperUI,
        openCameraUseCase: OpenCameraUseCase,
        closeCameraUseCase: CloseCameraUseCase,
        setInputCharacteristicsUseCase: SetInputCharacteristicsUseCase,
        recordVideoUseCase: RecordVideoUseCase,
        subscribeCameraSettingsUseCase:SubscribeCameraSettingsUseCase,
    ): CameraViewModel.Factory {
        return CameraViewModel.Factory(
            mapper,
            openCameraUseCase,
            closeCameraUseCase,
            setInputCharacteristicsUseCase,
            recordVideoUseCase,
            subscribeCameraSettingsUseCase,
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