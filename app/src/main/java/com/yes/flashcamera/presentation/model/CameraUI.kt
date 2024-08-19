package com.yes.flashcamera.presentation.model

import android.util.Range

data class CameraUI (
    val iso: Range<Int>? = null,
    val exposure: Range<Long>? = null
)