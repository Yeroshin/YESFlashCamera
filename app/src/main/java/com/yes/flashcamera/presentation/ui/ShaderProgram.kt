package com.yes.flashcamera.presentation.ui

import android.content.Context
import android.opengl.GLES20.GL_COMPILE_STATUS
import android.opengl.GLES20.GL_FRAGMENT_SHADER
import android.opengl.GLES20.GL_LINK_STATUS
import android.opengl.GLES20.GL_VERTEX_SHADER
import android.opengl.GLES20.glAttachShader
import android.opengl.GLES20.glCompileShader
import android.opengl.GLES20.glCreateProgram
import android.opengl.GLES20.glCreateShader
import android.opengl.GLES20.glDeleteProgram
import android.opengl.GLES20.glDeleteShader
import android.opengl.GLES20.glGetProgramiv
import android.opengl.GLES20.glGetShaderiv
import android.opengl.GLES20.glLinkProgram
import android.opengl.GLES20.glShaderSource
import android.opengl.GLES20.glUseProgram
import com.yes.flashcamera.presentation.ui.MainActivity.FileUtils
import com.yes.flashcamera.presentation.ui.ShaderProgram.ShaderHelper.createShader


abstract class ShaderProgram protected constructor(
    context: Context, vertexShaderResourceId: Int,
    fragmentShaderResourceId: Int
) {


    protected  val A_POSITION: String = "a_Position"
    protected val U_MATRIX: String = "u_Matrix"
    protected  val A_COLOR: String = "a_Color"


    protected val program: Int = ShaderHelper.buildProgram(
        createShader(
            context, GL_VERTEX_SHADER, vertexShaderResourceId
        ),
        createShader(
            context, GL_FRAGMENT_SHADER, fragmentShaderResourceId
        )
    )

    fun useProgram() {
        // Set the current OpenGL shader program to this program.
        glUseProgram(program)
    }

    object ShaderHelper {
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