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
import com.yes.camera.R
import com.yes.camera.domain.model.Characteristics
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.camera.presentation.model.ModeItem
import com.yes.camera.presentation.model.RadioGroupItem
import com.yes.camera.presentation.model.SelectorItem
import com.yes.camera.presentation.model.SettingsItem
import com.yes.camera.utils.ResourceProvider
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.abs

class MapperUI(
    private val resources: ResourceProvider
) {
    private val standardShutterSpeeds = mapOf(
        2_000_000L to "1/500",
        4_000_000L to "1/250",
        8_000_000L to "1/125",
        16_000_000L to "1/60",
    )
    private val standardIsoValues = listOf(50, 100, 200, 400, 800, 1600, 3200, 6400)
    private val standardWbValues = listOf(1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000)
    
    private fun generateFocusValues(min: Float, max: Float, step: Float): List<Float> {
        val size = ((max - min) / step).toInt() + 1
        return List(size) { i -> min + i * step }
    }
    
    private val standardMagnifierValues = listOf(1F, 2F, 3F, 4F, 5F, 6F, 7F, 8F, 9F, 10F)

    /**
     * Maps purely from Hardware/Domain reality.
     */
    fun map(characteristics: Characteristics): CharacteristicsUI {
        val displayShutter = characteristics.shutterValue ?: characteristics.actualShutter
        val shutterValueStr = displayShutter?.let { s ->
            standardShutterSpeeds.entries.minByOrNull { abs(it.key - s) }?.value
        } ?: "1/60"
        val shutterPosition = standardShutterSpeeds.toSortedMap(compareByDescending { it }).values.toList().indexOf(shutterValueStr)

        val displayIso = characteristics.isoValue ?: characteristics.actualIso
        val isoValue = displayIso?.let { i -> standardIsoValues.minByOrNull { abs(it - i) } }
        val isoPosition = isoValue?.let { standardIsoValues.indexOf(it) } ?: 0
        
        val displayWb = characteristics.wbValue ?: characteristics.actualWbKelvin
        val wbValueStr = displayWb?.let { "${it}K" } ?: "A"
        val wbPosition = displayWb?.let { w ->
            val closest = standardWbValues.minByOrNull { abs(it - w) }
            standardWbValues.indexOf(closest)
        } ?: 0

        val focusValues = generateFocusValues(characteristics.maxFocusValue, characteristics.minFocusValue, 1f)
        val displayFocus = characteristics.focusValue ?: characteristics.actualFocusDistance
        val focusPosition = displayFocus?.let { f ->
            val closest = focusValues.minByOrNull { abs(it - f) }
            focusValues.indexOf(closest)
        } ?: 0

        return CharacteristicsUI(
            shutterValue = shutterValueStr,
            shutterPosition = shutterPosition,
            isoValue = isoValue?.toString() ?: "",
            isoPosition = isoPosition,
            wbValue = wbValueStr,
            wbPosition = wbPosition,
            focusValue = displayFocus?.toString() ?: "",
            focusPosition = focusPosition,
            fullScreen = characteristics.fullscreen ?: false,
            resolution = "${characteristics.resolution.width}x${characteristics.resolution.height}",
            aspectRatio = characteristics.resolution,

            isShutterAuto = characteristics.shutterValue == null,
            isIsoAuto = characteristics.isoValue == null,
            isWbAuto = characteristics.wbValue == null,
            isFocusAuto = characteristics.focusValue == null,

            characteristicsItems = persistentListOf(
                RadioGroupItem.TextItem(SettingsItem.SHUTTER, "SHUTTER", shutterValueStr),
                RadioGroupItem.TextItem(SettingsItem.ISO, "ISO", isoValue?.toString() ?: characteristics.actualIso?.toString() ?: ""),
                RadioGroupItem.TextItem(SettingsItem.WB, "WB", wbValueStr),
                RadioGroupItem.TextItem(SettingsItem.FOCUS, "FOCUS", if (characteristics.focusValue == null) "A" else displayFocus.toString()),
                RadioGroupItem.TextItem(SettingsItem.MAGNIFIER, "MAGNIFIER", "1")
            ),
            shutterItems = standardShutterSpeeds.toSortedMap(compareByDescending { it }).entries.map { SelectorItem(it.key.toInt(), it.value) },
            isoItems = standardIsoValues.mapIndexed { i, v -> SelectorItem(i, v.toString()) },
            wbItems = standardWbValues.mapIndexed { i, v -> SelectorItem(i, "${v}K") },
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
            focusItems = focusValues.map { SelectorItem(it.toInt(), it.toString()) },
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
            magnifierItems = standardMagnifierValues.map { SelectorItem(it.toInt(), it.toString()) },
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
            histogramData = hardwareMapped.histogramData,
            
            characteristicsItems = persistentListOf(
                RadioGroupItem.TextItem(SettingsItem.SHUTTER, "SHUTTER", if (intent.isShutterAuto) hardwareMapped.shutterValue ?: "" else intent.shutterValue ?: ""),
                RadioGroupItem.TextItem(SettingsItem.ISO, "ISO", if (intent.isIsoAuto) (hardwareMapped.isoValue ?: "") else (intent.isoValue ?: "")),
                RadioGroupItem.TextItem(SettingsItem.WB, "WB", if (intent.isWbAuto) hardwareMapped.wbValue ?: "" else intent.wbValue ?: ""),
                RadioGroupItem.TextItem(SettingsItem.FOCUS, "FOCUS", if (intent.isFocusAuto) "A" else intent.focusValue ?: ""),
                RadioGroupItem.TextItem(SettingsItem.MAGNIFIER, "MAGNIFIER", intent.magnifierValue ?: "1")
            )
        )
    }

    fun map(characteristics: CharacteristicsUI): Characteristics {
        val isoValue = if (characteristics.isIsoAuto) null else characteristics.isoValue?.toIntOrNull()
        val shutterValue = if (characteristics.isShutterAuto) null else standardShutterSpeeds.entries.firstOrNull { it.value == characteristics.shutterValue }?.key
        
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
                ModeItem.FocusItem.MACRO -> { focusValue = characteristics.focusItems.map { it.value.toFloat() }.maxOrNull(); null }
                ModeItem.FocusItem.CONTINUOUS -> CONTROL_AF_MODE_CONTINUOUS_PICTURE
                ModeItem.FocusItem.TOUCH -> -1
                ModeItem.FocusItem.INFINITE -> { focusValue = characteristics.focusItems.map { it.value.toFloat() }.minOrNull(); null }
                else -> CONTROL_AF_MODE_CONTINUOUS_PICTURE
            }
        } else null

        return Characteristics(
            isoValue = isoValue,
            shutterValue = shutterValue,
            wbValue = wbValue,
            wbMode = wbMode,
            focusValue = focusValue,
            focusMode = focusMode,
            touchPoint = floatArrayOf(characteristics.touchPoint?.x ?: 0f, characteristics.touchPoint?.y ?: 0f)
        )
    }
}
