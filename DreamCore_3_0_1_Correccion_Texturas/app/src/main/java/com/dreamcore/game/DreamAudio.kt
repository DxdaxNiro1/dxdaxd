package com.dreamcore.game

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.animation.LinearInterpolator
import kotlin.math.sqrt

/** Original score for every scene and the title; at most two bases + two layers. */
class DreamAudio(private val context: Context) {
    private var musicGain=.7f
    private var ambienceGain=.65f
    private val volume = .46f
    private val layerVolume = .22f
    private var activeWorld = DreamWorlds.STATION
    private var zone = "CALMA"
    private var running = false
    private var released = false
    private var atTitle = true
    private var preview: DreamMusic24.Track? = null
    private var previewPaused = false
    private val resources = DreamMusic24.tracks.associate { it.world to context.resources.getIdentifier(it.resource,"raw",context.packageName) }
    private fun selected() = preview?.world ?: if(atTitle) DreamMusic24.TITLE else activeWorld
    private fun currentLayer() = if(atTitle || preview!=null) null else layerFor(zone)
    private fun applySelection(duration:Long=2600L) {
        if(preview==null || !previewPaused)music.select(track(selected()),duration)
        atmosphere.select(currentLayer())
    }
    fun titleScreen(value:Boolean) { if(atTitle==value)return;atTitle=value;applySelection() }
    fun audition(value:DreamMusic24.Track) {preview=value;previewPaused=false;applySelection(900L)}
    fun auditionPlaying(value:Boolean) {
        if(preview==null)return
        previewPaused=!value
        if(value) { if(running)music.select(track(selected()),350L) } else music.pause()
    }
    fun endAudition(){preview=null;previewPaused=false;applySelection(1300L)}


    private class Slot(val resource: Int, val player: MediaPlayer) {
        var gain = 0f
    }

    private fun create(resource: Int): MediaPlayer? {
        var player: MediaPlayer? = null
        return try {
            player = MediaPlayer.create(context, resource)
            player?.apply { isLooping = true; setVolume(0f, 0f) }
        } catch (problem: Exception) {
            player?.release()
            Log.w("DreamCore", "No se pudo abrir un recurso de audio", problem)
            null
        }
    }

    /** If a third theme arrives mid-fade, free one slot without a volume jump first. */
    private inner class Mixer(private val fullVolume: Float, private val fadeMs: Long) {
        private val slots = mutableListOf<Slot>()
        private var animator: ValueAnimator? = null
        private var wanted: Int? = null
        private var draining = false

        private fun gain(slot: Slot, value: Float) {
            slot.gain = value.coerceIn(0f, 1f)
            val level=slot.gain*(if(this===music)musicGain else ambienceGain)
            slot.player.setVolume(level,level)
        }

        private fun remove(slot: Slot) {
            slot.player.release()
            slots.remove(slot)
        }

        fun select(resource: Int?, duration: Long = fadeMs) {
            wanted = resource
            if (!running) {
                // No decoding while paused, and no stale world retained after a switch.
                slots.toList().filter { it.resource != wanted }.forEach { remove(it) }
                return
            }
            // Keep a running bridge short even when several newer requests arrive.
            if (!draining) begin(duration)
        }

        private fun begin(duration: Long) {
            animator?.cancel(); animator = null
            val resource = wanted
            var selected = slots.firstOrNull { it.resource == resource }
            if (resource != null && selected == null && slots.size == 2) {
                draining = true
                val bridge = slots.maxByOrNull { it.gain }
                animate(bridge, 320L) {
                    draining = false
                    if (running && !released) begin(fadeMs)
                }
                return
            }
            if (resource != null && selected == null) {
                create(resource)?.let { player ->
                    selected = Slot(resource, player).also { slots += it }
                }
            }
            animate(selected, duration)
        }

        private fun animate(selected: Slot?, duration: Long, after: (() -> Unit)? = null) {
            val players = slots.toList()
            val from = players.associateWith { it.gain }
            if (selected != null && !selected.player.isPlaying) selected.player.start()
            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                this.duration = duration
                interpolator = LinearInterpolator()
                addUpdateListener { frame ->
                    val t = frame.animatedValue as Float
                    players.forEach { slot ->
                        val start = from[slot] ?: 0f
                        val end = if (slot === selected) fullVolume else 0f
                        gain(slot, sqrt(start * start * (1f - t) + end * end * t))
                    }
                }
                addListener(object : AnimatorListenerAdapter() {
                    private var cancelled = false
                    override fun onAnimationCancel(animation: Animator) { cancelled = true }
                    override fun onAnimationEnd(animation: Animator) {
                        if (cancelled || released) return
                        players.filter { it !== selected }.forEach { remove(it) }
                        animator = null
                        after?.invoke()
                    }
                })
                start()
            }
        }

