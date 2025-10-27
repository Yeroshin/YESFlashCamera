package com.yes.camera.domain.usecase

import android.graphics.SurfaceTexture
import android.os.Build
import androidx.annotation.RequiresApi
import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.data.repository.SettingsRepository
import com.yes.camera.domain.model.Characteristics
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

class OpenCameraUseCase(
    dispatcher: CoroutineDispatcher,
    private val cameraRepository: CameraRepository,
    private val settingsRepository: SettingsRepository
) : UseCase<OpenCameraUseCase.Params, Flow<Characteristics>>(dispatcher) {
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun run(params: Params):Flow< Characteristics> {
        val settingsCharacteristics=settingsRepository.getCharacteristics()
        val firstLaunch:Boolean= settingsCharacteristics.backCamera==null
        val cameraCharacteristics=cameraRepository.openCamera(
            params.glSurfaceTexture,
            settingsCharacteristics.backCamera?:true
        )
        val cameraCharacteristicsValue=cameraCharacteristics.filterNotNull().first()
        if (firstLaunch){
            settingsRepository.setBackCamera(true)
            val s=settingsRepository.getBackCamera()
            val k=s
            settingsRepository.setResolutions(cameraCharacteristicsValue.resolutionItems)
            settingsRepository.setResolutionValue(cameraCharacteristicsValue.resolutionItems.maxBy { it.width })
            val e = settingsRepository.getResolutions()
            val y=e
            val o = settingsRepository.getResolutionValue()
            val d=o
        }
        cameraRepository.startVideoSession(settingsCharacteristics)
        return cameraCharacteristics.filterNotNull()
      /*  return cameraRepository.openCamera(
            params.glSurfaceTexture,
            settingsRepository.getCharacteristics()
        ).filterNotNull()*/
    }

    data class Params(
        val backCamera: Boolean,
        val glSurfaceTexture: SurfaceTexture
    )
}