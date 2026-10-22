package com.dreamcore.game

import kotlin.math.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Paths

fun main(){
    var checks=0
    fun verify(ok:Boolean,message:String){check(ok){message};checks++}
    val story=DreamChronicle()
    verify(story.ending()=="EL_ECO","unresolved journey ends as echo")
    val lamp=DreamObjects.items(DreamWorlds.MEADOW).first{it.kind=="lamp"}
    DreamObjects.use(lamp,story)
    verify("lamp:meadow" in story.flags,"lamp switch remembers state")
    verify(story.answer("phone:meadow").contains("lámpara"),"another world's phone acknowledges lamp")
    val drawer=DreamObjects.items(DreamWorlds.HOUSE_MEADOW).first()
    DreamObjects.use(drawer,story)
    val hotel=DreamObjects.items(DreamExpansion.CLOUD_HOTEL)
    val door=hotel.first{it.kind=="door"};val radio=hotel.first{it.kind=="radio"}
    verify(DreamObjects.use(door,story).pocket==DreamPocket.LAUNDRY,"first hotel door opens laundry")
    DreamObjects.use(radio,story)
    verify(DreamObjects.use(door,story).pocket==DreamPocket.DAWN,"key plus radio changes hotel door destination")
    story.answer("phone:hotel");story.answer("phone:park")
    verify(story.ending()=="PERMANECER","calls without memories can choose remaining")
    for(id in DreamLands.ids)story.found.add("$id:memory")
    for(g in listOf("OJO","ESPIRAL","CASA","SOL"))DreamObjects.use(DreamObjects.Item("symbol:$g",0f,0f,"symbol"),story)
    story.choose(true)
    verify(story.ending()=="DESPERTAR","complete motif, memories, calls and returned ticket awaken")
    story.choose(false);verify(story.ending()=="PERMANECER","keeping ticket changes ending")
    verify("choice:awaken" !in story.flags,"choices exclusive")
    story.endings.add(story.ending())
    story.enter(DreamWorlds.MEADOW);verify(story.layout(DreamWorlds.MEADOW)==0,"first visit original street")
    story.enter(DreamWorlds.MEADOW);verify(story.layout(DreamWorlds.MEADOW)==1,"return changes street")
    val street=DreamObjects.items(DreamWorlds.MEADOW).first{it.kind=="street"}
    val a=DreamObjects.use(street,story).warp!!
    story.enter(DreamWorlds.MEADOW);val b=DreamObjects.use(street,story).warp!!
    verify(a!=b,"street actually leads to different gardens")
    verify(DreamWorlds.canWalk(DreamWorlds.MEADOW,a.x,a.z)&&DreamWorlds.canWalk(DreamWorlds.MEADOW,b.x,b.z),"changed garden landings walkable")
    story.checkpoint=DreamChronicle.Checkpoint(DreamPocket.TRAIN,0f,1.65f,-40f,.25f,-.15f)
    story.returnPoint=DreamChronicle.Checkpoint(DreamWorlds.AQUARIUM,12f,1.65f,-245f,1f,.1f)
    val moon=DreamMoonLife();val yaw=atan2(DreamMoonLife.MAIN_X,-DreamMoonLife.MAIN_Z);val pitch=asin(DreamMoonLife.MAIN_Y)
    repeat(1205){moon.step(true,0f,1.65f,36f,yaw,pitch,.1f)}
    story.moon=moon.snapshot()
    val encoded=DreamChronicle.encode(story);val loaded=DreamChronicle.decode(encoded)!!
    verify(loaded.checkpoint==story.checkpoint && loaded.returnPoint==story.returnPoint,"position and return door survive restart")
    verify(loaded.flags==story.flags && loaded.found==story.found && loaded.calls==story.calls && loaded.symbols==story.symbols,"choices, memories, calls, motif survive restart")
    verify(loaded.visits==story.visits && loaded.endings==story.endings,"return variants and endings survive restart")
    val restored=DreamMoonLife();restored.restore(loaded.moon)
    verify(restored.giantVisible && abs(restored.vigil-moon.vigil)<.01f,"lunar event survives restart")
    for(index in listOf(0,encoded.length/2,encoded.length-1)){
        val corrupted=encoded.substring(0,index)+(if(encoded[index]=='x')'y' else 'x')+encoded.substring(index+1)
        verify(DreamChronicle.decode(corrupted)==null,"corruption detected at $index")
        verify(DreamChronicle.recover(corrupted,encoded).checkpoint==story.checkpoint,"previous snapshot recovers corruption")
    }
    verify(DreamChronicle.decode(null)==null && DreamChronicle.recover(null,null).checkpoint.world==DreamWorlds.STATION,"fresh installation safe")
    val exploration=DreamExploration(loaded.found)
    val memory=DreamMemories.secret(DreamWorlds.MEADOW)
    exploration.step(DreamWorlds.MEADOW,memory.x,memory.z,0f,0f,.1f)
    verify(exploration.memoriesFound==9,"loaded memories cannot duplicate")
    val out=Paths.get(System.getProperty("dreamcore.output","build"));Files.createDirectories(out)
    fun dump(id:String,scene:DreamWorlds.Scene){val vertices=scene.vertices+DreamObjects.geometry(id,story).vertices;val bytes=ByteBuffer.allocate(vertices.size*4).order(ByteOrder.LITTLE_ENDIAN);bytes.asFloatBuffer().put(vertices);Files.write(out.resolve("$id.f32"),bytes.array());verify(vertices.all{it.isFinite()}&&vertices.size%30==0,"$id valid triangles")}
    for(id in listOf(DreamPocket.LAUNDRY,DreamPocket.TRAIN,DreamPocket.DAWN)){
        dump(id,DreamPocket.create(id))
        val spawn=DreamJourney.arrival(id,DreamWorlds.STATION)
        verify(DreamWorlds.canWalk(id,spawn.x,spawn.z),"$id spawn supported")
        for(z in -98..8)if(z>=if(id==DreamPocket.TRAIN)-98 else -53)verify(DreamWorlds.canWalk(id,0f,z.toFloat()),"$id central route unblocked $z")
        verify(DreamJourney.portalAt(id,0f,8f,0f)==DreamPocket.RETURN,"$id explicit return door")
        for(o in DreamObjects.items(id))verify(DreamWorlds.canWalk(id,o.x,o.z),"$id object approachable")
    }
    dump(DreamWaterpark.ID,DreamJourney.decorate(DreamWaterpark.ID,DreamWaterpark.create()))
    for(z in -488..84){val x=DreamWaterpark.centre(z.toFloat());verify(DreamWorlds.canWalk(DreamWaterpark.ID,x,z.toFloat()),"park curved route supported $z")}
    verify(!DreamWorlds.canWalk(DreamWaterpark.ID,110f,-100f),"no walking in waterpark void")
    val weather=DreamAtmosphere();val notices=mutableListOf<String>()
    repeat(1600){weather.step(DreamWorlds.MEADOW,5f,0f,.1f,1)?.let{notices.add(it)}}
    verify(notices.size==1,"rare phenomenon once per cycle, not every frame")
    val night=DreamAtmosphere();var faded=false
    repeat(1000){night.step(DreamJourney.DARK,0f,36f,.1f,1);if(night.type==4&&night.strength>.9f)faded=true}
    verify(faded,"stars fade gradually in night world")
    val actors=DreamActors();actors.step(DreamWorlds.MEADOW,0f,0f,PI.toFloat(),.1f,story)
    val before=actors.actors.first().let{hypot(it.x,it.z)}
    repeat(100){actors.step(DreamWorlds.MEADOW,0f,0f,PI.toFloat(),.1f,story)}
    verify(actors.actors.first().let{hypot(it.x,it.z)}<before,"curious figure approaches while unseen")
    val watcher=DreamActors();watcher.step(DreamExpansion.CLOUD_HOTEL,80f,-40f,0f,.1f,story)
    repeat(35){watcher.step(DreamExpansion.CLOUD_HOTEL,80f,-40f,0f,.1f,story)}
    watcher.step(DreamExpansion.CLOUD_HOTEL,80f,-40f,PI.toFloat(),.1f,story)
    verify(watcher.actors.first().x<0f && watcher.traceLife>0f,"window watcher moves when distracted and leaves traces")
    println("PASS $checks persistence / consequences / secrets / endings / waterpark / entities / weather checks")
}
