package com.dreamcore.game

/** The activity persists these settings; only simple volatile values cross to GL/audio. */
class DreamOptions {
    @Volatile var sensitivity=1f
    @Volatile var music=.70f
    @Volatile var ambience=.65f
    @Volatile var quality=1
    @Volatile var fps=30
    @Volatile var thermalCap=60
}
