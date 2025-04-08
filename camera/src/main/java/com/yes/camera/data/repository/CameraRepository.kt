package com.yes.camera.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.ImageFormat.NV21
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.graphics.YuvImage
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.MeteringRectangle
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.RggbChannelVector
import android.hardware.camera2.params.SessionConfiguration
import android.icu.text.SimpleDateFormat
import android.media.Image
import android.media.ImageReader
import android.media.MediaCodec
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.yes.camera.domain.model.Characteristics
import com.yes.camera.domain.model.Dimensions
import com.yes.camera.utils.ImageComparator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Math.log
import java.nio.ByteBuffer
import java.util.Date
import java.util.Locale
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow


@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class CameraRepository(
    private val context: Context,
    private val cameraManager: CameraManager,
    private val encoder: MediaEncoder
) {
    var sessio: CameraCaptureSession? = null
    //  private var captureResult: CaptureResult? = null

    private var captureRequest: CaptureRequest.Builder? = null
    private var glSurfaceTexture: SurfaceTexture? = null

    private val mBackgroundThread = HandlerThread("CameraThread").apply { start() }
    private val mBackgroundHandler: Handler = Handler(mBackgroundThread.looper)
    private lateinit var cameraDevice: CameraDevice
    private val previewSurface by lazy {
        Surface(glSurfaceTexture)
    }
    private val previewSurfaceConfiguration by lazy {
        OutputConfiguration(previewSurface).apply {
            //  enableSurfaceSharing()
        }
    }
    private val videoSurface by lazy {
        encoder.configure(640, 480)
    }


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private val imageAvailableListener = ImageReader.OnImageAvailableListener { reader ->

        /* if (running){
             val image = reader.acquireNextImage()
             if (image!= null) {
                 val buffer = image.planes[0].buffer
                 val bytes = ByteArray(buffer.remaining())
                 buffer.get(bytes)

                 try {
                     val byteBuffer = ByteBuffer.wrap(bytes) // Создаем ByteBuffer из массива байтов
                     sink.write(byteBuffer.array())
                 } catch (e: IOException) {
                     println()
                 } finally {
                     image.close() // Освобождаем изображение
                 }
             }
         }*/
        // val image = reader.acquireNextImage()
        reader.acquireNextImage()?.let {
            val ybytes = ByteArray(it.planes[0].buffer.capacity())
            it.planes[0].buffer.get(ybytes)
            _outputBuffer.value = ybytes
            it.close()
        }
        //  image?.close()
        /* if (running) {
             val image = reader.acquireNextImage()
             image?.let {
                 // val tmp =yuv420ToBitmap(it)
                 //  _event.value = convertYUV420_888to420p(it)
                 ///////////////////
                 val uvPos = it.width * image.height
                 val uvSize = it.width / 2 * image.height / 2

              //   fps1.get("before")
                 /////////////////////////////////
                /* val yPlane = it.planes[0].buffer
                 val uPlane = it.planes[1].buffer
                 val vPlane = it.planes[2].buffer*/

                 //val buffer = image.planes[0].buffer
                 ////////////////
               /*  val ybytes = ByteArray(it.planes[0].buffer.capacity())
                 it.planes[0].buffer.get(randomAccessFile)
                 val ubytes = ByteArray(uvSize)
                 it.planes[1].buffer.get(ubytes,0,uvSize)
                 val vbytes = ByteArray(uvSize)
                 it.planes[2].buffer.get(vbytes,0,uvSize)*/
               //  fps1.get("middle")
                 ///////////////////

             /*    val a = randomAccessFile?.channel?.write(it.planes[0].buffer)
                 it.planes[1].buffer.limit(uvSize)
                 val b = randomAccessFile?.channel?.write(it.planes[1].buffer)
                 it.planes[2].buffer.limit(uvSize)
                 val c = randomAccessFile?.channel?.write(it.planes[2].buffer)*/
                 /////////////////
                /* val b = randomAccessFile?.write(ubytes)
                 val c = randomAccessFile?.write(vbytes)*/
                /* val b = randomAccessFile?.channel?.write(uPlane.slice(0, uPlane.capacity() / 4))
                 val c = randomAccessFile?.channel?.write(vPlane.slice(0, uPlane.capacity() / 4))*/
                 //////////////////////////////////
                 //    randomAccessFile?.seek(0)
                 // copyImage(image)
                 _event.tryEmit(getByteBufferYUVPlanes(image))
               //  fps1.get("after")
                 /////////////////////////

                 /*  bufferedOutputStream?.write(
                       imageToYUVPlanes(image)
                   )
                   bufferedOutputStream?.flush()*/
                 ///////////////////////
                 // _event.tryEmit(getYUVPlanes(it))
                 it.close()

             }
         } else if (finished) {
             /////////////////
            /* randomAccessFile?.close()
             FFmpegKitConfig.closeFFmpegPipe(pipe1)
             finished = false*/
             /////////////////
             //job.cancel()
             /*   process?.destroy()
                process?.waitFor()
                FFmpegKitConfig.closeFFmpegPipe(pipe1)*/
             ///////////////////
             /*  pipe1?.let {
                   FFmpegKitConfig.closeFFmpegPipe(it)
               }
             //  process?.waitFor()
               process?.destroy()*/
             //   FFmpegKit.cancel()
             // process?.outputStream?.flush()
             //  process?.outputStream?.close()
             //  finished = false
         } else {
             reader.acquireLatestImage()?.close()
             // fps1.get("fps")
         }*/
        //  fps1.get("fps")
        /////////////////

    }
    private val imageReaderHandlerThread = HandlerThread("ImageReaderThread").apply {
        priority = Thread.MAX_PRIORITY
        start()
    }
    private val imageReaderHandler = Handler(imageReaderHandlerThread.looper)
    val rWidth = 4096;
    val rHeight = 3072

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private val imageReader =
        ImageReader.newInstance(rWidth, rHeight, ImageFormat.YUV_420_888, 30).apply {
            setOnImageAvailableListener(imageAvailableListener, imageReaderHandler)
        }
    private val captureSurface by lazy {
        imageReader.surface
    }


    private fun getCameraByFacing(facing: Int): String? {
        cameraManager.cameraIdList.forEach {
            val characteristics = cameraManager.getCameraCharacteristics(it)
            if (characteristics.get(CameraCharacteristics.LENS_FACING) == facing) {
                return it
            }
        }
        return null
    }

    private val _characteristicsFlow: MutableStateFlow<Characteristics?> =
        MutableStateFlow(null)
    private val characteristicsFlow: StateFlow<Characteristics?> =
        _characteristicsFlow

    /*  private val _event: MutableStateFlow<Bitmap?> = MutableStateFlow(null)
      private val event = _event*/
    /*  private val _event: MutableStateFlow<ByteArray?> = MutableStateFlow(null)
      private val event = _event*/
    private val _event: MutableSharedFlow<LightYUVPlanes?> = MutableSharedFlow(
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    private val event = _event
    val comparator = ImageComparator()

    //  var prevImage: Bitmap? = null
    var prevImage: YuvImage? = null

    //enable this comparator!!
    /* init {
         /* CoroutineScope(Dispatchers.IO).launch {
              event.collect {image->
                  image?.let {
                      prevImage?.let {
                          val dif=comparator.compareImageValues(it,image)
                          if (dif>36){//1/15s worked;1/8s relible(1/15s )
                              println("capturd")
                              // Toast.makeText(context,"capture",Toast.LENGTH_SHORT).show()
                          }
                          Log.e("","dif:${
                              dif
                          }")
                          prevImage=image
                      }?:run{
                          prevImage=image
                      }
                  }
              }

          }*/
         /* CoroutineScope(Dispatchers.IO).launch {
              event.collect { yuvImage ->
                  yuvImage?.let {
                      /* val yuvBytes = ByteArrayOutputStream()
                        val bytes=it.getJpegDataWithQuality(100)
                       val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                       prevImage?.let {prev->
                           val dif=comparator.compareImageValues(prev,bitmap)
                           if (dif>36){//1/15s worked;1/8s relible(1/15s )
                               println("capture")
                               // Toast.makeText(context,"capture",Toast.LENGTH_SHORT).show()
                           }
                           Log.e("","dif:${
                               dif
                           }")
                           prevImage=bitmap
                       }?:run{
                           prevImage=bitmap
                       }*/
                      prevImage?.let { prev ->
                          val dif = comparator.compareImageValues(prev, yuvImage)
                          if (dif > 36) {//1/15s worked;1/8s relible(1/15s )
                              println("capture")
                              // Toast.makeText(context,"capture",Toast.LENGTH_SHORT).show()
                          }
                          Log.e(
                              "", "dif:${
                                  dif
                              }"
                          )
                          //   prevImage=bitmap
                      } ?: run {
                          //  prevImage=bitmap
                      }
                  }
                  /*    byteArray?.let {
                     YuvImage(it, NV21, yuv420_888.width, yuv420_888.height, null) }
                 }

                      .getJpegDataWithQuality(100)
                      prevImage?.let {
                          val dif=comparator.compareImageValues(it,image)
                          if (dif>36){//1/15s worked;1/8s relible(1/15s )
                              println("capturd")
                              // Toast.makeText(context,"capture",Toast.LENGTH_SHORT).show()
                          }
                          Log.e("","dif:${
                              dif
                          }")
                          prevImage=image
                      }?:run{
                          prevImage=image
                      }*/
              }
          }*/
     }*/

    fun openBackCamera(glSurfaceTexture: SurfaceTexture): StateFlow<Characteristics?> {
        this.glSurfaceTexture = glSurfaceTexture
        // getCameraByFacing(CameraCharacteristics.LENS_FACING_BACK)?.let {
        getCameraByFacing(CameraCharacteristics.LENS_FACING_BACK)?.let {
            openCamera(
                it
            ) { characteristics ->
                _characteristicsFlow.value = characteristics
            }
        }
        return characteristicsFlow
    }

    fun openFrontCamera(glSurfaceTexture: SurfaceTexture): StateFlow<Characteristics?> {
        this.glSurfaceTexture = glSurfaceTexture
        getCameraByFacing(CameraCharacteristics.LENS_FACING_FRONT)?.let {
            openCamera(
                it
            ) { characteristics ->
                _characteristicsFlow.value = characteristics
            }
        }
        return characteristicsFlow
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(id: String, onCameraOpened: (characteristics: Characteristics) -> Unit) {
        // this.onCameraOpened = onCameraOpened
        cameraManager.getCameraCharacteristics(id)
        cameraManager.openCamera(
            id,
            object : CameraDevice.StateCallback() {

                @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    //  previewCaptureBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    // setCharacteristics(51200)
                    startVideoSession()

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


    private fun getCameraCharacteristics(id: String): Characteristics {
        val characteristics = cameraManager.getCameraCharacteristics(id)

        val config = characteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )

        val config2 = characteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP_MAXIMUM_RESOLUTION
        )
        val у = config2?.getOutputSizes(ImageFormat.RAW_SENSOR)
        // If image format is provided, use it to determine supported sizes; or else use target class
        // val allSizes = config?.getOutputSizes(ImageReader::class.java)
        val e = config?.getOutputSizes(MediaCodec::class.java)
        val v = config?.getOutputSizes(ImageFormat.YUV_420_888)
        val t = config?.getOutputSizes(ImageFormat.RAW_SENSOR)
        val allSizes = config?.getOutputSizes(ImageFormat.JPEG)
        allSizes?.maxBy { it.height * it.width }
        val iso = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
        val exposure = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
        val minFocusDistance =
            characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)
        val minFocus = characteristics.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE)

        /////////////////
        val availablePixelModes =
            characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
        val g =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP_MAXIMUM_RESOLUTION)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val sizes = map?.getOutputSizes(MediaRecorder::class.java)
        // Check AF supported
        val activeArraySize =
            characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
        val maxRegionsAf = characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)

