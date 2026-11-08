package com.yes.camera.domain.usecase

import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import androidx.annotation.RequiresApi
import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.data.repository.SettingsRepository
import com.yes.camera.domain.model.Characteristics
import com.yes.shared.domain.Dimensions
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

class OpenCameraUseCase(
    dispatcher: CoroutineDispatcher,
    private val cameraRepository: CameraRepository,
    private val settingsRepository: SettingsRepository,
    private val closeCameraUseCase: CloseCameraUseCase
) : UseCase<OpenCameraUseCase.Params, Unit>(dispatcher) {

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun run(params: Params) {
        // 1. Закрываем текущую камеру и сессию, освобождая ресурсы перед открытием новой камеры
        closeCameraUseCase()

        // 2. Получаем ориентацию сенсора целевой камеры и обновляем характеристики (поворот проекции на закрытой камере ДО открытия новой)
        val targetOrientation = cameraRepository.getCameraSensorOrientation(params.backCamera)
        settingsRepository.setBackCamera(params.backCamera)
        cameraRepository.updateCharacteristicsForSwitch(params.backCamera, targetOrientation)

        // 3. Открываем выбранную камеру (основную или фронтальную)
        val settingsCharacteristics = settingsRepository.getCharacteristics()
        val cameraCharacteristics = cameraRepository.openCamera(params.backCamera)

        // 3. Ждем получения аппаратных характеристик (поддерживаемые размеры и т.д.)
        val hardwareInfo = cameraCharacteristics.filterNotNull().first()

        // 4. Определяем рабочее разрешение для этой камеры
        val selectedResolution = if (settingsCharacteristics.resolution.width > 0 &&
            hardwareInfo.resolutionItems.contains(settingsCharacteristics.resolution)) {
            settingsCharacteristics.resolution
        } else {
            val bestSize = hardwareInfo.resolutionItems.maxByOrNull { it.width * it.height } ?: Dimensions(640, 480)
            settingsRepository.setResolutionValue(bestSize)
            bestSize
        }

        // 5. Сохраняем список всех доступных разрешений
        settingsRepository.setResolutions(hardwareInfo.resolutionItems)

        // 5.1. Записываем актуальную поддержку RAW/DNG в настройки
        settingsRepository.setSupportsRaw(hardwareInfo.supportsRaw)

        // 6. Запускаем сессию с открытой камерой
        cameraRepository.startSession(
            params.glSurfaceTexture,
            settingsCharacteristics.copy(
                backCamera = params.backCamera,
                resolution = selectedResolution,
                supportsRaw = hardwareInfo.supportsRaw
            )
        )
    }

    data class Params(
        val backCamera: Boolean,
        val glSurfaceTexture: SurfaceTexture
    )
}
