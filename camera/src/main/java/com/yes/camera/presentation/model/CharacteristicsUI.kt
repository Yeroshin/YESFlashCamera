package com.yes.camera.presentation.model

data class CharacteristicsUI(
    val settings: Settings = Settings(),
    val histogramData: ByteArray = ByteArray(0),

)
data class Settings(
    val backCamera: Boolean = true,
    val shutterValue: String = "0",
    val shutterItems:List<SettingsItemUI>? =null,
    val shutterPosition:Int=0,
    val isoValue: String = "0",
    val isoItems:List<SettingsItemUI> =emptyList(),
    val isoPosition:Int=0,
    val wbValue: String = "0",
    val wbItems:List<SettingsItemUI> =emptyList(),
    val wbPosition:Int=0,
    val focusValue: String = "0",
    val focusItems:List<SettingsItemUI> =emptyList(),
    val focusPosition:Int=0,
    val magnifierValue: String = "0",
    val magnifierItems:List<SettingsItemUI> =emptyList(),
    val magnifierPosition:Int=0,
    val touchPoint: FloatArray = FloatArray(2),
)

enum class Item {
    SHUTTER, ISO, FOCUS,WB, MAGNIFIER
}
