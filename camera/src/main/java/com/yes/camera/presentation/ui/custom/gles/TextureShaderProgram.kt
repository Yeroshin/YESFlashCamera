package com.yes.camera.presentation.ui.custom.gles


import android.content.Context
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniformMatrix4fv
import com.yes.flashcamera.R


class TextureShaderProgram(context: Context) : ShaderProgram(
    context, R.raw.vertex, R.raw.fragment
) {


    private val U_TEXTURE_UNIT: String = "u_TextureUnit"
    private  val A_TEXTURE_COORDINATES: String = "a_TextureCoordinates"

    val positionAttributeLocation = glGetAttribLocation(program, A_POSITION)
    val textureCoordinatesAttributeLocation = glGetAttribLocation(program, A_TEXTURE_COORDINATES)
    private val uMatrixLocation = glGetUniformLocation(program, U_MATRIX)
    private val uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT)



    fun setUniforms(matrix: FloatArray?) {
        // Pass the matrix into the shader program.
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)

        // Set the active texture unit to texture unit 0.
     //   glActiveTexture(GL_TEXTURE0)

        // Bind the texture to this unit.
      //  glBindTexture(GL_TEXTURE_2D, textureId)
//
        // Tell the texture uniform sampler to use this texture in the shader by
        // telling it to read from texture unit 0.
     //   glUniform1i(uTextureUnitLocation, 0)
    }
}