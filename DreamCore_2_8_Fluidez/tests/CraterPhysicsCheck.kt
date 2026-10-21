package com.dreamcore.game

import kotlin.math.abs
import kotlin.math.hypot

/** Run on JVM, independent of Android, against the actual geometry helpers. */
fun main() {
    var checks = 0
    fun verify(ok: Boolean, message: String) { check(ok) { message }; checks++ }
    val center = DreamWorlds.CRATER_CENTER_Z
    val fallTimes = mutableListOf<Float>()
    for (fps in listOf(20,30,60,120)) {
        val p = DreamCraterPhysics()
        p.reset(0f,center+93f,DreamWorlds.craterWalkSurface(0f,center+93f))
        var time=0f; var began=false; var landed=false; var maxSpeed=0f
        repeat(fps*13) {
            if (!landed) {
                p.step(1f/fps,0f,-6.1f,false)
                time += 1f/fps
                if (!p.grounded) began=true
                maxSpeed=maxOf(maxSpeed,-p.verticalSpeed)
                if (began && p.grounded) landed=true
            }
        }
        verify(began,"$fps fps: edge must allow falling")
        verify(landed,"$fps fps: must land without falling through map")
        verify(time in 6f..9f,"$fps fps: 180m fall time must be physical, got $time")
        verify(maxSpeed<=32.001f,"terminal velocity")
        verify(abs(p.feetY-DreamWorlds.craterWalkSurface(p.x,p.z))<.01f,"land at mapped surface")
        fallTimes+=time
        // Walk from the landing area to the island and central door.
        repeat(fps*35) {
            if (p.z>DreamWorlds.CRATER_HOUSE_FRONT_Z+.8f) p.step(1f/fps,0f,-3.5f,false)
        }
        verify(DreamWorlds.craterReturnDoor(p.x,p.z,p.feetY),"$fps fps: central doorway reachable after fall: ${p.x},${p.feetY},${p.z}")
        println("PASS $fps fps: fall ${"%.2f".format(time)}s -> walk to return door")
    }
    verify((fallTimes.maxOrNull()!!-fallTimes.minOrNull()!!)<.15f,"consistent falling across frame rates")
    val p=DreamCraterPhysics()
    // Under the rim, attempting to walk out must meet the cliff wall.
    p.reset(0f,center+90f,DreamWorlds.craterWalkSurface(0f,center+90f))
    repeat(600) { p.step(1f/60,0f,6.1f,false) }
    verify(hypot(p.x,p.z-center)<DreamWorlds.CRATER_RIM_RADIUS,"no walking through cliff")
    verify(p.feetY < -170f,"no teleport from bottom to rim")
    // Jump is grounded-only, cannot repeatedly fly upward.
    p.reset(15f,center,DreamWorlds.craterWalkSurface(15f,center))
    val base=p.feetY; var peak=base
    repeat(180) { p.step(1f/60,0f,0f,true); peak=maxOf(peak,p.feetY) }
    verify(peak-base in .7f..1.3f,"grounded-only jump height, got ${peak-base}")
    // A vertical fall onto the roof lands on it; moving away can fall to island.
    p.reset(0f,center,-155f)
    repeat(300) { p.step(1f/60,0f,0f,false) }
    verify(abs(p.feetY-DreamWorlds.craterRoofSurface(0f,center)!!)<.02f,"roof supports falling player")
    repeat(240) { p.step(1f/60,6.1f,0f,false) }
    verify(p.feetY < -175f,"roof exit must permit drop to bottom")
    verify(!DreamWorlds.craterReturnDoor(0f,DreamWorlds.CRATER_HOUSE_FRONT_Z,0f),"door cannot activate from top")
    verify(!DreamWorlds.craterBodyBlocked(0f,DreamWorlds.CRATER_HOUSE_FRONT_Z,-176f),"door opening walkable")
    verify(DreamWorlds.craterBodyBlocked(3.8f,center,-176f),"central house wall blocks player")
    p.reset(0f,center+93f,DreamWorlds.craterWalkSurface(0f,center+93f))
    p.step(0f,0f,-6f,true)
    verify(p.grounded,"zero dt does not consume frame")
    verify(!DreamWorlds.craterIsWater(0f,center+10f),"island is dry")
    verify(DreamWorlds.craterIsWater(0f,center+50f),"outer bottom is water")
    println("PASS $checks gameplay checks")
}
