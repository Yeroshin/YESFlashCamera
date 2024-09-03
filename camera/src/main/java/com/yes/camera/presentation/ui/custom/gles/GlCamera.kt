package com.yes.camera.presentation.ui.custom.gles

import android.content.Context
import android.opengl.GLES20.glViewport
import android.opengl.Matrix.multiplyMM
import android.opengl.Matrix.orthoM
import android.opengl.Matrix.setLookAtM
import android.view.Surface
import android.view.WindowManager

class GlCamera(context: Context,) {
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)
    val viewProjectionMatrix = FloatArray(16)
    private var rotationX = 0f
    private var rotationY = 0f
    private val windowManager: WindowManager = context
        .getSystemService(Context.WINDOW_SERVICE) as WindowManager
    fun setProjection( width: Int, height: Int){
        val ratio = if (width > height) width.toFloat() / height.toFloat() else height.toFloat() / width.toFloat()
        when (windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> {
                rotationX = -1f
                rotationY = 0f
            }

            Surface.ROTATION_90 -> {
                rotationX = 0f
                rotationY = 1f
            }

            Surface.ROTATION_180 -> {
                rotationX = 1f
                rotationY = 0f
            }

            Surface.ROTATION_270 -> {
                rotationX = 0f
                rotationY = -1f
            }

            else -> "Не понятно"
        }
        glViewport(0, 0, width, height)
        setLookAtM(viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, rotationX, rotationY, 0.0f)
        if (width > height) {
            // Landscape

            orthoM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, -1f, 1f)
        } else {
            // Portrait or square
            orthoM(projectionMatrix, 0, -1f, 1f, -ratio, ratio, -1f, 1f)
        }
        multiplyMM(
            viewProjectionMatrix, 0, projectionMatrix, 0,
            viewMatrix, 0
        )
    }
    fun translateObjectInScene(modelMatrix:FloatArray):FloatArray {
        multiplyMM(
            modelViewProjectionMatrix, 0, viewProjectionMatrix,
            0, modelMatrix, 0
        )
        return modelViewProjectionMatrix
    }
}