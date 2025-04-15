package com.yes.camera.presentation.model

import com.yes.camera.R
import com.yes.camera.presentation.ui.views.ImmutableCollection

data class CharacteristicsUI(
    val settings: Settings = Settings(),
    val items: Items = Items(),
    val histogramData: ByteArray = ByteArray(0),
) {
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
}
data class Items(
    val shutterItems: ImmutableCollection<TextItem>? = null,
    val isoItems: ImmutableCollection<TextItem>? = null,
    val wbItems: ImmutableCollection<IconItem>? = null,
    val focusItems: ImmutableCollection<TextItem>? = null,
    val magnifierItems: ImmutableCollection<TextItem>? = null,
)
{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Items

        if (shutterItems != other.shutterItems) return false
        if (isoItems != other.isoItems) return false
        if (wbItems != other.wbItems) return false
        if (focusItems != other.focusItems) return false
        if (magnifierItems != other.magnifierItems) return false

        return true
    }

    override fun hashCode(): Int {
        var result = shutterItems.hashCode()
        result = 31 * result + isoItems.hashCode()
        result = 31 * result + wbItems.hashCode()
        result = 31 * result + focusItems.hashCode()
        result = 31 * result + magnifierItems.hashCode()
        return result
    }
}

data class Settings(
    val backCamera: Boolean = true,

    val shutterValue: String = "0",
    val shutterPosition: Int = 0,

    val isoValue: String = "0",
    val isoPosition: Int = 0,

    val wbValue: Int? = null,
    val wbPosition: Int = 0,

    val focusValue: String = "0",
    val focusPosition: Int = 0,

    val magnifierValue: String = "0",
    val magnifierPosition: Int = 0,

    val touchPoint: FloatArray = FloatArray(2),
) {
    // Переопределяем equals/hashCode для FloatArray
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Settings

        if (backCamera != other.backCamera) return false
        if (shutterValue != other.shutterValue) return false
        if (shutterPosition != other.shutterPosition) return false
        if (isoValue != other.isoValue) return false
        if (isoPosition != other.isoPosition) return false
        if (wbValue != other.wbValue) return false
        if (wbPosition != other.wbPosition) return false
        if (focusValue != other.focusValue) return false
        if (focusPosition != other.focusPosition) return false
        if (magnifierValue != other.magnifierValue) return false
        if (magnifierPosition != other.magnifierPosition) return false
        if (!touchPoint.contentEquals(other.touchPoint)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = backCamera.hashCode()
        result = 31 * result + shutterValue.hashCode()
        result = 31 * result + shutterPosition
        result = 31 * result + isoValue.hashCode()
        result = 31 * result + isoPosition
        result = 31 * result + wbValue.hashCode()
        result = 31 * result + wbPosition
        result = 31 * result + focusValue.hashCode()
        result = 31 * result + focusPosition
        result = 31 * result + magnifierValue.hashCode()
        result = 31 * result + magnifierPosition
        result = 31 * result + touchPoint.contentHashCode()
        return result
    }
}

enum class Item {
    SHUTTER, ISO, FOCUS, WB, MAGNIFIER
}
enum class WbItem {
    AUTO,INCANDESCENT,FLUORESCENT,WARM_FLUORESCENT,DAYLIGHT,CLOUDY_DAYLIGHT,TWILIGHT,SHADE
}
