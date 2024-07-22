package com.yes.flashcamera.presentation.ui

import android.opengl.GLES10.glDrawArrays
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glVertexAttribPointer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Magnifier(
    val radius:Float, val  height:Float, val numPointsAroundMallet:Int
) {
    private val BYTES_PER_FLOAT: Int = 4

    private val POSITION_COMPONENT_COUNT = 2
    private val TEXTURE_COORDINATES_COMPONENT_COUNT = 2
    private val STRIDE: Int = (POSITION_COMPONENT_COUNT
            + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT

    private val VERTEX_DATA = floatArrayOf( // Order of coordinates: X, Y, S, T
        -1f,  1f,   0f, 0f,
        1f,  1f,   1f, 0f,
        1f,  -1f,   1f, 1f,
        1f,  -1f,   1f, 1f,
        -1f,  -1f,   0f, 1f,
        -1f,  1f,   0f, 0f
    )
    private val textureData = floatArrayOf( // Order of coordinates: X, Y, S, T
         /*  0f, 0f,
          1f, 0f,
          1f, 1f,
           1f, 1f,
          0f, 1f,
           0f, 0f*/
        0f, 0f,
        1f, 0f,
        1f, 1f,
        1f, 1f,
        0f, 1f,
        0f, 0f
    )

    private val floatBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(VERTEX_DATA.size * BYTES_PER_FLOAT)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(VERTEX_DATA)
    private val textureBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(VERTEX_DATA.size * BYTES_PER_FLOAT)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(textureData)

    fun updateBuffer(vertexData: FloatArray?, start: Int, count: Int) {
        textureBuffer.position(start)
        textureBuffer.put(vertexData, start, count)
        textureBuffer.position(0)
    }
    fun bindData(textureProgram: ScaledTextureProgram) {
        floatBuffer.position(0)
        glVertexAttribPointer(
            textureProgram.positionAttributeLocation,
            2,
            GL_FLOAT,
            false,
            STRIDE,
            floatBuffer
        )
        glEnableVertexAttribArray(
            textureProgram.positionAttributeLocation
        )
        ////////////////////////
        textureBuffer.position(0)
        glVertexAttribPointer(
            textureProgram.textureCoordinatesAttributeLocation,
            2,
            GL_FLOAT,
            false,
            8,
            textureBuffer
        )
        glEnableVertexAttribArray(
            textureProgram.textureCoordinatesAttributeLocation
        )
    }

  /*  fun bindData(textureProgram: ScaledTextureProgram) {

        setVertexAttribPointer(
            0,
            textureProgram.positionAttributeLocation,
            POSITION_COMPONENT_COUNT,
            STRIDE
        )

        setVertexAttribPointer(
            POSITION_COMPONENT_COUNT,
            textureProgram.textureCoordinatesAttributeLocation,
            TEXTURE_COORDINATES_COMPONENT_COUNT,
            STRIDE
        )
    }*/
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


    }
    fun draw() {

        glDrawArrays(GL_TRIANGLES, 0, 6)
    }




}