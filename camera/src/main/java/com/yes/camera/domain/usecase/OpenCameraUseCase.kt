package com.yes.camera.domain.usecase

import android.graphics.SurfaceTexture
import android.os.Build
import android.view.TextureView
import androidx.annotation.RequiresApi
import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.data.repository.SettingsRepository
import com.yes.camera.domain.model.Characteristics
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

class OpenCameraUseCase(
    dispatcher: CoroutineDispatcher,
    private val cameraRepository: CameraRepository,
    private val settingsRepository: SettingsRepository
) : UseCase<OpenCameraUseCase.Params, Unit>(dispatcher) {
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun run(params: Params){
        val settingsCharacteristics=settingsRepository.getCharacteristics()

        val cameraCharacteristics=cameraRepository.openCamera(

            settingsCharacteristics.backCamera?:true
        )
     //   val tmp=cameraCharacteristics.filterNotNull().first().resolutionItems
        //delay(1000)

        val cameraCharacteristicsValue=cameraCharacteristics.filterNotNull().first()
        /////
      /*  settingsRepository.setResolutions(cameraCharacteristicsValue.resolutionItems)
        settingsRepository.setResolutionValue(
            cameraCharacteristicsValue.resolutionItems.maxByOrNull { it.width*it.height }
        )*/
        ////
        settingsCharacteristics.backCamera?.let {
            cameraRepository.startVideoSession(
                params.glSurfaceTexture,
                settingsCharacteristics
            )
        }?:run{
            settingsRepository.setBackCamera(true)
        //    val cameraCharacteristicsValue=cameraCharacteristics.filterNotNull().first()
           /* val tmp=cameraCharacteristics.filterNotNull().first().resolutionItems
            val t=tmp*/
            settingsRepository.setResolutions(cameraCharacteristicsValue.resolutionItems)

            settingsRepository.setResolutionValue(
                cameraCharacteristicsValue.resolutionItems.maxByOrNull { it.width*it.height }
            )
            val tmp=settingsRepository.subscribeResolutionValue().first()
            val t=tmp
            cameraRepository.startVideoSession(
                params.glSurfaceTexture,
                settingsRepository.getCharacteristics()
            )
        }
    }

    data class Params(
        val backCamera: Boolean,
        val glSurfaceTexture: SurfaceTexture
    )
}