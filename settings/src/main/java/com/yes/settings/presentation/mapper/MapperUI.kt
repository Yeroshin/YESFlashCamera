package com.yes.settings.presentation.mapper

import com.yes.settings.domain.model.Settings
import com.yes.settings.presentation.model.SettingsUI
import com.yes.settings.presentation.ui.views.ImmutableCollection
import com.yes.shared.domain.Dimensions
import com.yes.shared.domain.ImgFormat

class MapperUI {
    fun map(settings: Settings): SettingsUI {
        return SettingsUI(
            resolutionValue = settings.resolutionValue?.width.toString() + "x" + settings.resolutionValue?.height.toString(),
            resolutionItems = ImmutableCollection(
                settings.resolutionItems?.map {
                    it.width.toString() + "x" + it.height.toString()
                }?:run { emptyList() }
            ),
            fullScreen = settings.fullScreen?:run { true },
            imgFormatValue = when(settings.imageFormat){
                ImgFormat.JPEG->"JPEG"
                ImgFormat.RAW->"RAW"
                ImgFormat.JPEGRAW->"JPEG+RAW"
                null->"JPEG"
            },
            imgFormatItems = ImmutableCollection(
                list= listOf("JPEG","RAW","JPEG+RAW")
            )
        )
    }
    fun map(settingsUI: SettingsUI):Settings{
        return Settings(
            resolutionValue =  settingsUI.resolutionValue.toDimensions(),
            resolutionItems = settingsUI.resolutionItems.list.mapNotNull {
               it.toDimensions()
            },
            fullScreen = settingsUI.fullScreen,
            imageFormat =when(settingsUI.imgFormatValue){
                "JPEG"->ImgFormat.JPEG
                "RAW"->ImgFormat.RAW
                "JPEG+RAW"->ImgFormat.JPEGRAW
                else -> ImgFormat.JPEG
            }
        )
    }
    private fun String.toDimensions(): Dimensions? {
        val (width, height) = split("x").map { it.toIntOrNull() ?: return null }
        return Dimensions(width, height)
    }
}