/////////////////
        /////////////////
        return Characteristics(
            isoValue = 0,
            isoRange = iso?.let { IntRange(it.lower, it.upper) } ?: IntRange(0, 0),
            shutterValue = 0,
            shutterRange = exposure?.let { LongRange(it.lower, it.upper) } ?: LongRange(0, 0),
            focusValue = 0F,
            minFocusValue = minFocusDistance ?: 0f,
            resolutions = allSizes?.map {
                Dimensions(
                    it.width, it.height
                )
            } ?: listOf(
                Dimensions(0, 0)
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun startVideoSession() {
        createCaptureSession(
            listOf(
                previewSurface,
                //   encoder.configure(640,480),
                captureSurface
            )
        )

    }

    private fun createCaptureSession(surfaces: List<Surface>) {
        val configs = mutableListOf<OutputConfiguration>()
        /* captureRequest =
             cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL)*/
        ////////preview
        for (surface in surfaces) {
            //  captureRequest?.addTarget(surface)
            configs.add(
                OutputConfiguration(surface)
            )
        }

        val config = SessionConfiguration(
            SessionConfiguration.SESSION_REGULAR,
            configs,
            Dispatchers.IO.asExecutor(),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                        sessio = session
                        startPreviewCaptureRequest()
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            }
        )
        cameraDevice.createCaptureSession(config)
    }

    var frameTime: Long = 0
    var autoShutter:Long?=null
    var autoIso:Int?=null
    var wb:Int?=0
    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)
            //////////////////////////
            val iso = request.get(CaptureRequest.SENSOR_SENSITIVITY)
            val exposureTime = request.get(CaptureRequest.SENSOR_EXPOSURE_TIME)
            //  if (request.get(CaptureRequest.CONTROL_AE_MODE) == CaptureRequest.CONTROL_AE_MODE_ON) {
            autoIso = result.get(CaptureResult.SENSOR_SENSITIVITY)
            autoShutter = result.get(CaptureResult.SENSOR_EXPOSURE_TIME)
            val whiteBalanceGains1 = request.get(CaptureRequest.COLOR_CORRECTION_GAINS)
            val whiteBalanceGains = result.get(CaptureResult.COLOR_CORRECTION_GAINS)
              _characteristicsFlow.update { current ->
                  if(autoAE){
                      current?.copy(
                          wbValue  = wb,
                          shutterValue = autoShutter,
                          // shutterValue = Random.nextLong(16_000_000L),
                          isoValue = autoIso
                      )
                  }else{
                      current?.copy(
                          wbValue  = wb,
                          shutterValue = exposureTime?:autoShutter,
                          // shutterValue = Random.nextLong(16_000_000L),
                          isoValue = iso?:autoIso
                      )
                  }

              }
            /*  _characteristicsFlow.value = _characteristicsFlow.value?.copy(
                  shutterValue = exposureTimeNs,
                  isoValue = iso
              )*/
            //  }
            ///////////////////focus
            val afState = result[CaptureResult.CONTROL_AF_STATE]!!

            //  if (request.tag == "focus"){
            if (focus) {
                when (afState) {
                    CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED -> {
                        Toast.makeText(context, "FOCUSED", Toast.LENGTH_SHORT).show()
                        captureRequest?.set(
                            CaptureRequest.CONTROL_AF_TRIGGER,
                            CaptureRequest.CONTROL_AF_TRIGGER_IDLE
                        )
                        focus = false
                        captureRequest?.let {
                            sessio?.capture(it.build(), null, null)
                        }
                    }

                    CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED -> {
                        Toast.makeText(context, "not focused", Toast.LENGTH_SHORT).show()
                    }
                }
                val afRegions = request.get(CaptureRequest.CONTROL_AF_REGIONS)

                /*  captureRequest =
                      cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                  captureRequest?.addTarget(previewSurface)
                  captureRequest?.addTarget(captureSurface)*/

                /*  captureRequest?.set(
                      CaptureRequest.CONTROL_AF_TRIGGER,
                       CaptureRequest.CONTROL_AF_TRIGGER_IDLE
                  )*/
                /* captureRequest?.set(
                     CaptureRequest.CONTROL_AF_TRIGGER,
                     CaptureRequest.CONTROL_AF_TRIGGER_CANCEL
                 )
              //   captureRequest?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
                 captureRequest?.let {
                     sessio?.capture(it.build(), null, null)
                 }*/
                /*  captureRequest = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                  captureRequest?.addTarget(previewSurface)
                  captureRequest?.addTarget(captureSurface)*/

                //  captureRequest?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
                //  captureRequest?.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_CANCEL)
                /*   captureRequest?.let {
                       captureRequest?.setTag("capture")
                       sessio?.stopRepeating()
                       sessio?.setRepeatingRequest(it.build(),null, mBackgroundHandler)
                   }*/
                /* captureRequest =
                     cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                 captureRequest?.addTarget(previewSurface)
                 captureRequest?.addTarget(captureSurface)
                 captureRequest?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)

                 captureRequest?.let {
                      sessio?.stopRepeating()
                     sessio?.setRepeatingRequest(it.build(), null, null)
                 }*/
                /*  when (afState) {

                      CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED -> {
                          val afRegions = request.get(CaptureRequest.CONTROL_AF_REGIONS)

                          captureRequest =
                              cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                          captureRequest?.addTarget(previewSurface)
                          captureRequest?.addTarget(captureSurface)
                          val focusArea = Rect(1, 1, 920, 1230)


                        /*  captureRequest?.set(
                              CaptureRequest.CONTROL_AF_MODE,
                              CaptureRequest.CONTROL_AF_MODE_OFF
                          )*/
                       /*   captureRequest?.set(
                              CaptureRequest.CONTROL_AF_TRIGGER,
                              CaptureRequest.CONTROL_AF_TRIGGER_IDLE
                          )*/
                          captureRequest?.set(
                              CaptureRequest.CONTROL_AF_TRIGGER,
                              null
                          )
                        /*  captureRequest?.set(
                              CaptureRequest.CONTROL_AF_REGIONS,
                              afRegions
                              // arrayOf(MeteringRectangle(focusArea, MeteringRectangle.METERING_WEIGHT_MAX))
                          )*/
                          captureRequest?.let {
                             // sessio?.stopRepeating()
                              sessio?.setRepeatingRequest(it.build(), this, mBackgroundHandler)
                          }
                          println()
                      }
                      CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED -> {
                          println()
                      }
              }
                   when (afState) {

                       CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED -> {
                          /* val afRegions = request.get(CaptureRequest.CONTROL_AF_REGIONS)
                           captureRequest = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                           captureRequest?.addTarget(previewSurface)
                           captureRequest?.addTarget(captureSurface)
                           val focusArea = Rect(1, 1, 920, 1230)


                          /* captureRequest?.set(
                               CaptureRequest.CONTROL_AF_MODE,
                               CaptureRequest.CONTROL_AF_MODE_OFF
                           )*/
                           captureRequest?.set(
                               CaptureRequest.CONTROL_AF_TRIGGER,
                               CaptureRequest.CONTROL_AF_TRIGGER_IDLE
                           )
                          /* captureRequest?.set(
                               CaptureRequest.CONTROL_AF_REGIONS,
                               afRegions
                               // arrayOf(MeteringRectangle(focusArea, MeteringRectangle.METERING_WEIGHT_MAX))
                           )*/
                           captureRequest?.let {
                               sessio?.stopRepeating()
                               sessio?.setRepeatingRequest(it.build(), this, mBackgroundHandler)
                           }*/

                       }
                       CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED -> {
                           println()
                       }
                   }*/

            }

            /*  captureRequest?.let {
                  //  sessio?.stopRepeating()
                  //  sessio?.capture(it.build(), this, mBackgroundHandler)
                    sessio?.setRepeatingRequest(it.build(), this, mBackgroundHandler)
              }*/
            /* when (afState) {
                 CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED -> {
                     // Здесь вы можете выполнить действие в зависимости от результата фокусировки
                     Log.e("f","FOCUSED")
                     println("FOCUSED!!!!")
                     val afRegions = request.get(CaptureRequest.CONTROL_AF_REGIONS)

                     captureRequest?.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE)
                     captureRequest?.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_CANCEL)

                     captureRequest?.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
                     captureRequest?.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
                     captureRequest?.set(CaptureRequest.CONTROL_AF_TRIGGER, null);// As documentation says AF_trigger can be null in some device


                     captureRequest?.let {
                         sessio?.stopRepeating()
                         sessio?.setRepeatingRequest(it.build(), null, mBackgroundHandler)
                     }
                     /*  captureRequest?.let {
                             //  sessio?.stopRepeating()
                           //  sessio?.capture(it.build(), this, mBackgroundHandler)
                             sessio?.setRepeatingRequest(it.build(), this, mBackgroundHandler)
                         }*/
                 }
                 CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED -> {

                     println("not focused")
                     captureRequest?.set(
                         CaptureRequest.CONTROL_AF_TRIGGER,
                         CaptureRequest.CONTROL_AF_TRIGGER_START
                     )
                     /*  captureRequest?.let {
                             //  sessio?.stopRepeating()
                             sessio?.capture(it.build(), this, mBackgroundHandler)
                         }*/
                 }
                 CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN -> {

                     println("scan")
                     //   captureRequest?.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START)
                     /*  captureRequest?.let {
                                 //  sessio?.stopRepeating()
                                 sessio?.capture(it.build(), this, mBackgroundHandler)
                             }*/
                     /*  captureRequest?.set(
                             CaptureRequest.CONTROL_AF_TRIGGER,
                             CameraMetadata.CONTROL_AF_TRIGGER_START
                         );
                         captureRequest?.let {
                             sessio?.setRepeatingRequest(it.build(), this, null);
                         }*/

                     ////////////////////////
                     val currentTime = System.currentTimeMillis()
                     if (frameTime != 0L) {
                         val fps = 1000.0 / (currentTime - frameTime)
                         //   Log.e("CaptureSession", "FPS: $fps")
                         //  println("FPS: $fps")
                     }
                     frameTime = currentTime
                     /////////////////////////
                 }
                 else -> {
                     // Обработка других состояний, если необходимо
                     println("Состояние фокуса: $afState")
                 }
             }*/
        }

        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
            // Получение состояний фокуса
            val focusState = partialResult.get(CaptureResult.CONTROL_AF_STATE)
            focusState?.let {
                when (focusState) {
                    CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN -> {}
                    CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED -> {}
                    CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED -> {}
                    CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN -> {}
                    CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED -> {}
                    else -> {}
                }
            }
        }
    }

    fun startPreviewCaptureRequest() {
        /*  captureRequest =
              cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
          captureRequest?.addTarget(previewSurface)
          captureRequest?.addTarget(captureSurface)*/
        //  previewCaptureBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        /*  ///test
          captureRequest?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
          captureRequest?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
          captureRequest?.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);*/

        //////////////////
        /*   captureRequest?.set(
               CaptureRequest.CONTROL_AE_MODE,
               CaptureRequest.CONTROL_AE_MODE_OFF
           )*/
        //////////settings
        /* captureRequest?.set(
             CaptureRequest.EDGE_MODE,
             CaptureRequest.EDGE_MODE_OFF
         )
         captureRequest?.set(
             CaptureRequest.NOISE_REDUCTION_MODE,
             CaptureRequest.NOISE_REDUCTION_MODE_OFF
         )
         captureRequest?.set(
             CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
             CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_OFF
         )*/


        // previewCaptureBuilder?.set(CaptureRequest.LENS_FOCUS_DISTANCE, 0.2f)
        //  previewCaptureBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL)

        // previewCaptureBuilder?.set(CaptureRequest.CONTROL_ZOOM_RATIO, 10F)
        /* captureRequest?.set(CaptureRequest.SENSOR_SENSITIVITY, 1600)
         captureRequest?.set(CaptureRequest.SENSOR_EXPOSURE_TIME, 33_333_333L)*/
        /*  captureRequest?.let {

              sessio?.setRepeatingRequest(it.build(), captureCallback, mBackgroundHandler)
          }*/

        ////////////////////
        submitRequest(
            CameraDevice.TEMPLATE_PREVIEW,
            listOf(
                previewSurface,
                captureSurface
            ),
            true
        ) { builder ->
            builder.apply {
                set(
                    CaptureRequest.EDGE_MODE,
                    CaptureRequest.EDGE_MODE_OFF
                )
                set(
                    CaptureRequest.NOISE_REDUCTION_MODE,
                    CaptureRequest.NOISE_REDUCTION_MODE_OFF
                )
                set(
                    CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
                    CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_OFF
                )
            }
        }
    }


    private val METERING_RECTANGLE_SIZE = 0.15f
    private fun meteringRectangle(touchPoint: FloatArray): MeteringRectangle {
        val characteristics = cameraManager.getCameraCharacteristics("0")
        val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
        val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)!!

        val halfMeteringRectWidth = (METERING_RECTANGLE_SIZE * sensorSize.width())
        val halfMeteringRectHeight = (METERING_RECTANGLE_SIZE * sensorSize.height())


        /////////////////////////
        val x = touchPoint[0] * sensorSize.height()
        val y = touchPoint[1] * sensorSize.width()


        //////////////////////////
        // Normalize the [x,y] touch point in the view port to values in the range of [0,1]
        //   val normalizedPoint = floatArrayOf(event.x / previewSize.height, event.y / previewSize.width)

        // Scale and rotate the normalized point such that it maps to the sensor region
        Matrix().apply {
            postRotate(-sensorOrientation.toFloat(), 0.5f, 0.5f)
            postScale(sensorSize.width().toFloat(), sensorSize.height().toFloat())
            mapPoints(touchPoint)
        }

        val meteringRegion = Rect(
            (touchPoint[0] - halfMeteringRectWidth).toInt().coerceIn(0, sensorSize.width()),
            (touchPoint[1] - halfMeteringRectHeight).toInt().coerceIn(0, sensorSize.height()),
            (touchPoint[0] + halfMeteringRectWidth).toInt().coerceIn(0, sensorSize.width()),
            (touchPoint[1] + halfMeteringRectHeight).toInt().coerceIn(0, sensorSize.height())
        )

        return MeteringRectangle(meteringRegion, MeteringRectangle.METERING_WEIGHT_MAX)
    }

    var focus = false
    fun setInputCharacteristicsOldWorked(characteristics: Characteristics) {
        /* captureRequest =
             cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL)*/
        /* previewCaptureBuilder =
             cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG)*/
        //  captureRequest?.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF)
        captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequest?.addTarget(previewSurface)
        captureRequest?.addTarget(captureSurface)

        captureRequest?.set(
            CaptureRequest.EDGE_MODE,
            CaptureRequest.EDGE_MODE_OFF
        )
        //   captureRequest?.set(CaptureRequest.TONEMAP_MODE, CaptureRequest.TONEMAP_MODE_CONTRAST_CURVE)
        captureRequest?.set(
            CaptureRequest.NOISE_REDUCTION_MODE,
            CaptureRequest.NOISE_REDUCTION_MODE_OFF
        )
        captureRequest?.set(
            CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
            CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_OFF
        )
        ///////test
        // sessio?.stopRepeating();

        //cancel any existing AF trigger (repeated touches, etc.)
        // captureRequest?.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
        //  captureRequest?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

        /*  captureRequest?.let {
              //  sessio?.stopRepeating()
              sessio?.capture(it.build(), captureCallback, mBackgroundHandler)
          }*/

        //  captureRequest?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
        // captureRequest?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

        ///////////////////////////
        /*  val cameraCharacteristics = cameraManager.getCameraCharacteristics("0")
          val afRegion: Int? = cameraCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)
          val aeRegion: Int? = cameraCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE)
          val awbRegion: Int? = cameraCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AWB)

          val sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
          val sensorArraySize: Rect = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)!!
          val height = sensorArraySize.height()
          val width = sensorArraySize.width()
          val meteringRectWidth = METERING_RECTANGLE_SIZE * sensorArraySize.width()
          val meteringRectHeight = METERING_RECTANGLE_SIZE * sensorArraySize.height()
          val centerX = sensorArraySize.centerX()
          val centerY = sensorArraySize.centerY()
          println(centerY)
          println(centerX)*/

        ////////////////////////////


        captureRequest?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
        //  captureRequest?.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_CANCEL)
        captureRequest?.let {
            captureRequest?.setTag("capture")
            sessio?.stopRepeating()
            sessio?.setRepeatingRequest(it.build(), captureCallback, mBackgroundHandler)
        }
        //////////
        /*  captureRequest = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
          captureRequest?.addTarget(previewSurface)
          captureRequest?.addTarget(captureSurface)*/
        /* captureRequest?.set(
             CaptureRequest.CONTROL_AF_REGIONS,
             arrayOf(MeteringRectangle(focusArea, MeteringRectangle.METERING_WEIGHT_MAX ))
         )*/
        /* captureRequest?.set(
            CaptureRequest.CONTROL_AE_REGIONS,
            arrayOf(meteringRectangle(characteristics.touchPoint ))
        )*/
        val r = meteringRectangle(characteristics.touchPoint)
        val focusArea = Rect(1, 1, 300, 300)
        captureRequest?.set(
            CaptureRequest.CONTROL_AF_REGIONS,
            arrayOf(r)
        )
        /*  captureRequest?.set(
               CaptureRequest.CONTROL_AE_REGIONS,
               arrayOf(r)
           )
           captureRequest?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)*/
        // captureRequest?.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        captureRequest?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
        // captureRequest?.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE)
        captureRequest?.set(
            CaptureRequest.CONTROL_AF_TRIGGER,
            CaptureRequest.CONTROL_AF_TRIGGER_START
        )
        captureRequest?.setTag("focus")
        focus = true
        captureRequest?.let {
            sessio?.capture(it.build(), captureCallback, mBackgroundHandler)
        }
        /* captureRequest?.set(
             CaptureRequest.CONTROL_AE_REGIONS,
               arrayOf(MeteringRectangle(focusArea, MeteringRectangle.METERING_WEIGHT_MAX ))
         )
         captureRequest?.set(
             CaptureRequest.CONTROL_AWB_REGIONS,
             arrayOf(MeteringRectangle(focusArea, MeteringRectangle.METERING_WEIGHT_MAX ))
         )*/

        // captureRequest?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)

        //  captureRequest?.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
        /////////bad working
        /*  captureRequest?.set(CaptureRequest.DISTORTION_CORRECTION_MODE, CameraMetadata.DISTORTION_CORRECTION_MODE_OFF)
          captureRequest?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
          captureRequest?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
          captureRequest?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)


          captureRequest?.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)
          captureRequest?.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE)
          captureRequest?.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START)
          captureRequest?.let {
              sessio?.stopRepeating()
              sessio?.capture(it.build(), captureCallback, mBackgroundHandler)
          }*/
        ///////////////////////


        /*  captureRequest?.let {
              sessio?.stopRepeating()
              sessio?.setRepeatingRequest(it.build(), captureCallback, mBackgroundHandler)
          }*/


        /* captureRequest?.set(CaptureRequest.SENSOR_SENSITIVITY, characteristics.isoValue)
         captureRequest?.set(
             CaptureRequest.SENSOR_EXPOSURE_TIME,
             characteristics.shutterValue
         )*/


        /*  captureRequest?.set(
              CaptureRequest.CONTROL_AE_MODE,
              CaptureRequest.CONTROL_AE_MODE_OFF
          )*/
        //////////settings
        /* captureRequest?.set(
             CaptureRequest.EDGE_MODE,
             CaptureRequest.EDGE_MODE_OFF
         )
         captureRequest?.set(
             CaptureRequest.NOISE_REDUCTION_MODE,
             CaptureRequest.NOISE_REDUCTION_MODE_OFF
         )
         captureRequest?.set(
             CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
             CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_OFF
         )*/
        ///////////////focus

        //  captureRequest?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        //   captureRequest?.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
        ////////////////////
        // captureRequest?.set(CaptureRequest.LENS_FOCUS_DISTANCE, characteristics.focusValue)
        //  previewCaptureBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL)

        // previewCaptureBuilder?.set(CaptureRequest.CONTROL_ZOOM_RATIO, 10F)
        ////////////////
        /* captureRequest?.set(CaptureRequest.SENSOR_SENSITIVITY, characteristics.isoValue)
         captureRequest?.set(
             CaptureRequest.SENSOR_EXPOSURE_TIME,
             characteristics.shutterValue
         )*/

        /*   captureRequest?.let {
                sessio?.stopRepeating()
               sessio?.setRepeatingRequest(it.build(), captureCallback, mBackgroundHandler)
           }*/

    }
    ////////////////////////
  /*  fun colorCorrectionGainsToKelvin(gains: RggbChannelVector): Int {
        val rGain = gains.red.toDouble()
        val bGain = gains.blue.toDouble()

        // Добавляем проверку на минимальные значения
        val rLin = (1.0 / rGain).coerceAtMost(4.0)
        val bLin = (1.0 / bGain).coerceAtMost(4.0)
        val gLin = 1.0

        // Уточнённая обратная матрица
        val X = 0.4124 * rLin + 0.3576 * gLin + 0.1805 * bLin
        val Y = 0.2126 * rLin + 0.7152 * gLin + 0.0722 * bLin
        val Z = 0.0193 * rLin + 0.1192 * gLin + 0.9505 * bLin

        val sum = X + Y + Z
        val x = (X / sum).coerceIn(0.0, 1.0)
        val y = (Y / sum).coerceIn(0.0, 1.0)

        // Модифицированный алгоритм поиска
        var low = 1000
        var high = 40000
        var bestTemp = 6500
        var minError = Double.MAX_VALUE

        repeat(100) {
            val mid = (low + high) / 2
            val (xCalc, yCalc) = calculateXY(mid.toDouble())

            val error = (xCalc - x).pow(2) + (yCalc - y).pow(2)

            if (error < minError) {
                minError = error
                bestTemp = mid
            }

            when {
                xCalc < x -> low = mid
                else -> high = mid
            }

            if (high - low <= 1) return bestTemp
        }

        return bestTemp
    }

    private fun calculateXY(temp: Double): Pair<Double, Double> {
        val x = if (temp <= 4000) {
            (-0.2661239e9 / temp.pow(3) - 0.2343580e6 / temp.pow(2)
                    + 0.8776956e3 / temp + 0.179910)
        } else {
            (-3.0258469e9 / temp.pow(3) + 2.1070379e6 / temp.pow(2)
                    + 0.2226347e3 / temp + 0.240390)
        }

        val y = when {
            temp <= 2222 -> {
                -1.1063814 * x.pow(3) - 1.34811020 * x.pow(2)
                + 2.18555832 * x - 0.20219683
            }
            temp <= 4000 -> {
                -0.9549476 * x.pow(3) - 1.37418593 * x.pow(2)
                + 2.09137015 * x - 0.16748867
            }
            else -> {
                3.0817580 * x.pow(3) - 5.87338670 * x.pow(2)
                + 3.75112997 * x - 0.37001483
            }
        }

        return x to y
    }*/
   /* fun kelvinToColorCorrectionGains(tempKelvin: Int): RggbChannelVector {
        val temp = tempKelvin.coerceIn(1000, 40000).toDouble()

        val x = if (temp <= 4000) {
            (-0.2661239e9 / temp.pow(3) - 0.2343580e6 / temp.pow(2)
                    + 0.8776956e3 / temp + 0.179910)
        } else {
            (-3.0258469e9 / temp.pow(3) + 2.1070379e6 / temp.pow(2)
                    + 0.2226347e3 / temp + 0.240390)
        }

        val y = when {
            temp <= 2222 -> {
                -1.1063814 * x.pow(3) - 1.34811020 * x.pow(2)
                + 2.18555832 * x - 0.20219683
            }
            temp <= 4000 -> {
                -0.9549476 * x.pow(3) - 1.37418593 * x.pow(2)
                + 2.09137015 * x - 0.16748867
            }
            else -> {
                3.0817580 * x.pow(3) - 5.87338670 * x.pow(2)
                + 3.75112997 * x - 0.37001483
            }
        }

        // Фиксируем отрицательные значения RGB
        val Y = 1.0
        val X = (Y * x / y).coerceAtLeast(0.0)
        val Z = (Y * (1 - x - y) / y).coerceAtLeast(0.0)

        val rLin = (3.2406 * X - 1.5372 * Y - 0.4986 * Z).coerceAtLeast(0.001)
        val gLin = (-0.9689 * X + 1.8758 * Y + 0.0415 * Z).coerceAtLeast(0.001)
        val bLin = (0.0557 * X - 0.2040 * Y + 1.0570 * Z).coerceAtLeast(0.001)

        val rGain = (gLin / rLin).coerceIn(0.25, 4.0)
        val bGain = (gLin / bLin).coerceIn(0.25, 4.0)

        return RggbChannelVector(rGain.toFloat(), 1.0f, 1.0f, bGain.toFloat())
    }*/

   /* fun colorCorrectionGainsToKelvin(gains: RggbChannelVector): Int {
        val rGain = gains.red.toDouble()
        val gGain = gains.greenEven.toDouble()
        val bGain = gains.blue.toDouble()

        // Проверка на минимальные значения
        val rLin = (1.0 / rGain).coerceAtMost(4.0)
        val gLin = (1.0 / gGain).coerceAtMost(4.0)
        val bLin = (1.0 / bGain).coerceAtMost(4.0)

        // Обратные значения X, Y, Z
        val X = 0.4124 * rLin + 0.3576 * gLin + 0.1805 * bLin
        val Y = 0.2126 * rLin + 0.7152 * gLin + 0.0722 * bLin
        val Z = 0.0193 * rLin + 0.1192 * gLin + 0.9505 * bLin

        val sum = X + Y + Z
        val x = (X / sum).coerceIn(0.0, 1.0)
        val y = (Y / sum).coerceIn(0.0, 1.0)

        // Алгоритм поиска температуры
        var low = 1000
        var high = 40000
        var bestTemp = 6500
        var minError = Double.MAX_VALUE

        repeat(100) {
            val mid = (low + high) / 2
            val (xCalc, yCalc) = calculateXY(mid.toDouble())

            val error = (xCalc - x).pow(2) + (yCalc - y).pow(2)

            if (error < minError) {
                minError = error
                bestTemp = mid
            }

            // Обновляем границы для бинарного поиска
            if (xCalc < x) {
                low = mid + 1
            } else {
                high = mid - 1
            }
        }

        return bestTemp
    }

    fun calculateXY(kelvin: Double): Pair<Double, Double> {
        val temp = kelvin
        val x: Double
        val y: Double

        // Рассчитываем значения x и y на основе температуры
        if (temp <= 4000) {
            x = (-0.2661239e9 / temp.pow(3) - 0.2343580e6 / temp.pow(2) + 0.8776956e3 / temp + 0.179910)
        } else {
            x = (-3.0258469e9 / temp.pow(3) + 2.1070379e6 / temp.pow(2) + 0.2226347e3 / temp + 0.240390)
        }

        y = when {
            temp <= 2222 -> {
                -1.1063814 * x.pow(3) - 1.34811020 * x.pow(2) + 2.18555832 * x - 0.20219683
            }
            temp <= 4000 -> {
                -0.9549476 * x.pow(3) - 1.37418593 * x.pow(2) + 2.09137015 * x - 0.16748867
            }
            else -> {
                3.0817580 * x.pow(3) - 5.87338670 * x.pow(2) + 3.75112997 * x - 0.37001483
            }
        }

        return Pair(x, y)
    }*/

    /*private fun calculateXY(temp: Double): Pair<Double, Double> {
        val x = if (temp <= 4000) {
            (-0.2661239e9 / temp.pow(3) - 0.2343580e6 / temp.pow(2)
                    + 0.8776956e3 / temp + 0.179910)
        } else {
            (-3.0258469e9 / temp.pow(3) + 2.1070379e6 / temp.pow(2)
                    + 0.2226347e3 / temp + 0.240390)
        }

        val y = when {
            temp <= 2222 -> {
                -1.1063814 * x.pow(3) - 1.34811020 * x.pow(2)
                + 2.18555832 * x - 0.20219683
            }
            temp <= 4000 -> {
                -0.9549476 * x.pow(3) - 1.37418593 * x.pow(2)
                + 2.09137015 * x - 0.16748867
            }
            else -> {
                3.0817580 * x.pow(3) - 5.87338670 * x.pow(2)
                + 3.75112997 * x - 0.37001483
            }
        }

        return x to y
    }*/
   /* fun kelvinToColorCorrectionGains(kelvin: Int): RggbChannelVector {
        val temp = kelvin.toDouble()

        val x: Double
        val y: Double

        // Рассчитываем значения x и y на основе температуры
        if (temp <= 4000) {
            x = (-0.2661239e9 / temp.pow(3) - 0.2343580e6 / temp.pow(2) + 0.8776956e3 / temp + 0.179910)
        } else {
            x = (-3.0258469e9 / temp.pow(3) + 2.1070379e6 / temp.pow(2) + 0.2226347e3 / temp + 0.240390)
        }

        y = when {
            temp <= 2222 -> {
                -1.1063814 * x.pow(3) - 1.34811020 * x.pow(2) + 2.18555832 * x - 0.20219683
            }
            temp <= 4000 -> {
                -0.9549476 * x.pow(3) - 1.37418593 * x.pow(2) + 2.09137015 * x - 0.16748867
            }
            else -> {
                3.0817580 * x.pow(3) - 5.87338670 * x.pow(2) + 3.75112997 * x - 0.37001483
            }
        }

        // Преобразуем x и y в коэффициенты коррекции цвета
        val rGain = (1.0 / (x / y)).coerceIn(0.25, 4.0)
        val gGain = 1.0f // Грин всегда равен 1.0
        val bGain = (1.0 / ((1 - x) / (1 - y))).coerceIn(0.25, 4.0)

        return RggbChannelVector(rGain.toFloat(), gGain,gGain, bGain.toFloat())
    }*/


    fun colorCorrectionGainsToKelvin(rggb: RggbChannelVector): Int {
        val targetRed = rggb.red
        val targetBlue = rggb.blue
        var bestKelvin = 6500
        var minError = Float.MAX_VALUE

        // Коэффициенты для поиска (можно оптимизировать)
        val searchParams = listOf(
            Triple(1000, 40000, 500),  // Грубый поиск
            Triple(-500, 500, 50),     // Средняя точность
            Triple(-50, 50, 1)         // Точное уточнение
        )

        searchParams.forEach { (startOffset, endOffset, step) ->
            val searchStart = (bestKelvin + startOffset).coerceAtLeast(1000)
            val searchEnd = (bestKelvin + endOffset).coerceAtMost(40000)

            for (kelvin in searchStart..searchEnd step step) {
                val gains = kelvinToColorCorrectionGains(kelvin)
                val error = abs(gains.red - targetRed) + abs(gains.blue - targetBlue)

                if (error < minError) {
                    minError = error
                    bestKelvin = kelvin
                }
            }
        }

        return bestKelvin
    }

    /*fun kelvinToColorCorrectionGains(whiteBalance: Int): RggbChannelVector {
        var tmpKelvin = whiteBalance.coerceIn(1000, 40000) / 100

        val r = if (tmpKelvin <= 66) 255 else {
            (329.698727446 * (tmpKelvin - 60.0).pow(-0.1332047592))
                .coerceIn(0.0, 255.0).toInt()
        }

        val g = if (tmpKelvin <= 66) {
            (99.4708025861 * ln(tmpKelvin.toDouble()) - 161.1195681661)
                .coerceIn(0.0, 255.0).toInt()
        } else {
            (288.1221695283 * (tmpKelvin - 60.0).pow(-0.0755148492))
                .coerceIn(0.0, 255.0).toInt()
        }

        val b = when {
            tmpKelvin >= 66 -> 255
            tmpKelvin <= 19 -> 0
            else -> (138.5177312231 * ln(tmpKelvin - 10.0) - 305.0447927307)
                .coerceIn(0.0, 255.0).toInt()
        }

        return RggbChannelVector(
            if (r == 0) 1f else g.toFloat() / r,
            1f,
            1f,
            when {
                b == 0 && g == 0 -> 1f
                b == 0 -> g.toFloat()
                else -> g.toFloat() / b
            }
        )
    }*/

    fun kelvinToColorCorrectionGains(kelvin: Int): RggbChannelVector {
        val scaledTemp = (kelvin.coerceIn(1000, 40000) / 100)

        val red = when {
            scaledTemp <= 66 -> 255f
            else -> 329.698727446f * (scaledTemp - 60).toFloat().pow(-0.1332047592f)
        }.coerceIn(0f, 255f)

        val green = when {
            scaledTemp <= 66 -> 99.4708025861f * ln(scaledTemp.toFloat()) - 161.1195681661f
            else -> 288.1221695283f * (scaledTemp - 60).toFloat().pow(-0.0755148492f)
        }.coerceIn(0f, 255f)

        val blue = when {
            scaledTemp >= 66 -> 255f
            scaledTemp <= 19 -> 0f
            else -> 138.5177312231f * ln((scaledTemp - 10).toFloat()) - 305.0447927307f
        }.coerceIn(0f, 255f)

        return RggbChannelVector(
            (red / 255f) * 2f,
            green / 255f,
            green / 255f,
            (blue / 255f) * 2f
        )
    }
    /////////////////////////////////
    class WhiteBalanceHelper {

        companion object {
            private const val MAX_CHANNEL_VALUE = 255.0f
            private const val TEMPERATURE_DIVIDER = 100

            // Основная функция преобразования температуры в коэффициенты усиления
            fun calculateRggbVector(temperatureKelvin: Int): RggbChannelVector {
                val temp = temperatureKelvin / TEMPERATURE_DIVIDER.toFloat()

                val red = calculateRedChannel(temp)
                val green = calculateGreenChannel(temp)
                val blue = calculateBlueChannel(temp)

                return normalizeAndCreateVector(red, green, blue)
            }

            private fun calculateRedChannel(temp: Float): Float {
                return when {
                    temp > 66 -> {
                        val value = 329.698727446 * (temp - 60).toDouble().pow(-0.1332047592)
                        value.coerceIn(0.0, MAX_CHANNEL_VALUE.toDouble()).toFloat()
                    }
                    else -> 0f
                }
            }

            private fun calculateGreenChannel(temp: Float): Float {
                return when {
                    temp > 66 -> {
                        val value = 288.1221695283 * (temp - 60).toDouble().pow(-0.0755148492)
                        value.coerceIn(0.0, MAX_CHANNEL_VALUE.toDouble()).toFloat()
                    }
                    else -> {
                        val value = 99.4708025861 * log(temp.toDouble()) - 161.1195681661
                        value.coerceIn(0.0, MAX_CHANNEL_VALUE.toDouble()).toFloat()
                    }
                }
            }

            private fun calculateBlueChannel(temp: Float): Float {
                return when {
                    temp >= 66 -> MAX_CHANNEL_VALUE
                    temp <= 19 -> 0f
                    else -> {
                        val value = 138.5177312231 * log((temp - 10).toDouble()) - 305.0447927307
                        value.coerceIn(0.0, MAX_CHANNEL_VALUE.toDouble()).toFloat()
                    }
                }
            }

            private fun normalizeAndCreateVector(red: Float, green: Float, blue: Float): RggbChannelVector {
                val normalizedRed = (red / MAX_CHANNEL_VALUE) * 2.0f
                val normalizedGreen = green / MAX_CHANNEL_VALUE
                val normalizedBlue = (blue / MAX_CHANNEL_VALUE) * 2.0f

                return RggbChannelVector(
                    normalizedRed,
                    normalizedGreen,
                    normalizedGreen, // Дублируем для второго зеленого канала
                    normalizedBlue
                )
            }

            // Функция для установки баланса белого
            fun setCustomWhiteBalance(
                characteristics: CameraCharacteristics,
                cameraDevice: CameraDevice,
                temperatureKelvin: Int
            ) {
                // Проверка поддержки ручного режима
                val awbModes = characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES)
             /*   if (!awbModes.contains(CaptureRequest.CONTROL_AWB_MODE_OFF)) {
                    throw IllegalStateException("Manual white balance not supported")
                }*/

                // Расчет коэффициентов
              /*  val rggbVector = calculateRggbVector(temperatureKelvin)
                val gains=floatArrayOf(
                    rggbVector.redGain,
                    rggbVector.greenEvenGain,
                    rggbVector.greenOddGain,
                    rggbVector.blueGain
                )*/
                // Создание запроса
               /* val requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL).apply {
                    set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF)
                    set(
                        CaptureRequest.COLOR_CORRECTION_GAINS,
                        floatArrayOf(
                            rggbVector.redGain,
                            rggbVector.greenEvenGain,
                            rggbVector.greenOddGain,
                            rggbVector.blueGain
                        )
                    )
                }*/

                // Применение настроек (пример для повторяющегося запроса)

            }
        }
    }
    ////////////////////////
    private var autoAE=false
    fun setInputCharacteristics(characteristics: Characteristics) {
        /* captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
         captureRequest?.addTarget(previewSurface)
         captureRequest?.addTarget(captureSurface)

         captureRequest?.set(
             CaptureRequest.EDGE_MODE,
             CaptureRequest.EDGE_MODE_OFF
         )
         //   captureRequest?.set(CaptureRequest.TONEMAP_MODE, CaptureRequest.TONEMAP_MODE_CONTRAST_CURVE)
         captureRequest?.set(
             CaptureRequest.NOISE_REDUCTION_MODE,
             CaptureRequest.NOISE_REDUCTION_MODE_OFF
         )
         captureRequest?.set(
             CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
             CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_OFF
         )

         captureRequest?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
         //  captureRequest?.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_CANCEL)
         captureRequest?.let {
             captureRequest?.setTag("capture")
             sessio?.stopRepeating()
             sessio?.setRepeatingRequest(it.build(), captureCallback, mBackgroundHandler)
         }

         val r = meteringRectangle(characteristics.touchPoint)
         val focusArea = Rect(1, 1, 300, 300)
         captureRequest?.set(
             CaptureRequest.CONTROL_AF_REGIONS,
             arrayOf(r)
         )
         /*  captureRequest?.set(
                CaptureRequest.CONTROL_AE_REGIONS,
                arrayOf(r)
            )
            captureRequest?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)*/
         // captureRequest?.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
         captureRequest?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
         // captureRequest?.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE)
         captureRequest?.set(
             CaptureRequest.CONTROL_AF_TRIGGER,
             CaptureRequest.CONTROL_AF_TRIGGER_START
         )
         captureRequest?.setTag("focus")
         focus = true
         captureRequest?.let {
             sessio?.capture(it.build(), captureCallback, mBackgroundHandler)
         }*/

        submitRequest(
            CameraDevice.TEMPLATE_PREVIEW,
            listOf(
                previewSurface,
                 captureSurface
            ),
            true
        ) { builder ->
            builder.apply {
                if (characteristics.isoValue != null && characteristics.shutterValue != null) {
                    // set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF)
                    autoAE=false
                    set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF)
                     set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                     set(
                         CaptureRequest.SENSOR_EXPOSURE_TIME,
                         characteristics.shutterValue
                     )
                     set(
                         CaptureRequest.SENSOR_SENSITIVITY,
                         characteristics.isoValue
                     )
                   /* set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
                    set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)*/
                } else if (characteristics.isoValue == null && characteristics.shutterValue == null) {
                    autoAE=true
                    set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
                    set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                } else if (characteristics.isoValue == null){
                    autoAE=false
                    set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
                    characteristics.shutterValue?.let {shutterValue->
                        set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                        autoShutter?.let {autoShutter->
                            autoIso?.let {autoIso->
                                set(
                                    CaptureRequest.SENSOR_EXPOSURE_TIME,
                                    characteristics.shutterValue
                                )
                                val isoValue=autoIso*(autoShutter/shutterValue)
                                set(
                                    CaptureRequest.SENSOR_SENSITIVITY,
                                    isoValue.toInt()
                                )
                            }

                        }
                    }
                }else {
                    autoAE=false
                    set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF)
                    set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                        autoShutter?.let {autoShutter->
                            autoIso?.let {autoIso->
                                set(
                                    CaptureRequest.SENSOR_SENSITIVITY,
                                    characteristics.isoValue.toInt()
                                )
                                val shutterValue=autoShutter*(autoIso/characteristics.isoValue)
                                set(
                                    CaptureRequest.SENSOR_EXPOSURE_TIME,
                                    shutterValue
                                )
                            }
                        }
                }
                //////wb
                wb=characteristics.wbValue
                val rggbVector = kelvinToColorCorrectionGains(characteristics.wbValue!!)
                set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF)
                set(
                    CaptureRequest.COLOR_CORRECTION_GAINS,
                    rggbVector
                )
                set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX)
                ////////////////////////////////
            }
        }
    }

    private fun submitRequest(
        templateType: Int,
        targets: List<Surface>,
        isRepeating: Boolean,
        block: (captureRequest: CaptureRequest.Builder) -> CaptureRequest.Builder
    ) {
        try {
            val captureBuilder = cameraDevice.createCaptureRequest(templateType)
                .apply {
                    targets.forEach {
                        addTarget(it)
                    }
                    block(this)
                }
            if (isRepeating) {
                sessio?.stopRepeating()
                sessio?.setRepeatingRequest(
                    captureBuilder.build(),
                    captureCallback, mBackgroundHandler
                )
            } else {
                sessio?.capture(
                    captureBuilder.build(),
                    captureCallback, mBackgroundHandler
                )
            }
        } catch (e: CameraAccessException) {
            Toast
                .makeText(
                    context,
                    "Camera failed to submit capture request!.",
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }


    /*   private val imageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
          ////////ffmpeg
           val image = reader.acquireNextImage()
           val buffer = image.planes[0].buffer
           val bytes = ByteArray(buffer.remaining())
           buffer.get(bytes)

           try {

               sink.write(buffer)
           } catch (e: IOException) {
               println()
           } finally {
               source.close()
               sink.close()
           }
           image.close()
           //////////////////
           /* val image = reader.acquireLatestImage()


           // val image = reader.acquireNextImage()
           image?.let {
               characteristics?.let { characteristics ->
                   captureResult?.let { captureResult ->
                       saveImage(image, characteristics, captureResult)
                   }
               }
           }

           /* if(singleCapture){
                characteristics?.let { characteristics ->
                    captureResult?.let { captureResult ->
                        saveImage(image, characteristics, captureResult)
                    }
                }
                if (!repeatingCapture){
                    singleCapture=false
                }

            }*/
           ////////////////////////
           val currentTime = System.currentTimeMillis()
           if (lastFrameTime != 0L) {
               val fps = 1000.0 / (currentTime - lastFrameTime)
               Log.i("", "FPS: $fps")
               //  println("FPS: $fps")
           }
           lastFrameTime = currentTime
           /////////////////////////
           image?.close()
           //  println("render")*/
       }*/


    var running = false
    var finished = false
    var frameCount = 0

    class ImageBuffer(private val bufferSize: Int) {
        private val buffer: MutableList<YuvImage> = mutableListOf()
        private val lock = ReentrantLock()
        private val condition = lock.newCondition()

        fun addImage(image: YuvImage) {
            lock.lock()
            try {
                while (buffer.size >= bufferSize) {
                    condition.await() // wait until buffer has space
                }
                buffer.add(image)
                condition.signalAll() // notify that buffer has new image
            } finally {
                lock.unlock()
            }
        }

        fun getImage(): YuvImage {
            lock.lock()
            try {
                while (buffer.isEmpty()) {
                    condition.await() // wait until buffer has image
                }
                return buffer.removeAt(0)
            } finally {
                lock.unlock()
            }
        }
    }

    /*  inner class WriterThread(private val imageBuffer: ImageBuffer) : Thread() {
          override fun run() {
              while (true) {
                  val image = imageBuffer.getImage()
                  if (image != null) {
                      try {
                          convertNV21toyuv420(image)
                      } catch (e: IOException) {
                          Log.e("WriterThread", "Error writing to output stream: $e")
                      }
                  } else {
                      // buffer is empty, exit thread
                      break
                  }
              }
          }
      }*/

    private var previousFrameTime = 0L

    private val fps1 = Fps()

    private var process: Process? = null

    //  private var bufferedOutputStream: BufferedOutputStream? = null
    private var output: BufferedOutputStream? = null


    private val byteArray = ByteArray(120000)

    private val _outputBuffer: MutableStateFlow<ByteArray> = MutableStateFlow(byteArray)
    private val outputBuffer: StateFlow<ByteArray> = _outputBuffer
    fun subscribeOutputBuffer(): StateFlow<ByteArray> {
        return outputBuffer
    }


    fun copyImage(image: Image) {

        var uvPos = image.width * image.height
        val uvSize = image.width / 2 * image.height / 2
        /*   yBuffer.clear()
           yBuffer.put(image.planes[0].buffer)
           uBuffer.clear()
           uBuffer.put(image.planes[1].buffer)
           vBuffer.clear()
           vBuffer.put(image.planes[2].buffer)*/
        image.planes?.let {
            yuvPlanesBuffer.clear()
            yuvPlanesBuffer.put(image.planes[0].buffer)
            yuvPlanesBuffer.put(image.planes[1].buffer)
            //   yuvPlanesBuffer.put(image.planes[2].buffer)

        }

    }


    //3840,2160
    //  val rWidth = 640; val rHeight = 480
    //  val rWidth = 4096; val rHeight = 3072

    //   val rWidth=1920; val rHeight=1080


    private fun saveImage(
        image: YuvImage,
        // cameraCharacteristics: CameraCharacteristics,
        //  captureResult: CaptureResult
    ) {
        //   _event.value = image

        // Toast.makeText(context,"capture",Toast.LENGTH_SHORT).show()
        //////////////////////////
        /*  val jpegBytes = ByteArray(image.planes[0].buffer.remaining())
          image.planes[0].buffer.get(jpegBytes)

          process?.outputStream?.write(jpegBytes)
          process?.outputStream?.flush()*/
        //  bufferedOutputStream.write(jpegBytes)
        //  bufferedOutputStream.flush()
        // _event.value = YuvImage(image.nv21ByteArray, NV21, image.width, image.height, null)
        //  _event.value = YuvImage(image.nv21ByteArray, NV21, image.width, image.height, null)

        // _event.value= comparator.yuv420_888imageToBitmap(image)
        /* _event.update {
            comparator.yuv420_888imageToBitmap(image)
         }*/


        ////////////////////////////
        /*  when (image.format) {
              ImageFormat.JPEG -> {
                  val buffer = image.planes[0].buffer
                  val bytes = ByteArray(buffer.remaining())
                  buffer.get(bytes)

                  var output: FileOutputStream? = null
                  try {
                      output = FileOutputStream(
                          createFile("jpg")
                      )
                      output.write(bytes)
                  } finally {
                      output?.close()
                  }
              }

              ImageFormat.RAW_SENSOR -> {
                  val dngCreator= DngCreator(cameraCharacteristics,captureResult)
                  var output: FileOutputStream? = null
                  try {
                      output = FileOutputStream(
                          createFile("dng")
                      )
                      dngCreator.writeImage(output,image)
                  } finally {
                      output?.close()
                  }
              }
              ImageFormat.YUV_420_888->{
                  val bytes=jpegByteArrayFrom(image)
                  var output: FileOutputStream? = null
                  try {
                      output = FileOutputStream(
                          createFile("jpg")
                      )
                      output.write(bytes)
                  } finally {
                      output?.close()
                  }
              }

              else -> {}
          }*/
    }

    fun imageToMat(image: Image): ByteArray {
        val planes = image.planes

        val buffer0 = planes[0].buffer
        val buffer1 = planes[1].buffer
        val buffer2 = planes[2].buffer

        val offset = 0

        val width = image.width
        val height = image.height

        val data =
            ByteArray(image.width * image.height * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8)
        val rowData1 = ByteArray(planes[1].rowStride)
        val rowData2 = ByteArray(planes[2].rowStride)

        val bytesPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8

        // loop via rows of u/v channels
        var offsetY = 0

        val sizeY = width * height * bytesPerPixel
        val sizeUV = (width * height * bytesPerPixel) / 4

        for (row in 0 until height) {
            // fill data for Y channel, two row

            run {
                val length = bytesPerPixel * width
                buffer0[data, offsetY, length]

                if (height - row != 1) buffer0.position(
                    buffer0.position() + planes[0].rowStride - length
                )
                offsetY += length
            }

            if (row >= height / 2) continue

            run {
                var uvlength = planes[1].rowStride
                if ((height / 2 - row) == 1) {
                    uvlength = width / 2 - planes[1].pixelStride + 1
                }

                buffer1[rowData1, 0, uvlength]
                buffer2[rowData2, 0, uvlength]

                // fill data for u/v channels
                for (col in 0 until width / 2) {
                    // u channel
                    data[sizeY + (row * width) / 2 + col] =
                        rowData1[col * planes[1].pixelStride]

                    // v channel
                    data[sizeY + sizeUV + (row * width) / 2 + col] =
                        rowData2[col * planes[2].pixelStride]
                }
            }
        }

        return data
    }

    /*  private fun addImage(image: Image) {
        /*  val buffer = image.planes[0].buffer

          val bytes = ByteArray(buffer.remaining())
          buffer.get(bytes)*/

          //   imageQueue.add(bytes)
          ///////////////
         /* val yBuffer = image.planes[0].buffer
          val uBuffer = image.planes[1].buffer
          val vBuffer = image.planes[2].buffer

          val yBytes = ByteArray(yBuffer.remaining())
          yBuffer.get(yBytes)

          val uBytes = ByteArray(uBuffer.remaining())
          uBuffer.get(uBytes)

          val vBytes = ByteArray(vBuffer.remaining())
          vBuffer.get(vBytes)

          val totalBytes = yBytes.size + uBytes.size + vBytes.size
          val outputBytes = ByteArray(totalBytes)

          System.arraycopy(yBytes, 0, outputBytes, 0, yBytes.size)
          System.arraycopy(uBytes, 0, outputBytes, yBytes.size, uBytes.size)
          System.arraycopy(vBytes, 0, outputBytes, yBytes.size + uBytes.size, vBytes.size)*/
          //////////////////bad but working
        /*  val width = image.width
          val height = image.height
          val pixelStride = image.planes[0].pixelStride
          val rowStride = image.planes[0].rowStride
          val rowPadding = rowStride - pixelStride * width
          val bufferSize = width * height * 3 / 2
          val buffer = ByteBuffer.allocate(bufferSize)
          for (i in 0 until image.planes.size) {
              val planeBuffer = image.planes[i].buffer
              val planeBytes = ByteArray(planeBuffer.remaining())
              planeBuffer[planeBytes]
              buffer.put(planeBytes)
          }
          val bytes = ByteArray(bufferSize)
          buffer.rewind()
          buffer[bytes]*/
          //////////////////endof bad but working
          //////////////////


              // Now write the actual planes.
              for (i in 0 until 3) {
                  val buffer = image.planes[i].buffer
                  val bytes =
                      ByteArray(buffer.remaining()) // makes byte array large enough to hold image
                  buffer.get(bytes) // copies image from buffer to byte array
                  process?.outputStream?.write(bytes)
              }


          ///////////////////


          image.close()
      }*/


    /*   private fun createCaptureSession() {
           val configs = mutableListOf<OutputConfiguration>()
           captureRequest =
               cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG)
           ////////preview

           captureRequest?.addTarget(previewSurface)

           configs.add(
               previewSurfaceConfiguration
           )
           ////////////////
           // ffmpeg()
           ////video
           /* setUpMediaRecorder()
           // val recorderSurface= MediaCodec.createPersistentInputSurface();
            val recorderSurface = mMediaRecorder.surface
          //  mMediaRecorder.setInputSurface(recorderSurface)
         configs.add(
             OutputConfiguration(recorderSurface)
         )
        // val conf2 = OutputConfiguration(recorderSurface)
         captureRequest?.addTarget(recorderSurface)*/
           /////////////photo

           // imageReader = ImageReader.newInstance(4096, 3072, ImageFormat.JPEG, 1)
           // val imageReader =ImageReader.newInstance(4096, 3072, ImageFormat.RAW_SENSOR, 1)

           // val  imageReader=ImageReader.newInstance(4096, 3072, ImageFormat.YUV_420_888, 1)
           // imageReader.setOnImageAvailableListener(imageAvailableListener, null)//imageReaderHandler)


         /*  configs.add(
               imageReaderSurfaceConfiguration
           )*/

         //  captureRequest?.addTarget(imageReader.surface)

           /////////////////media codec
          /* val mEncoderSurface=encoder.start(
               createFile("mp4")
           )*/
         //  val mEncoderSurface = MediaCodec.createPersistentInputSurface()

         //  mCodec!!.setInputSurface(mEncoderSurface)
           configs.add(
               videoSurfaceConfiguration
           )
         //  captureRequest?.addTarget(videoSurface)
           /*   prepareMediaCodec()
              try {
                  val mEncoderSurface = mCodec!!.createInputSurface()
                  mCodec!!.setInputSurface(mEncoderSurface)
                  captureRequest?.addTarget(mEncoderSurface)
                  configs.add(
                      OutputConfiguration(mEncoderSurface)
                  )
              } catch (e: java.lang.Exception) {
                  println()
              }*/


           // val mEncoderSurface = MediaCodec.createPersistentInputSurface()

           /////////////////////////////////
           //   configs.add(conf2)
           val config = SessionConfiguration(
               SessionConfiguration.SESSION_REGULAR,
               configs,
               Dispatchers.IO.asExecutor(),
               object : CameraCaptureSession.StateCallback() {
                   override fun onConfigured(session: CameraCaptureSession) {
                       try {
                           sessio = session
                           //session.stopRepeating()
                           startPreviewCaptureRequest()

                       } catch (e: CameraAccessException) {
                           e.printStackTrace()
                       }
                   }

                   override fun onConfigureFailed(session: CameraCaptureSession) {}
               }
           )
           cameraDevice?.createCaptureSession(config)
       }*/
    ////////////////////////////////////////////////
    ////////////////////////////////////////////////
    ////////////////////////////////////////////////
    fun singleCapture(enable: Boolean) {
        //   singleCapture = true
        // imageReader.setOnImageAvailableListener(imageAvailableListener, mBackgroundHandler)
        /*if (enable) {
            mMediaRecorder.start()
        } else {
            mMediaRecorder.stop()
        }*/


        /* if (enable) {
             mpses = startFFmpeg()
         } else {
             mpses?.cancel()
         }*/
        /* if (enable) {
             running = enable
             finished = !enable
             mpses = startFFmpeg()
             //startTimer()

         } else {
             running = false
             finished = true
             mpses?.cancel()
         }*/
        if (enable) {

            encoder.start(createFile("mp4"))
            captureRequest?.addTarget(videoSurface)
            captureRequest?.let {
                //sessio?.stopRepeating()
                sessio?.setRepeatingRequest(it.build(), captureCallback, mBackgroundHandler)
            }
        } else {
            encoder.stop()
            captureRequest?.removeTarget(videoSurface)
            captureRequest?.let {
                // sessio?.stopRepeating()
                sessio?.setRepeatingRequest(it.build(), captureCallback, mBackgroundHandler)
            }
        }

    }

    /////////////////////////////
    private fun startTimer() {

        Handler(Looper.getMainLooper()).postDelayed(
            {
                /*  running = false
                  finished = true*/
            },
            3000
        ) // 1000 milliseconds = 1 second
    }


    /* fun ffmpeg() {
         val outputFile = File("/storage/emulated/0/DCIM/output.mp4")
         if (outputFile.exists()) {
             outputFile.delete()
         }
         val outputFilePath = createFile("mp4")

         // Создаем pipe
         sink = PipedOutputStream()
         source = PipedInputStream(sink)



         val pipe1 = FFmpegKitConfig.registerNewFFmpegPipe(context)
         val command = "-f image2pipe -i $pipe1  -c:v libx264 $outputFilePath" // Пример команды
         FFmpegKit.executeAsync(command) {
             when {
                 ReturnCode.isSuccess(it.returnCode) -> {
                     println("FFMPEG SUCCESS :${it.state.name}")
                 }

                 ReturnCode.isCancel(it.returnCode) -> {
                     println("FFMPEG CANCELLED :${it.returnCode}")
                 }

                 it.returnCode.isValueError -> {
                     println("FFMPEG ERROR :${it.logs.last()}")
                 }
             }
         }

         // Инициализируем FFmpeg
         FFmpegKit.executeAsync(
             "-f rawvideo -pix_fmt yuv420p -s ${1920}x${1080} -r 30 -i " + pipe1 + " -c:v libx264 -f mp4 $outputFilePath",
             {
                 when {
                     ReturnCode.isSuccess(it.returnCode) -> {
                         println("FFMPEG SUCCESS :${it.state.name}")
                     }

                     ReturnCode.isCancel(it.returnCode) -> {
                         println("FFMPEG CANCELLED :${it.returnCode}")
                     }

                     it.returnCode.isValueError -> {
                         println("FFMPEG ERROR :${it.logs.last()}")
                     }
                 }
             },
             {
                 println("FFMEPG LOG :${it.message}")
             },
             {
                 println("FFMEPG STATS :${it.speed}")
             })
     }*/
    //////////////////////////////
    /*  private fun addJpegImage(byteArray: ByteArray) {
          bufferedOutputStream?.write(byteArray)
          bufferedOutputStream?.flush()
      }*/

    private fun copyImageToYuvImage(image: Image): YuvImage {
        val planes = image.planes
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val yData = ByteArray(ySize)
        val uData = ByteArray(uSize)
        val vData = ByteArray(vSize)

        yBuffer.get(yData)
        uBuffer.get(uData)
        vBuffer.get(vData)

        val yuvByteArray = ByteArray(ySize + uSize + vSize)
        System.arraycopy(yData, 0, yuvByteArray, 0, ySize)
        System.arraycopy(uData, 0, yuvByteArray, ySize, uSize)
        System.arraycopy(vData, 0, yuvByteArray, ySize + uSize, vSize)

        val yuvImage = YuvImage(
            yuvByteArray,
            ImageFormat.NV21,
            image.width,
            image.height,
            null
        )

        return yuvImage
    }

    fun yuv420ToBitmap(image: Image): Bitmap {
        val width = image.width
        val height = image.height
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer[nv21, 0, ySize]
        vBuffer[nv21, ySize, vSize]
        uBuffer[nv21, ySize + vSize, uSize]

        val yuvImage = YuvImage(nv21, NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        return bitmap
    }

    fun getYUVPlanes(image: Image): YUVPlanes {
        val width = image.width
        val height = image.height
        val (y, u, v) = extractYUVPlanes(image)
        val yRowStride = image.planes[0].rowStride
        val uRowStride = image.planes[1].rowStride
        val vRowStride = image.planes[2].rowStride
        val uPixelStride = image.planes[1].pixelStride
        val vPixelStride = image.planes[2].pixelStride
        return YUVPlanes(
            y,
            u,
            v,
            width,
            height,
            yRowStride,
            uRowStride,
            vRowStride,
            uPixelStride,
            vPixelStride
        )
        //return  combineYUVPlanes(YUVPlanes(y, u, v, width, height, yRowStride, uRowStride, vRowStride, uPixelStride, vPixelStride))

    }

    //8294400
    //4147199
    //4147199
    val capacity = 26_000_000
    var yBuffer: ByteBuffer = ByteBuffer.allocate(capacity)
    val uBuffer: ByteBuffer = ByteBuffer.allocate(capacity / 2)
    val vBuffer: ByteBuffer = ByteBuffer.allocate(capacity / 2)
    fun copyYUVPlanes(image: Image) {
        yBuffer.clear()
        yBuffer.put(image.planes[0].buffer)
        uBuffer.clear()
        uBuffer.put(image.planes[1].buffer)
        vBuffer.clear()
        vBuffer.put(image.planes[2].buffer)
    }

    fun getByteBufferYUVPlanes(image: Image): LightYUVPlanes {

        copyImage(image)
        // copyYUVPlanes(image)
        val yRowStride = image.planes[0].rowStride
        val uRowStride = image.planes[1].rowStride
        val vRowStride = image.planes[2].rowStride
        val uPixelStride = image.planes[1].pixelStride
        val vPixelStride = image.planes[2].pixelStride
        return LightYUVPlanes(
            image.width,
            image.height,
            yRowStride,
            uRowStride,
            vRowStride,
            uPixelStride,
            vPixelStride
        )
        //return  combineYUVPlanes(YUVPlanes(y, u, v, width, height, yRowStride, uRowStride, vRowStride, uPixelStride, vPixelStride))

    }

    fun extractYUVPlanes(image: Image): Triple<ByteArray, ByteArray, ByteArray> {
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        /* val ySize = yPlane.rowStride * image.height
         val uSize = uPlane.rowStride * (image.height / 2)-1
         val vSize = vPlane.rowStride * (image.height / 2)-1*/

        val ySize = yPlane.buffer.remaining()
        val uSize = uPlane.buffer.remaining()
        val vSize = vPlane.buffer.remaining()

        val yBuffer = ByteArray(ySize)
        val uBuffer = ByteArray(uSize)
        val vBuffer = ByteArray(vSize)

        yPlane.buffer.get(yBuffer)
        uPlane.buffer.get(uBuffer)
        vPlane.buffer.get(vBuffer)

        return Triple(yBuffer, uBuffer, vBuffer)
    }

    data class LightYUVPlanes(
        val width: Int,
        val height: Int,
        val yRowStride: Int,
        val uRowStride: Int,
        val vRowStride: Int,
        val uPixelStride: Int,
        val vPixelStride: Int
    )

    data class YUVPlanes(
        val y: ByteArray,
        val u: ByteArray,
        val v: ByteArray,
        val width: Int,
        val height: Int,
        val yRowStride: Int,
        val uRowStride: Int,
        val vRowStride: Int,
        val uPixelStride: Int,
        val vPixelStride: Int
    )

    var yuvPlanesBuffer: ByteBuffer = ByteBuffer.allocateDirect(capacity + capacity / 4)
    fun combineYUVPlanesToByteBuffer(yuvPlanes: LightYUVPlanes) {
        //   fps1.get("before")
        var uvPos = yuvPlanes.width * yuvPlanes.height
        val uvSize = yuvPlanes.width / 2 * yuvPlanes.height / 2

        // Копирование Y плоскости
        yuvPlanesBuffer.clear()
        yuvPlanesBuffer.put(yBuffer.array(), 0, uvPos)
        /*  for (i in 0 until yuvPlanes.height) {
              yuvPlanesBuffer.put(yBuffer.array(), i*yuvPlanes.yRowStride, yuvPlanes.width)
          }*/


        yuvPlanesBuffer.put(uBuffer.array(), 0, uvSize)
        yuvPlanesBuffer.put(vBuffer.array(), 0, uvSize)


        /*  val uv = ByteArray(yuvPlanes.width * yuvPlanes.height * 3 / 2)
          val uByteArray = ByteArray(uBuffer.remaining())
          uBuffer.get(uByteArray, 0, uBuffer.remaining())
          val vByteArray = ByteArray(vBuffer.remaining())
          vBuffer.get(uByteArray, 0, vBuffer.remaining())


          for (i in 0 until yuvPlanes.height / 2) {
              for (j in 0 until yuvPlanes.width / 2) {
                  uv[uvPos] = uByteArray[i * yuvPlanes.uRowStride + j * yuvPlanes.uPixelStride]
                  uvPos++
              }
          }
          for (i in 0 until yuvPlanes.height / 2) {
              for (j in 0 until yuvPlanes.width / 2) {
                  uv[uvPos] = vByteArray[i * yuvPlanes.uRowStride + j * yuvPlanes.uPixelStride]
                  uvPos++
              }
          }*/

        //  fps1.get("after")

    }

    fun combineYUVPlanes(yuvPlanes: YUVPlanes): ByteArray {
        fps1.get("before")
        /*  val yuv = ByteArray(yuvPlanes.width * yuvPlanes.height * 3 / 2)
          var yPos = 0
          var uvPos = yuvPlanes.width * yuvPlanes.height

          // Копирование Y плоскости
          for (i in 0 until yuvPlanes.height) {
              System.arraycopy(yuvPlanes.y, i * yuvPlanes.yRowStride, yuv, yPos, yuvPlanes.width)
              yPos += yuvPlanes.width
          }
          ///////////////////////
          for (i in 0 until yuvPlanes.height / 2) {
              for (j in 0 until yuvPlanes.width / 2) {
                  yuv[uvPos] = yuvPlanes.u[i * yuvPlanes.uRowStride + j * yuvPlanes.uPixelStride]
                  uvPos++
              }
          }
          for (i in 0 until yuvPlanes.height / 2) {
              for (j in 0 until yuvPlanes.width / 2) {
                  yuv[uvPos] = yuvPlanes.v[i * yuvPlanes.uRowStride + j * yuvPlanes.uPixelStride]
                  uvPos++
              }
          }*/
        //////////////////////////////fast
        /*  val yuv = ByteArray(yuvPlanes.width * yuvPlanes.height * 3 / 2)
           val yuvBuffer = ByteBuffer.wrap(yuv)
           var yPos = 0
           var uvPos = yuvPlanes.width * yuvPlanes.height

           // Копирование Y плоскости

               yuvBuffer.put(yuvPlanes.y, 0, yuvPlanes.width*yuvPlanes.height)

               yPos += yuvPlanes.width*yuvPlanes.height

           ///////////////////////
           for (i in 0 until yuvPlanes.height / 2) {
               for (j in 0 until yuvPlanes.width / 2 ) {
                   yuvBuffer.put(yuvPlanes.u[i * yuvPlanes.uRowStride + j * yuvPlanes.uPixelStride])
                   uvPos++
               }
           }
           for (i in 0 until yuvPlanes.height / 2) {
               for (j in 0 until yuvPlanes.width / 2 ) {
                   yuvBuffer.put(yuvPlanes.v[i * yuvPlanes.uRowStride + j * yuvPlanes.uPixelStride])
                   uvPos++
               }
           }*/
        ///////////////////////////////
        val yuv = ByteArray(yuvPlanes.width * yuvPlanes.height * 3 / 2)
        var yPos = 0
        var uvPos = yuvPlanes.width * yuvPlanes.height

// Копирование Y плоскости
        System.arraycopy(yuvPlanes.y, 0, yuv, 0, yuvPlanes.width * yuvPlanes.height)

// Копирование UV плоскости

        for (i in 0 until yuvPlanes.height / 2) {
            for (j in 0 until yuvPlanes.width / 2) {
                System.arraycopy(
                    yuvPlanes.u,
                    i * yuvPlanes.uRowStride + j * yuvPlanes.uPixelStride,
                    yuv,
                    uvPos,
                    1
                )
                uvPos++
            }
        }
        for (i in 0 until yuvPlanes.height / 2) {
            for (j in 0 until yuvPlanes.width / 2) {
                System.arraycopy(
                    yuvPlanes.v,
                    i * yuvPlanes.uRowStride + j * yuvPlanes.uPixelStride,
                    yuv,
                    uvPos,
                    1
                )
                uvPos++
            }
        }
        ///////////////////////////////
        fps1.get("after")
        ///////////////////////
        return yuv
    }

    fun imageToYUVPlanes(image: Image): ByteArray {
        fps1.get("before")

        val yuv = ByteArray(image.width * image.height * 3 / 2)
        var yPos = 0
        var uvPos = image.width * image.height

// Копирование Y плоскости
        image.planes[0].buffer.get(yuv, 0, uvPos)
        // System.arraycopy(image.planes[0].buffer, 0, yuv, 0, image.planes[0].buffer.remaining())

// Копирование UV плоскости

        for (i in 0 until image.height / 2) {
            for (j in 0 until image.width / 2) {
                image.planes[1].buffer.position(i * image.planes[1].rowStride + j * image.planes[1].pixelStride)
                image.planes[1].buffer.get(
                    yuv,
                    uvPos,
                    1
                )
                uvPos++
            }
        }
        for (i in 0 until image.height / 2) {
            for (j in 0 until image.width / 2) {
                image.planes[2].buffer.position(i * image.planes[2].rowStride + j * image.planes[2].pixelStride)
                image.planes[2].buffer.get(
                    yuv,
                    uvPos,
                    1
                )
                uvPos++
            }
        }
        ///////////////////////////////
        fps1.get("after")
        ///////////////////////
        return yuv
    }

    fun convertYUV420_888to420p(image: Image): ByteArray {

        ///////////////////////////
        val planes = image.planes
        val yPlane = planes[0]
        val uPlane = planes[1]
        val vPlane = planes[2]

        val yRowStride = image.planes[0].rowStride
        val yPixelStride = image.planes[0].pixelStride
        val uvRowStride = image.planes[1].rowStride
        val uvPixelStride = image.planes[1].pixelStride


        val ySize = yPlane.buffer.remaining()
        val uSize = uPlane.buffer.remaining()
        val vSize = vPlane.buffer.remaining()
        val totalSize = ySize + uSize + vSize


        val yuvByteArray = ByteArray(totalSize)
        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        ////////////////////nv21
        /*  val nv21 = ByteArray(ySize + uSize + vSize)

          yBuffer.get(nv21, 0, ySize)
          vBuffer.get(nv21, ySize, vSize)
          uBuffer.get(nv21, ySize + vSize, uSize)*/
        ////////////////////
        var offset = 0
        for (i in 0 until image.height) {
            yBuffer.position(i * yRowStride)
            yBuffer.get(yuvByteArray, offset, image.width)
            offset += image.width
        }
        /*   for (i in 0 until image.height / 2) {
               for (j in 0 until image.width ) {
                   uBuffer.position(j + i * uvRowStride)
                   uBuffer.get(yuvByteArray, offset, 1)
                   offset++

                   vBuffer.position(j + i * uvRowStride)
                   vBuffer.get(yuvByteArray, offset, 1)
                   offset++
               }
           }*/
        ////
        /* for (i in 0 until image.height / 2) {
             for (j in 0 until image.width/2 step uvPixelStride){
                 uBuffer.position(j+i * uvRowStride)
                 uBuffer.get(yuvByteArray, offset, 1)
                 offset++
             }


         }
         for (i in 0 until image.height / 2) {
             for (j in 0 until image.width/2 step uvPixelStride) {
                 vBuffer.position(j+i * uvRowStride)
                 vBuffer.get(yuvByteArray, offset, 1)
                 offset++
             }
         }*/
        for (i in 0 until image.height / 2) {
            vBuffer.position(i * uvRowStride)
            for (j in 0..image.width step uvPixelStride) {
                vBuffer.get(yuvByteArray, offset, 1)
                offset++
            }
        }
        for (i in 0 until image.height / 2) {
            uBuffer.position(i * uvRowStride)
            for (j in 0..image.width step uvPixelStride) {
                uBuffer.get(yuvByteArray, offset, 1)
                offset++
            }
        }

        ///////////////////////
        val yuvImage = YuvImage(yuvByteArray, ImageFormat.NV21, image.width, image.height, null)
        val outputStream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, outputStream)
        val jpegByteArray = outputStream.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.size)

