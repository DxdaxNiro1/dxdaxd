package com.dreamcore.game

import java.io.File

/** Factory-retention probe; explicit GC is used only in this desktop diagnostic, never in the game. */
fun main(){
 val optimized=System.getProperty("dreamcore.optimized","false").toBoolean();val rt=Runtime.getRuntime();val story=DreamChronicle();story.universeSeed=3271
 DreamBackrooms.configure(story.universeSeed);DreamMystery.configure(story.universeSeed);DreamMystery.sync(story)
 val worlds=DreamMusic24.tracks.filter{it.world!=DreamMusic24.TITLE}.map{it.world}
 val samples=ArrayList<Long>();var triangles=0L
 for(cycle in 0 until 3){for(id in worlds){
  if(optimized)DreamCache28.retain(id)
  val scene=when{
   DreamVigilia27.contains(id)->DreamVigilia27.create(id)
   id==DreamMystery.HOUSE->DreamMystery.geometry(id,story)
   DreamRoutes23.contains(id)->DreamRoutes23.create(id)
   DreamNewDreams.contains(id)->DreamNewDreams.create(id)
   DreamReferences.contains(id)->DreamReferences.create(id)
   DreamVoyage.contains(id)->DreamVoyage.create(id,story)
   DreamHorizons.contains(id)->DreamHorizons.create(id)
   id==DreamWaterpark.ID->DreamWaterpark.create()
   DreamPocket.contains(id)->DreamPocket.create(id)
   DreamLands.contains(id)->DreamLands.create(id)
   id==DreamJourney.DARK->DreamDarkMap.create()
   id==DreamWorlds.STATION->DreamJourney.createHub()
   id==DreamWorlds.POOLS->DreamWorlds.createPools()
   id==DreamWorlds.CRATER->DreamWorlds.createCrater()
   else->error(id)
  }
  triangles+=scene.vertices.size/30
 }
  System.gc();System.runFinalization();System.gc()
  samples.add(rt.totalMemory()-rt.freeMemory())
 }
 val text="{\"optimized\":$optimized,\"transitions\":${worlds.size*3},\"factory_retained_heap_bytes\":${samples.joinToString(prefix="[",postfix="]")},\"triangles_built\":$triangles,\"phone_measurement\":false}"
 println(text);File(System.getProperty("dreamcore.memoryReport","/tmp/memory28.json")).writeText(text+"\n")
}
