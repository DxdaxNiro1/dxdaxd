package com.dreamcore.game

import kotlin.math.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.CRC32

fun main(){
    var checks=0
    fun verify(ok:Boolean,label:String){check(ok){label};checks++}
    val out=Paths.get(System.getProperty("dreamcore.output","build/validation19"));Files.createDirectories(out)
    fun dump(id:String,scene:DreamWorlds.Scene){
        val story=DreamChronicle();val v=DreamJourney.decorate(id,scene).vertices+DreamObjects.geometry(id,story).vertices+DreamEvents.geometry(id).vertices+DreamPhotography.geometry(id).vertices
        verify(v.size%30==0 && v.all{it.isFinite()},"$id valid vertex layout")
        var bad=0
        for(i in v.indices step 10){val length=sqrt(v[i+6]*v[i+6]+v[i+7]*v[i+7]+v[i+8]*v[i+8]);if(abs(length-1f)>.02f)bad++}
        verify(bad==0,"$id nondegenerate normals: $bad")
        val bytes=ByteBuffer.allocate(v.size*4).order(ByteOrder.LITTLE_ENDIAN);bytes.asFloatBuffer().put(v);Files.write(out.resolve("$id.f32"),bytes.array())
    }
    for(id in DreamHorizons.ids)dump(id,DreamHorizons.create(id))
    dump(DreamWaterpark.ID,DreamWaterpark.create())
    dump(DreamWorlds.AQUARIUM,DreamLands.create(DreamWorlds.AQUARIUM))
    for(id in listOf(DreamWorlds.MEADOW,DreamWorlds.HOUSE_CRATER,DreamExpansion.CLOUD_HOTEL))dump(id,DreamLands.create(id))
    val cloudbed=DreamJourney.arrival(DreamHorizons.BED,DreamHorizons.CELESTIAL)
    verify(DreamWorlds.canWalk(DreamHorizons.BED,cloudbed.x,cloudbed.z),"cloud bed spawn supported")
    for(id in listOf(DreamHorizons.TOWER,DreamHorizons.BED)){
        verify(DreamJourney.portalAt(id,0f,8f,0f)==DreamPocket.RETURN,"$id clear return")
        for(z in (if(id==DreamHorizons.BED)-20 else -45)..8)verify(DreamWorlds.canWalk(id,0f,z.toFloat()),"$id central access $z")
    }
    for(z in -145..0){verify(DreamWorlds.canWalk(DreamHorizons.TOWER,0f,z.toFloat()),"tower climb supported $z");val h=DreamJourney.surface(DreamHorizons.TOWER,0f,z.toFloat());verify(h in 0f..22f,"tower floor bounds")}
    val dark=DreamHorizons.DARK
    for(z in -330..30)verify(DreamWorlds.canWalk(dark,0f,z.toFloat()),"new dark central route $z")
    verify(DreamJourney.portalAt(DreamJourney.DARK,0f,-66f,0f)==dark,"old dark connects new dark")
    verify(DreamJourney.portalAt(dark,0f,30f,0f)==DreamJourney.DARK,"new dark can backtrack")
    verify(DreamJourney.portalAt(dark,0f,-330f,0f)==DreamVigilia27.DARK_THREE,"new dark continues third landscape")
    verify(DreamWorlds.HOUSE_MEADOW !in DreamJourney.sequence,"repeated interior removed from main journey")
    val story=DreamChronicle();story.universeSeed=3271
    val photoScenes=listOf(DreamWorlds.MEADOW,DreamWorlds.AQUARIUM,DreamExpansion.CLOUD_HOTEL,DreamHorizons.CELESTIAL)
    for(id in photoScenes){
        val t=DreamPhotography.targets(id).single();val x=t.x;val z=t.z+25f;val y=DreamJourney.surface(id,x,z)+1.65f
        val dx=t.x-x;val dy=t.y-y;val dz=t.z-z;val d=sqrt(dx*dx+dy*dy+dz*dz);val yaw=atan2(dx,-dz);val pitch=asin(dy/d)
        val source=if(DreamHorizons.contains(id))DreamHorizons.create(id)else DreamLands.create(id)
        val sight=DreamSkySight(source.vertices+DreamObjects.geometry(id,story).vertices)
        val found=DreamPhotography.photograph(id,x,y,z,yaw,pitch,story){target,distance->sight.clear(x,y,z,(target.x-x)/distance,(target.y-y)/distance,(target.z-z)/distance,distance-.8f)}
        verify(found.size==1,"$id photographed with actual clear triangles")
        verify(DreamPhotography.photograph(id,x,y,z,yaw,pitch,story).isEmpty(),"$id photograph cannot count twice")
    }
    verify(DreamPhotography.unlocked(story),"four photographs unlock tower")
    val tower=DreamObjects.items(DreamHorizons.CELESTIAL).first{it.kind=="tower"}
    verify(DreamObjects.use(tower,story).pocket==DreamHorizons.TOWER,"unlocked tower door functional")
    verify(DreamObjects.use(tower,DreamChronicle()).pocket==null,"locked tower gives clue")
    val blind=DreamChronicle();val t=DreamPhotography.targets(DreamWorlds.MEADOW).single()
    verify(DreamPhotography.photograph(DreamWorlds.MEADOW,t.x,1.65f,t.z+20f,0f,0f,blind){_,_->false}.isEmpty(),"occluded photographs cannot unlock clue")
    story.checkpoint=DreamChronicle.Checkpoint(DreamHorizons.TOWER,0f,23.65f,-145f,0f,0f)
    story.returnPoint=DreamChronicle.Checkpoint(DreamHorizons.CELESTIAL,0f,8f,-465f,0f,0f)
    val encoded=DreamChronicle.encode(story);val restored=DreamChronicle.decode(encoded)!!
    verify(restored.universeSeed==3271 && restored.flags==story.flags,"planet seed and photographs persist")
    verify(restored.checkpoint==story.checkpoint && restored.returnPoint==story.returnPoint,"tower position/return survive restart")
    val legacy=DreamChronicle();legacy.checkpoint=DreamChronicle.Checkpoint(DreamWorlds.HOUSE_MEADOW,0f,1.65f,-100f,0f,0f);legacy.flags.add("lamp:meadow");legacy.found.add("dream_07:memory")
    val current=DreamChronicle.encode(legacy).substringBefore("\ncrc=")
    val oldBody=current.lines().filter{!it.startsWith("universe=")}.joinToString("\n").replace("schema=23","schema=18")
    val crc=CRC32();crc.update(oldBody.toByteArray());val migrated=DreamChronicle.decode(oldBody+"\ncrc="+java.lang.Long.toHexString(crc.value))!!
    verify(migrated.checkpoint.world==DreamWorlds.HOUSE_CRATER,"old duplicated room migrates to consolidated station")
    verify(migrated.flags.contains("lamp:meadow") && migrated.found.contains("dream_07:memory"),"migration preserves decisions and memories")
    verify(DreamWorlds.canWalk(migrated.checkpoint.world,migrated.checkpoint.x,migrated.checkpoint.z),"migration spawn safe")
    val e=DreamEvents();var footsteps=0
    repeat(200){footsteps+=e.step(DreamHorizons.CITY,0f,50f,0f,true,.1f,story).count{it=="@companion"}}
    val count=footsteps
    repeat(200){footsteps+=e.step(DreamHorizons.CITY,0f,50f,0f,false,.1f,story).count{it=="@companion"}}
    verify(footsteps==count && count>0,"invisible companion footsteps stop with the player")
    val cup=DreamEvents();cup.step(DreamPocket.DAWN,-6f,-18f,PI.toFloat(),false,.1f,story)
    verify("companion:cup" in story.flags,"companion leaves second cup")
    val bus=DreamEvents();repeat(240){bus.step(DreamWorlds.MEADOW,35f,-230f,0f,false,.1f,story)}
    verify("bus:departed" in story.flags && !bus.busVisible,"bus encounter arrives and departs once")
    val change=DreamEvents();repeat(100){change.step(DreamHorizons.TOWER,0f,0f,0f,false,.1f,story)}
    verify(change.transform==1f,"transformation reaches stable completed state")
    val clock=DreamExploration();val memory=DreamMemories.secret(DreamWorlds.HOUSE_CRATER)
    clock.step(DreamWorlds.HOUSE_CRATER,memory.x,memory.z,0f,0f,.1f)
    verify(clock.found.contains("dream_07:memory") && clock.found.contains("dream_08:memory"),"consolidated station preserves both old memories")
    println("PASS $checks new worlds / real photo sightlines / migration / tower / encounters / consolidation checks")
}
