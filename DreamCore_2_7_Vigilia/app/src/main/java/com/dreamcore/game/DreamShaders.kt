package com.dreamcore.game

/**
 * Materiales 1.4 y cielo orientado al mundo para OpenGL ES 2.0, sin extensiones ni HDR.
 * 1.8: atlas detallado, mapa de pendientes/rugosidad y sombras direccionales RG16.
 * Compilación y enlace GLES2 probados también con fragmentos mediump.
 */
object DreamShaders {
    val vertexShader = """
        uniform mat4 uVP;
        uniform mat4 uLightVP;
        uniform mediump float uActorYaw;
        uniform mediump vec4 uLook;
        uniform mediump float uTrainOffset;
        uniform mediump vec4 uScene;
        uniform mediump vec3 uBus;
        uniform mediump vec4 uStory;
        uniform mediump vec4 uWeather;
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
        #ifdef GL_FRAGMENT_PRECISION_HIGH
        varying highp vec3 vPosition;
        #else
        varying mediump vec3 vPosition;
        #endif
        varying mediump vec3 vNormal;
        varying mediump vec3 vEye100;
        #ifdef GL_FRAGMENT_PRECISION_HIGH
        varying highp vec2 vTexCoord;
        #else
        varying mediump vec2 vTexCoord;
        #endif
        varying mediump float vFog;
        varying mediump float vKind;
        varying mediump vec3 vReflection;
        #ifdef GL_FRAGMENT_PRECISION_HIGH
        varying highp vec4 vShadow;
        #else
        varying mediump vec4 vShadow;
        #endif
        void main() {
            vec3 p = aPos;
            if(aKind>117.5 && aKind<118.5)p.z+=sin(uTime*.24+aPos.x*.037)*.13;
            if(aKind>110.5 && aKind<111.5){
                float sway=sin(uTime*.19634954+aPos.x*.13+aPos.z*.11)*.085*(1.0-aColor.y);
                p.x+=sway;p.z+=sway*.55;
                if(aColor.z>7.5){
                    float range=uLook.w>1.5?65.0:42.0;
                    float visibility=(1.0-smoothstep(range*.65,range,distance(aPos.xz,uCamera.xz)))*step(.5,uLook.w);
                    p.y-=max(0.0,1.0-aColor.y)*1.2*(1.0-visibility);
                    if(visibility<.02)p=vec3(aPos.x,aPos.y-(1.0-aColor.y)*1.2,aPos.z);
                }
            }
            if(aKind>108.5 && aKind<109.5)p.z+=uTrainOffset;
            if(aKind>82.5 && aKind<83.5){float a=uCreature.w; p=vec3(p.x*cos(a)-p.z*sin(a),p.y,p.x*sin(a)+p.z*cos(a))+uCreature.xyz;}
            if(aKind>80.5 && aKind<82.5){p.y+=sin(uTime*.10+aPos.z*.006)*2.5;p.x+=cos(uTime*.06+aPos.y)*1.5;}
            if(aKind>91.5 && aKind<92.5){p.x+=sin(uTime*.06+aPos.z*.05)*.65;p.y+=sin(uTime*.09+aPos.x*.05)*.18;}
            if(aKind>69.5 && aKind<70.5)p.y*=uScene.x;
            if(aKind>66.5 && aKind<67.5)p+=uBus;
            if(aKind>67.5 && aKind<68.5){p.y=mod(p.y-uTime*.7+512.0,12.0);p.x+=.45*sin(uTime*.3+aPos.z);}
            if(aKind>68.5 && aKind<69.5)p.y=mod(p.y-uTime*6.0+4096.0,12.0);
            if(aKind>64.5 && aKind<65.5)p.x+=uScene.y*8.0;
            if(aKind>56.5 && aKind<57.5)p.y-=1.2*(1.0-uScene.x);
            if(aKind>55.5 && aKind<56.5){p.x+=sin(uTime*.045+aPos.z*.01)*3.0;p.z+=cos(uTime*.031+aPos.x*.01)*2.0;}
            if(aKind>62.5 && aKind<64.5){p.x+=sin(uTime*.07+aPos.z*.02)*5.0;p.y+=sin(uTime*.25+aPos.x*.1)*.35;}
            if(uWorld>20.5 && uWorld<21.5 && abs(aPos.x)>8.7)p.x+=sign(aPos.x)*uScene.x*2.0;
            if(aKind>46.5 && aKind<47.5)p.z+=uStory.z*.45;
            if(aKind>51.5 && aKind<52.5){p.y=mod(p.y+uTime*3.5,16.0);p+=uEntity;}
            if(aKind>52.5 && aKind<54.5)p+=uEntity;
            if(((aKind>31.5 && aKind<32.5)||(aKind>96.5 && aKind<99.5))) {
                p.xz=mat2(cos(uActorYaw),sin(uActorYaw),-sin(uActorYaw),cos(uActorYaw))*p.xz;
                p+=uEntity;
                if(aKind<98.5)p.y+=.035*sin(uTime*.9+aPos.y);
                if(aKind>97.5)p.x+=sin(uTime*.85+aPos.y*1.8)*.14*max(0.0,3.6-aPos.y);
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
            if(((aKind>31.5 && aKind<32.5)||(aKind>96.5 && aKind<99.5)))n.xz=mat2(cos(uActorYaw),sin(uActorYaw),-sin(uActorYaw),cos(uActorYaw))*n.xz;
            if(aKind>82.5 && aKind<83.5){float a=uCreature.w;n=vec3(n.x*cos(a)-n.z*sin(a),n.y,n.x*sin(a)+n.z*cos(a));}
            // El generador histórico de suelos tiene algunas normales invertidas.
            if (uWorld<29.5 && aKind >= 4.0 && n.y < -.68) n.y = -n.y;
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
            if (((uWorld > 11.5 && uWorld < 12.5) || ((uWorld>18.5 && uWorld<19.5) || (uWorld>48.5 && uWorld<50.5)))) {
                vec3 moonlight=vec3(.48,.57,.82)*(.46+.48*sun+.15*hemisphere);
                vColor=aColor*moonlight;
            }
            if((uWorld>2.5 && uWorld<3.5) || (uWorld>4.5 && uWorld<5.5) ||
                (uWorld>6.5 && uWorld<7.5) || (uWorld>10.5 && uWorld<11.5)) {
                vColor*=vec3(.78,.83,.98);
            }
            if(uWorld>29.5 && uWorld<40.5){
                bool interior=(uWorld>30.5 && uWorld<31.5)||uWorld>37.5;
                vColor=aColor*(interior?.98:(.74+.26*sun));
            }
            if (uAnomaly > .01 && aKind < 19.5) {
                vColor = mix(vColor,vec3(vColor.b*.85,vColor.r*.82,vColor.g),uAnomaly*.14);
            }
            if (aKind > 19.5 && aKind < 20.5) {
                float fluffy = .79 + .18*max(n.y,0.0) + .06*sun;
                vColor = aColor * fluffy;
                p.x += .25 * sin(uTime*.11 + aPos.z*.017);
            }
            if((aKind>48.5 && aKind<49.5)||(aKind>77.5 && aKind<78.5)||(aKind>91.5 && aKind<92.5)||(aKind>93.5 && aKind<94.5))vColor=aColor;
            if(aKind>96.5 && (aKind<97.5 || aKind>98.04))vColor=aColor;
            if(aKind>104.5 && aKind<105.5)vColor=aColor;
            if(aKind>116.5 && aKind<121.5)vColor=aColor;
            // 2.5: material albedo is lit once, per fragment. Keep encoded UV,
            // emissive and animated entity colors on their existing paths.
            bool textured=aKind<.5 || (aKind>3.5 && aKind<19.5) ||
                (aKind>24.5 && aKind<27.5) || (aKind>39.5 && aKind<44.5) ||
                (aKind>89.5 && aKind<91.5) || (aKind>92.5 && aKind<93.5) ||
                (aKind>103.5 && aKind<104.5) || (aKind>107.5 && aKind<108.5) ||
                (aKind>109.5 && aKind<110.5);
            if(textured || (aKind>110.5 && aKind<115.5))vColor=aColor;
            vPosition = p;
            // Local texture coordinates keep the remote pool stable on mediump GPUs.
            if(p.x>1000.0)vPosition.x-=1600.0;
            vNormal = n;
            // Escalar antes de interpolar impide overflow de length() en mediump.
            // 300 metros al cuadrado supera el rango mínimo de GLES2 mediump.
            vEye100 = (uCamera - p) * .01;
            vKind = aKind;
            float scale = crater ? .30 : (uWorld>29.5?1.0:.62);
            if (abs(n.y) > .66) vTexCoord = vPosition.xz * scale;
            else if (abs(n.x) > abs(n.z)) vTexCoord = vPosition.zy * scale;
            else vTexCoord = vPosition.xy * scale;
            float dist = distance(p.xz,uCamera.xz);
            float fog = clamp((dist-uFogNear)/max(uFogFar-uFogNear,1.0),0.0,1.0);
            vFog = (aKind > 19.5 && aKind < 20.5)
                ? clamp(fog*.37,0.0,.34) : clamp(fog,0.0,.92);
            vec4 reflected=uMirrorVP*vec4(p,1.0);
            vReflection=vec3(reflected.xy*.5+reflected.w*.5,reflected.w);
            vShadow=uLightVP*vec4(p,1.0);
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
        uniform sampler2D uMaterial;
        uniform mediump vec4 uLook;
        uniform mediump vec3 uClothMean;
        #ifdef GL_FRAGMENT_PRECISION_HIGH
        uniform highp sampler2D uShadow;
        #else
        uniform mediump sampler2D uShadow;
        #endif
        uniform mediump float uShadowReady;
        uniform mediump float uShadowTexel;
        uniform mediump vec4 uStory;
        uniform mediump vec3 uLamp;
        uniform mediump float uPhoto;
        uniform mediump vec4 uScene;
        uniform mediump vec4 uWeather;
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
        uniform mediump vec3 uCloudCamera;
        uniform mediump vec4 uCloudParams;
        uniform mediump vec2 uCloudDrift;
        uniform sampler2D uMoon;
        uniform float uModeledMoons;uniform float uLunarBlood;
        uniform sampler2D uPlanets;
        uniform sampler2D uWeeping;
        uniform vec4 uTools;
        uniform vec3 uTorchForward;
        uniform mediump vec4 uCreature;
        uniform mediump float uUniverse;
        uniform mediump float uEclipse;
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
        #ifdef GL_FRAGMENT_PRECISION_HIGH
        varying highp vec3 vPosition;
        #else
        varying mediump vec3 vPosition;
        #endif
        varying mediump vec3 vNormal;
        varying mediump vec3 vEye100;
        #ifdef GL_FRAGMENT_PRECISION_HIGH
        varying highp vec2 vTexCoord;
        #else
        varying mediump vec2 vTexCoord;
        #endif
        varying mediump float vFog;
        varying mediump float vKind;
        varying mediump vec3 vReflection;
        #ifdef GL_FRAGMENT_PRECISION_HIGH
        varying highp vec4 vShadow;
        #else
        varying mediump vec4 vShadow;
        #endif

        vec3 planetAlbedo(vec3 n,float index,float time) {
            n=normalize(n);
            vec2 uv=vec2(fract(atan(n.z,n.x)/6.2831853+.5+time*.00065),.5-asin(clamp(n.y,-1.0,1.0))/3.1415926);
            vec2 tile=vec2(mod(index,2.0),floor(index*.5));
            vec3 land=texture2D(uPlanets,(tile+vec2(.012)+uv*.976)*.5).rgb;
            float clouds=texture2D(uClouds,uv*2.0+vec2(time*.0005,0.0)).r;
            float veil=smoothstep(.28,.73,clouds)*(.12+.12*step(index,.5));
            return mix(land,vec3(.91,.94,.98),veil);
        }
        // Animated stone face in lunar coordinates: eyelids, tracking irises and a widening grin.
        vec4 livingMoon(vec3 ray,vec3 direction,float radius,vec3 tint,float smile,float blink,vec2 eyeAim) {
            direction=normalize(direction);
            vec3 right=normalize(cross(direction,vec3(0.0,1.0,0.0)));
            vec3 up=cross(right,direction);
            float facing=dot(ray,direction);
            vec2 q=vec2(dot(ray,right),dot(ray,up))/max(.04,facing)/max(radius,.02);
            if(facing<=.001 || length(q)>1.01)return vec4(0.0);
            vec2 sampleQ=q;
            float mouth=exp(-dot((q-vec2(0.0,-.38))/vec2(.65,.23),(q-vec2(0.0,-.38))/vec2(.65,.23)));
            sampleQ.x/=1.0+smile*.19*mouth;
            sampleQ.y+=.055*smile*mouth*(1.0-q.x*q.x);
            for(int i=0;i<2;i++) {
                float side=float(i)*2.0-1.0;
                vec2 eye=q-vec2(side*.35,.19);
                float zone=1.0-smoothstep(.80,1.1,length(eye/vec2(.22,.14)));
                sampleQ-=eyeAim*zone*.6;
            }
            vec4 stone=texture2D(uMoon,vec2(.5+sampleQ.x*.5,.5-sampleQ.y*.5));
            vec3 color=stone.rgb*tint*1.25;
            for(int i=0;i<2;i++) {
                float side=float(i)*2.0-1.0;
                vec2 eye=q-vec2(side*.35,.19);
                float socket=1.0-smoothstep(.84,1.08,length(eye/vec2(.21,.125)));
                vec3 lid=texture2D(uMoon,vec2(.5+q.x*.5,.5-(.36+abs(eye.y)*.25)*.5)).rgb*tint;
                color=mix(color,lid,socket*blink);
                float pupil=exp(-dot((eye-eyeAim)*vec2(24.0,32.0),(eye-eyeAim)*vec2(24.0,32.0)));
                color+=vec3(.08,.19,.34)*pupil*(1.0-blink)*(.3+.7*smile);
            }
            float disk=1.0-smoothstep(.965,.995,length(q));
            return vec4(clamp(color,0.0,1.0),stone.a*disk);
        }
        vec3 lunarReflections(vec3 ray,vec3 sky) {
            vec4 a=livingMoon(ray,vec3(.08,.50,-.86),.285,mix(vec3(.25,.50,1.0),vec3(.20,.30,1.0),uMoonMood.z),uMoonMood.z,uMoonMood.x,uEyeTarget.xy);
            vec4 b=livingMoon(ray,vec3(-.60,.48,-.63),.105,vec3(.76,.67,.88),uMoonMood.w,uMoonMood.y,uEyeTarget.zw);
            sky=mix(sky,mix(a.rgb,a.rgb*vec3(2.8,.24,.16),uLunarBlood),a.a);sky=mix(sky,b.rgb,b.a);
            if(uGiant.w>.001) {
                vec4 g=livingMoon(ray,uGiantDirection,uGiant.x,vec3(.18,.40,1.15),uGiant.y,uGiant.z,uEyeTarget.xy);
                sky=mix(sky,g.rgb,g.a*uGiant.w);
            }
            return sky;
        }

        vec3 environmentColor(vec3 ray) {
            if(((uWorld>11.5 && uWorld<12.5) || ((uWorld>18.5 && uWorld<19.5) || (uWorld>48.5 && uWorld<50.5)))) {
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
        float surfaceShadow(vec3 normal) {
            if(uShadowReady<.5 || !((vKind<27.5 || (vKind>39.5 && vKind<44.5) || (vKind>111.5 && vKind<115.5)) &&
                !(vKind>.5 && vKind<3.5)))return 1.0;
            vec3 light=vShadow.xyz/max(vShadow.w,.001)*.5+.5;
            if(min(light.x,light.y)<=.003 || max(light.x,light.y)>=.997 || light.z<=0.0 || light.z>=1.0)return 1.0;
            float shade=0.0;
            float bias=.0015+.0025*(1.0-max(dot(normal,normalize(uSun)),0.0));
            // Bilinear percentage-closer filtering smooths a moving shadow edge
            // without interpolating packed depths across different surfaces.
            vec2 pixel=light.xy/uShadowTexel-.5;
            vec2 origin=(floor(pixel)+.5)*uShadowTexel;
            vec2 fraction=fract(pixel);
            for(int i=0;i<4;i++) {
                vec2 corner=vec2(mod(float(i),2.0),floor(float(i)*.5));
                vec2 depth=texture2D(uShadow,origin+corner*uShadowTexel).rg;
                vec2 weight=mix(1.0-fraction,fraction,corner);
                shade+=smoothstep(light.z-bias-.0014,light.z-bias+.0014,depth.r+depth.g/255.0)*weight.x*weight.y;
            }
            return shade;
        }
        float pointLight(vec3 position,vec3 normal,vec3 lamp,float radius) {
            vec3 delta=(lamp-position)/radius;
            float distance2=dot(delta,delta);
            float falloff=max(0.0,1.0-distance2);
            return falloff*falloff*max(dot(normal,delta*inversesqrt(max(distance2,.0001))),0.0);
        }
        vec3 roomLighting(vec3 position,vec3 normal) {
            vec3 light=vec3(0.0);
            // Positions match the authored fixtures. Nearest periodic fixtures
            // are evaluated per surface, rather than interpolated across a long wall.
            if(uWorld>37.5 && uWorld<38.5) {
                float index=clamp(floor((-position.z-6.0)/5.0+.5),0.0,31.0);
                for(int i=-1;i<=1;i++) {
                    float k=index+float(i);
                    if(k>=0.0 && k<=31.0) {
                        light+=vec3(1.0,.79,.53)*pointLight(position,normal,vec3(0.0,4.55,-6.0-k*5.0),7.0)*.72;
                        light+=vec3(.68,.85,1.0)*pointLight(position,normal,vec3(-3.3,2.4,-7.0-k*5.0),5.0)*.85;
                    }
                }
            } else if(uWorld>38.5 && uWorld<39.5) {
                float z=-12.0-clamp(floor((-position.z-12.0)/16.0+.5),0.0,11.0)*16.0;
                float y=clamp(floor(position.y/8.0+.5),0.0,2.0)*8.0+5.5;
                light+=vec3(.87,.85,.79)*pointLight(position,normal,vec3(-15.5,y,z),9.0)*1.2;
            } else if(uWorld>39.5 && uWorld<40.5) {
                float z=-4.0-clamp(floor((-position.z-4.0)/9.5+.5),0.0,3.0)*9.5;
                light+=vec3(.81,.89,1.0)*(pointLight(position,normal,vec3(-4.5,3.7,z),10.0)+pointLight(position,normal,vec3(4.5,3.7,z),10.0))*.70;
            } else if(uWorld>42.5 && uWorld<43.5) {
                vec2 cell=clamp(floor((position.xz+vec2(6.0))/12.0),vec2(-3.0,-6.0),vec2(3.0,0.0))*12.0;
                light+=vec3(1.0,.91,.64)*pointLight(position,normal,vec3(cell.x,3.42,cell.y),10.0)*.84;
            }
            return light;
        }
        void main() {
            if(vKind>59.5 && vKind<60.5 && uPhoto<.5)discard;
            if(vKind>78.5 && vKind<79.5 && uTools.x<.5)discard;
            if(vKind>65.5 && vKind<66.5 && uScene.z<.5)discard;
            if(uMirrorPass>.5 && (uWaterReflection>.5 ? (vPosition.y<uWaterPlane+.015 || (vKind>.5 && vKind<1.5)) : (vPosition.z<uMirrorZ+.08 || (vKind>30.5 && vKind<31.5))))discard;
            if(((vKind>31.5 && vKind<32.5)||(vKind>96.5 && vKind<99.5)) && uPresence<.015)discard;
            // El cielo sustituye las antiguas nubes de elipsoides sólidos.
            if (vKind > 19.5 && vKind < 20.5) discard;
            bool crater = uWorld > 1.5 && uWorld < 2.5;
            if(vKind>98.5 && vKind<99.5){vec2 q=vColor.xy*2.0-1.0;float opacity=(1.0-smoothstep(.1,1.0,dot(q,q)))*uPresence*.24;gl_FragColor=vec4(vec3(.015,.024,.025),opacity);return;}
            vec3 color = vColor;
            float eyeDistance = length(vEye100) * 100.0;
            vec3 normal = normalize(vNormal);
            float material=-1.0;
            if(vKind>103.5 && vKind<104.5)material=1.0;
            if(vKind>107.5 && vKind<108.5)material=0.0;
            if(vKind>109.5 && vKind<110.5)material=15.0;
            if(vKind<.5)material=(vColor.g>vColor.r*1.18 && vColor.g>vColor.b*1.12)?0.0:1.0;
            if(vKind>3.5 && vKind<19.5)material=floor(vKind-4.0+.5);
            if(vKind>24.5 && vKind<25.5)material=7.0;
            if(vKind>25.5 && vKind<26.5)material=6.0;
            if(vKind>26.5 && vKind<27.5)material=2.0;
            if(vKind>39.5 && vKind<44.5)material=4.0+floor(vKind-40.0+.5);
            if(vKind>41.5 && vKind<42.5)material=9.0;
            if(vKind>42.5 && vKind<43.5)material=8.0;
            if(vKind>43.5 && vKind<44.5)material=10.0;
            if(vKind>89.5 && vKind<90.5)material=0.0;
            if(vKind>90.5 && vKind<91.5)material=1.0;
            if(vKind>92.5 && vKind<93.5)material=3.0;
            if(uWorld>29.5 && vKind>14.5 && vKind<15.5)material=15.0;
            if(vKind>111.5 && vKind<115.5)material=16.0+floor(vKind-112.0+.5);
            float roughness=.85;
            if(material>=0.0) {
                vec2 tile=vec2(mod(material,4.0),floor(material/4.0));
                vec2 surfaceUV=vTexCoord;
                if(uWorld>29.5){
                    surfaceUV=(abs(vNormal.y)>.66?vPosition.xz:(abs(vNormal.x)>abs(vNormal.z)?vPosition.zy:vPosition.xy));
                    if(vKind>89.5 && vKind<90.5)surfaceUV=vPosition.zy*.36;
                }
                // Physical scale matters: cloth fibers and grout are much finer
                // than the large stone and ceramic slabs used elsewhere.
                if(material<.5)surfaceUV*=2.7;
                else if(material>8.5 && material<9.5)surfaceUV*=7.0;
                else if(material>4.5 && material<5.5)surfaceUV*=3.0;
                else if((material>.5 && material<1.5)||(material>9.5 && material<11.5))surfaceUV*=2.0;
                vec2 repeatUV=fract(surfaceUV*.55);
                vec2 slopeSign=vec2(1.0);
                if(material>=0.0) {
                    vec2 phase=fract(surfaceUV*.275)*2.0;
                    repeatUV=1.0-abs(phase-1.0);
                    slopeSign=vec2(phase.x<1.0?1.0:-1.0,phase.y<1.0?1.0:-1.0);
                }
                vec2 uv=(tile+vec2(material<.5?.12:.055)+repeatUV*(material<.5?.76:.89))/vec2(4.0,8.0);
                vec4 detail=vec4(.5,.5,.90,.50);
                bool rough=(material<4.0)||(material>7.5 && material<14.5)||material>15.5;
                bool cloth=material>8.5 && material<9.5;
                float clothFade=cloth?smoothstep(1.5,6.0,eyeDistance):0.0;
                if((eyeDistance<75.0 || !rough) && clothFade<.98)detail=texture2D(uMaterial,uv);
                vec3 surface=clothFade>.98?uClothMean:texture2D(uAtlas,uv).rgb;
                if(cloth)surface=mix(surface,uClothMean,.70+.30*clothFade);
                if(material<.5){
                    // Broad variation breaks the repeated carpet look without
                    // another sampler or a full-screen pass.
                    float patch=sin(vPosition.x*.071+sin(vPosition.z*.041))*sin(vPosition.z*.053);
                    surface*=.94+.10*patch;
                    surface=mix(surface,vec3(.26,.36,.16),smoothstep(25.0,130.0,eyeDistance)*.32);
                    if(abs(normal.y)<.5)surface*=.80+.20*sin(vPosition.y*.43+sin(vPosition.x*.29+vPosition.z*.27));
                }
                float relief=material<.5?.42:material<3.0?.25:.18;
                vec2 slope=(detail.rg*2.0-1.0)*slopeSign*relief*(1.0-smoothstep(12.0,75.0,eyeDistance))*(1.0-clothFade);
                vec3 tangent=abs(normal.y)>.66?vec3(1.0,0.0,0.0):(abs(normal.x)>.66?vec3(0.0,0.0,1.0):vec3(1.0,0.0,0.0));
                vec3 bitangent=abs(normal.y)>.66?vec3(0.0,0.0,1.0):vec3(0.0,1.0,0.0);
                vec3 gradient=tangent*slope.x+bitangent*slope.y;
                normal=normalize(normal-gradient+normal*dot(normal,gradient));
                roughness=cloth?.98:detail.b;
                // Approximate linear-light shading, with a preserved artistic
                // tint. Rough surfaces retain diffuse detail; glaze catches light.
                vec3 tint=mix(vec3(1.0),clamp(vColor*1.24,vec3(.26),vec3(1.12)),.42);
                vec3 albedo=surface*tint;
                vec3 linearAlbedo=albedo*albedo;
                float hemisphere=clamp(normal.y*.5+.5,0.0,1.0);
                vec3 ambient=mix(vec3(.63,.60,.66),vec3(.91,.97,1.04),hemisphere);
                ambient=mix(ambient,ambient*vec3(.57,.70,1.08),uLook.z);
                vec3 sunlight=mix(vec3(1.06,.97,.88),vec3(.61,.75,1.12),uLook.z);
                float visibility=surfaceShadow(normalize(vNormal));
                if(uLook.w>.5 && uLook.y>.11){
                    vec2 cloudPoint=(vPosition.xz+normalize(uSun).xz*130.0)*uCloudParams.x+uCloudDrift;
                    float cloud=texture2D(uClouds,cloudPoint).r;
                    visibility*=1.0-cloud*.19;
                }
                float diffuse=max(dot(normal,normalize(uSun)),0.0)*uLook.y*visibility;
                float cavity=mix(.86,1.0,smoothstep(.05,.80,detail.a));
                vec3 irradiance=ambient*uLook.x*cavity+sunlight*diffuse;
                if(uLook.y<.11)irradiance+=roomLighting(vPosition,normal);
                vec3 viewDir=normalize(vEye100+vec3(0.0,.0001,0.0));
                vec3 halfLight=normalize(normalize(uSun)+viewDir+vec3(0.0,.0001,0.0));
                float specular=pow(max(dot(normal,halfLight),0.0),mix(88.0,10.0,roughness));
                float rim=1.0-clamp(dot(normal,viewDir),0.0,1.0);
                float fresnel=.04+.18*rim*rim*rim;
                vec3 reflectedLight=sunlight*specular*(1.0-roughness)*fresnel*3.0*uLook.y*visibility;
                vec3 radiance=max(linearAlbedo*irradiance+reflectedLight,vec3(0.0));
                // Filmic shoulder keeps the porcelain and marble from clipping.
                radiance=(radiance*(2.51*radiance+.03))/(radiance*(2.43*radiance+.59)+.14);
                color=sqrt(clamp(radiance,0.0,1.0));
            }
            if(material<0.0)color*=.64+.36*surfaceShadow(normal);
            if(uWorld>15.5 && uWorld<16.5 && normal.y>.6) {
                float beam=exp(-pow(vPosition.x*.075,2.0))*smoothstep(8.0,50.0,-vPosition.z);
                color+=vec3(.17,.09,.025)*beam;
            }
            if(uWorld>14.5 && uWorld<15.5 && normal.y>.6)color+=vec3(.01,.025,.03)*pow(max(0.0,sin(vPosition.z*.8+uTime*.32)),4.0);
            if(uWorld>4.5 && uWorld<5.5 && normal.y>.65){float caustic=sin(vPosition.x*.23+sin(vPosition.z*.13)+uTime*.13)*sin(vPosition.z*.19-uTime*.17);color+=vec3(.01,.025,.03)*max(.0,caustic);}
            if(uStory.x>.5) {
                vec3 delta=(vPosition-uLamp)*.12;
                float glow=max(0.0,1.0-dot(delta,delta));
                color+=vec3(.27,.14,.045)*glow*glow;
            }
            if(vKind>45.5 && vKind<46.5)color=mix(vec3(.21,.22,.24),vec3(1.0,.80,.40),uStory.x);
            if(vKind>47.5 && vKind<48.5)color+=vec3(.055,.09,.08)*uStory.w*(.8+.2*sin(uTime*3.0));
            if(uTools.x>.5){
                vec3 toSurface=-normalize(vEye100);float cone=smoothstep(.80,.96,dot(toSurface,uTorchForward));
                float light=cone/(1.0+dot(vEye100,vEye100)*650.0);color+=vec3(.46,.36,.19)*light;
            }
            if(vKind>77.5 && vKind<78.5){
                float index=clamp(floor((vKind-78.0)*10.0+.5),0.0,3.0);
                vec2 uv=(clamp(vColor.xy,.008,.992)+vec2(mod(index,2.0),floor(index*.5)))*.5;
                color=texture2D(uWeeping,uv).rgb*.88+vec3(.025,.024,.028);
            }
            if(vKind>78.5 && vKind<79.5)color=vec3(.26,.62,.66)+vec3(.05)*sin(vPosition.y*3.0+uTime);
            if(vKind>82.5 && vKind<83.5){float ridges=.5+.5*sin(vPosition.z*1.8+sin(vPosition.x*.8));color=mix(vec3(.045,.11,.16),vec3(.22,.37,.42),ridges*.55+max(normal.y,0.0)*.2);}
            if(vKind>83.5 && vKind<84.5)color=vec3(.51,.48,.45);
            if(uWorld>4.5 && uWorld<5.5 && normal.y>.7 && vPosition.y<2.0){vec2 shadow=(vPosition.xz-uCreature.xz)*vec2(.035,.018);color*=1.0-.20*exp(-dot(shadow,shadow)*3.0);}
            if(vKind>48.5 && vKind<49.5) {
                vec2 face=vec2(vColor.r,1.0-vColor.g);
                vec3 faceTex=texture2D(uWeeping,face).rgb;
                color=faceTex*(.58+.42*max(dot(normal,normalize(uSun)),0.0));
            }
            if(vKind>49.5 && vKind<50.5) {
                float stream=.5+.5*sin(vPosition.x*5.0+vPosition.y*.8+uTime*3.2);
                color=mix(vec3(.22,.50,.54),vec3(.64,.85,.86),stream*.58);
            }
            if(vKind>54.5 && vKind<55.5) {
                color=planetAlbedo(normal,mod(floor((vKind-55.0)*10.0+.5)+uUniverse,4.0),uTime)*(.24+.76*max(dot(normal,normalize(uSun)),0.0));
                color+=vec3(.025,.07,.12)*pow(1.0-abs(dot(normal,normalize(vEye100+vec3(0.0,.001,0.0)))),3.0);
            }
            if(vKind>55.5 && vKind<56.5){float shade=.63+.30*max(dot(normal,normalize(uSun)),0.0);color=mix(vec3(.64,.73,.86),vec3(.99,.97,.96),shade);}
            if(vKind>69.5 && vKind<70.5){float flow=.5+.5*sin(vPosition.y*2.5-uTime*1.4);color=mix(vec3(.12,.31,.40),vec3(.56,.76,.83),flow);}
            if(vKind>57.5 && vKind<58.5)color=mix(vec3(.95,.32,.75),vec3(.99,.73,.25),clamp(1.0-vPosition.y*.08,0.0,1.0));
            if(vKind>59.5 && vKind<60.5)color=vec3(.89,.91,.72);
            if(vKind>62.5 && vKind<64.5){float g=.5+.5*sin(vPosition.x*.5+uTime*.3);color+=vec3(.035,.07,.10)*g;}
            if(vKind>50.5 && vKind<51.5) {
                vec3 halfLight=normalize(normalize(uSun)+normalize(vEye100+vec3(0.0,.0001,0.0)));
                color+=vec3(.35,.29,.16)*pow(max(dot(normal,halfLight),0.0),38.0);
            }
            if(uWeather.x>1.5 && uWeather.x<2.5) {
                vec2 d=(vPosition.xz-uWeather.zw)*.05;
                color*=1.0-.25*uWeather.y*exp(-min(dot(d,d),12.0));
            }
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
            if(((uWorld>11.5 && uWorld<12.5) || ((uWorld>18.5 && uWorld<19.5) || (uWorld>48.5 && uWorld<50.5))) && vKind>25.5 && vKind<26.5) {
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
            if ((vKind > .5 && vKind < 1.5) || (vKind>58.5 && vKind<59.5) || (vKind>115.5 && vKind<116.5)) {
                // Shared reflective water for the pools and later worlds.
                // Reuses one cached planar target for the lake or the two pool channels.
                float r1=sin(vPosition.x*.60+uTime*.32+sin(vPosition.z*.38-uTime*.19));
                float r2=cos(vPosition.z*.68-uTime*.29+sin(vPosition.x*.31+uTime*.16));
                vec3 waterN=normalize(vec3(r1*.028,(vKind>58.5 && vKind<59.5)?-1.0:1.0,r2*.028));
                vec3 viewDir=normalize(vEye100+vec3(0.0,.00001,0.0));
                float rim=1.0-clamp(dot(viewDir,waterN),0.0,1.0);
                float fresnel=rim*rim*rim*rim*rim;
                vec3 reflected=reflect(-viewDir,waterN);
                vec3 sky=environmentColor(reflected);
                vec3 body=mix(vColor,vec3(.12,.40,.43),crater?.48:.40);
                color=mix(body,sky,.12+.72*fresnel);
                vec3 halfLight=normalize(normalize(uSun)+viewDir+vec3(0.0,.0001,0.0));
                color+=vec3(.30,.27,.21)*pow(max(dot(waterN,halfLight),0.0),72.0);
                color+=vec3(.008,.021,.024)*(r1*r2);
                if(uWaterReflection>.5 && uMirrorReady>.5 && uMirrorPass<.5){
                    vec2 uv=vReflection.xy/max(vReflection.z,.001)+vec2(r1,r2)*.001;
                    vec3 actual=texture2D(uReflection,clamp(uv,vec2(.002),vec2(.998))).rgb;
                    color=mix(color,actual,.42+.40*fresnel);
                }
            }
            if(uWorld>41.5 && uWorld<42.5 && vKind>.5 && vKind<1.5){
                vec2 p=vPosition.xz;
                float swell=sin(p.x*.13+p.y*.085+uTime*.20);
                vec3 waterN=normalize(vec3(sin(p.x*.23+p.y*.14+uTime*.23)*.006,1.0,cos(p.y*.29-p.x*.17-uTime*.27)*.007));
                vec3 viewDir=normalize(vEye100+vec3(0.0,.0001,0.0));float fresnel=pow(1.0-clamp(dot(viewDir,waterN),0.0,1.0),3.0);
                color=mix(vec3(.075,.37,.40),environmentColor(reflect(-viewDir,waterN))*vec3(.75,.98,1.0),.20+fresnel*.40);
                color+=vec3(.014,.026,.021)*swell;
                float shore=abs(length(p-vec2(0.0,-45.0))-34.0);
                float foam=(1.0-smoothstep(.8,2.7,shore))*(.55+.45*sin(p.x*.7+p.y*.9-uTime*.6));
                color=mix(color,vec3(.70,.86,.79),foam*.25);
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
                color=mix(color,color.bgr,uStory.y*.7);
                if(uWorld>13.5 && uWorld<14.5)color=mix(vec3(.64,.73,.84),vec3(.96,.93,.90),field.r*.7+horizon*.3);
                if(uWorld>14.5 && uWorld<15.5) {
                    float caustic=pow(max(0.0,sin(vPosition.z*.23+uTime*.19)*sin(vPosition.y*1.9+uTime*.13)),3.0);
                    color=mix(vec3(.025,.12,.18),vec3(.16,.39,.44),horizon)+vec3(.035,.07,.06)*caustic;
                    float reeds=pow(max(0.0,sin(vPosition.z*.38+sin(vPosition.y*.47+uTime*.08)*.4)),24.0)*(1.0-horizon);
                    color*=1.0-reeds*.45;
                }
                if(uWorld>15.5 && uWorld<16.5) {
                    float sun=exp(-dot((vPosition.xy-vec2(0.0,3.2))*vec2(.18,.65),(vPosition.xy-vec2(0.0,3.2))*vec2(.18,.65)));
                    color=mix(vec3(.92,.51,.40),vec3(.96,.82,.62),horizon)+vec3(.16,.12,.04)*sun;
                }
                if(uStory.x>.5 && uWorld>5.5 && uWorld<6.5)color=mix(color,vec3(.95,.72,.35),.6);
            }
            if(uTools.y>.5 && vKind>33.5 && vKind<34.5){color=mix(color,color.brg*vec3(.75,1.10,1.18),.8);color+=vec3(.10,.04,.13)*sin(vPosition.y*.4+uTime*.15);}
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
            if(vKind>89.5 && vKind<90.5){float wet=.5+.5*sin(vPosition.z*.12+vPosition.y*.08);color*=vec3(.68,.85,.59)*(.78+.22*wet);}
            if(vKind>90.5 && vKind<91.5){
                vec2 grid=abs(fract(vPosition.xz*1.25)-.5);float joint=smoothstep(.485,.497,max(grid.x,grid.y));
                vec2 uv=vPosition.xz*.028;float cloud=smoothstep(.025,.30,texture2D(uClouds,uv+vec2(.3,.5)).r);
                color=mix(color,vec3(.24,.28,.33),cloud*.75);color*=1.0-joint*.30;
            }
            if(vKind>91.5 && vKind<92.5){
                vec2 q=vColor.xy*2.0-1.0;float radial=1.0-smoothstep(.40,1.0,dot(q,q));
                float noise=texture2D(uClouds,vColor.xy*.65+vec2(uTime*.0013,.23)).r;
                float density=radial*smoothstep(.015,.52,noise)*.65;
                if(density<.012)discard;
                color=mix(vec3(.67,.74,.83),vec3(.98,.97,.94),noise);gl_FragColor=vec4(color,density);return;
            }
            if(vKind>92.5 && vKind<93.5){
                float sheen=.5+.5*sin(vPosition.x*.22+vPosition.z*.11+normal.x*.4);
                vec3 oil=.5+.5*cos(6.283*(sheen+vec3(.0,.33,.67)));
                float puddle=smoothstep(.15,.70,sin(vPosition.x*.30+sin(vPosition.z*.10))*sin(vPosition.z*.17));
                color=mix(color,oil*.55+color*.5,.28*puddle);color+=vec3(.10,.08,.04)*pow(max(normal.y,0.0),6.0);
            }
            if(vKind>93.5 && vKind<94.5){
                vec2 q=vColor.xy-.5;float t=uTime*.0015;float wave=smoothstep(.025,.42,texture2D(uClouds,vColor.xy*.7+vec2(t,.31)).r);
                vec3 film=mix(vec3(.08,.13,.27),vec3(.79,.60,.49),wave);
                color=film*(.72+.12*sin(uTime*.17))+vec3(.10,.08,.05);
            }
            if(vKind>94.5 && vKind<95.5){float scan=.75+.15*sin(vPosition.y*80.0);color=vec3(.05,.13,.16)*scan+vec3(.02,.045,.03)*step(.7,sin(vPosition.x*18.0+uTime*.2));}
            if(vKind>95.5 && vKind<96.5)color=vec3(.94,.95,.88);

            if(uWorld>42.5 && uWorld<43.5 && vKind<104.5){vec2 local=mod(vPosition.xz+vec2(6.0),12.0)-6.0;float lamp=.76+.22/(1.0+dot(local,local)*.045);color*=lamp*vec3(1.03,1.0,.91);if(abs(normal.y)<.5)color*=.78+.22*smoothstep(.05,.7,vPosition.y);}
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

            if(vKind>97.5 && vKind<98.04)color=vColor*(.75+.25*max(normal.y,0.0));

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
                float index=20.0+floor(vColor.z+.5);
                vec2 tile=vec2(mod(index,4.0),floor(index/4.0));
                vec2 uv=(tile+vec2(.018)+clamp(vColor.xy,0.0,1.0)*.964)/vec2(4.0,8.0);
                vec4 leaf=texture2D(uAtlas,uv);
                if(leaf.a<mix(.46,.30,smoothstep(20.0,90.0,eyeDistance)))discard;
                float solar=abs(dot(normal,normalize(uSun)));
                float backlight=pow(max(dot(-normalize(vEye100+vec3(0.0,.0001,0.0)),normalize(uSun)),0.0),5.0);
                vec3 diffuse=mix(vec3(.55,.62,.63),vec3(.32,.43,.65),uLook.z)*uLook.x+vec3(1.0,.97,.82)*(solar*.52+backlight*.26)*uLook.y;
                color=sqrt(max(leaf.rgb*leaf.rgb*diffuse,vec3(0.0)));
                color=mix(color,uFogColor,smoothstep(uFogNear,uFogFar,eyeDistance)*.72);
            }
            if(uWorld>.5 && uWorld<1.5 && vPosition.y<-.13 && material>=0.0){
                float ripple=sin(vPosition.x*2.8+sin(vPosition.z*1.8+uTime*.42))*sin(vPosition.z*3.4+sin(vPosition.x*1.3-uTime*.37));
                float caustic=pow(max(0.0,ripple),6.0);
                color=color*vec3(.66,.88,.91)+vec3(.035,.070,.074)*caustic;
            }
            color*=1.0-uEclipse*.45;
            float alpha=((vKind>31.5 && vKind<32.5)||(vKind>96.5 && vKind<99.5))?uPresence:1.0;
            if(vKind>120.5 && vKind<121.5){
                float lit=.4+.6*max(dot(normal,normalize(uSun)),0.0);
                vec3 edge=vec3(.025,.028,.045)*pow(1.0-abs(dot(normal,normalize(vEye100+vec3(.0,.001,.0)))),2.0);
                gl_FragColor=vec4(vColor*lit+edge,1.0);return;
            }
            if(vKind>116.5 && vKind<120.5){
                float tile=vKind<117.5?0.0:(vKind<118.5?2.0:(vKind<119.5?1.0:3.0));
                vec2 uv=clamp(vColor.xy,.012,.988);
                if(vKind>118.5 && vKind<119.5)uv=fract(vPosition.xy*.19+vPosition.z*.073);
                if(vKind>119.5)uv=fract(vPosition.xy*.011+vPosition.z*.005);
                if(vKind<117.5){
                    vec3 look=normalize(vEye100+vec3(0.0,.0001,0.0));uv+=clamp(look.xy,vec2(-.4),vec2(.4))*.065;
                }
                vec3 detail=texture2D(uWeeping,(uv+vec2(mod(tile,2.0),floor(tile/2.0)))*.5).rgb;
                if(vKind>119.5)detail=mix(detail,vec3(.93,.91,.86),.68);
                if(vKind>117.5 && vKind<118.5)detail=mix(detail,vec3(.98,.97,.94),.24);
                float illumination=.68+.52*max(dot(normal,normalize(uSun)),0.0);
                if(vKind<117.5){float closed=exp(-pow((mod(uTime+vPosition.x*.002,6.7)-.24)/.105,2.0));
                    float lid=1.0-smoothstep(.015,.06,abs(uv.y-.5)-.46*(1.0-closed));
                    vec3 skin=texture2D(uWeeping,(vec2(uv.x,.16)+vec2(1.0,1.0))*.5).rgb;detail=mix(detail,skin,lid*closed);
                    illumination=max(.74,illumination);
                }
                color=detail*illumination;
                float fog=1.0-exp(-max(.0,eyeDistance-uFogNear)/max(1.0,uFogFar-uFogNear));
                gl_FragColor=vec4(mix(color,uFogColor,fog*.55),1.0);return;
            }
            if(vKind>115.5 && vKind<116.5){
                vec3 viewDir=normalize(vEye100+vec3(0.0,.0001,0.0));
                float fresnel=pow(1.0-clamp(abs(viewDir.y),0.0,1.0),5.0);
                alpha=.28+.66*fresnel;
            }
            if(vKind>49.5 && vKind<50.5)alpha=.27+.12*sin(vPosition.y*5.0-uTime*6.0+vPosition.x*17.0);
            if(vKind>51.5 && vKind<53.5)alpha=uWeather.y*(vKind<52.5?.38:.65);
            if(vKind>53.5 && vKind<54.5)alpha=.35;
            if(vKind>69.5 && vKind<70.5)alpha=.55*uScene.x;
            if(vKind>67.5 && vKind<69.5)alpha=vKind>68.5?.42:.75;
            gl_FragColor = vec4(clamp(color,0.0,1.0),alpha);
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
            gl_Position = vec4(aXY,1.0,1.0);
        }
    """.trimIndent()

