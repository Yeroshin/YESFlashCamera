package com.yes.camera.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.ImageFormat.NV21
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
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.icu.text.SimpleDateFormat
import android.media.Image
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.Surface
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
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.channels.WritableByteChannel
import java.util.Date
import java.util.Locale
import java.util.concurrent.locks.ReentrantLock


class CameraRepository(
    private val context: Context,
    private val cameraManager: CameraManager,
    private val encoder:MediaEncoder
) {
    private val mBackgroundThread = HandlerThread("CameraThread").apply { start() }
    private val mBackgroundHandler: Handler= Handler(mBackgroundThread.looper)
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

                @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    //  previewCaptureBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    // setCharacteristics(51200)
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

    var characteristics: CameraCharacteristics? = null
    private fun getCameraCharacteristics(id: String): Characteristics {
        characteristics = cameraManager.getCameraCharacteristics(id)

        val config = characteristics?.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )

        // If image format is provided, use it to determine supported sizes; or else use target class
        // val allSizes = config?.getOutputSizes(ImageReader::class.java)
        val v = config?.getOutputSizes(ImageFormat.YUV_420_888)
        val t = config?.getOutputSizes(ImageFormat.RAW_SENSOR)
        val allSizes = config?.getOutputSizes(ImageFormat.JPEG)
        allSizes?.maxBy { it.height * it.width }
        val iso = characteristics?.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
        val exposure = characteristics?.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
        val minFocusDistance =
            characteristics?.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)
        val minFocus = characteristics?.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE)
        /////////////////
        val g =
            characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP_MAXIMUM_RESOLUTION)
        val map = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val sizes = map?.getOutputSizes(MediaRecorder::class.java)
        /////////////////
        try {

            val map = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val sizes: Array<Size> = map!!.getOutputSizes(MediaRecorder::class.java)
            for (size in sizes) {
                Log.d(
                    "Camera2",
                    ("Supported resolution: " + size.getWidth()).toString() + "x" + size.getHeight()
                )
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return Characteristics(
            isoValue = 0,
            isoRange = iso?.let { IntRange(it.lower, it.upper) } ?: IntRange(0, 0),
            shutterValue = 0,
            focusValue = 0F,
            minFocusValue = minFocusDistance ?: 0f,
            shutterRange = exposure?.let { LongRange(it.lower, it.upper) } ?: LongRange(0, 0),
            resolutions = allSizes?.map {
                Dimensions(
                    it.width, it.height
                )
            } ?: listOf(
                Dimensions(0, 0)
            )
        )
    }

    private var captureRequest: CaptureRequest.Builder? = null
    private var glSurfaceTexture: SurfaceTexture? = null

    fun tmpStartDefaultCaptureRequest(){

        //  previewCaptureBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequest?.set(
            CaptureRequest.CONTROL_AE_MODE,
            CaptureRequest.CONTROL_AE_MODE_OFF
        )
        //////////settings
        captureRequest?.set(
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
        )


        // previewCaptureBuilder?.set(CaptureRequest.LENS_FOCUS_DISTANCE, 0.2f)
        //  previewCaptureBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL)

        // previewCaptureBuilder?.set(CaptureRequest.CONTROL_ZOOM_RATIO, 10F)
        captureRequest?.set(CaptureRequest.SENSOR_SENSITIVITY, 100)
        captureRequest?.set(CaptureRequest.SENSOR_EXPOSURE_TIME, 33_333_333L)
        captureRequest?.let {
            sessio?.setRepeatingRequest(it.build(), captureCallback, mBackgroundHandler)
        }
    }

    fun setCharacteristics(characteristics: Characteristics) {
        /* previewCaptureBuilder =
             cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG)*/
        /*  captureRequest?.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF)


          captureRequest?.set(CaptureRequest.EDGE_MODE, CaptureRequest.EDGE_MODE_OFF)
          captureRequest?.set(
              CaptureRequest.NOISE_REDUCTION_MODE,
              CaptureRequest.NOISE_REDUCTION_MODE_OFF
          )
          captureRequest?.set(
              CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
              CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_OFF
          )*/


        captureRequest?.set(CaptureRequest.LENS_FOCUS_DISTANCE, characteristics.focusValue)
        //  previewCaptureBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL)

        // previewCaptureBuilder?.set(CaptureRequest.CONTROL_ZOOM_RATIO, 10F)
        captureRequest?.set(CaptureRequest.SENSOR_SENSITIVITY, characteristics.isoValue)
        captureRequest?.set(
            CaptureRequest.SENSOR_EXPOSURE_TIME,
            characteristics.shutterValue
        )
        ////////preview
        //   val surface = Surface(glSurfaceTexture)
        // previewCaptureBuilder?.addTarget(surface)

        ////////////////

        ///capture
        /*  val imageReader=ImageReader.newInstance(4096, 3072, ImageFormat.YUV_420_888, 1)
         // imageReader.setOnImageAvailableListener(imageAvailableListener, mBackgroundHandler)

          previewCaptureBuilder?.addTarget(imageReader.surface)*/


        // captureRequest?.addTarget(imageReader.surface)
        //////////////

        // cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler)
        // createCaptureSession()

        captureRequest?.let {
            // sessio?.stopRepeating()
            sessio?.setRepeatingRequest(it.build(), captureCallback, mBackgroundHandler)
        }

    }

    var sessio: CameraCaptureSession? = null
    private var captureResult: CaptureResult? = null
    var frameTime: Long = 0
    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)
            captureResult = result
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

    private val imageReaderHandlerThread = HandlerThread("ImageReaderThread").apply {
        priority = Thread.MAX_PRIORITY
        start()
    }
    private val imageReaderHandler = Handler(imageReaderHandlerThread.looper)

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
        if (running) {
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
        }
        //  fps1.get("fps")
        /////////////////

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

    private val previewSurface by lazy {
        Surface(glSurfaceTexture)
    }
    private val previewSurfaceConfiguration by lazy {
        OutputConfiguration(previewSurface).apply {
          //  enableSurfaceSharing()
        }
    }
    private val videoSurface by lazy {
        encoder.configure(640,480)
    }
    private val videoSurfaceConfiguration by lazy {
        OutputConfiguration(videoSurface).apply {
           // enableSurfaceSharing()
        }
    }

    //3840,2160
   //  val rWidth = 640; val rHeight = 480
  //  val rWidth = 4096; val rHeight = 3072

   //   val rWidth=1920; val rHeight=1080
     val rWidth = 3840; val rHeight = 2160
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private val imageReader =
        ImageReader.newInstance(rWidth, rHeight, ImageFormat.YUV_420_888, 30).apply {
            setOnImageAvailableListener(imageAvailableListener, imageReaderHandler)

        }
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private val imageReaderSurfaceConfiguration = OutputConfiguration(imageReader.surface).apply {
        enableSurfaceSharing()
    }


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

    private fun createVideoCaptureSession(){

    }
    private fun createCaptureSession() {
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
                        tmpStartDefaultCaptureRequest()

                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            }
        )
        cameraDevice?.createCaptureSession(config)
    }
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
                // sessio?.stopRepeating()
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