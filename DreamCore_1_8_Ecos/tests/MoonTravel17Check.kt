package com.dreamcore.game

import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.*

fun main() {
    var checks=0
    fun verify(ok:Boolean,message:String){check(ok){message};checks++}
    val yaw=atan2(DreamMoonLife.MAIN_X,-DreamMoonLife.MAIN_Z)
    val pitch=asin(DreamMoonLife.MAIN_Y)
    for(fps in listOf(10,20,30,60,120)) {
        val moon=DreamMoonLife();var events=0
        repeat(fps*120-1) { events+=if(moon.step(true,0f,1.65f,36f,yaw,pitch,1f/fps))1 else 0 }
        verify(!moon.giantVisible,"$fps fps: no giant before two minutes")
        repeat(2){events+=if(moon.step(true,0f,1.65f,36f,yaw,pitch,1f/fps))1 else 0}
        verify(moon.giantVisible && events==1,"$fps fps: giant appears once at 120 seconds")
        verify(moon.mainSmile>.99f,"smile grows with observation")
        repeat(fps*20){events+=if(moon.step(true,0f,1.65f,36f,yaw,pitch,1f/fps))1 else 0}
        verify(events==1,"giant does not retrigger every frame")
        val before=moon.time
        repeat(100){moon.step(true,0f,1.65f,36f,yaw,pitch,0f)}
        verify(moon.time==before,"paused frames do not advance moon time")
        moon.step(false,0f,1.65f,36f,yaw,pitch,.02f)
        verify(!moon.giantVisible && moon.vigil==0f,"leaving Dark resets the event")
    }
    val away=DreamMoonLife()
    repeat(1000){away.step(true,0f,1.65f,36f,yaw,pitch,.1f)}
    repeat(30){away.step(true,0f,1.65f,36f,PI.toFloat(),0f,.1f)}
    verify(away.vigil==0f && !away.giantVisible,"looking away for >2s resets the vigil")
    repeat(250){away.step(true,0f,1.65f,36f,yaw,pitch,.1f)}
    verify(!away.giantVisible,"old attention cannot instantly trigger the giant")
    val hidden=DreamMoonLife()
    repeat(1400){hidden.step(true,0f,1.65f,36f,yaw,pitch,.1f,false)}
    verify(hidden.vigil==0f && !hidden.giantVisible,"occluded sky cannot advance attention")
    verify(DreamMoonLife.blink(5.11f,5.7f,.8f)>.99f,"first blink closes")
    verify(DreamMoonLife.blink(10.81f,5.7f,.8f)>.99f,"blink recurs")
    verify(DreamMoonLife.blink(6.0f,5.7f,.8f)<.01f,"eyes reopen")
    val eyes=DreamMoonLife()
    eyes.step(true,-80f,1.65f,0f,yaw,pitch,.1f);val left=eyes.eyeX
    eyes.step(true,80f,1.65f,0f,yaw,pitch,.1f)
    verify(eyes.eyeX>left && abs(eyes.eyeX)<=.06001f,"pupils follow player movement within socket bounds")
    val sight=DreamSkySight(DreamDarkMap.create().vertices)
    verify(sight.clear(0f,1.65f,36f,DreamMoonLife.MAIN_X,DreamMoonLife.MAIN_Y,DreamMoonLife.MAIN_Z),"moon visible from Dark arrival")
    verify(!sight.clear(0f,1.65f,-60f,DreamMoonLife.MAIN_X,DreamMoonLife.MAIN_Y,DreamMoonLife.MAIN_Z),"cathedral wall occludes moon")
    verify(DreamTravel.speed(true,0f,false)>6.1f*1.6f,"running is substantially faster")
    verify(DreamTravel.speed(true,4f,false)==12.4f,"cruise reaches 12.4 m/s")
    verify(DreamTravel.speed(true,4f,true)==8.2f,"interior speed stays controllable")
    verify(DreamTravel.speed(false,4f,false)==4.2f,"walking speed")
    for(fps in listOf(10,20,30,60,120)) {
        var x=10f;var z=-100f
        repeat(fps*2){val p=DreamTravel.move(DreamWorlds.HOUSE_MEADOW,x,z,12.4f,0f,1f/fps);x=p.first;z=p.second}
        verify(x<11.5f && DreamWorlds.canWalk(DreamWorlds.HOUSE_MEADOW,x,z),"$fps fps: sprint does not cross a thin wall")
        x=0f;z=-200f
        repeat(fps*3){val p=DreamTravel.move(DreamExpansion.TUNNELS,x,z,12.4f,0f,1f/fps);x=p.first;z=p.second}
        verify(x<20f && DreamWorlds.canWalk(DreamExpansion.TUNNELS,x,z),"$fps fps: sprint stops at pond edge")
    }
    verify(DreamLands.ids.size==9,"nine memory encounters")
    for(id in DreamLands.ids) {
        val s=DreamMemories.secret(id)
        verify(DreamWorlds.canWalk(id,s.x,s.z),"$id memory encounter centre stays clear")
        val e=DreamExploration();val notices=e.step(id,s.x,s.z,0f,0f,.02f)
        verify(e.memoriesFound==1 && notices.any{it.startsWith("Recuerdo:")},"$id memory is discoverable")
        e.step(id,s.x,s.z,0f,0f,.02f)
        verify(e.memoriesFound==1,"$id memory cannot be collected twice")
    }
    // Capture actual CPU animation states for the GLES2 preview, including a recurrent blink.
    val out=Paths.get(System.getProperty("dreamcore.output","build/journey-validation"));Files.createDirectories(out)
    for((name,seconds)in listOf("calma" to 0f,"parpadeo" to 5.11f,"sonrisa" to 46f,"gigante" to 120.5f)) {
        val m=DreamMoonLife();m.step(true,0f,1.65f,36f,yaw,pitch,0f)
        var elapsed=0f
        while(elapsed<seconds) {val dt=min(.05f,seconds-elapsed);m.step(true,0f,1.65f,36f,yaw,pitch,dt);elapsed+=dt}
        val text="""{"time":${m.time},"mood":[${m.mainBlink},${m.smallBlink},${m.mainSmile},${m.smallSmile}],"eyes":[${m.eyeX},${m.eyeY},${m.smallEyeX},${m.smallEyeY}],"giant":[${m.giantRadius},1.0,${m.giantBlink},${m.giantReveal}],"direction":[${m.giantX},${m.giantY},${m.giantZ}]}"""
        Files.write(out.resolve("moon_$name.json"),text.toByteArray())
    }
    println("PASS $checks lunar attention / recurrent animation / movement / memories checks")
}
