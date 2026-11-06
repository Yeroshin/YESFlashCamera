package com.yes.camera.domain.usecase

import android.os.Build
import androidx.annotation.RequiresApi
import com.yes.camera.data.repository.CameraRepository
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher

class ClearCapturedBitmapUseCase(
    dispatcher: CoroutineDispatcher,
    private val cameraRepository: CameraRepository
) : UseCase<Unit, Unit>(dispatcher) {

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun run(params: Unit) {
        cameraRepository.clearCapturedBitmap()
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun run() {
        run(Unit)
    }
}
