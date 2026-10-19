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

/** Dream Core 1.9: mundos por zonas, reflejos planos y exploración. */
class DreamRenderer(
    private val context: Context,
    private val onWorldChanged: (String) -> Unit,
    private val onZoneChanged: (String) -> Unit,
    private val onDreamEvent: (String) -> Unit,
    private val onFootstep: (String,Float,Boolean,Float) -> Unit = {_,_,_,_->},
    private val story:DreamChronicle=DreamChronicle(),
    private val options:DreamOptions=DreamOptions(),
    private val onSave:(String,Boolean)->Unit={_,_->}
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
    private val exploration=DreamExploration(story.found)
    private val actors=DreamActors()
    private val weather=DreamAtmosphere()
    private val events=DreamEvents()
    private val photoStore=DreamPhotoStore(context)
    private var photoSight:DreamSkySight?=null
    @Volatile private var photoQueued=false
    @Volatile private var photoBusy=false
    @Volatile private var photoComplete:Boolean?=null
    private var pendingClues=emptyList<String>()
    private var photoFlash=0f
    @Volatile var transitionFade=0f;private set
    @Volatile var photoNotice="";private set
    fun close(){photoStore.close()}
    fun takePhoto(){if(!photoBusy){photoQueued=true;photoBusy=true}}
    private var planetsTexture=0;private var faceTexture=0
    private var mournerBuffer=0;private var mournerCount=0
    private var busBuffer=0;private var busCount=0
    private var eclipse=0f
    private var objectNear:DreamObjects.Item?=null
    private var saveTimer=0f
    private var headBuffer=0;private var headCount=0
    private var rainBuffer=0;private var rainCount=0
    private var skyHouseBuffer=0;private var skyHouseCount=0
    private var traceBuffer=0;private var traceCount=0
    private var materialTexture=0
    private var enhanced=true
    private var appliedQuality=-1
    private var shadows:DreamShadowMap?=null
    @Volatile var journal=""
        private set
    init {
        val c=story.checkpoint
        player.worldId=c.world;player.x=c.x;player.y=c.y;player.z=c.z;player.yaw=c.yaw
        pitch=c.pitch;followY=c.y;currentWorld=c.world
        if(c.world==DreamWorlds.CRATER)craterPhysics.reset(c.x,c.z,c.y-1.65f)
        if(story.visits.isEmpty())story.enter(c.world)
    }
    fun save(flush:Boolean=false) {
        story.checkpoint=DreamChronicle.Checkpoint(player.worldId,player.x,player.y,player.z,player.yaw,pitch)
        story.moon=moonLife.snapshot()
        onSave(DreamChronicle.encode(story),flush)
        journal="Recuerdos ${story.memories}/9 · Símbolos ${story.symbols.size}/4 · Llamadas ${story.calls.size}/3\n"+
            "Sueños secretos: ${story.flags.count{it.startsWith("secret:")}}/5 · Fotografías ${DreamPhotography.count(story)}/4\nFinales ${story.endings.joinToString()}"
    }
    private fun warp(c:DreamChronicle.Checkpoint) {
        player.worldId=c.world;player.x=c.x;player.y=c.y;player.z=c.z;player.yaw=c.yaw;pitch=c.pitch
        followY=c.y;jumpOffset=0f;jumpSpeed=0f;moveX=0f;moveY=0f;walkVelocityX=0f;walkVelocityY=0f
        if(c.world==DreamWorlds.CRATER)craterPhysics.reset(c.x,c.z,c.y-1.65f)
        lastStepX=c.x;lastStepZ=c.z;stepDistance=0f;switchCooldown=1.4f
        localDoor=null;objectNear=null;portalTarget=null;nearPortal=false;activeWorld=null
        transitionFade=1f
        currentWorld=c.world;currentZone="CALMA";onZoneChanged(currentZone);onWorldChanged(c.world)
        save()
    }
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
    private var configuredWorld:String?=null
    private var width = 1
    private var height = 1

    // Se llama exclusivamente en el hilo OpenGL mediante GLSurfaceView.queueEvent.
    fun turnCamera(dx: Float, dy: Float) {
        player.yaw += dx * 0.004f * options.sensitivity
        pitch = (pitch - dy * 0.004f * options.sensitivity).coerceIn(-1.48f,1.35f)
    }

    fun resetFrameClock(){lastFrameNanos=0L}

    // Las decisiones de portal viven en el motor, no en la pantalla.
    // Más adelante los eventos de red podrán pedir estos mismos cambios.
    fun interact() {
        if(!nearPortal || switchCooldown>0f)return
        objectNear?.let { item ->
            val result=DreamObjects.use(item,story)
            onDreamEvent(result.text);onFootstep(result.sound,0f,true,.22f)
            switchCooldown=.65f
            result.warp?.let { point ->
                val y=DreamJourney.surface(player.worldId,point.x,point.z)+1.65f
                warp(DreamChronicle.Checkpoint(player.worldId,point.x,y,point.z,player.yaw,pitch))
            }
            result.world?.let { target ->
                val spawn=DreamJourney.arrival(target,player.worldId)
                story.enter(target);warp(DreamChronicle.Checkpoint(target,spawn.x,DreamJourney.surface(target,spawn.x,spawn.z)+1.65f,spawn.z,spawn.yaw,0f))
            }
            result.pocket?.let { target ->
                story.returnPoint=DreamChronicle.Checkpoint(player.worldId,player.x,player.y,player.z,player.yaw,pitch)
                story.enter(target);story.flags.add("secret:"+target)
                warp(DreamChronicle.Checkpoint(target,0f,1.65f,2f,0f,0f))
            }
            save();return
        }
        if(portalTarget==DreamPocket.RETURN) {
            val c=story.returnPoint?:DreamChronicle.Checkpoint(DreamWorlds.STATION,0f,1.65f,6f,0f,0f)
            story.returnPoint=null;warp(c);onDreamEvent("Vuelves al lugar que la puerta recordaba.");return
        }
        localDoor?.let { door ->
            player.x=door.toX;player.z=door.toZ;player.yaw=door.yaw
            player.y=1.65f;followY=1.65f;jumpOffset=0f;jumpSpeed=0f;pitch=0f
            moveX=0f;moveY=0f;walkVelocityX=0f;walkVelocityY=0f
            lastStepX=player.x;lastStepZ=player.z;stepDistance=0f
            switchCooldown=1.4f;localDoor=null;nearPortal=false;save()
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
        val darkCompleted=(from==DreamJourney.DARK || from==DreamHorizons.DARK) && target==DreamWorlds.STATION && player.z < -55f
        transitionFade=1f
        story.enter(target)
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
        if(completed) {
            val ending=story.ending();story.endings.add(ending)
            story.flags.removeAll{it.startsWith("ending:")};story.flags.add("ending:"+ending);activeWorld=null
            onDreamEvent(when(ending){"DESPERTAR"->"DESPERTAR · La melodía está completa. Devuelves el billete y reconoces la mañana.";"PERMANECER"->"PERMANECER · La casa guarda tu nombre. Has elegido un lugar dentro del sueño.";else->"EL ECO · Regresas al Umbral. Todavía hay voces y recuerdos por encontrar."})
        }
        save()
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
        moonLife.restore(story.moon)
        uniformLocations.clear();attributeLocations.clear()
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
        sceneProgram = try { enhanced=true;program(vertexShader,fragmentShader) } catch(e:Exception) {
            enhanced=false;onDreamEvent("Tu dispositivo usa iluminación compatible. "+e.message.orEmpty().take(70))
            program(DreamLegacyShaders.vertexShader,DreamLegacyShaders.fragmentShader)
        }
        skyProgram = program(skyVertexShader,skyFragmentShader)
        val maps=DreamSurfaceTextures.load(context,options.quality)
        atlasTexture=maps.first;materialTexture=maps.second
        appliedQuality=options.quality
        shadows=DreamShadowMap(::program).also{try{it.configure(if(enhanced)options.quality else 0)}catch(_:Exception){it.configure(0)}}
        cloudTexture = DreamCloudTexture.load(context)
        moonTexture = DreamMoonTexture.load(context)
        planetsTexture=DreamArtTextures.load(context,R.drawable.dream_planets_19,1024,512)
        faceTexture=DreamArtTextures.load(context,R.drawable.dream_weeping_face_19,1024,1024)
        resident.clear();chunks=emptyMap();residentCell=null;activeWorld=null
        figureBuffer=uploadScene(DreamShapes.presence("curious"))
        figureCount=DreamShapes.presence("curious").vertices.size/10
        mirrorFbo=0;mirrorColor=0;mirrorDepth=0;mirrorValid=false
        createMirrorTarget()
        fun asset(scene:DreamWorlds.Scene):Pair<Int,Int> = uploadScene(scene) to scene.vertices.size/10
        asset(DreamShapes.presence("mourner")).let{mournerBuffer=it.first;mournerCount=it.second}
        asset(DreamEvents.bus()).let{busBuffer=it.first;busCount=it.second}
        asset(DreamActors.head()).let{headBuffer=it.first;headCount=it.second}
        asset(DreamActors.traces()).let{traceBuffer=it.first;traceCount=it.second}
        asset(DreamAtmosphere.particles()).let{rainBuffer=it.first;rainCount=it.second}
        asset(DreamAtmosphere.skyHouses()).let{skyHouseBuffer=it.first;skyHouseCount=it.second}
        onWorldChanged(player.worldId);save()
        lastFrameNanos=0L
    }

    /** Build only the selected world; discard its GPU buffer on a world change. */
    private fun ensureActiveScene() {
        if(activeWorld==player.worldId && chunks.isNotEmpty()) return
        for(buffer in resident.values)GLES20.glDeleteBuffers(1,intArrayOf(buffer),0)
        resident.clear();residentCell=null;mirrorValid=false
        if(configuredWorld!=player.worldId) {
            val toggle=if(player.worldId==DreamWorlds.HOUSE_MEADOW && "corridor:shift" in story.flags)1 else 0
            DreamLands.configure(story.layout(player.worldId) xor toggle);configuredWorld=player.worldId
        }
        shadows?.invalidate()
        val scene=when {
            DreamHorizons.contains(player.worldId)->DreamHorizons.create(player.worldId)
            player.worldId==DreamWaterpark.ID->DreamWaterpark.create()
            DreamPocket.contains(player.worldId)->DreamPocket.create(player.worldId)
            DreamLands.contains(player.worldId)->DreamLands.create(player.worldId)
            player.worldId==DreamWorlds.STATION->DreamJourney.createHub()
            player.worldId==DreamWorlds.POOLS->DreamWorlds.createPools()
            player.worldId==DreamWorlds.CRATER->DreamWorlds.createCrater()
            player.worldId==DreamJourney.DARK->DreamDarkMap.create()
            else->error("Unknown world")
        }
        skySight=if(player.worldId==DreamJourney.DARK || player.worldId==DreamHorizons.DARK)DreamSkySight(scene.vertices)else null
        moonSightTimer=0f
        val decorated=DreamJourney.decorate(player.worldId,scene)
        val props=DreamObjects.geometry(player.worldId,story)
        val additions=DreamEvents.geometry(player.worldId).vertices
        photoSight=DreamSkySight(scene.vertices+props.vertices)
        chunks=DreamChunks.split(DreamWorlds.Scene(decorated.vertices+props.vertices+additions+DreamPhotography.geometry(player.worldId).vertices))
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
        photoFlash=(photoFlash-dt).coerceAtLeast(0f)
        transitionFade=(transitionFade-dt*1.2f).coerceAtLeast(0f)
        eclipse=if(player.worldId==DreamHorizons.CELESTIAL || player.worldId==DreamHorizons.DARK){
            val phase=elapsed%210f
            if(phase in 140f..170f)sin((phase-140f)/30f*kotlin.math.PI.toFloat()).coerceAtLeast(0f)else 0f
        }else 0f
        photoComplete?.let { success ->
            photoComplete=null;photoBusy=false
            if(success){for(key in pendingClues)story.flags.add("photo:"+key);onDreamEvent(if(pendingClues.isEmpty())"Fotografía guardada en el álbum." else "La foto revela algo que no viste: ${pendingClues.joinToString()}. ${DreamPhotography.count(story)}/4.");save()}
            else onDreamEvent("No se pudo guardar la fotografía. Puedes intentarlo otra vez.")
            pendingClues=emptyList()
        }
        for(event in events.step(player.worldId,player.x,player.z,player.yaw,kotlin.math.abs(moveX)+kotlin.math.abs(moveY)>.1f,dt,story)) {
            when(event){"@companion"->onFootstep("wood",.55f,false,.065f);"@piano"->onFootstep("piano",-.5f,true,.15f);else->onDreamEvent(event)}
        }
        saveTimer+=dt
        if(saveTimer>=8f){saveTimer=0f;save()}
        weather.step(player.worldId,player.x,player.z,dt,story.visits[player.worldId]?:1)?.let{onDreamEvent(it)}
        for(notice in actors.step(player.worldId,player.x,player.z,player.yaw,dt,story)) {
            if(notice=="@guide") {
                val dx=actors.soundX-player.x;val dz=actors.soundZ-player.z
                val distance=hypot(dx,dz).coerceAtLeast(1f)
                onFootstep("tone0",((cos(player.yaw)*dx+sin(player.yaw)*dz)/distance).coerceIn(-.8f,.8f),false,(.3f*(1f-distance/85f)).coerceIn(.02f,.25f))
            } else onDreamEvent(notice)
        }
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
        val indoors=DreamLands.indoor(player.worldId,player.x,player.z) || player.worldId==DreamWorlds.POOLS || DreamPocket.contains(player.worldId) || DreamHorizons.indoor(player.worldId,player.x,player.z)
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
        objectNear=if(switchCooldown<=0f)DreamObjects.nearest(player.worldId,player.x,player.z,player.y-1.65f)else null
        if(objectNear!=null)portalTitle=DreamObjects.label(objectNear!!,story)
        nearPortal = portalTarget != null || localDoor!=null || objectNear!=null
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
            corridorRevision=exploration.corridorRevision;story.toggle("corridor:shift");activeWorld=null;save()
        }
        val inDark=player.worldId==DreamJourney.DARK || player.worldId==DreamHorizons.DARK
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
        photoNotice=if(DreamPhotography.targets(player.worldId).any{("photo:"+it.key) !in story.flags && kotlin.math.hypot(player.x-it.x,player.z-it.z)<75f})" · Señal fotográfica cerca" else ""
        routeHint+=photoNotice
        if(DreamLands.contains(player.worldId))routeHint+=" · Recuerdos ${exploration.memoriesFound}/9"
        val travelled=hypot(player.x-lastStepX,player.z-lastStepZ)
        if(travelled<2f)stepDistance+=travelled
        lastStepX=player.x;lastStepZ=player.z
        val grounded=if(inCrater)craterPhysics.grounded else jumpOffset<=.001f
        val stride=if(sprinting)3.5f else 1.85f
        if(grounded && stepDistance>stride && stepCooldown<=0f) {
            stepDistance=0f;stepCooldown=.29f;footPan=-footPan
            val surface=if(inWater)"water" else if(DreamLands.contains(player.worldId))DreamLands.material(player.worldId,player.x,player.z) else "stone"
            val indoor=DreamLands.indoor(player.worldId,player.x,player.z) || player.worldId==DreamWorlds.POOLS || DreamPocket.contains(player.worldId)
            onFootstep(surface,footPan,indoor,if(sprinting).24f else .19f)
        }

        val zone= when {
            DreamHorizons.indoor(player.worldId,player.x,player.z)->"CASA"
            DreamObjects.radioNear(player.worldId,player.x,player.z,story)->"RADIO"
            DreamPocket.contains(player.worldId)->if(player.worldId==DreamPocket.TRAIN)"PROFUNDO" else "CASA"
            player.worldId==DreamWaterpark.ID->"PISCINA_INTERIOR"
            inDark && moonLife.giantVisible -> "ECLIPSE"
            inDark && moonLife.looking && moonLife.vigil>10f -> "OBSERVADO"
            anomalyTimer>0f -> "ANOMALIA"
            DreamLands.contains(player.worldId)->DreamLands.zone(player.worldId,player.x,player.z)
            player.worldId==DreamWorlds.HOUSE_MEADOW || player.worldId==DreamWorlds.HOUSE_CRATER -> "CASA"
            player.worldId==DreamWorlds.CRATER && floor < -31f -> "PROFUNDO"
            player.worldId==DreamWorlds.AQUARIUM && player.z < -85f -> "PROFUNDO"
            player.worldId==DreamWorlds.MEADOW && player.z < -110f -> "UMBRAL"
            player.worldId==DreamWorlds.GARDEN && player.z < -45f -> "UMBRAL"
            player.worldId==DreamWorlds.STATION -> story.flags.firstOrNull{it.startsWith("ending:")}?.substringAfter(':')?:"CALMA"
            else -> "CALMA"
        }
        if(zone != currentZone) { currentZone=zone; onZoneChanged(zone) }
    }

    private val uniformLocations=HashMap<Pair<Int,String>,Int>()
    private val attributeLocations=HashMap<Pair<Int,String>,Int>()
    private fun location(id:Int,key:String)=uniformLocations.getOrPut(id to key){GLES20.glGetUniformLocation(id,key)}
    private fun attribute(id:Int,key:String)=attributeLocations.getOrPut(id to key){GLES20.glGetAttribLocation(id,key)}

    private fun artUniforms(program:Int) {
        GLES20.glUniform1f(location(program,"uUniverse"),(story.universeSeed%89).toFloat())
        GLES20.glUniform1f(location(program,"uEclipse"),eclipse)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE6);GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,planetsTexture);GLES20.glUniform1i(location(program,"uPlanets"),6)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE7);GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,faceTexture);GLES20.glUniform1i(location(program,"uWeeping"),7)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
    }

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
        artUniforms(skyProgram)
        GLES20.glUniform4f(location(skyProgram,"uWeather"),weather.type.toFloat(),weather.strength,weather.anchorX,weather.anchorZ)
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
        when(player.worldId) {
            DreamHorizons.CELESTIAL->{GLES20.glUniform3f(top,.04f,.31f,.70f);GLES20.glUniform3f(bottom,.66f,.80f,.94f)}
            DreamHorizons.CITY->{GLES20.glUniform3f(top,.22f,.31f,.54f);GLES20.glUniform3f(bottom,.94f,.70f,.52f)}
            DreamHorizons.DARK,DreamHorizons.BED->{GLES20.glUniform3f(top,.008f,.018f,.055f);GLES20.glUniform3f(bottom,.08f,.13f,.25f)}
            DreamHorizons.TOWER->{GLES20.glUniform3f(top,.08f,.15f,.30f);GLES20.glUniform3f(bottom,.55f,.57f,.65f)}
        }
        if(player.worldId==DreamWorlds.STATION)when {
            "ending:DESPERTAR" in story.flags->{GLES20.glUniform3f(top,.36f,.60f,.87f);GLES20.glUniform3f(bottom,1f,.79f,.49f)}
            "ending:PERMANECER" in story.flags->{GLES20.glUniform3f(top,.012f,.025f,.09f);GLES20.glUniform3f(bottom,.24f,.26f,.42f)}
            "ending:EL_ECO" in story.flags->{GLES20.glUniform3f(top,.26f,.27f,.48f);GLES20.glUniform3f(bottom,.74f,.66f,.79f)}
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
        artUniforms(sceneProgram)
        GLES20.glUniform1f(location(sceneProgram,"uActorYaw"),0f)
        GLES20.glUniform1f(location(sceneProgram,"uPhoto"),if(photoQueued || photoFlash>0f)1f else 0f)
        GLES20.glUniform4f(location(sceneProgram,"uScene"),events.transform,if("companion:door" in story.flags)1f else 0f,if("companion:cup" in story.flags)1f else 0f,0f)
        GLES20.glUniform3f(location(sceneProgram,"uBus"),events.busX,DreamJourney.surface(player.worldId,events.busX,events.busZ),events.busZ)
        GLES20.glUniform4f(location(sceneProgram,"uWeather"),weather.type.toFloat(),weather.strength,weather.anchorX,weather.anchorZ)
        GLES20.glUniform4f(location(sceneProgram,"uStory"),if("lamp:meadow" in story.flags)1f else 0f,story.layout(player.worldId).toFloat(),if(story.flags.any{it.startsWith("drawer:")})1f else 0f,if(DreamObjects.radioNear(player.worldId,player.x,player.z,story))1f else 0f)
        val lamp=DreamObjects.lamp(player.worldId)
        GLES20.glUniform3f(location(sceneProgram,"uLamp"),lamp.x,DreamJourney.surface(player.worldId,lamp.x,lamp.z)+2.7f,lamp.z)
        shadows?.let { shadow ->
            GLES20.glUniformMatrix4fv(location(sceneProgram,"uLightVP"),1,false,shadow.matrix,0)
            GLES20.glUniform1f(location(sceneProgram,"uShadowReady"),if(shadow.ready)1f else 0f)
            GLES20.glUniform1f(location(sceneProgram,"uShadowTexel"),if(options.quality==2)1f/1024f else 1f/512f)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE5);GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,shadow.texture)
            GLES20.glUniform1i(location(sceneProgram,"uShadow"),5)
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE4);GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,materialTexture)
        GLES20.glUniform1i(location(sceneProgram,"uMaterial"),4)
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
        val nightLight=DreamLands.night(player.worldId)||player.worldId==DreamJourney.DARK||player.worldId==DreamHorizons.DARK||player.worldId==DreamHorizons.BED
        GLES20.glUniform3f(location(sceneProgram,"uSun"),if(nightLight).10f else -.40f,if(nightLight).65f else .82f,if(nightLight)-.80f else .40f)
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
        GLES20.glEnable(GLES20.GL_BLEND);GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE_MINUS_SRC_ALPHA)
        for((key,buffer) in resident)drawBuffer(buffer,chunks.getValue(key).size/10)
        if(exploration.presenceAlpha>.001f) {
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE_MINUS_SRC_ALPHA)
            GLES20.glDepthMask(false)
            drawBuffer(figureBuffer,figureCount)
            GLES20.glDepthMask(true)
            GLES20.glDisable(GLES20.GL_BLEND)
        }
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE_MINUS_SRC_ALPHA)
        for(a in actors.actors)if(a.alpha>.01f) {
            val y=DreamJourney.surface(player.worldId,a.x,a.z)+(if(a.role=="window" && player.worldId==DreamExpansion.CLOUD_HOTEL)1.2f else 0f)
            GLES20.glUniform3f(location(sceneProgram,"uEntity"),a.x,y,a.z)
            GLES20.glUniform1f(location(sceneProgram,"uPresence"),a.alpha)
            GLES20.glUniform1f(location(sceneProgram,"uActorYaw"),-kotlin.math.atan2(player.x-a.x,player.z-a.z))
            drawBuffer(if(a.role=="mourner")mournerBuffer else if(a.role=="window")headBuffer else figureBuffer,if(a.role=="mourner")mournerCount else if(a.role=="window")headCount else figureCount)
        }
        if(player.worldId==DreamWorlds.MEADOW && events.busVisible) {
            drawBuffer(busBuffer,busCount)
            if(events.busAge<15f) {
                GLES20.glUniform3f(location(sceneProgram,"uEntity"),35f-(if(events.busAge>10f)(events.busAge-10f).coerceAtMost(4f)*2.2f else 0f),DreamJourney.surface(player.worldId,35f,-250f),-250f)
                GLES20.glUniform1f(location(sceneProgram,"uPresence"),(15f-events.busAge).coerceIn(0f,1f))
                GLES20.glUniform1f(location(sceneProgram,"uActorYaw"),1.57f)
                drawBuffer(figureBuffer,figureCount)
            }
        }
        if(actors.traceLife>0f) {
            GLES20.glUniform3f(location(sceneProgram,"uEntity"),actors.traceX,DreamJourney.surface(player.worldId,actors.traceX,actors.traceZ),actors.traceZ)
            drawBuffer(traceBuffer,traceCount)
        }
        if(weather.strength>.01f && weather.type in 1..3) {
            GLES20.glDepthMask(false)
            GLES20.glUniform3f(location(sceneProgram,"uEntity"),weather.anchorX,DreamJourney.surface(player.worldId,weather.anchorX,weather.anchorZ),weather.anchorZ)
            if(weather.type==1)drawBuffer(rainBuffer,rainCount)
            if(weather.type==3)drawBuffer(skyHouseBuffer,skyHouseCount)
            GLES20.glDepthMask(true)
        }
        GLES20.glDisable(GLES20.GL_BLEND)
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
        if(appliedQuality!=options.quality) {
            appliedQuality=options.quality
            GLES20.glDeleteTextures(2,intArrayOf(atlasTexture,materialTexture),0)
            val maps=DreamSurfaceTextures.load(context,options.quality);atlasTexture=maps.first;materialTexture=maps.second
            shadows?.configure(if(enhanced)options.quality else 0)
        }
        shadows?.render(elapsed,player.x,player.y,player.z,DreamLands.night(player.worldId)||player.worldId==DreamJourney.DARK||player.worldId==DreamHorizons.DARK||player.worldId==DreamHorizons.BED,width,height){ id ->
            val pos=GLES20.glGetAttribLocation(id,"aPos");val kind=GLES20.glGetAttribLocation(id,"aKind")
            GLES20.glUniform3f(GLES20.glGetUniformLocation(id,"uEntity"),0f,0f,0f)
            for((key,buffer)in resident) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,buffer)
                GLES20.glEnableVertexAttribArray(pos);GLES20.glVertexAttribPointer(pos,3,GLES20.GL_FLOAT,false,40,0)
                GLES20.glEnableVertexAttribArray(kind);GLES20.glVertexAttribPointer(kind,1,GLES20.GL_FLOAT,false,40,36)
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,chunks.getValue(key).size/10)
            }
            GLES20.glDisableVertexAttribArray(pos);GLES20.glDisableVertexAttribArray(kind)
        }
        val mirror=DreamLands.mirror(player.worldId)
        val mirrorNear=mirror!=null && abs(player.x-mirror.x)<65f && player.z>mirror.z+.35f && player.z<mirror.z+70f
        if(!mirrorNear)mirrorValid=false
        if(options.quality>0 && mirrorNear && mirrorFbo!=0 && elapsed-mirrorTime>.10f) {
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
        if(photoQueued) {
            photoQueued=false;photoFlash=.25f
            val temporary=DreamChronicle();temporary.flags.addAll(story.flags)
            pendingClues=DreamPhotography.photograph(player.worldId,player.x,player.y,player.z,player.yaw,pitch,temporary){ target,distance ->
                photoSight?.clear(player.x,player.y,player.z,(target.x-player.x)/distance,(target.y-player.y)/distance,(target.z-player.z)/distance,distance-.8f)?:true
            }
            photoStore.capture(width,height){success->photoComplete=success}
        }
    }
}
