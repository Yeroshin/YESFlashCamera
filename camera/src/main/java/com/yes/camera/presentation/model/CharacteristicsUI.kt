package com.yes.camera.presentation.model

import com.yes.camera.presentation.ui.custom.compose.IconRadioItem
import com.yes.camera.presentation.ui.custom.compose.RadioButton
import com.yes.camera.presentation.ui.views.ImmutableCollection

data class CharacteristicsUI(
    val settings: Settings = Settings(),
    val items: Items = Items(),
    val histogramData: ByteArray = ByteArray(0),
)
data class Items(
    val shutterItems: ImmutableCollection<TextItem>? = null,
    val isoItems: ImmutableCollection<TextItem>? = null,
    val wbManualItems: ImmutableCollection<TextItem>? = null,
    val wbAutoItems: List<RadioButton>? = emptyList(),
    val focusItems: List<TextItem>? = null,
    val magnifierItems: ImmutableCollection<TextItem>? = null,
)


data class Settings(
    val backCamera: Boolean = true,

    val shutterValue: String = "1",
    val shutterPosition: Int = 0,

    val isoValue: String = "1",
    val isoPosition: Int = 0,

    val wbValue: String = "A",
    val wbPosition: Int = 0,
    val wbAutoMode:Int?=1,

    val focusValue: String = "A",
    val focusPosition: Int = 0,

    val magnifierValue: String = "1",
    val magnifierPosition: Int = 0,

    val touchPoint: FloatArray?=null,
)


sealed interface Item
enum class SettingsItem:Item {
    SHUTTER, ISO, FOCUS, WB, MAGNIFIER
}
enum class WbItem:Item {
    AUTO,INCANDESCENT,FLUORESCENT,WARM_FLUORESCENT,DAYLIGHT,CLOUDY_DAYLIGHT,TWILIGHT,SHADE
}
