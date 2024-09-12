package com.yes.camera.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.DngCreator
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.icu.text.SimpleDateFormat
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
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.annotation.RequiresApi
import com.yes.camera.domain.model.Characteristics
import com.yes.camera.domain.model.Dimensions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.Date
import java.util.Locale


class CameraRepository(
    context: Context,
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

    private val _characteristicsFlow:MutableStateFlow<com.yes.camera.domain.model.Characteristics?> = MutableStateFlow(null)
    private val characteristicsFlow: StateFlow<com.yes.camera.domain.model.Characteristics?> = _characteristicsFlow
    fun openBackCamera(glSurfaceTexture: SurfaceTexture): StateFlow<com.yes.camera.domain.model.Characteristics?> {
        this.glSurfaceTexture=glSurfaceTexture
        getCameraByFacing(CameraCharacteristics.LENS_FACING_BACK)?.let {
            openCamera(
                it
            ) {camera->
                _characteristicsFlow.value=camera
            }
        }
        return characteristicsFlow
    }

    fun openFrontCamera(glSurfaceTexture: SurfaceTexture): StateFlow<com.yes.camera.domain.model.Characteristics?>  {
        this.glSurfaceTexture=glSurfaceTexture
        getCameraByFacing(CameraCharacteristics.LENS_FACING_FRONT)?.let {
            openCamera(
                it
            ) {camera->
                _characteristicsFlow.value=camera
            }
        }
        return characteristicsFlow
    }


    // private var onCameraOpened:(()->Unit)?=null
    @SuppressLint("MissingPermission")
    private fun openCamera(id: String, onCameraOpened: (characteristics: Characteristics) -> Unit) {
        // this.onCameraOpened = onCameraOpened

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
    var characteristics:CameraCharacteristics?=null
    private fun getCameraCharacteristics(id: String): Characteristics {
        characteristics = cameraManager.getCameraCharacteristics(id)

        val config = characteristics?.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )

        // If image format is provided, use it to determine supported sizes; or else use target class
       // val allSizes = config?.getOutputSizes(ImageReader::class.java)
        val t = config?.getOutputSizes(ImageFormat.RAW_SENSOR)
        val allSizes = config?.getOutputSizes(ImageFormat.JPEG)
        allSizes?.maxBy { it.height * it.width }
        val iso=characteristics?.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
        val exposure=characteristics?.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
        val minFocusDistance =
            characteristics?.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)
        val minFocus = characteristics?.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE)
        /////////////////
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
            isoRange = iso?.let{IntRange(it.lower,it.upper)}?: IntRange(0,0),
            shutterValue = 0,
            focusValue = 0F,
            minFocusValue = minFocusDistance?:0f,
            shutterRange = exposure?.let{LongRange(it.lower,it.upper)}?:LongRange(0,0),
            resolutions = allSizes?.map {
                Dimensions(
                    it.width,it.height
                )
            }?:listOf(
                Dimensions(0,0)
            )
        )
    }

    private var previewCaptureBuilder: CaptureRequest.Builder? = null
    private var glSurfaceTexture: SurfaceTexture?=null

    fun setCharacteristics(characteristics: Characteristics){
        previewCaptureBuilder?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);

        previewCaptureBuilder?.set(CaptureRequest.LENS_FOCUS_DISTANCE, characteristics.focusValue)
        //  previewCaptureBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL)

      //  previewCaptureBuilder?.set(CaptureRequest.CONTROL_ZOOM_RATIO, 10F)
        previewCaptureBuilder?.set(CaptureRequest.SENSOR_SENSITIVITY, characteristics.isoValue)
        previewCaptureBuilder?.set(CaptureRequest.SENSOR_EXPOSURE_TIME, characteristics.shutterValue)
        previewCaptureBuilder?.let {
            sessio?.setRepeatingRequest(it.build(), null, mBackgroundHandler)
        }

       // cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler)
       // createCaptureSession()

    }
