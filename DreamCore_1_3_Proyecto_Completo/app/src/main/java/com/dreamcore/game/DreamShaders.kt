package com.dreamcore.game

/**
 * Materiales 1.3 para OpenGL ES 2.0. No requiere HDR, extensiones ni texturas nuevas.
 * La iluminación y la oclusión del abismo son aproximaciones artísticas; no shadow maps.
 * Siete vectores varying como máximo (el mínimo de GLES2 es ocho).
 */
object DreamShaders {
    val vertexShader = """
        uniform mat4 uVP;
        uniform highp vec3 uCamera;
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
        void main() {
            vec3 p = aPos;
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
            if (crater && aKind > 13.5 && aKind < 14.5 && abs(n.y) < .45) {
                // La pared cilíndrica ilumina su cara interior, visible desde el pozo.
                vec2 radial = p.xz - vec2(0.0,-102.0);
                if (dot(n.xz, radial) > 0.0) n.xz = -n.xz;
            }
            float sun = max(dot(n, normalize(uSun)), 0.0);
            float hemisphere = .5 + .5 * max(n.y,-.5);
            if (crater) {
                float depth = smoothstep(4.0,178.0,-p.y);
                vec3 ambient = mix(vec3(.48,.54,.66),vec3(.79,.80,.86),hemisphere);
                vec3 sunlight = vec3(1.0,.85,.70) * (.35 * sun);
                vec3 light = mix(ambient + sunlight,
                    vec3(.65,.73,.86) + sunlight*.27, depth);
                vColor = aColor * light;
            } else {
                // Conserva la dirección artística y el brillo de los otros siete mapas.
                vColor = aColor * (.64 + .30*sun + .095*hemisphere);
                if (uWorld > 5.5) vColor *= .84 + .14*hemisphere;
            }
            if (uAnomaly > .01 && aKind < 19.5) {
                vColor = mix(vColor,vec3(vColor.b*.85,vColor.r*.82,vColor.g),uAnomaly*.14);
            }
            if (aKind > 19.5 && aKind < 20.5) {
                float fluffy = .79 + .18*max(n.y,0.0) + .06*sun;
                vColor = aColor * fluffy;
                p.x += .25 * sin(uTime*.11 + aPos.z*.017);
            }
            vPosition = p;
            vNormal = n;
            // Escalar antes de interpolar impide overflow de length() en mediump.
            // 300 metros al cuadrado supera el rango mínimo de GLES2 mediump.
            vEye100 = (uCamera - p) * .01;
            vKind = aKind;
            float scale = crater ? .30 : .45;
            if (abs(n.y) > .66) vTexCoord = p.xz * scale;
            else if (abs(n.x) > abs(n.z)) vTexCoord = p.zy * scale;
            else vTexCoord = p.xy * scale;
            float dist = distance(p.xz,uCamera.xz);
            float fog = clamp((dist-uFogNear)/max(uFogFar-uFogNear,1.0),0.0,1.0);
            vFog = (aKind > 19.5 && aKind < 20.5)
                ? clamp(fog*.37,0.0,.34) : clamp(fog,0.0,.92);
            gl_Position = uVP * vec4(p,1.0);
        }
    """.trimIndent()

    val fragmentShader = """
        precision mediump float;
        uniform sampler2D uAtlas;
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
        void main() {
            bool crater = uWorld > 1.5 && uWorld < 2.5;
            vec3 color = vColor;
            float eyeDistance = length(vEye100) * 100.0;
            vec3 normal = normalize(vNormal);
            if (vKind > 3.5 && vKind < 19.5) {
                float material = floor(vKind-4.0+.5);
                vec2 tile = vec2(mod(material,4.0),floor(material/4.0));
                vec2 atlasUV = (tile+vec2(.055)+fract(vTexCoord)*.89)/4.0;
                vec3 surface = texture2D(uAtlas,atlasUV).rgb;
                float textureStrength = .72;
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
            if (vKind > .5 && vKind < 1.5) {
                if (crater) {
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
                    vec3 baseWater = mix(color,vec3(.24,.49,.56),.48);
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
            if (crater) {
                // Se despeja al acercarse a cualquier piso, incluso a 176 m de profundidad.
                // Antes el término de altura ocultaba el fondo también estando allí.
                float fog = smoothstep(uFogNear,max(uFogNear+1.0,uFogFar),eyeDistance);
                fog = min(fog*.73,.73);
                if (vKind > 19.5 && vKind < 20.5) fog *= .45;
                if (vKind > 23.5 && vKind < 24.5) fog *= .68;
                float depth = smoothstep(8.0,178.0,-vPosition.y);
                vec3 haze = mix(uFogColor,vec3(.44,.57,.73),depth*.38);
                color = mix(color,haze,fog);
                // Curva suave para blancos de porcelana; no HDR ni autoexposición.
                color = color*(1.025-.025*color);
            } else {
                color = mix(color,uFogColor,vFog*.89);
            }
            gl_FragColor = vec4(clamp(color,0.0,1.0),1.0);
        }
    """.trimIndent()

