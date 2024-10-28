precision mediump float;
uniform sampler2D u_TextureUnit;
varying vec2 v_TextureCoordinates;

void main()
{
   mat2 rotationMatrix = mat2(cos(45.0), -sin(45.0),
                                sin(45.0), cos(45.0));

    // Применяем поворот к координатам текстурирования
    vec2 rotatedCoords = rotationMatrix * (v_TextureCoordinates - 0.5) + 0.5; // Центрируем текстуру

    gl_FragColor = texture2D(u_TextureUnit, v_TextureCoordinates);
 //   gl_FragColor = texture2D(u_TextureUnit, rotatedCoords);
}