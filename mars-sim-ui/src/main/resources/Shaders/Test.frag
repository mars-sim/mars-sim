vec2 resolution = vec2(1.0,1.0);

uniform float g_Time;
uniform sampler2D m_Texture;
varying vec2 texCoord;

float rand(vec2 co) {
    return fract(sin(dot(co.xy ,vec2(12.9898,708.233))) * 403758.5453);
}

void main() {
    gl_FragColor = vec4(1.0,0.0,0.0,1.0);
}

