package com.yes.camera.presentation.mapper

import com.yes.camera.domain.model.Characteristics
import com.yes.camera.presentation.model.Characteristic
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.camera.presentation.model.Item
import com.yes.camera.presentation.model.SettingsItemUI

class MapperUI() {

    fun map(characteristics:Characteristics):CharacteristicsUI{

        val standardShutterSpeeds = setOf(
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
            1_000_000_000L to "1"
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
        val supportedShutterSpeeds=standardShutterSpeeds
            .filter { it.first in characteristics.shutterRange.first ..characteristics.shutterRange.last }
            .map { SettingsItemUI(it.second )}
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
                        SettingsItemUI("1/8"),
                        SettingsItemUI("1/20"),
                        SettingsItemUI("1/100"),
                        SettingsItemUI("1/250"),
                        SettingsItemUI("1/500"),
                    )
                ),
                Item.ISO to Characteristic(
                    value = 0,
                    title = "Shutter",
                    items= listOf(
                        SettingsItemUI("100"),
                        SettingsItemUI("200"),
                        SettingsItemUI("400"),
                        SettingsItemUI("800"),
                        SettingsItemUI("1200"),
                    )
                )
            )

        )
    }
}