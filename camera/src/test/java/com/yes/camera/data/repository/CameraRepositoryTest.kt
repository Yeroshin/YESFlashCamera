package com.yes.camera.data.repository

import android.content.Context
import android.graphics.Rect
import android.hardware.camera2.*
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.util.Range
import com.yes.camera.domain.model.Characteristics
import com.yes.shared.utils.CameraThreadManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

class CameraRepositoryTest {

    @MockK
    lateinit var context: Context
    @MockK
    lateinit var cameraManager: CameraManager
    @MockK
    lateinit var cameraThreadManager: CameraThreadManager
    @MockK
    lateinit var encoder: MediaEncoder
    @MockK
    lateinit var cameraDevice: CameraDevice
    @MockK
    lateinit var cameraSession: CameraCaptureSession
    private lateinit var builder: CaptureRequest.Builder
    @MockK
    lateinit var characteristics: CameraCharacteristics

    private lateinit var repository: CameraRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        Dispatchers.setMain(testDispatcher)

        mockkStatic(Looper::class)
        mockkStatic(Log::class)
        mockkStatic(SystemClock::class)
        
        val mainLooper = mockk<Looper>()
        every { Looper.getMainLooper() } returns mainLooper
        every { Looper.myLooper() } returns mainLooper
        every { SystemClock.uptimeMillis() } returns 1000L
        
        every { Log.e(any<String>(), any<String>(), any()) } answers { println("ERROR: ${args[1]}"); 0 }
        every { Log.e(any<String>(), any<String>()) } answers { println("ERROR: ${args[1]}"); 0 }
        every { Log.w(any<String>(), any<String>()) } answers { println("WARN: ${args[1]}"); 0 }
        every { Log.d(any<String>(), any<String>()) } answers { println("DEBUG: ${args[1]}"); 0 }

        val handler = mockk<Handler>(relaxed = true)
        every { handler.looper } returns mainLooper
        every { handler.post(any()) } answers { (args[0] as Runnable).run(); true }
        every { handler.postAtTime(any(), any(), any<Long>()) } answers { (args[0] as Runnable).run(); true }
        
        every { cameraThreadManager.handler } returns handler
        every { cameraThreadManager.dispatcher } returns testDispatcher

        builder = mockk<CaptureRequest.Builder>(relaxed = true)
        every { builder.build() } returns mockk()

        repository = CameraRepository(cameraThreadManager, context, cameraManager, encoder)
        repository.cameraSession = cameraSession
        repository.persistentBuilder = builder
        repository.cameraDevice = cameraDevice
        repository.cameraCharacteristics = characteristics

        val mockExpRange = Range(100L, 1_000_000_000L)
        val mockIsoRange = Range(50, 6400)
        val mockRect = Rect(0, 0, 4000, 3000)

        // Умный мок характеристик: возвращает значения по смыслу вызова
        every { characteristics.get(any<CameraCharacteristics.Key<*>>()) } answers {
            val stack = Thread.currentThread().stackTrace
            val isAutoExposure = stack.any { it.className.contains("AutoExposure") }
            val isMetering = stack.any { it.methodName == "meteringRectangle" }
            
            when {
                isAutoExposure -> {
                    if (stack.any { it.methodName.contains("Shutter") }) mockExpRange else mockIsoRange
                }
                isMetering -> {
                    // Первым вызывается sensorSize (Rect), вторым orientation (Int)
                    if (meteringCounter++ % 2 == 0) mockRect else 90
                }
                else -> mockRect
            }
        }
        
        every { cameraSession.setRepeatingRequest(any(), any(), any()) } returns 1
        every { cameraSession.stopRepeating() } returns Unit
        every { cameraSession.capture(any(), any(), any()) } returns 1
        
        meteringCounter = 0
    }

    private var meteringCounter = 0

    @Test
    fun `changing ISO updates appliedCharacteristics`() {
        val initialChars = Characteristics(isoValue = 100)
        setInternalField(repository, "appliedCharacteristics", initialChars)
        
        val newChars = initialChars.copy(isoValue = 200)
        repository.startPreviewCaptureRequest(newChars)

        val applied = getInternalField(repository, "appliedCharacteristics") as Characteristics
        assert(applied.isoValue == 200)
    }

    @Test
    fun `focus lock remains stable during ISO change`() {
        // Given: Фокус заблокирован
        val initialChars = Characteristics(isoValue = 100, touchPoint = floatArrayOf(0.5f, 0.5f), focusMode = -1)
        setInternalField(repository, "appliedCharacteristics", initialChars)
        setInternalField(repository, "isAfLocked", true)
        setInternalField(repository, "lastLockedFocusDistance", 3.5f)
        
        // When: Меняем ISO
        val newChars = initialChars.copy(isoValue = 500)
        repository.startPreviewCaptureRequest(newChars)

        // Then: ISO обновилось, но импульс фокусировки (capture) не вызывался
        val applied = getInternalField(repository, "appliedCharacteristics") as Characteristics
        assert(applied.isoValue == 500)
        verify(exactly = 0) { cameraSession.capture(any(), any(), any()) }
    }

    @Test
    fun `new tap point resets lock and triggers AF refocus`() {
        // Given: Замок на старой точке
        val p1 = floatArrayOf(0.1f, 0.1f)
        val initialChars = Characteristics(isoValue = 100, touchPoint = p1, focusMode = -1)
        setInternalField(repository, "appliedCharacteristics", initialChars)
        setInternalField(repository, "isAfLocked", true)
        
        // When: Тапаем в НОВУЮ точку
        val p2 = floatArrayOf(0.9f, 0.9f)
        val newChars = initialChars.copy(touchPoint = p2)
        repository.startPreviewCaptureRequest(newChars)

        // Then: Замок сброшен, импульс отправлен
        val isAfLocked = getInternalField(repository, "isAfLocked") as Boolean
        assert(!isAfLocked)
        verify(exactly = 1) { cameraSession.capture(any(), any(), any()) }
    }

    private fun setInternalField(target: Any, fieldName: String, value: Any?) {
        val field = target.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(target, value)
    }

    private fun getInternalField(target: Any, fieldName: String): Any? {
        val field = target.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(target)
    }
}
