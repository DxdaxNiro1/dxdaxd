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
    var userGain=1f
    var ambienceGain=.65f
    private var seed=1729
    private var running=false
    private var released=false
    init {
        pool.setOnLoadCompleteListener{_,id,status->if(status==0)ready.add(id)}
        val resources=mapOf(
            "grass" to intArrayOf(R.raw.fx_grass_0_17,R.raw.fx_grass_1_17,R.raw.fx_grass_2_17),
            "stone" to intArrayOf(R.raw.fx_stone_0_17,R.raw.fx_stone_1_17,R.raw.fx_stone_2_17),
            "tile" to intArrayOf(R.raw.fx_tile_0_17,R.raw.fx_tile_1_17,R.raw.fx_tile_2_17),
            "wood" to intArrayOf(R.raw.fx_wood_0_17,R.raw.fx_wood_1_17,R.raw.fx_wood_2_17),
            "water" to intArrayOf(R.raw.fx_water_0_17,R.raw.fx_water_1_17,R.raw.fx_water_2_17),
            "chime" to intArrayOf(R.raw.fx_chime_16),
            "tone0" to intArrayOf(R.raw.fx_tone0_18),"tone1" to intArrayOf(R.raw.fx_tone1_18),
            "tone2" to intArrayOf(R.raw.fx_tone2_18),"tone3" to intArrayOf(R.raw.fx_tone3_18))
        for((name,ids)in resources)sounds[name]=ids.map{pool.load(context,it,1)}.toIntArray()
    }
    fun play(surface:String,pan:Float,indoor:Boolean,volume:Float) {
        if(released || !running)return
        val variants=sounds[surface]?:return
        seed=seed*1664525+1013904223
        var choice=(seed ushr 16)%variants.size
        if(variants.size>1 && choice==last[surface])choice=(choice+1)%variants.size
        last[surface]=choice
        val id=variants[choice]
        if(id !in ready)return
        val level=(volume*ambienceGain*(if(surface=="chime" || surface.startsWith("tone"))1f else userGain)).coerceIn(0f,.40f)
        if(level<=0f)return
        val p=pan.coerceIn(-.85f,.85f)
        val rate=if(surface=="chime" || surface.startsWith("tone"))1f else .98f+(seed ushr 24)/255f*.04f
        val left=(level*(1f-p)).coerceAtMost(1f);val right=(level*(1f+p)).coerceAtMost(1f)
        pool.play(id,left,right,1,0,rate)
        if(indoor)for((delay,gain)in listOf(83L to .075f,187L to .025f))handler.postDelayed({
            if(!released && running)pool.play(id,right*gain,left*gain,0,0,rate)
        },delay)
    }
    fun resume(){if(!released){running=true;pool.autoResume()}}
    fun pause(){running=false;handler.removeCallbacksAndMessages(null);if(!released)pool.autoPause()}
    fun release(){if(released)return;pause();released=true;pool.release()}
}
