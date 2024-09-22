package com.yes.camera.data.repository

import android.annotation.SuppressLint
import android.content.Context
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
import android.media.CamcorderProfile
import android.media.Image
import android.media.ImageReader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.annotation.RequiresApi
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.yes.camera.domain.model.Characteristics
import com.yes.camera.domain.model.Dimensions
import com.yes.camera.utils.ImageComparator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.Date
import java.util.Locale
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock


class CameraRepository(
    val context: Context,
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

    private val _characteristicsFlow: MutableStateFlow<Characteristics?> =
        MutableStateFlow(null)
    private val characteristicsFlow: StateFlow<Characteristics?> =
        _characteristicsFlow

    /*  private val _event: MutableStateFlow<Bitmap?> = MutableStateFlow(null)
      private val event = _event*/
    private val _event: MutableStateFlow<ByteArray?> = MutableStateFlow(null)
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

  //  var job: Job


    private var outputStream: BufferedOutputStream? = null
    init {
        startFFmpeg()
      /*  val scope = CoroutineScope(Dispatchers.IO)
        job = CoroutineScope(Dispatchers.IO).launch {
            event.collect { jpegImage ->
                jpegImage?.let {
                   // imageBuffer.addImage(it)
                   // addNVImage(it)
                    addJpegImage(it)
                }
            }
        }*/
    }

    var sessio: CameraCaptureSession? = null
    private var captureResult: CaptureResult? = null
    var frameTime: Long = 0
    val captureCallback = object : CameraCaptureSession.CaptureCallback() {
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


    private val surface by lazy {
        Surface(glSurfaceTexture)
    }
    private val surfaceConfiguration by lazy {
        OutputConfiguration(surface).apply {
            enableSurfaceSharing()
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


    private var pipe1: String? = null
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
    inner class WriterThread(private val imageBuffer: ImageBuffer) : Thread() {
        override fun run() {
            while (true) {
                val image = imageBuffer.getImage()
                if (image != null) {
                    try {
                        addNVImage(image)
                    } catch (e: IOException) {
                        Log.e("WriterThread", "Error writing to output stream: $e")
                    }
                } else {
                    // buffer is empty, exit thread
                    break
                }
            }
        }
    }
    private var previousFrameTime = 0L
    private val imageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        fps1.get("fps")
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
                val currentTime = System.nanoTime()
                val timeDiff = currentTime - previousFrameTime

                // if (timeDiff > 1_000_000_000 / 30) { // 1/30 second in nanoseconds
                frameCount++


                val fps: Double = 1 / (timeDiff / 1e9)
                Log.e("FPS", "captured")
                saveImage(image)
                //  addNVImage(image)
                previousFrameTime = currentTime
                //  }

                //processImage(it.planes[0].buffer)
                it.close()
            }
        } else if (finished) {
            /////////////////
            //job.cancel()
              process?.destroy()
              process?.waitFor()
              FFmpegKitConfig.closeFFmpegPipe(pipe1)
            ///////////////////
            /*  pipe1?.let {
                  FFmpegKitConfig.closeFFmpegPipe(it)
              }
            //  process?.waitFor()
              process?.destroy()*/
            //   FFmpegKit.cancel()
            // process?.outputStream?.flush()
            //  process?.outputStream?.close()
            finished = false
        } else {
            reader.acquireLatestImage()?.close()
        }
        /////////////////

    }
    val fps1=Fps()

    private var process: Process? = null
    val bufferedOutputStream = BufferedOutputStream(process?.outputStream)
    private fun addJpegImage(byteArray: ByteArray){
        bufferedOutputStream.write(byteArray)
        bufferedOutputStream.flush()
    }
    private fun addNVImage(yuvImage: YuvImage) {
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
        bufferedOutputStream.write(output)
        bufferedOutputStream.flush()
       // process?.outputStream?.write(output)
        fps1.get("afterWrite")

    }

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

    private val imageReaderHandlerThread = HandlerThread("ImageReaderThread").apply {
        priority = Thread.MAX_PRIORITY
        start()
    }
    private val imageReaderHandler = Handler(imageReaderHandlerThread.looper)
    private val imageReader =
        ImageReader.newInstance(640,480, ImageFormat.JPEG, 30).apply {
            setOnImageAvailableListener(imageAvailableListener, imageReaderHandler)

        }

    val mpegSurfaceTexture: SurfaceTexture = SurfaceTexture(0)
    val mpegSurface: Surface = Surface(mpegSurfaceTexture)
    private val mpegSurfaceConfiguration = OutputConfiguration(mpegSurface).apply {
        enableSurfaceSharing()
    }
    val mpegSurfaceTextureName=mpegSurfaceTexture.toString()
    private fun startFFmpeg() {

        pipe1 = FFmpegKitConfig.registerNewFFmpegPipe(context)
        process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "cat > $pipe1"))
        val outputFilePath = createFile("mp4")
        val framerate = 24//23.976
        val command =
            "-f android_camera -i $mpegSurfaceTexture -s 640x480  -c:v libx264 -preset ultrafast  -r 24 -b:v 1m -bufsize 100m -f mp4 -loglevel debug -y $outputFilePath -v debug -threads 2"//-loglevel info or -v warning

      //  val command = "-f rawvideo -vcodec rawvideo -pix_fmt yuv420p -s 3840x2160 -i $pipe1 -c:v libx264 -preset ultrafast  -r 24 -b:v 50m -bufsize 500m -f mp4 -loglevel debug -y $outputFilePath -v debug -threads 2"//-loglevel info or -v warning
        /*  val command = "-f rawvideo -pix_fmt yuv420p -s 1920x1080 -i $pipe1 -r 30 " +
                   "-vf yadif,minterpolate,fps=30,hqdn3d,scale=w=1920:h=1080,crop=w=1920:h=1080:x=0:y=0,format=yuv420p " +
                   "-c:v libx264 -crf 18 -y $outputFilePath -v error"*/
        /*  FFmpegKit.executeAsync(command) { session ->

              val returnCode = session.returnCode
              if (returnCode.isValueSuccess) {
                  println("sucess")
              } else {
                  println("error")
              }
          }*/
        FFmpegKit.executeAsync(command,
            { session ->
                val state = session.state
                val returnCode = session.returnCode

                if (returnCode.isValueSuccess) {
                    println("sucess")
                } else {
                    println("error")
                }
            }, { log ->
                println(log.message)
                // CALLED WHEN SESSION PRINTS LOGS
            }, { statistics ->
                println("frame number:" + statistics.videoFrameNumber)
                // CALLED WHEN SESSION GENERATES STATISTICS
            })


    }
    private fun saveImage(
        image: Image,
        // cameraCharacteristics: CameraCharacteristics,
        //  captureResult: CaptureResult
    ) {
        // Toast.makeText(context,"capture",Toast.LENGTH_SHORT).show()
        //////////////////////////
        val jpegBytes = ByteArray(image.planes[0].buffer.remaining())
        image.planes[0].buffer.get(jpegBytes)

        process?.outputStream?.write(jpegBytes)
        process?.outputStream?.flush()
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


    /* init {
         thread.start()

         // Останавливаем поток через 10 секунд
         Handler(Looper.getMainLooper()).postDelayed({
             if (thread.isAlive) {
                 thread.interrupt()
                 running = false
             }
         }, 10000)
     }*/
    val imageFormat = ImageFormat.YUV_420_888//ImageFormat.RAW_SENSOR//


    /*  private val imageReader: ImageReader = ImageReader.newInstance(1024, 768, imageFormat, 10).apply {
              setOnImageAvailableListener(imageAvailableListener, imageReaderHandler)
          }*/
    private val imageReaderSurfaceConfiguration = OutputConfiguration(imageReader.surface).apply {
        enableSurfaceSharing()
    }


    private fun createCaptureSession() {
        val configs = mutableListOf<OutputConfiguration>()
        captureRequest =
            cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG)
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
        captureRequest?.set(CaptureRequest.SENSOR_SENSITIVITY, 800)
        captureRequest?.set(CaptureRequest.SENSOR_EXPOSURE_TIME, 16_000_000L )
        ////////preview

        captureRequest?.addTarget(surface)

        configs.add(
            surfaceConfiguration
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


        configs.add(
            imageReaderSurfaceConfiguration
        )

      //  captureRequest?.addTarget(imageReader.surface)
        /////////////mpeg
        mpegSurfaceTexture.setDefaultBufferSize(640,480)//(1920,1080)//4096,3072//3840,2160

        configs.add(
            mpegSurfaceConfiguration
        )
        captureRequest?.addTarget(mpegSurface)

        /////////////////media codec
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
                        captureRequest?.let {
                            sessio?.setRepeatingRequest(
                                it.build(),
                                captureCallback,
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
    /////////////////////////////

    /*  val ffmpegThread = Thread {

          processImages()
      }*/


    private val imageQueue = LinkedBlockingQueue<ByteArray>()
    private fun processImages() {
        while (running) {
            imageQueue?.let {
                val imageData = it.take()
                process?.outputStream?.write(imageData)

            }
        }

        process?.outputStream?.flush()
        process?.outputStream?.close()
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

    private var mMediaRecorder: MediaRecorder = MediaRecorder(context)
    private fun setUpMediaRecorder() {
        // mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        /*   mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
           mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
           val mCurrentFile = File(
               Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
               "test.mp4"
           )
           mMediaRecorder.setOutputFile(mCurrentFile.absolutePath)
           val profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P)
           mMediaRecorder.setVideoSize(640, 480)
           mMediaRecorder.setVideoFrameRate(profile.videoFrameRate)
           mMediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight)
           mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate)
           mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)*/
        //  mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        //  mMediaRecorder.setAudioEncodingBitRate(profile.audioBitRate)
        //   mMediaRecorder.setAudioSamplingRate(profile.audioSampleRate)
        ///////////tmp

        //////////////////////
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val codecs = codecList.codecInfos
        for (codecInfo in codecs) {
            if (!codecInfo.isEncoder) continue
            val types = codecInfo.supportedTypes
            for (type in types) {
                Log.d("CodecInfo", "Encoder: ${codecInfo.name}, Type: $type")
            }
        }
        //////////////////////
        val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
        val t = sdf.format(Date())
        val mCurrentFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "test${t}.mp4"
        )
        if (isSupported("video/avc")) {
            //////////////////

            val profiles: CamcorderProfile = CamcorderProfile.get(
                0,
                CamcorderProfile.QUALITY_HIGH
            )
            var highQualityProfile: CamcorderProfile? = null

            /* for (profile in profiles!!) {
                 if (profile.quality == CamcorderProfile.QUALITY_HIGH) {
                     highQualityProfile = profile
                     break
                 }
             }*/
            /////////////////
            val profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
            //   mMediaRecorder.setProfile(profile)
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mMediaRecorder.setVideoSize(1920, 1080) // 480p
            mMediaRecorder.setVideoFrameRate(10)
            mMediaRecorder.setOutputFile(mCurrentFile.absolutePath)
            try {
                mMediaRecorder.prepare()
            } catch (e: Exception) {
                println()
            }
        }
        println()

        //  mMediaRecorder.prepare()
    }

    private fun createFile(extension: String): File {
        val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
        val t = sdf.format(Date())
        return File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "test${t}.${extension}"
        )
    }


    private fun getMaxResolution(codecInfo: MediaCodecInfo, mimeType: String): Pair<Int, Int> {
        val capabilities = codecInfo.getCapabilitiesForType(mimeType)

        val videoCapabilities = capabilities.videoCapabilities
        val maxWidth = videoCapabilities.supportedWidths.upper
        val maxHeight = videoCapabilities.supportedHeights.upper
        return Pair(maxWidth, maxHeight)
    }

    private fun getCodecs() {
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val codecs = codecList.codecInfos.filter { it.isEncoder }
        for (codec in codecs) {
            val mimeTypes = codec.supportedTypes
            for (mimeType in mimeTypes) {
                if (mimeType.startsWith("video/")) {
                    val (maxWidth, maxHeight) = getMaxResolution(codec, mimeType)
                    println("Codec: ${codec.name}, MIME Type: $mimeType, Max Resolution: ${maxWidth}x${maxHeight}")
                } else {
                    println("Codec: ${codec.name}, MIME Type: $mimeType")
                }
            }
        }
    }


    private fun prepareMediaCodec() {
        getCodecs()
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val codecInfos = codecList.codecInfos
        val supported = isSupported(MediaFormat.MIMETYPE_VIDEO_HEVC)
        val mFile = createFile("mp4")
        try {
            outputStream = BufferedOutputStream(FileOutputStream(mFile))
            Log.i("Encoder", "outputStream initialized")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }


        val width = 2048 // ширина видео 4096,3072// 3840 x2160//max 1920x1080
        val height = 2048
        try {
            mCodec = MediaCodec.createEncoderByType("video/avc")
        } catch (e: java.lang.Exception) {
            println()
        }
        val format = MediaFormat.createVideoFormat("video/avc", width, height)
        /////////////////////////////
        /*  var softwareCodec: MediaCodecInfo? = null
          for (codecInfo in codecInfos) {
              if (!codecInfo.isHardwareAccelerated && codecInfo.isEncoder && codecInfo.supportedTypes.contains(MediaFormat.MIMETYPE_VIDEO_HEVC)) {
                  softwareCodec = codecInfo
                  break
              }
          }

          softwareCodec?.let {
              val codec = MediaCodec.createByCodecName(it.name)
              codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
              codec.start()
          }*/
        ////////////////////////////
        format.setInteger(MediaFormat.KEY_BIT_RATE, 3000000) // битрейт видео в bps (бит в секунду)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
        try {
            mCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        } catch (e: java.lang.Exception) {
            println()
        }



        mCodec?.setCallback(
            EncoderCallback(
                outPutByteBuffer,
                mCodec,
                outputStream
            )
        )


    }

    private var mCodec: MediaCodec? = null // кодер
    var mEncoderSurface: Surface? = null // Surface как вход данных для кодера

    private var outPutByteBuffer: ByteBuffer? = null

    class EncoderCallback(
        private var outPutByteBuffer: ByteBuffer?,
        private val mCodec: MediaCodec?,
        private val outputStream: BufferedOutputStream?
    ) : MediaCodec.Callback() {
        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        }

        override fun onOutputBufferAvailable(
            codec: MediaCodec,
            index: Int,
            info: MediaCodec.BufferInfo
        ) {
            outPutByteBuffer = mCodec?.getOutputBuffer(index)
            val outDate = ByteArray(info.size)
            outPutByteBuffer?.get(outDate)


            try {
                outputStream?.write(outDate, 0, outDate.size) // гоним байты в поток
            } catch (e: IOException) {
                e.printStackTrace()
            }

            mCodec?.releaseOutputBuffer(index, false)
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            println()
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            println()
        }
    }

    private var singleCapture = false
    private var repeatingCapture = false

    // private var imageReader:ImageReader? = null

    fun singleCapture(enable: Boolean) {
        //   singleCapture = true
        // imageReader.setOnImageAvailableListener(imageAvailableListener, mBackgroundHandler)
        /*if (enable) {
            mMediaRecorder.start()
        } else {
            mMediaRecorder.stop()
        }*/

        running = enable
        finished = !enable


    }

    fun repeatingCapture() {

    }




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
        setUpMediaRecorder()
        //   mMediaRecorder.prepare()
        if (enable) {
            mCodec!!.start()
        } else {
            mCodec!!.stop()
        }

    }

    /* fun recordVideo(enable:Boolean){
        // setUpMediaRecorder()
      //   mMediaRecorder.prepare()
         if (enable){
             mMediaRecorder.start()
         }else{
             mMediaRecorder.stop()
         }

     }*/
    private fun isCodecSupported(mimeType: String?): Boolean {
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val codecInfos = codecList.codecInfos
        for (codecInfo in codecInfos) {
            if (!codecInfo.isEncoder) {
                continue
            }
            val supportedTypes = codecInfo.supportedTypes
            for (type in supportedTypes) {
                if (type.equals(mimeType, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }

    private fun isSupported(type: String): Boolean {
        val isOutputFormatSupported = isCodecSupported(type)
        //  val isVideoEncoderSupported = isCodecSupported("video/avc")
        return isOutputFormatSupported //&& isVideoEncoderSupported
    }
    class Fps{
        private var lastFrameTime: Long = 0
        fun get(tag:String) {
            ////////////////////////
            val currentTime = System.currentTimeMillis()
            if (lastFrameTime != 0L) {
                val fps = 1000.0 / (currentTime - lastFrameTime)
                Log.e("FPS", "$tag: $fps")
            }
            lastFrameTime = currentTime
            /////////////////////////
        }
    }
}