package com.dreamcore.game

/**
 * Materiales 1.4 y cielo orientado al mundo para OpenGL ES 2.0, sin extensiones ni HDR.
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
                if (uWorld > 5.5 && uWorld < 7.5) vColor *= .84 + .14*hemisphere;
                if (uWorld > 7.5) {
                    vec3 ambient = mix(vec3(.52,.56,.67),vec3(.82,.83,.87),hemisphere);
                    vColor = aColor*(ambient+vec3(.24,.21,.18)*sun);
                }
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
        uniform sampler2D uClouds;
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
        vec3 environmentColor(vec3 ray) {
            float elevation = clamp(ray.y*.65+.30,0.0,1.0);
            vec3 horizon = vec3(.83,.76,.82);
            vec3 upper = vec3(.40,.59,.78);
            if (uWorld > 9.5) {
                horizon = vec3(.95,.70,.78);
                upper = vec3(.44,.46,.71);
            }
            vec3 sky = mix(horizon,upper,elevation);
            vec2 uv = ray.xz/(abs(ray.y)+.42)*.42 + vec2(uTime*.00021,uTime*.00009);
            vec3 field = texture2D(uClouds,uv).rgb;
            float density = smoothstep(.025,.50,field.r);
            float shade = clamp(.80+(field.g-.50196)*.35-(field.b-.50196)*.28,0.0,1.0);
            vec3 cloud = mix(vec3(.63,.64,.75),vec3(.98,.95,.95),shade);
            float fade = smoothstep(.0,.16,abs(ray.y));
            return mix(sky,cloud,density*fade*.88);
        }
        void main() {
            // El cielo sustituye las antiguas nubes de elipsoides sólidos.
            if (vKind > 19.5 && vKind < 20.5) discard;
            bool crater = uWorld > 1.5 && uWorld < 2.5;
            vec3 color = vColor;
            float eyeDistance = length(vEye100) * 100.0;
            vec3 normal = normalize(vNormal);
            if (vKind > 3.5 && vKind < 19.5) {
                float material = floor(vKind-4.0+.5);
                vec2 tile = vec2(mod(material,4.0),floor(material/4.0));
                vec2 atlasUV = (tile+vec2(.055)+fract(vTexCoord)*.89)/4.0;
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
            } else if (uWorld > 7.5) {
                float fog = smoothstep(uFogNear,max(uFogNear+1.0,uFogFar),eyeDistance);
                color = mix(color,uFogColor,fog*.72);
            } else {
                color = mix(color,uFogColor,vFog*.89);
            }
            gl_FragColor = vec4(clamp(color,0.0,1.0),1.0);
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
        precision mediump float;
        uniform sampler2D uClouds;
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

        void main() {
            vec3 ray = normalize(vRay);
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
            vec2 wind = vec2(uTime*.00021,uTime*.00009);
            vec4 upper = cloudLayer(projected*.23+vec2(.41,.23)+wind*.65,
                ray,sunDir,.10,1.0);
            vec4 lower = cloudLayer(projected*.42+vec2(.08,.51)+wind,
                ray,sunDir,.20,0.0);
            float visible = smoothstep(-.025,.14,ray.y);
            float skyStrength = 1.0;
            if (uWorld > 5.5 && uWorld < 7.5) skyStrength = .60;
            color = mix(color,upper.rgb,upper.a*visible*.68*skyStrength);
            color = mix(color,lower.rgb,lower.a*visible*.94*skyStrength);
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
