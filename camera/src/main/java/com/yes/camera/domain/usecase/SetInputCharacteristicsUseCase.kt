package com.yes.camera.domain.usecase

import android.annotation.SuppressLint
import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.data.repository.SettingsRepository
import com.yes.camera.domain.model.Characteristics
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher

@SuppressLint("NewApi")
class SetInputCharacteristicsUseCase(
    dispatcher: CoroutineDispatcher,
    private val cameraRepository: CameraRepository,
    private val settingsRepository: SettingsRepository
) : UseCase<SetInputCharacteristicsUseCase.Params, Unit>(dispatcher) {
    
    // Мгновенная часть: Камера
    override fun runSync(params: Params) {
        cameraRepository.startPreviewCaptureRequest(params.characteristics)
    }

    // Асинхронная часть: Диск
    override suspend fun run(params: Params) {
        settingsRepository.setCharacteristics(params.characteristics)
    }

    data class Params(
        val characteristics:Characteristics
    )
}
