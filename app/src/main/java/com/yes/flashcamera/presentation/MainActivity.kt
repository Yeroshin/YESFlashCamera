package com.yes.flashcamera.presentation

import android.Manifest
import android.annotation.SuppressLint
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
import android.util.Size
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yes.camera.data.repository.CameraRepository
import com.yes.flashcamera.databinding.MainBinding
import com.yes.camera.presentation.model.CameraUI
import com.yes.camera.presentation.ui.CameraScreen
import com.yes.camera.presentation.ui.custom.gles.GLRenderer
import com.yes.settings.presentation.ui.SettingsScreen
import com.yes.flashcamera.presentation.ui.theme.FlashCameraTheme
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
private const val PERMISSIONS_REQUEST_CODE = 10
class MainActivity : ComponentActivity() {

    /*  private val adapter by lazy {
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
        }*/

    /*   private fun dataLoaded() {
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
            0
        )
      //  val helper: SnapHelper = LinearSnapHelper()
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


    }*/

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ///////////////////////////
        setContent {
            FlashCameraTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "Camera") {
                    composable("Camera") {
                        CameraScreen(
                            LocalContext.current,
                            onButtonClick = {
                                navController.navigate("Settings")
                            }
                        )
                    }
                    composable("Settings") {
                        SettingsScreen(
                            onButtonClick = {
                                navController.navigate("Camera")
                            }
                        )
                    }
                }
            }
        }

        ///////////////////////////
        /* cameraService = CameraService(
             this,
             getSystemService(CAMERA_SERVICE) as CameraManager,
             mBackgroundHandler
         ) { state ->
             render(state)
         }*/


        /*  binding = MainBinding.inflate(layoutInflater)

        setUpView()

        checkPermission {

        }
        gles()
        setContentView(binding.root)
        dataLoaded()*/
    }


    private var rendererSet = false

    private val renderer = GLRenderer(
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


    /*  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
            v.performClick()
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

    }/


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




    @RequiresApi(Build.VERSION_CODES.O)
    private fun render(state: CameraUI) {
        state.iso?.let {
            binding.isoOld.min = it.lower
            binding.isoOld.max = it.upper
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
      /*  binding.isoOld.setOnSeekBarChangeListener(listenerIsoSeekBar)
        binding.exposure.setOnSeekBarChangeListener(listenerExposureSeekBar)*/

      /*  binding.settingsGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId !== -1) {
                val radioButton = findViewById<RadioButton>(checkedId)
                if (radioButton.isChecked) {
                    binding.settingsSelector.visibility = View.VISIBLE
                    when (checkedId) {
                        R.id.shutter -> {
                            Log.d("chk", "id" + checkedId);
                        }

                        else -> {
                            Log.d("chk", "id" + checkedId);
                        }
                    }
                } else {
                    binding.settingsSelector.visibility = View.GONE
                }
            }
        }*/
        /*  binding.settingsGroup.setOnCheckedChangeListener { group, checkedId ->
            /*  when (checkedId) {
                  -1 -> {
                      binding.settingsSelector.visibility = GONE
                  }

                  else -> {
                      val checkedButton = binding.root.findViewById<RadioButton>(checkedId)
                      if (checkedButton.isChecked) {
                          //  group?.clearCheck()
                          binding.settingsSelector.visibility = GONE
                      } else {
                          binding.settingsSelector.visibility = VISIBLE
                      }

                  }
              }*/
          }*/
       /* for (i in 0 until binding.settingsGroup.childCount) {
            val child: View = binding.settingsGroup.getChildAt(i)
            if (child is RadioButton) {

                child.setOnClickListener {button->
                  //  (button as ToggleButton).toggle()

                    if ((button as RadioButton).isChecked){
                       // binding.settingsGroup.clearCheck()
                      //  button.isChecked=false
                      //  binding.settingsSelector.visibility = GONE
                    }else{
                       // button.isChecked=true
                       // binding.settingsSelector.visibility = VISIBLE
                    }
                }
            }
        }*/
       /* binding.settingsGroup.addOnButtonCheckedListener(OnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {

            }
        })*/

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

   */
}