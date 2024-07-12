package com.yes.flashcamera.presentation.ui

import android.opengl.GLES10.glDrawArrays
import android.opengl.GLES20
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES20.glVertexAttribPointer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10.GL_TRIANGLE_FAN

class GLScreen {
    private val BYTES_PER_FLOAT: Int = 4

    private val POSITION_COMPONENT_COUNT = 2
    private val TEXTURE_COORDINATES_COMPONENT_COUNT = 2
    private val STRIDE: Int = (POSITION_COMPONENT_COUNT
            + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT

    private val VERTEX_DATA = floatArrayOf( // Order of coordinates: X, Y, S, T
        // Triangle Fan
           1f,  1f,   1f, 1f,
          -1f, -1f,   0f, 1f,
          -1f, -1f,   0f, 0f,
           1f,  1f,   1f, 1f,
          -1f, -1f,   0f, 0f,
           1f, -1f,   1f, 0f
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
      //  floatBuffer.position(dataOffset)
        glVertexAttribPointer(
            attributeLocation, componentCount, GL_FLOAT,
            false, 8, floatBuffer
        )
        glEnableVertexAttribArray(attributeLocation)

//floatBuffer.position(0)
    }
    val vertex_coords: FloatArray = floatArrayOf(
        1f, 1f,
        -1f, 1f,
        -1f, -1f,
        1f, 1f,
        -1f, -1f,
        1f, -1f
    )

    val vertex_coords_order: FloatArray = floatArrayOf(
        1f, 1f,
        0f, 1f,
        0f, 0f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )
    private var vertexBuffer:FloatBuffer?=null
    private var vertexOrederBuffer: FloatBuffer?=null
    fun bindData(textureProgram: TextureShaderProgram) {
       /* floatBuffer.position(0)
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
        )*/
        vertexBuffer = ByteBuffer.allocateDirect(vertex_coords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        (vertexBuffer as FloatBuffer).put(vertex_coords).position(0);

        vertexOrederBuffer = ByteBuffer.allocateDirect(vertex_coords_order.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertex_coords_order)
        (vertexOrederBuffer as FloatBuffer).position(0)
        glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer);
        glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 8, vertexOrederBuffer);

    }

    fun draw() {

        glDrawArrays(GL_TRIANGLES, 0, 6)
    }




}