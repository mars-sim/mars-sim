#if defined(DISCARD_ALPHA)
    uniform float m_AlphaDiscardThreshold;
#endif

uniform sampler2D m_ColorMap;

varying vec2 texCoord;


void main(){
     
#if defined(SWIZZLE_MODE) && (SWIZZLE_MODE == 1)
    vec4 color = texture2D(m_ColorMap, texCoord).bgra;
#else
	vec4 color = texture2D(m_ColorMap, texCoord).rgba;
#endif

#if defined(DISCARD_ALPHA)
    if(color.a < m_AlphaDiscardThreshold){
       discard;
    }
#endif

    gl_FragColor = color;
}