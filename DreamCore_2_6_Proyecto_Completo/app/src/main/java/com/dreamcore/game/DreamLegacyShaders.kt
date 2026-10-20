package com.dreamcore.game

/**
 * Materiales 1.4 y cielo orientado al mundo para OpenGL ES 2.0, sin extensiones ni HDR.
 * La iluminación y la oclusión del abismo son aproximaciones artísticas; no shadow maps.
 * Siete vectores varying como máximo (el mínimo de GLES2 es ocho).
 */
object DreamLegacyShaders {
    val vertexShader = """
        uniform mat4 uVP;
        uniform mediump vec4 uScene;
        uniform mediump vec3 uBus;
        uniform mediump float uActorYaw;
        uniform mediump float uTrainOffset;
        uniform mat4 uMirrorVP;
        uniform mediump vec3 uEntity;
        uniform mediump float uPresence;
        uniform highp vec3 uCamera;
        uniform mediump vec4 uCreature;
        uniform mediump vec3 uSun;
        uniform mediump float uTime;
        uniform mediump float uFogNear;
        uniform mediump float uFogFar;
        uniform mediump float uWorld;
        uniform mediump float uAnomaly;
        attribute vec3 aPos;
        attribute vec3 aColor;
        attribute vec3 aNormal;
        attribute float aKind;
        varying mediump vec3 vColor;
        varying mediump vec3 vPosition;
        varying mediump vec3 vNormal;
        varying mediump vec3 vEye100;
        varying mediump vec2 vTexCoord;
        varying mediump float vFog;
        varying mediump float vKind;
        varying mediump vec3 vReflection;
        void main() {
            vec3 p = aPos;
            if(aKind>108.5 && aKind<109.5)p.z+=uTrainOffset;
            if(aKind>91.5 && aKind<92.5){p.x+=sin(uTime*.06+aPos.z*.05)*.65;p.y+=sin(uTime*.09+aPos.x*.05)*.18;}
            if(aKind>82.5 && aKind<83.5){float a=uCreature.w;p=vec3(p.x*cos(a)-p.z*sin(a),p.y,p.x*sin(a)+p.z*cos(a))+uCreature.xyz;}
            if(aKind>80.5 && aKind<82.5){p.y+=sin(uTime*.10+aPos.z*.006)*2.5;p.x+=cos(uTime*.06+aPos.y)*1.5;}
            if(aKind>66.5 && aKind<67.5)p+=uBus;
            if(aKind>67.5 && aKind<69.5)p.y=mod(p.y-uTime*(aKind>68.5?6.0:.7)+4096.0,12.0);
            if(aKind>64.5 && aKind<65.5)p.x+=uScene.y*8.0;
            if(aKind>56.5 && aKind<57.5)p.y-=1.2*(1.0-uScene.x);
            if(aKind>69.5 && aKind<70.5)p.y*=uScene.x;
            if(((aKind>31.5 && aKind<32.5)||(aKind>96.5 && aKind<99.5))) {
                p.xz=mat2(cos(uActorYaw),sin(uActorYaw),-sin(uActorYaw),cos(uActorYaw))*p.xz;
                p+=uEntity;
                if(aKind<98.5)p.y+=.035*sin(uTime*.9+aPos.y);
                p.x+=max(aPos.y-3.8,0.0)*.23*(1.0-uPresence);
            }
            if(aKind>35.5 && aKind<36.5) {
                p.x+=3.2*sin(uTime*.11+aPos.z*.015);
                p.y+=1.8*sin(uTime*.17+aPos.x*.025);
                p.z+=2.5*cos(uTime*.12+aPos.x*.013);
            }
            if(aKind>36.5 && aKind<37.5) {
                p.x+=.75*sin(uTime*.24+aPos.z*.09);
                p.y+=.45*sin(uTime*.31+aPos.x*.11);
                p.z+=.48*cos(uTime*.27+aPos.y);
            }
            if(aKind>37.5 && aKind<38.5) {
                p.x+=.22*sin(uTime*.65+aPos.z*.04)*(1.0+.04*aPos.y);
                p.z+=.13*cos(uTime*.52+aPos.x*.03);
            }
            if(aKind>38.5 && aKind<39.5) {
                float swing=sin(uTime*.74+aPos.x*.12)*.8;
                p.z+=swing*max(8.0-aPos.y,0.0)/6.2;
                p.y+=abs(swing)*.075;
            }
            bool crater = uWorld > 1.5 && uWorld < 2.5;
            if (aKind > 21.5 && aKind < 22.5) {
                p.x += .12 * uAnomaly * sin(uTime * 2.1 + aPos.y * 1.8);
            }
            if (aKind > .5 && aKind < 1.5) {
                float strength = crater ? .022 : .055;
                p.y += strength * sin(p.x * 1.4 + uTime * .85) *
                       cos(p.z * 1.9 - uTime * 1.08);
            }
            // Saltos digitales espaciados; no hay parpadeos ni colores estroboscópicos.
            // Evitamos hashes de 43758, incompatibles con mediump de algunos teléfonos.
            if (aKind > 22.5 && aKind < 23.5) {
                float tick = floor(uTime * .65);
                float pulse = step(.76, sin(tick*.91 + floor(aPos.y*.25)*1.8));
                p.x += pulse * .36;
                p.z += pulse * .12;
            }
            vec3 n = normalize(aNormal);
            // El generador histórico de suelos tiene algunas normales invertidas.
            if (aKind >= 4.0 && n.y < -.68) n.y = -n.y;
            if (crater && ((aKind > 13.5 && aKind < 14.5) || (aKind > 26.5 && aKind < 27.5)) && abs(n.y) < .45) {
                // La pared cilíndrica ilumina su cara interior, visible desde el pozo.
                vec2 radial = p.xz - vec2(0.0,-102.0);
                if (dot(n.xz, radial) > 0.0) n.xz = -n.xz;
            }
            float sun = max(dot(n, normalize(uSun)), 0.0);
            float hemisphere = .5 + .5 * max(n.y,-.5);
            if (crater) {
                float depth = smoothstep(4.0,178.0,-p.y);
                vec3 ambient = mix(vec3(.57,.62,.44),vec3(.88,.90,.75),hemisphere);
                vec3 sunlight = vec3(1.0,.85,.70) * (.35 * sun);
                vec3 light = mix(ambient + sunlight,
                    vec3(.64,.73,.54) + sunlight*.27, depth);
                vColor = aColor * light;
            } else {
                // Conserva la dirección artística y el brillo de los otros siete mapas.
                vColor = aColor * (.64 + .30*sun + .095*hemisphere);
                if (uWorld > 5.5 && uWorld < 7.5) vColor *= .84 + .14*hemisphere;
                if (uWorld > 7.5) {
                    vec3 ambient = mix(vec3(.52,.56,.67),vec3(.82,.83,.87),hemisphere);
                    vColor = aColor*(ambient+vec3(.24,.21,.18)*sun);
                }
            }
            if ((uWorld > 11.5 && uWorld < 12.5)) {
                vec3 moonlight=vec3(.48,.57,.82)*(.46+.48*sun+.15*hemisphere);
                vColor=aColor*moonlight;
            }
            if((uWorld>2.5 && uWorld<3.5) || (uWorld>4.5 && uWorld<5.5) ||
                (uWorld>6.5 && uWorld<7.5) || (uWorld>10.5 && uWorld<11.5)) {
                vColor*=vec3(.78,.83,.98);
            }
            if (uAnomaly > .01 && aKind < 19.5) {
                vColor = mix(vColor,vec3(vColor.b*.85,vColor.r*.82,vColor.g),uAnomaly*.14);
            }
            if (aKind > 19.5 && aKind < 20.5) {
                float fluffy = .79 + .18*max(n.y,0.0) + .06*sun;
                vColor = aColor * fluffy;
                p.x += .25 * sin(uTime*.11 + aPos.z*.017);
            }
            if((aKind>77.5 && aKind<78.5)||(aKind>91.5 && aKind<92.5)||(aKind>93.5 && aKind<94.5))vColor=aColor;
            if(aKind>96.5 && (aKind<97.5 || aKind>98.04))vColor=aColor;
            if(aKind>104.5 && aKind<105.5)vColor=aColor;
            if(aKind>110.5 && aKind<111.5)vColor=aColor;
            vPosition = p;
            // Local texture coordinates keep the remote pool stable on mediump GPUs.
            if(p.x>1000.0)vPosition.x-=1600.0;
            vNormal = n;
            // Escalar antes de interpolar impide overflow de length() en mediump.
            // 300 metros al cuadrado supera el rango mínimo de GLES2 mediump.
            vEye100 = (uCamera - p) * .01;
            vKind = aKind;
            float scale = crater ? .30 : .45;
            if (abs(n.y) > .66) vTexCoord = vPosition.xz * scale;
            else if (abs(n.x) > abs(n.z)) vTexCoord = vPosition.zy * scale;
            else vTexCoord = vPosition.xy * scale;
            float dist = distance(p.xz,uCamera.xz);
            float fog = clamp((dist-uFogNear)/max(uFogFar-uFogNear,1.0),0.0,1.0);
            vFog = (aKind > 19.5 && aKind < 20.5)
                ? clamp(fog*.37,0.0,.34) : clamp(fog,0.0,.92);
            vec4 reflected=uMirrorVP*vec4(p,1.0);
            vReflection=vec3(reflected.xy*.5+reflected.w*.5,reflected.w);
            gl_Position = uVP * vec4(p,1.0);
        }
    """.trimIndent()

