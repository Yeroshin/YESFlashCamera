package com.yes.camera.presentation.ui.custom.gles

import android.opengl.GLES10.glDrawArrays
import android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES
import android.opengl.GLES20
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_ONE
import android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glBlendFunc
import android.opengl.GLES20.glDisable
import android.opengl.GLES20.glEnable
import android.opengl.GLES20.glUniform1i
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
    override fun draw(modelViewProjectionMatrix: FloatArray) {


        val mTextureUniformHandle =
            GLES20.glGetUniformLocation(textureProgram.program, "u_TextureUnit")
        glActiveTexture(GLES20.GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, 1)

      /*  glEnable(GL_BLEND)
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)*/
        glUniform1i(mTextureUniformHandle, 0)

        /* glEnable(GL_BLEND)
         glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)*/

        bindData()
        textureProgram.useProgram()
        textureProgram.setUniforms(modelViewProjectionMatrix)
        glDrawArrays(GL_TRIANGLES, 0, 6)
       // glDisable(GL_BLEND)
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0)

    }

}