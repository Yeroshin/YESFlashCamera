package com.yes.camera.utils

import android.content.Context
import android.graphics.drawable.Icon
import androidx.compose.ui.graphics.Color

class AndroidResourceProvider(
    private val context: Context
) : ResourceProvider {
    override fun getString(stringResId: Int): String =
        context.getString(stringResId)

    override fun getString(stringResId: Int, vararg args: Any): String =
        context.getString(stringResId, *args)

    override fun getColor(colorResId: Int): Color =
        Color(context.getColor(colorResId))


}
