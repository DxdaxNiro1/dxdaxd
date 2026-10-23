package com.dreamcore.game

import java.util.Locale
import kotlin.math.abs

/** Fold world-invariant branches in the driver, preserving every expression for this world. */
object DreamShader28 {
    private val declaration=Regex("uniform\\s+(?:(?:lowp|mediump|highp)\\s+)?float\\s+uWorld\\s*;")
    fun specialize(source:String,code:Float):String {
        require(code.isFinite()&&code in 0f..61f)
        val world=source.replace(declaration,"const mediump float uWorld = "+String.format(Locale.US,"%.1f",code)+";")
        return if(volumeSky(code))world else world.replace(Regex("uniform\\s+(?:(?:lowp|mediump|highp)\\s+)?float\\s+uCloudPass\\s*;"),"const mediump float uCloudPass = 0.0;")
    }
    private val noVolume=setOf(6f,7f,12f,19f,14f,15f,16f,22f,23f,31f,38f,39f,40f,41f,43f,44f,48f,49f,50f,52f,55f)
    fun volumeSky(code:Float)=code !in noVolume
    private val sea=setOf(10f,33f,45f,47f,51f)
    /** Bounds the entire sky quad, including rolled gravity cameras. False means no ray can hit a cloud layer. */
    fun cloudMayIntersect(code:Float,eyeY:Float,forwardY:Float,rightY:Float,upY:Float,aspect:Float):Boolean {
        val spread=.649408f*(abs(rightY)*aspect+abs(upY));val lo=forwardY-spread;val hi=forwardY+spread
        if(code in sea)return (eyeY<190f&&hi>0f)||(eyeY> -135f&&lo<0f)
        return when{eyeY<85f->hi>0f;eyeY>190f->lo<0f;else->true}
    }

}
