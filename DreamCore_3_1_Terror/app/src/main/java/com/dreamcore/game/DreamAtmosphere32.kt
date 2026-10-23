package com.dreamcore.game

/** One in-shader grade for all worlds, including the compatible GLES2 path. No extra framebuffer. */
object DreamAtmosphere32 {
    fun grade(source:String):String{
        if(source.contains("void dreamMain32"))return source
        return source.replace("void main()", "void dreamMain32()")+"""
        void main(){
            dreamMain32();
            vec3 c=clamp(gl_FragColor.rgb,0.0,1.0);
            float l=dot(c,vec3(.2126,.7152,.0722));
            c=mix(vec3(l),c,.87);
            // Cool shadow separation and a soft warm shoulder preserve route readability.
            c*=mix(vec3(.76,.86,1.04),vec3(1.01,.98,.91),smoothstep(.06,.82,l));
            c=c*(.88+.12*c);
            gl_FragColor.rgb=clamp(c,0.0,1.0);
        }
        """.trimIndent()
    }
}
