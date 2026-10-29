package com.dreamcore.game

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Handler
import android.os.Looper

/** Soft, non-repeating shoe variants. Mono sources pan naturally; echoes stay subtle. */
class DreamFootsteps(context:Context) {
    @Suppress("DEPRECATION")
    private val pool=SoundPool(8,AudioManager.STREAM_MUSIC,0)
    private val handler=Handler(Looper.getMainLooper())
    private val ready=HashSet<Int>()
    private val sounds=HashMap<String,IntArray>()
    private val last=HashMap<String,Int>()
    var voiceGain34=.8f
    private var voiceStream34=0
    private val loops34=IntArray(3)
    private var lastFrame34:DreamAcoustics34.Frame?=null
    private var lastOptions34=0f
    var userGain=1f
    var ambienceGain=.65f
    private var seed=1729
    private var running=false
    private var released=false
    init {
        pool.setOnLoadCompleteListener{_,id,status->if(status==0)ready.add(id)}
        val resources=mapOf(
            "piano0" to intArrayOf(R.raw.fx_piano0_30),"piano1" to intArrayOf(R.raw.fx_piano1_30),"piano2" to intArrayOf(R.raw.fx_piano2_30),"piano3" to intArrayOf(R.raw.fx_piano3_30),"piano4" to intArrayOf(R.raw.fx_piano4_30),"piano5" to intArrayOf(R.raw.fx_piano5_30),"piano6" to intArrayOf(R.raw.fx_piano6_30),
            "chalk30" to intArrayOf(R.raw.fx_chalk30),"train30" to intArrayOf(R.raw.fx_train30),"room30" to intArrayOf(R.raw.fx_room30),"wind30" to intArrayOf(R.raw.fx_wind30),"tape30" to intArrayOf(R.raw.fx_tape30),"paper30" to intArrayOf(R.raw.fx_paper30),"projector30" to intArrayOf(R.raw.fx_projector30),"valve30" to intArrayOf(R.raw.fx_valve30),"carousel30" to intArrayOf(R.raw.fx_carousel30),
            "carpet" to intArrayOf(R.raw.fx_wood_0_17,R.raw.fx_wood_1_17,R.raw.fx_wood_2_17),
            "grass" to intArrayOf(R.raw.fx_grass_0_17,R.raw.fx_grass_1_17,R.raw.fx_grass_2_17),
            "stone" to intArrayOf(R.raw.fx_stone_0_17,R.raw.fx_stone_1_17,R.raw.fx_stone_2_17),
            "tile" to intArrayOf(R.raw.fx_tile_0_17,R.raw.fx_tile_1_17,R.raw.fx_tile_2_17),
            "wood" to intArrayOf(R.raw.fx_wood_0_17,R.raw.fx_wood_1_17,R.raw.fx_wood_2_17),
            "water" to intArrayOf(R.raw.fx_water_0_17,R.raw.fx_water_1_17,R.raw.fx_water_2_17),
            "chime" to intArrayOf(R.raw.fx_chime_16),
            "whale" to intArrayOf(R.raw.fx_whale_20),
            "piano" to intArrayOf(R.raw.fx_piano_19),
            "tone0" to intArrayOf(R.raw.fx_tone0_18),"tone1" to intArrayOf(R.raw.fx_tone1_18),
            "tone2" to intArrayOf(R.raw.fx_tone2_18),"tone3" to intArrayOf(R.raw.fx_tone3_18))
        for((name,id)in mapOf("wind_loop34" to R.raw.fx_wind_loop34,"rain_loop34" to R.raw.fx_rain_loop34,"breath_loop34" to R.raw.fx_breath_loop34,"presence_choir34" to R.raw.fx_presence_choir34,"presence_throat34" to R.raw.fx_presence_throat34,"presence_bone34" to R.raw.fx_presence_bone34,"presence_rasp34" to R.raw.fx_presence_rasp34,"presence_whisper34" to R.raw.fx_presence_whisper34,"presence_wet34" to R.raw.fx_presence_wet34))sounds[name]=intArrayOf(pool.load(context,id,1))
        for((name,id)in mapOf("roomWall33" to R.raw.fx_room_wall33,"piano0Wall33" to R.raw.fx_piano0_wall33,"piano1Wall33" to R.raw.fx_piano1_wall33,"piano2Wall33" to R.raw.fx_piano2_wall33,"piano3Wall33" to R.raw.fx_piano3_wall33,"piano4Wall33" to R.raw.fx_piano4_wall33,"piano5Wall33" to R.raw.fx_piano5_wall33,"piano6Wall33" to R.raw.fx_piano6_wall33,"tone0Wall33" to R.raw.fx_tone0_wall33,"tone1Wall33" to R.raw.fx_tone1_wall33,"tone2Wall33" to R.raw.fx_tone2_wall33,"tone3Wall33" to R.raw.fx_tone3_wall33,"trainWall33" to R.raw.fx_train_wall33,"pianoWall33" to R.raw.fx_piano_wall33,"whaleWall33" to R.raw.fx_whale_wall33,"woodWall33" to R.raw.fx_wood_wall33))sounds[name]=intArrayOf(pool.load(context,id,1))
        for(style in DreamFatal35.styles.values){val key="attack_"+style.sound+"35";val id=context.resources.getIdentifier("fx_"+key,"raw",context.packageName);if(id!=0)sounds[key]=intArrayOf(pool.load(context,id,1))}
        for((name,ids)in resources)sounds[name]=ids.map{pool.load(context,it,1)}.toIntArray()
    }
    fun play(surface:String,pan:Float,indoor:Boolean,volume:Float) {
        if(released || !running)return
        val variants=sounds[surface]?:return
        val floorGain=if(surface=="carpet").38f else 1f
        seed=seed*1664525+1013904223
        var choice=(seed ushr 16)%variants.size
        if(variants.size>1 && choice==last[surface])choice=(choice+1)%variants.size
        last[surface]=choice
        val id=variants[choice]
        if(id !in ready)return
        val level=(if((surface.startsWith("presence_")||surface.startsWith("attack_")))voiceGain34 else 1f)*floorGain*(volume*ambienceGain*(if(surface=="whale" || surface=="chime" || surface.startsWith("piano") || (surface.endsWith("30")||surface.endsWith("33")||surface.endsWith("34")||surface.endsWith("35")) || surface.startsWith("tone"))1f else userGain)).coerceIn(0f,.40f)
        if(level<=0f)return
        val p=pan.coerceIn(-.85f,.85f)
        val rate=if(surface=="whale" || surface=="chime" || surface.startsWith("piano") || (surface.endsWith("30")||surface.endsWith("33")||surface.endsWith("34")||surface.endsWith("35")) || surface.startsWith("tone"))1f else .98f+(seed ushr 24)/255f*.04f
        val left=(level*(1f-p)).coerceAtMost(1f);val right=(level*(1f+p)).coerceAtMost(1f)
        val stream=pool.play(id,left,right,1,0,rate);if((surface.startsWith("presence_")||surface.startsWith("attack_"))){if(voiceStream34!=0)pool.stop(voiceStream34);voiceStream34=stream}
        if(indoor)for((delay,gain)in listOf(83L to .075f,187L to .025f))handler.postDelayed({
            if(!released && running)pool.play(id,right*gain,left*gain,0,0,rate)
        },delay)
    }
    fun update34(frame:DreamAcoustics34.Frame,options:DreamOptions){
        if(released||!running)return
        val signature=options.breathGain34+options.weatherGain34*3f+ambienceGain*7f
        if(frame===lastFrame34&&signature==lastOptions34)return
        lastFrame34=frame;lastOptions34=signature
        fun loop(index:Int,name:String,level:Float,rate:Float=1f){
            val gain=(level*ambienceGain).coerceIn(0f,.28f);val id=sounds[name]?.firstOrNull()?:return
            if(gain<.003f){if(loops34[index]!=0)pool.stop(loops34[index]);loops34[index]=0;return}
            if(id !in ready){lastFrame34=null;return}
            if(loops34[index]==0)loops34[index]=pool.play(id,gain,gain,3,-1,rate)
            if(loops34[index]!=0){pool.setVolume(loops34[index],gain,gain);pool.setRate(loops34[index],rate)}
        }
        loop(0,"wind_loop34",frame.wind*options.weatherGain34*.26f)
        loop(1,"rain_loop34",frame.rain*options.weatherGain34*.32f)
        loop(2,"breath_loop34",frame.tension*frame.tension*options.breathGain34*.32f,.72f+frame.tension*.65f)
    }
    fun silence34(){if(released)return;if(voiceStream34!=0)pool.stop(voiceStream34);voiceStream34=0;for(i in loops34.indices){if(loops34[i]!=0)pool.stop(loops34[i]);loops34[i]=0};lastFrame34=null}
    fun resume(){if(!released){running=true;pool.autoResume()}}
    fun pause(){if(released)return;silence34();running=false;handler.removeCallbacksAndMessages(null);if(!released)pool.autoPause()}
    fun release(){if(released)return;pause();released=true;pool.release()}
}
