package com.yes.flashcamera.presentation.ui


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.Rect
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
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Range
import android.util.Size
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.WindowInsets
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import com.yes.flashcamera.databinding.MainBinding
import com.yes.flashcamera.presentation.ui.MainActivity.CameraUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.asExecutor
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread


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
            this,
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
    private fun openCamera() {
        surfaceTexture?.let { st ->
            surfaceTexture2?.let {
                cameraService.openCamera(st, it)
            }
        }
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

    private var surfaceTexture: SurfaceTexture? = null
    private var surfaceTexture2: SurfaceTexture? = null
    fun setTexture1(surfaceTexture: SurfaceTexture) {
        this.surfaceTexture = surfaceTexture
    }

    fun setTexture2(surfaceTexture: SurfaceTexture) {
        this.surfaceTexture2 = surfaceTexture
    }

    private val textureListener = object : TextureView.SurfaceTextureListener {
        @RequiresApi(Build.VERSION_CODES.S)
        override fun onSurfaceTextureAvailable(
            surfaceTexture: SurfaceTexture,
            width: Int,
            height: Int
        ) {
            surfaceTexture.setDefaultBufferSize(width, height)
            setTexture1(surfaceTexture)
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

        }

    }
    private val texture2Listener = object : TextureView.SurfaceTextureListener {
        @RequiresApi(Build.VERSION_CODES.S)
        override fun onSurfaceTextureAvailable(
            surfaceTexture: SurfaceTexture,
            width: Int,
            height: Int
        ) {
            surfaceTexture.setDefaultBufferSize(width, height)
            setTexture2(surfaceTexture)
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

        }

    }
    private var dX: Float = 0f
    private var dY: Float = 0f

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setUpView() {
        binding.textureView.surfaceTextureListener = textureListener
        binding.textureView2.surfaceTextureListener = texture2Listener
      //  binding.camera1.setOnClickListener(listenerButtonCamera1)
        binding.iso.setOnSeekBarChangeListener(listenerIsoSeekBar)
        binding.exposure.setOnSeekBarChangeListener(listenerExposureSeekBar)
        var xP=0
        var yP=0
        binding.textureView2.setOnTouchListener { view, event ->

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX + dX
                    val newY = event.rawY + dY

                    // Ограничиваем перемещение внутри границ FrameLayout
                    val maxX = binding.frameLayout.width - view.width
                    val maxY = binding.frameLayout.height - view.height

                    val clampedX = newX.coerceIn(0f, maxX.toFloat())
                    val clampedY = newY.coerceIn(0f, maxY.toFloat())

                    view.animate()
                        .x(clampedX)
                        .y(clampedY)
                        .setDuration(0)
                        .start()
                    xP = ((newX * 100) / maxX).toInt()
                    yP = ((newY * 100) / maxY).toInt()

                }
                MotionEvent.ACTION_UP->{
                    cameraService.setMagnifierPosition(xP, yP)
                }

                else -> return@setOnTouchListener false
            }
            true
        }

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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
    private val context: Context,
    private val mCameraManager: CameraManager,
    private val mBackgroundHandler: Handler,
    private val onGetState: (state: CameraUI) -> Unit,
) {
    private var iso: Int? = null//290
    private var exposure: Long? = null// 138181824

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

    private var cameraDevice: CameraDevice? = null
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
    private var surfaceTexture2: SurfaceTexture? = null

    data class DualCamera(val logicalId: String, val physicalId1: String, val physicalId2: String)

    @RequiresApi(Build.VERSION_CODES.P)
    fun findDualCameras(manager: CameraManager, facing: Int? = null): Array<DualCamera> {
        val dualCameras = ArrayList<DualCamera>()

        // Iterate over all the available camera characteristics
        manager.cameraIdList.map {
            Pair(manager.getCameraCharacteristics(it), it)
        }.filter {
            // Filter by cameras facing the requested direction
            facing == null || it.first.get(CameraCharacteristics.LENS_FACING) == facing
        }.filter {
            // Filter by logical cameras
            it.first.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)!!.contains(
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA
            )
        }.forEach {
            // All possible pairs from the list of physical cameras are valid results
            // NOTE: There could be N physical cameras as part of a logical camera grouping
            val physicalCameras = it.first.physicalCameraIds.toTypedArray()
            for (idx1 in 0 until physicalCameras.size) {
                for (idx2 in (idx1 + 1) until physicalCameras.size) {
                    dualCameras.add(
                        DualCamera(
                            it.second, physicalCameras[idx1], physicalCameras[idx2]
                        )
                    )
                }
            }
        }

        return dualCameras.toTypedArray()
    }


    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    fun openCamera(surfaceTexture: SurfaceTexture, surfaceTexture2: SurfaceTexture) {
        this.surfaceTexture = surfaceTexture
        this.surfaceTexture2 = surfaceTexture2

        val myCameras = mCameraManager.cameraIdList

        val characteristics = mutableListOf<CameraCharacteristics>()
        for (cameraId in myCameras) {
            characteristics.add(
                mCameraManager.getCameraCharacteristics(cameraId)
            )
        }
        val phys = mCameraManager.getCameraCharacteristics("0").physicalCameraIds
        val phys2 = mCameraManager.getCameraCharacteristics("1").physicalCameraIds
        ///////////////////////
        findDualCameras(mCameraManager, CameraCharacteristics.LENS_FACING_FRONT)

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
        val capabilities = characteristics[0].get(
            CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES
        )!!
        val outputFormats = characteristics[0].get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )!!.outputFormats
        if (capabilities.contains(
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT
            )
        ) {

            val t = true
        }
        mCameraManager.openCamera(
            myCameras[0],
            mCameraCallback,
            mBackgroundHandler
        )
    }

    private fun isHardwareLevelSupported(c: CameraCharacteristics, requiredLevel: Int): Boolean {
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

        for (sortedLevel in sortedHwLevels) {
            if (sortedLevel == requiredLevel) {
                return true
            } else if (sortedLevel == deviceLevel) {
                return false
            }
        }
        return false // Should never reach here
    }


    private var mImageReader: ImageReader? = null
    private fun getContext(): Context {
        return context
    }

    var averagImageValue: Long = 0
    private val mOnImageAvailableListener =
        ImageReader.OnImageAvailableListener { reader ->
            mBackgroundHandler.post(
                Runnable {
                    val image = reader.acquireNextImage()
                    if (image != null) {
                        /////////////////////////
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.remaining()).apply { buffer.get(this) }


                        val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
                        val output = File(
                            Environment.getExternalStorageDirectory().toString(),
                            "/DCIM/IMG_${sdf.format(Date())}.jpg"
                        )
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        //  FileOutputStream(output).use { it.write(bytes) }
                        val imageComparator = ImageComparator()
                        val imageValue = imageComparator.getImageValue(bitmap)
                        if (averagImageValue != 0L) {
                            val dif = imageComparator.compareValues(averagImageValue, imageValue)
                            if (dif > 20) {//28
                                println("got  it!")
                            }
                            averagImageValue = (averagImageValue + imageValue) / 2
                        } else {
                            averagImageValue = imageValue
                        }
                        //////////////////////////
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
    val zoomRatio = 0F
    var positionX = 0
    var positionY = 0
    val surfaceWidth = 1920
    val surfaceHeight = 1080
    val magnifierWidth = surfaceWidth / zoomRatio
    val magnifierHeight = surfaceHeight / zoomRatio

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun setMagnifierPosition(positionX: Int, positionY: Int) {
      //  Thread.sleep(100)
        this.positionX = (((positionX * surfaceWidth) / 100) ).toInt()
        this.positionY = (((positionY * surfaceHeight) / 100) ).toInt()
        updateSession()

    }
    var magnifierCaptureBuilder:CaptureRequest.Builder?=null
    @RequiresApi(Build.VERSION_CODES.R)
    fun updateSession(){
        val surface2 = Surface(surfaceTexture2)
        magnifierCaptureBuilder=

            //   device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL)
        // cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)

        magnifierCaptureBuilder?.addTarget(surface2)
        magnifierCaptureBuilder?.set(CaptureRequest.CONTROL_ZOOM_RATIO, 4f)
        magnifierCaptureBuilder?.set(
            CaptureRequest.SCALER_CROP_REGION,
            Rect(positionX, positionY, magnifierWidth.toInt(), magnifierHeight.toInt())
        )
      /*  magnifierCaptureBuilder?.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF)
        magnifierCaptureBuilder?.set(
            CaptureRequest.CONTROL_AF_MODE,
            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
        )*/

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createCaptureSession() {


        val configs = mutableListOf<OutputConfiguration>()
        val surface = Surface(surfaceTexture)
        val surface2 = Surface(surfaceTexture2)
        //this.surface = surface
        /////////////////////////////////
        mImageReader = ImageReader.newInstance(surfaceWidth, surfaceHeight, ImageFormat.JPEG, 1)
        mImageReader!!.setOnImageAvailableListener(mOnImageAvailableListener, null)
        val surface3 = mImageReader?.surface
        val conf3 = OutputConfiguration(surface3!!)
        configs.add(conf3)
        ////////////////////////////////magnifier
        magnifierCaptureBuilder =
            //   device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL)
        // cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)

        magnifierCaptureBuilder?.addTarget(surface2)
        magnifierCaptureBuilder?.set(CaptureRequest.CONTROL_ZOOM_RATIO, zoomRatio)
        magnifierCaptureBuilder?.set(
            CaptureRequest.SCALER_CROP_REGION,
            Rect(positionX, positionY, magnifierWidth.toInt(), magnifierHeight.toInt())
        )
        magnifierCaptureBuilder?.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF)
        magnifierCaptureBuilder?.set(
            CaptureRequest.CONTROL_AF_MODE,
            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
        )
        val conf2 = OutputConfiguration(surface2)
        configs.add(conf2)
        /////////////////////////////////preview
        val captureBuilder =
            //   device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL)
        //   captureBuilder.addTarget(surface2!!)
        captureBuilder.addTarget(surface)
        captureBuilder.addTarget(surface3)
        captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF)
        captureBuilder.set(
            CaptureRequest.CONTROL_AF_MODE,
            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
        )
        iso?.let {
            captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, it)
            magnifierCaptureBuilder?.set(CaptureRequest.SENSOR_SENSITIVITY, it)
        }
        exposure?.let {
            captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, it)
            magnifierCaptureBuilder?.set(CaptureRequest.SENSOR_EXPOSURE_TIME, it)
        }
        //   captureBuilder.set(CaptureRequest.CONTROL_ZOOM_RATIO, 10F)


        val streamUseCase = CameraMetadata
            .SCALER_AVAILABLE_STREAM_USE_CASES_PREVIEW_VIDEO_STILL

        /* targets.forEach {
             val config = OutputConfiguration(it)
             //   config.streamUseCase = streamUseCase.toLong()
             configs.add(config)
         }*/
        val conf1 = OutputConfiguration(surface)
        configs.add(conf1)
        /*  val conf2 = OutputConfiguration(surface2)
          configs.add(conf2)*/
        val cameraCaptureSessionCaptureCallback = object : CameraCaptureSession.CaptureCallback() {

            override fun onCaptureStarted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                timestamp: Long,
                frameNumber: Long
            ) {
                super.onCaptureStarted(session, request, timestamp, frameNumber)

            }

            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                Thread.sleep(60)
                magnifierCaptureBuilder?.let {
                    session.capture(it.build(), this, mBackgroundHandler)
                }


            }
        }
        val config = SessionConfiguration(
            SessionConfiguration.SESSION_REGULAR,
            configs,
            Dispatchers.IO.asExecutor(),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                       // session.stopRepeating()
                        session.setRepeatingRequest(
                            captureBuilder.build(),
                            cameraCaptureSessionCaptureCallback,
                            mBackgroundHandler
                        )
                      /*  while (true){
                            Thread.sleep(100)
                            session.capture(captureBuilder2.build(), null, mBackgroundHandler)
                        }*/




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