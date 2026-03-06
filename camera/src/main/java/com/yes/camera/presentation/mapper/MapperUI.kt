package com.yes.camera.presentation.mapper

import android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE
import android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_MACRO
import android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_AUTO
import android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT
import android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT
import android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT
import android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT
import android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_SHADE
import android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_TWILIGHT
import android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT
import android.view.Display.Mode
import androidx.compose.ui.util.fastMapNotNull
import com.yes.camera.R
import com.yes.camera.domain.model.Characteristics
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.camera.presentation.model.ModeItem

import com.yes.camera.presentation.model.RadioGroupItem
import com.yes.camera.presentation.model.SelectorItem
import com.yes.camera.presentation.model.SettingsItem


import com.yes.camera.utils.ResourceProvider
import com.yes.shared.domain.Dimensions
import kotlin.math.abs

class MapperUI(
    private val resources: ResourceProvider
) {
    private val standardShutterSpeeds = mapOf(
        /*   31_250L to "1/32000",
           62_500L to "1/16000",
           125_000L to "1/8000",
           250_000L to "1/4000",
           500_000L to "1/2000",
           1_000_000L to "1/1000",*/
        2_000_000L to "1/500",
        4_000_000L to "1/250",
        8_000_000L to "1/125",
        16_000_000L to "1/60",
        /* 33_333_333L to "1/30",
         66_666_667L to "1/15",
         125_000_000L to "1/8",
         250_000_000L to "1/4",
         500_000_000L to "1/2",
         1_000_000_000L to "1",
         2_000_000_000L to "2",
         4_000_000_000L to "4",
         8_000_000_000L to "8",*/
    )
    private val standardIsoValues = listOf(
        50,
        100,
        200,
        400,
        800,
        1600,
        3200,
        6400,
        12800,
        25600,
        51200,
        102400,
        204800,
        409600,
        819200,
        1638400,
        3280000,
        4560000
    )

    private val standardWbValues = listOf(
        1000,
        2000,
        3000,
        4000,
        5000,
        6000,
        7000,
        8000,
        9000,
        10000
    )

    private fun generateFocusValues(min: Float, max: Float, step: Float): List<Float> {
        val size = ((max - min) / step).toInt() + 1
        return List(size) { i -> min + i * step }
    }

    private val standardMagnifierValues = listOf(
        1F,
        2F,
        3F,
        4F,
        5F,
        6F,
        7F,
        8F,
        9F,
        10F,
    )

    fun map(characteristics: Characteristics): CharacteristicsUI {


        /*   val supportedShutterSpeeds = standardShutterSpeeds
               .entries
               .filter { it.key in characteristics.shutterRange.first..characteristics.shutterRange.last }
               .sortedBy { it.key }
               .map { TextSelectorItemUI(it.value) }
           val supportedIsoValues = standardIsoValues
               .filter { it in characteristics.isoRange.first..characteristics.isoRange.last }
               .map { TextSelectorItemUI(it.toString()) }*/
        val shutterValue = characteristics.shutterValue?.let {
            standardShutterSpeeds.entries
                .minByOrNull { (key, _) ->
                    abs(key - characteristics.shutterValue)
                }
                ?.toPair()
                ?.second

        } ?: run {
            "1/60"
        }
        val shutterPosition = standardShutterSpeeds
            //  .toSortedMap(compareByDescending { it })
            .toSortedMap(compareByDescending { it })
            .values.toList().indexOf(shutterValue)

        val isoValue = characteristics.isoValue?.let {
            standardIsoValues.minByOrNull {
                abs(it - characteristics.isoValue)
            }
        }
        val isoPosition = isoValue?.let { standardIsoValues.indexOf(isoValue) } ?: 0
        val wbValue = characteristics.wbValue?.let { "${it}K" } ?: "A"
        val wbPosition = characteristics.wbValue?.let {
            val closestValue = standardWbValues.minByOrNull { value ->
                abs(value - it)
            }
            standardWbValues.indexOf(closestValue)
        } ?: 0
        val focusValues =
            generateFocusValues(characteristics.maxFocusValue, characteristics.minFocusValue, 1f)
        val focusValue = characteristics.focusValue
        val focusPosition = characteristics.focusValue?.let {
            val closestValue = focusValues.minByOrNull { value ->
                abs(value - it)
            }
            focusValues.indexOf(closestValue)
        } ?: 0





        return CharacteristicsUI(


            shutterValue = shutterValue,
            shutterPosition = shutterPosition,

            isoValue = isoValue.toString(),
            isoPosition = isoPosition,

            wbValue = wbValue,
            wbPosition = wbPosition,

            focusValue = focusValue?.toString() ?: "",
            focusPosition = focusPosition,
            fullScreen = characteristics.fullscreen ?: false,
            resolution = characteristics.resolution.width.toString() + "x" + characteristics.resolution.height.toString(),
            aspectRatio = characteristics.resolution,


            characteristicsItems = listOf(
                RadioGroupItem.TextItem(
                    SettingsItem.SHUTTER,
                    "SHUTTER",
                    shutterValue
                ),
                RadioGroupItem.TextItem(
                    SettingsItem.ISO,
                    "ISO",
                    isoValue.toString()
                ),
                RadioGroupItem.TextItem(
                    SettingsItem.WB,
                    "WB",
                    wbValue
                ),
                RadioGroupItem.TextItem(
                    SettingsItem.FOCUS,
                    "FOCUS",
                    focusValue.toString()
                ),
                RadioGroupItem.TextItem(
                    SettingsItem.MAGNIFIER,
                    "MAGNIFIER",
                    standardMagnifierValues.first().toString()
                )
            ),

            shutterItems =
            standardShutterSpeeds
                .toSortedMap(compareByDescending { it })
                .entries
                .map { entry ->
                    SelectorItem(
                        entry.key.toInt(),
                        entry.value.toString(),
                    )
                },
            isoItems =
            standardIsoValues.mapIndexed { index, entry ->
                SelectorItem(
                    index,
                    entry.toString(),

                    )
            },
            wbItems =
            standardWbValues.mapIndexed { index, entry ->
                SelectorItem(
                    index,
                    entry.toString() + "K",

                    )
            },
            wbModeItems =
            characteristics.wbModeItems?.map { item ->
            RadioGroupItem.IconItem(
                id =  when (item) {
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
                    else ->R.drawable.wb_auto
                }
            )
        } ?: emptyList()
           /* ModeItem.WbItem.entries.map { item ->
                RadioGroupItem.IconItem(
                    id = item,
                    iconRes = when (item) {
                        ModeItem.WbItem.AUTO -> R.drawable.wb_auto
                        ModeItem.WbItem.INCANDESCENT -> R.drawable.wb_incandescent
                        ModeItem.WbItem.FLUORESCENT -> R.drawable.fluorescent
                        ModeItem.WbItem.WARM_FLUORESCENT -> R.drawable.fluorescent
                        ModeItem.WbItem.DAYLIGHT -> R.drawable.wb_sunny
                        ModeItem.WbItem.CLOUDY_DAYLIGHT -> R.drawable.wb_cloudy
                        ModeItem.WbItem.TWILIGHT -> R.drawable.wb_twilight
                        ModeItem.WbItem.SHADE -> R.drawable.wb_shade

                    },

                    )
            }*/,
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
            /*  characteristics.wbItems?.toList()?.mapNotNull { mode ->
                  when (mode) {
                      CONTROL_AWB_MODE_AUTO -> {
                          RadioGroupItem.IconItem(
                              WbItem.AUTO,
                              R.drawable.wb_auto
                          )
                        //  WbItem.AUTO to R.drawable.wb_auto
                      }

                      CONTROL_AWB_MODE_INCANDESCENT -> {
                          WbItem.INCANDESCENT to R.drawable.wb_incandescent
                      }

                      CONTROL_AWB_MODE_FLUORESCENT -> {
                          WbItem.FLUORESCENT to R.drawable.fluorescent
                      }

                      CONTROL_AWB_MODE_WARM_FLUORESCENT -> {
                          WbItem.WARM_FLUORESCENT to R.drawable.fluorescent
                      }

                      CONTROL_AWB_MODE_DAYLIGHT -> {
                          WbItem.DAYLIGHT to R.drawable.wb_sunny
                      }

                      CONTROL_AWB_MODE_CLOUDY_DAYLIGHT -> {
                          WbItem.CLOUDY_DAYLIGHT to R.drawable.wb_cloudy
                      }

                      CONTROL_AWB_MODE_TWILIGHT -> {
                          WbItem.TWILIGHT to R.drawable.wb_twilight
                      }

                      CONTROL_AWB_MODE_SHADE -> {
                          WbItem.SHADE to R.drawable.wb_shade
                      }

                      else -> null
                  }?.let { (id, res) -> IconRadioItem(id, "Auto", res) }
                  //   IconRadioItem(id,"Auto", res)


              },*/
            focusItems = focusValues.map { entry ->
                SelectorItem(
                    entry.toInt(),
                    entry.toString(),

                    )
            },
            focusModeItems =
            ModeItem.FocusItem.entries.map { item ->
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
                characteristics.maxFocusValue.toInt() -> ModeItem.FocusItem.MACRO
                CONTROL_AF_MODE_CONTINUOUS_PICTURE -> ModeItem.FocusItem.CONTINUOUS
                -1 -> ModeItem.FocusItem.TOUCH
                characteristics.minFocusValue.toInt() -> ModeItem.FocusItem.INFINITE
                else -> ModeItem.FocusItem.MACRO
            },

            magnifierItems =
            standardMagnifierValues.map { entry ->
                SelectorItem(
                    entry.toInt(),
                    entry.toString(),
                )
            },
            magnifierValue = "1",
            histogramData = characteristics.histogramData,


            )
    }

    fun map(characteristics: CharacteristicsUI): Characteristics {
        val isoValue = characteristics.isoValue?.toIntOrNull()
        val shutterValue = standardShutterSpeeds.entries.firstOrNull {
            it.value == characteristics.shutterValue
        }?.key
        val wbValue = characteristics.wbValue?.filter { it.isDigit() }?.toIntOrNull()
        val wbMode = wbValue?.let { null } ?: run {
            when (characteristics.wbMode) {
                ModeItem.WbItem.AUTO -> CONTROL_AWB_MODE_AUTO
                ModeItem.WbItem.INCANDESCENT -> CONTROL_AWB_MODE_INCANDESCENT
                ModeItem.WbItem.FLUORESCENT -> CONTROL_AWB_MODE_FLUORESCENT
                ModeItem.WbItem.WARM_FLUORESCENT -> CONTROL_AWB_MODE_WARM_FLUORESCENT
                ModeItem.WbItem.DAYLIGHT -> CONTROL_AWB_MODE_DAYLIGHT
                ModeItem.WbItem.CLOUDY_DAYLIGHT -> CONTROL_AWB_MODE_CLOUDY_DAYLIGHT
                ModeItem.WbItem.TWILIGHT -> CONTROL_AWB_MODE_TWILIGHT
                ModeItem.WbItem.SHADE -> CONTROL_AWB_MODE_SHADE
                else -> {
                    null
                }
            }
        }
        var focusValue: Float? = characteristics.focusValue?.toFloatOrNull()

        val focusMode: Int? = when (characteristics.focusMode) {
            ModeItem.FocusItem.MACRO -> {
                focusValue=  characteristics.focusItems.map {
                    it.value.toFloat()
                }.max()
                //  focusMode = CONTROL_AF_MODE_MACRO
                  null
             //   CONTROL_AF_MODE_MACRO
            }

            ModeItem.FocusItem.CONTINUOUS -> {
                CONTROL_AF_MODE_CONTINUOUS_PICTURE
                // null
            }

            ModeItem.FocusItem.TOUCH -> {
                focusValue=null
                    -1
                // null
            }

            ModeItem.FocusItem.INFINITE -> {
                focusValue=   characteristics.focusItems.map {
                    it.value.toFloat()
                }.min()
                null
            }

            null -> null
        }



        return Characteristics(
            isoValue = isoValue,

            shutterValue = shutterValue,
            wbValue = wbValue,
            wbMode = wbMode,
            focusValue = focusValue,
            focusMode = focusMode,

            resolutionItems = emptyList(),

            touchPoint = floatArrayOf(
                characteristics.touchPoint?.x ?: 0f,
                characteristics.touchPoint?.y ?: 0f,
            )
        )
    }
}