        fun refresh(){for(slot in slots)gain(slot,slot.gain)}
        fun pause() {
            animator?.cancel(); animator = null
            draining = false
            slots.toList().forEach { slot ->
                if (slot.resource != wanted) remove(slot)
                else {
                    if (slot.player.isPlaying) slot.player.pause()
                    gain(slot, 0f)
                }
            }
        }

        fun release() {
            animator?.cancel(); animator = null
            draining = false
            slots.toList().forEach { remove(it) }
        }
    }

    private val music = Mixer(volume, 2600L)
    private val atmosphere = Mixer(layerVolume, 1800L)

    fun levels(music:Float,ambience:Float){musicGain=music.coerceIn(0f,1f);ambienceGain=ambience.coerceIn(0f,1f);this.music.refresh();atmosphere.refresh()}
    private fun track(world:String):Int? = resources[world]?.takeIf{it!=0}

    private fun layerFor(value: String): Int? = if(activeWorld==DreamBackrooms.ID)R.raw.air_backrooms_23 else when (value) {
        "OBSERVADO" -> R.raw.air_moon_17
        "ECLIPSE" -> R.raw.air_eclipse_17
        "DESPERTAR" -> R.raw.air_radio_18
        "PERMANECER" -> R.raw.air_moon_17
        "EL_ECO" -> R.raw.air_hollow_18
        "RADIO" -> R.raw.air_radio_18
        "CASA" -> R.raw.air_hollow_18
        "VIENTO" -> R.raw.air_wind_16
        "JARDIN" -> R.raw.air_garden_16
        "PISCINA_INTERIOR" -> R.raw.air_water_16
        "PROFUNDO" -> if (activeWorld == DreamWorlds.CRATER) R.raw.layer_abyss_13 else R.raw.layer_threshold
        "UMBRAL" -> R.raw.layer_threshold
        "ANOMALIA" -> R.raw.layer_anomaly
        else -> when(activeWorld) {
            DreamNewDreams.ISLAND->R.raw.air_water_16
            DreamVigilia27.DARK_THREE,DreamVigilia27.DARK_FOUR,DreamVigilia27.SKY_ISLAND->R.raw.air_wind_16
            DreamNewDreams.EYES->R.raw.air_wind_16
            DreamHorizons.DARK,DreamHorizons.CELESTIAL,DreamHorizons.CITY->R.raw.air_wind_16
            DreamHorizons.BED,DreamHorizons.TOWER->R.raw.air_hollow_18
            DreamWorlds.POOLS,DreamWaterpark.ID->R.raw.air_water_16
            DreamPocket.LAUNDRY,DreamPocket.DAWN->R.raw.air_hollow_18
            DreamPocket.TRAIN->R.raw.layer_abyss_13
            DreamWorlds.CRATER,DreamJourney.DARK->R.raw.air_wind_16
            else->null
        }
    }

    fun resume() {
        if (released || running) return
        running = true
        applySelection(900L)
    }

    fun pause() {
        if (released) return
        running = false
        music.pause()
        atmosphere.pause()
    }

    fun changeWorld(world:String) {
        if(released || world==activeWorld)return
        activeWorld=world
        if(!atTitle && preview==null)applySelection()
    }

    fun setZone(nextZone:String) {
        if(released || nextZone==zone)return
        val previous=currentLayer();zone=nextZone
        if(previous!=currentLayer())atmosphere.select(currentLayer())
    }

    fun release() {
        if (released) return
        released = true
        running = false
        music.release()
        atmosphere.release()
    }
}
