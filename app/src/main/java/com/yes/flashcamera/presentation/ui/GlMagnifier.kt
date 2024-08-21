package com.yes.flashcamera.presentation.ui

import android.opengl.GLES10.glDrawArrays
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.Matrix.setIdentityM
import android.opengl.Matrix.translateM
import androidx.core.math.MathUtils.clamp
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class GlMagnifier() {
    private var magnification = 2.0f
    private var magnifierSizeW = 0.5f
    private var magnifierSizeH = 0.5f
    private var ratio = 1f
    var selected = false


    // var vertexPosition= Geometry.Point(0f, 0f, 0f)
    //  var texturePosition=Geometry.Point(0f, 0f, 0f)
    ///////////////////////////

    private var posXcorrection = 0f
    private var posYcorrection = 0f
    fun setSelected(pressed: Boolean, touchedPointX: Float, touchedPointY: Float) {
        this.selected = pressed
        posXcorrection = touchedPointX - vertexPosition.x
        posYcorrection = touchedPointY - vertexPosition.y
    }

    private fun mapVertexToTextureCords(
        vertexPositionX: Float,
        vertexPositionY: Float
    ): Pair<Float,Float> {
        val textureX = (vertexPositionX + 1.0f) / 2.0f
        val textureY = 1.0f - (vertexPositionY + 1.0f) / 2.0f

        return Pair(textureX, textureY)
    }

    private val BYTES_PER_FLOAT: Int = 4
    private val vertexDataSize: Int = 12

    private val POSITION_COMPONENT_COUNT = 2
    private val TEXTURE_COORDINATES_COMPONENT_COUNT = 2
    private val STRIDE: Int = (POSITION_COMPONENT_COUNT
            + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT


    private val VERTEX_DATA = floatArrayOf(
        // Order of coordinates: X, Y, S, T
        -1.0f, 1.0f,
        1.0f, 1.0f,
        0.5f, -0.5f,
        0.5f, -0.5f,
        -0.5f, -0.5f,
        -0.5f, 0.5f,
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
        .allocateDirect(vertexDataSize * BYTES_PER_FLOAT)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(VERTEX_DATA)
    private val textureBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(vertexDataSize * BYTES_PER_FLOAT)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(textureData)

    val modelMatrix = FloatArray(16)
    private fun updateVertexBuffer(width: Float, height: Float) {

        val vertexData = floatArrayOf( // Order of coordinates: X, Y, S, T
            0.0f - width / 2, 0.0f + height / 2,
            0.0f + width / 2, 0.0f + height / 2,
            0.0f + width / 2, 0.0f - height / 2,
            0.0f + width / 2, 0.0f - height / 2,
            0.0f - width / 2, 0.0f - height / 2,
            0.0f - width / 2, 0.0f + height / 2
        )
        vertexBuffer.position(0)
        vertexBuffer.put(vertexData, 0, vertexDataSize)
        vertexBuffer.position(0)
    }

    private fun updateTextureBuffer(
        positionX: Float,
        positionY: Float,
        width: Float,
        height: Float
    ) {
        val textureData = floatArrayOf( // Order of coordinates: X, Y, S, T
            positionX - width / 2, positionY - height / 2,
            positionX + width / 2, positionY - height / 2,
            positionX + width / 2, positionY + height / 2,
            positionX + width / 2, positionY + height / 2,
            positionX - width / 2, positionY + height / 2,
            positionX - width / 2, positionY - height / 2
        )
        textureBuffer.position(0)
        textureBuffer.put(textureData, 0, vertexDataSize)
        textureBuffer.position(0)
    }

    private fun bindData(textureProgram: ScaledTextureProgram) {
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
    }

    private var texturePosition = Pair(0f, 0f)
    var vertexPosition = Geometry.Point(0f, 0f, 0f)
    fun translate(draggedPoint: Geometry.Point) {
        val touchedPoint = Geometry.Point(
            draggedPoint.x - posXcorrection,
            draggedPoint.y - posYcorrection,
            0f
        )

        vertexPosition = Geometry.Point(
            clamp(
                touchedPoint.x,
                -1 * ratio + vertexWidth / 2,
                1 * ratio - vertexWidth / 2
            ),
            clamp(
                touchedPoint.y,
                -1 + vertexHeight / 2,
                1 - vertexHeight / 2
            ),
            0f// mallet.radius,
        )
        texturePosition = mapVertexToTextureCords(
            vertexPosition.x / ratio,
            vertexPosition.y
        )
        updateVertexBuffer(
            vertexWidth,
            vertexHeight
        )

        updateTextureBuffer(
            texturePosition.first,
            texturePosition.second,
            textureWidth,
            textureHeight,
        )
        ////////////////////
        setIdentityM(modelMatrix, 0)
        translateM(modelMatrix, 0, vertexPosition.x, vertexPosition.y, 0f)
        /////////////////////
    }


    fun draw(textureProgram: ScaledTextureProgram, modelViewProjectionMatrix: FloatArray) {
        bindData(textureProgram)
        textureProgram.useProgram()
        textureProgram.setUniforms(modelViewProjectionMatrix)
        glDrawArrays(GL_TRIANGLES, 0, 6)
    }

    var vertexWidth = 0f
    var vertexHeight = 0f

    private var textureWidth = 0f
    private var textureHeight = 0f

    fun configure(
        magnification: Float,
        magnifierSizeW: Float,
        magnifierSizeH: Float,
    ) {
        this.magnification = magnification
        this.magnifierSizeW = magnifierSizeW
        this.magnifierSizeH = magnifierSizeH

        vertexWidth = maxOf(width, height) * magnifierSizeW//1.0f/ratio// wid*magnifierSizeW
        vertexHeight = minOf(width, height) * magnifierSizeH//1.0f// he*magnifierSizeW

        textureWidth = 1f * (magnifierSizeW / magnification) // 0.0625fratio
        textureHeight = 1f * (magnifierSizeH / magnification) // 0.0625f


        updateVertexBuffer(
            vertexWidth,
            vertexHeight
        )
        texturePosition = mapVertexToTextureCords(
            vertexPosition.x / ratio,
            vertexPosition.y
        )
        updateTextureBuffer(
            texturePosition.first,
            texturePosition.second,
            textureWidth,
            textureHeight,
        )
        setIdentityM(modelMatrix, 0)
        translateM(modelMatrix, 0, 0f, 0f, 0f)

    }
    private var width:Float=0f
    private var height:Float=0f
    fun onRatioChanged(ratio:Float){
        this.ratio = ratio
        this.height = 2f
        this.width = ratio * height
    }

}