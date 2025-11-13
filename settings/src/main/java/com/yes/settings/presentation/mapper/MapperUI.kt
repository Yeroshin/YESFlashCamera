package com.yes.settings.presentation.mapper

import com.yes.settings.domain.model.Settings
import com.yes.settings.presentation.model.SettingsUI
import com.yes.settings.presentation.ui.views.ImmutableCollection
import com.yes.shared.domain.Dimensions

class MapperUI {
    fun map(settings: Settings): SettingsUI {
        return SettingsUI(
            resolutionValue = settings.resolutionValue?.width.toString() + "x" + settings.resolutionValue?.height.toString(),
            resolutionItems = ImmutableCollection(
                settings.resolutionItems?.map {
                    it.width.toString() + "x" + it.height.toString()
                }?:run { emptyList() }
            ),
            fullScreen = settings.fullScreen?:run { true }
        )
    }
    fun map(settingsUI: SettingsUI):Settings{
        return Settings(
            resolutionValue =  settingsUI.resolutionValue.toDimensions(),
            fullScreen = settingsUI.fullScreen
        )
    }
    fun String.toDimensions(): Dimensions? {
        val (width, height) = split("x").map { it.toIntOrNull() ?: return null }
        return Dimensions(width, height)
    }
}