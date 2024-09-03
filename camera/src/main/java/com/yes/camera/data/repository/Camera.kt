package com.yes.camera.data.repository

import android.util.Range
import android.util.Size

data class Camera(
    val iso: Range<Int>? = null,
    val exposure: Range<Long>? = null,
    val resolutions:Array<Size>?=null
)