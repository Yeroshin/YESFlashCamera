package com.yes.camera.data.repository

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.graphics.YuvImage
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.DngCreator
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.MeteringRectangle
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.RggbChannelVector
import android.hardware.camera2.params.SessionConfiguration
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.os.SystemClock
import android.os.VibrationEffect
import java.nio.ByteBuffer
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.Surface
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import com.yes.camera.data.repository.AutoExposure.getIsoPriorityWithClassicSteps
import com.yes.camera.data.repository.AutoExposure.getShutterPriorityWithClassicSteps
import com.yes.camera.domain.model.Characteristics
import com.yes.camera.utils.ImageComparator
import com.yes.shared.domain.Dimensions
import com.yes.shared.domain.ImgFormat
import com.yes.shared.utils.CameraThreadManager
import com.yes.shared.utils.FileNameGenerator
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Arrays
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class CameraRepository(
    private val cameraThreadManager: CameraThreadManager,
    private val context: Context,
    private val cameraManager: CameraManager,
    private val encoder: MediaEncoder
) {
    var cameraSession: CameraCaptureSession? = null
    internal lateinit var cameraDevice: CameraDevice
    internal lateinit var cameraCharacteristics: CameraCharacteristics
    internal var persistentBuilder: CaptureRequest.Builder? = null

    private lateinit var previewSurface: Surface
    private lateinit var histogramSurface: Surface
    private lateinit var captureSurfaceJpeg: Surface
    private lateinit var captureSurfaceRaw: Surface

    private val _characteristicsFlow = MutableStateFlow<Characteristics?>(null)
    fun subscribeCameraSettings(): StateFlow<Characteristics?> = _characteristicsFlow

    fun clearCapturedBitmap() {
        _characteristicsFlow.update { it?.copy(capturedBitmap = null) }
        lastCapturedBitmap = null
    }

    private val histogramDataBuffer = ByteArray(320 * 240)
    private val _histogramBufferFlow = MutableSharedFlow<ByteArray>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    fun subscribeHistogramBuffer(): Flow<ByteArray> = _histogramBufferFlow

    private lateinit var imageReaderHistogram: ImageReader
    private lateinit var imageReaderJpeg: ImageReader
    private lateinit var imageReaderRaw: ImageReader
    private lateinit var filePath: String

    private val listenerHistogram = ImageReader.OnImageAvailableListener { reader ->
        val image = reader.acquireLatestImage() ?: return@OnImageAvailableListener
        val plane = image.planes[0]
        val buffer = plane.buffer
        val rowStride = plane.rowStride
        val srcW = image.width
        val srcH = image.height

        for (y in 0 until 240) {
            val srcY = (y * srcH) / 240
            val srcRowOffset = srcY * rowStride
            val targetRowOffset = y * 320
            for (x in 0 until 320) {
                histogramDataBuffer[targetRowOffset + x] = buffer.get(srcRowOffset + (x * srcW / 320))
            }
        }
        _histogramBufferFlow.tryEmit(histogramDataBuffer)
        
        // Автоматический анализ гистограммы для детекции вспышки (порог разницы яркостей = 25.0)
        checkAndSaveFlashFrame(brightnessThreshold = 25.0)

        image.close()
    }

    private fun processCapturedBitmap(bitmap: Bitmap): Bitmap {
        return try {
            val orientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
            val lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
            val isFront = lensFacing == CameraCharacteristics.LENS_FACING_FRONT

            val matrix = Matrix().apply {
                postRotate(orientation.toFloat())
                if (isFront) {
                    postScale(-1f, 1f)
                }
            }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) {
                bitmap.recycle()
            }
            rotated
        } catch (e: Exception) {
            Log.e("CameraRepository", "Error rotating bitmap: ${e.message}")
            bitmap
        }
    }

    private fun jpegToBitmap(image: Image): Bitmap? {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        return processCapturedBitmap(bitmap)
    }

    private fun yuv420ToBitmap(image: Image): Bitmap? {
        return try {
            val yBuffer = image.planes[0].buffer
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
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
            val imageBytes = out.toByteArray()
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) ?: return null
            processCapturedBitmap(bitmap)
        } catch (e: Exception) {
            Log.e("CameraRepository", "Error converting YUV to Bitmap: ${e.message}")
            null
        }
    }

    private fun detachCaptureListeners() {
        try { imageReaderJpeg.setOnImageAvailableListener(null, null) } catch (_: Exception) {}
        try { imageReaderRaw.setOnImageAvailableListener(null, null) } catch (_: Exception) {}
    }

    private var lastJpegBytes: ByteArray? = null

    private val listenerJpeg = ImageReader.OnImageAvailableListener { reader ->
        reader.acquireLatestImage()?.let { image ->
            try {
                val bytes = if (image.format == ImageFormat.JPEG) {
                    val buffer = image.planes[0].buffer
                    val data = ByteArray(buffer.remaining())
                    buffer.get(data)
                    data
                } else if (image.format == ImageFormat.YUV_420_888) {
                    val out = ByteArrayOutputStream()
                    val yBuffer = image.planes[0].buffer
                    val uBuffer = image.planes[1].buffer
                    val vBuffer = image.planes[2].buffer
                    val ySize = yBuffer.remaining()
                    val uSize = uBuffer.remaining()
                    val vSize = vBuffer.remaining()
                    val nv21 = ByteArray(ySize + uSize + vSize)
                    yBuffer.get(nv21, 0, ySize)
                    vBuffer.get(nv21, ySize, vSize)
                    uBuffer.get(nv21, ySize + vSize, uSize)
                    YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null).compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
                    out.toByteArray()
                } else {
                    val buffer = image.planes[0].buffer
                    val data = ByteArray(buffer.remaining())
                    buffer.get(data)
                    data
                }

                lastJpegBytes = bytes
                val decodedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                image.close()

                val bitmap = if (decodedBitmap != null) processCapturedBitmap(decodedBitmap) else null

                if (bitmap != null) {
                    _characteristicsFlow.update { it?.copy(capturedBitmap = bitmap, isCaptureRequested = false) }
                }
            } catch (e: Exception) {
                Log.e("CameraRepository", "Error processing JPEG image: ${e.message}")
                image.close()
            } finally {
                try { imageReaderJpeg.setOnImageAvailableListener(null, null) } catch (_: Exception) {}
            }
        }
    }

    private var lastRawBytes: ByteArray? = null
    private var lastRawWidth: Int = 0
    private var lastRawHeight: Int = 0
    private var lastCaptureResult: TotalCaptureResult? = null

    private val listenerRaw = ImageReader.OnImageAvailableListener { reader ->
        reader.acquireNextImage()?.let { image ->
            try {
                val buffer = image.planes[0].buffer
                val data = ByteArray(buffer.remaining())
                buffer.get(data)
                lastRawBytes = data
                lastRawWidth = image.width
                lastRawHeight = image.height
            } catch (e: Exception) {
                Log.e("CameraRepository", "Error processing RAW image: ${e.message}")
            } finally {
                image.close()
                try { imageReaderRaw.setOnImageAvailableListener(null, null) } catch (_: Exception) {}
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun openCamera(backCamera: Boolean): StateFlow<Characteristics?> {
        val facing = if (backCamera) CameraCharacteristics.LENS_FACING_BACK else CameraCharacteristics.LENS_FACING_FRONT
        cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == facing
        }?.let { id ->
            cameraManager.openCamera(id, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    cameraCharacteristics = cameraManager.getCameraCharacteristics(camera.id)
                    _characteristicsFlow.value = getCameraCharacteristics(camera.id)
                }
                override fun onDisconnected(camera: CameraDevice) = camera.close()
                override fun onError(camera: CameraDevice, error: Int) = camera.close()
            }, cameraThreadManager.handler)
        }
        return _characteristicsFlow
    }

    fun closeCamera() {
        cameraSession?.close()
        cameraDevice.close()
        previewSurface.release()
        cameraSession = null
        persistentBuilder = null
        _characteristicsFlow.update { null }
    }

    private fun getCameraCharacteristics(id: String): Characteristics {
        val map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val allSizes = map?.getOutputSizes(ImageFormat.JPEG)
        val iso = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
        val exposure = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
        val minFocus = cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)
        val maxFocus = cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE)
        val awbModes = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES)
        val caps = cameraCharacteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
        val supportsRaw = caps?.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW) == true
        val allrSizes = map?.getOutputSizes(ImageFormat.RAW_SENSOR)
        return Characteristics(
            isoRange = iso?.let { IntRange(it.lower, it.upper) } ?: IntRange(0, 0),
            shutterRange = exposure?.let { LongRange(it.lower, it.upper) } ?: LongRange(0, 0),
            wbModeItems = awbModes,
            minFocusValue = minFocus ?: 0f,
            maxFocusValue = maxFocus ?: 0f,
            resolutionItems = allSizes?.map { Dimensions(it.width, it.height) } ?: emptyList(),
            resolution = Dimensions(0, 0),
            focusMode = CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE,
            supportsRaw = supportsRaw
        )
    }

    fun startSession(glSurfaceTexture: SurfaceTexture, characteristics: Characteristics) {
        filePath = characteristics.filePath
        previewSurface = Surface(glSurfaceTexture)
        imageReaderHistogram = ImageReader.newInstance(
          320,
          240,
          ImageFormat.YUV_420_888, 3
          ).apply { setOnImageAvailableListener(listenerHistogram, cameraThreadManager.handler) }
        histogramSurface = imageReaderHistogram.surface

        val width = if (characteristics.resolution.width > 0) characteristics.resolution.width else 1920
        val height = if (characteristics.resolution.height > 0) characteristics.resolution.height else 1080

        imageReaderJpeg = ImageReader.newInstance(
            width,
            height,
            ImageFormat.JPEG,
            3)
        captureSurfaceJpeg = imageReaderJpeg.surface

        val rawSize = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(ImageFormat.RAW_SENSOR)
            ?.maxByOrNull { it.width * it.height }
            ?: Size(width, height)

        imageReaderRaw = ImageReader.newInstance(
            rawSize.width,
            rawSize.height,
            ImageFormat.RAW_SENSOR,
            2)
        captureSurfaceRaw = imageReaderRaw.surface

        val capabilities = cameraCharacteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
        val supportsRaw = capabilities?.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW) == true

        val surfaces = mutableListOf(previewSurface, histogramSurface, captureSurfaceJpeg)
        if (supportsRaw) {
            surfaces.add(captureSurfaceRaw)
        }

        val configs = surfaces.map { OutputConfiguration(it) }
        val config = SessionConfiguration(
            SessionConfiguration.SESSION_REGULAR,
            configs,
            cameraThreadManager.executor,
            object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                cameraSession = session
                persistentBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                    addTarget(previewSurface)
                    addTarget(histogramSurface)
                    addTarget(captureSurfaceJpeg)
                    if (supportsRaw) {
                        addTarget(captureSurfaceRaw)
                    }
                    set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
                    val fullSensor = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)!!
                    set(CaptureRequest.CONTROL_AE_REGIONS, arrayOf(MeteringRectangle(fullSensor, 1000)))
                    set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE)
                    set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE)
                }
                lastCharacteristics = characteristics
                startPreviewCaptureRequest(characteristics)
            }
            override fun onConfigureFailed(session: CameraCaptureSession) {
                Log.e("CameraRepository", "Camera session configuration FAILED!")
            }
        })
        try {
            cameraDevice.createCaptureSession(config)
        } catch (e: Exception) {
            Log.e("CameraRepository", "Create capture session error: ${e.message}", e)
        }
    }

    private val myScope = CoroutineScope(SupervisorJob() + cameraThreadManager.dispatcher)
    internal lateinit var lastCharacteristics: Characteristics
    internal var appliedCharacteristics: Characteristics? = null
    
    private var AE = false
    private var af = false
    private var isAfLocked = false
    private var isWaitingForFocus = false
    internal var lastLockedFocusDistance: Float? = null
    
    private var lastAeUpdateMillis = 0L
    private val UPDATE_TOKEN = Any()

    fun startPreviewCaptureRequest(characteristics: Characteristics) {
        lastCharacteristics = characteristics
        cameraThreadManager.handler.removeCallbacksAndMessages(UPDATE_TOKEN)
        val action = Runnable { processPreviewCaptureRequest(characteristics) }
        if (Looper.myLooper() == cameraThreadManager.handler.looper) { 
            processPreviewCaptureRequest(characteristics) 
        } else { 
            cameraThreadManager.handler.postAtTime(action, UPDATE_TOKEN, SystemClock.uptimeMillis()) 
        }
    }

    private fun processPreviewCaptureRequest(characteristics: Characteristics) {
        val builder = persistentBuilder ?: return
        val session = cameraSession ?: return
        val hw = _characteristicsFlow.value
        val old = appliedCharacteristics

        try {
            // 1. БАЛАНС БЕЛОГО
            if (characteristics.wbValue != null) {
                builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF)
                builder.set(CaptureRequest.COLOR_CORRECTION_GAINS, convertTemperatureToRggb(characteristics.wbValue))
                builder.set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX)
            } else {
                builder.set(CaptureRequest.CONTROL_AWB_MODE, characteristics.wbMode ?: CaptureRequest.CONTROL_AWB_MODE_AUTO)
            }

            // 2. ЭКСПОЗИЦИЯ
            updateExposure(builder, characteristics, old, hw)

            // 3. ФОКУС
            updateFocus(builder, characteristics, old)

            // 4. METERING REGIONS
            if (characteristics.touchPoint != null) {
                val rect = meteringRectangle(characteristics.touchPoint)
                builder.set(CaptureRequest.CONTROL_AF_REGIONS, arrayOf(rect))
                builder.set(CaptureRequest.CONTROL_AE_REGIONS, arrayOf(rect))
            } else {
                val fullSensor = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
                if (fullSensor != null) {
                    builder.set(CaptureRequest.CONTROL_AE_REGIONS, arrayOf(MeteringRectangle(fullSensor, 1000)))
                }
            }

            appliedCharacteristics = characteristics

            val capabilities = cameraCharacteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
            val supportsRaw = capabilities?.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW) == true
            val needRaw = supportsRaw && (characteristics.imgFormat == ImgFormat.DNG || characteristics.imgFormat == ImgFormat.JPEGDNG)

            if (characteristics.isCaptureRequested) {
                imageReaderJpeg.setOnImageAvailableListener(listenerJpeg, cameraThreadManager.handler)
                if (needRaw) {
                    imageReaderRaw.setOnImageAvailableListener(listenerRaw, cameraThreadManager.handler)
                }

                builder.addTarget(captureSurfaceJpeg)
                if (needRaw) {
                    builder.addTarget(captureSurfaceRaw)
                }

                session.capture(builder.build(), object : CameraCaptureSession.CaptureCallback() {
                    override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                        super.onCaptureCompleted(session, request, result)
                        lastCaptureResult = result
                        try {
                            builder.removeTarget(captureSurfaceJpeg)
                            if (needRaw) builder.removeTarget(captureSurfaceRaw)
                        } catch (_: Exception) {}
                        detachCaptureListeners()
                    }
                    override fun onCaptureFailed(session: CameraCaptureSession, request: CaptureRequest, failure: CaptureFailure) {
                        super.onCaptureFailed(session, request, failure)
                        try {
                            builder.removeTarget(captureSurfaceJpeg)
                            if (needRaw) builder.removeTarget(captureSurfaceRaw)
                        } catch (_: Exception) {}
                        detachCaptureListeners()
                    }
                }, cameraThreadManager.handler)
            } else {
                try {
                    builder.removeTarget(captureSurfaceJpeg)
                    builder.removeTarget(captureSurfaceRaw)
                } catch (_: Exception) {}

                val modeChanged = (characteristics.isoValue == null) != (old?.isoValue == null) || 
                                  (characteristics.shutterValue == null) != (old?.shutterValue == null) || 
                                  old == null

                if (modeChanged) {
                    session.stopRepeating()
                    session.capture(builder.build(), repeatingCaptureCallback, cameraThreadManager.handler)
                }
                session.setRepeatingRequest(builder.build(), repeatingCaptureCallback, cameraThreadManager.handler)
            }
        } catch (e: Exception) {
            Log.e("CameraRepository", "processPreviewCaptureRequest error: ${e.message}", e)
        }
    }

    fun singleCapture(): Flow<Bitmap?> = flow {
        val deferred = CompletableDeferred<Bitmap?>()
        captureDeferred = deferred
        lastRawBytes = null
        lastCaptureResult = null

        val current = if (::lastCharacteristics.isInitialized) lastCharacteristics else (_characteristicsFlow.value ?: Characteristics())
        startPreviewCaptureRequest(current.copy(isCaptureRequested = true))

        val bitmap = deferred.await()
        lastCapturedBitmap = bitmap
        emit(bitmap)
    }

    /**
     * Отдельная функция сохранения изображений с переданными параметрами:
     * @param savePath Место сохранения (путь к директории/файлу)
     * @param imageFormat Формат изображения (JPEG, DNG или JPEGDNG - и то и другое)
     * @param resolution Разрешение снимка (Dimensions с шириной и высотой)
     */
    fun saveCapturedImage(
        savePath: String,
        imageFormat: ImgFormat,
        resolution: Dimensions
    ) {
        Log.d("CameraRepository", "saveCapturedImage -> path: $savePath, format: $imageFormat, resolution: ${resolution.width}x${resolution.height}")
        val jpegBytes = lastJpegBytes
        val rawBytes = lastRawBytes
        val rawWidth = lastRawWidth
        val rawHeight = lastRawHeight
        val capResult = lastCaptureResult
        lastJpegBytes = null
        lastRawBytes = null
        lastCaptureResult = null

        cameraThreadManager.ioExecutor.execute {
            val baseFileName = FileNameGenerator().generateFileName()

            when (imageFormat) {
                ImgFormat.JPEG -> {
                    if (jpegBytes != null) {
                        ImageSaver.saveBytes(context, jpegBytes, baseFileName, savePath)
                    }
                }
                ImgFormat.DNG -> {
                    if (rawBytes != null && capResult != null) {
                        ImageSaver.saveDngBytes(context, cameraCharacteristics, capResult, rawBytes, rawWidth, rawHeight, "${baseFileName}_DNG", savePath)
                    }
                }
                ImgFormat.JPEGDNG -> {
                    if (jpegBytes != null) {
                        ImageSaver.saveBytes(context, jpegBytes, baseFileName, savePath)
                    }
                    if (rawBytes != null && capResult != null) {
                        ImageSaver.saveDngBytes(context, cameraCharacteristics, capResult, rawBytes, rawWidth, rawHeight, "${baseFileName}_DNG", savePath)
                    }
                }
            }
            clearCapturedBitmap()
        }
    }

    private data class ExposureResult(val modeChanged: Boolean)

    private fun updateExposure(
        builder: CaptureRequest.Builder, 
        new: Characteristics, 
        old: Characteristics?, 
        hw: Characteristics?
    ): ExposureResult {
        val iso = new.isoValue
        val shutter = new.shutterValue
        
        val modeChanged = (iso == null) != (old?.isoValue == null) || 
                          (shutter == null) != (old?.shutterValue == null) || 
                          old == null

        when {
            iso == null && shutter == null -> {
                AE = true
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                builder.set(CaptureRequest.CONTROL_AE_LOCK, isWaitingForFocus)
            }
            iso != null && shutter != null -> {
                AE = false
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                val clampedIso = if (new.isoRange.first > 0 && new.isoRange.last > 0) iso.coerceIn(new.isoRange.first, new.isoRange.last) else iso
                val clampedShutter = if (new.shutterRange.first > 0 && new.shutterRange.last > 0) shutter.coerceIn(new.shutterRange.first, new.shutterRange.last) else shutter
                builder.set(CaptureRequest.SENSOR_SENSITIVITY, clampedIso)
                builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, clampedShutter)
                builder.set(CaptureRequest.SENSOR_FRAME_DURATION, maxOf(clampedShutter, 33_333_333L))
                builder.set(CaptureRequest.CONTROL_AE_LOCK, false)
            }
            iso != null -> {
                AE = false
                val meanLum = calculateMeanLuminance()
                val bS = hw?.actualShutter ?: 33_333_333L
                val bI = hw?.actualIso ?: 100
                val calcShutter = getShutterPriorityWithClassicSteps(null, cameraCharacteristics, bI, bS, iso, meanLum.toInt())
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                builder.set(CaptureRequest.SENSOR_SENSITIVITY, iso)
                builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, calcShutter)
                builder.set(CaptureRequest.SENSOR_FRAME_DURATION, maxOf(calcShutter, 33_333_333L))
                builder.set(CaptureRequest.CONTROL_AE_LOCK, false)
            }
            else -> { // shutter != null
                AE = false
                val meanLum = calculateMeanLuminance()
                val bS = hw?.actualShutter ?: 33_333_333L
                val bI = hw?.actualIso ?: 100
                val calcIso = getIsoPriorityWithClassicSteps(null, cameraCharacteristics, bI, bS, shutter!!, meanLum.toInt())
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                builder.set(CaptureRequest.SENSOR_SENSITIVITY, calcIso)
                builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, shutter)
                builder.set(CaptureRequest.SENSOR_FRAME_DURATION, maxOf(shutter, 33_333_333L))
                builder.set(CaptureRequest.CONTROL_AE_LOCK, false)
            }
        }
        return ExposureResult(modeChanged)
    }

    private fun calculateMeanLuminance(): Double {
        var sum = 0L
        for (i in histogramDataBuffer.indices step 8) {
            sum += histogramDataBuffer[i].toInt() and 0xFF
        }
        return if (histogramDataBuffer.isEmpty()) 128.0 else sum.toDouble() / (histogramDataBuffer.size / 8)
    }

    private var prevLum = 0.0
    private var currLum = 0.0
    private var nextLum = 0.0

    /**
     * Функция анализа гистограммы для обнаружения вспышки и сохранения кадра.
     * Если текущий кадр светлее предыдущего и следующего больше чем на заданный brightnessThreshold,
     * а также принудительно устанавливается безопасный диапазон выдержек (чтобы исключить половинную экспозицию),
     * то кадр автоматически сохраняется.
     */
    fun checkAndSaveFlashFrame(brightnessThreshold: Double, minSafeShutterNs: Long = 4_000_000L, maxSafeShutterNs: Long = 33_333_333L) {
        val currentMeanLum = calculateMeanLuminance()

        // Сдвиг скользящего окна по 3 кадрам [предыдущий, текущий, следующий]
        prevLum = currLum
        currLum = nextLum
        nextLum = currentMeanLum

        // Детекция пика (вспышки): текущий кадр ярче предыдущего и следующего на величину порога
        if (currLum > prevLum + brightnessThreshold && currLum > nextLum + brightnessThreshold) {
            // Принудительно задаем безопасный диапазон выдержек для предотвращения артефактов rolling shutter
            applyForcedShutterRange(minSafeShutterNs, maxSafeShutterNs)

            // Сохраняем кадр
            triggerFlashCapture()
        }
    }

    private fun applyForcedShutterRange(minNs: Long, maxNs: Long) {
        val builder = persistentBuilder ?: return
        val currentShutter = builder.get(CaptureRequest.SENSOR_EXPOSURE_TIME) ?: 10_000_000L
        val clampedShutter = currentShutter.coerceIn(minNs, maxNs)
        builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, clampedShutter)
        builder.set(CaptureRequest.SENSOR_FRAME_DURATION, maxOf(clampedShutter, 33_333_333L))
        try {
            cameraSession?.setRepeatingRequest(builder.build(), repeatingCaptureCallback, cameraThreadManager.handler)
        } catch (e: Exception) {
            Log.e("CameraRepository", "Forced shutter range error: ${e.message}")
        }
    }

    private fun triggerFlashCapture() {
        val session = cameraSession ?: return
        try {
            val captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                addTarget(imageReaderJpeg.surface)
            }
            session.capture(captureBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                    Log.d("CameraRepository", "Flash detected! Frame saved via ImageSaver.")
                }
            }, cameraThreadManager.handler)
        } catch (e: Exception) {
            Log.e("CameraRepository", "Flash capture error: ${e.message}")
        }
    }

    private fun updateFocus(builder: CaptureRequest.Builder, new: Characteristics, old: Characteristics?) {
        val isManual = new.focusValue != null && new.focusMode != -1 && new.focusMode != -2 && new.focusMode != -3
        val wasManual = old?.focusValue != null && old?.focusMode != -1 && old?.focusMode != -2 && old?.focusMode != -3
        val modeChanged = new.focusMode != old?.focusMode || isManual != wasManual

        if (new.focusMode == -2 || new.focusMode == -3) {
            if (modeChanged || new.focusValue != old?.focusValue) {
                isAfLocked = false; isWaitingForFocus = false; lastLockedFocusDistance = null
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
                builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, new.focusValue ?: 0f)
            }
        } else if (isManual) {
            if (modeChanged || new.focusValue != old?.focusValue) {
                isAfLocked = false; isWaitingForFocus = false; lastLockedFocusDistance = null
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
                builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, new.focusValue)
            }
        } else {
            val targetMode = new.focusMode ?: CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            if (targetMode == -1) { // TOUCH
                if (modeChanged && new.touchPoint == null) {
                    isAfLocked = false; isWaitingForFocus = false; lastLockedFocusDistance = null
                    builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
                }
                new.touchPoint?.let { point ->
                    val pointChanged = !Arrays.equals(point, old?.touchPoint)
                    val rect = meteringRectangle(point)
                    builder.set(CaptureRequest.CONTROL_AF_REGIONS, arrayOf(rect))

                    if (pointChanged || modeChanged) {
                        isAfLocked = false; isWaitingForFocus = true; lastLockedFocusDistance = null
                        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
                        _characteristicsFlow.update { it?.copy(isFocused = false) }
                        launchAfTrigger(builder, rect)
                    } else if (isAfLocked && lastLockedFocusDistance != null) {
                        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
                        builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, lastLockedFocusDistance)
                    }
                }
            } else if (modeChanged) {
                isAfLocked = false; isWaitingForFocus = false; lastLockedFocusDistance = null
                builder.set(CaptureRequest.CONTROL_AF_MODE, targetMode)
                builder.set(CaptureRequest.CONTROL_AF_REGIONS, null)
            }
        }
    }

    private fun launchAfTrigger(base: CaptureRequest.Builder, rect: MeteringRectangle) {
        val session = cameraSession ?: return
        try {
            val trigger = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL).apply {
                addTarget(previewSurface)
                set(CaptureRequest.CONTROL_AE_MODE, base.get(CaptureRequest.CONTROL_AE_MODE))
                set(CaptureRequest.SENSOR_SENSITIVITY, base.get(CaptureRequest.SENSOR_SENSITIVITY))
                set(CaptureRequest.SENSOR_EXPOSURE_TIME, base.get(CaptureRequest.SENSOR_EXPOSURE_TIME))
                set(CaptureRequest.CONTROL_AE_REGIONS, base.get(CaptureRequest.CONTROL_AE_REGIONS))
                set(CaptureRequest.CONTROL_AE_LOCK, true) 
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
                set(CaptureRequest.CONTROL_AF_REGIONS, arrayOf(rect))
                set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START)
            }
            af = true
            session.capture(trigger.build(), repeatingCaptureCallback, cameraThreadManager.handler)
        } catch (e: Exception) {}
    }

    private val repeatingCaptureCallback = object : CameraCaptureSession.CaptureCallback() {
        private var lastUIUpdate = 0L
        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
            val now = System.currentTimeMillis()
            val sIso = result.get(CaptureResult.SENSOR_SENSITIVITY) ?: 100
            val sExp = result.get(CaptureResult.SENSOR_EXPOSURE_TIME) ?: 10_000_000L
            val fDist = result.get(CaptureResult.LENS_FOCUS_DISTANCE) ?: 0f
            val wbG = result.get(CaptureResult.COLOR_CORRECTION_GAINS)

            val isPriorityMode = (lastCharacteristics.isoValue == null) != (lastCharacteristics.shutterValue == null)
            if (!isAfLocked && isPriorityMode && (now - lastAeUpdateMillis > 500)) {
                lastAeUpdateMillis = now
                startPreviewCaptureRequest(lastCharacteristics)
            }

            if (now - lastUIUpdate > 100) {
                lastUIUpdate = now
                val kelvin = wbG?.let { rgbToKelvin(it) } ?: 0
                val afState = result.get(CaptureResult.CONTROL_AF_STATE)
                val focused = afState == CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED
                _characteristicsFlow.update { current ->
                    val base = current ?: Characteristics(isoRange = IntRange(0, 0), shutterRange = LongRange(0, 0))
                    base.copy(
                        actualIso = sIso, 
                        actualShutter = sExp, 
                        actualFocusDistance = fDist, 
                        actualWbKelvin = kelvin,
                        isFocused = focused
                    )
                }
            }

            if (af) {
                val afState = result.get(CaptureResult.CONTROL_AF_STATE)
                if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                    af = false; isWaitingForFocus = false
                    if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED) {
                        isAfLocked = true
                        lastLockedFocusDistance = result.get(CaptureResult.LENS_FOCUS_DISTANCE)
                    }
                    handleFocusResult(afState == CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED)
                    startPreviewCaptureRequest(lastCharacteristics)
                }
            }
        }
    }

    private fun handleFocusResult(success: Boolean) {
        myScope.launch(Dispatchers.Main) { 
            if (success) {
                triggerVibration(context)
            }
            Toast.makeText(context, if (success) "FOCUSED" else "Not focused", Toast.LENGTH_SHORT).show() 
        }
    }

    @SuppressLint("MissingPermission")
    private fun triggerVibration(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(50)
                }
            }
        } catch (e: Exception) {
            Log.e("CameraRepository", "Vibration error: ${e.message}")
        }
    }

    private fun meteringRectangle(touchPoint: FloatArray): MeteringRectangle {
        val sensorSize = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)!!
        val orientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
        val coords = touchPoint.copyOf()
        Matrix().apply { postRotate(-orientation.toFloat(), 0.5f, 0.5f); postScale(sensorSize.width().toFloat(), sensorSize.height().toFloat()); mapPoints(coords) }
        val rect = Rect((coords[0] - 150).toInt().coerceIn(0, sensorSize.width()), (coords[1] - 150).toInt().coerceIn(0, sensorSize.height()), (coords[0] + 150).toInt().coerceIn(0, sensorSize.width()), (coords[1] + 150).toInt().coerceIn(0, sensorSize.height()))
        return MeteringRectangle(rect, MeteringRectangle.METERING_WEIGHT_MAX)
    }

    private fun rgbToKelvin(rgb: RggbChannelVector): Int {
        var minTemp = 1000f; var maxTemp = 40000f
        repeat(20) { val temp = (minTemp + maxTemp) / 2; val testRgb = kelvinToRgb(temp) ?: return@repeat; if ((testRgb.blue / testRgb.red) >= (rgb.blue / rgb.red)) maxTemp = temp else minTemp = temp }
        return ((minTemp + maxTemp) / 2f + 0.5f).toInt()
    }

    private fun convertTemperatureToRggb(kelvin: Int): RggbChannelVector {
        val t = kelvin / 100.0f
        val red = if (t <= 66) 255f else (329.698727446 * (t - 60.0).pow(-0.1332047592)).toFloat().coerceIn(0f, 255f)
        val green = if (t <= 66) (99.4708025861 * ln(t.toDouble()) - 161.1195681661).toFloat().coerceIn(0f, 255f) else (288.1221695283 * (t - 60.0).pow(-0.0755148492)).toFloat().coerceIn(0f, 255f)
        val blue = if (t >= 66) 255f else if (t <= 19) 0f else (138.5177312231 * ln(t.toDouble() - 10.0) - 305.0447927307).toFloat().coerceIn(0f, 255f)
        return RggbChannelVector((red / 255f) * 2f, green / 255f, green / 255f, (blue / 255f) * 2f)
    }

    private fun kelvinToRgb(kelvin: Float): RggbChannelVector? {
        val t = kelvin / 100.0
        val r = if (t < 66.0) 255.0 else (351.97690566805693 + 0.114206453484165 * (t - 55.0) - 40.25366309332127 * ln(t - 55.0)).coerceIn(0.0, 255.0)
        val g = if (t < 66.0) (-155.25485562709179 - 0.44596950469579133 * (t - 2.0) + 104.49216199393888 * ln(t - 2.0)).coerceIn(0.0, 255.0) else (325.4494125711974 + 0.07943456536662342 * (t - 50.0) - 28.0852963507957 * ln(t - 50.0)).coerceIn(0.0, 255.0)
        val b = if (t >= 66.0) 255.0 else if (t <= 20.0) 0.0 else (-254.76935184120902 + 0.8274096064007395 * (t - 10.0) + 115.67994401066147 * ln(t - 10.0)).coerceIn(0.0, 255.0)
        return RggbChannelVector(r.toFloat(), g.toFloat(), g.toFloat(), b.toFloat())
    }

    private var lastCapturedBitmap: Bitmap? = null
    private var captureDeferred: CompletableDeferred<Bitmap?>? = null



    fun saveCapturedImage(
        savePath: String,
        formatStr: String,
        width: Int,
        height: Int
    ) {
        val format = when (formatStr.lowercase()) {
            "dng", "raw" -> ImgFormat.DNG
            "jpeg+dng", "jpeg+raw", "jpegdng", "jpeg_dng" -> ImgFormat.JPEGDNG
            else -> ImgFormat.JPEG
        }
        saveCapturedImage(savePath, format, Dimensions(width, height))
    }
}

