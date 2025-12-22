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
import androidx.compose.ui.util.fastMapNotNull
import com.yes.camera.R
import com.yes.camera.domain.model.Characteristics
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.camera.presentation.model.FocusItem
import com.yes.camera.presentation.model.TextItem

import com.yes.camera.presentation.model.WbItem
import com.yes.camera.presentation.ui.custom.compose.IconRadioItem
import com.yes.camera.presentation.ui.views.ImmutableCollection
import com.yes.camera.utils.ResourceProvider
import com.yes.shared.domain.Dimensions
import kotlin.math.abs

class MapperUI(
    private val resources: ResourceProvider
) {
    private val standardShutterSpeeds = mapOf(
        31_250L to "1/32000",
        62_500L to "1/16000",
        125_000L to "1/8000",
        250_000L to "1/4000",
        500_000L to "1/2000",
        1_000_000L to "1/1000",
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
        /*  2048,
         4096,
         8192,
         1638,
         3280,
         4560*/
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
    private val standardFocusValues=listOf(
       0.2F,
       1F,
       2F,
       3F,
       4F,
       5F,
       6F,
       7F,
       8F,
       9F,
       9.5F,
       10F,
       11F,
       12F,
       13F,
       14F,
       15F,
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
            .toSortedMap(compareByDescending { it })
            .values.toList().indexOf(shutterValue)

        val isoValue = characteristics.isoValue?.let {
            standardIsoValues.minByOrNull {
                abs(it - characteristics.isoValue)
            }
        }
        val isoPosition = isoValue?.let { standardIsoValues.indexOf(isoValue) } ?: 0
       val wbItems=characteristics.wbItems?.toList()?.mapNotNull {mode->

           when(mode){
                    CONTROL_AWB_MODE_AUTO->{
                        WbItem.AUTO to R.drawable.wb_auto
                    }
                    CONTROL_AWB_MODE_INCANDESCENT->{
                        WbItem.INCANDESCENT to R.drawable.wb_incandescent
                    }
                    CONTROL_AWB_MODE_FLUORESCENT->{
                        WbItem.FLUORESCENT to R.drawable.fluorescent
                    }
                    CONTROL_AWB_MODE_WARM_FLUORESCENT->{
                        WbItem.WARM_FLUORESCENT to R.drawable.fluorescent
                    }
                    CONTROL_AWB_MODE_DAYLIGHT->{
                        WbItem.DAYLIGHT to R.drawable.wb_sunny
                    }
                    CONTROL_AWB_MODE_CLOUDY_DAYLIGHT->{
                        WbItem.CLOUDY_DAYLIGHT to R.drawable.wb_cloudy
                    }
                    CONTROL_AWB_MODE_TWILIGHT->{
                        WbItem.TWILIGHT to R.drawable.wb_twilight
                    }
                    CONTROL_AWB_MODE_SHADE->{
                        WbItem.SHADE to R.drawable.wb_shade
                    }
                    else -> null
                }?.let { (id, res) -> IconRadioItem(id,"Auto", res) }
            //   IconRadioItem(id,"Auto", res)


       }
        val wbValue=characteristics.wbValue?.toString()?:"A"
        val wbPosition=characteristics.wbValue?.let {
            val closestValue=standardWbValues.minByOrNull { value->
                abs(value - it)
            }
            standardWbValues.indexOf(closestValue)
        }?:0
        val focusValues=generateFocusValues(characteristics.maxFocusValue,characteristics.minFocusValue,1f)
        val focusValue=characteristics.focusValue?.toString()?:"A"
        val focusPosition=characteristics.focusValue?.let {
            val closestValue=focusValues.minByOrNull { value->
                abs(value - it)
            }
            focusValues.indexOf(closestValue)
        }?:0





        return CharacteristicsUI(
            shutterValue = shutterValue,
            shutterPosition = shutterPosition,

            isoValue = isoValue.toString(),
            isoPosition = isoPosition,

            wbValue = wbValue,
            wbPosition = wbPosition,

            focusValue = focusValue,
            focusPosition = focusPosition,
            fullScreen = characteristics.fullscreen?:run { false },
            resolution = characteristics.resolution.width.toString() + "x" + characteristics.resolution.height.toString(),
            aspectRatio = characteristics.resolution,
            shutterItems = ImmutableCollection(
                standardShutterSpeeds
                    .toSortedMap(compareByDescending { it })
                    .map {
                        TextItem( it.value.toString())
                    }
            ),
            isoItems = ImmutableCollection(
                standardIsoValues.map {
                    TextItem( it.toString())
                }
            ),
            wbManualItems = ImmutableCollection(
                standardWbValues.map {
                    TextItem(
                        it.toString() + "K"
                    )
                }
            ),
            wbModeItems = characteristics.wbItems?.toList()?.mapNotNull { mode->

                when(mode){
                    CONTROL_AWB_MODE_AUTO->{
                        WbItem.AUTO to R.drawable.wb_auto
                    }
                    CONTROL_AWB_MODE_INCANDESCENT->{
                        WbItem.INCANDESCENT to R.drawable.wb_incandescent
                    }
                    CONTROL_AWB_MODE_FLUORESCENT->{
                        WbItem.FLUORESCENT to R.drawable.fluorescent
                    }
                    CONTROL_AWB_MODE_WARM_FLUORESCENT->{
                        WbItem.WARM_FLUORESCENT to R.drawable.fluorescent
                    }
                    CONTROL_AWB_MODE_DAYLIGHT->{
                        WbItem.DAYLIGHT to R.drawable.wb_sunny
                    }
                    CONTROL_AWB_MODE_CLOUDY_DAYLIGHT->{
                        WbItem.CLOUDY_DAYLIGHT to R.drawable.wb_cloudy
                    }
                    CONTROL_AWB_MODE_TWILIGHT->{
                        WbItem.TWILIGHT to R.drawable.wb_twilight
                    }
                    CONTROL_AWB_MODE_SHADE->{
                        WbItem.SHADE to R.drawable.wb_shade
                    }
                    else -> null
                }?.let { (id, res) -> IconRadioItem(id,"Auto", res) }
                //   IconRadioItem(id,"Auto", res)


            },
            focusItems =focusValues.map {
                TextItem(it.toString())
            },

            magnifierItems = ImmutableCollection(
                listOf(
                    TextItem( "1"),
                    TextItem( "2"),
                    TextItem( "3"),
                    TextItem( "4"),
                    TextItem( "5"),
                    TextItem( "6"),
                    TextItem( "7"),
                    TextItem( "8"),
                    TextItem( "9"),
                    TextItem( "10"),
                )
            ),
            histogramData = characteristics.histogramData
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
                WbItem.AUTO -> CONTROL_AWB_MODE_AUTO
                WbItem.INCANDESCENT -> CONTROL_AWB_MODE_INCANDESCENT
                WbItem.FLUORESCENT -> CONTROL_AWB_MODE_FLUORESCENT
                WbItem.WARM_FLUORESCENT -> CONTROL_AWB_MODE_WARM_FLUORESCENT
                WbItem.DAYLIGHT -> CONTROL_AWB_MODE_DAYLIGHT
                WbItem.CLOUDY_DAYLIGHT -> CONTROL_AWB_MODE_CLOUDY_DAYLIGHT
                WbItem.TWILIGHT -> CONTROL_AWB_MODE_TWILIGHT
                WbItem.SHADE -> CONTROL_AWB_MODE_SHADE
                else -> {
                    null
                }
            }
        }
        var focusMode: Int? = null
        val focusValue: Float? = characteristics.focusValue?.toFloatOrNull()
            ?: when (characteristics.focusMode) {
                FocusItem.MACRO -> {
                    characteristics.focusItems?.fastMapNotNull {
                        it.text.toFloatOrNull()
                    }?.maxOrNull()

                }

                FocusItem.CONTINUOUS -> {
                    focusMode = CONTROL_AF_MODE_CONTINUOUS_PICTURE
                    null
                }

                FocusItem.TOUCH -> {
                    focusMode = -1
                    null
                }

                FocusItem.INFINITE -> characteristics.focusItems?.fastMapNotNull {
                    it.text.toFloatOrNull()
                }?.minOrNull()

                null -> null
            }


        return Characteristics(
            isoValue = isoValue,
            isoRange = IntRange(0, 0),
            shutterValue = shutterValue,
            wbValue = wbValue,
            wbMode = wbMode,
            focusValue = focusValue,
            focusMode = focusMode,
            shutterRange = LongRange(0, 0),
            resolutionItems = emptyList(),
            resolution = Dimensions(0, 0),
            touchPoint = characteristics.touchPoint
        )
    }
}