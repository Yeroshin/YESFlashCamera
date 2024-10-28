uniform mat4 u_Matrix;
attribute vec4 a_Position;     // Позиция вершин
attribute vec2 a_TextureCoordinates;    // Координаты текстурирования для первой текстуры
attribute vec2 aTexCoord2;    // Координаты текстурирования для второй текстуры

varying vec2 vTexCoord1; // Передаем координаты текстурирования первой текстуры во фрагментный шейдер
varying vec2 vTexCoord2; // Передаем координаты текстурирования второй текстуры во фрагментный шейдер

void main()
{
    gl_Position = u_Matrix * a_Position; // Устанавливаем позицию
    vTexCoord1 = a_TextureCoordinates; // Передаем текстурные координаты первой текстуры
    vTexCoord2 = aTexCoord2; // Передаем текстурные координаты второй текстуры
}