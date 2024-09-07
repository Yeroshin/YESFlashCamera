package com.yes.camera.domain.usecase

import android.graphics.SurfaceTexture
import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.domain.model.Characteristics
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

class SetCharacteristicsUsCase(
    dispatcher: CoroutineDispatcher,
    private val cameraRepository: CameraRepository
) : UseCase<SetCharacteristicsUsCase.Params, Unit>(dispatcher) {
    override suspend fun run(params: Params) {

        cameraRepository.setCharacteristics(params.characteristics)

    }

    data class Params(
        val characteristics:Characteristics
    )
}