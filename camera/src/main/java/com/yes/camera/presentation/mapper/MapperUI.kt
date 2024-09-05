package com.yes.camera.presentation.mapper

import com.yes.camera.domain.model.Camera
import com.yes.camera.presentation.model.CameraUI
import com.yes.camera.presentation.model.SettingsItemUI

class MapperUI {
    fun map(camera:Camera):CameraUI{
        return CameraUI(
            true,
            2,
            listOf(
                SettingsItemUI("1/50"),
                SettingsItemUI("1/100"),
                SettingsItemUI("1/150"),
                SettingsItemUI("1/250"),
                ),
            listOf(
                SettingsItemUI("100"),
                SettingsItemUI("200"),
                SettingsItemUI("400"),
                SettingsItemUI("800"),
                SettingsItemUI("1200"),
            )
        )
    }
}