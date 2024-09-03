package com.yes.flashcamera.presentation.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
@Deprecated("not used")
class ShadowDrawableView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

   // private var drawable: Drawable? = null

    init {
        // Устанавливаем ViewOutlineProvider для создания тени по контуру drawable
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                background?.let {
                    it.setBounds(0, 0, view.width, view.height)
                    it.getOutline(outline)
                }
            }
        }
        // Включаем тень
        elevation = 50f
    }

    fun setDrawable(drawable: Drawable) {
        //this.drawable = drawable
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        background.draw(canvas)
    }
}