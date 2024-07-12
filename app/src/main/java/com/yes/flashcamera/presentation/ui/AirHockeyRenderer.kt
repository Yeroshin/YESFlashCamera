package com.yes.flashcamera.presentation.ui



import android.content.Context
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10




class AirHockeyRenderer(
    private val context: Context
) : GLSurfaceView.Renderer {
   // private val vertexData: FloatBuffer


   // private var program:Int=0
 /*   private var aPositionLocation = 0
    private var aColorLocation = 0*/

   /* init {
        /*
       float[] tableVerticesWithTriangles = {
           // Triangle Fan
              0,     0,
           -0.5f, -0.5f,
            0.5f, -0.5f,
            0.5f,  0.5f,
           -0.5f,  0.5f,
           -0.5f, -0.5f,

           // Line 1
           -0.5f, 0f,
            0.5f, 0f,

           // Mallets
           0f, -0.25f,
           0f,  0.25f
       };*/

        //
        // Vertex data is stored in the following manner:
        //
        // The first two numbers are part of the position: X, Y
        // The next three numbers are part of the color: R, G, B
        //
        val tableVerticesWithTriangles = floatArrayOf( // Order of coordinates: X, Y, R, G, B
            // Triangle Fan
            0f, 0f, 1f, 1f, 1f,
            -0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
            0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
            0.5f, 0.5f, 0.7f, 0.7f, 0.7f,
            -0.5f, 0.5f, 0.7f, 0.7f, 0.7f,
            -0.5f, -0.5f, 0.7f, 0.7f, 0.7f,  // Line 1

            -0.5f, 0f, 1f, 0f, 0f,
            0.5f, 0f, 1f, 0f, 0f,  // Mallets

            0f, -0.25f, 0f, 0f, 1f,
            0f, 0.25f, 1f, 0f, 0f

        )

        vertexData = ByteBuffer
            .allocateDirect(tableVerticesWithTriangles.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()

        vertexData.put(tableVerticesWithTriangles)
    }*/
    private val mallet by lazy {
        Mallet()
    }
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

      /*  val vertexShaderSource: String = TextResourceReader
            .readTextFileFromResource(context, R.raw.simple_vertex)
        val fragmentShaderSource: String = TextResourceReader
            .readTextFileFromResource(context, R.raw.simple_fragment)

        val vertexShader: Int = ShaderHelper.compileVertexShader(vertexShaderSource)
        val fragmentShader: Int = ShaderHelper
            .compileFragmentShader(fragmentShaderSource)

        program = ShaderHelper.linkProgram(vertexShader, fragmentShader)



        glUseProgram(program)*/
        val program=ColorShaderProgram(context)
        program.useProgram()
        mallet.bindData(program)
      /*  aPositionLocation = program.positionAttributeLocation
        aColorLocation = program.colorAttributeLocation

      /*  aPositionLocation = glGetAttribLocation(program, A_POSITION)
        aColorLocation = glGetAttribLocation(program, A_COLOR)*/


        // Bind our data, specified by the variable vertexData, to the vertex
        // attribute at location A_POSITION_LOCATION.
        vertexData.position(0)
        glVertexAttribPointer(
            aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT,
            false, STRIDE, vertexData
        )

        glEnableVertexAttribArray(aPositionLocation)


        // Bind our data, specified by the variable vertexData, to the vertex
        // attribute at location A_COLOR_LOCATION.
        vertexData.position(POSITION_COMPONENT_COUNT)
        glVertexAttribPointer(
            aColorLocation, COLOR_COMPONENT_COUNT, GL_FLOAT,
            false, STRIDE, vertexData
        )

        glEnableVertexAttribArray(aColorLocation)*/
    }

    override fun onSurfaceChanged(glUnused: GL10?, width: Int, height: Int) {
        // Set the OpenGL viewport to fill the entire surface.
        glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(glUnused: GL10?) {


        glClearColor(1.0f, 0.0f, 0.0f, 0.0f)
        glClear(GL_COLOR_BUFFER_BIT)
        mallet.draw()
        // Draw the table.
    /*    glDrawArrays(GL_TRIANGLE_FAN, 0, 6)

        // Draw the center dividing line.
        glDrawArrays(GL_LINES, 6, 2)

        // Draw the first mallet.
        glDrawArrays(GL_POINTS, 8, 1)

        // Draw the second mallet.
        glDrawArrays(GL_POINTS, 9, 1)*/
    }

   /* companion object {
        private const val A_POSITION = "a_Position"
        private const val A_COLOR = "a_Color"
        private const val POSITION_COMPONENT_COUNT = 2
        private const val COLOR_COMPONENT_COUNT = 3
        private const val BYTES_PER_FLOAT = 4
        private const val STRIDE =
            (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT
    }*/
}
/*object ShaderHelper {
    private const val TAG = "ShaderHelper"

    /**
     * Loads and compiles a vertex shader, returning the OpenGL object ID.
     */
    fun compileVertexShader(shaderCode: String): Int {
        return compileShader(GL_VERTEX_SHADER, shaderCode)
    }

    /**
     * Loads and compiles a fragment shader, returning the OpenGL object ID.
     */
    fun compileFragmentShader(shaderCode: String): Int {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode)
    }

    /**
     * Compiles a shader, returning the OpenGL object ID.
     */
    private fun compileShader(type: Int, shaderCode: String): Int {
        // Create a new shader object.
        val shaderObjectId = glCreateShader(type)

        if (shaderObjectId == 0) {


            return 0
        }


        // Pass in the shader source.
        glShaderSource(shaderObjectId, shaderCode)

        glCompileShader(shaderObjectId)

        val compileStatus = IntArray(1)
        glGetShaderiv(
            shaderObjectId, GL_COMPILE_STATUS,
            compileStatus, 0
        )
        if (compileStatus[0] == 0) {
            // If it failed, delete the shader object.
            glDeleteShader(shaderObjectId)
            return 0
        }


        // Return the shader object ID.
        return shaderObjectId
    }

    fun linkProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {
        // Create a new program object.
        val programObjectId = glCreateProgram()

        if (programObjectId == 0) {


            return 0
        }

        glAttachShader(programObjectId, vertexShaderId)
        glAttachShader(programObjectId, fragmentShaderId)
        glLinkProgram(programObjectId)

        return programObjectId
    }


}
object TextResourceReader {

    fun readTextFileFromResource(
        context: Context,
        resourceId: Int
    ): String {
        val body = StringBuilder()

        try {
            val inputStream = context.resources
                .openRawResource(resourceId)
            val inputStreamReader = InputStreamReader(
                inputStream
            )
            val bufferedReader = BufferedReader(
                inputStreamReader
            )

            var nextLine: String?

            while ((bufferedReader.readLine().also { nextLine = it }) != null) {
                body.append(nextLine)
                body.append('\n')
            }
        } catch (e: IOException) {
            throw RuntimeException(
                "Could not open resource: $resourceId", e
            )
        } catch (nfe: Resources.NotFoundException) {
            throw RuntimeException(
                "Resource not found: "
                        + resourceId, nfe
            )
        }

        return body.toString()
    }
}*/
