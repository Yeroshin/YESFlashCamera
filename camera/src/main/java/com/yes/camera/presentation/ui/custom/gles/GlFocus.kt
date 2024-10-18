package com.yes.camera.presentation.ui.custom.gles

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES10.glDrawArrays
import android.opengl.GLES10.glTexImage2D
import android.opengl.GLES20
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA
import android.opengl.GLES20.GL_RGBA
import android.opengl.GLES20.GL_SRC_ALPHA
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.GL_UNSIGNED_BYTE
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glBlendFunc
import android.opengl.GLES20.glEnable
import android.opengl.GLUtils
import android.opengl.Matrix.setIdentityM
import android.opengl.Matrix.translateM
import androidx.core.math.MathUtils.clamp
import com.yes.camera.R
import com.yes.camera.utils.Geometry


class GlFocus(
    val glShaderProgram: GLRenderer.GlShaderProgram,
    val context: Context
) :
GLRenderer.GLObject(glShaderProgram) {
    private var magnification = 4.0f
    private var magnifierSizeW = 0.5f
    private var magnifierSizeH = 0.5f
    private var ratio = 1f

    override val vertexData = FloatArray(12)
    override val textureData = FloatArray(12)

    private var posXcorrection = 0f
    private var posYcorrection = 0f


    override fun setSelected(pressed: Boolean, touchedPointX: Float, touchedPointY: Float) {
        this.selected = pressed
        posXcorrection = touchedPointX - centerPosition.first
        posYcorrection = touchedPointY - centerPosition.second
    }

    private fun mapVertexToTextureCords(
        vertexPositionX: Float,
        vertexPositionY: Float
    ): Pair<Float, Float> {
        val textureX = (vertexPositionX + 1.0f) / 2.0f
        val textureY = 1.0f - (vertexPositionY + 1.0f) / 2.0f

        return Pair(textureX, textureY)
    }


    private var texturePosition = Pair(0f, 0f)

    override fun translate(draggedPointX: Float, draggedPointY: Float) {
        val touchedPoint = Geometry.Point(
            draggedPointX - posXcorrection,
            draggedPointY - posYcorrection,
            0f
        )

        centerPosition = Triple(
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
            centerPosition.first / ratio,
            centerPosition.second
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
        translateM(modelMatrix, 0, centerPosition.first, centerPosition.second, 0f)
        /////////////////////
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
            centerPosition.first / ratio,
            centerPosition.second
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

    private var width: Float = 0f
    private var height: Float = 0f
    override fun onRatioChanged(ratio: Float) {
        this.ratio = ratio
        this.height = 2f
        this.width = ratio * height
    }
    init {
        loadTexture(context)
    }
    val textureHandle =2
    fun loadTexture(context: Context) {



            val options = BitmapFactory.Options()
            options.inScaled = false // No pre-scaling

            // Read in the resource
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.center_focus_weak_24dp, options)

            // Bind to the texture in OpenGL

            glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)


            // Set filtering
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST
            )

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle()
    }

    override fun draw(modelViewProjectionMatrix: FloatArray) {



       val  mTextureUniformHandle = GLES20.glGetUniformLocation( textureProgram.program, "u_TextureUnit")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, textureHandle)
        GLES20.glUniform1i(mTextureUniformHandle, 1)

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        bindData()
        textureProgram.useProgram()
        textureProgram.setUniforms(modelViewProjectionMatrix)
        glDrawArrays(GL_TRIANGLES, 0, 6)
    }
}
