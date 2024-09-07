package com.yes.camera.data.repository

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.view.Surface
import androidx.annotation.RequiresApi
import com.yes.camera.domain.model.Dimensions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class CameraRepository(
    private val cameraManager: CameraManager,
    private val mBackgroundHandler: Handler,

    ) {

    private var cameraDevice: CameraDevice? = null
    private fun getCameraByFacing(facing: Int): String? {
        cameraManager.cameraIdList.forEach {
            val characteristics = cameraManager.getCameraCharacteristics(it)
            if (characteristics.get(CameraCharacteristics.LENS_FACING) == facing) {
                return it
            }
        }
        return null
    }

    private val _CharacteristicsFlow:MutableStateFlow<com.yes.camera.domain.model.Characteristics?> = MutableStateFlow(null)
    private val characteristicsFlow: StateFlow<com.yes.camera.domain.model.Characteristics?> = _CharacteristicsFlow
    fun openBackCamera(glSurfaceTexture: SurfaceTexture): StateFlow<com.yes.camera.domain.model.Characteristics?> {
        this.glSurfaceTexture=glSurfaceTexture
        getCameraByFacing(CameraCharacteristics.LENS_FACING_BACK)?.let {
            openCamera(
                it
            ) {camera->
                _CharacteristicsFlow.value=camera
            }
        }
        return characteristicsFlow
    }

    fun openFrontCamera(glSurfaceTexture: SurfaceTexture): StateFlow<com.yes.camera.domain.model.Characteristics?>  {
        this.glSurfaceTexture=glSurfaceTexture
        getCameraByFacing(CameraCharacteristics.LENS_FACING_FRONT)?.let {
            openCamera(
                it
            ) {camera->
                _CharacteristicsFlow.value=camera
            }
        }
        return characteristicsFlow
    }

    // private var onCameraOpened:(()->Unit)?=null
    @SuppressLint("MissingPermission")
    private fun openCamera(id: String, onCameraOpened: (characteristics: com.yes.camera.domain.model.Characteristics) -> Unit) {
        // this.onCameraOpened = onCameraOpened
        cameraManager.openCamera(
            id,
            object : CameraDevice.StateCallback() {

                @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                  //  previewCaptureBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    setCharacteristics(51200)
                   createCaptureSession()
                    onCameraOpened(
                        getCameraCharacteristics(camera.id)
                    )
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    println()
                }
            },
            mBackgroundHandler
        )
    }

    private fun getCameraCharacteristics(id: String): com.yes.camera.domain.model.Characteristics {
        val characteristics = cameraManager.getCameraCharacteristics(id)

        val config = characteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )

        // If image format is provided, use it to determine supported sizes; or else use target class
        val allSizes = config?.getOutputSizes(ImageReader::class.java)
        allSizes?.maxBy { it.height * it.width }
        val iso=characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
        val exposure=characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
        /*   characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.let {map->
               Arrays.sort(
                   map.getOutputSizes(ImageFormat.JPEG),
                   Collections.reverseOrder { lhs, rhs -> // Cast to ensure the multiplications won't overflow
                       java.lang.Long.signum((lhs.width.toLong() * lhs.height.toLong()) - (rhs.width.toLong() * rhs.height.toLong() ))
                   })
           }*/

        return com.yes.camera.domain.model.Characteristics(
            isoValue = 0,
            isoRange = iso?.let{IntRange(it.lower,it.upper)}?: IntRange(0,0),
            shutterValue = 0,
            shutterRange = exposure?.let{LongRange(it.lower,it.upper)}?:LongRange(0,0),
            resolutions = allSizes?.map {
                Dimensions(
                    it.width,it.height
                )
            }?:listOf(
                Dimensions(0,0)
            )
        )
    }

    private var previewCaptureBuilder: CaptureRequest.Builder? = null
    private var glSurfaceTexture: SurfaceTexture?=null

    fun setCharacteristics(iso:Int){
      //  previewCaptureBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL)
        previewCaptureBuilder =cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL)

        previewCaptureBuilder?.set(CaptureRequest.SENSOR_SENSITIVITY, iso)
       // cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler)
        createCaptureSession()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun createCaptureSession() {



        //  glSurfaceTexture?.setDefaultBufferSize(4096, 3072)//3072x4096
       // glSurfaceTexture.setDefaultBufferSize(1280, 720)//(3072x4096)//(1280, 720)//(1920,1080)

        /////////////////////////////////preview
       // previewCaptureBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

        /* previewCaptureBuilder?.set(
             CaptureRequest.CONTROL_AE_MODE,
             CameraMetadata.CONTROL_AE_MODE_OFF
         )
         previewCaptureBuilder?.set(
             CaptureRequest.CONTROL_AF_MODE,
             CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
         */
        //   captureBuilder.set(CaptureRequest.CONTROL_ZOOM_RATIO, 10F)
        /*     val streamUseCase = CameraMetadata
                 .SCALER_AVAILABLE_STREAM_USE_CASES_PREVIEW_VIDEO_STILL
     */
        /* targets.forEach {
             val config = OutputConfiguration(it)
             //   config.streamUseCase = streamUseCase.toLong()
             configs.add(config)
         }*/
        val surface = Surface(glSurfaceTexture)
        previewCaptureBuilder?.addTarget(surface)
        val configs = mutableListOf<OutputConfiguration>()
        val conf = OutputConfiguration(surface)
        configs.add(conf)
        val config = SessionConfiguration(
            SessionConfiguration.SESSION_REGULAR,
            configs,
            Dispatchers.IO.asExecutor(),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                         session.stopRepeating()
                        previewCaptureBuilder?.let {
                            session.setRepeatingRequest(
                                it.build(),
                                null,// cameraCaptureSessionCaptureCallback,
                                mBackgroundHandler
                            )
                        }
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            }

        )

        cameraDevice?.createCaptureSession(config)
    }
}