package com.yes.camera.presentation.mapper

import com.yes.camera.domain.model.Characteristics
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.camera.presentation.model.Items
import com.yes.camera.presentation.model.Settings
import com.yes.camera.presentation.model.SelectorItemUI
import kotlin.math.abs

class MapperUI {
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

    fun map(characteristics: Characteristics): CharacteristicsUI {


        val supportedShutterSpeeds = standardShutterSpeeds
            .entries
            .filter { it.key in characteristics.shutterRange.first..characteristics.shutterRange.last }
            .sortedBy { it.key }
            .map { SelectorItemUI(it.value) }
        val supportedIsoValues = standardIsoValues
            .filter { it in characteristics.isoRange.first..characteristics.isoRange.last }
            .map { SelectorItemUI(it.toString()) }
        val shutterValue=characteristics.shutterValue?.let {
            standardShutterSpeeds.entries
                .minByOrNull { (key, _) ->
                    abs(key - characteristics.shutterValue)
                }
                ?.toPair()
                ?.second

        }?:run {
            "1/60"
        }
        val shutterPosition= standardShutterSpeeds
            .toSortedMap(compareByDescending { it })
            .values.toList().indexOf(shutterValue)

        val isoValue=characteristics.isoValue?.let {
            standardIsoValues.minByOrNull {
                abs(it - characteristics.isoValue)
            }
        }
        val isoPosition=isoValue?.let {standardIsoValues.indexOf(isoValue) }?:0
        val items= Items(
            shutterItems = standardShutterSpeeds
                .toSortedMap(compareByDescending { it })
                .map {
                    SelectorItemUI(it.value)
                },
            isoItems = standardIsoValues.map {
                SelectorItemUI(it.toString())
            },
            wbItems = standardWbValues.map {
                SelectorItemUI(it.toString()+"K")
            },
            focusItems = listOf(
                SelectorItemUI("0.2"),
                SelectorItemUI("1"),
                SelectorItemUI("2"),
                SelectorItemUI("3"),
                SelectorItemUI("4"),
                SelectorItemUI("5"),
                SelectorItemUI("6"),
                SelectorItemUI("7"),
                SelectorItemUI("8"),
                SelectorItemUI("9"),
                SelectorItemUI("9.5"),
                SelectorItemUI("10"),
                SelectorItemUI("11"),
                SelectorItemUI("12"),
                SelectorItemUI("13"),
                SelectorItemUI("14"),
                SelectorItemUI("15"),

                ),
            magnifierItems = listOf(
                SelectorItemUI("1"),
                SelectorItemUI("2"),
                SelectorItemUI("3"),
                SelectorItemUI("4"),
                SelectorItemUI("5"),
                SelectorItemUI("6"),
                SelectorItemUI("7"),
                SelectorItemUI("8"),
                SelectorItemUI("9"),
                SelectorItemUI("10"),
            )
        )
        val settings=Settings(
            shutterValue = shutterValue,
            shutterPosition = shutterPosition,

            isoValue = isoValue.toString(),
            isoPosition = isoPosition,
            /*  shutterItems = supportedShutterSpeeds,
              isoItems = supportedIsoValues,*/

            )

        return CharacteristicsUI(
            settings = settings,
            items = items
            /*  shutterItems= standardShutterSpeeds
                  .filter { it.first in characteristics.shutterRange.first ..characteristics.shutterRange.last }
                  .map { SettingsItemUI(it.second )},
              isoItems = standardIsoValues
                  .filter { it in characteristics.isoRange.first .. characteristics.isoRange.last }
                  .map { SettingsItemUI(it.toString() )},*/
            /*  characteristics = mapOf(
                  Item.SHUTTER to Characteristic(
                      value = 0,
                      title = "Shutter",
                     /* items= listOf(
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
                      )*/
                      items = standardShutterSpeeds
                          .keys // Преобразуем Map в List пар
                          .sortedDescending()  // Сортируем по ключам (первый элемент пары)
                          .map { SettingsItemUI(standardShutterSpeeds[it]!!) }
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
                          SettingsItemUI("0.2"),
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
                          SettingsItemUI("11"),
                          SettingsItemUI("12"),
                          SettingsItemUI("13"),
                          SettingsItemUI("14"),
                          SettingsItemUI("15"),

                      )
                  ),
                  Item.MAGNIFIER to Characteristic(
                      value = 0,
                      title = "Focus",
                      items = listOf(
                          SettingsItemUI("1"),
                          SettingsItemUI("2"),
                          SettingsItemUI("3"),
                          SettingsItemUI("4"),
                          SettingsItemUI("5"),
                          SettingsItemUI("6"),
                          SettingsItemUI("7"),
                          SettingsItemUI("8"),
                          SettingsItemUI("9"),
                          SettingsItemUI("10"),
                      )
                  )
              )*/

        )
    }

    fun map(characteristics: CharacteristicsUI): Characteristics {
        val isoValue = characteristics.settings.isoValue.toIntOrNull()

        val shutterValue =
            standardShutterSpeeds.entries.firstOrNull { it.value == characteristics.settings.shutterValue }?.key
        val wbValue=characteristics.settings.wbValue.filter { it.isDigit() }.toIntOrNull()

        val focusValue = characteristics.settings.focusValue.toFloat()

        return Characteristics(
            isoValue = isoValue,
            isoRange = IntRange(0, 0),
            shutterValue = shutterValue,
            wbValue=wbValue,
            focusValue = focusValue ?: 0f,
            minFocusValue = 0f,
            shutterRange = LongRange(0, 0),
            resolutions = emptyList(),
            touchPoint = characteristics.settings.touchPoint
        )
    }

    fun map(histogramData: ByteArray): CharacteristicsUI {
        return CharacteristicsUI(
            histogramData = histogramData
        )
    }
}