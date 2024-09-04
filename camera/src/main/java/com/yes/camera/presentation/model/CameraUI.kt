package com.yes.camera.presentation.model

import android.util.Range

data class CameraUI (
    val backCamera:Boolean=true,
    val shutterValues:List<SettingsItemUI>,
    val isoValues:List<SettingsItemUI>

)