var sessio: CameraCaptureSession?=null

    val captureCallback=object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun createCaptureSession() {
        val configs = mutableListOf<OutputConfiguration>()
        previewCaptureBuilder =cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        ////////preview
        val surface = Surface(glSurfaceTexture)
        previewCaptureBuilder?.addTarget(surface)
        configs.add(
            OutputConfiguration(surface)
        )
        ////////////////
        ////video


     /*   setUpMediaRecorder()
       // val recorderSurface= MediaCodec.createPersistentInputSurface();
        val recorderSurface = mMediaRecorder.surface
      //  mMediaRecorder.setInputSurface(recorderSurface)
   //  val conf2 = OutputConfiguration(recorderSurface)
        previewCaptureBuilder?.addTarget(recorderSurface)*/
        /////////////photo
        val imageReader=ImageReader.newInstance(640, 480, ImageFormat.JPEG, 1)
    //  imageReader=ImageReader.newInstance(4096, 3072, ImageFormat.RAW_SENSOR, 1)
        imageReader.setOnImageAvailableListener(imageAvailableListener, mBackgroundHandler)
        imageReader.let {
            previewCaptureBuilder?.addTarget(it.surface)
            configs.add(
                OutputConfiguration(it.surface)
            )
        }

        /////////////////media codec
     /*   prepareMediaCodec()

       // val mEncoderSurface =  mCodec!!.createInputSurface()

        val mEncoderSurface = MediaCodec.createPersistentInputSurface()
        mCodec!!.setInputSurface(mEncoderSurface)
      //  val mEncoderSurface=mCodec!!.createInputSurface()
        previewCaptureBuilder?.addTarget(mEncoderSurface)
        configs.add(
            OutputConfiguration(mEncoderSurface)
        )*/
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
                        previewCaptureBuilder?.let {
                            sessio?.setRepeatingRequest(
                                it.build(),
                                captureCallback,// cameraCaptureSessionCaptureCallback,
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
        val t= sdf.format(Date())
        val mCurrentFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "test${t}.mp4"
        )
      /*  if (isSupported()){
            //////////////////

            val profiles:CamcorderProfile = CamcorderProfile.get(
               0,
                  CamcorderProfile.QUALITY_HIGH)
            var highQualityProfile: CamcorderProfile? = null

           /* for (profile in profiles!!) {
                if (profile.quality == CamcorderProfile.QUALITY_HIGH) {
                    highQualityProfile = profile
                    break
                }
            }*/
            /////////////////
            val profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
            mMediaRecorder.setProfile(profile)
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mMediaRecorder.setVideoSize(1920,1080) // 480p
            mMediaRecorder.setVideoFrameRate(10)
            mMediaRecorder.setOutputFile(mCurrentFile.absolutePath)
            try {
                mMediaRecorder.prepare()
            } catch (e: Exception) {
                println()
            }
        }*/
        println()

      //  mMediaRecorder.prepare()
    }
    private fun createFile():File{
        val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
        val t= sdf.format(Date())
        return File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "test${t}.jpg"
        )
    }


    private fun prepareMediaCodec(){
       val supported=isSupported(MediaFormat.MIMETYPE_VIDEO_MPEG4)
        val mFile = createFile()
        try {
            outputStream = BufferedOutputStream(FileOutputStream(mFile))
            Log.i("Encoder", "outputStream initialized")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        val width = 640 // ширина видео
        val height = 480
        try {
            mCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        } catch (e: java.lang.Exception) {
            println()
        }
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 3840, 2160)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 45000000) // битрейт видео в bps (бит в секунду)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
        mCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)


        mCodec?.setCallback(EncoderCallback(
            outPutByteBuffer,
            mCodec,
        outputStream
        ))



    }
    private var mCodec: MediaCodec? = null // кодер
    var mEncoderSurface: Surface? = null // Surface как вход данных для кодера
    private var outputStream: BufferedOutputStream? = null
    private var outPutByteBuffer: ByteBuffer? = null
     class EncoderCallback (
         private var outPutByteBuffer: ByteBuffer?,
         private val mCodec: MediaCodec?,
         private val outputStream: BufferedOutputStream?
     ): MediaCodec.Callback() {
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
    private var singleCapture=false
    private var repeatingCapture=false
    private val imageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        val image = reader.acquireLatestImage()

        if(singleCapture){
           // val image = reader.acquireLatestImage()
            saveImage(image)
            if (!repeatingCapture){
                singleCapture=false
            }
           // image.close()
        }
        image?.close()


    }
   // private var imageReader:ImageReader? = null

    fun singleCapture(enable:Boolean){
        singleCapture=true
       // imageReader.setOnImageAvailableListener(imageAvailableListener, mBackgroundHandler)

    }
    fun repeatingCapture(){

    }
    private fun saveImage(image: Image) {
        when(image.format){
            ImageFormat.JPEG->{
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)

                var output: FileOutputStream? = null
                try {
                    output = FileOutputStream(
                        createFile()
                    )
                    output.write(bytes)
                } finally {
                    output?.close()
                }
            }
            ImageFormat.RAW_SENSOR->{}
        }
        ////////////////
       // val dngCreator=DngCreator()

        ////////////////


    }
   /* fun recordVideo(enable:Boolean){
        // setUpMediaRecorder()
        //   mMediaRecorder.prepare()
        if (enable){
            mCodec!!.start()
        }else{
            mCodec!!.stop()
        }

    }*/
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

    private fun isSupported(type:String): Boolean {
        val isOutputFormatSupported = isCodecSupported(type)
      //  val isVideoEncoderSupported = isCodecSupported("video/avc")
        return isOutputFormatSupported //&& isVideoEncoderSupported
    }
}