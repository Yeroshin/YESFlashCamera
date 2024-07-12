package com.yes.flashcamera.presentation.ui


import android.content.Context
import android.opengl.GLES20.GL_COMPILE_STATUS
import android.opengl.GLES20.GL_FRAGMENT_SHADER
import android.opengl.GLES20.GL_LINK_STATUS
import android.opengl.GLES20.GL_TEXTURE0
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_VERTEX_SHADER
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glAttachShader
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glCompileShader
import android.opengl.GLES20.glCreateProgram
import android.opengl.GLES20.glCreateShader
import android.opengl.GLES20.glDeleteProgram
import android.opengl.GLES20.glDeleteShader
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetProgramiv
import android.opengl.GLES20.glGetShaderiv
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glLinkProgram
import android.opengl.GLES20.glShaderSource
import android.opengl.GLES20.glUniform1i
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES20.glUseProgram
import com.yes.flashcamera.R
import com.yes.flashcamera.presentation.ui.MainActivity.FileUtils
import com.yes.flashcamera.presentation.ui.TextureShaderProgram.ShaderHelper.createShader


class TextureShaderProgram(val context: Context)  {


    private val U_TEXTURE_UNIT: String = "u_TextureUnit"
    private  val A_TEXTURE_COORDINATES: String = "a_TextureCoordinates"
/*
    val positionAttributeLocation = glGetAttribLocation(program, A_POSITION)
    val textureCoordinatesAttributeLocation = glGetAttribLocation(program, A_TEXTURE_COORDINATES)
    private val uMatrixLocation = glGetUniformLocation(program, U_MATRIX)
    private val uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT)

*/
var program=0


    fun useProgram(program:Int) {

        // Set the current OpenGL shader program to this program.
        glUseProgram(program)
    }
    fun setUniforms(matrix: FloatArray?, textureId: Int) {
        // Pass the matrix into the shader program.
     //   glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)

        // Set the active texture unit to texture unit 0.
      //  glActiveTexture(GL_TEXTURE0)

        // Bind the texture to this unit.
        glBindTexture(GL_TEXTURE_2D, textureId)
//
        // Tell the texture uniform sampler to use this texture in the shader by
        // telling it to read from texture unit 0.
      //  glUniform1i(uTextureUnitLocation, 0)
    }
    object ShaderHelper {
        fun build(context: Context):Int{
            return  buildProgram(
                createShader(
                    context, GL_VERTEX_SHADER, R.raw.vertex
                ),
                createShader(
                    context, GL_FRAGMENT_SHADER, R.raw.fragment
                )
            )
        }
        fun buildProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {
            val programId = glCreateProgram()
            if (programId == 0) {
                return 0
            }
            glAttachShader(programId, vertexShaderId)
            glAttachShader(programId, fragmentShaderId)
            glLinkProgram(programId)
            val linkStatus = IntArray(1)
            glGetProgramiv(programId, GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                glDeleteProgram(programId)
                return 0
            }
            return programId
        }

        fun createShader(context: Context, type: Int, shaderRawId: Int): Int {
            val shaderText = FileUtils.readTextFromRaw(context, shaderRawId)
            return createShader(type, shaderText)
        }

        private fun createShader(type: Int, shaderText: String?): Int {

            val shaderId = glCreateShader(type)
            if (shaderId == 0) {
                return 0
            }
            glShaderSource(shaderId, shaderText)
            glCompileShader(shaderId)
            val compileStatus = IntArray(1)
            glGetShaderiv(shaderId, GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                glDeleteShader(shaderId)
                return 0
            }
            return shaderId
        }
    }
}