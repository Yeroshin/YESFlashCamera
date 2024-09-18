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
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.annotation.RequiresApi
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.ReturnCode
import com.yes.camera.domain.model.Characteristics
import com.yes.camera.domain.model.Dimensions
import com.yes.camera.utils.ImageComparator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.ByteBuffer
import java.nio.channels.Pipe
import java.util.Date
import java.util.Locale
import java.util.concurrent.LinkedBlockingQueue


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
    private val _event: MutableStateFlow<YuvImage?> = MutableStateFlow(null)
    private val event = _event
    val comparator = ImageComparator()

    //  var prevImage: Bitmap? = null
    var prevImage: YuvImage? = null

    init {
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
        CoroutineScope(Dispatchers.IO).launch {
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
        }
    }

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


    // private var onCameraOpened:(()->Unit)?=null

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

    var sessio: CameraCaptureSession? = null
    private var captureResult: CaptureResult? = null

    val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)
            captureResult = result
            val currentTime = System.currentTimeMillis()
            if (lastFrameTime != 0L) {
                val fps = 1000.0 / (currentTime - lastFrameTime)
                //    println("FPS: $fps")
            }
            lastFrameTime = currentTime
        }
    }


    val imageReaderHandlerThread = HandlerThread("ImageReaderThread").apply {
        priority = Thread.MAX_PRIORITY
        start()
    }
    val imageReaderHandler = Handler(imageReaderHandlerThread.looper)

    private val surface by lazy {
        Surface(glSurfaceTexture)
    }
    private val surfaceConfiguration by lazy {
        OutputConfiguration(surface).apply {
            enableSurfaceSharing()
        }
    }
    private var lastFrameTime: Long = 0


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
    var finished=false
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
                addImage(image)
                //processImage(it.planes[0].buffer)
                it.close()
            }
        } else if (finished){
            FFmpegKit.cancel()
            process?.outputStream?.flush()
            process?.outputStream?.close()
            finished=false
        }else{
            reader.acquireLatestImage()?.close()
        }

    }
    private fun addImage(image: Image) {
        val buffer = image.planes[0].buffer

        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        image.close()
        //   imageQueue.add(bytes)

        process?.outputStream?.write(bytes)

    }
    private lateinit var sink: PipedOutputStream
    private lateinit var source: PipedInputStream
    val thread = Thread {
        while (running) {
            // Ждем события ImageReader.OnImageAvailableListener
            Thread.sleep(10)
        }
        sink.close()
        source.close()
        FFmpegKit.cancel()
    }

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
    val imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.YUV_420_888, 10).apply {
        setOnImageAvailableListener(imageAvailableListener, imageReaderHandler)
    }

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
        captureRequest?.set(CaptureRequest.SENSOR_EXPOSURE_TIME, 66_666_667L)
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

        captureRequest?.addTarget(imageReader.surface)

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

    init {
        //   ffmpegThread.start()
        startFFmpeg()
        // Останавливаем поток через 10 секунд
      /*  Handler(Looper.getMainLooper()).postDelayed({
          //  running = false
          /*  if (ffmpegThread.isAlive) {
               // ffmpegThread.interrupt()
                running = false
            }*/
        }, 10000)*/
    }

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


    var pipe1: String? = null
    var process: Process? = null
    private fun startFFmpeg() {
        pipe1 = FFmpegKitConfig.registerNewFFmpegPipe(context)
        process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "cat > $pipe1"))
        val outputFilePath = createFile("mp4")
        val command =
            "-f rawvideo -pix_fmt yuv420p -s 1920x1080 -i $pipe1 -c:v mpeg4 -y $outputFilePath"
        FFmpegKit.executeAsync(command) { session ->

            val returnCode = session.returnCode
            if (returnCode.isValueSuccess) {
                println("sucess")
            } else {
                println("error")
            }
        }

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

    private fun createFile(extensionn: String): File {
        val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
        val t = sdf.format(Date())
        return File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "test${t}.${extensionn}"
        )
    }


    fun getMaxResolution(codecInfo: MediaCodecInfo, mimeType: String): Pair<Int, Int> {
        val capabilities = codecInfo.getCapabilitiesForType(mimeType)

        val videoCapabilities = capabilities.videoCapabilities
        val maxWidth = videoCapabilities.supportedWidths.upper
        val maxHeight = videoCapabilities.supportedHeights.upper
        return Pair(maxWidth, maxHeight)
    }

    fun main() {
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
        main()
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
    private var outputStream: BufferedOutputStream? = null
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

            running=enable
            finished=!enable

    }

    fun repeatingCapture() {

    }


    private fun saveImage(
        image: Image,
        cameraCharacteristics: CameraCharacteristics,
        captureResult: CaptureResult
    ) {
        // Toast.makeText(context,"capture",Toast.LENGTH_SHORT).show()
        //////////////////////////
        _event.value = YuvImage(image.nv21ByteArray, NV21, image.width, image.height, null)
        println()
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
}