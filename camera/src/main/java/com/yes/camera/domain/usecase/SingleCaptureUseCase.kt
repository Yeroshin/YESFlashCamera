package com.yes.camera.domain.usecase

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import com.yes.camera.data.repository.CameraRepository
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher

class SingleCaptureUseCase(
    dispatcher: CoroutineDispatcher,
    private val cameraRepository: CameraRepository
) : UseCase<SingleCaptureUseCase.Params, Bitmap?>(dispatcher) {

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun run(params: Params): Bitmap? {
        return cameraRepository.singleCapture()
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun run(): Bitmap? {
        return run(Params())
    }

    data class Params(val enable: Boolean = true)
}
