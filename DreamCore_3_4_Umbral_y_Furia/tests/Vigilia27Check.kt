package com.dreamcore.game

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

fun main(){
    var checks=0
    fun verify(ok:Boolean,label:String){check(ok){label};checks++}
    val out=File(System.getProperty("dreamcore.output","/tmp"));out.mkdirs()
    fun dump(name:String,v:FloatArray){val memory=ByteBuffer.allocate(v.size*4).order(ByteOrder.LITTLE_ENDIAN);memory.asFloatBuffer().put(v);File(out,"$name.f32").writeBytes(memory.array())}
    val yaw=atan2(DreamMoonLife.MAIN_X,-DreamMoonLife.MAIN_Z);val pitch=asin(DreamMoonLife.MAIN_Y)
    for(fps in listOf(10,20,24,30,60,120)){
        val moon=DreamMoonLife();var events=0
        for(i in 0 until fps*121){val before=moon.vigil
            if(moon.step(true,0f,1.65f,0f,yaw,pitch,1f/fps))events++
            if(before<59.8f)verify(!moon.bloodMoon,"not red before one minute $fps")
            if(before>60.1f)verify(moon.bloodMoon,"red after one minute $fps")
            if(before<119.8f)verify(!moon.giantVisible,"no premature giant $fps")
        }
        verify(events==1 && moon.giantVisible,"one giant after 120s $fps")
        val restored=DreamMoonLife();restored.restore(moon.snapshot());verify(restored.bloodMoon&&restored.giantVisible,"ten-field save retains both events $fps")
        val before=moon.vigil;repeat(100){moon.step(true,0f,1.65f,0f,yaw,pitch,0f)};verify(moon.vigil==before,"background pause does not count")
    }
    val occluded=DreamMoonLife();repeat(1000){occluded.step(true,0f,1.65f,0f,yaw,pitch,.25f,false)};verify(!occluded.bloodMoon&&!occluded.giantVisible,"closed ceiling blocks vigil")
    for(crescent in listOf(false,true)){
        val geometry=DreamMoonMesh27.create(crescent).vertices;verify(geometry.size%30==0&&geometry.all{it.isFinite()},"moon mesh finite")
        val kinds=geometry.indices.step(10).map{geometry[it+9]}.toSet();verify(kinds.containsAll(listOf(0f,1f,2f,3f,4f,5f,7f)),"separate animated eye and lid geometry")
        verify(geometry.indices.step(10).any{geometry[it+2]<-.5f},"true back hemisphere")
        verify(geometry.indices.step(10).any{geometry[it+2]>1f},"face has actual relief")
        dump(if(crescent)"moon_crescent27" else "moon_round27",geometry)
    }
    for(id in DreamVigilia27.ids){
        verify(DreamChronicle.validWorld(id),"new save registry $id")
        verify(DreamJourney.title(id)==DreamVigilia27.title(id),"map name $id")
        val p=DreamJourney.arrival(id,DreamWorlds.STATION);verify(DreamWorlds.canWalk(id,p.x,p.z),"safe spawn $id")
        verify(DreamJourney.portalAt(id,p.x,p.z,DreamJourney.surface(id,p.x,p.z))==null,"no spawn in active portal $id")
        val c=DreamChronicle.Checkpoint(id,p.x,DreamJourney.surface(id,p.x,p.z)+1.65f,p.z,0f,0f);val story=DreamChronicle();story.checkpoint=c
        verify(DreamChronicle.decode(DreamChronicle.encode(story))!!.checkpoint==c,"new checkpoint round trip $id")
        val mesh=DreamVigilia27.create(id).vertices;verify(mesh.size%30==0 && mesh.all{it.isFinite()},"scene geometry $id")
        dump(id,mesh)
        for(g in DreamVigilia27.gates(id)){verify(DreamWorlds.canWalk(id,g.x,g.z),"portal accessible");verify(DreamJourney.portalAt(id,g.x,g.z,DreamJourney.surface(id,g.x,g.z))!=null,"portal connected")}
        if(id!=DreamVigilia27.SKY_ISLAND)for(z in -285..24)verify(DreamWorlds.canWalk(id,0f,z.toFloat()),"continuous dark central path")
        else{
            verify(!DreamWorlds.canWalk(id,100f,-45f),"island cliff is safe")
            verify(mesh.indices.step(10).any{mesh[it+2]<-180f&&mesh[it+1]>120f&&mesh[it+9]==118f},"giant winged guardian beyond island")
            verify(mesh.indices.step(10).none{mesh[it+9]==15f},"no slide material/geometry")
        }
    }
    verify(DreamJourney.portalAt(DreamWorlds.STATION,-25f,-37f,0f)==DreamVigilia27.SKY_ISLAND,"hub physical gate")
    verify(DreamJourney.portalAt(DreamHorizons.DARK,0f,-330f,0f)==DreamVigilia27.DARK_THREE,"dark II to III")
    verify(DreamJourney.portalAt(DreamVigilia27.DARK_THREE,0f,-286f,0f)==DreamVigilia27.DARK_FOUR,"III to IV")
    verify(DreamJourney.portalAt(DreamVigilia27.DARK_FOUR,0f,-286f,0f)==DreamWorlds.STATION,"IV returns to hub")
    verify((listOf(DreamJourney.DARK,DreamHorizons.DARK)+DreamVigilia27.ids.take(2)).flatMap{DreamVigilia27.moons(it).let{p->listOf(p.first,p.second)}}.toSet()==(0..7).toSet(),"all eight lunar looks present")
    for(p in DreamGuardians27.restingPositions)verify(!DreamWorlds.canWalk(DreamNewDreams.EYES,p.x,p.z),"resting entities not walk-through")
    println("PASS $checks checks")
}
