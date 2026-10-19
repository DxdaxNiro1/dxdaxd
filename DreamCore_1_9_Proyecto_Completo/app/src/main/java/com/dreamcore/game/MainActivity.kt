package com.dreamcore.game

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import kotlin.math.hypot
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : Activity() {
    private lateinit var gameView: GLSurfaceView
    private lateinit var renderer: DreamRenderer
    private lateinit var controls: ControlsView
    private var audio: DreamAudio? = null
    private var footsteps:DreamFootsteps?=null
    private var footstepMode=1
    private lateinit var saves:DreamSaveStore
    private val options=DreamOptions()
    private var startupWarning: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        // El audio nunca debe impedir que se abra el mundo 3D.
        try {
            audio = DreamAudio(this)
            footstepMode=getPreferences(MODE_PRIVATE).getInt("footstep_mode",1).coerceIn(0,2)
            footsteps=DreamFootsteps(this)
            footsteps?.userGain=when(footstepMode){0->0f;2->1.65f;else->1f}
        } catch (e: Exception) {
            Log.e("DreamCore", "Fallo al iniciar el audio", e)
            startupWarning = "AUDIO: ${e.javaClass.simpleName}: ${e.message.orEmpty()}"
        }
        saves=DreamSaveStore(this)
        val prefs=getPreferences(MODE_PRIVATE)
        options.sensitivity=prefs.getFloat("sensitivity",1f).coerceIn(.3f,2.5f)
        options.music=prefs.getFloat("music",.70f).coerceIn(0f,1f)
        options.ambience=prefs.getFloat("ambience",.65f).coerceIn(0f,1f)
        options.quality=prefs.getInt("quality",1).coerceIn(0,2)
        audio?.levels(options.music,options.ambience)
        footsteps?.ambienceGain=options.ambience
        renderer = DreamRenderer(this, { world ->
            // Los callbacks del motor 3D llegan desde su propio hilo.
            runOnUiThread {
                controls.resetControls()
                controls.showMessage(
                    when(world) {
                        DreamWorlds.STATION -> "Izquierda: Dreamcore. Derecha: Dark Fantasy."
                        DreamWorlds.CRATER -> "Sigue hacia el fondo: la casa continúa el viaje."
                        DreamJourney.DARK -> "Las lunas miran hacia la catedral."
                        else -> DreamJourney.hudTitle(world)
                    }
                )
                try {
                    audio?.changeWorld(world)
                } catch (e: Exception) {
                    reportProblem("AUDIO", e)
                    audio = null
                }
            }
        }, { zone ->
            // Cambiamos capas de sonido en el hilo principal, no en OpenGL.
            runOnUiThread {
                try { audio?.setZone(zone) } catch (e: Exception) {
                    reportProblem("AUDIO ZONA", e)
                    audio = null
                }
            }
        }, { notice ->
            runOnUiThread { if (::controls.isInitialized) controls.showMessage(notice) }
        }, { surface,pan,inside,level ->
            runOnUiThread { footsteps?.play(surface,pan,inside,level) }
        },saves.load(),options,{snapshot,flush->saves.save(snapshot,flush)})
        gameView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(2)
            setEGLConfigChooser(DreamEglConfigChooser())
            setRenderer(SafeRenderer(renderer))
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            preserveEGLContextOnPause = true
        }
        controls = ControlsView()
        val container = FrameLayout(this)
        container.addView(gameView)
        container.addView(controls)
        setContentView(container)
        controls.showMessage("Elige una experiencia: Dreamcore o Dark Fantasy.")
        startupWarning?.let { controls.showError(it) }
    }

    override fun onResume() {
        super.onResume()
        if(::gameView.isInitialized) {
            gameView.queueEvent{renderer.resetFrameClock()}
            gameView.onResume()
        }
        footsteps?.resume()
        try { audio?.resume() } catch (e: Exception) {
            reportProblem("AUDIO", e)
            audio = null
        }
    }

    override fun onPause() {
        if(::controls.isInitialized) controls.resetControls()
        footsteps?.pause()
        try { audio?.pause() } catch (e: Exception) { Log.e("DreamCore", "Audio pause", e) }
        if(::gameView.isInitialized) {
            val done=java.util.concurrent.CountDownLatch(1)
            gameView.queueEvent{try{renderer.save(true)}finally{done.countDown()}}
            try{done.await(900,java.util.concurrent.TimeUnit.MILLISECONDS)}catch(_:InterruptedException){Thread.currentThread().interrupt()}
            gameView.onPause()
        }
        super.onPause()
    }

    override fun onDestroy() {
        if(::renderer.isInitialized)renderer.close()
        if(::saves.isInitialized)saves.close()
        footsteps?.release()
        try { audio?.release() } catch (e: Exception) { Log.e("DreamCore", "Audio release", e) }
        super.onDestroy()
    }


    private fun showAlbum() {
        val photos=java.io.File(filesDir,"dream_photos").listFiles()?.filter{it.extension=="jpg"}?.sortedByDescending{it.lastModified()}.orEmpty()
        if(photos.isEmpty()){android.app.AlertDialog.Builder(this).setMessage("Todavía no hay fotografías. Usa FOTO mientras exploras.").setPositiveButton("Volver",null).show();return}
        android.app.AlertDialog.Builder(this).setTitle("Tus últimas ${photos.size} fotografías").setItems(photos.mapIndexed{i,_->"Fotografía ${i+1}"}.toTypedArray()){_,index->
            val bitmap=android.graphics.BitmapFactory.decodeFile(photos[index].path,android.graphics.BitmapFactory.Options().apply{inSampleSize=2})
            val view=android.widget.ImageView(this).apply{setImageBitmap(bitmap);adjustViewBounds=true}
            android.app.AlertDialog.Builder(this).setView(view).setPositiveButton("Cerrar",null).create().apply{setOnDismissListener{view.setImageDrawable(null);bitmap?.recycle()};show()}
        }.setNegativeButton("Cerrar",null).show()
    }

    private fun showSettings() {
        val box=android.widget.LinearLayout(this).apply{orientation=android.widget.LinearLayout.VERTICAL;setPadding(28,12,28,12)}
        fun persist(){getPreferences(MODE_PRIVATE).edit().putFloat("sensitivity",options.sensitivity).putFloat("music",options.music).putFloat("ambience",options.ambience).putInt("quality",options.quality).putInt("footstep_mode",footstepMode).apply()}
        fun slider(name:String,value:Float,change:(Float)->Unit) {
            val label=android.widget.TextView(this).apply{text=name};box.addView(label)
            box.addView(android.widget.SeekBar(this).apply{
                max=100;progress=(value*100).toInt()
                setOnSeekBarChangeListener(object:android.widget.SeekBar.OnSeekBarChangeListener{
                    override fun onStartTrackingTouch(b:android.widget.SeekBar){}
                    override fun onStopTrackingTouch(b:android.widget.SeekBar){persist()}
                    override fun onProgressChanged(b:android.widget.SeekBar,n:Int,user:Boolean){if(user){change(n/100f);audio?.levels(options.music,options.ambience);footsteps?.ambienceGain=options.ambience}}
                })
            })
        }
        slider("Sensibilidad de cámara",(options.sensitivity-.3f)/2.2f){options.sensitivity=.3f+2.2f*it}
        slider("Música",options.music){options.music=it}
        slider("Ambiente y objetos",options.ambience){options.ambience=it}
        box.addView(android.widget.Button(this).apply{
            text="Calidad: "+listOf("Baja","Media","Alta")[options.quality]
            setOnClickListener{options.quality=(options.quality+1)%3;text="Calidad: "+listOf("Baja","Media","Alta")[options.quality];persist()}
        })
        box.addView(android.widget.Button(this).apply{
            text="Pasos: "+listOf("Silencio","Suaves","Normales")[footstepMode]
            setOnClickListener{footstepMode=(footstepMode+1)%3;text="Pasos: "+listOf("Silencio","Suaves","Normales")[footstepMode];footsteps?.userGain=when(footstepMode){0->0f;2->1.65f;else->1f};persist()}
        })
        box.addView(android.widget.TextView(this).apply{text=renderer.journal})
        val scroll=android.widget.ScrollView(this).apply{addView(box)}
        android.app.AlertDialog.Builder(this).setTitle("Comodidad y recuerdos").setView(scroll).setPositiveButton("Volver"){_,_->persist()}.setNeutralButton("Álbum"){_,_->showAlbum()}.show()
    }

    /** Si un shader no funciona en este celular, muestra la causa en pantalla
     * en lugar de cerrar inesperadamente toda la aplicación. */
    private inner class SafeRenderer(
        private val delegate: GLSurfaceView.Renderer
    ) : GLSurfaceView.Renderer {
        private var failed = false

        private fun handle(where: String, e: Exception) {
            if (failed) return
            failed = true
            reportProblem(where, e)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            failed = false
            try {
                delegate.onSurfaceCreated(gl, config)
            } catch (e: Exception) {
                handle("OPENGL INICIO", e)
            }
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            if (failed) return
            try {
                delegate.onSurfaceChanged(gl, width, height)
            } catch (e: Exception) {
                handle("OPENGL TAMAÑO", e)
            }
        }

        override fun onDrawFrame(gl: GL10?) {
            if (failed) {
                GLES20.glClearColor(0.36f, 0.20f, 0.48f, 1f)
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                return
            }
            try {
                delegate.onDrawFrame(gl)
            } catch (e: Exception) {
                handle("OPENGL DIBUJO", e)
            }
        }
    }

    private fun reportProblem(where: String, e: Exception) {
        Log.e("DreamCore", "$where: ${e.message}", e)
        val message = "$where: ${e.javaClass.simpleName}: ${e.message.orEmpty()}"
        runOnUiThread {
            if (::controls.isInitialized) controls.showError(message)
            else startupWarning = message
        }
    }

    private inner class ControlsView : View(this@MainActivity) {
        private val d = resources.displayMetrics.density
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var stickPointer = -1
        private var cameraPointer = -1
        private var sprintPointer = -1
        private var lastCameraX = 0f
        private var lastCameraY = 0f
        private var stickX = 0f
        private var stickY = 0f
        private var message = ""
        private var messageUntil = 0L
        private var errorMessage: String? = null

        private val jX get() = width * .16f
        private val jY get() = height * .74f
        private val radius get() = (height * .155f).coerceAtLeast(46f*d)
        private val actionX get() = width * .91f
        private val actionY get() = height * .43f
        private val actionRadius get() = (height*.086f).coerceIn(28f*d,40f*d)
        private val sprintX get() = width * .89f
        private val sprintY get() = height * .80f
        private val jumpX get() = width * .73f
        private val jumpY get() = height * .79f
        private val keyRadius get() = (height*.085f).coerceIn(28f*d,39f*d)

        fun showMessage(text: String) {
            message = text
            messageUntil = SystemClock.uptimeMillis() + 6500L
            invalidate()
        }

        fun showError(message: String) {
            errorMessage = message.take(240)
            invalidate()
        }

        private fun setStick(x: Float,y: Float) {
            val dx=x-jX; val dy=y-jY
            val length=hypot(dx,dy)
            val scale=if(length>radius) radius/length else 1f
            stickX=dx*scale/radius
            stickY=dy*scale/radius
            renderer.moveX=stickX
            renderer.moveY=-stickY
            invalidate()
        }

        fun resetControls() {
            stickPointer=-1
            cameraPointer=-1
            sprintPointer=-1
            stickX=0f; stickY=0f
            renderer.moveX=0f; renderer.moveY=0f
            renderer.sprinting=false; renderer.jumpQueued=false
            invalidate()
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val index=event.actionIndex
            when(event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    val id=event.getPointerId(index)
                    val x=event.getX(index); val y=event.getY(index)
                    if(x>width-145f*d && y<52f*d) {
                        resetControls();showSettings()
                    } else if(x>width-245f*d && x<width-150f*d && y<52f*d) {
                        gameView.queueEvent{renderer.takePhoto()}
                    } else if(renderer.nearPortal && hypot(x-actionX,y-actionY)<actionRadius*1.18f) {
                        gameView.queueEvent { renderer.interact() }
                    } else if (hypot(x-jumpX,y-jumpY)<keyRadius*1.25f) {
                        renderer.jumpQueued=true
                    } else if (hypot(x-sprintX,y-sprintY)<keyRadius*1.25f) {
                        sprintPointer=id
                        renderer.sprinting=true
                    } else if(x < width*.47f && stickPointer==-1) {
                        stickPointer=id
                        setStick(x,y)
                    } else if(cameraPointer==-1) {
                        cameraPointer=id
                        lastCameraX=x; lastCameraY=y
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    for(i in 0 until event.pointerCount) {
                        val id=event.getPointerId(i)
                        val x=event.getX(i); val y=event.getY(i)
                        if(id==stickPointer) {
                            setStick(x,y)
                        } else if(id==cameraPointer) {
                            val dx=x-lastCameraX
                            val dy=y-lastCameraY
                            lastCameraX=x; lastCameraY=y
                            gameView.queueEvent { renderer.turnCamera(dx,dy) }
                        }
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    val id=event.getPointerId(index)
                    if(id==stickPointer) {
                        stickPointer=-1
                        stickX=0f; stickY=0f
                        renderer.moveX=0f; renderer.moveY=0f
                    }
                    if(id==cameraPointer) cameraPointer=-1
                    if(id==sprintPointer) {
                        sprintPointer=-1; renderer.sprinting=false
                    }
                }
                MotionEvent.ACTION_CANCEL -> resetControls()
            }
            invalidate()
            return true
        }

        private fun text(value: String, x: Float, y: Float, size: Float,
                         alpha: Int = 225, align: Paint.Align = Paint.Align.LEFT) {
            paint.style=Paint.Style.FILL
            paint.color=Color.argb(alpha,255,255,255)
            paint.textSize=size*d
            paint.textAlign=align
            paint.setShadowLayer(3f*d,0f,1f*d,Color.argb(160,20,19,50))
            canvasText(value,x,y)
            paint.clearShadowLayer()
        }
        // Se usa únicamente durante onDraw, por lo que Canvas existe aquí.
        private lateinit var activeCanvas: Canvas
        private fun canvasText(value: String,x: Float,y: Float) {
            activeCanvas.drawText(value,x,y,paint)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            activeCanvas=canvas
            if(renderer.transitionFade>.001f){paint.style=Paint.Style.FILL;paint.color=Color.argb((renderer.transitionFade*245).toInt(),13,18,32);canvas.drawRect(0f,0f,width.toFloat(),height.toFloat(),paint)}
            val title=DreamJourney.hudTitle(renderer.currentWorld)
            text(if(renderer.currentWorld==DreamJourney.DARK || renderer.currentWorld==DreamHorizons.DARK) "DARK FANTASY  ·  1.9" else "DREAM CORE  ·  1.9",17f*d,26f*d,14f,240)
            text(title,17f*d,47f*d,11f,215)
            if(renderer.routeHint.isNotEmpty())text(renderer.routeHint,17f*d,65f*d,10f,150)
            paint.style=Paint.Style.FILL
            paint.color=Color.argb(38,180,175,210)
            canvas.drawRoundRect(RectF(width-145f*d,8f*d,width-10f*d,42f*d),9f*d,9f*d,paint)
            text("AJUSTES · DIARIO",width-77f*d,30f*d,10f,190,Paint.Align.CENTER)
            paint.style=Paint.Style.FILL;paint.color=Color.argb(65,80,93,123)
            canvas.drawRoundRect(RectF(width-245f*d,8f*d,width-153f*d,42f*d),9f*d,9f*d,paint)
            text("FOTO",width-199f*d,30f*d,11f,220,Paint.Align.CENTER)
            if(messageUntil>SystemClock.uptimeMillis()) {
                paint.textSize=16f*d
                val lines=ArrayList<String>();var line=""
                for(word in message.split(' ')) {
                    val next=if(line.isEmpty())word else "$line $word"
                    if(paint.measureText(next)>width*.82f && line.isNotEmpty()){lines+=line;line=word}else line=next
                }
                if(line.isNotEmpty())lines+=line
                val top=maxOf(87f*d,height*.18f)
                for((i,value)in lines.take(3).withIndex())text(value,width*.5f,top+i*20f*d,16f,238,Paint.Align.CENTER)
            }
            // Puntero muy discreto en el centro de la mirada.
            paint.style=Paint.Style.FILL
            paint.color=Color.argb(125,255,255,255)
            canvas.drawCircle(width*.5f,height*.5f,2.3f*d,paint)
            // Joystick original de la versión 0.3, ahora con aspecto suave.
            paint.style=Paint.Style.FILL
            paint.color=Color.argb(45,244,232,255)
            canvas.drawCircle(jX,jY,radius,paint)
            paint.style=Paint.Style.STROKE
            paint.strokeWidth=2.1f*d
            paint.color=Color.argb(132,255,255,255)
            canvas.drawCircle(jX,jY,radius,paint)
            paint.style=Paint.Style.FILL
            paint.color=Color.argb(172,255,255,255)
            canvas.drawCircle(jX+stickX*radius,jY+stickY*radius,radius*.40f,paint)
            text("MOVER",jX,jY-radius-10f*d,11f,193,Paint.Align.CENTER)
            // Botones nuevos: combinables con el joystick y la cámara multitáctil.
            fun key(cx:Float,cy:Float,label:String,pressed:Boolean) {
                paint.style=Paint.Style.FILL
                paint.color=Color.argb(if(pressed)155 else 72,146,117,185)
                canvas.drawCircle(cx,cy,keyRadius,paint)
                paint.style=Paint.Style.STROKE
                paint.strokeWidth=1.9f*d
                paint.color=Color.argb(210,245,239,255)
                canvas.drawCircle(cx,cy,keyRadius,paint)
                text(label,cx,cy+4f*d,11f,245,Paint.Align.CENTER)
            }
            key(jumpX,jumpY,"SALTAR",false)
            key(sprintX,sprintY,"CORRER",renderer.sprinting)
            if(renderer.nearPortal) {
                paint.style=Paint.Style.FILL
                paint.color=Color.argb(100,90,65,146)
                canvas.drawCircle(actionX,actionY,actionRadius,paint)
                paint.style=Paint.Style.STROKE
                paint.strokeWidth=2.5f*d
                paint.color=Color.argb(220,255,255,255)
                canvas.drawCircle(actionX,actionY,actionRadius,paint)
                text("USAR",actionX,actionY+5f*d,12f,255,Paint.Align.CENTER)
                text(renderer.portalTitle,width*.5f,
                    height*.84f,13f,230,Paint.Align.CENTER)
            } else {
                text("MIRAR",width*.86f,height*.90f,11f,185,Paint.Align.CENTER)
            }
            // Mensaje de diagnóstico visible para facilitar la captura de pantalla.
            errorMessage?.let { error ->
                paint.style = Paint.Style.FILL
                paint.color = Color.argb(230, 50, 18, 55)
                canvas.drawRect(0f, height*.25f, width.toFloat(), height*.69f, paint)
                text("ERROR DETECTADO - envia captura a ChatGPT",
                    width*.5f, height*.34f, 13f, 255, Paint.Align.CENTER)
                val lines = error.chunked(55).take(4)
                lines.forEachIndexed { index, line ->
                    text(line, width*.5f, height*.42f+index*20f*d,
                        11f, 255, Paint.Align.CENTER)
                }
            }
            // Actualiza el aviso de portal aun cuando el dedo esté quieto.
            postInvalidateDelayed(90L)
        }
    }
}
