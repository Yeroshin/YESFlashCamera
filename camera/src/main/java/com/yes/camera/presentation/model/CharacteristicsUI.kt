package com.yes.camera.presentation.model

data class CharacteristicsUI(
    val backCamera: Boolean = true,
    val shutterPosition: Int = 0,
    val isoPosition: Int = 0,
    val focusPosition: Int = 0,
    val magnifierPosition: Int = 0,
    val touchPoint: FloatArray = FloatArray(2),
    val histogramData: ByteArray = ByteArray(0),
    val characteristics: Map<Item, Characteristic> = emptyMap()

)

enum class Item {
    SHUTTER, ISO, FOCUS, MAGNIFIER
}

data class Characteristic(
    val value: Int,
    val items: List<SettingsItemUI>,
    val title: String,
)