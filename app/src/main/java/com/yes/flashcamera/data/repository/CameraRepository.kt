package com.yes.flashcamera.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Build
import android.os.Handler
import android.util.Size
import android.view.Surface
import androidx.annotation.RequiresApi
import com.yes.flashcamera.domain.model.Camera
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.util.Arrays
import java.util.Collections


class CameraRepository(
    private val context: Context,
    private val cameraManager: CameraManager,
    private val mBackgroundHandler: Handler,
) {
    private fun openCamera(facing: Int):String?{
        cameraManager.cameraIdList.forEach {
            val characteristics = cameraManager.getCameraCharacteristics(it)
            if (characteristics.get(CameraCharacteristics.LENS_FACING) == facing) {
                return it
            }
        }
        return null
    }
    private var cameraDevice: CameraDevice? = null
    private val mCameraCallback: CameraDevice.StateCallback =
        object : CameraDevice.StateCallback() {

            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onOpened(camera: CameraDevice) {
                setCamera(camera)
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {}
        }
    private fun setCamera(camera: CameraDevice) {
        this.cameraDevice = camera
    }
    @SuppressLint("MissingPermission")
    fun getFrontCamera(): Camera? {
        openCamera(CameraCharacteristics.LENS_FACING_FRONT)?.let {
            cameraManager.openCamera(
                it,
                mCameraCallback,
                mBackgroundHandler
            )

            val characteristics =cameraManager.getCameraCharacteristics(it)
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.let {map->
                Arrays.sort(
                    map.getOutputSizes(ImageFormat.JPEG),
                    Collections.reverseOrder { lhs, rhs -> // Cast to ensure the multiplications won't overflow
                        java.lang.Long.signum((lhs.width.toLong() * lhs.height.toLong()) - (rhs.width.toLong() * rhs.height.toLong() ))
                    })
            }


            return Camera(
                iso=characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE),
                exposure = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
            )
        }
        return null
    }
    var previewCaptureBuilder: CaptureRequest.Builder? = null
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createCaptureSession(glSurfaceTexture: SurfaceTexture) {


        val configs = mutableListOf<OutputConfiguration>()
        //  glSurfaceTexture?.setDefaultBufferSize(4096, 3072)//3072x4096
        glSurfaceTexture.setDefaultBufferSize(176, 144)//(3072x4096)//(1280, 720)//(1920,1080)
        val surface = Surface(glSurfaceTexture)

        /////////////////////////////////preview
        previewCaptureBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        previewCaptureBuilder?.addTarget(surface)
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
        val conf = OutputConfiguration(surface)
        configs.add(conf)
        /*  val conf2 = OutputConfiguration(surface2)
          configs.add(conf2)*/

        val config = SessionConfiguration(
            SessionConfiguration.SESSION_REGULAR,
            configs,
            Dispatchers.IO.asExecutor(),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                        // session.stopRepeating()
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