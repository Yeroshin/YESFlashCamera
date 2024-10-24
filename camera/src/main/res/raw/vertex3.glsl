#version 320 es

// Declare the uniform matrix for transformations
uniform mat4 u_Matrix;

// Declare the attributes for position and texture coordinates
in vec4 a_Position;
in vec2 a_TextureCoordinates;

// Declare the varying variable to pass texture coordinates to the fragment shader
out vec2 v_TextureCoordinates;

void main()
{
    // Pass the texture coordinates to the fragment shader
    v_TextureCoordinates = a_TextureCoordinates;

    // Transform the vertex position using the uniform matrix
    gl_Position = u_Matrix * a_Position;
}