package com.yes.camera.presentation.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset

import com.yes.shared.domain.Dimensions
/*
data class CharacteristicsUI(
    val backCamera: Boolean? = null,
    val shutterValue: String? = null,
    val shutterPosition: Int = 0,
    val isoValue: String? = null,
    val isoPosition: Int = 0,
    val wbValue: String? = null,
    val wbPosition: Int = 0,
    val wbMode: WbItem? = null,
    val focusValue: String? = null,
    val focusPosition: Int = 0,
    val focusMode: FocusItem? = null,
    val magnifierValue: String? = null,
    val magnifierPosition: Int = 0,
    val touchPoint: FloatArray? = null,
    val fullScreen: Boolean = false,
    val resolution:String?=null,
    val aspectRatio: Dimensions?=null,

    val shutterItems: List<TextItem>? = null,
    val isoItems: List<TextItem>? = null,
    val wbManualItems: List<TextItem>? = null,
    val wbModeItems: List<RadioButton>? = null,
    val focusItems: List<TextItem>? = null,
    val magnifierItems: List<TextItem>? = null,

    val histogramData: MutableMap<Int, Int> = mutableMapOf(),
)
*/
@Immutable
data class CharacteristicsUI(
    val shutterItems: List<SelectorItem> = emptyList(),
    val isoItems: List<SelectorItem> = emptyList(),
    val wbItems: List<SelectorItem> = emptyList(),
    val wbModeItems: List<RadioGroupItem.IconItem> = emptyList(),
    val focusItems: List<SelectorItem> = emptyList(),
    val focusModeItems: List<RadioGroupItem> = emptyList(),
    val magnifierItems: List<SelectorItem> = emptyList(),
    val characteristicsItems:List<RadioGroupItem > = listOf(
        RadioGroupItem.TextItem(
            SettingsItem.SHUTTER,
            "SHUTTER",
            ""
        ),
        RadioGroupItem.TextItem(
            SettingsItem.ISO,
            "ISO",
            ""
        ),
        RadioGroupItem.TextItem(
            SettingsItem.WB,
            "WB",
            ""
        ),
        RadioGroupItem.TextItem(
            SettingsItem.FOCUS,
            "FOCUS",
            ""
        ),
        RadioGroupItem.TextItem(
            SettingsItem.MAGNIFIER,
            "MAGNIFIER",
            ""
        )
    ),

    val backCamera: Boolean = true,
    val shutterValue: String? = null,
    val shutterPosition: Int = 0,
    val isoValue: String? = null,
    val isoPosition: Int = 0,
    val wbValue: String? = null,
    val wbPosition: Int = 0,
    val wbMode: WbItem? = null,
    val focusValue: String? = null,
    val focusPosition: Int = 0,
    val focusMode: FocusItem? = null,
    val magnifierValue: String? = null,
    val magnifierPosition: Int = 0,
    val touchPoint: Offset? = null,
    val fullScreen: Boolean = false,
    val resolution: String? = null,
    val aspectRatio: Dimensions? = null,
    // Гистограмму лучше вынести в отдельный StateFlow, но если здесь, то:
    val histogramData: Map<Int, Int> = emptyMap() // Используем обычный Map вместо Mutable

)


@Immutable
sealed interface RadioGroupItem  {
    val id: Enum<*> // Оригинальный элемент (для логики клика)

    data class IconItem(
        override val id: Enum<*>,
        val iconRes: Int,
       // val labelRes: Int
    ) : RadioGroupItem

    data class TextItem(
        override val id: Enum<*>,
        val title: String,
        val currentValue: String
    ) : RadioGroupItem
}
data class SelectorItem(
    val id:Int,
    val value:String
)


enum class SettingsItem  {
    SHUTTER, ISO, FOCUS, WB, MAGNIFIER
}

enum class WbItem {
    AUTO, INCANDESCENT, FLUORESCENT, WARM_FLUORESCENT, DAYLIGHT, CLOUDY_DAYLIGHT, TWILIGHT, SHADE
}

enum class FocusItem  {
    MACRO, CONTINUOUS, TOUCH, INFINITE
}







