package com.yes.camera.data.repository

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
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
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.MeteringRectangle
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.RggbChannelVector
import android.hardware.camera2.params.SessionConfiguration
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.yes.camera.data.repository.AutoExposure.getIsoPriorityWithClassicSteps
import com.yes.camera.data.repository.AutoExposure.getShutterPriorityWithClassicSteps
import com.yes.camera.domain.model.Characteristics
import com.yes.camera.utils.ImageComparator
import com.yes.shared.domain.Dimensions
import com.yes.shared.utils.CameraThreadManager
import com.yes.shared.utils.FileNameGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private lateinit var cameraDevice: CameraDevice
    private lateinit var cameraCharacteristics: CameraCharacteristics
    private var persistentBuilder: CaptureRequest.Builder? = null

    private lateinit var previewSurface: Surface
    private lateinit var histogramSurface: Surface
    private lateinit var captureSurfaceJpeg: Surface
    private lateinit var captureSurfaceRaw: Surface

    private val _characteristicsFlow = MutableStateFlow<Characteristics?>(null)
    fun subscribeCameraSettings(): StateFlow<Characteristics?> = _characteristicsFlow

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
        image.close()
    }

    private val listenerJpeg = ImageReader.OnImageAvailableListener { reader ->
        reader.acquireLatestImage()?.let { image ->
            cameraThreadManager.ioExecutor.execute {
                ImageSaver.saveImage(context, image, FileNameGenerator().generateFileName())
            }
        }
    }

    private val listenerRaw = ImageReader.OnImageAvailableListener { it.acquireLatestImage()?.close() }

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

        return Characteristics(
            isoRange = iso?.let { IntRange(it.lower, it.upper) } ?: IntRange(0, 0),
            shutterRange = exposure?.let { LongRange(it.lower, it.upper) } ?: LongRange(0, 0),
            wbModeItems = awbModes,
            minFocusValue = minFocus ?: 0f,
            maxFocusValue = maxFocus ?: 0f,
            resolutionItems = allSizes?.map { Dimensions(it.width, it.height) } ?: emptyList(),
            resolution = Dimensions(0, 0),
            focusMode = CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE
        )
    }

    fun startSession(glSurfaceTexture: SurfaceTexture, characteristics: Characteristics) {
        filePath = characteristics.filePath
        previewSurface = Surface(glSurfaceTexture)
        imageReaderHistogram = ImageReader.newInstance(320, 240, ImageFormat.YUV_420_888, 3).apply { setOnImageAvailableListener(listenerHistogram, cameraThreadManager.handler) }
        histogramSurface = imageReaderHistogram.surface
        imageReaderJpeg = ImageReader.newInstance(characteristics.resolution.width, characteristics.resolution.height, ImageFormat.YUV_420_888, 3).apply { setOnImageAvailableListener(listenerJpeg, cameraThreadManager.handler) }
        captureSurfaceJpeg = imageReaderJpeg.surface
        imageReaderRaw = ImageReader.newInstance(characteristics.resolution.width, characteristics.resolution.height, ImageFormat.YUV_420_888, 3).apply { setOnImageAvailableListener(listenerRaw, cameraThreadManager.handler) }
        captureSurfaceRaw = imageReaderRaw.surface

        val configs = listOf(previewSurface, histogramSurface, captureSurfaceJpeg, captureSurfaceRaw).map { OutputConfiguration(it) }
        val config = SessionConfiguration(SessionConfiguration.SESSION_REGULAR, configs, cameraThreadManager.executor, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                cameraSession = session
                persistentBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL).apply {
                    addTarget(previewSurface)
                    addTarget(histogramSurface)
                    set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
                    // Глобальный замер AE на весь сенсор по умолчанию
                    val fullSensor = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)!!
                    set(CaptureRequest.CONTROL_AE_REGIONS, arrayOf(MeteringRectangle(fullSensor, 1000)))
                    set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE)
                    set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE)
                }
                lastCharacteristics = characteristics
                applyState(characteristics, forceFlush = true)
            }
            override fun onConfigureFailed(session: CameraCaptureSession) {}
        })
        cameraDevice.createCaptureSession(config)
    }

    private val myScope = CoroutineScope(SupervisorJob() + cameraThreadManager.dispatcher)
    private lateinit var lastCharacteristics: Characteristics
    private var appliedCharacteristics: Characteristics? = null
    
    private var AE = false
    private var af = false
    private var isAfLocked = false
    private var isWaitingForFocus = false
    private var lastLockedFocusDistance: Float? = null
    
    private var lastAeUpdateMillis = 0L
    private val UPDATE_TOKEN = Any()

    fun startPreviewCaptureRequest(characteristics: Characteristics) {
        lastCharacteristics = characteristics
        cameraThreadManager.handler.removeCallbacksAndMessages(UPDATE_TOKEN)
        val action = Runnable { applyState(characteristics, forceFlush = true) }
        if (Looper.myLooper() == cameraThreadManager.handler.looper) { applyState(characteristics, forceFlush = true) } 
        else { cameraThreadManager.handler.postAtTime(action, UPDATE_TOKEN, SystemClock.uptimeMillis()) }
    }

    private fun applyState(characteristics: Characteristics, forceFlush: Boolean = false) {
        val builder = persistentBuilder ?: return
        val session = cameraSession ?: return
        val old = appliedCharacteristics
        val hw = _characteristicsFlow.value

        try {
            // --- 1. БАЛАНС БЕЛОГО (Только если изменился) ---
            if (characteristics.wbValue != old?.wbValue || characteristics.wbMode != old?.wbMode) {
                if (characteristics.wbValue != null) {
                    builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF)
                    builder.set(CaptureRequest.COLOR_CORRECTION_GAINS, convertTemperatureToRggb(characteristics.wbValue))
                    builder.set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX)
                } else {
                    builder.set(CaptureRequest.CONTROL_AWB_MODE, characteristics.wbMode ?: CaptureRequest.CONTROL_AWB_MODE_AUTO)
                }
            }

            // --- 2. ЭКСПОЗИЦИЯ (Только если изменилась) ---
            var iso = characteristics.isoValue
            var shutter = characteristics.shutterValue
            val modeChanged = (iso == null) != (old?.isoValue == null) || (shutter == null) != (old?.shutterValue == null)

            if (iso != null || shutter != null) {
                if (iso != null && shutter == null) {
                    val bS = hw?.actualShutter ?: old?.shutterValue ?: 33_333_333L
                    val bI = hw?.actualIso ?: old?.isoValue ?: 100
                    shutter = getShutterPriorityWithClassicSteps(null, cameraCharacteristics, bI, bS, iso, 0)
                } else if (shutter != null && iso == null) {
                    val bS = hw?.actualShutter ?: old?.shutterValue ?: 33_333_333L
                    val bI = hw?.actualIso ?: old?.isoValue ?: 100
                    iso = getIsoPriorityWithClassicSteps(null, cameraCharacteristics, bI, bS, shutter, 0)
                }
                AE = false
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                builder.set(CaptureRequest.SENSOR_SENSITIVITY, iso ?: 100)
                builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, shutter ?: 10_000_000L)
                builder.set(CaptureRequest.SENSOR_FRAME_DURATION, maxOf(shutter ?: 33_333_333L, 33_333_333L))
                builder.set(CaptureRequest.CONTROL_AE_LOCK, false)
            } else if (modeChanged || old == null) {
                AE = true
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                builder.set(CaptureRequest.CONTROL_AE_LOCK, isWaitingForFocus)
            }

            // --- 3. ФОКУС (Жесткая изоляция от изменений ISO) ---
            val isManual = characteristics.focusValue != null && characteristics.focusMode != -1
            val afModeChanged = characteristics.focusMode != old?.focusMode || isManual != (old?.focusValue != null)

            if (isManual) {
                if (afModeChanged || characteristics.focusValue != old?.focusValue) {
                    isAfLocked = false; isWaitingForFocus = false; lastLockedFocusDistance = null
                    builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
                    builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, characteristics.focusValue)
                }
            } else {
                val mode = characteristics.focusMode ?: CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                if (mode == -1) { // TOUCH
                    characteristics.touchPoint?.let { point ->
                        val pointChanged = !Arrays.equals(point, old?.touchPoint)
                        if (pointChanged) {
                            isAfLocked = false; isWaitingForFocus = true; lastLockedFocusDistance = null
                            val rect = meteringRectangle(point)
                            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
                            builder.set(CaptureRequest.CONTROL_AF_REGIONS, arrayOf(rect))
                            launchAfTrigger(builder, rect)
                        } else if (isAfLocked && lastLockedFocusDistance != null) {
                            // Если точка та же и мы залочены - ПРИНУДИТЕЛЬНО держим замок
                            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
                            builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, lastLockedFocusDistance)
                        }
                    }
                } else if (afModeChanged) {
                    isAfLocked = false; isWaitingForFocus = false
                    builder.set(CaptureRequest.CONTROL_AF_MODE, mode)
                }
            }
            
            appliedCharacteristics = characteristics
            if (forceFlush && modeChanged) {
                session.stopRepeating()
                session.capture(builder.build(), repeatingCaptureCallback, cameraThreadManager.handler)
            }
            session.setRepeatingRequest(builder.build(), repeatingCaptureCallback, cameraThreadManager.handler)
            
        } catch (e: Exception) { Log.e("CameraRepository", "Apply error") }
    }

    private fun launchAfTrigger(base: CaptureRequest.Builder, rect: MeteringRectangle) {
        val session = cameraSession ?: return
        try {
            val trigger = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL).apply {
                addTarget(previewSurface)
                set(CaptureRequest.CONTROL_AE_MODE, base.get(CaptureRequest.CONTROL_AE_MODE))
                set(CaptureRequest.SENSOR_SENSITIVITY, base.get(CaptureRequest.SENSOR_SENSITIVITY))
                set(CaptureRequest.SENSOR_EXPOSURE_TIME, base.get(CaptureRequest.SENSOR_EXPOSURE_TIME))
                // ФИКС: Явно копируем регионы AE, чтобы яркость не прыгнула при тапе
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

            if (AE && (now - lastAeUpdateMillis > 500) && (lastCharacteristics.isoValue == null || lastCharacteristics.shutterValue == null)) {
                val tIso = lastCharacteristics.isoValue
                val tExp = lastCharacteristics.shutterValue
                when {
                    tIso != null && tExp == null -> {
                        val calc = getShutterPriorityWithClassicSteps(null, cameraCharacteristics, sIso, sExp, tIso, 0)
                        if (abs(calc - sExp) > calc * 0.1) { lastAeUpdateMillis = now; applyState(lastCharacteristics.copy(shutterValue = calc)) }
                    }
                    tExp != null && tIso == null -> {
                        val calc = getIsoPriorityWithClassicSteps(null, cameraCharacteristics, sIso, sExp, tExp, 0)
                        if (abs(calc - sIso) > calc * 0.1) { lastAeUpdateMillis = now; applyState(lastCharacteristics.copy(isoValue = calc)) }
                    }
                }
            }

            if (now - lastUIUpdate > 100) {
                lastUIUpdate = now
                val kelvin = wbG?.let { rgbToKelvin(it) } ?: 0
                _characteristicsFlow.update { current ->
                    val base = current ?: Characteristics(isoRange = IntRange(0, 0), shutterRange = LongRange(0, 0))
                    base.copy(
                        actualIso = sIso, actualShutter = sExp, actualFocusDistance = fDist, actualWbKelvin = kelvin,
                        focusMode = if (::lastCharacteristics.isInitialized) lastCharacteristics.focusMode else base.focusMode,
                        wbMode = if (::lastCharacteristics.isInitialized) lastCharacteristics.wbMode else base.wbMode,
                        touchPoint = if (::lastCharacteristics.isInitialized) lastCharacteristics.touchPoint else base.touchPoint
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
                    handleFocusResult(afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED)
                    // ПРИНУДИТЕЛЬНОЕ применение для фиксации AF_MODE_OFF
                    applyState(lastCharacteristics, forceFlush = false)
                }
            }
        }
    }

    fun singleCapture(enable: Boolean) {}

    private fun handleFocusResult(success: Boolean) {
        myScope.launch(Dispatchers.Main) { Toast.makeText(context, if (success) "FOCUSED" else "Not focused", Toast.LENGTH_SHORT).show() }
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
        return (minTemp + maxTemp).toInt() / 2
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
        val r = if (t < 66.0) 255.0 else (351.97690566805693 + 0.114206453784165 * (t - 55.0) - 40.25366309332127 * ln(t - 55.0)).coerceIn(0.0, 255.0)
        val g = if (t < 66.0) (-155.25485562709179 - 0.44596950469579133 * (t - 2.0) + 104.49216199393888 * ln(t - 2.0)).coerceIn(0.0, 255.0) else (325.4494125711974 + 0.07943456536662342 * (t - 50.0) - 28.0852963507957 * ln(t - 50.0)).coerceIn(0.0, 255.0)
        val b = if (t >= 66.0) 255.0 else if (t <= 20.0) 0.0 else (-254.76935184120902 + 0.8274096064007395 * (t - 10.0) + 115.67994401066147 * ln(t - 10.0)).coerceIn(0.0, 255.0)
        return RggbChannelVector(r.toFloat(), g.toFloat(), g.toFloat(), b.toFloat())
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
}

object AutoExposure {
    private val SHUTTERS = listOf(125_000L, 250_000L, 500_000L, 1_000_000L, 2_000_000L, 4_000_000L, 8_000_000L, 16_666_666L, 33_333_333L, 66_666_666L, 125_000_000L, 250_000_000L, 500_000_000L, 1_000_000_000L)
    private val ISOS = listOf(50, 100, 200, 400, 800, 1600, 3200, 6400)
    fun getShutterPriorityWithClassicSteps(rb: CaptureRequest.Builder?, chars: CameraCharacteristics, aeIso: Int, aeExp: Long, targetIso: Int, ev: Int): Long {
        val ideal = aeExp * (aeIso.toDouble() / targetIso.toDouble()); val closest = SHUTTERS.minByOrNull { abs(ln(it.toDouble() / ideal)) } ?: ideal.toLong()
        val range = chars.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE); return range?.let { closest.coerceIn(it.lower, it.upper) } ?: closest
    }
    fun getIsoPriorityWithClassicSteps(rb: CaptureRequest.Builder?, chars: CameraCharacteristics, aeIso: Int, aeExp: Long, targetExp: Long, ev: Int): Int {
        val ideal = aeIso.toDouble() * (aeExp.toDouble() / targetExp.toDouble()); val closest = ISOS.minByOrNull { abs(ln(it.toDouble() / ideal)) } ?: ideal.toInt()
        val range = chars.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE); return range?.let { closest.coerceIn(it.lower, it.upper) } ?: closest
    }
}