    val fragmentShader = """
        #ifdef GL_FRAGMENT_PRECISION_HIGH
        precision highp float;
        #else
        precision mediump float;
        #endif
        uniform sampler2D uAtlas;
        uniform sampler2D uWeeping;
        uniform mediump vec4 uTools;
        uniform mediump vec3 uTorchForward;
        uniform mediump float uPhoto;
        uniform mediump vec4 uScene;
        uniform sampler2D uReflection;
        uniform mediump float uMirrorReady;
        uniform mediump float uMirrorPass;
        uniform mediump float uWaterReflection;
        uniform mediump float uWaterPlane;
        uniform mediump float uMirrorZ;
        uniform mediump vec3 uMirrorCenter;
        uniform mediump float uPresence;
        uniform mediump float uCircuit;
        uniform mediump vec3 uEntity;
        uniform sampler2D uClouds;
        uniform sampler2D uMoon;
        uniform mediump vec4 uMoonMood;
        uniform mediump vec4 uEyeTarget;
        uniform mediump vec4 uGiant;
        uniform mediump vec3 uGiantDirection;

        uniform mediump float uWorld;
        uniform mediump vec3 uSun;
        uniform mediump vec3 uFogColor;
        uniform mediump float uFogNear;
        uniform mediump float uFogFar;
        uniform mediump float uTime;
        uniform mediump float uAnomaly;
        varying mediump vec3 vColor;
        varying mediump vec3 vPosition;
        varying mediump vec3 vNormal;
        varying mediump vec3 vEye100;
        varying mediump vec2 vTexCoord;
        varying mediump float vFog;
        varying mediump float vKind;
        varying mediump vec3 vReflection;

        // Animated stone face in lunar coordinates: eyelids, tracking irises and a widening grin.
        vec4 livingMoon(vec3 ray,vec3 direction,float radius,vec3 tint,float smile,float blink,vec2 eyeAim) {
            direction=normalize(direction);
            vec3 right=normalize(cross(direction,vec3(0.0,1.0,0.0)));
            vec3 up=cross(right,direction);
            float facing=dot(ray,direction);
            vec2 q=vec2(dot(ray,right),dot(ray,up))/max(.04,facing)/max(radius,.02);
            if(facing<=.001 || max(abs(q.x),abs(q.y))>1.01)return vec4(0.0);
            vec2 sampleQ=q;
            sampleQ.x+=.006*smile*sin(uTime*.31+q.y*2.0);
            vec4 stone=texture2D(uMoon,vec2(.5+sampleQ.x*.5,.5-sampleQ.y*.5));
            float disk=(1.0-smoothstep(.978,.992,length(q)))*step(0.0,facing);
            vec3 color=stone.rgb*tint;
            for(int i=0;i<2;i++) {
                float side=float(i)*2.0-1.0;
                vec2 eye=q-vec2(side*.255,.16);
                float height=.008+.083*(1.0-blink);
                float socket=length(eye/vec2(.188,.136));
                color*=1.0-.28*(1.0-smoothstep(.8,1.2,socket));
                float opening=length(eye/vec2(.161,height));
                float mask=1.0-smoothstep(.90,1.03,opening);
                vec2 pupil=eye-eyeAim;
                float iris=1.0-smoothstep(.051,.067,length(pupil));
                float black=1.0-smoothstep(.023,.037,length(pupil));
                vec3 eyeball=vec3(.22,.24,.31)*(1.0-.52*clamp(opening,0.0,1.0));
                eyeball=mix(eyeball,vec3(.075,.11,.24),iris);
                eyeball=mix(eyeball,vec3(.003,.005,.012),black);
                float glint=1.0-smoothstep(.005,.014,length(pupil-vec2(-.013,.017)));
                eyeball+=vec3(.28,.31,.40)*glint;
                color=mix(color,eyeball,mask);
                float lid=exp(-pow((eye.y-height)/.018,2.0))*(1.0-smoothstep(.14,.18,abs(eye.x)));
                color+=tint*.025*lid;
                float browY=.307+.037*smile+.035*pow((q.x-side*.25)/.20,2.0);
                float brow=exp(-pow((q.y-browY)/.027,2.0))*(1.0-smoothstep(.15,.23,abs(q.x-side*.25)));
                color*=1.0-.24*brow;
            }
            float width=.215+.285*smile;
            float mouthX=q.x/width;
            float upper=-.365+(.025+.135*smile)*mouthX*mouthX;
            float opening=(.024+.160*smile)*max(0.0,1.0-mouthX*mouthX);
            float lower=upper-opening;
            float mouth=(1.0-smoothstep(.96,1.025,abs(mouthX)))*smoothstep(lower-.008,lower+.008,q.y)*(1.0-smoothstep(upper-.008,upper+.008,q.y));
            vec3 cavity=vec3(.012,.007,.016);
            float toothHeight=(.022+.047*smile)*(.86+.14*sin(floor((mouthX+1.0)*5.5)*7.1));
            float cells=(mouthX+1.0)*5.5;
            float edge=min(fract(cells),1.0-fract(cells));
            float teeth=smoothstep(.045,.10,edge)*smoothstep(upper-toothHeight-.006,upper-toothHeight+.010,q.y)*(1.0-smoothstep(upper-.015,upper+.005,q.y));
            vec3 enamel=mix(vec3(.32,.29,.26),vec3(.72,.69,.61),clamp((q.y-upper+toothHeight)/max(toothHeight,.01),0.0,1.0));
            cavity=mix(cavity,enamel,teeth*(.35+.65*smile));
            float bottom=smoothstep(.65,.95,smile)*smoothstep(.06,.12,edge)*(1.0-smoothstep(lower+.018,lower+.047,q.y));
            cavity=mix(cavity,vec3(.53,.52,.43),bottom);
            color=mix(color,cavity,mouth);
            float lip=exp(-pow((q.y-upper)/.016,2.0))*(1.0-smoothstep(.94,1.06,abs(mouthX)));
            color+=tint*.035*lip;
            return vec4(clamp(color,0.0,1.0),stone.a*disk);
        }
        vec3 lunarReflections(vec3 ray,vec3 sky) {
            vec4 a=livingMoon(ray,vec3(.08,.50,-.86),.285,mix(vec3(.81,.86,.97),vec3(.43,.50,1.0),uMoonMood.z),uMoonMood.z,uMoonMood.x,uEyeTarget.xy);
            vec4 b=livingMoon(ray,vec3(-.60,.48,-.63),.105,vec3(.76,.67,.88),uMoonMood.w,uMoonMood.y,uEyeTarget.zw);
            sky=mix(sky,a.rgb,a.a);sky=mix(sky,b.rgb,b.a);
            if(uGiant.w>.001) {
                vec4 g=livingMoon(ray,uGiantDirection,uGiant.x,vec3(.09,.23,.94),uGiant.y,uGiant.z,uEyeTarget.xy);
                sky=mix(sky,g.rgb,g.a*uGiant.w);
            }
            return sky;
        }

        vec3 environmentColor(vec3 ray) {
            if((uWorld>11.5 && uWorld<12.5)) {
                vec3 darkSky=mix(vec3(.055,.045,.12),vec3(.012,.022,.065),clamp(ray.y,0.0,1.0));
                return lunarReflections(ray,darkSky);
            }

            float elevation = clamp(ray.y*.65+.30,0.0,1.0);
            vec3 horizon = vec3(.83,.76,.82);
            vec3 upper = vec3(.40,.59,.78);
            if (uWorld > 9.5) {
                horizon = vec3(.95,.70,.78);
                upper = vec3(.44,.46,.71);
            }
            bool night=(uWorld>2.5 && uWorld<3.5) || (uWorld>4.5 && uWorld<5.5) ||
                (uWorld>6.5 && uWorld<7.5) || (uWorld>10.5 && uWorld<11.5);
            if(night){horizon=vec3(.22,.24,.40);upper=vec3(.025,.035,.12);}
            vec3 sky = mix(horizon,upper,elevation);
            vec2 uv = ray.xz/(abs(ray.y)+.42)*.42 + vec2(uTime*.00021,uTime*.00009);
            vec3 field = texture2D(uClouds,uv).rgb;
            float density = smoothstep(.025,.50,field.r);
            float shade = clamp(.80+(field.g-.50196)*.35-(field.b-.50196)*.28,0.0,1.0);
            vec3 cloud = mix(vec3(.63,.64,.75),vec3(.98,.95,.95),shade);
            if(night)cloud*=vec3(.32,.34,.46);
            float fade = smoothstep(.0,.16,abs(ray.y));
            return mix(sky,cloud,density*fade*.88);
        }
        void main() {
            if(vKind>78.5 && vKind<79.5 && uTools.x<.5)discard;
            if(vKind>59.5 && vKind<60.5 && uPhoto<.5)discard;
            if(vKind>65.5 && vKind<66.5 && uScene.z<.5)discard;
            if(uMirrorPass>.5 && (uWaterReflection>.5 ? (vPosition.y<uWaterPlane+.015 || (vKind>.5 && vKind<1.5)) : (vPosition.z<uMirrorZ+.08 || (vKind>30.5 && vKind<31.5))))discard;
            if(vKind>31.5 && vKind<32.5 && uPresence<.015)discard;
            // El cielo sustituye las antiguas nubes de elipsoides sólidos.
            if (vKind > 19.5 && vKind < 20.5) discard;
            bool crater = uWorld > 1.5 && uWorld < 2.5;
            if(vKind>98.5 && vKind<99.5){vec2 q=vColor.xy*2.0-1.0;float opacity=(1.0-smoothstep(.1,1.0,dot(q,q)))*uPresence*.24;gl_FragColor=vec4(vec3(.015,.024,.025),opacity);return;}
            vec3 color = vColor;
            float eyeDistance = length(vEye100) * 100.0;
            vec3 normal = normalize(vNormal);
            if (vKind > 3.5 && vKind < 19.5) {
                float material = floor(vKind-4.0+.5);
                vec2 tile = vec2(mod(material,4.0),floor(material/4.0));
                vec2 atlasUV = (tile+vec2(.055)+fract(vTexCoord)*.89)/vec2(4.0,8.0);
                vec3 surface = texture2D(uAtlas,atlasUV).rgb;
                float textureStrength = vKind < 4.5 ? .18 : .38;
                textureStrength *= 1.0-.55*smoothstep(25.0,130.0,eyeDistance);
                if (crater) {
                    // Los materiales acompañan el volumen; el ruido ya no lo tapa.
                    textureStrength = .28;
                    if (vKind < 4.5) textureStrength = .15;
                    if (vKind > 13.5 && vKind < 14.5) textureStrength = .20;
                    textureStrength *= 1.0-.70*smoothstep(24.0,145.0,eyeDistance);
                    surface = clamp(surface*1.12,0.0,1.0);
                }
                color *= mix(vec3(1.0),surface,textureStrength);
            }
            if(uTools.x>.5){float cone=smoothstep(.80,.96,dot(-normalize(vEye100),uTorchForward));color+=vec3(.46,.36,.19)*cone/(1.0+dot(vEye100,vEye100)*650.0);}
            if(vKind>77.5 && vKind<78.5){float i=clamp(floor((vKind-78.0)*10.0+.5),0.0,3.0);vec2 uv=(clamp(vColor.xy,.008,.992)+vec2(mod(i,2.0),floor(i*.5)))*.5;color=texture2D(uWeeping,uv).rgb*.88+vec3(.025);}
            if(vKind>78.5 && vKind<79.5)color=vec3(.26,.62,.66);
            if(uTools.y>.5 && vKind>33.5 && vKind<34.5)color=color.brg*vec3(.75,1.1,1.18);
            if(uWorld>3.5 && uWorld<4.5 && vKind<.5 && abs(normal.y)>.7) {
                float field=.5+.5*sin(vPosition.x*.025+sin(vPosition.z*.018)*2.0);
                color*=.94+.10*field;
                float blade=sin(vPosition.x*5.3+sin(vPosition.z*3.1)) * sin(vPosition.z*6.2+uTime*.3);
                color+=vec3(.009,.016,.006)*blade*(1.0-smoothstep(3.0,22.0,eyeDistance));
            }
            if(vKind>26.5 && vKind<27.5) {
                float ribs=.5+.5*sin(vPosition.x*.63+vPosition.z*.51);
                color*=.88+.12*ribs;
            }
            if (vKind > 24.5 && vKind < 26.5) {
                vec3 viewDir = normalize(vEye100+vec3(0.0,.00001,0.0));
                vec3 surfaceNormal = normal;
                if (dot(surfaceNormal,viewDir) < 0.0) surfaceNormal = -surfaceNormal;
                float tileFade = 1.0-smoothstep(18.0,65.0,eyeDistance);
                if (vKind > 25.5 && (uWorld < 7.5 || uWorld > 8.5)) {
                    // Juntas anchas y suaves que desaparecen antes de aliasar a distancia.
                    vec2 cell = fract(vTexCoord*3.4);
                    vec2 edge = min(cell,1.0-cell);
                    float grout = 1.0-smoothstep(.018,.045,min(edge.x,edge.y));
                    color *= 1.0-.23*grout*tileFade;
                    float tiny = sin(vPosition.x*.39+vPosition.z*.31+uTime*.32) *
                        sin(vPosition.y*.42-vPosition.z*.36-uTime*.26);
                    if ((uWorld > .5 && uWorld < 1.5) ||
                        (uWorld > 4.5 && uWorld < 5.5) || (uWorld > 8.5 && uWorld < 9.5)) {
                        color += vec3(.035,.060,.063)*max(tiny,0.0)*(.55+.45*tileFade);
                    }
                }
                vec3 reflected = reflect(-viewDir,surfaceNormal);
                float rim = 1.0-clamp(dot(viewDir,surfaceNormal),0.0,1.0);
                float fresnel = rim*rim*rim;
                vec3 env = environmentColor(reflected);
                float reflection = vKind < 25.5 ? .07+.27*fresnel : .035+.13*fresnel;
                // La reflexión de cielo conserva los colores de las casillas y las piezas.
                color = color*(1.0-reflection*.55)+env*reflection;
                vec3 halfLight = normalize(normalize(uSun)+viewDir+vec3(.0,.0001,.0));
                float specular = pow(max(dot(surfaceNormal,halfLight),0.0),64.0);
                color += vec3(.25,.22,.18)*specular*(vKind < 25.5 ? 1.0 : .55);
            }
            if((uWorld>11.5 && uWorld<12.5) && vKind>25.5 && vKind<26.5) {
                vec2 brick=vTexCoord*1.1;
                brick.x+=mod(floor(brick.y),2.0)*.5;
                vec2 edge=min(fract(brick),1.0-fract(brick));
                float mortar=1.0-smoothstep(.025,.060,min(edge.x,edge.y));
                float variation=.94+.055*sin(floor(brick.x)*1.7+floor(brick.y)*.9);
                color*=variation*(1.0-.24*mortar*(1.0-smoothstep(80.0,160.0,eyeDistance)));
            }
            if (crater && vKind > 13.5 && vKind < 14.5 && abs(normal.y) < .45) {
                // Estratos lentos y amplios, sin líneas finas que aliasen a distancia.
                float strata = sin(vPosition.y*.17 +
                    .45*sin(vPosition.x*.045 + vPosition.z*.037));
                color *= .97 + .03*strata;
                float nearFloor = 1.0-smoothstep(1.5,12.0,vPosition.y+180.0);
                color *= 1.0-.15*nearFloor;
                float caustic = sin(vPosition.x*.53+uTime*.37+
                    sin(vPosition.z*.39-uTime*.21));
                caustic *= sin(vPosition.z*.60-uTime*.30);
                color += vec3(.025,.055,.060)*max(caustic,0.0)*nearFloor;
            }
            if (crater && vKind < 19.5 && vPosition.y < -164.0) {
                // Oclusión aproximada de la unión pared-suelo, no una sombra dinámica.
                vec2 offset = (vPosition.xz-vec2(0.0,-102.0))*.01;
                float radial = length(offset)*100.0;
                float corner = smoothstep(82.0,92.0,radial) *
                    (1.0-smoothstep(1.0,9.0,vPosition.y+180.0));
                color *= 1.0-.13*corner;
                if (radial < 20.0) {
                    // Rebote cálido local de la puerta, calculado sin pases extra.
                    vec3 fromDoor = (vPosition-vec3(0.0,-174.5,-97.28))*.1;
                    float pool = 1.0-smoothstep(.08,.72,length(fromDoor));
                    color += vec3(.13,.075,.028)*pool*max(normal.z,.28);
                }
            }
            if (vKind > 22.5 && vKind < 23.5) {
                // Bandas pastel estables: el glitch se percibe en la geometría.
                float band = .5+.5*sin(vPosition.y*1.8+uTime*.45);
                color = mix(color,vec3(.57,.88,.91),.13+.10*band);
            }
            if (vKind > 20.5 && vKind < 21.5) {
                float vert = fract(vPosition.y*.105+.12);
                float breath = .5+.5*sin(uTime*.52+vPosition.z*.14);
                vec3 twilight = mix(vec3(.18,.29,.48),vec3(.68,.76,.94),vert);
                color = mix(twilight,vec3(.80,.77,.91),breath*.20);
                color += vec3(.18,.06,.17)*uAnomaly*(.4+.6*breath);
            }
            if (vKind > 19.5 && vKind < 20.5) {
                float pattern = sin(vPosition.x*.10+vPosition.z*.075) *
                    cos(vPosition.y*.19-vPosition.z*.055);
                color *= .965+.035*pattern;
            }
            if (vKind > 21.5 && vKind < 22.5) {
                float breath = .5+.5*sin(uTime*1.65+vPosition.y*1.8);
                color = mix(vec3(.32,.43,.53),vec3(.91,.81,.98),.17+uAnomaly*.75*breath);
            }
            if ((vKind > .5 && vKind < 1.5)||(vKind>115.5 && vKind<116.5)) {
                if (crater || uWorld > 7.5) {
                    // Reflejo estilizado de cielo, Fresnel y brillo de sol. No RTT.
                    float r1 = sin(vPosition.x*.60 + uTime*.46 +
                        sin(vPosition.z*.38-uTime*.27));
                    float r2 = cos(vPosition.z*.68-uTime*.39 +
                        sin(vPosition.x*.31+uTime*.21));
                    vec3 waterN = normalize(vec3(r1*.045,1.0,r2*.045));
                    vec3 viewDir = normalize(vEye100+vec3(0.0,.00001,0.0));
                    float rim = 1.0-clamp(dot(viewDir,waterN),0.0,1.0);
                    float fresnel = rim*rim*rim;
                    vec3 reflected = reflect(-viewDir,waterN);
                    vec3 sky = mix(vec3(.72,.66,.79),vec3(.43,.67,.83),
                        clamp(reflected.y*.65+.45,0.0,1.0));
                    if((uWorld>11.5 && uWorld<12.5))sky=environmentColor(reflected);
                    if (uWorld > 7.5) sky = environmentColor(reflected);
                    vec3 baseWater = mix(color,vec3(.24,.49,.56),crater ? .48 : .18);
                    color = mix(baseWater,sky,.24+.52*fresnel);
                    vec3 halfLight = normalize(normalize(uSun)+viewDir+vec3(0.0,.0001,0.0));
                    float sunSpark = pow(max(dot(waterN,halfLight),0.0),48.0);
                    color += vec3(.30,.25,.19)*sunSpark;
                    color += vec3(.012,.025,.027)*(r1*r2);
                } else {
                    float r1 = sin(vPosition.x*2.7+uTime*1.15+
                        sin(vPosition.z*1.20)*1.5);
                    float r2 = cos(vPosition.z*3.0-uTime*.93+sin(vPosition.x*.81));
                    float wave = r1*r2;
                    float shine = pow(max(.0,.5+.5*wave),6.0);
                    color += vec3(.045,.075,.10)*wave+vec3(.12,.16,.18)*shine;
                }
            }
            if(vKind>27.5 && vKind<29.5) {
                float wave=.5+.5*sin(vPosition.y*1.9+uTime*.9+sin(vPosition.x*2.4));
                color=mix(vColor,vec3(.68,.89,.87),.32+.20*wave);
                if(uWorld<.5 && vPosition.x>0.0)color=mix(vec3(.18,.07,.31),vec3(.69,.39,.92),wave*.65+.22);
            }
            if(vKind>29.5 && vKind<30.5) {
                float wave=.5+.5*sin(uTime*.8+vPosition.y*2.7);
                color=mix(vec3(.24,.10,.48),vec3(.59,.55,1.0),.45+.24*wave);
            }
            if (vKind > 23.5 && vKind < 24.5) {
                // Puerta central y luminarias: señal cálida constante, respiración de 14 s.
                float breath = .5+.5*sin(uTime*.45+vPosition.y*.15);
                float ribbon = .5+.5*sin(vPosition.y*.85+uTime*.22);
                color = mix(vec3(.96,.71,.43),vec3(1.0,.94,.76),.40+.32*ribbon);
                color *= .96+.04*breath;
            } else if (vKind > 2.5 && vKind < 3.5) {
                float pulse = .50+.50*sin(uTime*1.65+vPosition.y*2.0);
                color = mix(vec3(.20,.48,.52),vec3(.72,.96,.90),pulse);
            } else if (vKind > 1.5 && vKind < 2.5) {
                float pulse = .50+.50*sin(uTime*2.2+vPosition.y*2.4);
                color = mix(vec3(.27,.22,.49),vec3(.72,.56,.91),pulse);
            }
            if(vKind>30.5 && vKind<31.5) {
                vec3 viewDir=normalize(vEye100+vec3(0.0,.00001,0.0));
                vec3 reflected=reflect(-viewDir,normal);
                vec3 reflection=environmentColor(reflected);
                vec2 uv=vReflection.xy/max(vReflection.z,.001);
                if(uMirrorReady>.5 && vReflection.z>0.0 && uv.x>0.0 && uv.x<1.0 && uv.y>0.0 && uv.y<1.0)
                    reflection=texture2D(uReflection,uv).rgb;
                float grazing=1.0-abs(dot(viewDir,normal));
                color=mix(reflection*.91,vec3(.56,.63,.72),.045+grazing*.055);
            }
            if(vKind>31.5 && vKind<32.5) {
                color=mix(uFogColor,vColor*(.65+.35*max(dot(normal,normalize(uSun)),0.0))+vec3(.015,.018,.025),uPresence);
            }
            if(vKind>33.5 && vKind<34.5) {
                // Windows show an alternate, slowly moving sky.
                vec2 q=vTexCoord*.09;
                vec3 field=texture2D(uClouds,q+vec2(uTime*.0008,-uTime*.0003)).rgb;
                float horizon=smoothstep(.0,.75,fract(vPosition.y*.075));
                color=mix(vec3(.20,.23,.48),vec3(.89,.69,.78),horizon);
                color=mix(color,vec3(.81,.87,.93),smoothstep(.06,.54,field.r)*.65);
            }
            if(vKind>36.5 && vKind<37.5) {
                float nearLight=1.0-smoothstep(10.0,35.0,eyeDistance);
                color=mix(vColor,vec3(.88,.83,.98),.25+.35*nearLight)*(.86+.10*sin(uTime*.6+vPosition.z));
            }
            if(vKind>34.5 && vKind<36.5) {
                float glow=.72+.12*sin(uTime*.45+vPosition.z*.03);
                color=mix(vColor,vec3(.83,.88,.98),.40)*glow;
            }
            // Restrained spill on the floor; no full-screen bloom or moving hard shadows.
            if(normal.y>.7 && abs(vPosition.y)<.25 && uWorld>2.5 && uWorld<11.5) {
                vec2 mirrorDelta=clamp((vPosition.xz-uMirrorCenter.xz)*.13,vec2(-12.0),vec2(12.0));
                float spill=exp(-dot(mirrorDelta,mirrorDelta));
                float lampX=abs(abs(vPosition.x)-7.0);
                float lampZ=abs(mod(vPosition.z+20.0,40.0)-20.0);
                vec2 lampDelta=clamp(vec2(lampX,lampZ)*.25,vec2(-12.0),vec2(12.0));
                float lamp=exp(-dot(lampDelta,lampDelta));
                color+=vec3(.025,.035,.052)*spill+vec3(.032,.023,.018)*lamp;
                if((uWorld>2.5 && uWorld<3.5) || (uWorld>10.5 && uWorld<11.5)) {
                    vec2 movingCenter=vec2(sin(uTime*.09)*70.0,-210.0+cos(uTime*.07)*95.0);
                    vec2 delta=clamp((vPosition.xz-movingCenter)*.045,vec2(-12.0),vec2(12.0));
                    color+=vec3(.025,.035,.065)*exp(-dot(delta,delta));
                }
            }
            if (crater) {
                // Se despeja al acercarse a cualquier piso, incluso a 176 m de profundidad.
                // Antes el término de altura ocultaba el fondo también estando allí.
                float fog = smoothstep(uFogNear,max(uFogNear+1.0,uFogFar),eyeDistance);
                fog = min(fog*.73,.73);
                if (vKind > 19.5 && vKind < 20.5) fog *= .45;
                if (vKind > 23.5 && vKind < 24.5) fog *= .68;
                float depth = smoothstep(8.0,178.0,-vPosition.y);
                vec3 haze = mix(uFogColor,vec3(.49,.60,.46),depth*.28);
                color = mix(color,haze,fog);
                // Curva suave para blancos de porcelana; no HDR ni autoexposición.
                color = color*(1.025-.025*color);
            } else if (uWorld > 7.5) {
                float fog = smoothstep(uFogNear,max(uFogNear+1.0,uFogFar),eyeDistance);
                color = mix(color,uFogColor,fog*.72);
            } else {
                color = mix(color,uFogColor,vFog*.89);
            }
            if(vKind>91.5 && vKind<92.5){vec2 q=vColor.xy*2.0-1.0;float density=(1.0-smoothstep(.40,1.0,dot(q,q)))*smoothstep(.015,.52,texture2D(uClouds,vColor.xy*.65+vec2(uTime*.0013,.23)).r)*.65;if(density<.012)discard;gl_FragColor=vec4(vec3(.85,.88,.91),density);return;}
            if(vKind>93.5 && vKind<94.5)color=mix(vec3(.08,.13,.27),vec3(.79,.47,.35),.5+.5*sin(length(vColor.xy-.5)*18.0-uTime*.1));
            if(vKind>94.5 && vKind<95.5)color=vec3(.05,.13,.16);
            if(vKind>95.5 && vKind<96.5)color=vec3(.94,.95,.88);

            if(vKind>103.5 && vKind<104.5){vec2 q=abs(normal.x)>abs(normal.z)?vPosition.zy:vPosition.xy;float print=.5+.5*sin(q.x*6.0+sin(q.y*4.0)*.35);color*=.92+.08*print;}
            if(vKind>104.5 && vKind<105.5){
                float edge=1.0-smoothstep(.12,.50,abs(vColor.x-.5));float fade=sin(clamp(vColor.y,.0,1.0)*3.14159);
                vec3 light=mix(vec3(.19,.62,.93),vec3(.91,.30,.55),.5+.5*sin(vColor.x*5.0+vPosition.x*.013));
                gl_FragColor=vec4(light,edge*fade*.105);return;
            }
            if(vKind>105.5 && vKind<106.5){float pulse=.25+.40*(.5+.5*sin(uTime*.70+vPosition.z*.19));color=mix(vec3(.30,.30,.18),vec3(.89,.85,.55),pulse+uCircuit*.10);}
            if(vKind>107.5 && vKind<108.5){
                vec2 q=vPosition.xz*.72;vec2 cell=floor(q);float seed=fract(sin(dot(cell,vec2(13.13,7.37)))*137.7);vec2 p=fract(q)-.5;
                float petal=(1.0-smoothstep(.07,.14,length(p)))*step(.35,seed)*(1.0-smoothstep(12.0,60.0,eyeDistance));
                vec3 bloom=seed>.82?vec3(.38,.36,.72):(seed>.65?vec3(.86,.88,.79):vec3(.90,.77,.18));color=mix(color,bloom,petal*.86);
            }
            if(vKind>109.5 && vKind<110.5){
                vec2 q=abs(normal.x)>abs(normal.z)?vPosition.zy:vPosition.xy;vec2 grid=abs(fract(q*vec2(.25,.18))-.5);
                float glass=1.0-smoothstep(.39,.47,max(grid.x,grid.y));float band=.5+.5*sin(floor(q.x*.25)*1.73+floor(q.y*.18)*.71);
                color=mix(color*.82,vec3(.14,.29,.43)+vec3(.08,.11,.13)*band,glass*.62);
            }


            if(vKind>96.5 && vKind<97.04){
                vec2 uv=vColor.xy;float tile=floor(uv.x*2.0);
                float blink=.07+.93*smoothstep(.0,.11,abs(mod(uTime+uEntity.x*.17,6.7)-.14));
                float q=(uv.y-.225)/blink;
                if(abs(q)>.205)color=vec3(.27,.25,.25);
                else color=texture2D(uMoon,vec2(uv.x,.225+q)).rgb;
                color=mix(color,uFogColor,smoothstep(uFogNear,uFogFar,eyeDistance)*.72);
            }
            if(vKind>97.04 && vKind<97.5){color=texture2D(uMoon,vColor.xy).rgb*(.65+.25*max(normal.y,0.0));color=mix(color,uFogColor,smoothstep(uFogNear,uFogFar,eyeDistance)*.72);}
            if(vKind>98.04 && vKind<98.4){color=texture2D(uMoon,vColor.xy).rgb*vec3(1.12,.78,.84)*(.83+.12*max(normal.y,0.0));color=mix(color,uFogColor,smoothstep(uFogNear,uFogFar,eyeDistance)*.72);}
            if(vKind>110.5 && vKind<111.5){
                float i=20.0+floor(vColor.z+.5);vec2 cell=vec2(mod(i,4.0),floor(i/4.0));
                vec4 leaf=texture2D(uAtlas,(cell+vec2(.018)+vColor.xy*.964)/vec2(4.0,8.0));
                if(leaf.a<.40)discard;
                color=leaf.rgb*(.56+.40*abs(dot(normal,normalize(vec3(.4,.8,.3)))));
                color=mix(color,uFogColor,smoothstep(35.0,180.0,eyeDistance)*.60);
            }
            if(vKind>115.5 && vKind<116.5){gl_FragColor=vec4(clamp(color,0.0,1.0),.55);return;}
            gl_FragColor = vec4(clamp(color,0.0,1.0),(vKind>49.5 && vKind<50.5)?.35:(((vKind>31.5 && vKind<32.5)||(vKind>96.5 && vKind<99.5))?uPresence:1.0));
        }
    """.trimIndent()