object ImageSaver {
    private fun Image.toNv21(): ByteArray {
        val y = planes[0].buffer.apply { rewind() }
        val u = planes[1].buffer.apply { rewind() }
        val v = planes[2].buffer.apply { rewind() }
        val out = ByteArray(width * height * 3 / 2); y.get(out, 0, y.capacity())
        if (planes[2].pixelStride == 2) { v.get(out, y.capacity(), v.capacity()); out[out.size - 1] = u.get(u.capacity() - 1) } 
        else { var offset = out.size - 1; for (i in v.capacity() - 1 downTo 0) { out[offset--] = u[i]; out[offset--] = v[i] } }
        return out
    }
    fun saveImage(context: Context, image: Image, name: String) {
        try {
            val jpeg = ByteArrayOutputStream().apply { YuvImage(image.toNv21(), ImageFormat.NV21, image.width, image.height, null).compressToJpeg(Rect(0, 0, image.width, image.height), 95, this) }.toByteArray()
            val values = ContentValues().apply { put(MediaStore.Images.Media.DISPLAY_NAME, "$name.jpg"); put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/FlashCamera") }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri -> context.contentResolver.openOutputStream(uri)?.use { it.write(jpeg) } }
        } finally { image.close() }
    }
    fun saveBitmap(context: Context, bitmap: Bitmap, name: String, savePath: String = "DCIM/FlashCamera") {
        try {
            val bytes = ByteArrayOutputStream().apply { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, this) }.toByteArray()
            if (savePath.startsWith("content://")) {
                try {
                    val uri = Uri.parse(savePath)
                    val docFile = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, uri)
                    if (docFile != null && docFile.canWrite()) {
                        val newFile = docFile.createFile("image/jpeg", "$name.jpg")
                        if (newFile != null) {
                            context.contentResolver.openOutputStream(newFile.uri)?.use { it.write(bytes) }
                            return
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ImageSaver", "TreeUri save bitmap error: ${e.message}")
                }
            }
            val relativePath = getRelativePath(savePath)
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$name.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            }
        } catch (e: Exception) {
            Log.e("ImageSaver", "Save bitmap error: ${e.message}")
        }
    }
    fun saveBytes(context: Context, bytes: ByteArray, name: String, savePath: String = "DCIM/FlashCamera") {
        try {
            if (savePath.startsWith("content://")) {
                try {
                    val uri = Uri.parse(savePath)
                    val docFile = DocumentFile.fromTreeUri(context, uri)
                    if (docFile != null && docFile.canWrite()) {
                        val newFile = docFile.createFile("image/jpeg", "$name.jpg")
                        if (newFile != null) {
                            context.contentResolver.openOutputStream(newFile.uri)?.use { it.write(bytes) }
                            return
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ImageSaver", "TreeUri save bytes error: ${e.message}")
                }
            }
            val relativePath = getRelativePath(savePath)
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$name.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            }
        } catch (e: Exception) {
            Log.e("ImageSaver", "Save bytes error: ${e.message}")
        }
    }
    fun saveDngBytes(context: Context, cameraCharacteristics: CameraCharacteristics, captureResult: TotalCaptureResult, rawBytes: ByteArray, width: Int, height: Int, name: String, savePath: String = "DCIM/FlashCamera") {
        try {
            val dngCreator = DngCreator(cameraCharacteristics, captureResult)
            val byteBuffer = ByteBuffer.wrap(rawBytes)
            val size = Size(width, height)

            if (savePath.startsWith("content://")) {
                try {
                    val uri = Uri.parse(savePath)
                    val docFile = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, uri)
                    if (docFile != null && docFile.canWrite()) {
                        val newFile = docFile.createFile("image/x-adobe-dng", "$name.dng")
                        if (newFile != null) {
                            context.contentResolver.openOutputStream(newFile.uri)?.use { outputStream ->
                                dngCreator.writeByteBuffer(outputStream, size, byteBuffer, 0L)
                            }
                            return
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ImageSaver", "TreeUri save DNG bytes error: ${e.message}")
                }
            }
            val relativePath = getRelativePath(savePath)
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$name.dng")
                put(MediaStore.Images.Media.MIME_TYPE, "image/x-adobe-dng")
                put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    dngCreator.writeByteBuffer(outputStream, size, byteBuffer, 0L)
                }
            }
        } catch (e: Exception) {
            Log.e("ImageSaver", "Save DNG bytes error: ${e.message}")
        }
    }

