package com.yes.camera.presentation.mapper

import android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE
import android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_AUTO
import android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT
import android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT
import android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT
import android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT
import android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_SHADE
import android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_TWILIGHT
import android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT
import androidx.compose.ui.geometry.Offset
import com.yes.camera.R
import com.yes.camera.domain.model.Characteristics
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.camera.presentation.model.ModeItem
import com.yes.camera.presentation.model.RadioGroupItem
import com.yes.camera.presentation.model.SelectorItem
import com.yes.camera.presentation.model.SettingsItem
import com.yes.camera.utils.ResourceProvider
import com.yes.shared.domain.ImgFormat
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.abs

class MapperUI(
    private val resources: ResourceProvider
) {
    private val allStandardShutterSpeeds = mapOf(
        250_000L to "1/4000",
        500_000L to "1/2000",
        1_000_000L to "1/1000",
        2_000_000L to "1/500",
        4_000_000L to "1/250",
        8_000_000L to "1/125",
        16_000_000L to "1/60",
        33_333_333L to "1/30",
        66_666_666L to "1/15",
        125_000_000L to "1/8",
        250_000_000L to "1/4",
        500_000_000L to "1/2",
        1_000_000_000L to "1s",
        2_000_000_000L to "2s",
        4_000_000_000L to "4s"
    )

    private val allStandardIsoValues = listOf(50, 100, 200, 400, 800, 1600, 3200, 6400, 12800, 25600)
    private val standardWbValues = listOf(1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000)
    
    private fun getSupportedShutterSpeeds(range: LongRange): Map<Long, String> {
        if (range.first <= 0L || range.last <= 0L) return allStandardShutterSpeeds
        val filtered = allStandardShutterSpeeds.filter { (ns, _) -> ns in range }
        return if (filtered.isEmpty()) allStandardShutterSpeeds else filtered
    }

    private fun getSupportedIsoValues(range: IntRange): List<Int> {
        if (range.first <= 0 || range.last <= 0) return allStandardIsoValues
        val filtered = allStandardIsoValues.filter { iso -> iso in range }
        return if (filtered.isEmpty()) allStandardIsoValues else filtered
    }

    private fun generateFocusValues(min: Float, max: Float, step: Float): List<Float> {
        val size = ((max - min) / step).toInt() + 1
        return List(size) { i -> min + i * step }
    }
    
    private val standardMagnifierValues = listOf(1F, 2F, 3F, 4F, 5F, 6F, 7F, 8F, 9F, 10F)

    /**
     * Maps purely from Hardware/Domain reality.
     */
    fun map(characteristics: Characteristics): CharacteristicsUI {
        val standardShutterSpeeds = getSupportedShutterSpeeds(characteristics.shutterRange)
        val standardIsoValues = getSupportedIsoValues(characteristics.isoRange)

        val displayShutter = characteristics.shutterValue ?: characteristics.actualShutter
        val sortedShutters = standardShutterSpeeds.entries.sortedBy { it.key }
        val shutterValueStr = displayShutter?.let { s ->
            sortedShutters.minByOrNull { abs(it.key - s) }?.value
        } ?: "1/60"
        val shutterPosition = sortedShutters.map { it.value }.indexOf(shutterValueStr).coerceAtLeast(0)

        val displayIso = characteristics.isoValue ?: characteristics.actualIso
        val isoValue = displayIso?.let { i -> standardIsoValues.minByOrNull { abs(it - i) } }
        val isoPosition = isoValue?.let { standardIsoValues.indexOf(it) } ?: 0
        
        val displayWb = characteristics.wbValue ?: characteristics.actualWbKelvin
        val wbValueStr = displayWb?.let { "${it}K" } ?: "A"
        val wbPosition = displayWb?.let { w ->
            val closest = standardWbValues.minByOrNull { abs(it - w) }
            standardWbValues.indexOf(closest)
        } ?: 0

        val wbModeEnum = when (characteristics.wbMode) {
            CONTROL_AWB_MODE_AUTO -> ModeItem.WbItem.AUTO
            CONTROL_AWB_MODE_INCANDESCENT -> ModeItem.WbItem.INCANDESCENT
            CONTROL_AWB_MODE_FLUORESCENT -> ModeItem.WbItem.FLUORESCENT
            CONTROL_AWB_MODE_WARM_FLUORESCENT -> ModeItem.WbItem.WARM_FLUORESCENT
            CONTROL_AWB_MODE_DAYLIGHT -> ModeItem.WbItem.DAYLIGHT
            CONTROL_AWB_MODE_CLOUDY_DAYLIGHT -> ModeItem.WbItem.CLOUDY_DAYLIGHT
            CONTROL_AWB_MODE_TWILIGHT -> ModeItem.WbItem.TWILIGHT
            CONTROL_AWB_MODE_SHADE -> ModeItem.WbItem.SHADE
            else -> ModeItem.WbItem.AUTO
        }
        val isWbAuto = characteristics.wbValue == null

        val focusValues = generateFocusValues(characteristics.maxFocusValue, characteristics.minFocusValue, 1f)
        val displayFocus = characteristics.focusValue ?: characteristics.actualFocusDistance
        val focusPosition = displayFocus?.let { f ->
            val closest = focusValues.minByOrNull { abs(it - f) }
            focusValues.indexOf(closest)
        } ?: 0

        val focusModeEnum = when (characteristics.focusMode) {
            -2 -> ModeItem.FocusItem.MACRO
            -3 -> ModeItem.FocusItem.INFINITE
            -1 -> ModeItem.FocusItem.TOUCH
            CONTROL_AF_MODE_CONTINUOUS_PICTURE -> ModeItem.FocusItem.CONTINUOUS
            else -> ModeItem.FocusItem.CONTINUOUS
        }
        val focusModeStr = when (focusModeEnum) {
            ModeItem.FocusItem.MACRO -> "MACRO"
            ModeItem.FocusItem.CONTINUOUS -> "CONT"
            ModeItem.FocusItem.TOUCH -> "TOUCH"
            ModeItem.FocusItem.INFINITE -> "INF"
        }
        val isFocusAuto = characteristics.focusValue == null || characteristics.focusMode == -2 || characteristics.focusMode == -3 || characteristics.focusMode == CONTROL_AF_MODE_CONTINUOUS_PICTURE || characteristics.focusMode == -1
        val focusDisplayStr = if (isFocusAuto) {
            focusModeStr
        } else {
            displayFocus?.toString() ?: ""
        }

        return CharacteristicsUI(
            shutterValue = shutterValueStr,
            shutterPosition = shutterPosition,
            isoValue = isoValue?.toString() ?: "",
            isoPosition = isoPosition,
            wbValue = wbValueStr,
            wbPosition = wbPosition,
            focusValue = displayFocus?.toString() ?: "",
            focusPosition = focusPosition,
            fullScreen = characteristics.fullscreen ?: true,
            resolution = "${characteristics.resolution.width}x${characteristics.resolution.height}",
            aspectRatio = characteristics.resolution,
            imgFormat = when (characteristics.imgFormat) {
                ImgFormat.JPEG -> "JPEG"
                ImgFormat.DNG -> "DNG"
                ImgFormat.JPEGDNG -> "JPEG+DNG"
            },
            touchPoint = characteristics.touchPoint?.let {
                if (it.size >= 2) Offset(it[0], it[1]) else null
            } ?: Offset(0.5f, 0.5f),
            isFocused = characteristics.isFocused,
            capturedBitmap = characteristics.capturedBitmap,
            isCaptureRequested = characteristics.isCaptureRequested,

            isShutterAuto = characteristics.shutterValue == null,
            isIsoAuto = characteristics.isoValue == null,
            isWbAuto = isWbAuto,
            isFocusAuto = isFocusAuto,

            characteristicsItems = persistentListOf(
                RadioGroupItem.TextItem(SettingsItem.SHUTTER, "SHUTTER", shutterValueStr),
                RadioGroupItem.TextItem(SettingsItem.ISO, "ISO", isoValue?.toString() ?: characteristics.actualIso?.toString() ?: ""),
                RadioGroupItem.TextItem(SettingsItem.WB, "WB", wbValueStr),
                if (isFocusAuto) {
                    RadioGroupItem.IconItem(SettingsItem.FOCUS, "FOCUS", getFocusIconRes(focusModeEnum))
                } else {
                    RadioGroupItem.TextItem(SettingsItem.FOCUS, "FOCUS", focusDisplayStr)
                },
                RadioGroupItem.TextItem(SettingsItem.MAGNIFIER, "MAGNIFIER", "1")
            ),
            shutterItems = standardShutterSpeeds.entries.sortedBy { it.key }.mapIndexed { i, entry -> SelectorItem(i, entry.value) }.toImmutableList(),
            isoItems = standardIsoValues.mapIndexed { i, v -> SelectorItem(i, v.toString()) }.toImmutableList(),
            wbItems = standardWbValues.mapIndexed { i, v -> SelectorItem(i, "${v}K") }.toImmutableList(),
            wbModeItems = characteristics.wbModeItems?.map { item ->
                RadioGroupItem.IconItem(
                    id = when (item) {
                        CONTROL_AWB_MODE_AUTO -> ModeItem.WbItem.AUTO
                        CONTROL_AWB_MODE_INCANDESCENT -> ModeItem.WbItem.INCANDESCENT
                        CONTROL_AWB_MODE_FLUORESCENT -> ModeItem.WbItem.FLUORESCENT
                        CONTROL_AWB_MODE_WARM_FLUORESCENT -> ModeItem.WbItem.WARM_FLUORESCENT
                        CONTROL_AWB_MODE_DAYLIGHT -> ModeItem.WbItem.DAYLIGHT
                        CONTROL_AWB_MODE_CLOUDY_DAYLIGHT -> ModeItem.WbItem.CLOUDY_DAYLIGHT
                        CONTROL_AWB_MODE_TWILIGHT -> ModeItem.WbItem.TWILIGHT
                        CONTROL_AWB_MODE_SHADE -> ModeItem.WbItem.SHADE
                        else -> ModeItem.WbItem.AUTO
                    },
                    iconRes = when (item) {
                        CONTROL_AWB_MODE_AUTO -> R.drawable.wb_auto
                        CONTROL_AWB_MODE_INCANDESCENT -> R.drawable.wb_incandescent
                        CONTROL_AWB_MODE_FLUORESCENT -> R.drawable.fluorescent
                        CONTROL_AWB_MODE_WARM_FLUORESCENT -> R.drawable.fluorescent
                        CONTROL_AWB_MODE_DAYLIGHT -> R.drawable.wb_sunny
                        CONTROL_AWB_MODE_CLOUDY_DAYLIGHT -> R.drawable.wb_cloudy
                        CONTROL_AWB_MODE_TWILIGHT -> R.drawable.wb_twilight
                        CONTROL_AWB_MODE_SHADE -> R.drawable.wb_shade
                        else -> R.drawable.wb_auto
                    }
                )
            } ?: emptyList(),
            wbMode = when (characteristics.wbMode) {
                CONTROL_AWB_MODE_AUTO -> ModeItem.WbItem.AUTO
                CONTROL_AWB_MODE_INCANDESCENT -> ModeItem.WbItem.INCANDESCENT
                CONTROL_AWB_MODE_FLUORESCENT -> ModeItem.WbItem.FLUORESCENT
                CONTROL_AWB_MODE_WARM_FLUORESCENT -> ModeItem.WbItem.WARM_FLUORESCENT
                CONTROL_AWB_MODE_DAYLIGHT -> ModeItem.WbItem.DAYLIGHT
                CONTROL_AWB_MODE_CLOUDY_DAYLIGHT -> ModeItem.WbItem.CLOUDY_DAYLIGHT
                CONTROL_AWB_MODE_TWILIGHT -> ModeItem.WbItem.TWILIGHT
                CONTROL_AWB_MODE_SHADE -> ModeItem.WbItem.SHADE
                else -> ModeItem.WbItem.AUTO
            },
            focusItems = focusValues.map { SelectorItem(it.toInt(), it.toString()) }.toImmutableList(),
            focusModeItems = ModeItem.FocusItem.entries.map { item ->
                RadioGroupItem.IconItem(
                    id = item,
                    iconRes = when (item) {
                        ModeItem.FocusItem.MACRO -> R.drawable.macro_auto
                        ModeItem.FocusItem.CONTINUOUS -> R.drawable.continuous
                        ModeItem.FocusItem.TOUCH -> R.drawable.touch
                        ModeItem.FocusItem.INFINITE -> R.drawable.infinity
                    }
                )
            },
            focusMode = when (characteristics.focusMode) {
                CONTROL_AF_MODE_CONTINUOUS_PICTURE -> ModeItem.FocusItem.CONTINUOUS
                -1 -> ModeItem.FocusItem.TOUCH
                else -> ModeItem.FocusItem.CONTINUOUS
            },
            magnifierItems = standardMagnifierValues.map { SelectorItem(it.toInt(), it.toString()) }.toImmutableList(),
            magnifierValue = "1",
            histogramData = characteristics.histogramData
        )
    }

    /**
     * Merges hardware reality into user intent.
     * CRITICAL FIX: If we are in Manual mode for a parameter, we keep the intent value.
     * If we are in Auto mode, we update the intent string with the current hardware reality.
     */
    fun merge(intent: CharacteristicsUI, reality: Characteristics): CharacteristicsUI {
        val hardwareMapped = map(reality)
        
        return intent.copy(
            shutterValue = if (intent.isShutterAuto) hardwareMapped.shutterValue else intent.shutterValue,
            shutterPosition = if (intent.isShutterAuto) hardwareMapped.shutterPosition else intent.shutterPosition,
            
            isoValue = if (intent.isIsoAuto) hardwareMapped.isoValue else intent.isoValue,
            isoPosition = if (intent.isIsoAuto) hardwareMapped.isoPosition else intent.isoPosition,
            
            wbValue = if (intent.isWbAuto) hardwareMapped.wbValue else intent.wbValue,
            wbPosition = if (intent.isWbAuto) hardwareMapped.wbPosition else intent.wbPosition,
            wbMode = intent.wbMode ?: hardwareMapped.wbMode,
            
            focusValue = if (intent.isFocusAuto) hardwareMapped.focusValue else intent.focusValue,
            focusPosition = if (intent.isFocusAuto) hardwareMapped.focusPosition else intent.focusPosition,
            focusMode = intent.focusMode ?: hardwareMapped.focusMode,

            resolution = hardwareMapped.resolution,
            aspectRatio = hardwareMapped.aspectRatio,
            imgFormat = hardwareMapped.imgFormat,
            histogramData = hardwareMapped.histogramData,
            isFocused = hardwareMapped.isFocused,
            capturedBitmap = reality.capturedBitmap,
            isCaptureRequested = intent.isCaptureRequested || reality.isCaptureRequested,
            
            characteristicsItems = persistentListOf(
                RadioGroupItem.TextItem(SettingsItem.SHUTTER, "SHUTTER", if (intent.isShutterAuto) hardwareMapped.shutterValue ?: "" else intent.shutterValue ?: ""),
                RadioGroupItem.TextItem(SettingsItem.ISO, "ISO", if (intent.isIsoAuto) (hardwareMapped.isoValue ?: "") else (intent.isoValue ?: "")),
                (hardwareMapped.characteristicsItems.firstOrNull { it.id == SettingsItem.WB } ?: RadioGroupItem.TextItem(SettingsItem.WB, "WB", "A")),
                if (intent.isFocusAuto) {
                    RadioGroupItem.IconItem(SettingsItem.FOCUS, "FOCUS", getFocusIconRes(intent.focusMode ?: hardwareMapped.focusMode))
                } else {
                    RadioGroupItem.TextItem(SettingsItem.FOCUS, "FOCUS", intent.focusValue?.takeIf { it.isNotBlank() } ?: hardwareMapped.focusValue ?: "")
                },
                RadioGroupItem.TextItem(SettingsItem.MAGNIFIER, "MAGNIFIER", intent.magnifierValue ?: "1")
            )
        )
    }

    fun map(characteristics: CharacteristicsUI): Characteristics {
        val isoValue = if (characteristics.isIsoAuto) null else characteristics.isoValue?.toIntOrNull()
        val shutterValue = if (characteristics.isShutterAuto) null else allStandardShutterSpeeds.entries.firstOrNull { it.value == characteristics.shutterValue }?.key
        
        val wbValue = if (characteristics.isWbAuto) null else characteristics.wbValue?.filter { it.isDigit() }?.toIntOrNull()
        val wbMode = if (characteristics.isWbAuto) {
            when (characteristics.wbMode) {
                ModeItem.WbItem.AUTO -> CONTROL_AWB_MODE_AUTO
                ModeItem.WbItem.INCANDESCENT -> CONTROL_AWB_MODE_INCANDESCENT
                ModeItem.WbItem.FLUORESCENT -> CONTROL_AWB_MODE_FLUORESCENT
                ModeItem.WbItem.WARM_FLUORESCENT -> CONTROL_AWB_MODE_WARM_FLUORESCENT
                ModeItem.WbItem.DAYLIGHT -> CONTROL_AWB_MODE_DAYLIGHT
                ModeItem.WbItem.CLOUDY_DAYLIGHT -> CONTROL_AWB_MODE_CLOUDY_DAYLIGHT
                ModeItem.WbItem.TWILIGHT -> CONTROL_AWB_MODE_TWILIGHT
                ModeItem.WbItem.SHADE -> CONTROL_AWB_MODE_SHADE
                else -> CONTROL_AWB_MODE_AUTO
            }
        } else null

        var focusValue: Float? = if (characteristics.isFocusAuto) null else characteristics.focusValue?.toFloatOrNull()
        val focusMode: Int? = if (characteristics.isFocusAuto) {
            when (characteristics.focusMode) {
                ModeItem.FocusItem.MACRO -> { focusValue = characteristics.focusItems.map { it.value.toFloat() }.maxOrNull(); -2 }
                ModeItem.FocusItem.CONTINUOUS -> CONTROL_AF_MODE_CONTINUOUS_PICTURE
                ModeItem.FocusItem.TOUCH -> -1
                ModeItem.FocusItem.INFINITE -> { focusValue = characteristics.focusItems.map { it.value.toFloat() }.minOrNull(); -3 }
                else -> CONTROL_AF_MODE_CONTINUOUS_PICTURE
            }
        } else null

        val imgFormat = when (characteristics.imgFormat.uppercase()) {
            "DNG", "RAW" -> ImgFormat.DNG
            "JPEG+DNG", "JPEG + DNG", "JPEG+RAW", "JPEG + RAW", "JPEG_DNG" -> ImgFormat.JPEGDNG
            else -> ImgFormat.JPEG
        }

        return Characteristics(
            isoValue = isoValue,
            shutterValue = shutterValue,
            wbValue = wbValue,
            wbMode = wbMode,
            focusValue = focusValue,
            focusMode = focusMode,
            touchPoint = floatArrayOf(characteristics.touchPoint?.x ?: 0.5f, characteristics.touchPoint?.y ?: 0.5f),
            isFocused = characteristics.isFocused,
            capturedBitmap = characteristics.capturedBitmap,
            isCaptureRequested = characteristics.isCaptureRequested,
            imgFormat = imgFormat
        )
    }

    private fun getWbIconRes(mode: ModeItem.WbItem?): Int {
        return when (mode) {
            ModeItem.WbItem.AUTO -> R.drawable.wb_auto
            ModeItem.WbItem.INCANDESCENT -> R.drawable.wb_incandescent
            ModeItem.WbItem.FLUORESCENT -> R.drawable.fluorescent
            ModeItem.WbItem.WARM_FLUORESCENT -> R.drawable.fluorescent
            ModeItem.WbItem.DAYLIGHT -> R.drawable.wb_sunny
            ModeItem.WbItem.CLOUDY_DAYLIGHT -> R.drawable.wb_cloudy
            ModeItem.WbItem.TWILIGHT -> R.drawable.wb_twilight
            ModeItem.WbItem.SHADE -> R.drawable.wb_shade
            else -> R.drawable.wb_auto
        }
    }

    private fun getFocusIconRes(mode: ModeItem.FocusItem?): Int {
        return when (mode) {
            ModeItem.FocusItem.MACRO -> R.drawable.macro_auto
            ModeItem.FocusItem.CONTINUOUS -> R.drawable.continuous
            ModeItem.FocusItem.TOUCH -> R.drawable.touch
            ModeItem.FocusItem.INFINITE -> R.drawable.infinity
            else -> R.drawable.continuous
        }
    }
}
