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

/** Dream Core 1.7: mundos por zonas, reflejos planos y exploración. */
class DreamRenderer(
    private val context: Context,
    private val onWorldChanged: (String) -> Unit,
    private val onZoneChanged: (String) -> Unit,
    private val onDreamEvent: (String) -> Unit,
    private val onFootstep: (String,Float,Boolean,Float) -> Unit = {_,_,_,_->}
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
    @Volatile var routeHint=""
        private set
    private var currentZone = "CALMA"
    private var anomalyTimer = 0f
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
    private var moonTexture = 0
    private var chunks=emptyMap<DreamChunks.Key,FloatArray>()
    private val resident=HashMap<DreamChunks.Key,Int>()
    private var residentCell:DreamChunks.Key?=null
    private var figureBuffer=0
    private var figureCount=0
    private val exploration=DreamExploration()
    private var corridorRevision=0
    private var stepDistance=0f
    private var lastStepX=0f
    private var lastStepZ=6f
    private var footPan=-.18f
    private var distantTimer=12f
    private var runHeld=0f
    private var stepCooldown=0f
    private val moonLife=DreamMoonLife()
    private var skySight:DreamSkySight?=null
    private var moonSightTimer=0f
    private var moonSightClear=false
    private var localDoor:DreamExploration.Door?=null
    private var mirrorFbo=0
    private var mirrorColor=0
    private var mirrorDepth=0
    private var mirrorValid=false
    private var mirrorTime=-1f
    private val mirrorVP=FloatArray(16)
    private val mirrorView=FloatArray(16)
    private var activeWorld: String? = null
    private var width = 1
    private var height = 1

    // Se llama exclusivamente en el hilo OpenGL mediante GLSurfaceView.queueEvent.
    fun turnCamera(dx: Float, dy: Float) {
        player.yaw += dx * 0.004f
        pitch = (pitch - dy * 0.004f).coerceIn(-1.48f,1.35f)
    }

    fun resetFrameClock(){lastFrameNanos=0L}

    // Las decisiones de portal viven en el motor, no en la pantalla.
    // Más adelante los eventos de red podrán pedir estos mismos cambios.
    fun interact() {
        if(!nearPortal || switchCooldown>0f)return
        localDoor?.let { door ->
            player.x=door.toX;player.z=door.toZ;player.yaw=door.yaw
            player.y=1.65f;followY=1.65f;jumpOffset=0f;jumpSpeed=0f;pitch=0f
            moveX=0f;moveY=0f;walkVelocityX=0f;walkVelocityY=0f
            lastStepX=player.x;lastStepZ=player.z;stepDistance=0f
            switchCooldown=1.4f;localDoor=null;nearPortal=false
            onDreamEvent(when {
                player.worldId==DreamWorlds.HOUSE_MEADOW->"El espejo era una puerta. El pasillo sigue cambiando."
                player.x>1000f->"La casa era pequeña. La piscina no tiene sentido."
                else->"La calle sigue aquí, como si nada hubiera pasado."
            })
            return
        }
        val target=portalTarget ?: return
        val from=player.worldId
        val completed=from==DreamJourney.sequence.last() && target==DreamWorlds.STATION
        val darkCompleted=from==DreamJourney.DARK && target==DreamWorlds.STATION && player.z < -55f
        val spawn=DreamJourney.arrival(target,from)
        player.worldId=target;player.x=spawn.x;player.z=spawn.z;player.yaw=spawn.yaw
        val floor=DreamJourney.surface(target,player.x,player.z)
        if(target==DreamWorlds.CRATER)craterPhysics.reset(player.x,player.z,floor)
        wasFalling=false;followY=floor+1.65f;player.y=followY
        jumpSpeed=0f;jumpOffset=0f;jumpQueued=false;sprinting=false
        pitch=0f;walkVelocityX=0f;walkVelocityY=0f;moveX=0f;moveY=0f
        switchCooldown=1.4f;anomalyTimer=0f;anomalyPower=0f
        runHeld=0f;stepCooldown=0f;moonSightTimer=0f;moonSightClear=false
        currentZone=if(target==DreamWorlds.HOUSE_MEADOW || target==DreamWorlds.HOUSE_CRATER) "CASA" else "CALMA"
        onZoneChanged(currentZone)
        nearPortal=false;portalTarget=null;portalTitle=""
        lastStepX=player.x;lastStepZ=player.z;stepDistance=0f
        currentWorld=target;onWorldChanged(target)
        if(completed)onDreamEvent(if(exploration.memoriesFound>=9)"Los nueve recuerdos vuelven contigo. El sueño por fin te reconoce." else "Llegaste al final de Dreamcore. Los recuerdos que faltan siguen esperándote.")
        if(darkCompleted)onDreamEvent("Las lunas han guardado tu recuerdo. Has vuelto al Umbral.")
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
        uniformLocations.clear();attributeLocations.clear()
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
        sceneProgram = program(vertexShader,fragmentShader)
        skyProgram = program(skyVertexShader,skyFragmentShader)
        atlasTexture = DreamTextures.load(context)
        cloudTexture = DreamCloudTexture.load(context)
        moonTexture = DreamMoonTexture.load(context)
        resident.clear();chunks=emptyMap();residentCell=null;activeWorld=null
        figureBuffer=uploadScene(DreamExploration.figure())
        figureCount=DreamExploration.figure().vertices.size/10
        mirrorFbo=0;mirrorColor=0;mirrorDepth=0;mirrorValid=false
        createMirrorTarget()
        lastFrameNanos=0L
    }

    /** Build only the selected world; discard its GPU buffer on a world change. */
    private fun ensureActiveScene() {
        if(activeWorld==player.worldId && chunks.isNotEmpty()) return
        for(buffer in resident.values)GLES20.glDeleteBuffers(1,intArrayOf(buffer),0)
        resident.clear();residentCell=null;mirrorValid=false
        val scene=when {
            DreamLands.contains(player.worldId)->DreamLands.create(player.worldId)
            player.worldId==DreamWorlds.STATION->DreamJourney.createHub()
            player.worldId==DreamWorlds.POOLS->DreamWorlds.createPools()
            player.worldId==DreamWorlds.CRATER->DreamWorlds.createCrater()
            player.worldId==DreamJourney.DARK->DreamDarkMap.create()
            else->error("Unknown world")
        }
        skySight=if(player.worldId==DreamJourney.DARK)DreamSkySight(scene.vertices)else null
        moonSightTimer=0f
        chunks=DreamChunks.split(DreamJourney.decorate(player.worldId,scene))
        activeWorld=player.worldId
    }

    private fun updateResidency() {
        val cell=DreamChunks.key(player.x,player.z)
        if(cell==residentCell)return
        residentCell=cell
        val selected=chunks.keys.filter{DreamChunks.nearby(it,(cell.x+.5f)*DreamChunks.SIZE,(cell.z+.5f)*DreamChunks.SIZE)}.toSet()
        for(key in resident.keys.toList())if(key !in selected) {
            GLES20.glDeleteBuffers(1,intArrayOf(resident.getValue(key)),0);resident.remove(key)
        }
        for(key in selected)if(key !in resident)resident[key]=uploadScene(DreamWorlds.Scene(chunks.getValue(key)))
    }

    private fun createMirrorTarget() {
        val ids=IntArray(1)
        GLES20.glGenTextures(1,ids,0);mirrorColor=ids[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mirrorColor)
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_RGB,512,512,0,GLES20.GL_RGB,GLES20.GL_UNSIGNED_BYTE,null)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glGenRenderbuffers(1,ids,0);mirrorDepth=ids[0]
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER,mirrorDepth)
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER,GLES20.GL_DEPTH_COMPONENT16,512,512)
        GLES20.glGenFramebuffers(1,ids,0);mirrorFbo=ids[0]
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,mirrorFbo)
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,GLES20.GL_COLOR_ATTACHMENT0,GLES20.GL_TEXTURE_2D,mirrorColor,0)
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER,GLES20.GL_DEPTH_ATTACHMENT,GLES20.GL_RENDERBUFFER,mirrorDepth)
        if(GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)!=GLES20.GL_FRAMEBUFFER_COMPLETE) {
            GLES20.glDeleteFramebuffers(1,intArrayOf(mirrorFbo),0);mirrorFbo=0
            GLES20.glDeleteTextures(1,intArrayOf(mirrorColor),0);mirrorColor=0
            GLES20.glDeleteRenderbuffers(1,intArrayOf(mirrorDepth),0);mirrorDepth=0
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0)
    }

    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
        width = w.coerceAtLeast(1)
        height = h.coerceAtLeast(1)
        GLES20.glViewport(0,0,width,height)
        Matrix.perspectiveM(projection,0,66f,width.toFloat()/height,0.18f,520f)
    }

    private fun update(dt: Float,watchDt:Float) {
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
        runHeld=if(sprinting && abs(inputX)+abs(inputY)>.1f)runHeld+dt else 0f
        stepCooldown=(stepCooldown-dt).coerceAtLeast(0f)
        val indoors=DreamLands.indoor(player.worldId,player.x,player.z) || player.worldId==DreamWorlds.POOLS
        val speed=DreamTravel.speed(sprinting,runHeld,indoors)
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
                onDreamEvent("Llegaste al fondo. La casa iluminada continúa el recorrido.")
            }
        } else {
            val moved=DreamTravel.move(player.worldId,player.x,player.z,vx,vz,dt)
            player.x=moved.first;player.z=moved.second
            floor=DreamJourney.surface(player.worldId,player.x,player.z)
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
        portalTarget=if(switchCooldown<=0f)
            DreamJourney.portalAt(player.worldId,player.x,player.z,player.y-1.65f) else null
        localDoor=if(switchCooldown<=0f)exploration.door(player.worldId,player.x,player.z)else null
        portalTitle=portalTarget?.let{DreamJourney.gateLabel(player.worldId,it)} ?: ""
        if(localDoor!=null)portalTitle=localDoor!!.label
        nearPortal = portalTarget != null || localDoor!=null
        if(player.worldId in DreamJourney.sequence) {
            val target=if(player.x>1000f)DreamJourney.Point(1600f,8f)else DreamJourney.exit(player.worldId)
            val dx=target.x-player.x;val dz=target.z-player.z
            var angle=kotlin.math.atan2(dx,-dz)-player.yaw
            while(angle>kotlin.math.PI)angle-=(kotlin.math.PI*2).toFloat()
            while(angle< -kotlin.math.PI)angle+=(kotlin.math.PI*2).toFloat()
            val arrow=when {abs(angle)<.45f->"↑";abs(angle)>2.5f->"↓";angle<0f->"←";else->"→"}
            routeHint="$arrow  ${hypot(dx,dz).toInt()} m · "+if(player.x>1000f)"La puerta del pueblo" else "Siguiente umbral"
        } else routeHint=""
        for(notice in exploration.step(player.worldId,player.x,player.z,player.yaw,pitch,dt)) {
            onDreamEvent(notice);anomalyTimer=7f
        }
        if(corridorRevision!=exploration.corridorRevision) {
            corridorRevision=exploration.corridorRevision;activeWorld=null
        }
        val inDark=player.worldId==DreamJourney.DARK
        if(inDark) {
            moonSightTimer-=watchDt
            if(moonSightTimer<=0f) {
                moonSightTimer=.25f
                moonSightClear=skySight?.clear(player.x,player.y,player.z,
                    sin(player.yaw)*cos(pitch),sin(pitch),-cos(player.yaw)*cos(pitch))?:false
            }
        }
        if(moonLife.step(inDark,player.x,player.y,player.z,player.yaw,pitch,watchDt,moonSightClear)) {
            onDreamEvent("El cielo acaba de recordar tu nombre.")
            onFootstep("chime",0f,false,.12f)
        }
        distantTimer-=dt
        if(distantTimer<=0f && DreamLands.contains(player.worldId)) {
            distantTimer=22f+(kotlin.math.sin(elapsed*.1f)+1f)*8f
            val anchor=DreamLands.secrets(player.worldId).minByOrNull{hypot(player.x-it.x,player.z-it.z)}
            if(anchor!=null) {
                val dx=anchor.x-player.x;val dz=anchor.z-player.z
                val distance=hypot(dx,dz).coerceAtLeast(1f)
                val pan=((cos(player.yaw)*dx+sin(player.yaw)*dz)/distance).coerceIn(-.85f,.85f)
                val level=(.28f*(1f-distance/170f)).coerceIn(0f,.28f)
                if(level>.01f)onFootstep("chime",pan,DreamLands.indoor(player.worldId,player.x,player.z),level)
            }
        }
        if(DreamLands.contains(player.worldId))routeHint+=" · Recuerdos ${exploration.memoriesFound}/9"
        val travelled=hypot(player.x-lastStepX,player.z-lastStepZ)
        if(travelled<2f)stepDistance+=travelled
        lastStepX=player.x;lastStepZ=player.z
        val grounded=if(inCrater)craterPhysics.grounded else jumpOffset<=.001f
        val stride=if(sprinting)3.5f else 1.85f
        if(grounded && stepDistance>stride && stepCooldown<=0f) {
            stepDistance=0f;stepCooldown=.29f;footPan=-footPan
            val surface=if(inWater)"water" else if(DreamLands.contains(player.worldId))DreamLands.material(player.worldId,player.x,player.z) else "stone"
            val indoor=DreamLands.indoor(player.worldId,player.x,player.z) || player.worldId==DreamWorlds.POOLS
            onFootstep(surface,footPan,indoor,if(sprinting).24f else .19f)
        }

        val zone= when {
            inDark && moonLife.giantVisible -> "ECLIPSE"
            inDark && moonLife.looking && moonLife.vigil>10f -> "OBSERVADO"
            anomalyTimer>0f -> "ANOMALIA"
            DreamLands.contains(player.worldId)->DreamLands.zone(player.worldId,player.x,player.z)
            player.worldId==DreamWorlds.HOUSE_MEADOW || player.worldId==DreamWorlds.HOUSE_CRATER -> "CASA"
            player.worldId==DreamWorlds.CRATER && floor < -31f -> "PROFUNDO"
            player.worldId==DreamWorlds.AQUARIUM && player.z < -85f -> "PROFUNDO"
            player.worldId==DreamWorlds.MEADOW && player.z < -110f -> "UMBRAL"
            player.worldId==DreamWorlds.GARDEN && player.z < -45f -> "UMBRAL"
            else -> "CALMA"
        }
        if(zone != currentZone) { currentZone=zone; onZoneChanged(zone) }
    }

    private val uniformLocations=HashMap<Pair<Int,String>,Int>()
    private val attributeLocations=HashMap<Pair<Int,String>,Int>()
    private fun location(id:Int,key:String)=uniformLocations.getOrPut(id to key){GLES20.glGetUniformLocation(id,key)}
    private fun attribute(id:Int,key:String)=attributeLocations.getOrPut(id to key){GLES20.glGetAttribLocation(id,key)}

    private fun moonUniforms(program:Int) {
        GLES20.glUniform4f(location(program,"uMoonMood"),moonLife.mainBlink,moonLife.smallBlink,moonLife.mainSmile,moonLife.smallSmile)
        GLES20.glUniform4f(location(program,"uEyeTarget"),moonLife.eyeX,moonLife.eyeY,moonLife.smallEyeX,moonLife.smallEyeY)
        GLES20.glUniform4f(location(program,"uGiant"),moonLife.giantRadius,1f,moonLife.giantBlink,moonLife.giantReveal)
        GLES20.glUniform3f(location(program,"uGiantDirection"),moonLife.giantX,moonLife.giantY,moonLife.giantZ)
    }

    private fun drawSky(mirrored:Boolean=false) {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0)
        GLES20.glUseProgram(skyProgram)
        moonUniforms(skyProgram)
        val top=location(skyProgram,"uTop")
        val bottom=location(skyProgram,"uHorizon")
        if(DreamLands.contains(player.worldId) && DreamLands.night(player.worldId)) {
            GLES20.glUniform3f(top,.025f,.035f,.12f)
            GLES20.glUniform3f(bottom,.22f,.24f,.40f)
        } else if(player.worldId==DreamJourney.DARK) {
            GLES20.glUniform3f(top,.008f,.012f,.04f)
            GLES20.glUniform3f(bottom,.065f,.050f,.13f)
        } else if(DreamExpansion.contains(player.worldId)) {
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
            DreamJourney.worldCode(player.worldId))
        GLES20.glUniform1f(location(skyProgram,"uTime"),elapsed%512f)
        GLES20.glUniform3f(location(skyProgram,"uForward"),
            sin(player.yaw)*cos(pitch),sin(pitch),(if(mirrored)1f else -1f)*cos(player.yaw)*cos(pitch))
        GLES20.glUniform3f(location(skyProgram,"uRight"),cos(player.yaw),0f,(if(mirrored)-1f else 1f)*sin(player.yaw))
        GLES20.glUniform3f(location(skyProgram,"uUp"),
            -sin(player.yaw)*sin(pitch),cos(pitch),(if(mirrored)-1f else 1f)*cos(player.yaw)*sin(pitch))
        GLES20.glUniform1f(location(skyProgram,"uAspect"),width.toFloat()/height)
        GLES20.glUniform3f(location(skyProgram,"uSun"),-.34f,.82f,.46f)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,cloudTexture)
        GLES20.glUniform1i(location(skyProgram,"uClouds"),1)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,moonTexture)
        GLES20.glUniform1i(location(skyProgram,"uMoon"),2)
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

    private fun drawWorld(mirrorPass:Boolean=false) {
        GLES20.glUseProgram(sceneProgram)
        moonUniforms(sceneProgram)
        GLES20.glUniformMatrix4fv(location(sceneProgram,"uVP"),1,false,if(mirrorPass)mirrorVP else vp,0)
        GLES20.glUniformMatrix4fv(location(sceneProgram,"uMirrorVP"),1,false,mirrorVP,0)
        GLES20.glUniform1f(location(sceneProgram,"uMirrorPass"),if(mirrorPass)1f else 0f)
        val mirror=DreamLands.mirror(player.worldId)
        GLES20.glUniform1f(location(sceneProgram,"uMirrorZ"),mirror?.z?:0f)
        GLES20.glUniform1f(location(sceneProgram,"uMirrorReady"),if(mirrorValid && !mirrorPass)1f else 0f)
        GLES20.glUniform3f(location(sceneProgram,"uMirrorCenter"),mirror?.x?:9999f,0f,mirror?.z?:9999f)
        GLES20.glUniform3f(location(sceneProgram,"uEntity"),exploration.presenceX,DreamLands.height(player.worldId,exploration.presenceX,exploration.presenceZ),exploration.presenceZ)
        GLES20.glUniform1f(location(sceneProgram,"uPresence"),exploration.presenceAlpha)
        GLES20.glUniform3f(location(sceneProgram,"uCamera"),player.x,player.y,if(mirrorPass && mirror!=null)2f*mirror.z-player.z else player.z)
        GLES20.glUniform1f(location(sceneProgram,"uTime"),elapsed%512f)
        GLES20.glUniform1f(location(sceneProgram,"uAnomaly"),anomalyPower)
        GLES20.glUniform1f(location(sceneProgram,"uWorld"),
            DreamJourney.worldCode(player.worldId))
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
                GLES20.glUniform3f(location(sceneProgram,"uFogColor"),.77f,.82f,.76f)
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
        if(player.worldId==DreamJourney.DARK) {
            GLES20.glUniform3f(location(sceneProgram,"uFogColor"),.055f,.065f,.115f)
            GLES20.glUniform3f(location(sceneProgram,"uSun"),.10f,.56f,-.82f)
            GLES20.glUniform1f(location(sceneProgram,"uFogNear"),60f)
            GLES20.glUniform1f(location(sceneProgram,"uFogFar"),240f)
        }
        if(DreamLands.contains(player.worldId)) {
            val night=DreamLands.night(player.worldId)
            GLES20.glUniform3f(location(sceneProgram,"uFogColor"),if(night).22f else .78f,if(night).24f else .81f,if(night).40f else .88f)
            GLES20.glUniform1f(location(sceneProgram,"uFogNear"),if(DreamLands.indoor(player.worldId,player.x,player.z))45f else 125f)
            GLES20.glUniform1f(location(sceneProgram,"uFogFar"),if(DreamLands.indoor(player.worldId,player.x,player.z))180f else 315f)
        }
        if(!mirrorPass) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE3)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mirrorColor)
            GLES20.glUniform1i(location(sceneProgram,"uReflection"),3)
        } else {
            // Never sample a texture while it is attached to the render target.
            GLES20.glActiveTexture(GLES20.GL_TEXTURE3)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0)
            GLES20.glUniform1i(location(sceneProgram,"uReflection"),3)
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,cloudTexture)
        GLES20.glUniform1i(location(sceneProgram,"uClouds"),1)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,moonTexture)
        GLES20.glUniform1i(location(sceneProgram,"uMoon"),2)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,atlasTexture)
        GLES20.glUniform1i(location(sceneProgram,"uAtlas"),0)
        val props=arrayOf("aPos","aColor","aNormal","aKind")
        val sizes=intArrayOf(3,3,3,1)
        val offsets=intArrayOf(0,12,24,36)
        fun drawBuffer(buffer:Int,count:Int) {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,buffer)
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
        }
        for((key,buffer) in resident)drawBuffer(buffer,chunks.getValue(key).size/10)
        if(exploration.presenceAlpha>.001f) {
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE_MINUS_SRC_ALPHA)
            GLES20.glDepthMask(false)
            drawBuffer(figureBuffer,figureCount)
            GLES20.glDepthMask(true)
            GLES20.glDisable(GLES20.GL_BLEND)
        }
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0)
    }

    override fun onDrawFrame(gl: GL10?) {
        val now=System.nanoTime()
        val foregroundDt=if(lastFrameNanos==0L)0f else
            ((now-lastFrameNanos)/1_000_000_000f).coerceIn(0f,.25f)
        val dt=foregroundDt.coerceAtMost(.05f)
        lastFrameNanos=now
        update(dt,foregroundDt)
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
        ensureActiveScene();updateResidency()
        val mirror=DreamLands.mirror(player.worldId)
        val mirrorNear=mirror!=null && abs(player.x-mirror.x)<65f && player.z>mirror.z+.35f && player.z<mirror.z+70f
        if(!mirrorNear)mirrorValid=false
        if(mirrorNear && mirrorFbo!=0 && elapsed-mirrorTime>.10f) {
            mirrorTime=elapsed
            // V * translation(2z) * reflection(z): a true planar camera reflection.
            view.copyInto(mirrorView)
            Matrix.translateM(mirrorView,0,0f,0f,2f*mirror!!.z)
            Matrix.scaleM(mirrorView,0,1f,1f,-1f)
            Matrix.multiplyMM(mirrorVP,0,projection,0,mirrorView,0)
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,mirrorFbo)
            GLES20.glViewport(0,0,512,512)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            drawSky(true);drawWorld(true)
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0)
            GLES20.glViewport(0,0,width,height)
            mirrorValid=true
        }
        drawWorld()
    }
}
