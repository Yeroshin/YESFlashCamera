package com.yes.camera.domain.usecase

import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.domain.model.Characteristics
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher

class SetOutputCharacteristicsUseCase (
    dispatcher: CoroutineDispatcher,
    private val cameraRepository: CameraRepository
) : UseCase<SetOutputCharacteristicsUseCase.Params, Unit>(dispatcher) {
    override suspend fun run(params: Params) {

        cameraRepository.startCaptureRequest(params.characteristics)

    }

    data class Params(
        val characteristics: Characteristics
    )
}