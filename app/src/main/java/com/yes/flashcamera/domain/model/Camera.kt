package com.yes.flashcamera.domain.model

import android.util.Range

data class Camera(
    val iso: Range<Int>? = null,
    val exposure: Range<Long>? = null
)