package com.yes.flashcamera.presentation.ui


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Range
import android.util.Size
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.yes.flashcamera.data.repository.CameraRepository
import com.yes.flashcamera.databinding.MainBinding
import com.yes.flashcamera.databinding.ParamValueItemBinding
import com.yes.flashcamera.presentation.model.IAdapterDelegate
import com.yes.flashcamera.presentation.model.ParamValueUI
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


private const val PERMISSIONS_REQUEST_CODE = 10

class MainActivity : Activity() {

    private val adapter by lazy {
        ListDelegationAdapter(
            parameterAdapterDelegate(),
        )
    }

    private fun parameterAdapterDelegate() =
        adapterDelegateViewBinding<ParamValueUI, IAdapterDelegate, ParamValueItemBinding>(
            { layoutInflater, root -> ParamValueItemBinding.inflate(layoutInflater, root, false) }
        ) {
            bind {
                binding.value.text = item.value
            }
        }

    private fun dataLoaded() {
        binding.RecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.RecyclerView.adapter = adapter
        binding.RecyclerView.setOnItemScrolledListener { position ->
            println((adapter.items?.get(position) as ParamValueUI).value)
          /*  binding.RecyclerView.smoothScrollToPosition(
                position
            )*/
        }
        adapter.items = listOf(
            ParamValueUI("100"),
            ParamValueUI("200"),
            ParamValueUI("300"),
            ParamValueUI("400"),
            ParamValueUI("500"),
            ParamValueUI("600"),
            ParamValueUI("700"),
            ParamValueUI("800"),
            ParamValueUI("900"),
            ParamValueUI("1000"),
            ParamValueUI("2000"),
            ParamValueUI("2100"),
            ParamValueUI("2200"),
            ParamValueUI("2300"),
            ParamValueUI("2400"),
            ParamValueUI("2500"),
        )
      //  val tmp=binding.RecyclerView.children.last().width/2
        binding.RecyclerView.smoothScrollToPosition(
            -adapter.itemCount/2
        )
        val helper: SnapHelper = LinearSnapHelper()
     //   helper.attachToRecyclerView(binding.RecyclerView)
        /*   binding .RecyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
               override fun onGlobalLayout() {
                   // Убедитесь, что этот код выполняется только один раз
                   binding .RecyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                   // Получите ширину RecyclerView
                   recyclerViewWidth = binding .RecyclerView.width
                   binding.RecyclerView.updatePaddingRelative(start = recyclerViewWidth/2, end = recyclerViewWidth/2)
                   binding.RecyclerView.clipToPadding=false
                   binding.RecyclerView.adapter = adapter
                   adapter.items = listOf(
                       ParamValueUI("100"),
                       ParamValueUI("200"),
                       ParamValueUI("300"),
                       ParamValueUI("400"),
                       ParamValueUI("500"),
                       ParamValueUI("600"),
                       ParamValueUI("700"),
                       ParamValueUI("800"),
                       ParamValueUI("900"),
                       ParamValueUI("1000"),
                       ParamValueUI("2000"),
                       ParamValueUI("2100"),
                       ParamValueUI("2200"),
                       ParamValueUI("2300"),
                       ParamValueUI("2400"),
                       ParamValueUI("2500"),
                   )

               }
           })*/
        /* binding.RecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
             override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {


                 // Проверяем, что элемент оказался в середине
                 val centerPosition = recyclerView.childCount / 2

                 val centerView = recyclerView.getChildAt(centerPosition)// as ParamValueUI
               val tmp=recyclerView.childCount
                 for(i in 0..recyclerView.childCount){
                     val view = recyclerView.layoutManager?.getChildAt(i)
                     view?.let {
                         if((view.x+(view.width/2))>recyclerViewWidth/2&&(view.x-(view.width/2))<recyclerViewWidth/2){
                             for(i in 0..adapter.itemCount){
                                 val tmpView=binding.RecyclerView.findViewHolderForAdapterPosition(i)
                                 if(tmpView?.itemView==view){
                                     println((adapter.items?.get(i) as ParamValueUI).value)
                                 }
                             }

                            /* val item=adapter.items?.get(i) as ParamValueUI
                               println(item.value)*/
                         }
                     }

                 }



                 // Обрабатываем событие для элемента в середине
                 // (например, вызываем нужный метод или отправляем событие)
             }
         })*/


    }

    private fun onItemScrolled() {

    }


    private lateinit var binding: MainBinding

    private val mBackgroundThread: HandlerThread = HandlerThread("CameraThread").apply { start() }
    private val mBackgroundHandler: Handler = Handler(mBackgroundThread.looper)

    // private lateinit var cameraService: CameraService
    private val cameraRepository by lazy {
        CameraRepository(
            getSystemService(CAMERA_SERVICE) as CameraManager,
            mBackgroundHandler
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* cameraService = CameraService(
             this,
             getSystemService(CAMERA_SERVICE) as CameraManager,
             mBackgroundHandler
         ) { state ->
             render(state)
         }*/


        binding = MainBinding.inflate(layoutInflater)

        setUpView()

        checkPermission {

        }
        gles()
        setContentView(binding.root)
        dataLoaded()
    }


    private var rendererSet = false


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val renderer = MyRenderer(
        this
    ) { surfaceTexture ->
        cameraRepository.getBackCameraId()?.let {
            cameraRepository.openCamera(
                it
            ) { camera ->
                val cam = camera
                cameraRepository.createCaptureSession(surfaceTexture)
            }
        }


    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun gles() {

        // glSurfaceView = GLSurfaceView(this)
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        val supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000
        if (supportsEs2) {
            binding.viewFinder.setEGLContextClientVersion(2)
            binding.viewFinder.setRenderer(
                renderer
            )

            /*  binding.glSurfaceView.setEGLContextClientVersion(2)
              binding.glSurfaceView.setRenderer(
                 renderer
              )*/
            rendererSet = true
        } else {
            Toast.makeText(this, "This device does not support OpenGL ES 2.0.", Toast.LENGTH_LONG)
                .show()
            return
        }

        binding.viewFinder.setOnTouchListener { v, event ->
            if (event != null) {
                // Convert touch coordinates into normalized device
                // coordinates, keeping in mind that Android's Y
                // coordinates are inverted.
                val normalizedX =
                    (event.x / v.width.toFloat()) * 2 - 1
                val normalizedY =
                    -((event.y / v.height.toFloat()) * 2 - 1)

                if (event.action == MotionEvent.ACTION_DOWN) {
                    //    glSurfaceView!!.queueEvent {

                    binding.viewFinder.setAspectRatio(3, 2)
                    renderer.handleTouchPress(
                        normalizedX, normalizedY
                    )
                    //   }
                } else if (event.action == MotionEvent.ACTION_MOVE) {
                    //   glSurfaceView!!.queueEvent {
                    renderer.handleTouchDrag(
                        normalizedX, normalizedY
                    )
                    //   }
                }

                true
            } else {
                false
            }
        }
        /* binding.glSurfaceView.setOnTouchListener { v, event ->
             if (event != null) {
                 // Convert touch coordinates into normalized device
                 // coordinates, keeping in mind that Android's Y
                 // coordinates are inverted.
                 val normalizedX =
                     (event.x / v.width.toFloat()) * 2 - 1
                 val normalizedY =
                     -((event.y / v.height.toFloat()) * 2 - 1)

                 if (event.action == MotionEvent.ACTION_DOWN) {
                 //    glSurfaceView!!.queueEvent {
                         renderer.handleTouchPress(
                             normalizedX, normalizedY
                         )
                  //   }
                 } else if (event.action == MotionEvent.ACTION_MOVE) {
                  //   glSurfaceView!!.queueEvent {
                         renderer.handleTouchDrag(
                             normalizedX, normalizedY
                         )
                  //   }
                 }

                 true
             } else {
                 false
             }
         }*/

    }


    object FileUtils {
        fun readTextFromRaw(context: Context, resourceId: Int): String {
            val stringBuilder = StringBuilder()
            try {
                var bufferedReader: BufferedReader? = null
                try {
                    val inputStream = context.resources.openRawResource(resourceId)
                    bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    var line: String?
                    while ((bufferedReader.readLine().also { line = it }) != null) {
                        stringBuilder.append(line)
                        stringBuilder.append("\r\n")
                    }
                } finally {
                    bufferedReader?.close()
                }
            } catch (ioex: IOException) {
                ioex.printStackTrace()
            } catch (nfex: Resources.NotFoundException) {
                nfex.printStackTrace()
            }
            return stringBuilder.toString()
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
                //cameraService.setIso(progress)
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
                //  cameraService.setExposure(progress)
            }

        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

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
    /* private var glTexture: SurfaceTexture? = null
     private fun setGLTexture(glTexture: SurfaceTexture) {
         this.glTexture = glTexture
     }*/

    /*  fun setTexture1(surfaceTexture: SurfaceTexture) {
          this.surfaceTexture = surfaceTexture
      }

      fun setTexture2(surfaceTexture: SurfaceTexture) {
          this.surfaceTexture2 = surfaceTexture
      }*/

    /*  private val textureListener = object : TextureView.SurfaceTextureListener {
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

      }*/


    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setUpView() {
        // binding.textureView.surfaceTextureListener = textureListener

        //  binding.camera1.setOnClickListener(listenerButtonCamera1)
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

/*class CameraService(
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
        //  createCaptureSession()
        previewCaptureBuilder?.set(CaptureRequest.SENSOR_SENSITIVITY, value)
        magnifierCaptureBuilder?.set(CaptureRequest.SENSOR_SENSITIVITY, value)

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun setExposure(value: Int) {
        exposure = value.toLong()
        //  createCaptureSession()
        previewCaptureBuilder?.set(CaptureRequest.SENSOR_EXPOSURE_TIME, value.toLong())
        magnifierCaptureBuilder?.set(CaptureRequest.SENSOR_EXPOSURE_TIME, value.toLong())


    }

    private var cameraDevice: CameraDevice? = null
   /* private fun setCamera(camera: CameraDevice) {
        this.cameraDevice = camera
    }*/

   /* private val mCameraCallback: CameraDevice.StateCallback =
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
        }*/
    private var surface: Surface? = null
    private var glSurfaceTexture: SurfaceTexture? = null

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

  /*  @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    fun openCamera(glSurfaceTexture: SurfaceTexture) {
        this.glSurfaceTexture = glSurfaceTexture

        val myCameras = mCameraManager.cameraIdList

        val characteristics = mutableListOf<CameraCharacteristics>()
        for (cameraId in myCameras) {
            characteristics.add(
                mCameraManager.getCameraCharacteristics(cameraId)
            )
        }
        val res = characteristics[0].get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

        val isoRange = characteristics[0].get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
        val exposure = characteristics[0].get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
        onGetState(
            CameraUI(
                iso = isoRange,
                exposure = exposure
            )
        )

        mCameraManager.openCamera(
            myCameras[0],
            mCameraCallback,
            mBackgroundHandler
        )
    }*/
    /* @RequiresApi(Build.VERSION_CODES.S)
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
         val maxZoom =
             characteristics[0].get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)
         val activeRect = characteristics[0].get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
         val digitalZoom =
             characteristics[0].get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)
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
     }*/

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

    val zoomRatio = 4F
    var positionX = 1
    var positionY = 1
    val surfaceWidth = 1920
    val surfaceHeight = 1080




    var magnifierCaptureBuilder: CaptureRequest.Builder? = null
    var previewCaptureBuilder: CaptureRequest.Builder? = null



    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createCaptureSession() {


        val configs = mutableListOf<OutputConfiguration>()
      //  glSurfaceTexture?.setDefaultBufferSize(4096, 3072)//3072x4096
        glSurfaceTexture?.setDefaultBufferSize(176, 144)//(3072x4096)//(1280, 720)//(1920,1080)
        val surface = Surface(glSurfaceTexture)

        /////////////////////////////////preview
        previewCaptureBuilder =
            cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
           // cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL)
        //   captureBuilder.addTarget(surface2!!)
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


        val streamUseCase = CameraMetadata
            .SCALER_AVAILABLE_STREAM_USE_CASES_PREVIEW_VIDEO_STILL

        /* targets.forEach {
             val config = OutputConfiguration(it)
             //   config.streamUseCase = streamUseCase.toLong()
             configs.add(config)
         }*/
        val conf = OutputConfiguration(surface)
        configs.add(conf)
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
                        previewCaptureBuilder?.let {
                            session.setRepeatingRequest(
                                it.build(),
                                null,// cameraCaptureSessionCaptureCallback,
                                mBackgroundHandler
                            )
                        }

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

    /* @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
         /*  magnifierCaptureBuilder?.set(
                   CaptureRequest.SCALER_CROP_REGION,
                   Rect(0, 0, 480, 640)
               */
         magnifierCaptureBuilder?.set(CaptureRequest.CONTROL_ZOOM_RATIO, 1.0F)
         /*  magnifierCaptureBuilder?.set(
               CaptureRequest.SCALER_CROP_REGION,
               Rect(positionX, positionY, magnifierWidth.toInt(), magnifierHeight.toInt())
           )*/
         magnifierCaptureBuilder?.set(
             CaptureRequest.CONTROL_AE_MODE,
             CameraMetadata.CONTROL_AE_MODE_OFF
         )
         magnifierCaptureBuilder?.set(
             CaptureRequest.CONTROL_AF_MODE,
             CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
         )
         val conf2 = OutputConfiguration(surface2)
         configs.add(conf2)
         /////////////////////////////////preview
         previewCaptureBuilder =
                 //   device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
             cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL)
         //   captureBuilder.addTarget(surface2!!)
         previewCaptureBuilder?.addTarget(surface)
         previewCaptureBuilder?.addTarget(surface3)
         previewCaptureBuilder?.set(
             CaptureRequest.CONTROL_AE_MODE,
             CameraMetadata.CONTROL_AE_MODE_OFF
         )
         previewCaptureBuilder?.set(
             CaptureRequest.CONTROL_AF_MODE,
             CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
         )

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
                         previewCaptureBuilder?.let {
                             session.setRepeatingRequest(
                                 it.build(),
                                 cameraCaptureSessionCaptureCallback,
                                 mBackgroundHandler
                             )
                         }

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
     }*/

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

}*/