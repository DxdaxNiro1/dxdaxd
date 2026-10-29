package com.dreamcore.game

object DreamEntityShaders32 {
    val vertex="""
    attribute vec3 aPos;attribute vec3 aColor;attribute vec3 aNormal;attribute float aKind;
    uniform mediump vec4 uAttack35;uniform mediump vec3 uGrip35;uniform mediump float uFace35;uniform mediump vec2 uLimb35;uniform mat4 uVP;uniform mat4 uModel;uniform vec3 uEye;uniform mediump float uTime;uniform mediump vec4 uArt33;uniform mediump float uGesture33;
    varying mediump vec3 vUV;varying mediump vec3 vNormal;varying mediump vec3 vLocal;varying mediump float vPart;varying mediump float vDistance;varying mediump float vWorldY;
    void main(){vec3 p=aPos;vec3 n=aNormal;
        if(aKind>1.5&&aKind<3.5){float a=uArt33.x*max(0.0,abs(p.x)-.4);p.z+=a;p.y+=a*.35;}
        if(aKind<1.5){p.z+=uArt33.y*(1.0-smoothstep(3.5,4.5,p.y));if(abs(p.x)>.75&&p.y>1.7&&p.y<2.7)p.y+=uGesture33*.12;}
        if(aKind>7.5&&aKind<8.5){float jaw=1.0-smoothstep(4.4,4.7,p.y);p.y-=jaw*uArt33.z*.10;p.z+=jaw*uArt33.z*.025;float a=uArt33.y;p.x+=sin(a)*(p.y-4.2);p.z+=uArt33.y*.4;}
        if(aKind>.5&&aKind<1.5){float w=(1.0-clamp(p.y/4.0,0.0,1.0));p.z+=sin(uTime*.5+p.x*3.1)*.025*w;}
        float grip=uAttack35.x;float rage=uAttack35.y;float style=uAttack35.z;float clock=uAttack35.w;
        if(grip>.001){
            float sway=sin(clock*(1.2+style*.23));
            if(aKind>8.5&&aKind<10.5){float side=aKind<9.5?-1.0:1.0;float weight=clamp((uLimb35.x-aPos.y)/max(.1,uLimb35.x-uLimb35.y),0.0,1.0);vec3 hand=vec3(side*1.02,uLimb35.y,.13);vec3 goal=uGrip35+vec3(side*.32,-.35,-.28);p+=grip*weight*(goal-hand);p.x+=side*sway*.06*rage*weight;p.z+=rage*max(0.0,uLimb35.y-aPos.y)*.12;}
            if(aKind>1.5&&aKind<3.5){float side=aKind<2.5?-1.0:1.0;float wing=max(0.0,abs(p.x)-.4);p.x-=side*wing*grip*.32*(1.0-rage*.5);p.z+=wing*grip*.62;p.y+=sin(clock+style)*wing*rage*.12;}
            if(aKind>7.5&&aKind<8.5){p.x+=sway*.07*rage;p.z+=rage*.10;float jaw=1.0-smoothstep(uFace35-.2,uFace35+.02,aPos.y);p.y-=jaw*rage*.24;}
            if(aKind>10.5&&aKind<12.5){if(aKind>11.5){p.y-=rage*.27;p.z+=rage*.04;}else{p.y+=rage*.045;}}
            if(style>1.5&&style<2.5&&p.y>3.9){p.x+=grip*.16;p.z+=rage*.22;}
            if(style>3.5&&style<4.5){p.x*=1.0+rage*.10;p.z+=sin(p.y*2.0+clock*3.0)*rage*.045;}
            if(style>4.5&&style<5.5&&p.y>4.2){p.z+=(p.y-4.2)*grip*.35;p.y-=grip*.20;}
            if(style>5.5&&style<6.5&&aKind>1.5&&aKind<3.5)p.y+=rage*.8;
            if(style>6.5&&style<7.5&&p.y>3.5){vec2 q=p.xy-vec2(0.0,4.35);float a=grip*.22*sin(clock*1.3);p.xy=vec2(cos(a)*q.x-sin(a)*q.y,sin(a)*q.x+cos(a)*q.y)+vec2(0.0,4.35);}
            if(style>7.5&&style<8.5&&p.y>7.0)p.z+=grip*(p.y-7.0)*.2;
            if(style>8.5&&style<9.5&&p.y>2.5){p.xz*=1.0-rage*.14;p.y+=grip*.1;}
            if(style>9.5&&style<10.5){p.y-=grip*.25;p.z+=sin(clock*2.0+p.x)*rage*.12;}
            if(style>10.5&&style<11.5&&p.y>3.7)p.x+=(p.y-3.7)*grip*.22;
            if(style>11.5&&style<12.5)p.z+=max(0.0,p.y-2.0)*grip*.18;
            if(style>12.5&&style<13.5)p.y-=grip*.20*max(0.0,p.y-1.0);
            if(style>13.5&&style<14.5)p.x+=grip*.17*sin(p.y);
            if(style>14.5&&style<15.5&&p.y>4.2)p.z+=(p.y-4.2)*grip*.3;
            if(style>15.5&&style<16.5&&aKind>8.5){p.x+=sin(p.y*1.4+clock)*rage*.24;p.z+=cos(p.y+clock)*rage*.18;}
            if(style>16.5&&style<17.5&&aKind>8.5&&aKind<10.5)p.y+=grip*.25;
            if(style>17.5&&aKind>1.5&&aKind<3.5)p.z+=grip*abs(p.x)*.25;
        }
        vec4 world=uModel*vec4(p,1.0);vWorldY=world.y;vNormal=mat3(uModel)*n;vUV=aColor;vPart=aKind;vLocal=p;vDistance=length(world.xyz-uEye);gl_Position=uVP*world;
    }
    """.trimIndent()
    val fragment="""
    precision mediump float;
    uniform mediump vec4 uAttack35;uniform sampler2D uAtlas;uniform mediump float uTime;uniform mediump vec4 uArt33;uniform mediump float uGesture33;uniform float uNight;uniform float uOpen;uniform float uClipY;
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
        if(vPart>3.5&&vPart<4.5){c=max(c,albedo*.64);float radius=length(vUV.xy-vec2(.5));float iris=smoothstep(.15,.23,radius)*(1.0-smoothstep(.39,.49,radius));vec3 angry=c*vec3(1.5,.30,.22)+vec3(.10,.006,0.0);c=mix(c,angry,uAttack35.y*.8*iris);}
        if(vPart>10.5&&vPart<11.5)c*=.35;
        // Fine surface wear breaks up smooth skin without another texture or screen pass.
        float grain=sin(vLocal.x*137.0+sin(vLocal.z*71.0))*sin(vLocal.y*119.0);
        c*=.96+.04*grain;
        if(vPart>5.5&&vPart<6.5)c*=.72;
        float fog=1.0-exp(-max(0.0,vDistance-90.0)/230.0);
        gl_FragColor=vec4(mix(c,mix(vec3(.20,.25,.29),vec3(.045,.065,.10),uNight),fog*.65),1.0);
    }
    """.trimIndent()
}
