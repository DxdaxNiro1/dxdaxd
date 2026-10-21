package com.dreamcore.game

import kotlin.math.*

/** Saved universe seed makes each abyss distinct without moving its collision terraces/exit. */
object DreamCraterLayout {
    var seed=7319
    fun value(index:Int,lane:Int):Float {var n=seed xor (index*73856093) xor(lane*19349663);n=(n xor(n ushr 16))*0x45d9f3b;n=(n xor(n ushr 16))*0x45d9f3b;return ((n xor(n ushr 16))ushr 8)/16777215f}
    data class House(val angle:Float,val radius:Float,val base:Float,val scale:Float,val style:Int)
    fun side(i:Int)=House((i+.18f+value(i,0)*.60f)*2f*PI.toFloat()/24f,84f+value(i,1)*5.5f,-14f-(i%8)*20f-value(i,2)*7f,1.1f+value(i,3)*.65f,(value(i,4)*120).toInt())
}
