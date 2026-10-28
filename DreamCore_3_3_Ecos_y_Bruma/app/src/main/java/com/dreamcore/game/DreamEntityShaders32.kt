package com.dreamcore.game

object DreamEntityShaders32 {
    val vertex="""
    attribute vec3 aPos;attribute vec3 aColor;attribute vec3 aNormal;attribute float aKind;
    uniform mat4 uVP;uniform mat4 uModel;uniform vec3 uEye;uniform mediump float uTime;uniform mediump vec4 uArt33;uniform mediump float uGesture33;
    varying mediump vec3 vUV;varying mediump vec3 vNormal;varying mediump vec3 vLocal;varying mediump float vPart;varying mediump float vDistance;varying mediump float vWorldY;
    void main(){vec3 p=aPos;vec3 n=aNormal;
        if(aKind>1.5&&aKind<3.5){float a=uArt33.x*max(0.0,abs(p.x)-.4);p.z+=a;p.y+=a*.35;}
        if(aKind<1.5){p.z+=uArt33.y*(1.0-smoothstep(3.5,4.5,p.y));if(abs(p.x)>.75&&p.y>1.7&&p.y<2.7)p.y+=uGesture33*.12;}
        if(aKind>7.5&&aKind<8.5){float jaw=1.0-smoothstep(4.4,4.7,p.y);p.y-=jaw*uArt33.z*.10;p.z+=jaw*uArt33.z*.025;float a=uArt33.y;p.x+=sin(a)*(p.y-4.2);p.z+=uArt33.y*.4;}
        if(aKind>.5&&aKind<1.5){float w=(1.0-clamp(p.y/4.0,0.0,1.0));p.z+=sin(uTime*.5+p.x*3.1)*.025*w;}
        vec4 world=uModel*vec4(p,1.0);vWorldY=world.y;vNormal=mat3(uModel)*n;vUV=aColor;vPart=aKind;vLocal=p;vDistance=length(world.xyz-uEye);gl_Position=uVP*world;
    }
    """.trimIndent()
    val fragment="""
    precision mediump float;
    uniform sampler2D uAtlas;uniform mediump float uTime;uniform mediump vec4 uArt33;uniform mediump float uGesture33;uniform float uNight;uniform float uOpen;uniform float uClipY;
    varying mediump vec3 vUV;varying mediump vec3 vNormal;varying mediump vec3 vLocal;varying mediump float vPart;varying mediump float vDistance;varying mediump float vWorldY;
    void main(){
        if(vWorldY<uClipY+.01)discard;
        if(vPart>.5&&vPart<1.5&&uOpen>.01&&vLocal.y>.75&&vLocal.y<3.25&&abs(vLocal.x)<.29*uOpen*(.6+.4*sin(vLocal.y*1.2)))discard;
        vec2 q=clamp(vUV.xy,.018,.982);float tile=floor(vUV.z+.5);
        if(vPart>3.5&&vPart<4.5){q+=vec2(sin(uTime*.12),sin(uTime*.16))*.018;q=clamp(q,.025,.975);}
        vec2 uv=(vec2(mod(tile,4.0),floor(tile/4.0))+.012+q*.976)*.25;
        vec3 albedo=texture2D(uAtlas,uv).rgb;
        if(vPart>3.5&&vPart<4.5)albedo=mix(albedo,vec3(.55,.51,.43),uArt33.w*(1.0-smoothstep(.1,.48,abs(vUV.y-.5))));
        vec3 n=vNormal/max(length(vNormal),.001);
        float facing=abs(dot(n,normalize(vec3(-.35,.68,.62))));
        vec3 light=mix(vec3(.26,.33,.43),vec3(.12,.21,.38),uNight)+mix(vec3(.82,.72,.56),vec3(.67,.74,1.0),uNight)*facing*.82;
        float fold=.82+.18*max(0.0,n.y);
        vec3 c=sqrt(max(albedo*albedo*light*fold,vec3(0.0)));
        if(vPart>3.5&&vPart<4.5)c=max(c,albedo*.64);
        if(vPart>5.5&&vPart<6.5)c*=.72;
        float fog=1.0-exp(-max(0.0,vDistance-90.0)/230.0);
        gl_FragColor=vec4(mix(c,mix(vec3(.20,.25,.29),vec3(.045,.065,.10),uNight),fog*.65),1.0);
    }
    """.trimIndent()
}
