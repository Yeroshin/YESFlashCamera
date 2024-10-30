#version 310 es

precision mediump float; // Устанавливаем точность для float

// Входящие переменные
in vec4 a_Position; // Позиция
in vec2 a_TextureCoordinates; // Координаты текстурирования первой текстуры
in vec2 aTexCord2; // Координаты текстурирования второй текстуры

// Унформы
uniform mat4 u_Matrix; // Матрица преобразования

// Выходящие переменные
out vec2 vTexCoord1; // Передаем координаты текстурирования первой текстуры во фрагментный шейдер
out vec2 vTexCoord2; // Передаем координаты текстурирования второй текстуры во фрагментный шейдер

void main()
{
    gl_Position = u_Matrix * a_Position; // Устанавливаем позицию
    vTexCoord1 = a_TextureCoordinates; // Передаем текстурные координаты первой текстуры
    vTexCoord2 = aTexCord2; // Передаем текстурные координаты второй текстуры
}