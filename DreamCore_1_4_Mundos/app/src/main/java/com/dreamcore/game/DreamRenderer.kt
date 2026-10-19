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

/** Dream Core 1.4: doce sueños, un solo mapa residente y cielo orientado a cámara. */
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
    private val craterPhysics = DreamCraterPhysics()
    private var wasFalling = false
    private var landingNoticeCooldown = 0f
    private var lastFrameNanos = 0L
    private var elapsed = 0f
    private var switchCooldown = 0f
    private var portalTarget: String? = null
    @Volatile var portalTitle = ""
        private set
    private var currentZone = "CALMA"
    private var anomalyTimer = 0f
    private var anomalySeen = false
    private var anomalyPower = 0f

    private val projection = FloatArray(16)
    private val view = FloatArray(16)
    private val vp = FloatArray(16)
    // 0.6: iluminación por hemisferios, atlas de materiales, niebla volumétrica
    // aproximada por altura y agua animada. GLES 2.0 (no requiere GLES 3).
    private val vertexShader = DreamShaders.vertexShader

    private val fragmentShader = DreamShaders.fragmentShader

    private val skyVertexShader = DreamShaders.skyVertexShader
    private val skyFragmentShader = DreamShaders.skyFragmentShader

    private val skyBuffer = ByteBuffer.allocateDirect(12 * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(floatArrayOf(-1f,-1f, 1f,-1f, -1f,1f, -1f,1f, 1f,-1f, 1f,1f))
            position(0)
        }

    private var sceneProgram = 0
    private var skyProgram = 0
    private var atlasTexture = 0
    private var cloudTexture = 0
    private var activeBuffer = 0
    private var activeCount = 0
    private var activeWorld: String? = null
    private var width = 1
    private var height = 1

    // Se llama exclusivamente en el hilo OpenGL mediante GLSurfaceView.queueEvent.
    fun turnCamera(dx: Float, dy: Float) {
        player.yaw += dx * 0.004f
        pitch = (pitch - dy * 0.004f).coerceIn(-1.48f,1.35f)
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
                if(DreamExpansion.contains(currentWorld)) {
                    player.x=DreamExpansion.stationGateX(currentWorld)
                    player.z=DreamExpansion.STATION_GATE_Z-7f
                    player.yaw=kotlin.math.PI.toFloat()
                }
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
            DreamExpansion.CHESS, DreamExpansion.TUNNELS,
            DreamExpansion.CLOUD_HOTEL, DreamExpansion.ROAD -> {
                val spawn=DreamExpansion.spawn(target)
                player.x=spawn.first; player.z=spawn.second; player.yaw=0f
            }
        }
        val floor = when (target) {
            DreamWorlds.STATION -> DreamWorlds.groundHeight(player.x,player.z)
            DreamWorlds.CRATER -> DreamWorlds.craterHeight(player.x,player.z)
            DreamWorlds.MEADOW -> DreamWorlds.meadowHeight(player.x,player.z)
            else -> 0f
        }
        if (target == DreamWorlds.CRATER) craterPhysics.reset(player.x,player.z,floor)
        wasFalling = false
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
        cloudTexture = DreamCloudTexture.load(context)
        activeBuffer=0; activeCount=0; activeWorld=null
        lastFrameNanos=0L
    }

    /** Build only the selected world; discard its GPU buffer on a world change. */
    private fun ensureActiveScene() {
        if(activeWorld==player.worldId && activeBuffer!=0) return
        val scene=when(player.worldId) {
            DreamWorlds.STATION -> DreamExpansion.stationScene()
            DreamWorlds.POOLS -> DreamWorlds.createPools()
            DreamWorlds.CRATER -> DreamWorlds.createCrater()
            DreamWorlds.GARDEN -> DreamWorlds.createGarden()
            DreamWorlds.MEADOW -> DreamWorlds.createMeadow()
            DreamWorlds.AQUARIUM -> DreamWorlds.createAquarium()
            DreamWorlds.HOUSE_MEADOW -> DreamWorlds.createHouseMeadow()
            DreamWorlds.HOUSE_CRATER -> DreamWorlds.createHouseCrater()
            else -> DreamExpansion.create(player.worldId)
        }
        // Old opaque ellipsoids are replaced by the illuminated cloud sky.
        var kept=0
        val data=scene.vertices
        for(i in data.indices step 30) if(data[i+9] !in 19.5f..20.5f) kept+=30
        val filtered=if(kept==data.size) data else FloatArray(kept).also { out ->
            var target=0
            for(i in data.indices step 30) if(data[i+9] !in 19.5f..20.5f) {
                data.copyInto(out,target,i,i+30); target+=30
            }
        }
        val buffer=uploadScene(DreamWorlds.Scene(filtered))
        if(activeBuffer!=0) GLES20.glDeleteBuffers(1,intArrayOf(activeBuffer),0)
        activeBuffer=buffer; activeCount=filtered.size/10; activeWorld=player.worldId
    }

    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
        width = w.coerceAtLeast(1)
        height = h.coerceAtLeast(1)
        GLES20.glViewport(0,0,width,height)
        Matrix.perspectiveM(projection,0,66f,width.toFloat()/height,0.18f,520f)
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
        val inCrater = player.worldId == DreamWorlds.CRATER
        val inWater = inCrater && craterPhysics.grounded && DreamWorlds.craterIsWater(player.x,player.z)
        val movementScale = if (inWater) .64f else 1f
        val vx = (rightX*inputX+fwdX*inputY)*speed*movementScale
        val vz = (rightZ*inputX+fwdZ*inputY)*speed*movementScale
        var floor = 0f
        if (inCrater) {
            craterPhysics.step(dt,vx,vz,jumpQueued)
            jumpQueued = false
            player.x=craterPhysics.x; player.z=craterPhysics.z
            player.y=craterPhysics.feetY+1.65f
            followY=player.y; jumpOffset=0f; jumpSpeed=0f
            floor=craterPhysics.feetY
            landingNoticeCooldown=(landingNoticeCooldown-dt).coerceAtLeast(0f)
            val falling=craterPhysics.verticalSpeed < -8f
            if (falling && !wasFalling) onDreamEvent("El abismo te deja caer. Busca la luz de la casa.")
            wasFalling=falling
            if (craterPhysics.landedThisStep && craterPhysics.landingSpeed>8f && landingNoticeCooldown<=0f) {
                landingNoticeCooldown=3f
                onDreamEvent("Has llegado al fondo. La puerta iluminada vuelve al inicio.")
            }
        } else {
            val newX=player.x+vx*dt
            val newZ=player.z+vz*dt
            if(DreamWorlds.canWalk(player.worldId,newX,player.z)) player.x=newX
            if(DreamWorlds.canWalk(player.worldId,player.x,newZ)) player.z=newZ
            floor=when(player.worldId) {
                DreamWorlds.STATION -> DreamWorlds.groundHeight(player.x,player.z)
                DreamWorlds.MEADOW -> DreamWorlds.meadowHeight(player.x,player.z)
                else -> 0f
            }
            followY += (1.65f+floor-followY)*(dt*9f).coerceIn(0f,1f)
            if (jumpQueued && jumpOffset <= .001f) {
                jumpSpeed=5.6f; jumpOffset=.002f
            }
            jumpQueued=false
            if (jumpOffset > 0f) {
                jumpSpeed -= 14.2f*dt
                jumpOffset=(jumpOffset+jumpSpeed*dt).coerceAtLeast(0f)
                if(jumpOffset<=0f) jumpSpeed=0f
            }
            player.y=followY+jumpOffset
        }
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
                    } else {
                        portalTarget=DreamExpansion.stationPortalAt(player.x,player.z)
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
                    if (DreamWorlds.craterReturnDoor(player.x,player.z,craterPhysics.feetY)) {
                        portalTarget = DreamWorlds.STATION
                    } else if (hypot(player.x+7.2f,player.z-24f) < 2.8f && player.y > -3f) {
                        portalTarget = DreamWorlds.STATION
                    } else if (hypot(player.x+24f,player.z-11.8f)<3.2f && player.y > -3f) {
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
        if(switchCooldown<=0f && DreamExpansion.contains(player.worldId)) {
            portalTarget=DreamExpansion.portalAt(player.worldId,player.x,player.z)
        }
        portalTitle=portalTarget?.let { DreamExpansion.title(it) } ?: ""
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
        if(DreamExpansion.contains(player.worldId)) {
            val topColor=DreamExpansion.skyTop(player.worldId)
            val horizonColor=DreamExpansion.skyHorizon(player.worldId)
            GLES20.glUniform3f(top,topColor[0],topColor[1],topColor[2])
            GLES20.glUniform3f(bottom,horizonColor[0],horizonColor[1],horizonColor[2])
        } else if(player.worldId==DreamWorlds.STATION) {
            GLES20.glUniform3f(top,.38f,.62f,.95f)
            GLES20.glUniform3f(bottom,.94f,.86f,.91f)
        } else if (player.worldId == DreamWorlds.POOLS) {
            GLES20.glUniform3f(top,.67f,.66f,.89f)
            GLES20.glUniform3f(bottom,.95f,.82f,.93f)
        } else if (player.worldId == DreamWorlds.CRATER) {
            GLES20.glUniform3f(top,.34f,.46f,.75f)
            GLES20.glUniform3f(bottom,.94f,.80f,.86f)
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
            DreamExpansion.worldCode(player.worldId))
        GLES20.glUniform1f(location(skyProgram,"uTime"),elapsed%512f)
        GLES20.glUniform3f(location(skyProgram,"uForward"),
            sin(player.yaw)*cos(pitch),sin(pitch),-cos(player.yaw)*cos(pitch))
        GLES20.glUniform3f(location(skyProgram,"uRight"),cos(player.yaw),0f,sin(player.yaw))
        GLES20.glUniform3f(location(skyProgram,"uUp"),
            -sin(player.yaw)*sin(pitch),cos(pitch),cos(player.yaw)*sin(pitch))
        GLES20.glUniform1f(location(skyProgram,"uAspect"),width.toFloat()/height)
        GLES20.glUniform3f(location(skyProgram,"uSun"),-.34f,.82f,.46f)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,cloudTexture)
        GLES20.glUniform1i(location(skyProgram,"uClouds"),1)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
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
        ensureActiveScene()
        GLES20.glUseProgram(sceneProgram)
        GLES20.glUniformMatrix4fv(location(sceneProgram,"uVP"),1,false,vp,0)
        GLES20.glUniform3f(location(sceneProgram,"uCamera"),player.x,player.y,player.z)
        GLES20.glUniform1f(location(sceneProgram,"uTime"),elapsed%512f)
        GLES20.glUniform1f(location(sceneProgram,"uAnomaly"),anomalyPower)
        GLES20.glUniform1f(location(sceneProgram,"uWorld"),
            DreamExpansion.worldCode(player.worldId))
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
                GLES20.glUniform3f(location(sceneProgram,"uFogColor"),.48f,.54f,.69f)
                GLES20.glUniform3f(location(sceneProgram,"uSun"),-.41f,.74f,.39f)
                GLES20.glUniform1f(location(sceneProgram,"uFogNear"),105f)
                GLES20.glUniform1f(location(sceneProgram,"uFogFar"),420f)
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
        if(DreamExpansion.contains(player.worldId)) {
            val fog=DreamExpansion.fogColor(player.worldId)
            GLES20.glUniform3f(location(sceneProgram,"uFogColor"),fog[0],fog[1],fog[2])
            GLES20.glUniform3f(location(sceneProgram,"uSun"),-.34f,.82f,.46f)
            GLES20.glUniform1f(location(sceneProgram,"uFogNear"),DreamExpansion.fogNear(player.worldId))
            GLES20.glUniform1f(location(sceneProgram,"uFogFar"),DreamExpansion.fogFar(player.worldId))
        }
        val buffer=activeBuffer
        val count=activeCount
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,cloudTexture)
        GLES20.glUniform1i(location(sceneProgram,"uClouds"),1)
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
        val bob=sin(walkTime*8.5f)*.017f*walking *
            (if(player.worldId==DreamWorlds.CRATER && !craterPhysics.grounded) 0f else 1f)
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
