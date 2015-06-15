uniform mat4 g_WorldViewProjectionMatrix;
uniform vec4 m_Color;

attribute vec3 inPosition;

attribute vec2 inTexCoord;
varying vec2 texCoord;

varying vec4 color;

void main() {
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
    texCoord = inTexCoord;
    color = m_Color;
}