package com.yes.flashcamera.presentation.ui

import android.opengl.Matrix.setIdentityM
import android.opengl.Matrix.translateM

class GLScreen(glShaderProgram: GLRenderer.GlShaderProgram) :
GLRenderer.GLObject(glShaderProgram){

    override val vertexData = FloatArray(12)
    override val textureData  = floatArrayOf( // Order of coordinates: X, Y, S, T
    0f, 0f,
    1f, 0f,
    1f, 1f,
    1f, 1f,
    0f, 1f,
    0f, 0f
    )
    override fun setSelected(pressed: Boolean, touchedPointX: Float, touchedPointY: Float) {

    }


    override fun translate(draggedPointX: Float, draggedPointY: Float){
        setIdentityM(modelMatrix, 0)
        translateM(modelMatrix, 0, draggedPointX, draggedPointY, 0f)
    }


    override fun onRatioChanged(ratio:Float){
        updateVertexBuffer(
            ratio * 2,
            2f,
        )
        translate(0f, 0f)
    }


}