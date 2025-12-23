package com.yes.camera.domain.usecase

import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.data.repository.SettingsRepository
import com.yes.camera.domain.model.Characteristics
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher

class SetInputCharacteristicsUseCase(
    dispatcher: CoroutineDispatcher,
    private val cameraRepository: CameraRepository,
    private val settingsRepository: SettingsRepository
) : UseCase<SetInputCharacteristicsUseCase.Params, Unit>(dispatcher) {
    override suspend fun run(params: Params) {
        cameraRepository.startCaptureRequest(params.characteristics)
        settingsRepository.setCharacteristics(params.characteristics)


    }

    data class Params(
        val characteristics:Characteristics
    )
}