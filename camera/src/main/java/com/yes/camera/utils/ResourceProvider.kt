package com.yes.camera.utils

import android.graphics.drawable.Icon
import androidx.compose.ui.graphics.Color

interface ResourceProvider {
    fun getString(stringResId: Int): String
    fun getString(stringResId: Int, vararg args: Any): String
    fun getColor(colorResId: Int): Color

}