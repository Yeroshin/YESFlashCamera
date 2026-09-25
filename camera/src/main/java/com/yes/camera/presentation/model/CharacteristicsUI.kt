package com.yes.camera.presentation.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import kotlinx.collections.immutable.persistentListOf
import com.yes.shared.domain.Dimensions
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class CharacteristicsUI(
    val shutterItems: List<SelectorItem> = emptyList(),
    val isoItems: List<SelectorItem> = emptyList(),
    val wbItems: List<SelectorItem> = emptyList(),
    val wbModeItems: List<RadioGroupItem> = emptyList(),
    val focusItems: List<SelectorItem> = emptyList(),
    val focusModeItems: List<RadioGroupItem> = emptyList(),
    val magnifierItems: List<SelectorItem> = emptyList(),
    val characteristicsItems: ImmutableList<RadioGroupItem> = persistentListOf(),

    val backCamera: Boolean = true,
    val shutterValue: String? = null,
    val shutterPosition: Int = 0,
    val isoValue: String? = null,
    val isoPosition: Int = 0,
    val wbValue: String? = null,
    val wbPosition: Int = 0,
    val wbMode: ModeItem.WbItem? = null,
    val focusValue: String? = null,
    val focusPosition: Int = 0,
    val focusMode: ModeItem.FocusItem? = null,
    val magnifierValue: String? = null,
    val touchPoint: Offset? = null,
    val fullScreen: Boolean = false,
    val resolution: String? = null,
    val aspectRatio: Dimensions? = null,

    // Flags for Auto/Manual status from Domain
    val isShutterAuto: Boolean = true,
    val isIsoAuto: Boolean = true,
    val isWbAuto: Boolean = true,
    val isFocusAuto: Boolean = true,

    val histogramData: Map<Int, Int> = emptyMap(),
    val selectedCategory: SettingsItem = SettingsItem.SHUTTER
) {
    val isAutoForSelectedCategory: Boolean
        get() = when (selectedCategory) {
            SettingsItem.SHUTTER -> isShutterAuto
            SettingsItem.ISO -> isIsoAuto
            SettingsItem.WB -> isWbAuto
            SettingsItem.FOCUS -> isFocusAuto
            else -> false
        }

    val currentCategoryItems: List<SelectorItem>
        get() = when (selectedCategory) {
            SettingsItem.SHUTTER -> shutterItems
            SettingsItem.ISO -> isoItems
            SettingsItem.WB -> wbItems
            SettingsItem.FOCUS -> focusItems
            SettingsItem.MAGNIFIER -> magnifierItems
        }

    val currentCategoryPosition: Int
        get() = when (selectedCategory) {
            SettingsItem.SHUTTER -> shutterPosition
            SettingsItem.ISO -> isoPosition
            SettingsItem.WB -> wbPosition
            SettingsItem.FOCUS -> focusPosition
            SettingsItem.MAGNIFIER -> {
                val index = magnifierItems.indexOfFirst { it.value == magnifierValue }
                if (index >= 0) index else 0
            }
        }
}

@Immutable
sealed interface RadioGroupItem  {
    val id: Enum<*>

    data class IconItem(
        override val id: Enum<*>,
        val iconRes: Int
    ) : RadioGroupItem

    data class TextItem(
        override val id: Enum<*>,
        val title: String,
        val currentValue: String
    ) : RadioGroupItem
}

data class SelectorItem(
    val id: Int,
    val value: String
)

enum class SettingsItem  {
    SHUTTER, ISO, FOCUS, WB, MAGNIFIER
}

sealed interface ModeItem {
    enum class WbItem: ModeItem {
        AUTO, INCANDESCENT, FLUORESCENT, WARM_FLUORESCENT, DAYLIGHT, CLOUDY_DAYLIGHT, TWILIGHT, SHADE
    }

    enum class FocusItem: ModeItem {
        MACRO, CONTINUOUS, TOUCH, INFINITE
    }
}