    val skyVertexShader = """
        attribute vec2 aXY;
        varying mediump vec2 vUV;
        void main() {
            vUV = aXY*.5+.5;
            gl_Position = vec4(aXY,.999,1.0);
        }
    """.trimIndent()

    val skyFragmentShader = """
        precision mediump float;
        uniform mediump vec3 uTop;
        uniform mediump vec3 uHorizon;
        uniform mediump float uWorld;
        uniform mediump float uTime;
        varying mediump vec2 vUV;
        void main() {
            float t = smoothstep(.06,.92,vUV.y);
            vec3 c = mix(uHorizon,uTop,t);
            float haze = exp(-pow((vUV.y-.36)*5.0,2.0))*.09;
            c += vec3(haze);
            if (uWorld > 1.5 && uWorld < 2.5) {
                // Atardecer lavanda: sirve de contrapunto a las profundidades azules.
                vec3 dreamSky = mix(vec3(.93,.78,.76),vec3(.49,.61,.82),t);
                c = mix(c,dreamSky,.62);
                vec2 delta = (vUV-vec2(.74,.72))*vec2(1.7,1.0);
                float d = length(delta);
                float halo = 1.0-smoothstep(.025,.32,d);
                c += vec3(.14,.09,.045)*halo*halo;
                float disc = 1.0-smoothstep(.012,.018,d);
                c = mix(c,vec3(1.0,.94,.79),disc*.78);
                float highCloud = sin(vUV.x*11.0+vUV.y*7.0+uTime*.006) *
                    sin(vUV.x*6.0-vUV.y*15.0);
                float veil = smoothstep(.45,.78,highCloud)*smoothstep(.36,.85,vUV.y);
                c = mix(c,vec3(.91,.83,.91),veil*.13);
            } else {
                if (uWorld < .5) {
                    float aureole = 1.0-smoothstep(.07,.39,
                        length((vUV-vec2(.57,.72))*vec2(1.65,1.0)));
                    c += vec3(.12,.082,.095)*aureole;
                    float roseBand = exp(-pow((vUV.y-.34)*7.0,2.0));
                    c += vec3(.065,.032,.046)*roseBand;
                }
                vec2 sunPos = uWorld > 2.5 ? vec2(.42,.78) : vec2(.68,.72);
                float d = length((vUV-sunPos)*vec2(1.7,1.0));
                float glow = .14*(1.0-smoothstep(.06,.48,d));
                c += (uWorld > 2.5 ? vec3(.74,.49,.38) : vec3(.55,.43,.30))*glow;
                if (uWorld > 2.5 && uWorld < 3.5) {
                    float veil = sin(vUV.x*130.0+uTime*.14)*sin(vUV.x*58.0-uTime*.09);
                    float density = 1.0-smoothstep(.12,.74,vUV.y);
                    c += vec3(.018,.022,.032)*max(veil,0.0)*density;
                }
                if (uWorld > 3.5 && uWorld < 4.5) {
                    float cloudGlow = exp(-pow((vUV.y-.30)*5.2,2.0));
                    c += vec3(.05,.018,.040)*cloudGlow;
                }
                if (uWorld < .5 || (uWorld > 3.5 && uWorld < 4.5)) {
                    vec2 q = vUV*vec2(7.5,5.5);
                    float a = sin(q.x*1.9+sin(q.y*1.35+uTime*.009)*.70);
                    float b = cos(q.x*1.1-q.y*2.2+sin(q.x*1.4)*.5);
                    float cNoise = .52*a+.31*b+.17*sin(q.x*3.7+q.y*2.8);
                    float cloud = smoothstep(.34,.70,cNoise)*(.55+.45*smoothstep(.23,.65,vUV.y));
                    vec3 tint = uWorld < .5 ? vec3(.99,.91,.96) : vec3(.98,.83,.87);
                    c = mix(c,tint,cloud*.21);
                    float ribbons = sin(q.x*.72+q.y*1.6-uTime*.003);
                    c += vec3(.027,.020,.030)*max(ribbons,0.0)*smoothstep(.18,.60,vUV.y);
                }
                if (uWorld > 4.5) {
                    float tidalLight = sin(vUV.x*42.0+uTime*.055)*.5+.5;
                    c += vec3(.035,.042,.060)*tidalLight*(1.0-smoothstep(.25,.8,vUV.y));
                }
            }
            gl_FragColor = vec4(clamp(c,0.0,1.0),1.0);
        }
    """.trimIndent()
}
