package com.yes.camera.presentation.mapper

import android.util.Range
import com.yes.camera.domain.model.Characteristics
import com.yes.camera.domain.model.Dimensions
import com.yes.camera.presentation.model.Characteristic
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.camera.presentation.model.Item
import com.yes.camera.presentation.model.SettingsItemUI

class MapperUI {
    val standardShutterSpeeds = mapOf(
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
        33_333_333L to "1/30",
        66_666_667L to "1/15",
        125_000_000L to "1/8",
        250_000_000L to "1/4",
        500_000_000L to "1/2",
        1_000_000_000L to "1",
        2_000_000_000L to "2",
        4_000_000_000L to "4",
        8_000_000_000L to "8",
    )
    val standardIsoValues = listOf(
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
    fun map(characteristics:Characteristics):CharacteristicsUI{



        val supportedShutterSpeeds=standardShutterSpeeds
            .filter { it.key in characteristics.shutterRange.first ..characteristics.shutterRange.last }
            .map { SettingsItemUI(it.value)}
        val supportedIsoValues=standardIsoValues
            .filter { it in characteristics.isoRange.first .. characteristics.isoRange.last }
            .map { SettingsItemUI(it.toString() )}

        return CharacteristicsUI(
          /*  shutterValues = standardShutterSpeeds
                .filter { it.first in characteristics.shutterRange.first ..characteristics.shutterRange.last }
                .map { SettingsItemUI(it.second )},
            isoValues = standardIsoValues
                .filter { it in characteristics.isoRange.first .. characteristics.isoRange.last }
                .map { SettingsItemUI(it.toString() )},*/
            characteristics = mapOf(
                Item.SHUTTER to Characteristic(
                    value = 0,
                    title = "Shutter",
                    items= listOf(
                        SettingsItemUI("1/32000"),
                        SettingsItemUI("1/16000"),
                        SettingsItemUI("1/8000"),
                        SettingsItemUI("1/4000"),
                        SettingsItemUI("1/2000"),
                        SettingsItemUI("1/1000"),
                        SettingsItemUI("1/500"),
                        SettingsItemUI("1/250"),
                        SettingsItemUI("1/125"),
                        SettingsItemUI("1/60"),
                        SettingsItemUI("1/30"),
                        SettingsItemUI("1/15"),
                        SettingsItemUI("1/8"),
                        SettingsItemUI("1/4"),
                        SettingsItemUI("1/2"),
                        SettingsItemUI("1"),
                        SettingsItemUI("2"),
                        SettingsItemUI("4"),
                        SettingsItemUI("8"),
                    )
                ),
                Item.ISO to Characteristic(
                    value = 0,
                    title = "Shutter",
                    items= listOf(
                        SettingsItemUI("50"),
                        SettingsItemUI("100"),
                        SettingsItemUI("200"),
                        SettingsItemUI("400"),
                        SettingsItemUI("800"),
                        SettingsItemUI("1600"),
                        SettingsItemUI("3200"),
                        SettingsItemUI("6400"),
                        SettingsItemUI("12800"),
                        SettingsItemUI("25600"),
                        SettingsItemUI("51200"),
                        SettingsItemUI("102400"),
                        SettingsItemUI("204800"),
                        SettingsItemUI("409600"),
                        SettingsItemUI("819200"),
                        SettingsItemUI("1638400"),
                        SettingsItemUI("3280000"),
                        SettingsItemUI("4560000"),
                    )
                ),
                Item.FOCUS to Characteristic(
                    value = 0,
                    title = "Focus",
                    items= listOf(
                    SettingsItemUI("1"),
                        SettingsItemUI("2"),
                        SettingsItemUI("3"),
                        SettingsItemUI("4"),
                        SettingsItemUI("5"),
                        SettingsItemUI("6"),
                        SettingsItemUI("7"),
                        SettingsItemUI("8"),
                        SettingsItemUI("9"),
                        SettingsItemUI("9.5"),
                    SettingsItemUI("10"),
                    )
                )
            )

        )
    }
    fun map(characteristics:CharacteristicsUI):Characteristics{
       val iso= characteristics.characteristics[Item.ISO]?.items?.get(characteristics.isoValue)?.text?.toInt()

        val shutterString= characteristics.characteristics[Item.SHUTTER]?.items?.get(characteristics.shutterValue)?.text
        val shutter=standardShutterSpeeds.entries.firstOrNull { it.value == shutterString }?.key

        val focus= characteristics.characteristics[Item.FOCUS]?.items?.get(characteristics.focusValue)?.text?.toFloat()

        return Characteristics(
            isoValue= iso?:100,
        isoRange = IntRange(0,0),
        shutterValue= shutter?:33_333_333L,
            focusValue = focus?:0f,
            minFocusValue = 0f,
        shutterRange= LongRange(0,0),
        resolutions= emptyList()
        )
    }
}