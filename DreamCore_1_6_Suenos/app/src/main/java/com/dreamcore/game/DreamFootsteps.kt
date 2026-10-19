package com.dreamcore.game

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Handler
import android.os.Looper

/** Short synthesized effects; all public methods run on the activity thread. */
class DreamFootsteps(context:Context) {
    @Suppress("DEPRECATION")
    private val pool=SoundPool(8,AudioManager.STREAM_MUSIC,0)
    private val handler=Handler(Looper.getMainLooper())
    private val ready=HashSet<Int>()
    private val sounds=HashMap<String,Int>()
    private var running=false
    private var released=false
    private var alternate=false
    init {
        pool.setOnLoadCompleteListener{_,id,status->if(status==0)ready.add(id)}
        val resources=mapOf("grass" to R.raw.fx_grass_16,"stone" to R.raw.fx_stone_16,
            "tile" to R.raw.fx_tile_16,"wood" to R.raw.fx_wood_16,
            "water" to R.raw.fx_water_16,"chime" to R.raw.fx_chime_16)
        for((name,id)in resources)sounds[name]=pool.load(context,id,1)
    }
    fun play(surface:String,pan:Float,indoor:Boolean,volume:Float) {
        if(released || !running)return
        val id=sounds[surface]?:return
        if(id !in ready)return
        val level=volume.coerceIn(0f,.65f)
        val p=pan.coerceIn(-.85f,.85f)
        alternate=!alternate
        val rate=if(surface=="chime")1f else if(alternate).94f else 1.03f
        pool.play(id,level*(1f-p),level*(1f+p),1,0,rate)
        if(indoor)for((delay,gain)in listOf(95L to .18f,210L to .07f))handler.postDelayed({
            if(!released && running)pool.play(id,level*gain*(1f+p),level*gain*(1f-p),0,0,rate)
        },delay)
    }
    fun resume(){if(!released){running=true;pool.autoResume()}}
    fun pause(){running=false;handler.removeCallbacksAndMessages(null);if(!released)pool.autoPause()}
    fun release(){if(released)return;pause();released=true;pool.release()}
}
