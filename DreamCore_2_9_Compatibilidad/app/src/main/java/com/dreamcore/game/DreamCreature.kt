package com.dreamcore.game

import kotlin.math.*

/** A slow, non-colliding visitor below the suspended sea. No pursuit or damage. */
object DreamCreature {
    data class Pose(val x:Float,val y:Float,val z:Float,val yaw:Float)
    fun pose(time:Float):Pose {val a=time*.011f;return Pose(60f*sin(a),23f+1.2f*sin(a*.7f),-240f+112f*cos(a),-atan2(cos(a)*60f,-sin(a)*112f))}
    fun create():DreamWorlds.Scene {
        val b=DreamMesh();val c=floatArrayOf(.12f,.21f,.30f);val pale=floatArrayOf(.33f,.50f,.56f)
        b.ellipsoid(0f,0f,0f,6f,3.4f,22f,c,16,30,83f)
        b.ellipsoid(0f,.2f,15f,5.5f,3.3f,10f,c,12,24,83f)
        for(s in floatArrayOf(-1f,1f)){
            b.ellipsoid(s*5.1f,1.0f,18f,.38f,.32f,.50f,pale,6,10,83f)
            b.triangle(floatArrayOf(s*4f,0f,3f),floatArrayOf(s*22f,-1.5f,-5f),floatArrayOf(s*4f,-1f,-10f),c,83f)
            b.triangle(floatArrayOf(0f,0f,-20f),floatArrayOf(s*12f,.2f,-30f),floatArrayOf(s*2f,0f,-28f),c,83f)
        }
        for(i in -2..2)b.ellipsoid(i*1.4f,-2.7f,6f,.45f,.25f,12f,pale,5,12,83f)
        return b.finish()
    }
}
