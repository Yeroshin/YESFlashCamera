package com.yes.camera.presentation.mapper

import com.yes.camera.domain.model.Camera
import com.yes.camera.presentation.model.CameraUI
import com.yes.camera.presentation.model.SettingsItemUI

class MapperUI {
    fun map(camera:Camera):CameraUI{
        return CameraUI(
            true,
            listOf(
                SettingsItemUI("0"),
                ),
            listOf(
                SettingsItemUI("1"),
            )
        )
    }
}