    val skyVertexShader = """
        attribute vec2 aXY;
        uniform mediump vec3 uForward;
        uniform mediump vec3 uRight;
        uniform mediump vec3 uUp;
        uniform mediump float uAspect;
        varying mediump vec3 vRay;
        void main() {
            // Misma apertura vertical de 66 grados que la cámara del juego.
            vRay = uForward + (uRight*aXY.x*uAspect+uUp*aXY.y)*.649408;
            gl_Position = vec4(aXY,.999,1.0);
        }
    """.trimIndent()

    val skyFragmentShader = """
        #ifdef GL_FRAGMENT_PRECISION_HIGH
        precision highp float;
        #else
        precision mediump float;
        #endif
        uniform sampler2D uClouds;
        uniform sampler2D uMoon;
        uniform mediump vec4 uMoonMood;
        uniform mediump vec4 uEyeTarget;
        uniform mediump vec4 uGiant;
        uniform mediump vec3 uGiantDirection;

        uniform mediump vec3 uTop;
        uniform mediump vec3 uHorizon;
        uniform mediump vec3 uSun;
        uniform mediump float uWorld;
        uniform mediump float uTime;
        varying mediump vec3 vRay;

        vec4 cloudLayer(vec2 uv,vec3 ray,vec3 sunDir,float fullness,float farLayer) {
            vec3 data = texture2D(uClouds,uv).rgb;
            // Los canales G/B guardan el gradiente de densidad precalculado.
            vec2 slope = (data.gb-vec2(.5019608))*2.0;
            vec3 cloudNormal = normalize(vec3(-slope.x*1.4,.80,-slope.y*1.4));
            float sunFace = max(dot(cloudNormal,sunDir),0.0);
            float core = smoothstep(.15,.80,data.r);
            float behind = texture2D(uClouds,uv+sunDir.xz*.016).r;
            float shadow = clamp(behind-data.r*.35,0.0,1.0);
            float light = clamp(.74+.20*sunFace-.16*shadow-.04*core,0.0,1.0);
            vec3 low = vec3(.46,.51,.65);
            vec3 high = vec3(.99,.97,.96);
            if (uWorld > 9.5) {
                low = vec3(.54,.49,.65);
                high = vec3(1.0,.87,.89);
            }
            vec3 cloud = mix(low,high,light);
            float alignment = max(dot(ray,sunDir),0.0);
            float edge = smoothstep(.015,.16,data.r)*(1.0-smoothstep(.28,.65,data.r));
            cloud += vec3(.06,.04,.022)*edge*alignment*alignment;
            float opacity = smoothstep(.018,.78-fullness*.16,data.r);
            // La capa alta queda más diluida en la atmósfera.
            cloud = mix(cloud,uHorizon,farLayer*.16);
            return vec4(cloud,opacity);
        }


        // Animated stone face in lunar coordinates: eyelids, tracking irises and a widening grin.
        vec4 livingMoon(vec3 ray,vec3 direction,float radius,vec3 tint,float smile,float blink,vec2 eyeAim) {
            direction=normalize(direction);
            vec3 right=normalize(cross(direction,vec3(0.0,1.0,0.0)));
            vec3 up=cross(right,direction);
            float facing=dot(ray,direction);
            vec2 q=vec2(dot(ray,right),dot(ray,up))/max(.04,facing)/max(radius,.02);
            if(facing<=.001 || max(abs(q.x),abs(q.y))>1.01)return vec4(0.0);
            vec2 sampleQ=q;
            sampleQ.x+=.006*smile*sin(uTime*.31+q.y*2.0);
            vec4 stone=texture2D(uMoon,vec2(.5+sampleQ.x*.5,.5-sampleQ.y*.5));
            float disk=(1.0-smoothstep(.978,.992,length(q)))*step(0.0,facing);
            vec3 color=stone.rgb*tint;
            for(int i=0;i<2;i++) {
                float side=float(i)*2.0-1.0;
                vec2 eye=q-vec2(side*.255,.16);
                float height=.008+.083*(1.0-blink);
                float socket=length(eye/vec2(.188,.136));
                color*=1.0-.28*(1.0-smoothstep(.8,1.2,socket));
                float opening=length(eye/vec2(.161,height));
                float mask=1.0-smoothstep(.90,1.03,opening);
                vec2 pupil=eye-eyeAim;
                float iris=1.0-smoothstep(.051,.067,length(pupil));
                float black=1.0-smoothstep(.023,.037,length(pupil));
                vec3 eyeball=vec3(.22,.24,.31)*(1.0-.52*clamp(opening,0.0,1.0));
                eyeball=mix(eyeball,vec3(.075,.11,.24),iris);
                eyeball=mix(eyeball,vec3(.003,.005,.012),black);
                float glint=1.0-smoothstep(.005,.014,length(pupil-vec2(-.013,.017)));
                eyeball+=vec3(.28,.31,.40)*glint;
                color=mix(color,eyeball,mask);
                float lid=exp(-pow((eye.y-height)/.018,2.0))*(1.0-smoothstep(.14,.18,abs(eye.x)));
                color+=tint*.025*lid;
                float browY=.307+.037*smile+.035*pow((q.x-side*.25)/.20,2.0);
                float brow=exp(-pow((q.y-browY)/.027,2.0))*(1.0-smoothstep(.15,.23,abs(q.x-side*.25)));
                color*=1.0-.24*brow;
            }
            float width=.215+.285*smile;
            float mouthX=q.x/width;
            float upper=-.365+(.025+.135*smile)*mouthX*mouthX;
            float opening=(.024+.160*smile)*max(0.0,1.0-mouthX*mouthX);
            float lower=upper-opening;
            float mouth=(1.0-smoothstep(.96,1.025,abs(mouthX)))*smoothstep(lower-.008,lower+.008,q.y)*(1.0-smoothstep(upper-.008,upper+.008,q.y));
            vec3 cavity=vec3(.012,.007,.016);
            float toothHeight=(.022+.047*smile)*(.86+.14*sin(floor((mouthX+1.0)*5.5)*7.1));
            float cells=(mouthX+1.0)*5.5;
            float edge=min(fract(cells),1.0-fract(cells));
            float teeth=smoothstep(.045,.10,edge)*smoothstep(upper-toothHeight-.006,upper-toothHeight+.010,q.y)*(1.0-smoothstep(upper-.015,upper+.005,q.y));
            vec3 enamel=mix(vec3(.32,.29,.26),vec3(.72,.69,.61),clamp((q.y-upper+toothHeight)/max(toothHeight,.01),0.0,1.0));
            cavity=mix(cavity,enamel,teeth*(.35+.65*smile));
            float bottom=smoothstep(.65,.95,smile)*smoothstep(.06,.12,edge)*(1.0-smoothstep(lower+.018,lower+.047,q.y));
            cavity=mix(cavity,vec3(.53,.52,.43),bottom);
            color=mix(color,cavity,mouth);
            float lip=exp(-pow((q.y-upper)/.016,2.0))*(1.0-smoothstep(.94,1.06,abs(mouthX)));
            color+=tint*.035*lip;
            return vec4(clamp(color,0.0,1.0),stone.a*disk);
        }
        vec3 lunarReflections(vec3 ray,vec3 sky) {
            vec4 a=livingMoon(ray,vec3(.08,.50,-.86),.285,mix(vec3(.81,.86,.97),vec3(.43,.50,1.0),uMoonMood.z),uMoonMood.z,uMoonMood.x,uEyeTarget.xy);
            vec4 b=livingMoon(ray,vec3(-.60,.48,-.63),.105,vec3(.76,.67,.88),uMoonMood.w,uMoonMood.y,uEyeTarget.zw);
            sky=mix(sky,a.rgb,a.a);sky=mix(sky,b.rgb,b.a);
            if(uGiant.w>.001) {
                vec4 g=livingMoon(ray,uGiantDirection,uGiant.x,vec3(.09,.23,.94),uGiant.y,uGiant.z,uEyeTarget.xy);
                sky=mix(sky,g.rgb,g.a*uGiant.w);
            }
            return sky;
        }


        vec3 comets(vec3 ray) {
            vec3 result=vec3(0.0);
            for(int i=0;i<3;i++) {
                float k=float(i);
                float duration=7.0+k;
                float cycle=mod(uTime+k*17.0,46.0+k*13.0);
                float t=clamp(cycle/duration,0.0,1.0);
                vec3 forward=normalize(vec3(-.5+k*.4,.55+k*.08,-.8));
                vec3 side=normalize(cross(forward,vec3(0.0,1.0,0.0)));
                vec3 up=cross(side,forward);
                float facing=dot(ray,forward);
                vec2 q=vec2(dot(ray,side),dot(ray,up))/max(.12,facing);
                vec2 head=vec2(-.85+t*1.65,.32-t*.46);
                vec2 v=q-head;
                vec2 axis=normalize(vec2(1.0,-.28));
                float behind=dot(v,axis);
                float across=abs(dot(v,vec2(-axis.y,axis.x)));
                float tail=(1.0-smoothstep(.0,.28,-behind))*step(behind,0.0)*(1.0-smoothstep(.0015,.007,across));
                float point=1.0-smoothstep(.002,.010,length(v));
                float alive=(1.0-smoothstep(duration-.8,duration,cycle))*smoothstep(.0,.6,cycle)*step(0.0,facing);
                result+=mix(vec3(.38,.52,.89),vec3(.86,.74,.71),k*.35)*(tail*.19+point*.55)*alive;
            }
            return result;
        }

        void main() {
            vec3 ray = normalize(vRay);
            if((uWorld>11.5 && uWorld<12.5)) {
                vec3 color=mix(uHorizon,uTop,smoothstep(-.06,.7,ray.y));
                vec2 stars=ray.xz/(abs(ray.y)+.30)*70.0;
                vec2 cell=floor(stars);
                float seed=fract(sin(dot(cell,vec2(13.13,7.37)))*137.7);
                vec2 local=fract(stars)-.5;
                float star=(1.0-smoothstep(.015,.10,length(local)))*step(.975,seed)*smoothstep(.02,.15,ray.y);
                color+=vec3(.42,.48,.69)*star*(.72+.28*sin(uTime*(.4+seed)+seed*60.0));
                vec2 distant=ray.xz/(abs(ray.y)+.30)*112.0;
                float distantSeed=fract(sin(dot(floor(distant),vec2(9.31,17.7)))*171.3);
                float distantStar=(1.0-smoothstep(.008,.055,length(fract(distant)-.5)))*step(.991,distantSeed)*smoothstep(.02,.20,ray.y);
                color+=vec3(.22,.25,.35)*distantStar*(.76+.24*sin(uTime*.31+distantSeed*30.0));
                vec2 uv=ray.xz/(abs(ray.y)+.42)*.42+vec2(uTime*.00013,uTime*.00004);
                vec3 field=texture2D(uClouds,uv).rgb;
                float cloud=smoothstep(.04,.55,field.r)*smoothstep(-.04,.15,ray.y);
                color=mix(color,vec3(.095,.085,.18),cloud*.50);
                vec3 moonDir=normalize(vec3(.08,.50,-.86));
                float halo=pow(max(dot(ray,moonDir),0.0),24.0);
                color+=vec3(.13,.13,.24)*halo;
                color+=comets(ray);
                color=lunarReflections(ray,color);
                gl_FragColor=vec4(clamp(color,0.0,1.0),1.0);
                return;
            }

            vec3 sunDir = normalize(uSun+vec3(0.0,.0001,0.0));
            float altitude = smoothstep(-.06,.88,ray.y);
            vec3 color = mix(uHorizon,uTop,altitude);
            float solar = max(dot(ray,sunDir),0.0);
            float halo = pow(solar,12.0);
            color += vec3(.19,.12,.065)*halo;
            float disc = smoothstep(.99956,.99982,solar);
            color = mix(color,vec3(1.0,.96,.84),disc*.85);

            // Dos campos sobre una cúpula suave, con escalas y vientos distintos.
            // La proyección es mundial y evita estirar nubes en bandas al horizonte.
            vec2 projected = ray.xz/(max(ray.y,0.0)+.42);
            float giant=(uWorld>3.5 && uWorld<4.5) || (uWorld>9.5 && uWorld<10.5) ? .46 : 1.0;
            vec2 wind = vec2(uTime*.00048,uTime*.00019);
            vec2 otherWind=vec2(-uTime*.00014,uTime*.00037);
            vec4 upper = cloudLayer(projected*.23*giant+vec2(.41,.23)+otherWind,
                ray,sunDir,.10,1.0);
            vec4 lower = cloudLayer(projected*.42*giant+vec2(.08,.51)+wind,
                ray,sunDir,.20,0.0);
            float visible = smoothstep(-.025,.14,ray.y);
            float skyStrength = 1.0;
            if (uWorld > 5.5 && uWorld < 7.5) skyStrength = .60;
            color = mix(color,upper.rgb,upper.a*visible*.68*skyStrength);
            color = mix(color,lower.rgb,lower.a*visible*.94*skyStrength);
            bool night=(uWorld>2.5 && uWorld<3.5) || (uWorld>4.5 && uWorld<5.5) ||
                (uWorld>6.5 && uWorld<7.5) || (uWorld>10.5 && uWorld<11.5);
            if(night) {
                color+=comets(ray)*.65;
                // Sparse, independently twinkling stars, with slow gentle changes.
                vec2 stellar=ray.xz/(abs(ray.y)+.31)*62.0;
                vec2 cell=floor(stellar);vec2 local=fract(stellar)-.5;
                float seed=fract(sin(dot(cell,vec2(13.13,7.37)))*137.7);
                float star=(1.0-smoothstep(.015,.10,length(local)))*step(.974,seed)*smoothstep(.02,.18,ray.y);
                color=mix(color,mix(uHorizon,uTop,altitude),.55);
                color+=vec3(.46,.52,.67)*star*(.65+.35*sin(uTime*(.32+seed*.7)+seed*50.0));
                for(int i=0;i<3;i++) {
                    float k=float(i);
                    vec3 direction=normalize(vec3(sin(k*2.4+uTime*.012)*.65,.40+k*.13,-.8));
                    float alignment=dot(ray,direction);
                    float radius=.035+k*.022;
                    float angular=length(ray-direction);
                    float orb=1.0-smoothstep(radius*.85,radius,angular);
                    float halo=pow(max(alignment,0.0),180.0);
                    vec3 tint=mix(vec3(.65,.75,.99),vec3(.97,.67,.79),k*.35);
                    color+=tint*halo*.045;
                    color=mix(color,tint*.72,orb*.80);
                }
            }
            // La lejanía pierde contraste y evita una línea recortada en el horizonte.
            float horizonHaze = 1.0-smoothstep(.015,.19,abs(ray.y));
            color = mix(color,uHorizon,horizonHaze*.62);

            if (uWorld > 9.5 && uWorld < 10.5 && ray.y < .04) {
                // Mar de nubes bajo el hotel: no es un plano sólido ni una colisión.
                vec2 downUV = ray.xz/(max(-ray.y,0.0)+.28)*.36+vec2(.34,.17)+wind;
                vec4 sea = cloudLayer(downUV,vec3(ray.x,abs(ray.y),ray.z),sunDir,.65,0.0);
                vec4 seaFar = cloudLayer(downUV*.57+vec2(.23,.39),
                    vec3(ray.x,abs(ray.y),ray.z),sunDir,.5,1.0);
                vec3 seaColor = mix(seaFar.rgb*vec3(.80,.81,.91),sea.rgb,.12+.88*sea.a);
                seaColor = mix(seaColor,vec3(.91,.84,.91),.08);
                float seaVisible = 1.0-smoothstep(-.07,.04,ray.y);
                color = mix(color,seaColor,seaVisible);
            }
            gl_FragColor = vec4(clamp(color,0.0,1.0),1.0);
        }
    """.trimIndent()
}
