package com.yes.camera.presentation.model

import com.yes.camera.presentation.ui.custom.compose.RadioButton
import com.yes.camera.presentation.ui.views.ImmutableCollection

data class CharacteristicsUI(
    val settings: Settings = Settings(),
    val items: Items = Items(),
    val histogramData: ByteArray = ByteArray(0),
)/* {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CharacteristicsUI

        if (settings != other.settings) return false
        if (items != other.items) return false
        if (!histogramData.contentEquals(other.histogramData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = settings.hashCode()
        result = 31 * result + items.hashCode()
        result = 31 * result + histogramData.contentHashCode()
        return result
    }
}*/

data class Items(
    val shutterItems: ImmutableCollection<TextItem>? = null,
    val isoItems: ImmutableCollection<TextItem>? = null,
    val wbManualItems: ImmutableCollection<TextItem>? = null,
    val wbModeItems: List<RadioButton>? = null,
    val focusItems: List<TextItem>? = null,
    val magnifierItems: ImmutableCollection<TextItem>? = null,
)


data class Settings(
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
    val resolution:String?=null
)


sealed interface Item
interface SelectorRadioGroupItem : Item
enum class SettingsRadioGroupItem : Item {
    SHUTTER, ISO, FOCUS, WB, MAGNIFIER
}

enum class WbItem : SelectorRadioGroupItem {
    AUTO, INCANDESCENT, FLUORESCENT, WARM_FLUORESCENT, DAYLIGHT, CLOUDY_DAYLIGHT, TWILIGHT, SHADE
}

enum class FocusItem : SelectorRadioGroupItem {
    MACRO, CONTINUOUS, TOUCH, INFINITE
}

