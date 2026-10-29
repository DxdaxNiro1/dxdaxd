package com.dreamcore.game

/** One in-shader grade for all worlds, including the compatible GLES2 path. No extra framebuffer. */
object DreamAtmosphere32 {
    fun grade(source:String):String{
        if(source.contains("void dreamMain32"))return source
        return "uniform mediump vec4 uChapter33;uniform mediump vec4 uArtProfile33;\n"+source.replace("void main()", "void dreamMain32()")+"""
        void main(){
            dreamMain32();
            vec3 c=clamp(gl_FragColor.rgb,0.0,1.0);
            float l=dot(c,vec3(.2126,.7152,.0722));
            c=mix(vec3(l),c,.87);
            // Cool shadow separation and a soft warm shoulder preserve route readability.
            c*=mix(vec3(.76,.86,1.04),vec3(1.01,.98,.91),smoothstep(.06,.82,l));
            c=c*(.88+.12*c);
            c*=vec3(1.0)+uArtProfile33.xyz;c=mix(vec3(dot(c,vec3(.2126,.7152,.0722))),c,1.0+uArtProfile33.w);c=mix(c,c*vec3(1.10,.93,.72),uChapter33.x*.48);c*=1.0-uChapter33.y*.30;c=mix(c,c*vec3(1.03,1.0,.93),uChapter33.w*.20);gl_FragColor.rgb=clamp(c,0.0,1.0);
        }
        """.trimIndent()
    }
}