///////////////////////////////////
        return yuvByteArray
    }

    fun convertYUV420888ToByteArray(image: Image): ByteArray {
        // Get the planes of the YUV_420_888 image
        val planes = image.planes

        // Check that the image has three planes (Y, U, and V)
        if (planes.size != 3) {
            throw RuntimeException("Expected 3 planes in YUV_420_888 image")
        }

        // Get the planes and their row strides and pixel strides
        val yPlane = planes[0]
        val uPlane = planes[1]
        val vPlane = planes[2]

        val yRowStride = yPlane.rowStride
        val uRowStride = uPlane.rowStride
        val vRowStride = vPlane.rowStride

        val yPixelStride = yPlane.pixelStride
        val uPixelStride = uPlane.pixelStride
        val vPixelStride = vPlane.pixelStride

        // Get the buffer sizes for each plane
        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        // Calculate the height of each plane
        val yHeight = yBuffer.limit() / yRowStride
        val uHeight = uBuffer.limit() / uRowStride

        // Calculate the total size of the YUV420p byte array
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val totalSize = ySize + (uSize / uPixelStride) + (vSize / vPixelStride)

        // Create a byte array to store the YUV420p data
        val yuvByteArray = ByteArray(totalSize)

        // Copy the Y plane data
        var offset = 0
        for (i in 0 until yHeight) {
            yBuffer.position(i * yRowStride)
            yBuffer.get(yuvByteArray, offset, yRowStride)
            offset += yRowStride
        }

        // Copy the U and V plane data, interleaved
        for (i in 0 until uHeight) {
            uBuffer.position(i * uRowStride)
            vBuffer.position(i * vRowStride)
            for (j in 0 until uRowStride / uPixelStride) {
                yuvByteArray[offset++] = uBuffer.get()
                yuvByteArray[offset++] = vBuffer.get()
            }
        }

        // Convert YUV to RGB
        val rgbByteArray = ByteArray(ySize * 3)
        var rgbOffset = 0
        for (i in 0 until yHeight) {
            for (j in 0 until yRowStride) {
                val y = yuvByteArray[i * yRowStride + j].toInt() and 0xFF
                val u = yuvByteArray[ySize + (i / 2) * uRowStride + (j / 2)].toInt() and 0xFF
                val v = yuvByteArray[ySize + (i / 2) * uRowStride + (j / 2) + 1].toInt() and 0xFF

                val r = (y + 1.13983 * (v - 128)).toInt()
                val g = (y - 0.39465 * (u - 128) - 0.58060 * (v - 128)).toInt()
                val b = (y + 2.03211 * (u - 128)).toInt()

                rgbByteArray[rgbOffset++] = r.toByte()
                rgbByteArray[rgbOffset++] = g.toByte()
                rgbByteArray[rgbOffset++] = b.toByte()
            }
        }

        return rgbByteArray
    }

    /*  private fun convertNV21toyuv420(yuvImage: YuvImage) {
          fps1.get("beforeConvert")
          val nv21Data = yuvImage.yuvData

          var output: ByteArray? = null
          if (output == null) {
              output = ByteArray(nv21Data.size)
          }

          val size = yuvImage.width * yuvImage.height
          val quarter = size / 4
          val v0 = size
          val u0 = v0 + quarter

          System.arraycopy(nv21Data, 0, output, 0, size) // Y is same

          for (i in 0 until yuvImage.height / 2) {
              for (j in 0 until yuvImage.width / 2) {
                  val uvIndex = size + 2 * (i * yuvImage.width / 2 + j)
                  output[v0 + i * yuvImage.width / 2 + j] = nv21Data[uvIndex] // For NV21, V first
                  output[u0 + i * yuvImage.width / 2 + j] =
                      nv21Data[uvIndex + 1] // For NV21, U second
              }
          }

          // Swap U and V planes
          for (i in v0 until u0) {
              val temp = output[i]
              output[i] = output[i + quarter]
              output[i + quarter] = temp
          }
          fps1.get("afterConvert")
          bufferedOutputStream?.write(output)
          bufferedOutputStream?.flush()
          // process?.outputStream?.write(output)
          fps1.get("afterWrite")

      }*/

    private fun addImage(image: Image) {
        /*val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val outputStream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, outputStream)

        val jpegArray = outputStream.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.size)

        // Now you have the Bitmap, you can use it as needed
        // ...


        process?.outputStream?.write(jpegArray) // Write the JPEG data to the output stream*/
        /////////////////
        /////////////////awesome working
        /*  val planes = image.planes
          val yuvData = ByteArray(image.width * image.height * 3 / 2)

          // Get the Y plane
          val yBuffer = planes[0].buffer
          val yStride = planes[0].rowStride
          val yPixelStride = planes[0].pixelStride // always 1 for Y plane

          // Get the U plane
          val uBuffer = planes[1].buffer
          val uStride = planes[1].rowStride
          val uPixelStride = planes[1].pixelStride

          // Get the V plane
          val vBuffer = planes[2].buffer
          val vStride = planes[2].rowStride
          val vPixelStride = planes[2].pixelStride

          // Copy Y plane
          var yBufferOffset = yBuffer.position()
          var off= 0
          while (yBufferOffset < yStride * image.height) {
              yuvData[off] = yBuffer.get(yBufferOffset)
              off++
              yBufferOffset++
          }

          // Copy U plane
          // Copy U plane
          var offset = yStride * image.height
          val uBufferOffset = uBuffer.position()
          for (i in 0 until image.height / 2) {
              for (j in 0 until image.width / 2) {
                  yuvData[offset] = uBuffer.get(uBufferOffset + i * uStride + j * uPixelStride)
                  offset++
              }
          }

  // Copy V plane
          for (i in 0 until image.height / 2) {
              for (j in 0 until image.width / 2) {
                  yuvData[offset] = vBuffer.get(uBufferOffset + i * vStride + j * vPixelStride)
                  offset++
              }
          }
              process?.outputStream?.write(yuvData)


          image.close()*/
        /////////////////end of awesome working
        //////////////////////
        val planes = image.planes
        val yuvData = ByteBuffer.allocateDirect(image.width * image.height * 3 / 2)
// Get the Y plane

        val yStride = planes[0].rowStride
        val yPixelStride = planes[0].pixelStride // always 1 for Y plane

        // Get the U plane
        val uStride = planes[1].rowStride
        val uPixelStride = planes[1].pixelStride

        // Get the V plane
        val vStride = planes[2].rowStride
        val vPixelStride = planes[2].pixelStride
// Copy Y plane
        val yBuffer = planes[0].buffer
        yBuffer.rewind()
        yuvData.put(yBuffer)

// Copy U plane
        val uBuffer = planes[1].buffer
        uBuffer.rewind()
        for (i in 0 until image.height / 2) {
            for (j in 0 until image.width / 2) {
                yuvData.put(uBuffer.get(uBuffer.position() + i * uStride + j * uPixelStride))
            }
        }

// Copy V plane
        val vBuffer = planes[2].buffer
        vBuffer.rewind()
        for (i in 0 until image.height / 2) {
            for (j in 0 until image.width / 2) {
                yuvData.put(vBuffer.get(vBuffer.position() + i * vStride + j * vPixelStride))
            }
        }

        process?.outputStream?.write(yuvData.array())

        image.close()
        ///////////////////////
        // image.close()
        ////////worked
        /* for (i in 0 until 3) {
             val buffer = image.planes[i].buffer
             val bytes =
                 ByteArray(buffer.remaining()) // makes byte array large enough to hold image
             buffer.get(bytes) // copies image from buffer to byte array
             process?.outputStream?.write(bytes)
         }*/
        //////////////////
        // image.close()
    }
