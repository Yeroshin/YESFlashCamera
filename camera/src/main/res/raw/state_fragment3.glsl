#version 320 es
precision mediump float;

uniform mediump sampler2DArray u_TextureUnit; // Используем sampler2DArray
in vec2 v_TextureCoordinates; // Изменено с varying на in
uniform int selectedLayer;

out vec4 fragColor; // Используем out вместо gl_FragColor

void main()
{
   vec3 texCoord = vec3(v_TextureCoordinates, float(0));
    fragColor = texture(u_TextureUnit, texCoord); // Применяем цвет
}