    fun saveDngBitmap(context: Context, bitmap: Bitmap, name: String, savePath: String = "DCIM/FlashCamera") {
        try {
            val bytes = ByteArrayOutputStream().apply { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, this) }.toByteArray()
            if (savePath.startsWith("content://")) {
                try {
                    val uri = Uri.parse(savePath)
                    val docFile = DocumentFile.fromTreeUri(context, uri)
                    if (docFile != null && docFile.canWrite()) {
                        val newFile = docFile.createFile("image/x-adobe-dng", "$name.dng")
                        if (newFile != null) {
                            context.contentResolver.openOutputStream(newFile.uri)?.use { it.write(bytes) }
                            return
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ImageSaver", "TreeUri save DNG bitmap error: ${e.message}")
                }
            }
            val relativePath = getRelativePath(savePath)
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$name.dng")
                put(MediaStore.Images.Media.MIME_TYPE, "image/x-adobe-dng")
                put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            }
        } catch (e: Exception) {
            Log.e("ImageSaver", "Save DNG bitmap error: ${e.message}")
        }
    }

    private fun getRelativePath(savePath: String): String {
        val cleanPath = savePath.trim()
        val externalStoragePath = Environment.getExternalStorageDirectory().absolutePath
        if (cleanPath.startsWith(externalStoragePath)) {
            val relative = cleanPath.removePrefix(externalStoragePath).removePrefix("/")
            if (relative.isNotEmpty()) return relative
        }
        if (cleanPath.startsWith("/storage/") || cleanPath.startsWith("/sdcard/")) {
            val dcimIndex = cleanPath.indexOf("DCIM/")
            val picturesIndex = cleanPath.indexOf("Pictures/")
            val index = if (dcimIndex != -1) dcimIndex else if (picturesIndex != -1) picturesIndex else -1
            if (index != -1) {
                return cleanPath.substring(index)
            }
        }
        return cleanPath.removePrefix("/").ifEmpty { "DCIM/FlashCamera" }
    }
}

