package com.yes.camera.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.icu.text.SimpleDateFormat
import android.media.ImageReader
import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresApi
import com.yes.camera.domain.model.Characteristics
import com.yes.camera.domain.model.Dimensions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.Date
import java.util.Locale


class CameraRepository(
    private val context: Context,
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

    private val _CharacteristicsFlow:MutableStateFlow<com.yes.camera.domain.model.Characteristics?> = MutableStateFlow(null)
    private val characteristicsFlow: StateFlow<com.yes.camera.domain.model.Characteristics?> = _CharacteristicsFlow
    fun openBackCamera(glSurfaceTexture: SurfaceTexture): StateFlow<com.yes.camera.domain.model.Characteristics?> {
        this.glSurfaceTexture=glSurfaceTexture
        getCameraByFacing(CameraCharacteristics.LENS_FACING_BACK)?.let {
            openCamera(
                it
            ) {camera->
                _CharacteristicsFlow.value=camera
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
                _CharacteristicsFlow.value=camera
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

    private fun getCameraCharacteristics(id: String): com.yes.camera.domain.model.Characteristics {
        val characteristics = cameraManager.getCameraCharacteristics(id)

        val config = characteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )

        // If image format is provided, use it to determine supported sizes; or else use target class
        val allSizes = config?.getOutputSizes(ImageReader::class.java)
        allSizes?.maxBy { it.height * it.width }
        val iso=characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
        val exposure=characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
        val minFocusDistance =
            characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)
        val minFocus = characteristics.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE)
        /////////////////
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val sizes = map?.getOutputSizes(MediaRecorder::class.java)
        /////////////////
        /*   characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.let {map->
               Arrays.sort(
                   map.getOutputSizes(ImageFormat.JPEG),
                   Collections.reverseOrder { lhs, rhs -> // Cast to ensure the multiplications won't overflow
                       java.lang.Long.signum((lhs.width.toLong() * lhs.height.toLong()) - (rhs.width.toLong() * rhs.height.toLong() ))
                   })
           }*/

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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun createCaptureSession() {

        val surface = Surface(glSurfaceTexture)
        previewCaptureBuilder =cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        previewCaptureBuilder?.addTarget(surface)
        ////video


        setUpMediaRecorder()
       // val recorderSurface= MediaCodec.createPersistentInputSurface();
        val recorderSurface = mMediaRecorder.surface
      //  mMediaRecorder.setInputSurface(recorderSurface)

        previewCaptureBuilder?.addTarget(recorderSurface)
        /////////////
        val configs = mutableListOf<OutputConfiguration>()
        val conf = OutputConfiguration(surface)
        val conf2 = OutputConfiguration(recorderSurface)
        configs.add(conf)
        configs.add(conf2)
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
                                null,// cameraCaptureSessionCaptureCallback,
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
        if (isSupported()){
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mMediaRecorder.setVideoSize(640,480) // 480p
            mMediaRecorder.setVideoFrameRate(30)
            mMediaRecorder.setOutputFile(mCurrentFile.absolutePath)
            try {
                mMediaRecorder.prepare()
            } catch (e: Exception) {
                println()
            }
        }

      //  mMediaRecorder.prepare()
    }
    suspend fun recordVideo(){
       // setUpMediaRecorder()
     //   mMediaRecorder.prepare()
        mMediaRecorder.start()
        delay(5000)
        mMediaRecorder.stop()
    }
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

    private fun isSupported(): Boolean {
        val isOutputFormatSupported = isCodecSupported("video/mp4v-es")
        val isVideoEncoderSupported = isCodecSupported("video/avc")
        return isOutputFormatSupported && isVideoEncoderSupported
    }
}