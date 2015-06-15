vec2 resolution = vec2(1.0,1.0);

uniform float g_Time;
uniform sampler2D m_Texture;
varying vec2 texCoord;

float rand(vec2 co) {
    return fract(sin(dot(co.xy ,vec2(12.9898,708.233))) * 403758.5453);
}

void main() {
    vec2 q1 = texCoord.xy / resolution.xy;
    vec2 uv = 0.5 + (q1-0.5)*(0.98 + 0.006);//*sin(0.9));
    vec3 col = texture2D(m_Texture,texCoord).xyz;

    col = clamp(col*0.5+0.5*col*col*1.2,0.0,1.0);
    col *= 0.6 + 0.4*16.0*uv.x*uv.y*(1.0-uv.x)*(1.0-uv.y);
    col *= vec3(0.9,1.0,0.7);
    col *= 0.8+0.2*sin(10.0*g_Time+uv.y*512.0);
    col *= 1.0-0.9*rand(vec2(g_Time*100.0, tan(g_Time)));
    float comp = smoothstep( 0.2, 0.7, sin(g_Time) );
    
    gl_FragColor = vec4(col,texture2D(m_Texture,texCoord).w);
}

