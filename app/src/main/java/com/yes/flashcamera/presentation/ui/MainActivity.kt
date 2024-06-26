package com.yes.flashcamera.presentation.ui


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.Point
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
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Range
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.WindowInsets
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.yes.flashcamera.databinding.MainBinding
import com.yes.flashcamera.presentation.ui.MainActivity.CameraUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.asExecutor
import java.nio.ByteBuffer


private const val PERMISSIONS_REQUEST_CODE = 10

class MainActivity : Activity() {

    private lateinit var binding: MainBinding

    private val mBackgroundThread: HandlerThread = HandlerThread("CameraThread").apply { start() }
    private val mBackgroundHandler: Handler = Handler(mBackgroundThread.looper)
    private lateinit var cameraService: CameraService

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraService = CameraService(
            getSystemService(CAMERA_SERVICE) as CameraManager,
            mBackgroundHandler
        ) { state ->
            render(state)
        }
        binding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpView()

        checkPermission {

        }
    }

    data class CameraUI(
        val iso: Range<Int>? = null,
        val exposure: Range<Long>? = null
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun render(state: CameraUI) {
        state.iso?.let {
            binding.iso.min = it.lower
            binding.iso.max = it.upper
        }
        state.exposure?.let {
            binding.exposure.min = it.lower.toInt()
            binding.exposure.max = it.upper.toInt()
        }
    }

    private val listenerButtonCamera1 = View.OnClickListener {

    }
    private val listenerIsoSeekBar = object : SeekBar.OnSeekBarChangeListener {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                cameraService.setIso(progress)
            }

        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }

    }
    private val listenerExposureSeekBar = object : SeekBar.OnSeekBarChangeListener {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                cameraService.setExposure(progress)
            }

        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }

    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun openCamera(surfaceTexture: SurfaceTexture) {


        cameraService.openCamera(surfaceTexture)
    }

    private fun getDisplaySize(): Pair<Int, Int> {

        val width: Int
        val height: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val windowInsets: WindowInsets = windowMetrics.windowInsets

            val insets = windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.navigationBars() or
                        WindowInsets.Type.displayCutout()
            )
            val insetsWidth = insets.right + insets.left
            val insetsHeight = insets.top + insets.bottom

            val b = windowMetrics.bounds
            width = b.width() - insetsWidth
            height = b.height() - insetsHeight
        } else {
            val size = Point()
            val display = windowManager.defaultDisplay // deprecated in API 30
            display?.getSize(size) // deprecated in API 30
            width = size.x
            height = size.y
        }
        return Pair(width, height)
    }

    fun getPreviewOutputSize(
        characteristics: CameraCharacteristics,
    ): Size {
        val SIZE_1080P = Pair(1920, 1080)

        val screenSize = getDisplaySize()
        val hdScreen =
            screenSize.first >= SIZE_1080P.first || screenSize.second >= SIZE_1080P.second
        val maxSize = if (hdScreen) SIZE_1080P else screenSize

        val config = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
        val allSizes = config.getOutputSizes(SurfaceTexture::class.java)
        val validSizes = allSizes
            .sortedWith(compareBy { it.height * it.width })
            .reversed()
        return validSizes.first { it.height <= maxSize.first && it.width <= maxSize.second }
    }

    private val textureListener = object : TextureView.SurfaceTextureListener {
        @RequiresApi(Build.VERSION_CODES.S)
        override fun onSurfaceTextureAvailable(
            surfaceTexture: SurfaceTexture,
            width: Int,
            height: Int
        ) {
            surfaceTexture.setDefaultBufferSize(width, height)
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
        binding.iso.setOnSeekBarChangeListener(listenerIsoSeekBar)
        binding.exposure.setOnSeekBarChangeListener(listenerExposureSeekBar)

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
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
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
    private val mBackgroundHandler: Handler,
    private val onGetState: (state: CameraUI) -> Unit,
) {
    private var iso: Int? = null
    private var exposure: Long? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun setIso(value: Int) {
        iso = value
        createCaptureSession()

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun setExposure(value: Int) {
        exposure = value.toLong()
        createCaptureSession()

    }

    var cameraDevice: CameraDevice? = null
    private fun setCamera(camera: CameraDevice) {
        this.cameraDevice = camera
    }

    private val mCameraCallback: CameraDevice.StateCallback =
        object : CameraDevice.StateCallback() {

            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onOpened(camera: CameraDevice) {
                // configureSession(camera, listOf(surface!!))
                setCamera(camera)
                createCaptureSession()
                //    createCameraPreviewSession(camera)
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {
            }
        }
    private var surface: Surface? = null
    private var surfaceTexture: SurfaceTexture? = null

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    fun openCamera(surfaceTexture: SurfaceTexture) {
        this.surfaceTexture = surfaceTexture
        val surface = Surface(surfaceTexture)
        this.surface = surface
        val myCameras = mCameraManager.cameraIdList

        val characteristics = mutableListOf<CameraCharacteristics>()
        for (cameraId in myCameras) {
            characteristics.add(
                mCameraManager.getCameraCharacteristics(cameraId)
            )
        }

        ///////////////////////


        //////////////////////
        val zoom = characteristics[0].get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)
        val res = characteristics[0].get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

        val isoRange = characteristics[0].get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
        val analogIsoRange =
            characteristics[0].get(CameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY)
        val rawIsoBoostRange =
            characteristics[0].get(CameraCharacteristics.CONTROL_POST_RAW_SENSITIVITY_BOOST_RANGE)

        val exposure = characteristics[0].get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
        val aperture = characteristics[0].get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
        val focalLength =
            characteristics[0].get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
        val minFocus =
            characteristics[0].get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)
        val hyperFocalFocus =
            characteristics[0].get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE)
        onGetState(
            CameraUI(
                iso = isoRange,
                exposure = exposure
            )
        )
        ///////////////
        val SENSOR_INFO_ACTIVE_ARRAY_SIZE =
            characteristics[0].get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
        // val SENSOR_INFO_ACTIVE_ARRAY_SIZE_MAXIMUM_RESOLUTION=characteristics[0].get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE_MAXIMUM_RESOLUTION)//not supported


        val SENSOR_INFO_PIXEL_ARRAY_SIZE =
            characteristics[0].get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
        // val SENSOR_INFO_PIXEL_ARRAY_SIZE_MAXIMUM_RESOLUTION=characteristics[0].get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE_MAXIMUM_RESOLUTION)//not supported
        val SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE =
            characteristics[0].get(CameraCharacteristics.SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE)
        //  val SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE_MAXIMUM_RESOLUTION=characteristics[0].get(CameraCharacteristics.SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE_MAXIMUM_RESOLUTION)//not supported


//////////////////////////
        val supportFull = isHardwareLevelSupported(
            characteristics[0],
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL
        )
        val support3 = isHardwareLevelSupported(
            characteristics[0],
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3
        )
        val supportLimited = isHardwareLevelSupported(
            characteristics[0],
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
        )
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


    private var mImageReader: ImageReader? = null
    private val mOnImageAvailableListener =
        ImageReader.OnImageAvailableListener { reader ->
            mBackgroundHandler.post(
                Runnable {
                    val image = reader.acquireNextImage()
                    if (image != null) {
                        /* val width = image.width;
                         val height = image.height
                         val planes = image.planes
                         val buffer = planes[0].buffer
                         val pixelStride = planes[0].pixelStride
                         val rowStride = planes[0].rowStride
                      //   val rowPadding = rowStride - pixelStride * width

                         val bitmap = Bitmap.createBitmap(
                             1920 ,
                             1080,
                             Bitmap.Config.ARGB_8888
                         )
                         bitmap.copyPixelsFromBuffer(buffer)
                         val canvas=reader.surface.lockCanvas(null)
                         canvas.drawBitmap(bitmap,0f,0f,null)
                         reader.surface.unlockCanvasAndPost(canvas)*/

                    }
                    image.close()

                }
            )
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createCaptureSession() {
        mImageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 1)
        mImageReader!!.setOnImageAvailableListener(mOnImageAvailableListener, null)
        val surface2 = mImageReader?.surface
        ////////////////////////////////
        val captureBuilder =
            //   device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL)
     //   captureBuilder.addTarget(surface2!!)
        captureBuilder.addTarget(surface!!)
        captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF)
        iso?.let {
            captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, it)
        }
        exposure?.let {
            captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, it)
        }
     //   captureBuilder.set(CaptureRequest.CONTROL_ZOOM_RATIO, 10F)

        val configs = mutableListOf<OutputConfiguration>()
        val streamUseCase = CameraMetadata
            .SCALER_AVAILABLE_STREAM_USE_CASES_PREVIEW_VIDEO_STILL

        /* targets.forEach {
             val config = OutputConfiguration(it)
             //   config.streamUseCase = streamUseCase.toLong()
             configs.add(config)
         }*/
        val conf1 = OutputConfiguration(surface!!)
        configs.add(conf1)
        /*  val conf2 = OutputConfiguration(surface2)
          configs.add(conf2)*/

        val config = SessionConfiguration(
            SessionConfiguration.SESSION_REGULAR,
            configs,
            Dispatchers.IO.asExecutor(),
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
            }

        )

        cameraDevice!!.createCaptureSession(config)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun configureSession(device: CameraDevice, targets: List<Surface>) {

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

        val config = SessionConfiguration(
            SessionConfiguration.SESSION_REGULAR,
            configs,
            Dispatchers.IO.asExecutor(),
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