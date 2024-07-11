package com.yes.flashcamera.presentation.ui

import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_POINTS
import android.opengl.GLES20.glDrawArrays
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glVertexAttribPointer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Mallet {
    private val BYTES_PER_FLOAT: Int = 4

    private val POSITION_COMPONENT_COUNT = 2
    private val COLOR_COMPONENT_COUNT = 3
    private val STRIDE: Int = ((POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT)
            * BYTES_PER_FLOAT)
    private val VERTEX_DATA = floatArrayOf( // Order of coordinates: X, Y, R, G, B
        0f, -0.4f, 0f, 0f, 1f,
        0f, 0.4f, 0f, 1f, 0f
    )

    private val floatBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(VERTEX_DATA.size * BYTES_PER_FLOAT)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(VERTEX_DATA)

    private fun setVertexAttribPointer(
        dataOffset: Int, attributeLocation: Int,
        componentCount: Int, stride: Int
    ) {
        floatBuffer.position(dataOffset)
        glVertexAttribPointer(
            attributeLocation, componentCount, GL_FLOAT,
            false, stride, floatBuffer
        )
        glEnableVertexAttribArray(attributeLocation)

        floatBuffer.position(0)
    }

    fun bindData(colorProgram: ColorShaderProgram) {
        setVertexAttribPointer(
            0,
            colorProgram.positionAttributeLocation,
            POSITION_COMPONENT_COUNT,
            STRIDE
        )
        setVertexAttribPointer(
            POSITION_COMPONENT_COUNT,
            colorProgram.colorAttributeLocation,
            COLOR_COMPONENT_COUNT,
            STRIDE
        )
    }

    fun draw() {
        glDrawArrays(GL_POINTS, 0, 2)
    }




}