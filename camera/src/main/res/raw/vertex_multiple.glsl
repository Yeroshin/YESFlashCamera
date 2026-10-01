uniform mat4 u_Matrix;
uniform vec2 u_HalfSize;
attribute vec4 a_Position;
attribute vec2 a_TextureCoordinates;
attribute vec2 aTexCord2;

varying vec2 vTexCoord1;
varying vec2 vTexCoord2;
varying vec2 vNormalizedPos;

void main()
{
    gl_Position = u_Matrix * a_Position;
    vTexCoord1 = a_TextureCoordinates;
    vTexCoord2 = aTexCord2;

    vec2 halfSize = max(u_HalfSize, vec2(0.0001, 0.0001));
    vNormalizedPos = a_Position.xy / halfSize;
}
