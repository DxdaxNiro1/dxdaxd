package com.dreamcore.game

/** The activity persists these settings; only simple volatile values cross to GL/audio. */
class DreamOptions {
    @Volatile var controlScale=1f
    @Volatile var toggleSprint=false
    @Volatile var reduceMotion=false
    @Volatile var soundCaptions=true
    @Volatile var joyX=.16f
    @Volatile var joyY=.74f
    @Volatile var runX=.89f
    @Volatile var runY=.80f
    @Volatile var jumpX=.73f
    @Volatile var jumpY=.79f
    @Volatile var buttonX=.91f
    @Volatile var buttonY=.70f
    @Volatile var sensitivity=1f
    @Volatile var music=.70f
    @Volatile var ambience=.65f
    @Volatile var quality=1
    @Volatile var fps=30
    @Volatile var thermalCap=60
    @Volatile var powerMode=0
    @Volatile var compatibility=0
    @Volatile var runtimeScale=1f
    @Volatile var runtimeLimit=16384
}
