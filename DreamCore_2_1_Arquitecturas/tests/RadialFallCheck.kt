package com.dreamcore.game

import kotlin.math.*

fun main() {
    var checks=0
    for(i in 0 until 24) {
        val angle=2.0*PI*i/24
        val c=DreamWorlds.CRATER_CENTER_Z
        val p=DreamCraterPhysics()
        val x=(93*cos(angle)).toFloat();val z=c+(93*sin(angle)).toFloat()
        p.reset(x,z,DreamWorlds.craterWalkSurface(x,z))
        var began=false;var landed=false
        repeat(60*11) {
            if(!landed) {
                p.step(1f/60,(-6.1*cos(angle)).toFloat(),(-6.1*sin(angle)).toFloat(),false)
                began=began || !p.grounded
                landed=began && p.grounded
            }
        }
        check(landed) { "Failed landing from angle $i" };checks++
        fun walkTo(tx:Float,tz:Float,maxSeconds:Int=50) {
            repeat(60*maxSeconds) {
                val dx=tx-p.x;val dz=tz-p.z;val d=hypot(dx,dz)
                if(d>.1f) p.step(1f/60,dx/d*3.5f,dz/d*3.5f,false)
            }
            check(hypot(tx-p.x,tz-p.z)<.13f) { "Blocked route $i at ${p.x},${p.z},${p.feetY} target $tx,$tz" }
            checks++
        }
        // Reach shore along radial path, circle around the house at radius 20,
        // then approach the front opening. Each route is available to a player.
        walkTo((20*cos(angle)).toFloat(),c+(20*sin(angle)).toFloat())
        val increment=((PI/2-angle+2*PI)%(2*PI))/16
        for(j in 1..16) {
            val a=angle+increment*j
            walkTo((20*cos(a)).toFloat(),c+(20*sin(a)).toFloat(),4)
        }
        walkTo(0f,DreamWorlds.CRATER_HOUSE_FRONT_Z+.6f)
        check(DreamWorlds.craterReturnDoor(p.x,p.z,p.feetY)) { "Door not eligible angle $i" };checks++
    }
    println("PASS $checks radial landing/routing/door checks from all 24 rim angles")
}
