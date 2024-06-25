package com.yes.flashcamera.presentation.ui


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
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
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Display
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.yes.flashcamera.databinding.MainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor


private const val PERMISSIONS_REQUEST_CODE = 10
class MainActivity : Activity() {

    private lateinit var binding: MainBinding

    private val mBackgroundThread: HandlerThread= HandlerThread("CameraThread").apply { start() }
    private val mBackgroundHandler: Handler=Handler(mBackgroundThread.looper)
    private lateinit var cameraService: CameraService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraService= CameraService(
            getSystemService(CAMERA_SERVICE) as CameraManager,
            mBackgroundHandler
        )
        binding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpView()

        checkPermission {

        }
    }


    private val listenerButtonCamera1 = View.OnClickListener {

    }
    @RequiresApi(Build.VERSION_CODES.S)
    private fun openCamera(surfaceTexture: SurfaceTexture){
        surfaceTexture.setDefaultBufferSize(32, 24)
        val surface = Surface(surfaceTexture)
        cameraService.openCamera(surface)
    }
   /* fun getPreviewOutputSize(
        display: Display,
        characteristics: CameraCharacteristics,
    ): Size {

    }*/

    private val textureListener = object : TextureView.SurfaceTextureListener {
        @RequiresApi(Build.VERSION_CODES.S)
        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {

            openCamera(surfaceTexture)
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

        }

    }

    private fun setUpView() {


        binding.textureView.surfaceTextureListener = textureListener


        binding.camera1.setOnClickListener(listenerButtonCamera1)

    }

    private fun checkPermission(callback: () -> Unit) {
        if (
            checkSelfPermission(
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
            ||
            checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf<String>(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            callback()
        }
    }



    override fun onDestroy() {
        super.onDestroy()

        // Остановка потока
        mBackgroundThread.quitSafely()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpView()
            } else {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }
    }

}

class CameraService(
    private val mCameraManager: CameraManager,

    private val mBackgroundHandler: Handler
) {
    private val mCameraCallback: CameraDevice.StateCallback =
        object : CameraDevice.StateCallback() {

            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onOpened(camera: CameraDevice) {
                configureSession(camera, listOf(surface!!))
            //    createCameraPreviewSession(camera)
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {
            }
        }
    private var surface:Surface?=null
    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    fun openCamera(surface: Surface) {
        this.surface=surface
        val myCameras = mCameraManager.cameraIdList

        val characteristics= mutableListOf<CameraCharacteristics>()
        for (cameraId in myCameras){
            characteristics.add(
                mCameraManager.getCameraCharacteristics(cameraId)
            )
        }

        ///////////////////////



        //////////////////////
        val res=characteristics[0].get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

        val isoRange = characteristics[0].get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
        val analogIsoRange = characteristics[0].get(CameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY)
        val rawIsoBoostRange = characteristics[0].get(CameraCharacteristics.CONTROL_POST_RAW_SENSITIVITY_BOOST_RANGE)

        val exposure=characteristics[0].get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
        val aperture=characteristics[0].get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
        val focalLength=characteristics[0].get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
        val minFocus=characteristics[0].get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)
        val hyperFocalFocus=characteristics[0].get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE)
        ///////////////
        val SENSOR_INFO_ACTIVE_ARRAY_SIZE=characteristics[0].get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
       // val SENSOR_INFO_ACTIVE_ARRAY_SIZE_MAXIMUM_RESOLUTION=characteristics[0].get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE_MAXIMUM_RESOLUTION)//not supported


       val SENSOR_INFO_PIXEL_ARRAY_SIZE=characteristics[0].get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
       // val SENSOR_INFO_PIXEL_ARRAY_SIZE_MAXIMUM_RESOLUTION=characteristics[0].get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE_MAXIMUM_RESOLUTION)//not supported
        val SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE = characteristics[0].get(CameraCharacteristics.SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE)
      //  val SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE_MAXIMUM_RESOLUTION=characteristics[0].get(CameraCharacteristics.SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE_MAXIMUM_RESOLUTION)//not supported


//////////////////////////
        val supportFull=isHardwareLevelSupported(characteristics[0],CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL)
        val support3=isHardwareLevelSupported(characteristics[0],CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3)
        val supportLimited=isHardwareLevelSupported(characteristics[0],CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED)
        ///////////////////////////
      /*  val capabilities = characteristics[0].get(
            CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)!!
        val outputFormats = characteristics[0].get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.outputFormats
        if (capabilities.contains(
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT) &&
            outputFormats.contains(ImageFormat.DEPTH_JPEG)) {

               val t=true
        }*/
        mCameraManager.openCamera(
            myCameras[0],
            mCameraCallback,
            mBackgroundHandler
        )
    }
    fun isHardwareLevelSupported(c: CameraCharacteristics, requiredLevel: Int): Boolean {
        val sortedHwLevels = intArrayOf(
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY,
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL,
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED,
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL,
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3
        )
        val deviceLevel = c.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
        if (requiredLevel == deviceLevel) {
            return true
        }

        for (sortedlevel in sortedHwLevels) {
            if (sortedlevel == requiredLevel) {
                return true
            } else if (sortedlevel == deviceLevel) {
                return false
            }
        }
        return false // Should never reach here
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun configureSession(device: CameraDevice, targets: List<Surface>){
        val captureBuilder =
            device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
           // device.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL)
       captureBuilder.addTarget(surface!!)
    //    captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF)
    //    captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, 1600)
        val configs = mutableListOf<OutputConfiguration>()
        val streamUseCase = CameraMetadata
            .SCALER_AVAILABLE_STREAM_USE_CASES_PREVIEW_VIDEO_STILL

        targets.forEach {
            val config = OutputConfiguration(it)
         //   config.streamUseCase = streamUseCase.toLong()
            configs.add(config)
        }

        val config=SessionConfiguration(
            SessionConfiguration.SESSION_REGULAR,
            configs,
            Dispatchers.IO.asExecutor(),
            object :CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                        session.setRepeatingRequest(
                            captureBuilder.build(),
                            null,
                            mBackgroundHandler
                        )
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            }

        )

        device.createCaptureSession(config)
    }
    @RequiresApi(Build.VERSION_CODES.P)
    private fun createCameraPreviewSession(mCameraDevice: CameraDevice) {

        val captureBuilder =
            mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureBuilder.addTarget(surface!!)
        val targets = listOf(surface)
        mCameraDevice.createCaptureSession(
            targets,
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                        session.setRepeatingRequest(
                            captureBuilder.build(),
                            null,
                            mBackgroundHandler
                        )
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            },
            mBackgroundHandler
        )
    }

}