object AutoExposure {
    private val SHUTTERS = listOf(125_000L, 250_000L, 500_000L, 1_000_000L, 2_000_000L, 4_000_000L, 8_000_000L, 16_666_666L, 33_333_333L, 66_666_666L, 125_000_000L, 250_000_000L, 500_000_000L, 1_000_000_000L)
    private val ISOS = listOf(50, 100, 200, 400, 800, 1600, 3200, 6400)
    
    fun getShutterPriorityWithClassicSteps(rb: CaptureRequest.Builder?, chars: CameraCharacteristics, aeIso: Int, aeExp: Long, targetIso: Int, currentLum: Int): Long {
        if (targetIso <= 0) return aeExp
        val lumFactor = if (currentLum > 0) 128.0 / currentLum.toDouble() else 1.0
        val ideal = (aeExp * (aeIso.toDouble() / targetIso.toDouble()) * lumFactor).toLong()
        
        val closest = SHUTTERS.minByOrNull { abs(ln(it.toDouble() / ideal)) } ?: ideal
        val range = chars.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
        return range?.let { closest.coerceIn(it.lower, it.upper) } ?: closest
    }
    
    fun getIsoPriorityWithClassicSteps(rb: CaptureRequest.Builder?, chars: CameraCharacteristics, aeIso: Int, aeExp: Long, targetExp: Long, currentLum: Int): Int {
        if (targetExp <= 0) return aeIso
        val lumFactor = if (currentLum > 0) 128.0 / currentLum.toDouble() else 1.0
        val ideal = (aeIso.toDouble() * (aeExp.toDouble() / targetExp.toDouble()) * lumFactor).toInt()
        
        val closest = ISOS.minByOrNull { abs(ln(it.toDouble() / ideal)) } ?: ideal
        val range = chars.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
        return range?.let { closest.coerceIn(it.lower, it.upper) } ?: closest
    }
}
