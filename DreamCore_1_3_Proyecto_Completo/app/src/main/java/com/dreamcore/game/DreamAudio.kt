package com.dreamcore.game

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.animation.LinearInterpolator
import kotlin.math.sqrt

/** Música original del abismo 1.3. El resto de recursos se conserva. */
class DreamAudio(private val context: Context) {
    private fun create(resource: Int): MediaPlayer? = try {
        MediaPlayer.create(context, resource)?.apply {
            isLooping = true
            setVolume(0f, 0f)
        }
    } catch (problem: Exception) {
        // Un recurso defectuoso no debe apagar las demás músicas.
        Log.w("DreamCore", "No se pudo abrir un recurso de audio", problem)
        null
    }

    private val station = create(R.raw.music_station)
    private val pools = create(R.raw.music_pools)
    private val crater = create(R.raw.music_crater_13)
    private val garden = create(R.raw.music_garden)
    private val meadow = create(R.raw.music_meadow)
    private val aquarium = create(R.raw.music_aquarium)
    private val layerInside = create(R.raw.layer_inside)
    private val layerThreshold = create(R.raw.layer_threshold)
    private val layerAnomaly = create(R.raw.layer_anomaly)
    private val layerAbyss = create(R.raw.layer_abyss_13)
    private val baseTracks = listOfNotNull(station, pools, crater, garden, meadow, aquarium)
    private val layers = listOfNotNull(layerInside, layerThreshold, layerAnomaly, layerAbyss)
    private val all = baseTracks + layers
    private val gains = all.associateWith { 0f }.toMutableMap()
    private val volume = .46f
    private val layerVolume = .24f
    private var activeWorld = DreamWorlds.STATION
    private var zone = "CALMA"
    private var running = false
    private var released = false
    private var fade: ValueAnimator? = null
    private var layerFade: ValueAnimator? = null

    private fun track(world: String): MediaPlayer? = when (world) {
        DreamWorlds.STATION -> station
        DreamWorlds.POOLS -> pools
        DreamWorlds.CRATER, DreamWorlds.HOUSE_CRATER -> crater
        DreamWorlds.GARDEN -> garden
        DreamWorlds.MEADOW, DreamWorlds.HOUSE_MEADOW -> meadow
        DreamWorlds.AQUARIUM -> aquarium
        else -> null
    }

    private fun layerFor(value: String): MediaPlayer? = when (value) {
        "CASA" -> layerInside
        "PROFUNDO" -> if (activeWorld == DreamWorlds.CRATER) layerAbyss else layerThreshold
        "UMBRAL" -> layerThreshold
        "ANOMALIA" -> layerAnomaly
        else -> null
    }

    private fun gain(player: MediaPlayer, value: Float) {
        val bounded = value.coerceIn(0f, 1f)
        gains[player] = bounded
        player.setVolume(bounded, bounded)
    }

    /** Captura volúmenes reales: cambiar de mundo durante un fundido no da saltos. */
    private fun transition(
        players: List<MediaPlayer>, selected: MediaPlayer?, target: Float, durationMs: Long
    ): ValueAnimator {
        val from = players.associateWith { gains[it] ?: 0f }
        if (selected != null && !selected.isPlaying) {
            gain(selected, 0f)
            selected.start()
        }
        return ValueAnimator.ofFloat(0f, 1f).apply {
            duration = durationMs
            interpolator = LinearInterpolator()
            addUpdateListener { frame ->
                val t = frame.animatedValue as Float
                for (player in players) {
                    val start = from[player] ?: 0f
                    val end = if (player === selected) target else 0f
                    // Fundido de potencia constante para evitar un vacío entre pistas.
                    gain(player, sqrt(start * start * (1f - t) + end * end * t))
                }
            }
            addListener(object : AnimatorListenerAdapter() {
                private var cancelled = false
                override fun onAnimationCancel(animation: Animator) { cancelled = true }
                override fun onAnimationEnd(animation: Animator) {
                    if (cancelled || released) return
                    for (player in players) {
                        if (player !== selected) {
                            gain(player, 0f)
                            if (player.isPlaying) player.pause()
                        }
                    }
                }
            })
            start()
        }
    }

    fun resume() {
        if (released || running) return
        running = true
        // pause() deja todos los reproductores en silencio; solo arrancan los actuales.
        fade = transition(baseTracks, track(activeWorld), volume, 900L)
        layerFade = transition(layers, layerFor(zone), layerVolume, 1100L)
    }

    fun pause() {
        if (released) return
        fade?.cancel(); fade = null
        layerFade?.cancel(); layerFade = null
        running = false
        all.forEach {
            if (it.isPlaying) it.pause()
            gain(it, 0f)
        }
    }

    fun changeWorld(world: String) {
        if (released || world == activeWorld) return
        val oldLayer = layerFor(zone)
        val oldTrack = track(activeWorld)
        activeWorld = world
        if (!running) return
        if (oldTrack !== track(world)) {
            fade?.cancel()
            fade = transition(baseTracks, track(world), volume, 2600L)
        }
        // PROFUNDO usa una capa distinta en el abismo y en el acuario.
        if (oldLayer !== layerFor(zone)) {
            layerFade?.cancel()
            layerFade = transition(layers, layerFor(zone), layerVolume, 1800L)
        }
    }

    fun setZone(nextZone: String) {
        if (released || nextZone == zone) return
        val old = layerFor(zone)
        zone = nextZone
        if (!running || old === layerFor(zone)) return
        layerFade?.cancel()
        layerFade = transition(layers, layerFor(zone), layerVolume, 1800L)
    }

    fun release() {
        if (released) return
        pause()
        released = true
        all.forEach { it.release() }
        gains.clear()
    }
}
