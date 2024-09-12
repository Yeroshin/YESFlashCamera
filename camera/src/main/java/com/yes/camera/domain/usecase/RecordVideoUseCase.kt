package com.yes.camera.domain.usecase

import com.yes.camera.data.repository.CameraRepository
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher

class RecordVideoUseCase (dispatcher: CoroutineDispatcher,
                          private val cameraRepository: CameraRepository
) : UseCase<RecordVideoUseCase.Params, Unit>(dispatcher) {
    override suspend fun run(params:Params): Unit {

            cameraRepository.singleCapture(params.enable)

    }
    data class Params(val enable:Boolean)
}