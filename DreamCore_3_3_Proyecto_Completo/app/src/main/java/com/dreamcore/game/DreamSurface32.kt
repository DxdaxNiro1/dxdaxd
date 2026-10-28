package com.dreamcore.game

/** Compact opaque stream. Every original triangle remains in exactly one layer. */
object DreamSurface32 {
    fun common(k:Float)=k<.5f||k in 3.5f..19.49f||k in 24.5f..27.49f||k in 39.5f..44.49f||k in 89.5f..91.49f||k in 92.5f..93.49f||k in 103.5f..104.49f||k in 107.5f..108.49f||k in 109.5f..110.49f||k in 110.5f..115.49f||k in 121.5f..122.49f
    fun partition(page:DreamIndexed28.Page):Pair<ShortArray,ShortArray>{
        var n=0
        for(i in 0 until page.opaqueIndices step 3)if(common(page.vertices[(page.indices[i].toInt()and 65535)*10+9]))n+=3
        val a=ShortArray(n);val b=ShortArray(page.opaqueIndices-n);var x=0;var y=0
        for(i in 0 until page.opaqueIndices step 3){val solid=common(page.vertices[(page.indices[i].toInt()and 65535)*10+9]);for(j in 0..2)if(solid)a[x++]=page.indices[i+j]else b[y++]=page.indices[i+j]}
        return a to b
    }
    fun vertex(source:String):String = source.substringBefore("void main()")+"""
        void main(){
            vec3 p=aPos;
            if(aKind>121.5&&aKind<122.5)p.z+=sin(uTime*.8+aPos.x*3.0)*.08*(1.0-smoothstep(1.0,4.5,aPos.y));
            if(aKind>110.5&&aKind<111.5){
                vec2 d=p.xz-uPlay30.xy;float bend=max(0.0,1.0-length(d)/1.8)*.12;p.xz+=d*bend;
                float sway=sin(uTime*.19634954+aPos.x*.13+aPos.z*.11)*.085*(1.0-aColor.y);p.x+=sway;p.z+=sway*.55;
                if(aColor.z>7.5){float range=uLook.w>1.5?65.0:42.0;float visibility=(1.0-smoothstep(range*.65,range,distance(aPos.xz,uCamera.xz)))*step(.5,uLook.w);
                    p.y-=max(0.0,1.0-aColor.y)*1.2*(1.0-visibility);if(visibility<.02)p=vec3(aPos.x,aPos.y-(1.0-aColor.y)*1.2,aPos.z);}
            }
            if(uUseModel30>.5)p=(uModel30*vec4(p,1.0)).xyz;
            vec3 n=dreamNormalize(uUseModel30>.5?mat3(uModel30)*aNormal:aNormal);
            if(uWorld<29.5&&aKind>=4.0&&n.y<-.68)n.y=-n.y;
            bool crater=uWorld>1.5&&uWorld<2.5;
            if(crater&&((aKind>13.5&&aKind<14.5)||(aKind>26.5&&aKind<27.5))&&abs(n.y)<.45){vec2 radial=p.xz-vec2(0.0,-102.0);if(dot(n.xz,radial)>0.0)n.xz=-n.xz;}
            vColor=aColor;vPosition=p;if(p.x>1000.0)vPosition.x-=1600.0;
            vNormal=n;vEye100=(uCamera-p)*.01;vKind=aKind;
            float scale=crater?.30:(uWorld>29.5?1.0:.62);
            vTexCoord=(abs(n.y)>.66?vPosition.xz:(abs(n.x)>abs(n.z)?vPosition.zy:vPosition.xy))*scale;
            vShadow=uLightVP*vec4(p,1.0);vFog=0.0;vReflection=vec3(0.0);
            gl_Position=uVP*vec4(p,1.0);
        }
    """.trimIndent()
    fun fragment(source:String):String {
        // Keep the exact material, normal, PCF shadow and local fixture functions.
        // Exclude portals, lunar faces, water and character branches from this program.
        val at=source.indexOf("if(material<0.0)color*=")
        require(at>0)
        val foliage="""
            if(vKind>110.5&&vKind<111.5){
                float tile=20.0+floor(vColor.z+.5);vec2 uv=(vec2(mod(tile,4.0),floor(tile/4.0))+.018+clamp(vColor.xy,0.0,1.0)*.964)/vec2(4.0,8.0);
                vec4 leaf=texture2D(uAtlas,uv);if(leaf.a<.42)discard;
                vec3 n=dreamNormalize(vNormal);float solar=abs(dot(n,dreamNormalize(uSun)));
                vec3 light=mix(vec3(.48,.58,.65),vec3(.29,.39,.61),uLook.z)*uLook.x+vec3(1.0,.96,.80)*solar*.60*uLook.y;
                vec3 c=sqrt(max(leaf.rgb*leaf.rgb*light,vec3(0.0)));float d=length(vEye100)*100.0;
                c=mix(c,uFogColor,smoothstep(uFogNear,uFogFar,d)*.72);gl_FragColor=vec4(c,1.0);return;
            }
        """.trimIndent()
        val body=source.substring(0,at).replace("void main() {","void main() {\n"+foliage)
        fun effect(marker:String):String{
            val begin=source.indexOf(marker);require(begin>=0){marker};val open=source.indexOf('{',begin);var depth=1;var end=open+1
            while(depth>0){when(source[end++]){'{'->depth++;'}'->depth--}}
            return source.substring(begin,end)
        }
        val effects=listOf("if(vKind>26.5 && vKind<27.5) {","if (vKind > 24.5 && vKind < 26.5)","if(vKind>89.5 && vKind<90.5){","if(vKind>90.5 && vKind<91.5){","if(vKind>92.5 && vKind<93.5){","if(vKind>103.5 && vKind<104.5){vec2", "if(vKind>107.5 && vKind<108.5){", "if(vKind>109.5 && vKind<110.5){", "if(uWeather.x>1.5 && uWeather.x<2.5)","if(uWorld>15.5 && uWorld<16.5 && normal.y>.6)","if(uWorld>4.5 && uWorld<5.5 && normal.y>.65)","if(uWorld>3.5 && uWorld<4.5 && vKind<.5 && abs(normal.y)>.7)","if (crater && vKind > 13.5 && vKind < 14.5 && abs(normal.y) < .45)").joinToString("\n"){effect(it)}
        return body+effects+"""
            if(uStory.x>.5){vec3 d=(vPosition-uLamp)*.12;float g=max(0.0,1.0-dot(d,d));color+=vec3(.27,.14,.045)*g*g;}
            if(uTools.x>.5){vec3 ray=dreamNormalize(vPosition-(vPosition+vEye100*100.0));float cone=smoothstep(.84,.96,dot(ray,uTorchForward));float falloff=1.0/(1.0+eyeDistance*eyeDistance*.012);color+=vec3(.32,.27,.16)*cone*falloff;}
            if(uWorld>.5&&uWorld<1.5&&vPosition.y<-.13){float caustic=sin(vPosition.x*2.8+sin(vPosition.z*1.8+uTime*.42))*sin(vPosition.z*3.4+sin(vPosition.x*1.3-uTime*.37));color=color*vec3(.66,.88,.91)+vec3(.035,.07,.074)*dreamPow(max(0.0,caustic),6.0);}
            color*=1.0-uEclipse*.45;
            float fog=1.0-exp(-max(0.0,eyeDistance-uFogNear)/max(1.0,uFogFar-uFogNear));
            color=mix(color,uFogColor,fog*.68);
            color*=mix(vec3(1.0),vec3(.40,.48,.67),uExposure30.y)*(1.0+uExposure30.x*.18);
            gl_FragColor=vec4(clamp(color,0.0,1.0),1.0);
        }
        """.trimIndent()
    }
}
