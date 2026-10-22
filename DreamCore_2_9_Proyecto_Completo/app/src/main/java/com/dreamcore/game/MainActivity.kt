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
    private var foreground=false
    private lateinit var session:DreamSession
    private lateinit var home:View
    private lateinit var container:FrameLayout
    private var modalDepth=0
    private var bufferW=0;private var bufferH=0
    private var powerSourceW=0;private var powerSourceH=0;private var lastPowerScale=Float.NaN;private var lastPowerLimit=0
    private fun applyPower(){
        if(!::gameView.isInitialized||gameView.width<=0||gameView.height<=0)return
        val scale=DreamPower.scale(options.powerMode,options.thermalCap)*options.runtimeScale
        if(powerSourceW==gameView.width && powerSourceH==gameView.height && lastPowerScale==scale && lastPowerLimit==options.runtimeLimit)return
        powerSourceW=gameView.width;powerSourceH=gameView.height;lastPowerScale=scale;lastPowerLimit=options.runtimeLimit
        val fitted=DreamCompatibility29.fit(gameView.width,gameView.height,scale,options.runtimeLimit)
        val w=fitted.first;val h=fitted.second
        if(w!=bufferW||h!=bufferH){bufferW=w;bufferH=h;surfaceRateHint=-1;gameView.holder.setFixedSize(w,h)}
    }
    private val pacer=DreamPerformance.Pacer()
    private val framePending=java.util.concurrent.atomic.AtomicBoolean(false)
    private val frameBudget=DreamCompatibility29.Budget()
    private var lastDrawStart=0L
    private var surfaceRateHint=-1
    private fun matchSurfaceRate(fps:Int){
        if(android.os.Build.VERSION.SDK_INT<30 || surfaceRateHint==fps || !gameView.holder.surface.isValid)return
        surfaceRateHint=fps
        try{gameView.holder.surface.setFrameRate(fps.toFloat(),android.view.Surface.FRAME_RATE_COMPATIBILITY_DEFAULT)}catch(_:RuntimeException){}
    }

    private var thermal:DreamThermalMonitor?=null
    private val framePump=object:android.view.Choreographer.FrameCallback {
        override fun doFrame(time:Long){
            if(!foreground || modalDepth>0 || session.atTitle)return
            val fps=DreamPower.fps(options.fps,options.thermalCap,options.powerMode)
            matchSurfaceRate(fps)
            applyPower()
            if(pacer.due(time,fps) && framePending.compareAndSet(false,true))gameView.requestRender()
            android.view.Choreographer.getInstance().postFrameCallback(this)
        }
    }
    private fun startFrames(){android.view.Choreographer.getInstance().removeFrameCallback(framePump);pacer.reset();surfaceRateHint=-1;if(foreground&&modalDepth==0&&!session.atTitle)android.view.Choreographer.getInstance().postFrameCallback(framePump)}
    private fun showPaused(dialog:android.app.AlertDialog,cleanup:()->Unit={}) {
        controls.resetControls();modalDepth++;android.view.Choreographer.getInstance().removeFrameCallback(framePump)
        dialog.setOnDismissListener{cleanup();modalDepth=(modalDepth-1).coerceAtLeast(0);gameView.queueEvent{renderer.resetFrameClock()};controls.invalidate();startFrames()};dialog.show()
    }

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
        session=DreamSession(saves.hasProgress())
        val prefs=getPreferences(MODE_PRIVATE)
        options.sensitivity=prefs.getFloat("sensitivity",1f).coerceIn(.3f,2.9f)
        options.music=prefs.getFloat("music",.70f).coerceIn(0f,1f)
        options.ambience=prefs.getFloat("ambience",.65f).coerceIn(0f,1f)
        options.quality=prefs.getInt("quality",1).coerceIn(0,2)
        options.fps=if(prefs.getInt("fps",30)==60)60 else 30
        options.powerMode=prefs.getInt("power_mode",0).coerceIn(0,2)
        options.compatibility=prefs.getInt("compatibility",0).coerceIn(0,2)
        thermal=DreamThermalMonitor(this){options.thermalCap=it;runOnUiThread{applyPower()}}
        audio?.levels(options.music,options.ambience)
        footsteps?.ambienceGain=options.ambience
        renderer = DreamRenderer(this, { world ->
            // Los callbacks del motor 3D llegan desde su propio hilo.
            runOnUiThread {
                controls.resetControls()
                controls.showMessage(
                    when(world) {
                        DreamWorlds.STATION -> "Dreamcore y Dark Fantasy al frente. Atrás: Backrooms y Desvíos. También puedes elegir una ruta en Ajustes."
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
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            preserveEGLContextOnPause = true
        }
        gameView.addOnLayoutChangeListener{_,_,_,_,_,_,_,_,_->applyPower()}
        gameView.post{applyPower()}
        controls = ControlsView()
        container = FrameLayout(this)
        container.addView(gameView)
        container.addView(controls)
        setContentView(container)
        showHome()
        controls.showMessage("Elige una experiencia: Dreamcore o Dark Fantasy.")
        startupWarning?.let { controls.showError(it) }
    }

    override fun onResume() {
        super.onResume()
        if(::gameView.isInitialized) {
            gameView.queueEvent{renderer.resetFrameClock()}
            gameView.onResume()
        }
        foreground=true;controls.invalidate();startFrames()
        if(!session.atTitle)footsteps?.resume()
        try { audio?.resume() } catch (e: Exception) {
            reportProblem("AUDIO", e)
            audio = null
        }
    }

    override fun onPause() {
        foreground=false;android.view.Choreographer.getInstance().removeFrameCallback(framePump)
        if(::controls.isInitialized) controls.resetControls()
        footsteps?.pause()
        try { audio?.pause() } catch (e: Exception) { Log.e("DreamCore", "Audio pause", e) }
        if(::gameView.isInitialized) {
            val done=java.util.concurrent.CountDownLatch(1)
            gameView.queueEvent{try{if(session.maySave)renderer.save(true)}finally{done.countDown()}}
            try{done.await(900,java.util.concurrent.TimeUnit.MILLISECONDS)}catch(_:InterruptedException){Thread.currentThread().interrupt()}
            gameView.onPause()
        }
        super.onPause()
    }

    override fun onDestroy() {
        thermal?.close()
        if(::renderer.isInitialized)renderer.close()
        if(::saves.isInitialized)saves.close()
        footsteps?.release()
        try { audio?.release() } catch (e: Exception) { Log.e("DreamCore", "Audio release", e) }
        super.onDestroy()
    }



    private fun showHome(){
        session.home();controls.resetControls();controls.visibility=View.GONE
        android.view.Choreographer.getInstance().removeFrameCallback(framePump)
        renderer.suspended=true;audio?.titleScreen(true);if(foreground)audio?.resume();footsteps?.pause()
        if(::home.isInitialized)container.removeView(home)
        val density=resources.displayMetrics.density
        fun dp(v:Int)=(v*density).toInt()
        val layout=android.widget.LinearLayout(this).apply{
            orientation=android.widget.LinearLayout.HORIZONTAL;gravity=android.view.Gravity.CENTER
            setPadding(dp(28),dp(16),dp(28),dp(16))
            background=android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TL_BR,intArrayOf(Color.rgb(12,27,37),Color.rgb(46,49,70),Color.rgb(89,67,82)))
        }
        val intro=android.widget.LinearLayout(this).apply{orientation=android.widget.LinearLayout.VERTICAL;setPadding(0,0,dp(32),0)}
        intro.addView(android.widget.TextView(this).apply{text="DREAM CORE";textSize=30f;setTextColor(Color.rgb(236,235,218));if(android.os.Build.VERSION.SDK_INT>=21)letterSpacing=.14f})
        intro.addView(android.widget.TextView(this).apply{text="LUZ DE LOS SUEÑOS  ·  2.9\n\nCada puerta recuerda un lugar.\nTómate tu tiempo para encontrarlo.";textSize=16f;setTextColor(Color.rgb(186,208,207))})
        layout.addView(intro,android.widget.LinearLayout.LayoutParams(0,-2,1f))
        val buttons=android.widget.LinearLayout(this).apply{orientation=android.widget.LinearLayout.VERTICAL}
        fun button(label:String,enabled:Boolean=true,action:()->Unit){buttons.addView(android.widget.Button(this).apply{text=label;isEnabled=enabled;isAllCaps=false;minHeight=dp(48);setOnClickListener{action()}},android.widget.LinearLayout.LayoutParams(-1,-2))}
        button("Continuar partida",session.canContinue){beginSession(false)}
        button("Nueva partida"){
            if(session.canContinue)showPaused(android.app.AlertDialog.Builder(this).setTitle("¿Comenzar un sueño nuevo?").setMessage("Se reemplazará el progreso de la partida. Tus fotografías del álbum y tus ajustes se conservan.").setPositiveButton("Nueva partida"){_,_->beginSession(true)}.setNegativeButton("Conservar partida",null).create())
            else beginSession(true)
        }
        button("Ajustes"){showSettings()}
        button("Sala de escucha · 52 músicas"){showMusicRoom()}
        buttons.addView(android.widget.TextView(this).apply{text=if(session.canContinue)"Guardado automático · listo para continuar" else "Un viaje nuevo te espera";textSize=12f;setTextColor(Color.rgb(200,207,215));setPadding(dp(8),dp(8),0,0)})
        val scroll=android.widget.ScrollView(this).apply{addView(buttons);isFillViewport=false}
        layout.addView(scroll,android.widget.LinearLayout.LayoutParams(0,-2,.85f))
        home=layout;container.addView(home,FrameLayout.LayoutParams(-1,-1))
    }

    private fun beginSession(fresh:Boolean){
        if(fresh)session.newGame() else if(!session.continueGame())return
        audio?.titleScreen(false)
        container.removeView(home);controls.visibility=View.VISIBLE
        gameView.queueEvent{
            if(fresh)renderer.newGame{saves.replace(it)}
            renderer.resumeJourney()
            runOnUiThread{if(foreground&&!session.atTitle){audio?.resume();footsteps?.resume();startFrames()}}
        }
    }

    private fun showRoutes(){
        val names=arrayOf("Dreamcore · recorrido principal (13 mundos)","Dark Fantasy · 4 mundos y lunas vivas","Los lugares imposibles · 11 mundos","La isla de la casa rosa · nuevo sueño","Pradera de ojos · VISTA PREVIA SIN ATAQUES","Mi refugio · objetos y fotografías","El pueblo · investigar la llamada","Backrooms · las luces que no descansan","Las aberturas del cielo","Castillo y corredores de nubes","La ciudad de las flores","La pradera de las grietas","Dark Fantasy III · pueblo escarlata","Dark Fantasy IV · castillo pálido","Santuario entre nubes · sin tobogán")
        showPaused(android.app.AlertDialog.Builder(this).setTitle("Elige una ruta").setItems(names){_,i->
            gameView.queueEvent{renderer.goRoute(i)}
        }.setNegativeButton("Seguir explorando",null).create())
    }

    @Deprecated("Android legacy back callback")
    override fun onBackPressed(){if(session.atTitle)super.onBackPressed() else showSettings()}

    private fun showMusicRoom(){
        val tracks=DreamMusic24.tracks
        var index=tracks.indexOfFirst{it.world==(if(session.atTitle)DreamMusic24.TITLE else renderer.currentWorld)}.coerceAtLeast(0)
        var playing=true
        val box=android.widget.LinearLayout(this).apply{orientation=android.widget.LinearLayout.VERTICAL;setPadding(20,8,20,8)}
        val heading=android.widget.TextView(this).apply{textSize=17f;setPadding(0,8,0,8)}
        val cover=android.widget.ImageView(this).apply{adjustViewBounds=true;maxHeight=(resources.displayMetrics.density*145).toInt();scaleType=android.widget.ImageView.ScaleType.FIT_CENTER}
        val description=android.widget.TextView(this).apply{textSize=13f}
        val spinner=android.widget.Spinner(this)
        spinner.adapter=android.widget.ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,tracks.map{"%02d · %s".format(it.number,it.title)})
        val row=android.widget.LinearLayout(this).apply{orientation=android.widget.LinearLayout.HORIZONTAL}
        val toggle=android.widget.Button(this).apply{text="Pausa";isAllCaps=false}
        fun select(){
            val track=tracks[index];playing=true;toggle.text="Pausa"
            heading.text="${track.title}\n${track.map}"
            description.text="${track.seconds/60}:${"%02d".format(track.seconds%60)} · ${track.description}\nVista del escenario renderizada con el motor del juego."
            val resource=resources.getIdentifier(track.cover,"drawable",packageName)
            if(resource!=0)cover.setImageResource(resource) else cover.setImageDrawable(null)
            cover.contentDescription=track.map
            audio?.audition(track)
        }
        fun navigate(delta:Int){index=(index+delta+tracks.size)%tracks.size;spinner.setSelection(index)}
        val previous=android.widget.Button(this).apply{text="Anterior";isAllCaps=false;setOnClickListener{navigate(-1)}}
        val next=android.widget.Button(this).apply{text="Siguiente";isAllCaps=false;setOnClickListener{navigate(1)}}
        toggle.setOnClickListener{playing=!playing;toggle.text=if(playing)"Pausa" else "Escuchar";audio?.auditionPlaying(playing)}
        for(button in listOf(previous,toggle,next))row.addView(button,android.widget.LinearLayout.LayoutParams(0,-2,1f))
        box.addView(spinner);box.addView(heading);box.addView(cover);box.addView(description);box.addView(row)
        spinner.setSelection(index,false)
        var lastSelected=-1
        spinner.onItemSelectedListener=object:android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent:android.widget.AdapterView<*>?){}
            override fun onItemSelected(parent:android.widget.AdapterView<*>?,view:View?,position:Int,id:Long){if(position!=lastSelected){lastSelected=position;index=position;select()}}
        }
        val scroll=android.widget.ScrollView(this).apply{addView(box)}
        showPaused(android.app.AlertDialog.Builder(this).setTitle("Resonancias · sala de escucha").setView(scroll).setPositiveButton("Volver",null).create()){
            spinner.onItemSelectedListener=null;cover.setImageDrawable(null);audio?.endAudition()
        }
    }

    private fun showClues(){
        val text=android.widget.TextView(this).apply{this.text=renderer.clueDiary.ifBlank{"Todavía no has anotado pistas. Puedes investigar en cualquier orden."};textSize=16f;setPadding(28,18,28,18)}
        val scroll=android.widget.ScrollView(this).apply{addView(text)}
        showPaused(android.app.AlertDialog.Builder(this).setTitle("Diario de pistas").setView(scroll).setPositiveButton("Seguir explorando",null).create())
    }

    private fun showAlbum() {
        val folder=java.io.File(filesDir,"dream_photos")
        val photos=folder.listFiles()?.filter{it.extension=="jpg"}?.sortedByDescending{it.lastModified()}.orEmpty()
        if(photos.isEmpty()){showPaused(android.app.AlertDialog.Builder(this).setMessage("Todavía no hay fotografías. Usa FOTO mientras exploras.").setPositiveButton("Volver",null).create());return}
        showPaused(android.app.AlertDialog.Builder(this).setTitle("Tus últimas ${photos.size} fotografías").setItems(photos.mapIndexed{i,_->"Fotografía ${i+1}"}.toTypedArray()){_,index->
            val photo=photos[index]
            val source=try{java.io.File(folder,photo.nameWithoutExtension+".meta").readText()}catch(_:Exception){""}
            val bitmap=android.graphics.BitmapFactory.decodeFile(photo.path,android.graphics.BitmapFactory.Options().apply{inSampleSize=2})
            val view=android.widget.ImageView(this).apply{setImageBitmap(bitmap);adjustViewBounds=true}
            val builder=android.app.AlertDialog.Builder(this).setTitle(if(DreamVoyage.photoWorld(source)!=null)"Un recuerdo que puedes visitar" else "Fotografía del sueño").setView(view).setPositiveButton("Cerrar",null)
                .setNeutralButton("Colocar en refugio"){_,_->showPaused(android.app.AlertDialog.Builder(this).setTitle("Elige un marco").setItems(arrayOf("Marco 1","Marco 2","Marco 3","Marco 4")){_,slot->gameView.queueEvent{renderer.pinPhoto(photo.path,slot)}}.setNegativeButton("Volver",null).create())}
            if(DreamVoyage.photoWorld(source)!=null)builder.setNegativeButton("Entrar al recuerdo"){_,_->gameView.queueEvent{renderer.enterPhotograph(source)}}
            showPaused(builder.create()){view.setImageDrawable(null);bitmap?.recycle()}
        }.setNegativeButton("Cerrar",null).create())
    }

    private fun showTools(){showPaused(android.app.AlertDialog.Builder(this).setTitle("Objetos del refugio").setMessage("Recoge los objetos en la mesa. La linterna revela puertas; la brújula escucha señales; el prisma cambia las ventanas.").setPositiveButton("Elegir"){_,_->showPaused(android.app.AlertDialog.Builder(this).setItems(arrayOf("Manos libres","Linterna","Brújula","Prisma")){_,index->gameView.queueEvent{renderer.equipTool(DreamTools.names[index])}}.setNegativeButton("Volver",null).create())}.setNegativeButton("Volver",null).create())}

    private fun showSettings() {
        val box=android.widget.LinearLayout(this).apply{orientation=android.widget.LinearLayout.VERTICAL;setPadding(28,12,28,12)}
        fun persist(){getPreferences(MODE_PRIVATE).edit().putFloat("sensitivity",options.sensitivity).putFloat("music",options.music).putFloat("ambience",options.ambience).putInt("quality",options.quality).putInt("footstep_mode",footstepMode).putInt("fps",options.fps).putInt("power_mode",options.powerMode).putInt("compatibility",options.compatibility).apply()}
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
        box.addView(android.widget.Button(this).apply{text="Sala de escucha · 52 composiciones";setOnClickListener{showMusicRoom()}})
        slider("Ambiente y objetos",options.ambience){options.ambience=it}
        box.addView(android.widget.Button(this).apply{
            text="Calidad: "+listOf("Baja","Media","Alta")[options.quality]
            setOnClickListener{options.quality=(options.quality+1)%3;text="Calidad: "+listOf("Baja","Media","Alta")[options.quality];persist()}
        })
        box.addView(android.widget.Button(this).apply{
            text="Pasos: "+listOf("Silencio","Suaves","Normales")[footstepMode]
            setOnClickListener{footstepMode=(footstepMode+1)%3;text="Pasos: "+listOf("Silencio","Suaves","Normales")[footstepMode];footsteps?.userGain=when(footstepMode){0->0f;2->1.65f;else->1f};persist()}
        })
        box.addView(android.widget.Button(this).apply{
            text="Fluidez: ${options.fps} FPS";setOnClickListener{options.fps=if(options.fps==30)60 else 30;text="Fluidez: ${options.fps} FPS";persist()}
        })
        box.addView(android.widget.Button(this).apply{
            fun label()="Energía: "+listOf("Automática","Resolución completa","Fresco · 24 FPS")[options.powerMode]
            text=label();setOnClickListener{options.powerMode=(options.powerMode+1)%3;text=label();persist();applyPower()}
        })
        box.addView(android.widget.TextView(this).apply{text="Automática conserva modelos y texturas y adapta la resolución si el renderizado se atrasa o Android informa calor. Resolución completa mantiene el tamaño nativo; Fresco limita la carga."})
        box.addView(android.widget.Button(this).apply{
            text="Compatibilidad: "+listOf("Automática","Estable","Esencial")[options.compatibility]
            setOnClickListener{
                val choices=arrayOf("Automática · detectar GPU","Estable · errores de color","Esencial · teléfonos antiguos")
                showPaused(android.app.AlertDialog.Builder(this@MainActivity).setTitle("Compatibilidad gráfica").setSingleChoiceItems(choices,options.compatibility){dialog,n->
                    if(n!=options.compatibility){
                        options.compatibility=n;persist();dialog.dismiss()
                        gameView.queueEvent{renderer.save(true);runOnUiThread{recreate()}}
                    }else dialog.dismiss()
                }.setNegativeButton("Volver",null).create())
            }
        })
        box.addView(android.widget.TextView(this).apply{text="Estable evita la lectura de profundidad que puede fallar en algunas GPU. Esencial conserva mundos y texturas con iluminación simplificada. El cambio guarda la partida y reinicia el juego."})
        box.addView(android.widget.Button(this).apply{text="Información gráfica";setOnClickListener{showPaused(android.app.AlertDialog.Builder(this@MainActivity).setTitle("Gráficos del dispositivo").setMessage(renderer.graphicsInfo).setPositiveButton("Volver",null).create())}})
        var settingsDialog:android.app.AlertDialog?=null
        if(!session.atTitle){
        box.addView(android.widget.Button(this).apply{text="Pista del lugar";setOnClickListener{showPaused(android.app.AlertDialog.Builder(this@MainActivity).setTitle(DreamJourney.title(renderer.currentWorld)).setMessage(DreamRouteHelp24.hint(renderer.currentWorld)).setPositiveButton("Seguir explorando",null).create())}})
        box.addView(android.widget.Button(this).apply{text="Ir a mi refugio";setOnClickListener{gameView.queueEvent{renderer.goRefuge()};settingsDialog?.dismiss()}})
        box.addView(android.widget.Button(this).apply{text="Los lugares imposibles · 11 mundos";setOnClickListener{gameView.queueEvent{renderer.goReferences()};settingsDialog?.dismiss()}})
        box.addView(android.widget.Button(this).apply{text="Diario de pistas";setOnClickListener{showClues()}})
        box.addView(android.widget.Button(this).apply{text="Objetos";setOnClickListener{showTools()}})
        box.addView(android.widget.Button(this).apply{text="Rutas y destinos";setOnClickListener{settingsDialog?.dismiss();showRoutes()}})
        box.addView(android.widget.Button(this).apply{text="Guardar y volver al inicio";setOnClickListener{settingsDialog?.dismiss();gameView.queueEvent{renderer.save(true)};showHome()}})
        }
        box.addView(android.widget.TextView(this).apply{text="Equilibrado: 30 FPS. Si el teléfono se calienta, el juego reduce efectos y resolución para aliviar la carga. Los escenarios y la partida se conservan."})
        box.addView(android.widget.TextView(this).apply{text=renderer.journal})
        val scroll=android.widget.ScrollView(this).apply{addView(box)}
        settingsDialog=android.app.AlertDialog.Builder(this).setTitle("Comodidad y recuerdos").setView(scroll).setPositiveButton("Volver"){_,_->persist()}.apply{if(!session.atTitle)setNeutralButton("Álbum"){_,_->showAlbum()}}.create()
        showPaused(settingsDialog!!)
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
            failed = false;framePending.set(false);frameBudget.reset();lastDrawStart=0L
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
                framePending.set(false)
                GLES20.glClearColor(0.36f, 0.20f, 0.48f, 1f)
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                return
            }
            val started=System.nanoTime()
            try {
                delegate.onDrawFrame(gl)
                val fps=DreamPower.fps(options.fps,options.thermalCap,options.powerMode)
                options.runtimeScale=frameBudget.sample(System.nanoTime()-started,if(lastDrawStart==0L)0L else started-lastDrawStart,fps,options.powerMode==0)
                lastDrawStart=started
            } catch (e: Exception) {
                handle("OPENGL DIBUJO", e)
            } finally {framePending.set(false)}
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
            for(label in renderer.portalLabels){
                val anchorX=label.x*width;val py=label.y*height
                paint.textSize=11f*d;val half=(paint.measureText(label.name)*.5f+12f*d).coerceAtMost(width*.35f)
                val px=anchorX.coerceIn(half+8f*d,width-half-8f*d)
                if(kotlin.math.abs(label.y-label.anchorY)>.01f || kotlin.math.abs(px-anchorX)>d){paint.color=Color.argb(100,202,224,217);paint.strokeWidth=d;canvas.drawLine(px,py+9f*d,anchorX,label.anchorY*height+9f*d,paint)}
                paint.style=Paint.Style.FILL;paint.color=Color.argb((label.alpha*190).toInt(),15,26,37)
                canvas.drawRoundRect(RectF(px-half,py-17f*d,px+half,py+8f*d),7f*d,7f*d,paint)
                text(label.name,px,py,11f,(label.alpha*245).toInt(),Paint.Align.CENTER)
            }
            val title=DreamJourney.hudTitle(renderer.currentWorld)
            text(if(DreamVigilia27.dark(renderer.currentWorld)) "DARK FANTASY  ·  2.9" else "DREAM CORE  ·  2.9",17f*d,26f*d,14f,240)
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
                paint.strokeWidth=2.9f*d
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
            if(this@MainActivity.foreground&&modalDepth==0&&!session.atTitle)postInvalidateDelayed(90L)
        }
    }
}
