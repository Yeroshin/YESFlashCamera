package com.yes.flashcamera.presentation.ui

import android.opengl.GLES10.glDrawArrays
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.Matrix.setIdentityM
import android.opengl.Matrix.translateM
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class GLScreen(glShaderProgram: MyRenderer.GlShaderProgram) :
MyRenderer.GLObject(glShaderProgram){

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