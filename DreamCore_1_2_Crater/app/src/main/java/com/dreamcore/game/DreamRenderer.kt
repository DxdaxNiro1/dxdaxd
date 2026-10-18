package com.dreamcore.game

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/** Dream Core 1.1: ocho escenas conectadas, anomalías y zonas de audio. */
class DreamRenderer(
    private val context: Context,
    private val onWorldChanged: (String) -> Unit,
    private val onZoneChanged: (String) -> Unit,
    private val onDreamEvent: (String) -> Unit
) : GLSurfaceView.Renderer {
    private val player = PlayerState()

    @Volatile var moveX = 0f
    @Volatile var moveY = 0f
    @Volatile var sprinting = false
    @Volatile var jumpQueued = false
    @Volatile var nearPortal = false
        private set
    @Volatile var currentWorld = DreamWorlds.STATION
        private set

    private var pitch = 0f
    private var walkVelocityX = 0f
    private var walkVelocityY = 0f
    private var walkTime = 0f
    private var jumpOffset = 0f
    private var jumpSpeed = 0f
    private var followY = 1.65f
    private var lastFrameNanos = 0L
    private var elapsed = 0f
    private var switchCooldown = 0f
    private var portalTarget: String? = null
    private var currentZone = "CALMA"
    private var anomalyTimer = 0f
    private var anomalySeen = false
    private var anomalyPower = 0f

    private val projection = FloatArray(16)
    private val view = FloatArray(16)
    private val vp = FloatArray(16)
    // 0.6: iluminación por hemisferios, atlas de materiales, niebla volumétrica
    // aproximada por altura y agua animada. GLES 2.0 (no requiere GLES 3).
    private val vertexShader = """
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
        varying mediump vec2 vTexCoord;
        varying mediump float vFog;
        varying mediump float vKind;
        void main() {
            vec3 p = aPos;
            // Solo los objetos de umbral respiran cuando algo imposible sucede.
            if (aKind > 21.5 && aKind < 22.5) {
                p.x += .12 * uAnomaly * sin(uTime * 2.1 + aPos.y * 1.8);
            }
            if (aKind > 0.5 && aKind < 1.5) {
                p.y += 0.055 * sin(p.x * 1.4 + uTime * 0.85) *
                       cos(p.z * 1.9 - uTime * 1.08);
            }
            // Fragmentos exclusivos del cráter: desplazamiento digital a intervalos.
            if (aKind > 22.5 && aKind < 23.5) {
                float tick = floor(uTime * 7.0);
                float pulse = step(.80, fract(sin(tick*12.9898 + floor(aPos.y)*4.17)*43758.5453));
                p.x += pulse * .55;
                p.z += pulse * .18;
            }
            vec3 n = normalize(aNormal);
            // Los triángulos del suelo son visibles desde ambos lados.
            if (aKind >= 4.0 && n.y < -0.68) n.y = -n.y;
            float sun = max(dot(n, normalize(uSun)), 0.0);
            float hemisphere = 0.5 + 0.5 * max(n.y, -0.5);
            float lighting = 0.64 + 0.30 * sun + 0.095 * hemisphere;
            // Las profundidades del abismo son más frías y apagadas.
            float darkness = (uWorld > 1.5 && uWorld < 2.5) ? clamp((-p.y - 13.0) / 100.0, 0.0, .25) : 0.0;
            vColor = aColor * (lighting - darkness);
            if (uWorld > 5.5) vColor *= .84 + .14 * hemisphere;
            if (uAnomaly > .01 && aKind < 19.5) {
                vColor = mix(vColor, vec3(vColor.b*.85,vColor.r*.82,vColor.g), uAnomaly*.14);
            }
            // Nubes blancas con sombra propia y brillo en su parte superior.
            if (aKind > 19.5 && aKind < 20.5) {
                float fluffy = .79 + .18 * max(n.y,0.0) + .06 * max(dot(n,normalize(uSun)),0.0);
                vColor = aColor * fluffy;
                p.x += .25 * sin(uTime * .11 + aPos.z * .017);
            }
            vPosition = p;
            vKind = aKind;
            // Proyección de texturas según la cara. No modifica el VBO existente.
            if (abs(n.y) > 0.66) vTexCoord = p.xz * 0.45;
            else if (abs(n.x) > abs(n.z)) vTexCoord = p.zy * 0.45;
            else vTexCoord = p.xy * 0.45;
            float dist = distance(p.xz, uCamera.xz);
            float fog = clamp((dist - uFogNear) /
                         max(uFogFar - uFogNear, 1.0), 0.0, 1.0);
            float deepFog = (uWorld > 1.5 && uWorld < 2.5) ?
                clamp((-p.y - 16.0) / 80.0, 0.0, .36) : 0.0;
            // Que las nubes se vean grandes incluso a más de cien metros.
            vFog = (aKind > 19.5 && aKind < 20.5)
                ? clamp(fog * .37, 0.0, .34) : clamp(fog + deepFog, 0.0, .92);
            gl_Position = uVP * vec4(p,1.0);
        }
    """.trimIndent()

    private val fragmentShader = """
        precision mediump float;
        uniform sampler2D uAtlas;
        uniform mediump float uWorld;
        uniform mediump vec3 uFogColor;
        uniform mediump float uTime;
        uniform mediump float uAnomaly;
        varying mediump vec3 vColor;
        varying mediump vec3 vPosition;
        varying mediump vec2 vTexCoord;
        varying mediump float vFog;
        varying mediump float vKind;
        void main() {
            vec3 color = vColor;
            if (vKind > 3.5 && vKind < 19.5) {
                // PNG atlas de 4 columnas y 4 filas, repetido sin bleeding.
                float material = floor(vKind - 4.0 + 0.5);
                float tileX = mod(material,4.0);
                float tileY = floor(material / 4.0);
                vec2 repeated = fract(vTexCoord);
                vec2 atlasUV = (vec2(tileX,tileY) + vec2(.055) + repeated * .89) / 4.0;
                vec3 surface = texture2D(uAtlas, atlasUV).rgb;
                color *= mix(vec3(1.0),surface,(uWorld > 1.5 && uWorld < 2.5) ? 0.48 : 0.72);
            }
            if (vKind > 22.5 && vKind < 23.5) {
                float band = step(.70, fract(vPosition.y*5.0-uTime*2.0));
                color = mix(color, vec3(.50,.94,.95), band*.40);
            }
            // Ventanas imposibles: no son portales reales, sino otro cielo.
            if (vKind > 20.5 && vKind < 21.5) {
                float vert = fract(vPosition.y * .105 + .12);
                float breath = .5 + .5 * sin(uTime * .52 + vPosition.z * .14);
                vec3 twilight = mix(vec3(.18,.29,.48),vec3(.68,.76,.94),vert);
                color = mix(twilight,vec3(.80,.77,.91),breath*.20);
                color += vec3(.18,.06,.17)*uAnomaly*(.4+.6*breath);
            }
            if (vKind > 19.5 && vKind < 20.5) {
                // Variación en las superficies grandes: algodón irreal.
                float pattern = sin(vPosition.x*.10 + vPosition.z*.075) *
                                cos(vPosition.y*.19 - vPosition.z*.055);
                color *= .965 + .035 * pattern;
            }
            if (vKind > 21.5 && vKind < 22.5) {
                float breath = .5+.5*sin(uTime*1.65+vPosition.y*1.8);
                color = mix(vec3(.32,.43,.53),vec3(.91,.81,.98), .17+uAnomaly*.75*breath);
            }
            if (vKind > .5 && vKind < 1.5) {
                // Ondas cruzadas y destellos suaves sobre agua clara u oscura.
                float r1 = sin(vPosition.x * 2.7 + uTime * 1.15 +
                               sin(vPosition.z * 1.20) * 1.5);
                float r2 = cos(vPosition.z * 3.0 - uTime * .93 +
                               sin(vPosition.x * .81));
                float wave = r1 * r2;
                float shine = pow(max(.0, .5 + .5*wave), 6.0);
                color += vec3(.045,.075,.10) * wave + vec3(.12,.16,.18) * shine;
            }
            if (vKind > 2.5 && vKind < 3.5) {
                float pulse = .50 + .50 * sin(uTime * 1.65 + vPosition.y * 2.0);
                color = mix(vec3(.20,.48,.52),vec3(.72,.96,.90),pulse);
            } else if (vKind > 1.5 && vKind < 2.5) {
                float pulse = .50 + .50 * sin(uTime * 2.2 + vPosition.y * 2.4);
                color = mix(vec3(.27,.22,.49),vec3(.72,.56,.91),pulse);
            }
            color = mix(color,uFogColor,vFog*.89);
            // Curva suave de color, sin depender de HDR ni posprocesado costoso.
            color = clamp(color,0.0,1.0);
            gl_FragColor = vec4(color,1.0);
        }
    """.trimIndent()

    private val skyVertexShader = """
        attribute vec2 aXY;
        varying mediump vec2 vUV;
        void main() {
            vUV = aXY * 0.5 + 0.5;
            gl_Position = vec4(aXY,0.999,1.0);
        }
    """.trimIndent()
    private val skyFragmentShader = """
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
            if (uWorld < .5) {
                // Cielo dulce con bandas casi imperceptibles de luz.
                float aureole = 1.0 - smoothstep(.07,.39,
                    length((vUV-vec2(.57,.72))*vec2(1.65,1.0)));
                c += vec3(.12,.082,.095) * aureole;
                float roseBand = exp(-pow((vUV.y-.34)*7.0,2.0));
                c += vec3(.065,.032,.046) * roseBand;
            }
            // Resplandor brumoso y discreto en el cielo, distinto por mundo.
            vec2 sunPos = uWorld > 2.5 ? vec2(.42,.78) :
                          (uWorld > 1.5 ? vec2(.78,.70) : vec2(.68,.72));
            float d = length((vUV-sunPos) * vec2(1.7,1.0));
            float glow = .14 * (1.0-smoothstep(.06,.48,d));
            c += (uWorld > 2.5 ? vec3(.74,.49,.38) :
                  (uWorld > 1.5 ? vec3(.24,.31,.44) : vec3(.55,.43,.30)))*glow;
            // En el jardin, un fino velo luminoso parece lluvia suspendida.
            if (uWorld > 2.5 && uWorld < 3.5) {
                float veil = sin(vUV.x * 130.0 + uTime * .14) *
                             sin(vUV.x * 58.0 - uTime * .09);
                float density = 1.0 - smoothstep(.12,.74,vUV.y);
                c += vec3(.018,.022,.032) * max(veil,0.0) * density;
            }
            if (uWorld > 3.5 && uWorld < 4.5) {
                float cloudGlow = exp(-pow((vUV.y-.30)*5.2,2.0));
                c += vec3(.05,.018,.040)*cloudGlow;
            }
            // Nubes planas muy lejanas, detrás de los volúmenes 3D.
            // Pocas operaciones para mantener GLES2 compatible.
            if (uWorld < .5 || (uWorld > 3.5 && uWorld < 4.5)) {
                vec2 q=vUV*vec2(7.5,5.5);
                float a=sin(q.x*1.9+sin(q.y*1.35+uTime*.009)*.70);
                float b=cos(q.x*1.1-q.y*2.2+sin(q.x*1.4)*.5);
                float cNoise=.52*a+.31*b+.17*sin(q.x*3.7+q.y*2.8);
                float cloud=smoothstep(.34,.70,cNoise)*(.55+.45*smoothstep(.23,.65,vUV.y));
                vec3 tint=uWorld<.5?vec3(.99,.91,.96):vec3(.98,.83,.87);
                c=mix(c,tint,cloud*.21);
                float ribbons=sin(q.x*.72+q.y*1.6-uTime*.003);
                c+=vec3(.027,.020,.030)*max(ribbons,0.0)*smoothstep(.18,.60,vUV.y);
            }
            if (uWorld > 4.5) {
                float tidalLight = sin(vUV.x*42.0 + uTime*.055)*.5 + .5;
                c += vec3(.035,.042,.060)*tidalLight*(1.0-smoothstep(.25,.8,vUV.y));
            }
            gl_FragColor = vec4(clamp(c,0.0,1.0),1.0);
        }
    """.trimIndent()

    private val skyBuffer = ByteBuffer.allocateDirect(12 * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(floatArrayOf(-1f,-1f, 1f,-1f, -1f,1f, -1f,1f, 1f,-1f, 1f,1f))
            position(0)
        }

    private var sceneProgram = 0
    private var skyProgram = 0
    private var atlasTexture = 0
    private var sceneBufferStation = 0
    private var sceneBufferPools = 0
    private var sceneBufferCrater = 0
    private var sceneBufferGarden = 0
    private var sceneBufferMeadow = 0
    private var sceneBufferAquarium = 0
    private var sceneBufferHouseMeadow = 0
    private var sceneBufferHouseCrater = 0
    private var vertexCountStation = 0
    private var vertexCountPools = 0
    private var vertexCountCrater = 0
    private var vertexCountGarden = 0
    private var vertexCountMeadow = 0
    private var vertexCountAquarium = 0
    private var vertexCountHouseMeadow = 0
    private var vertexCountHouseCrater = 0
    private var width = 1
    private var height = 1

    // Se llama exclusivamente en el hilo OpenGL mediante GLSurfaceView.queueEvent.
    fun turnCamera(dx: Float, dy: Float) {
        player.yaw += dx * 0.004f
        pitch = (pitch - dy * 0.004f).coerceIn(-1.22f,1.22f)
    }

    // Las decisiones de portal viven en el motor, no en la pantalla.
    // Más adelante los eventos de red podrán pedir estos mismos cambios.
    fun interact() {
        if (!nearPortal || switchCooldown > 0f) return
        val target = portalTarget ?: return
        player.worldId = target
        when (target) {
            DreamWorlds.STATION -> {
                // Desde cada mundo reaparece junto a su puerta correspondiente.
                when (currentWorld) {
                    DreamWorlds.CRATER -> { player.x = -9.2f; player.z = -14.0f }
                    DreamWorlds.GARDEN -> { player.x = 9.2f; player.z = -14.0f }
                    DreamWorlds.MEADOW -> { player.x = -18f; player.z = -1.5f }
                    else -> { player.x = 0f; player.z = -17.25f }
                }
                player.yaw = 0f
            }
            DreamWorlds.POOLS -> {
                if (currentWorld == DreamWorlds.AQUARIUM) {
                    player.x = 9f; player.z = 2.7f
                } else { player.x = 0f; player.z = 2.2f }
                player.yaw = 0f
            }
            DreamWorlds.CRATER -> {
                if (currentWorld == DreamWorlds.HOUSE_CRATER) {
                    player.x = -24f; player.z = 15f
                } else {
                    player.x = 0f; player.z = 26f
                }
                player.yaw = 0f
            }
            DreamWorlds.GARDEN -> {
                player.x = 0f; player.z = 26f; player.yaw = 0f
            }
            DreamWorlds.MEADOW -> {
                if (currentWorld == DreamWorlds.AQUARIUM) {
                    player.x = 0f; player.z = -156f; player.yaw = 0f
                } else if (currentWorld == DreamWorlds.HOUSE_MEADOW) {
                    player.x = 0f; player.z = -129.7f; player.yaw = 0f
                } else { player.x = 0f; player.z = 29f; player.yaw = 0f }
            }
            DreamWorlds.AQUARIUM -> {
                player.x = 0f; player.z = 25f; player.yaw = 0f
            }
            DreamWorlds.HOUSE_MEADOW, DreamWorlds.HOUSE_CRATER -> {
                player.x = 0f; player.z = 3.4f; player.yaw = 0f
            }
        }
        val floor = when (target) {
            DreamWorlds.STATION -> DreamWorlds.groundHeight(player.x,player.z)
            DreamWorlds.CRATER -> DreamWorlds.craterHeight(player.x,player.z)
            DreamWorlds.MEADOW -> DreamWorlds.meadowHeight(player.x,player.z)
            else -> 0f
        }
        followY = 1.65f + floor
        jumpSpeed = 0f; jumpOffset = 0f; jumpQueued = false; sprinting = false
        player.y = followY
        pitch = 0f
        walkVelocityX = 0f; walkVelocityY = 0f
        moveX = 0f; moveY = 0f
        switchCooldown = 1.4f
        anomalyTimer = 0f; anomalyPower = 0f; anomalySeen = false
        currentZone = if (target == DreamWorlds.HOUSE_MEADOW || target == DreamWorlds.HOUSE_CRATER) "CASA" else "CALMA"
        onZoneChanged(currentZone)
        nearPortal = false
        portalTarget = null
        currentWorld = player.worldId
        onWorldChanged(currentWorld)
    }

    private fun shader(type: Int, source: String): Int {
        val id = GLES20.glCreateShader(type)
        GLES20.glShaderSource(id, source)
        GLES20.glCompileShader(id)
        val status = IntArray(1)
        GLES20.glGetShaderiv(id,GLES20.GL_COMPILE_STATUS,status,0)
        if (status[0] == 0) {
            val error = GLES20.glGetShaderInfoLog(id)
            GLES20.glDeleteShader(id)
            error("Error del shader: $error")
        }
        return id
    }

    private fun program(v: String, f: String): Int {
        val vs = shader(GLES20.GL_VERTEX_SHADER,v)
        val fs = shader(GLES20.GL_FRAGMENT_SHADER,f)
        val p = GLES20.glCreateProgram()
        GLES20.glAttachShader(p,vs)
        GLES20.glAttachShader(p,fs)
        GLES20.glLinkProgram(p)
        val status = IntArray(1)
        GLES20.glGetProgramiv(p,GLES20.GL_LINK_STATUS,status,0)
        GLES20.glDeleteShader(vs)
        GLES20.glDeleteShader(fs)
        if (status[0] == 0) {
            val log = GLES20.glGetProgramInfoLog(p)
            GLES20.glDeleteProgram(p)
            error("Error enlazando shaders: $log")
        }
        return p
    }

    private fun uploadScene(scene: DreamWorlds.Scene): Int {
        val ids = IntArray(1)
        GLES20.glGenBuffers(1,ids,0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,ids[0])
        val buffer=ByteBuffer.allocateDirect(scene.vertices.size*4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(scene.vertices); position(0)
            }
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,scene.vertices.size*4,buffer,GLES20.GL_STATIC_DRAW
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0)
        return ids[0]
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
        sceneProgram = program(vertexShader,fragmentShader)
        skyProgram = program(skyVertexShader,skyFragmentShader)
        atlasTexture = DreamTextures.load(context)
        val station=DreamWorlds.createStation()
        val pool=DreamWorlds.createPools()
        val crater=DreamWorlds.createCrater()
        val garden=DreamWorlds.createGarden()
        val meadow=DreamWorlds.createMeadow()
        val aquarium=DreamWorlds.createAquarium()
        val houseMeadow=DreamWorlds.createHouseMeadow()
        val houseCrater=DreamWorlds.createHouseCrater()
        vertexCountHouseMeadow=houseMeadow.vertexCount
        vertexCountHouseCrater=houseCrater.vertexCount
        vertexCountMeadow=meadow.vertexCount
        vertexCountAquarium=aquarium.vertexCount
        vertexCountGarden=garden.vertexCount
        vertexCountStation=station.vertexCount
        vertexCountPools=pool.vertexCount
        vertexCountCrater=crater.vertexCount
        sceneBufferStation=uploadScene(station)
        sceneBufferPools=uploadScene(pool)
        sceneBufferCrater=uploadScene(crater)
        sceneBufferGarden=uploadScene(garden)
        sceneBufferMeadow=uploadScene(meadow)
        sceneBufferAquarium=uploadScene(aquarium)
        sceneBufferHouseMeadow=uploadScene(houseMeadow)
        sceneBufferHouseCrater=uploadScene(houseCrater)
        lastFrameNanos=0L
    }

    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
        width = w.coerceAtLeast(1)
        height = h.coerceAtLeast(1)
        GLES20.glViewport(0,0,width,height)
        Matrix.perspectiveM(projection,0,66f,width.toFloat()/height,0.08f,520f)
    }

    private fun update(dt: Float) {
        switchCooldown=(switchCooldown-dt).coerceAtLeast(0f)
        anomalyTimer=(anomalyTimer-dt).coerceAtLeast(0f)
        anomalyPower=(anomalyPower+(if(anomalyTimer>0f) dt*1.6f else -dt*.50f)).coerceIn(0f,1f)
        elapsed+=dt
        // Suavizado de joystick: el movimiento se acelera y frena gradualmente.
        val smooth=(dt*10f).coerceIn(0f,1f)
        walkVelocityX += (moveX-walkVelocityX)*smooth
        walkVelocityY += (moveY-walkVelocityY)*smooth
        val magnitude=hypot(walkVelocityX,walkVelocityY).coerceAtLeast(1f)
        val inputX=walkVelocityX/magnitude
        val inputY=walkVelocityY/magnitude
        val fwdX=sin(player.yaw); val fwdZ=-cos(player.yaw)
        val rightX=cos(player.yaw); val rightZ=sin(player.yaw)
        val speed = if (sprinting) 6.1f else 3.5f
        val velocityX=(rightX*inputX+fwdX*inputY)*speed*dt
        val velocityZ=(rightZ*inputX+fwdZ*inputY)*speed*dt
        val newX=player.x+velocityX
        val newZ=player.z+velocityZ
        // Deslizarse por paredes si solo uno de los ejes queda bloqueado.
        if(DreamWorlds.canWalk(player.worldId,newX,player.z)) player.x=newX
        if(DreamWorlds.canWalk(player.worldId,player.x,newZ)) player.z=newZ
        val floor=when(player.worldId) {
            DreamWorlds.STATION -> DreamWorlds.groundHeight(player.x,player.z)
            DreamWorlds.CRATER -> DreamWorlds.craterHeight(player.x,player.z)
            DreamWorlds.MEADOW -> DreamWorlds.meadowHeight(player.x,player.z)
            else -> 0f
        }
        // Saltar nunca altera el suelo; la cámara sigue la altura de la pendiente.
        followY += (1.65f+floor-followY)*(dt*9f).coerceIn(0f,1f)
        if (jumpQueued && jumpOffset <= .001f) {
            jumpSpeed=5.6f
            jumpOffset=.002f
        }
        jumpQueued=false
        if (jumpOffset > 0f) {
            jumpSpeed -= 14.2f * dt
            jumpOffset = (jumpOffset + jumpSpeed*dt).coerceAtLeast(0f)
            if(jumpOffset <= 0f) jumpSpeed=0f
        }
        player.y=followY+jumpOffset
        if(abs(inputX)+abs(inputY)>.1f) walkTime+=dt*(if(sprinting)1.45f else 1f)
        portalTarget = null
        if (switchCooldown <= 0f) {
            when (player.worldId) {
                DreamWorlds.STATION -> {
                    // La puerta clásica a piscinas tiene prioridad cerca de su marco.
                    if (hypot(player.x, player.z + 20.55f) < 2.75f) {
                        portalTarget = DreamWorlds.POOLS
                    } else if (hypot(player.x + 9.2f, player.z + 17.5f) < 2.80f) {
                        portalTarget = DreamWorlds.CRATER
                    } else if (hypot(player.x - 9.2f, player.z + 17.5f) < 2.80f) {
                        portalTarget = DreamWorlds.GARDEN
                    } else if (hypot(player.x + 18f,player.z + 5.5f) < 2.8f) {
                        portalTarget = DreamWorlds.MEADOW
                    }
                }
                DreamWorlds.POOLS -> {
                    if (hypot(player.x,player.z-7.6f) < 2.75f) {
                        portalTarget = DreamWorlds.STATION
                    } else if (hypot(player.x-10f,player.z-6.5f)<2.8f) {
                        portalTarget = DreamWorlds.AQUARIUM
                    }
                }
                DreamWorlds.CRATER -> {
                    if (hypot(player.x+7.2f,player.z-24f) < 2.8f) {
                        portalTarget = DreamWorlds.STATION
                    } else if (hypot(player.x+24f,player.z-11.8f)<3.2f) {
                        portalTarget = DreamWorlds.HOUSE_CRATER
                    }
                }
                DreamWorlds.GARDEN -> {
                    if (hypot(player.x+8f,player.z-22f) < 2.8f) {
                        portalTarget = DreamWorlds.STATION
                    }
                }
                DreamWorlds.MEADOW -> {
                    if (hypot(player.x+11f,player.z-27f)<2.85f) {
                        portalTarget=DreamWorlds.STATION
                    } else if (hypot(player.x,player.z+165f)<2.9f) {
                        portalTarget=DreamWorlds.AQUARIUM
                    } else if (hypot(player.x,player.z+132.3f)<3.1f) {
                        portalTarget=DreamWorlds.HOUSE_MEADOW
                    }
                }
                DreamWorlds.AQUARIUM -> {
                    if (hypot(player.x+11f,player.z-23f)<2.85f) {
                        portalTarget=DreamWorlds.MEADOW
                    } else if (hypot(player.x-11f,player.z-23f)<2.85f) {
                        portalTarget=DreamWorlds.POOLS
                    }
                }
                DreamWorlds.HOUSE_MEADOW -> {
                    if(hypot(player.x,player.z-8f)<2.7f) portalTarget=DreamWorlds.MEADOW
                }
                DreamWorlds.HOUSE_CRATER -> {
                    if(hypot(player.x,player.z-7.5f)<2.7f) portalTarget=DreamWorlds.CRATER
                }
            }
        }
        nearPortal = portalTarget != null

        // Dos eventos irreales que suceden por exploración, no al azar.
        if(!anomalySeen && player.worldId == DreamWorlds.HOUSE_MEADOW && player.z < -99f) {
            anomalySeen = true; anomalyTimer=18f
            onDreamEvent("La casa tiene una ventana que no existía...")
        }
        if(!anomalySeen && player.worldId == DreamWorlds.HOUSE_CRATER && player.z < -68f) {
            anomalySeen = true; anomalyTimer=18f
            onDreamEvent("Algo cambió al otro lado del pasillo")
        }
        val zone= when {
            anomalyTimer>0f -> "ANOMALIA"
            player.worldId==DreamWorlds.HOUSE_MEADOW || player.worldId==DreamWorlds.HOUSE_CRATER -> "CASA"
            player.worldId==DreamWorlds.CRATER && floor < -31f -> "PROFUNDO"
            player.worldId==DreamWorlds.AQUARIUM && player.z < -85f -> "PROFUNDO"
            player.worldId==DreamWorlds.MEADOW && player.z < -110f -> "UMBRAL"
            player.worldId==DreamWorlds.GARDEN && player.z < -45f -> "UMBRAL"
            else -> "CALMA"
        }
        if(zone != currentZone) { currentZone=zone; onZoneChanged(zone) }
    }

    private fun location(id: Int, key: String) = GLES20.glGetUniformLocation(id,key)
    private fun attribute(id: Int, key: String) = GLES20.glGetAttribLocation(id,key)

    private fun drawSky() {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0)
        GLES20.glUseProgram(skyProgram)
        val top=location(skyProgram,"uTop")
        val bottom=location(skyProgram,"uHorizon")
        if(player.worldId==DreamWorlds.STATION) {
            GLES20.glUniform3f(top,.38f,.62f,.95f)
            GLES20.glUniform3f(bottom,.94f,.86f,.91f)
        } else if (player.worldId == DreamWorlds.POOLS) {
            GLES20.glUniform3f(top,.67f,.66f,.89f)
            GLES20.glUniform3f(bottom,.95f,.82f,.93f)
        } else if (player.worldId == DreamWorlds.CRATER) {
            GLES20.glUniform3f(top,.43f,.65f,.93f)
            GLES20.glUniform3f(bottom,.80f,.89f,.98f)
        } else if(player.worldId==DreamWorlds.GARDEN) {
            GLES20.glUniform3f(top,.61f,.64f,.86f)
            GLES20.glUniform3f(bottom,.99f,.83f,.75f)
        } else if(player.worldId==DreamWorlds.MEADOW) {
            GLES20.glUniform3f(top,.15f,.48f,.94f)
            GLES20.glUniform3f(bottom,.89f,.79f,.90f)
        } else if(player.worldId==DreamWorlds.AQUARIUM) {
            GLES20.glUniform3f(top,.36f,.37f,.74f)
            GLES20.glUniform3f(bottom,.83f,.67f,.83f)
        } else {
            GLES20.glUniform3f(top,.30f,.39f,.55f)
            GLES20.glUniform3f(bottom,.68f,.62f,.77f)
        }
        GLES20.glUniform1f(location(skyProgram,"uWorld"),
            when (player.worldId) { DreamWorlds.STATION -> 0f; DreamWorlds.POOLS -> 1f; DreamWorlds.CRATER -> 2f; DreamWorlds.GARDEN -> 3f; DreamWorlds.MEADOW -> 4f; DreamWorlds.AQUARIUM -> 5f; DreamWorlds.HOUSE_MEADOW -> 6f; else -> 7f })
        GLES20.glUniform1f(location(skyProgram,"uTime"),elapsed)
        val xy=attribute(skyProgram,"aXY")
        GLES20.glEnableVertexAttribArray(xy)
        skyBuffer.position(0)
        GLES20.glVertexAttribPointer(xy,2,GLES20.GL_FLOAT,false,0,skyBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,6)
        GLES20.glDisableVertexAttribArray(xy)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)
    }

    private fun drawWorld() {
        GLES20.glUseProgram(sceneProgram)
        GLES20.glUniformMatrix4fv(location(sceneProgram,"uVP"),1,false,vp,0)
        GLES20.glUniform3f(location(sceneProgram,"uCamera"),player.x,player.y,player.z)
        GLES20.glUniform1f(location(sceneProgram,"uTime"),elapsed)
        GLES20.glUniform1f(location(sceneProgram,"uAnomaly"),anomalyPower)
        GLES20.glUniform1f(location(sceneProgram,"uWorld"),
            when (player.worldId) { DreamWorlds.STATION -> 0f; DreamWorlds.POOLS -> 1f; DreamWorlds.CRATER -> 2f; DreamWorlds.GARDEN -> 3f; DreamWorlds.MEADOW -> 4f; DreamWorlds.AQUARIUM -> 5f; DreamWorlds.HOUSE_MEADOW -> 6f; else -> 7f })
        when (player.worldId) {
            DreamWorlds.STATION -> {
                GLES20.glUniform3f(location(sceneProgram,"uFogColor"),.81f,.85f,.94f)
                GLES20.glUniform3f(location(sceneProgram,"uSun"),-.30f,.88f,.49f)
                GLES20.glUniform1f(location(sceneProgram,"uFogNear"),35f)
                GLES20.glUniform1f(location(sceneProgram,"uFogFar"),118f)
            }
            DreamWorlds.POOLS -> {
                GLES20.glUniform3f(location(sceneProgram,"uFogColor"),.87f,.79f,.91f)
                GLES20.glUniform3f(location(sceneProgram,"uSun"),-.42f,.92f,.19f)
                GLES20.glUniform1f(location(sceneProgram,"uFogNear"),24f)
                GLES20.glUniform1f(location(sceneProgram,"uFogFar"),80f)
            }
            DreamWorlds.CRATER -> {
                GLES20.glUniform3f(location(sceneProgram,"uFogColor"),.66f,.76f,.87f)
                GLES20.glUniform3f(location(sceneProgram,"uSun"),-.41f,.74f,.39f)
                GLES20.glUniform1f(location(sceneProgram,"uFogNear"),82f)
                GLES20.glUniform1f(location(sceneProgram,"uFogFar"),340f)
            }
            DreamWorlds.MEADOW -> {
                GLES20.glUniform3f(location(sceneProgram,"uFogColor"),.78f,.81f,.92f)
                GLES20.glUniform3f(location(sceneProgram,"uSun"),-.22f,.95f,.43f)
                GLES20.glUniform1f(location(sceneProgram,"uFogNear"),70f)
                GLES20.glUniform1f(location(sceneProgram,"uFogFar"),210f)
            }
            DreamWorlds.HOUSE_MEADOW, DreamWorlds.HOUSE_CRATER -> {
                GLES20.glUniform3f(location(sceneProgram,"uFogColor"),.51f,.54f,.65f)
                GLES20.glUniform3f(location(sceneProgram,"uSun"),-.30f,.93f,.29f)
                GLES20.glUniform1f(location(sceneProgram,"uFogNear"),18f)
                GLES20.glUniform1f(location(sceneProgram,"uFogFar"),85f)
            }
            DreamWorlds.AQUARIUM -> {
                GLES20.glUniform3f(location(sceneProgram,"uFogColor"),.67f,.65f,.81f)
                GLES20.glUniform3f(location(sceneProgram,"uSun"),-.30f,.88f,.49f)
                GLES20.glUniform1f(location(sceneProgram,"uFogNear"),42f)
                GLES20.glUniform1f(location(sceneProgram,"uFogFar"),215f)
            }
            else -> {
                GLES20.glUniform3f(location(sceneProgram,"uFogColor"),.88f,.78f,.80f)
                GLES20.glUniform3f(location(sceneProgram,"uSun"),-.25f,.90f,.37f)
                GLES20.glUniform1f(location(sceneProgram,"uFogNear"),56f)
                GLES20.glUniform1f(location(sceneProgram,"uFogFar"),240f)
            }
        }
        val buffer = when(player.worldId) {
            DreamWorlds.STATION -> sceneBufferStation
            DreamWorlds.POOLS -> sceneBufferPools
            DreamWorlds.CRATER -> sceneBufferCrater
            DreamWorlds.GARDEN -> sceneBufferGarden
            DreamWorlds.MEADOW -> sceneBufferMeadow
            DreamWorlds.HOUSE_MEADOW -> sceneBufferHouseMeadow
            DreamWorlds.HOUSE_CRATER -> sceneBufferHouseCrater
            else -> sceneBufferAquarium
        }
        val count = when(player.worldId) {
            DreamWorlds.STATION -> vertexCountStation
            DreamWorlds.POOLS -> vertexCountPools
            DreamWorlds.CRATER -> vertexCountCrater
            DreamWorlds.GARDEN -> vertexCountGarden
            DreamWorlds.MEADOW -> vertexCountMeadow
            DreamWorlds.HOUSE_MEADOW -> vertexCountHouseMeadow
            DreamWorlds.HOUSE_CRATER -> vertexCountHouseCrater
            else -> vertexCountAquarium
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,atlasTexture)
        GLES20.glUniform1i(location(sceneProgram,"uAtlas"),0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,buffer)
        val props=arrayOf("aPos","aColor","aNormal","aKind")
        val sizes=intArrayOf(3,3,3,1)
        val offsets=intArrayOf(0,12,24,36)
        for(i in props.indices) {
            val attr=attribute(sceneProgram,props[i])
            if(attr>=0) {
                GLES20.glEnableVertexAttribArray(attr)
                GLES20.glVertexAttribPointer(attr,sizes[i],GLES20.GL_FLOAT,false,40,offsets[i])
            }
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,count)
        for(s in props) {
            val attr=attribute(sceneProgram,s)
            if(attr>=0) GLES20.glDisableVertexAttribArray(attr)
        }
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0)
    }

    override fun onDrawFrame(gl: GL10?) {
        val now=System.nanoTime()
        val dt=if(lastFrameNanos==0L) 0f else
            ((now-lastFrameNanos)/1_000_000_000f).coerceIn(0f,.05f)
        lastFrameNanos=now
        update(dt)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        drawSky()
        val walking=(abs(walkVelocityX)+abs(walkVelocityY)).coerceIn(0f,1f)
        val bob=sin(walkTime*8.5f)*.017f*walking
        Matrix.setLookAtM(
            view,0,
            player.x,player.y+bob,player.z,
            player.x+sin(player.yaw)*cos(pitch),
            player.y+bob+sin(pitch),
            player.z-cos(player.yaw)*cos(pitch),
            0f,1f,0f
        )
        Matrix.multiplyMM(vp,0,projection,0,view,0)
        drawWorld()
    }
}