    val skyFragmentShader = """
        #ifdef GL_FRAGMENT_PRECISION_HIGH
        precision highp float;
        #else
        precision mediump float;
        #endif
        uniform sampler2D uClouds;
        uniform sampler2D uCloudVolume;
        uniform mediump vec2 uCloudViewport;
        uniform mediump float uCloudPass;
        uniform mediump float uCloudReady;
        uniform mediump vec3 uCloudCamera;
        uniform mediump vec4 uCloudParams;
        uniform mediump vec2 uCloudDrift;
        uniform sampler2D uMoon;
        uniform float uModeledMoons;uniform float uLunarBlood;
        uniform sampler2D uPlanets;
        uniform sampler2D uWeeping;
        uniform mediump float uUniverse;
        uniform mediump float uEclipse;
        uniform mediump vec4 uWeather;
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

        float volumeTexel(vec2 uv) {
            // The packed volume deliberately has no mip chain: adjacent slices
            // must never blend through derivative-selected atlas mip levels.
            return texture2D(uClouds,uv).a;
        }
        float cloudDensity(vec3 p) {
            if(p.y<0.0 || p.y>1.0)return 0.0;
            float slice=clamp(p.y,0.0,1.0)*63.0;
            float lo=floor(slice);float hi=min(lo+1.0,63.0);
            vec2 local=fract(p.xz)*.976+vec2(.012);
            vec2 a=vec2(mod(lo,8.0),floor(lo/8.0));
            vec2 b=vec2(mod(hi,8.0),floor(hi/8.0));
            return mix(volumeTexel((a+local)/8.0),volumeTexel((b+local)/8.0),fract(slice));
        }
        vec4 cloudVolume(vec3 ray,vec3 sunDir,float base,float height) {
            if(abs(ray.y)<.035 || uCloudParams.w<.5)return vec4(0.0);
            float a=(base-uCloudCamera.y)/ray.y;
            float b=(base+height-uCloudCamera.y)/ray.y;
            float start=max(0.0,min(a,b));float end=max(a,b);
            if(end<=start)return vec4(0.0);
            float stepLength=(end-start)/uCloudParams.w;
            vec3 stepDirection=vec3(ray.x*uCloudParams.x,ray.y/height,ray.z*uCloudParams.x)*stepLength;
            vec3 world=uCloudCamera+ray*(start+stepLength*.5);
            vec3 p=vec3(world.x*uCloudParams.x+uCloudDrift.x,(world.y-base)/height,world.z*uCloudParams.x+uCloudDrift.y);
            vec3 lightStep=vec3(sunDir.x*uCloudParams.x*height*.22,sunDir.y*.22,sunDir.z*uCloudParams.x*height*.22);
            float phase=pow(max(dot(ray,sunDir),0.0),8.0);
            vec3 accumulated=vec3(0.0);float transmission=1.0;
            for(int i=0;i<24;i++) {
                if(float(i)>=uCloudParams.w || transmission<.025)break;
                float density=cloudDensity(p);
                if(density>.012) {
                    float above=cloudDensity(p+lightStep);
                    float sunlight=exp2(-above*3.1);
                    vec3 ambient=mix(vec3(.29,.37,.54),vec3(.54,.62,.73),clamp(p.y,0.0,1.0));
                    vec3 light=ambient+vec3(.72,.67,.59)*sunlight+vec3(.16,.10,.045)*phase;
                    float alpha=1.0-exp2(-density*min(stepLength,80.0)*.060);
                    accumulated+=transmission*alpha*light;
                    transmission*=1.0-alpha;
                }
                p+=stepDirection;
            }
            float opacity=1.0-transmission;
            vec3 color=opacity>.001?sqrt(max(accumulated/max(opacity,.001),vec3(0.0))):vec3(0.0);
            float horizon=smoothstep(.10,.32,abs(ray.y));
            color=mix(color,uHorizon,(1.0-horizon)*.60);
            return vec4(color,opacity*horizon);
        }

        vec4 cloudLayer(vec2 uv,vec3 ray,vec3 sunDir,float fullness,float farLayer) {
            vec3 data=texture2D(uClouds,uv).rgb;
            vec2 slope=(data.gb-vec2(.5019608))*2.0;
            vec3 cloudNormal=normalize(vec3(-slope.x*2.1,.64,-slope.y*2.1));
            float sunFace=max(dot(cloudNormal,sunDir),0.0);
            float behind=texture2D(uClouds,uv+sunDir.xz*.022).r;
            float core=smoothstep(.14,.78,data.r);
            float selfShadow=clamp((behind-data.r*.22)*1.3,0.0,1.0);
            float light=clamp(.49+.43*sunFace-.24*selfShadow-.12*core,.10,1.0);
            vec3 low=mix(vec3(.31,.40,.57),vec3(.43,.36,.55),step(9.5,uWorld));
            vec3 high=mix(vec3(1.0,.98,.95),vec3(1.0,.89,.91),step(9.5,uWorld));
            vec3 cloud=mix(low,high,light);
            float alignment=max(dot(ray,sunDir),0.0);
            float edge=smoothstep(.02,.14,data.r)*(1.0-smoothstep(.27,.61,data.r));
            // Transmitted light on thin edges, shaded dense cores: two layers,
            // four lookups total, as in 2.4; no volumetric ray marching.
            cloud+=vec3(.19,.13,.08)*edge*alignment*alignment;
            float opticalDepth=max(data.r-.018,0.0)*(3.2+fullness);
            float opacity=(1.0-exp2(-opticalDepth))*smoothstep(.015,.13,data.r);
            cloud=mix(cloud,uHorizon,farLayer*.20);
            return vec4(cloud,opacity);
        }


        vec3 planetAlbedo(vec3 n,float index,float time) {
            n=normalize(n);
            vec2 uv=vec2(fract(atan(n.z,n.x)/6.2831853+.5+time*.00065),.5-asin(clamp(n.y,-1.0,1.0))/3.1415926);
            vec2 tile=vec2(mod(index,2.0),floor(index*.5));
            vec3 land=texture2D(uPlanets,(tile+vec2(.012)+uv*.976)*.5).rgb;
            float clouds=texture2D(uClouds,uv*2.0+vec2(time*.0005,0.0)).r;
            float veil=smoothstep(.28,.73,clouds)*(.12+.12*step(index,.5));
            return mix(land,vec3(.91,.94,.98),veil);
        }
        // Animated stone face in lunar coordinates: eyelids, tracking irises and a widening grin.
        vec4 livingMoon(vec3 ray,vec3 direction,float radius,vec3 tint,float smile,float blink,vec2 eyeAim) {
            direction=normalize(direction);
            vec3 right=normalize(cross(direction,vec3(0.0,1.0,0.0)));
            vec3 up=cross(right,direction);
            float facing=dot(ray,direction);
            vec2 q=vec2(dot(ray,right),dot(ray,up))/max(.04,facing)/max(radius,.02);
            if(facing<=.001 || length(q)>1.01)return vec4(0.0);
            vec2 sampleQ=q;
            float mouth=exp(-dot((q-vec2(0.0,-.38))/vec2(.65,.23),(q-vec2(0.0,-.38))/vec2(.65,.23)));
            sampleQ.x/=1.0+smile*.19*mouth;
            sampleQ.y+=.055*smile*mouth*(1.0-q.x*q.x);
            for(int i=0;i<2;i++) {
                float side=float(i)*2.0-1.0;
                vec2 eye=q-vec2(side*.35,.19);
                float zone=1.0-smoothstep(.80,1.1,length(eye/vec2(.22,.14)));
                sampleQ-=eyeAim*zone*.6;
            }
            vec4 stone=texture2D(uMoon,vec2(.5+sampleQ.x*.5,.5-sampleQ.y*.5));
            vec3 color=stone.rgb*tint*1.25;
            for(int i=0;i<2;i++) {
                float side=float(i)*2.0-1.0;
                vec2 eye=q-vec2(side*.35,.19);
                float socket=1.0-smoothstep(.84,1.08,length(eye/vec2(.21,.125)));
                vec3 lid=texture2D(uMoon,vec2(.5+q.x*.5,.5-(.36+abs(eye.y)*.25)*.5)).rgb*tint;
                color=mix(color,lid,socket*blink);
                float pupil=exp(-dot((eye-eyeAim)*vec2(24.0,32.0),(eye-eyeAim)*vec2(24.0,32.0)));
                color+=vec3(.08,.19,.34)*pupil*(1.0-blink)*(.3+.7*smile);
            }
            float disk=1.0-smoothstep(.965,.995,length(q));
            return vec4(clamp(color,0.0,1.0),stone.a*disk);
        }
        vec3 lunarReflections(vec3 ray,vec3 sky) {
            if(uModeledMoons>.5)return sky;
            vec4 a=livingMoon(ray,vec3(.08,.50,-.86),.285,mix(vec3(.25,.50,1.0),vec3(.20,.30,1.0),uMoonMood.z),uMoonMood.z,uMoonMood.x,uEyeTarget.xy);
            vec4 b=livingMoon(ray,vec3(-.60,.48,-.63),.105,vec3(.76,.67,.88),uMoonMood.w,uMoonMood.y,uEyeTarget.zw);
            sky=mix(sky,mix(a.rgb,a.rgb*vec3(2.8,.24,.16),uLunarBlood),a.a);sky=mix(sky,b.rgb,b.a);
            if(uGiant.w>.001) {
                vec4 g=livingMoon(ray,uGiantDirection,uGiant.x,vec3(.18,.40,1.15),uGiant.y,uGiant.z,uEyeTarget.xy);
                sky=mix(sky,g.rgb,g.a*uGiant.w);
            }
            return sky;
        }


        vec3 planetarySky(vec3 ray,vec3 background) {
            if((uWorld>11.5 && uWorld<12.5)||(uWorld>18.5 && uWorld<19.5)||(uWorld>48.5 && uWorld<50.5))return background;
            if(uWorld>29.5 && !(uWorld>30.5 && uWorld<31.5))return background;
            for(int i=0;i<3;i++) {
                if(uWorld>30.5 && uWorld<31.5 && i>0)continue;
                float k=float(i);float seed=mod(uUniverse+k*17.0+uWorld*5.0,89.0);
                float phase=seed*.071+uTime*.0009*(k+1.0);
                vec3 direction=normalize(vec3(sin(phase)*.9,.45+.16*k,-1.0));
                float radius=.09+.035*k;
                if(uWorld>16.5 && uWorld<17.5 && i==0){direction=normalize(vec3(.10,.29,-1.0));radius=.52;}
                if(uWorld>20.5 && uWorld<21.5 && i==0){direction=normalize(vec3(0.0,.40,-1.0));radius=.43;}
                if(uWorld>30.5 && uWorld<31.5 && i==0){direction=normalize(vec3(.35,.54,-1.0));radius=.60;}
                vec3 right=normalize(cross(direction,vec3(0.0,1.0,0.0)));vec3 up=cross(right,direction);
                float facing=dot(ray,direction);vec2 q=vec2(dot(ray,right),dot(ray,up))/max(.04,facing)/radius;
                float r=length(q);
                if(facing>.0 && r<1.08){
                    float nz=sqrt(max(.0,1.0-dot(q,q)));vec3 n=normalize(vec3(q,nz));
                    float index=mod(floor(seed),4.0);if((uWorld>16.5 && uWorld<17.5) || (uWorld>20.5 && uWorld<21.5))index=k;
                    if(uWorld>30.5 && uWorld<31.5)index=0.0;
                    vec3 land=planetAlbedo(n,index,uTime);
                    float daylight=max(dot(n,normalize(vec3(-.65,.48,.60))),0.0);
                    vec3 color=land*(.13+.92*daylight);
                    if(i==0 && uEclipse>.001){float cover=(1.0-smoothstep(.68,.70,length(q-vec2(1.5-uEclipse*1.5,.0))))*smoothstep(.0,.25,uEclipse);color=mix(color,vec3(.008,.014,.026),cover);}
                    float rim=pow(1.0-nz,3.0);color+=vec3(.08,.19,.34)*rim;
                    float disk=1.0-smoothstep(.985,1.0,r);
                    background=mix(background,color,disk);
                    float atmosphere=exp(-abs(r-1.0)*65.0)*.35;
                    background+=vec3(.19,.43,.70)*atmosphere;
                }
            }
            return background;
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
            if(uCloudPass>.5){
                bool sea=(uWorld>9.5 && uWorld<10.5)||(uWorld>32.5 && uWorld<33.5)||(uWorld>44.5 && uWorld<45.5)||((uWorld>46.5 && uWorld<47.5)||(uWorld>50.5 && uWorld<51.5));
                vec3 sunDir=normalize(uSun+vec3(0.0,.0001,0.0));
                gl_FragColor=cloudVolume(ray,sunDir,sea && ray.y<0.0?-135.0:85.0,sea && ray.y<0.0?110.0:105.0);
                return;
            }

            if(uWorld>43.5 && uWorld<44.5){
                vec2 p=ray.xz/(max(ray.y,0.0)+.45);float field=texture2D(uClouds,p*.31+vec2(uTime*.0001,.27)).r;
                float opening=0.0;
                for(int i=0;i<3;i++){float k=float(i);vec3 d=normalize(vec3(-.6+k*.6,.85,-.75));float ring=length(ray-d);opening=max(opening,1.0-smoothstep(.19+field*.035,.255+field*.045,ring));}
                vec3 cloud=mix(vec3(.025,.08,.14),vec3(.31,.48,.62),field);
                vec3 light=mix(vec3(.27,.68,.95),vec3(.68,.57,.89),.5+.5*sin(ray.x*9.0));
                vec3 color=mix(cloud,light,opening*.8);color+=vec3(.35,.42,.50)*opening*(1.0-opening)*3.0;
                gl_FragColor=vec4(clamp(color,0.0,1.0),1.0);return;
            }


            if(uWorld>40.5 && uWorld<41.5){
                vec3 color=vec3(.003,.005,.007);
                for(int i=0;i<9;i++){
                    float k=float(i);float az=k*2.399;float altitude=.16+mod(k*1.37,1.0)*.7;
                    vec3 direction=normalize(vec3(sin(az)*cos(altitude),sin(altitude),-cos(az)*cos(altitude)));
                    vec3 right=normalize(cross(direction,vec3(0.0,1.0,0.0)));vec3 up=cross(right,direction);
                    float facing=dot(ray,direction);float size=.22+mod(k*.37,1.0)*.13;
                    vec2 q=vec2(dot(ray,right),dot(ray,up))/max(.03,facing)/vec2(size,size*.61);
                    float blink=.05+.95*smoothstep(.0,.10,abs(mod(uTime*.65+k*1.27,7.5)-.13));q.y/=blink;
                    if(facing>0.0 && abs(q.x)<1.0 && abs(q.y)<1.0){
                        vec2 uv=vec2(mod(k,2.0)*.5+.25+q.x*.245,.225-q.y*.205);
                        vec3 iris=texture2D(uMoon,uv).rgb;
                        float edge=1.0-smoothstep(.78,1.0,max(abs(q.x),abs(q.y)));
                        color=mix(color,iris*.75,edge);
                    }
                }
                gl_FragColor=vec4(color,1.0);return;
            }

            if(((uWorld>11.5 && uWorld<12.5) || ((uWorld>18.5 && uWorld<19.5) || (uWorld>48.5 && uWorld<50.5)))) {
                vec3 color=mix(uHorizon,uTop,smoothstep(-.06,.7,ray.y));
                vec2 stars=ray.xz/(abs(ray.y)+.30)*70.0;
                vec2 cell=floor(stars);
                float seed=fract(sin(dot(cell,vec2(13.13,7.37)))*137.7);
                vec2 local=fract(stars)-.5;
                float star=(1.0-smoothstep(.015,.10,length(local)))*step(.975,seed)*smoothstep(.02,.15,ray.y);
                color+=vec3(.42,.48,.69)*star*(1.0-(uWeather.x>3.5?uWeather.y:0.0))*(.72+.28*sin(uTime*(.4+seed)+seed*60.0));
                vec2 distant=ray.xz/(abs(ray.y)+.30)*112.0;
                float distantSeed=fract(sin(dot(floor(distant),vec2(9.31,17.7)))*171.3);
                float distantStar=(1.0-smoothstep(.008,.055,length(fract(distant)-.5)))*step(.991,distantSeed)*smoothstep(.02,.20,ray.y);
                color+=vec3(.22,.25,.35)*distantStar*(.76+.24*sin(uTime*.31+distantSeed*30.0));
                color=planetarySky(ray,color);
                vec2 uv=ray.xz/(abs(ray.y)+.42)*.42+vec2(uTime*.00013,uTime*.00004);
                vec3 field=texture2D(uClouds,uv).rgb;
                float cloud=smoothstep(.04,.55,field.r)*smoothstep(-.04,.15,ray.y);
                color=mix(color,vec3(.095,.085,.18),cloud*.50);
                vec3 moonDir=normalize(vec3(.08,.50,-.86));
                float halo=pow(max(dot(ray,moonDir),0.0),24.0);
                color+=vec3(.13,.13,.24)*halo;
                color+=comets(ray);
                color=lunarReflections(ray,color);
                gl_FragColor=vec4(clamp(color*(1.0-uEclipse*.55),0.0,1.0),1.0);
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

            color=planetarySky(ray,color);
            // Dos campos sobre una cúpula suave, con escalas y vientos distintos.
            // La proyección es mundial y evita estirar nubes en bandas al horizonte.
            vec2 projected = ray.xz/(max(ray.y,0.0)+.42);
            float giant=(uWorld>34.5 && uWorld<35.5)? .28 : (uWorld>3.5 && uWorld<4.5) || (uWorld>9.5 && uWorld<10.5) ? .46 : 1.0;
            vec2 wind = uCloudDrift;
            vec2 otherWind=vec2(-uCloudDrift.y,uCloudDrift.x);
            float visible = smoothstep(-.025,.14,ray.y);
            float skyStrength = 1.0;
            if((uWorld>16.5 && uWorld<17.5) || (uWorld>20.5 && uWorld<21.5))skyStrength=.25;
            if(uWorld>30.5 && uWorld<31.5)skyStrength=.04;
            if(uWorld>29.5 && uWorld<30.5)skyStrength=.10;
            if(uWorld>38.5 && uWorld<39.5)skyStrength=0.0;
            if (uWorld > 5.5 && uWorld < 7.5) skyStrength = .60;
            if(uCloudParams.w>.5){
                vec4 volume=uCloudReady>.5?(ray.y>0.0?texture2D(uCloudVolume,gl_FragCoord.xy/uCloudViewport):vec4(0.0)):cloudVolume(ray,sunDir,85.0,105.0);
                color=mix(color,volume.rgb,volume.a*skyStrength);
            } else {
            vec4 upper = cloudLayer(projected*.23*giant+vec2(.41,.23)+otherWind,
                ray,sunDir,.10,1.0);
            vec4 lower = cloudLayer(projected*.42*giant+vec2(.08,.51)+wind,
                ray,sunDir,.20,0.0);
                color = mix(color,upper.rgb,upper.a*visible*.68*skyStrength);
                color = mix(color,lower.rgb,lower.a*visible*.94*skyStrength);
            }
            bool night=(uWorld>2.5 && uWorld<3.5) || (uWorld>4.5 && uWorld<5.5) ||
                (uWorld>6.5 && uWorld<7.5) || (uWorld>10.5 && uWorld<11.5);
            if(night || (uWorld>16.5 && uWorld<29.5) || (uWorld>30.5 && uWorld<31.5)) {

                color+=comets(ray)*.65;
                // Sparse, independently twinkling stars, with slow gentle changes.
                vec2 stellar=ray.xz/(abs(ray.y)+.31)*62.0;
                vec2 cell=floor(stellar);vec2 local=fract(stellar)-.5;
                float seed=fract(sin(dot(cell,vec2(13.13,7.37)))*137.7);
                float star=(1.0-smoothstep(.015,.10,length(local)))*step(.974,seed)*smoothstep(.02,.18,ray.y);
                if(night)color=mix(color,mix(uHorizon,uTop,altitude),.55);
                color+=vec3(.46,.52,.67)*star*(1.0-(uWeather.x>3.5?uWeather.y:0.0))*(.65+.35*sin(uTime*(.32+seed*.7)+seed*50.0));

            }
            // La lejanía pierde contraste y evita una línea recortada en el horizonte.
            float horizonHaze = 1.0-smoothstep(.015,.19,abs(ray.y));
            color = mix(color,uHorizon,horizonHaze*.62);

            if (((uWorld > 9.5 && uWorld < 10.5)||(uWorld>32.5 && uWorld<33.5)||(uWorld>44.5 && uWorld<45.5)||((uWorld>46.5 && uWorld<47.5)||(uWorld>50.5 && uWorld<51.5))) && ray.y < .04) {
                // Mar de nubes bajo el hotel: no es un plano sólido ni una colisión.
                vec2 downUV = ray.xz/(max(-ray.y,0.0)+.28)*.36+vec2(.34,.17)+wind;
                float seaVisible = 1.0-smoothstep(-.07,.04,ray.y);
                if(uCloudParams.w>.5){
                    vec4 volume=uCloudReady>.5?texture2D(uCloudVolume,gl_FragCoord.xy/uCloudViewport):cloudVolume(ray,sunDir,-135.0,110.0);
                    vec3 deep=mix(uHorizon,vec3(.31,.43,.64),clamp(-ray.y,0.0,1.0));
                    color=mix(color,mix(deep,volume.rgb,volume.a),seaVisible);
                } else {
                vec4 sea = cloudLayer(downUV,vec3(ray.x,abs(ray.y),ray.z),sunDir,.65,0.0);
                vec4 seaFar = cloudLayer(downUV*.57+vec2(.23,.39),
                    vec3(ray.x,abs(ray.y),ray.z),sunDir,.5,1.0);
                vec3 seaColor = mix(seaFar.rgb*vec3(.80,.81,.91),sea.rgb,.12+.88*sea.a);
                seaColor = mix(seaColor,vec3(.91,.84,.91),.08);
                    color=mix(color,seaColor,seaVisible);
                }
            }
            gl_FragColor = vec4(clamp(color*(1.0-uEclipse*.55),0.0,1.0),1.0);
        }
    """.trimIndent()
    val shadowVertexShader = """
        attribute vec3 aColor;
        varying mediump vec3 vLeafUV;
        uniform mat4 uLightVP;
        uniform vec3 uEntity;
        attribute vec3 aPos;
        attribute float aKind;
        varying mediump float vKind;
        void main(){vKind=aKind;vLeafUV=aColor;gl_Position=uLightVP*vec4(aPos,1.0);}
    """.trimIndent()
    val shadowFragmentShader = """
        uniform sampler2D uAtlas;
        varying mediump vec3 vLeafUV;
        #ifdef GL_FRAGMENT_PRECISION_HIGH
        precision highp float;
        #else
        precision mediump float;
        #endif
        varying mediump float vKind;
        void main(){
            if((vKind>.5 && vKind<3.5) || (vKind>19.5 && vKind<20.5) || (vKind>27.5 && vKind<32.5) || (vKind>49.5 && vKind<110.5) || vKind>115.5)discard;
            if(vKind>110.5 && vKind<111.5){
                float i=20.0+floor(vLeafUV.z+.5);
                vec2 cell=vec2(mod(i,4.0),floor(i/4.0));
                if(texture2D(uAtlas,(cell+vec2(.018)+vLeafUV.xy*.964)/vec2(4.0,8.0)).a<.40)discard;
            }
            vec2 encodedDepth=fract(gl_FragCoord.z*vec2(1.0,255.0));
            encodedDepth.x-=encodedDepth.y/255.0;
            gl_FragColor=vec4(encodedDepth,0.0,1.0);
        }
    """.trimIndent()
}
