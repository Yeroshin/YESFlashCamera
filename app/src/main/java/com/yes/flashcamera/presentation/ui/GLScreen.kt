package com.yes.flashcamera.presentation.ui

import android.opengl.GLES10.glDrawArrays
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glVertexAttribPointer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class GLScreen {
    private val BYTES_PER_FLOAT: Int = 4
    private val vertexDataSize:Int=12
    private val POSITION_COMPONENT_COUNT = 2
    private val TEXTURE_COORDINATES_COMPONENT_COUNT = 2
    private val STRIDE: Int = (POSITION_COMPONENT_COUNT
            + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT

   /* private val VERTEX_DATA = floatArrayOf( // Order of coordinates: X, Y, S, T
        -1f,  1f,   0f, 1f,
        1f,  1f,   1f, 1f,
        1f,  -1f,   1f, 0f,
        1f,  -1f,   1f, 0f,
        -1f,  -1f,   0f, 0f,
        -1f,  1f,   0f, 1f
    )*/
  /* private val VERTEX_DATA = floatArrayOf( // Order of coordinates: X, Y, S, T
       -3.6f,  6.4f,  0.0f, 0.0f,
       3.6f,   6.4f, 1.0f, 0.0f,
       3.6f,  -6.4f, 1.0f, 1.0f,
       3.6f,  -6.4f, 1.0f, 1.0f,
       -3.6f, -6.4f, 0.0f, 1.0f,
       -3.6f,  6.4f, 0.0f, 0.0f
   )*/
    private val VERTEX_DATA = floatArrayOf( // Order of coordinates: X, Y, S, T
        -1.0f,   1.0f,
        1.0f,   1.0f,
        1.0f,  -1.0f,
        1.0f,  -1.0f,
        -1.0f,  -1.0f,
        -1.0f,   1.0f,
    )

    private val textureData = floatArrayOf( // Order of coordinates: X, Y, S, T
        0f, 0f,
        1f, 0f,
        1f, 1f,
        1f, 1f,
        0f, 1f,
        0f, 0f
    )
    private val vertexBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(VERTEX_DATA.size * BYTES_PER_FLOAT)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(VERTEX_DATA)
    private val textureBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(vertexDataSize * BYTES_PER_FLOAT)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(textureData)
    fun updateVertexBuffer( width:Float,height:Float) {
        val vertexData = floatArrayOf( // Order of coordinates: X, Y, S, T
            0.0f-width/2, 0.0f+height/2,
            0.0f+width/2, 0.0f+height/2,
            0.0f+width/2, 0.0f-height/2,
            0.0f+width/2, 0.0f-height/2,
            0.0f-width/2, 0.0f-height/2,
            0.0f-width/2, 0.0f+height/2
        )
        vertexBuffer.position(0)
        vertexBuffer.put(vertexData, 0, vertexDataSize)
        vertexBuffer.position(0)
    }

    fun bindData(textureProgram: TextureShaderProgram) {

       vertexBuffer.position(0)
        glVertexAttribPointer(
            textureProgram.positionAttributeLocation,
            2,
            GL_FLOAT,
            false,
            8,
            vertexBuffer
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
        //////////////////////////////
        /*  glEnableVertexAttribArray(
            textureProgram.positionAttributeLocation
        )*/
       /* glEnableVertexAttribArray(
            textureProgram.textureCoordinatesAttributeLocation
        )*/
    }
    private fun setVertexAttribPointer(
        dataOffset: Int, attributeLocation: Int,
        componentCount: Int, stride: Int
    ) {
        vertexBuffer.position(dataOffset)
        glVertexAttribPointer(
            attributeLocation, componentCount, GL_FLOAT,
            false, stride, vertexBuffer
        )
        glEnableVertexAttribArray(attributeLocation)


    }
    fun draw() {

        glDrawArrays(GL_TRIANGLES, 0, 6)
    }




}