/////////////////////////////////////////////


    private fun createFile(extension: String): File {
        val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
        val t = sdf.format(Date())

        // Get the DCIM directory
        val dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)

        // Create a new directory inside the DCIM directory if it doesn't exist
        val newDir = File(dcimDir, "FC")
        if (!newDir.exists()) {
            newDir.mkdirs()
        }

        // Create a new file inside the new directory
        val file = File(newDir, "test${t}.${extension}")
        file.createNewFile()

        return file
    }

    private var outPutByteBuffer: ByteBuffer? = null

    fun jpegByteArrayFrom(yuv420_888: Image): ByteArray {
        return YuvImage(yuv420_888.nv21ByteArray, NV21, yuv420_888.width, yuv420_888.height, null)
            .getJpegDataWithQuality(100)
    }


    private val Image.nv21ByteArray
        get() = ByteArray(width * height * 3 / 2).also {
            val vPlane = planes[2]
            val y = planes[0].buffer.apply { rewind() }
            val u = planes[1].buffer.apply { rewind() }
            val v = vPlane.buffer.apply { rewind() }
            y.get(it, 0, y.capacity()) // copy Y components
            if (vPlane.pixelStride == 2) {
                // Both of U and V are interleaved data, so copying V makes VU series but last U
                v.get(it, y.capacity(), v.capacity())
                it[it.size - 1] = u.get(u.capacity() - 1) // put last U
            } else { // vPlane.pixelStride == 1
                var offset = it.size - 1
                var i = v.capacity()
                while (i-- != 0) { // make VU interleaved data into ByteArray
                    it[offset - 0] = u[i]
                    it[offset - 1] = v[i]
                    offset -= 2
                }
            }
        }

    private fun YuvImage.getJpegDataWithQuality(quality: Int) =
        ByteArrayOutputStream().also {
            compressToJpeg(Rect(0, 0, width, height), quality, it)
        }.toByteArray()

    private fun YUV_420_888toNV21(image: Image): ByteArray {
        val nv21: ByteArray
        val yBuffer = image.planes[0].buffer
        val vuBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        nv21 = ByteArray(ySize + vuSize)

        yBuffer[nv21, 0, ySize]
        vuBuffer[nv21, ySize, vuSize]

        return nv21
    }

    private fun NV21toJPEG(nv21: ByteArray, width: Int, height: Int): ByteArray {
        val out = ByteArrayOutputStream()
        val yuv = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        yuv.compressToJpeg(Rect(0, 0, width, height), 100, out)
        return out.toByteArray()
    }

    fun recordVideo(enable: Boolean) {

    }

    class Fps {
        private var lastFrameTime: Long = 0
        fun start() {
            lastFrameTime = System.currentTimeMillis()
        }

        fun get(type: String) {
            ////////////////////////
            val currentTime = System.currentTimeMillis()
            val dif = currentTime - lastFrameTime
            if (lastFrameTime != 0L) {
                val fps = 1000.0 / (currentTime - lastFrameTime)
                val value = if (type.equals("fps")) {
                    fps
                } else {
                    dif
                }
                Log.e("FPS", "$type: $value")
            }
            lastFrameTime = currentTime
            /////////////////////////
        }
    }
}