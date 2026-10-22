package com.dreamcore.game

object DreamLunarShaders27 {
    val vertexShader = """
        precision highp float;
        attribute vec3 aPosition;attribute vec3 aColor;attribute vec3 aNormal;attribute float aKind;
        uniform mat4 uVP;uniform vec3 uCamera;uniform vec3 uDirection;uniform float uDistance;uniform float uRadius;
        uniform vec2 uMood;uniform vec2 uEyeAim;uniform mediump float uRed;uniform mediump float uVariant;
        varying mediump vec3 vNormal;varying mediump vec3 vColor;varying mediump vec3 vLocal;varying mediump float vKind;

        // GLSL ES defines pow(x,y) only for nonnegative x. Squared signed
        // distances are common in eyelids/lights; mobile log2 lowering can produce NaN.
        float dreamPow(float x,float y){
            if(y==2.0){float q=clamp(x,-64.0,64.0);return q*q;}
            return pow(max(x,0.0),y);
        }
        vec2 dreamNormalize(vec2 v){float m=max(max(abs(v.x),abs(v.y)),.0001);vec2 q=v/m;return q*inversesqrt(max(dot(q,q),.0001));}
        vec3 dreamNormalize(vec3 v){float m=max(max(max(abs(v.x),abs(v.y)),abs(v.z)),.0001);vec3 q=v/m;return q*inversesqrt(max(dot(q,q),.0001));}

        void main(){
            vec3 p=aPosition;vKind=aKind;vColor=aColor;vLocal=p;
            float blink=uMood.x;
            if(aKind>1.5 && aKind<3.5){p.xy+=uEyeAim;p.y=.20+(p.y-.20)*(1.0-.95*blink);}
            if(aKind>3.5 && aKind<5.5){
                float side=aKind<4.5?1.0:-1.0;
                float xx=clamp((p.x-aColor.x)/.14,-1.0,1.0);
                float edge=.080*sqrt(max(.0,1.0-xx*xx));
                p.y=aColor.y+side*edge*(1.0-blink)+blink*(p.y-aColor.y);
            }
            if(aKind>.5 && aKind<5.5){
                bool left=aPosition.x<.0;vec2 target=left?vec2(.335,.395):vec2(.685,.395);
                if(uVariant>.5 && uVariant<1.5)target=left?vec2(.318,.391):vec2(.653,.393);
                if(uVariant>1.5 && uVariant<2.5)target=left?vec2(.318,.419):vec2(.688,.421);
                if(uVariant>2.5 && uVariant<3.5)target=left?vec2(.320,.417):vec2(.680,.419);
                if(uVariant>3.5 && uVariant<4.5)target=left?vec2(.355,.399):vec2(.708,.406);
                if(uVariant>4.5 && uVariant<5.5)target=left?vec2(.318,.411):vec2(.689,.400);
                if(uVariant>6.5)target=left?vec2(.320,.356):vec2(.662,.372);
                if(uRed>.5)target=left?vec2(.318,.391):vec2(.653,.393);
                vec2 centre=vec2((target.x-.5)/.48,(.5-target.y)/.48);
                vec2 original=vec2(left?-.34:.34,.20);
                if(uVariant>5.5 && uVariant<6.5){original=vec2(-.46,.20);centre=vec2(-.36,.34);}
                p.xy+=centre-original;p.z+=.13;
            }
            if(aKind<.5 || (aKind>5.5 && aKind<7.5)){
                float jaw=1.0+.15*uMood.y+.12*uRed;
                if(p.y<-.18){p.x*=1.0+.055*uMood.y;p.y=-.18+(p.y+.18)*jaw;}
            }
            p.z*=.55;
            vec3 d=dreamNormalize(uDirection);vec3 right=dreamNormalize(cross(d,vec3(0.0,1.0,0.0)));vec3 up=cross(right,d);
            vec3 world=uCamera+d*uDistance+(right*p.x+up*p.y-d*p.z)*uRadius;
            vNormal=dreamNormalize(aNormal/vec3(1.0,1.0,.55));
            gl_Position=uVP*vec4(world,1.0);
        }
    """.trimIndent()
    val fragmentShader = """
        precision mediump float;
        uniform mediump sampler2D uAtlas;uniform mediump float uVariant;uniform mediump float uRed;
        varying mediump vec3 vNormal;varying mediump vec3 vColor;varying mediump vec3 vLocal;varying mediump float vKind;

        // GLSL ES defines pow(x,y) only for nonnegative x. Squared signed
        // distances are common in eyelids/lights; mobile log2 lowering can produce NaN.
        float dreamPow(float x,float y){
            if(y==2.0){float q=clamp(x,-64.0,64.0);return q*q;}
            return pow(max(x,0.0),y);
        }
        vec2 dreamNormalize(vec2 v){float m=max(max(abs(v.x),abs(v.y)),.0001);vec2 q=v/m;return q*inversesqrt(max(dot(q,q),.0001));}
        vec3 dreamNormalize(vec3 v){float m=max(max(max(abs(v.x),abs(v.y)),abs(v.z)),.0001);vec3 q=v/m;return q*inversesqrt(max(dot(q,q),.0001));}

        vec3 face(vec2 uv,float tile){return texture2D(uAtlas,(clamp(uv,.015,.985)+vec2(mod(tile,2.0),floor(tile/2.0)))/vec2(2.0,4.0)).rgb;}
        void main(){
            vec3 n=dreamNormalize(vNormal);vec3 light=dreamNormalize(vec3(-.52,.57,1.0));float diffuse=max(dot(n,light),0.0);
            vec3 albedo=vColor;
            float warm=(abs(uVariant-1.0)<.1 || abs(uVariant-3.0)<.1 || uVariant>5.5)?1.0:0.0;
            if(vKind<.5){
                vec2 uv=vColor.xy;
                if(abs(uVariant-6.0)<.1)uv.x+=.22*(1.0-abs(vLocal.y));
                albedo=face(uv,uVariant);if(uRed>.0)albedo=mix(albedo,face(uv,1.0),uRed);
                // Remove the baked points of light; the independent moving pupils supply them.
                vec2 l=vec2(.335,.395);vec2 r=vec2(.685,.395);
                if(uVariant>.5 && uVariant<1.5){l=vec2(.318,.391);r=vec2(.653,.393);}
                if(uVariant>1.5 && uVariant<2.5){l=vec2(.318,.419);r=vec2(.688,.421);}
                if(uVariant>2.5 && uVariant<3.5){l=vec2(.320,.417);r=vec2(.680,.419);}
                if(uVariant>3.5 && uVariant<4.5){l=vec2(.355,.399);r=vec2(.708,.406);}
                if(uVariant>4.5 && uVariant<5.5){l=vec2(.318,.411);r=vec2(.689,.400);}
                if(uVariant>5.5 && uVariant<6.5){l=vec2(.47,.336);r=l;}
                if(uVariant>6.5){l=vec2(.320,.356);r=vec2(.662,.372);}
                if(uRed>.5){l=vec2(.318,.391);r=vec2(.653,.393);}
                float hole=max(1.0-smoothstep(.015,.036,length((uv-l)*vec2(1.0,1.1))),1.0-smoothstep(.015,.036,length((uv-r)*vec2(1.0,1.1))));
                albedo=mix(albedo,vec3(.009,.012,.020),hole*.95);
            }
            if(vKind>3.5 && vKind<5.5)albedo=face(vec2(.5+vLocal.x*.48,.24+(vLocal.y-.2)*.35),uRed>.5?1.0:uVariant)*.67;
            if(vKind>7.5)albedo=face(vec2(.15+vLocal.x*.1,.2+vLocal.y*.1),uVariant);
            vec3 color=albedo*(.48+.72*diffuse);
            color+=vec3(.05,.12,.23)*dreamPow(1.0-max(n.z,0.0),3.0)*.35;
            if(vKind>1.5 && vKind<2.5)color=mix(vec3(.10,.40,1.0),vec3(1.0,.12,.025),max(uRed,warm))*1.35;
            if(vKind>2.5 && vKind<3.5)color=vec3(.001,.002,.006);
            if(vKind>6.5 && vKind<7.5)color=vec3(.008,.001,.005);
            gl_FragColor=vec4(color,1.0);
        }
    """.trimIndent()
}
