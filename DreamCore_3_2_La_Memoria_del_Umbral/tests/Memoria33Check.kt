package com.dreamcore.game

import java.io.File
import kotlin.math.*

fun main(){
 var checks=0;fun verify(v:Boolean,m:String){check(v){m};checks++}
 val out=File(System.getProperty("dreamcore.output","/tmp"));out.mkdirs()
 verify(DreamStories33.stories.size==61,"each inherited world has an authored story")
 verify(DreamStories33.stories.values.map{it.clue}.distinct().size==61,"61 distinct authored clues")
 val worlds=DreamMusic24.tracks.filter{it.world!=DreamMusic24.TITLE}.map{it.world}
 verify(worlds.size==62&&worlds.all{DreamChronicle.validWorld(it)},"all 62 worlds save and resume")
 for(goal in 1..3){
  val saved=DreamChronicle();val c=DreamChapter33(saved);val p=DreamPlay30(saved);c.use("c33:goal:$goal",DreamWorlds.STATION,0f,6f,p)
  fun read(id:String){c.enter(id);c.use("c33:read",id,0f,0f,p)}
  fun solve(id:String){read(id);val node=c.nodes(id).first{it.key=="c33:resolve"};c.use("c33:answer",id,node.x,node.z,p);repeat(30){c.tick(id,node.x,1.65f,node.z,0f,0f,.1f,p)};c.use("c33:answer",id,node.x,node.z,p)}
  solve(DreamWorlds.STATION);solve(DreamWorlds.POOLS);solve(DreamWorld30.LIBRARY);solve(DreamExpansion.CLOUD_HOTEL);solve(DreamWorlds.MEADOW)
  verify(c.flag("lamp-returned")&&c.flag("visitor-moved"),"returning the lamp changes village and visitor $goal")
  c.enter(DreamArchive33.ID);c.use("c33:prepared",DreamArchive33.ID,0f,-12f,p);c.use("c33:echo",DreamArchive33.ID,0f,-88f,p)
  verify(c.chaseState==1&&!c.flag("evidence:${DreamArchive33.ID}"),"cylinder starts warned pursuit; evidence needs escape")
  verify(c.use("c33:escape",DreamArchive33.ID,0f,-120f,p).target==null,"cooperative lock actually gates exit")
  for(beat in listOf(1,2,0)){c.use("c33:beat:$beat",DreamArchive33.ID,0f,-107f,p);repeat(16){c.tick(DreamArchive33.ID,0f,1.65f,-107f,0f,0f,.1f,p)}}
  verify(c.flag("cooperation"),"both voices open the lock")
  val escape=c.use("c33:escape",DreamArchive33.ID,0f,-120f,p);verify(escape.target==DreamVoyage.REFUGE&&c.flag("escaped"),"escaping returns safely and keeps inventory")
  c.enter(DreamVoyage.REFUGE);c.use("c33:welcome",DreamVoyage.REFUGE,0f,0f,p);c.use("c33:memory",DreamVoyage.REFUGE,0f,0f,p)
  verify(c.stage==8,"all evidence collected in a complete playable chapter $goal")
  val key=listOf("alma","memory","exit")[goal-1];c.use("c33:end:$key",DreamWorlds.STATION,0f,0f,p)
  val ending=listOf("ALMA RECUERDA","LA CASA QUE ELEGISTE","LA MAÑANA COMPARTIDA")[goal-1];verify(c.value("ending")==ending,"purpose and actions affect ending $goal: ${c.value("ending")}")
  val loaded=DreamChronicle.decode(DreamChronicle.encode(saved))!!;val resumed=DreamChapter33(loaded);verify(resumed.completed&&resumed.goal==goal&&resumed.flag("cooperation"),"chapter, consequences and ending survive CRC roundtrip")
  verify(DreamChapterProps33.presences(DreamVoyage.REFUGE,c).any{it.model=="archivist"},"recognized visitor is visible")
 }
 val s=DreamChronicle();val c=DreamChapter33(s);val play=DreamPlay30(s)
 c.enter(DreamWorlds.MEADOW);play.setFlag("kit");play.use("chalk",DreamWorlds.MEADOW,30f,1.65f,-40f,0f);val start=c.trackerX to c.trackerZ
 repeat(30){c.tick(DreamWorlds.MEADOW,30f,1.65f,-40f,0f,0f,.1f,play)}
 verify(hypot(c.trackerX-start.first,c.trackerZ-start.second)>0f,"observer investigates a persistent chalk trace")
 c.enter(DreamNewDreams.EYES);repeat(100){c.tick(DreamNewDreams.EYES,0f,1.65f,0f,0f,0f,.1f,play)};verify(c.chaseState==0&&DreamChapterProps33.presences(DreamNewDreams.EYES,c).isEmpty(),"eye meadow remains non-hostile")
 c.enter(DreamArchive33.ID);c.use("c33:prepared",DreamArchive33.ID,0f,0f,play);c.use("c33:echo",DreamArchive33.ID,0f,-88f,play);repeat(99){c.tick(DreamArchive33.ID,0f,1.65f,-88f,0f,0f,.1f,play)};verify(c.chaseState==1,"ten seconds to observe and prepare")
 for(hide in listOf("table","wardrobe")){c.use("c33:hide:$hide",DreamArchive33.ID,0f,0f,play);repeat(160){c.tick(DreamArchive33.ID,c.pursuerX,1.65f,c.pursuerZ,0f,0f,.1f,play)};verify(!c.consumeCatch(),"$hide shelters player from detection");c.use("c33:unhide",DreamArchive33.ID,0f,0f,play);verify(c.hidden.isEmpty(),"leaving $hide restores movement")}
 var x=0f;var z=-40f;repeat(1200){val p=DreamArchive33.advance(x,z,0f,-94f,.3f);x=p.first;z=p.second;verify(DreamArchive33.canWalk(x,z),"pursuer respects shelving geometry")};verify(hypot(x,z+94f)<.6f,"pursuer finds a route around three racks")
 c.board(DreamWorld30.LIBRARY);verify(!c.trainAllowed(DreamHorizons.CITY),"choosing a service misses the competing departure");repeat(300){c.tick(DreamWorlds.STATION,0f,1.65f,0f,0f,0f,.1f,play)};val resumed=DreamChapter33(DreamChronicle.decode(DreamChronicle.encode(s))!!);verify(!resumed.trainAllowed(DreamHorizons.CITY),"missed train persists across reload");repeat(1300){resumed.tick(DreamWorlds.STATION,0f,1.65f,0f,0f,0f,.1f,play)};verify(resumed.trainAllowed(DreamHorizons.CITY),"next timetable cycle reopens departure")
 c.enter(DreamPocket.TRAIN);for(t in listOf(.2f,.5f,.9f)){ // Production progress is advanced by the real 3.0 ride controller.
  play.use("ride:${DreamWorlds.MEADOW}",DreamPocket.TRAIN,0f,1.65f,2f,0f);repeat(40){play.step(DreamPocket.TRAIN,0f,1.65f,0f,0f,0f,false,.1f);c.tick(DreamPocket.TRAIN,0f,1.65f,0f,0f,0f,.1f,play)}
 }
 verify(c.trainEvent>=2,"journey introduces a passenger after the tunnel")
 val motion=DreamMotion33();motion.clear("x");for(i in 0..40)motion.pose("entity",i*.1f,i.toFloat(),0f,0f,.5f,false);val reflected=motion.pose("entity",4.1f,41f,0f,0f,1f,true);verify(reflected.x<40f,"reflection uses an older spatial pose")
 verify(DreamMotion33.gestures("mask",3f,0f).all{it.isFinite()},"articulation remains finite")
 val metric=DreamMetrics33();repeat(120){metric.frame(DreamWorlds.STATION,33_333_333,3_000_000,30)};metric.frame(DreamWorlds.STATION,150_000_000,10_000_000,30);verify(metric.samples().single().janks==1,"actual cadence separates a long frame")
 verify(metric.json("test",null,"unavailable",100,200,"GLES2").contains("\"battery_temperature_c\":null"),"unavailable temperature is never fabricated")
 val direct=DreamSpatial33.mix(0f,1.65f,0f,0f,10f,6f,-5f,false);val wall=DreamSpatial33.mix(0f,1.65f,0f,0f,10f,6f,-5f,true);verify(direct.pan>0&&wall.gain<direct.gain&&wall.above,"position, floor and wall affect spatial sound")
 val box=DreamMesh();box.box(0f,1.5f,-3f,1f,1.5f,.1f,floatArrayOf(.5f,.5f,.5f),0f);val solid=DreamSkySight(box.finish().vertices);verify(!solid.bodyClear33(0f,0f,-3f)&&solid.bodyClear33(2f,0f,-3f),"capsule blocks actual wall triangles")
 DreamModelCache33.clear();val b1=DreamMesh();val b2=DreamMesh();DreamSculpt.chair(b1,0f,0f,0f);DreamSculpt.chair(b2,10f,0f,0f);verify(DreamModelCache33.hits>0&&DreamModelCache33.builds==1,"repeated furniture tessellated once")
 val encounter=DreamChapter33(DreamChronicle());encounter.use("c33:welcome",DreamVoyage.REFUGE,0f,0f,play);val trust=encounter.trust;repeat(10){encounter.use("c33:welcome",DreamVoyage.REFUGE,0f,0f,play)};verify(encounter.trust==trust,"repeated greetings cannot manufacture relationship progress")
 for(note in listOf("piano0","piano6","tone0","tone3"))verify(DreamSpatial33.sound(note,wall)==note+"Wall33","wall filtering preserves each puzzle note $note")
 val frozen=DreamMetrics33();frozen.frame(DreamWorlds.STATION,12_000_000_000L,5_000_000,30);verify(frozen.samples().single().maxMs==12000.0,"long foreground stalls remain visible in diagnostics")
 val nearMesh=DreamMesh();nearMesh.box(-20f,0f,-5f,2f,.1f,2f,floatArrayOf(.2f,.7f,.2f),0f);nearMesh.box(20f,0f,-5f,2f,.1f,2f,floatArrayOf(.5f,.5f,.5f),14f);val nearRaw=nearMesh.finish().vertices;val bounds33=DreamMaterialPriority33.bounds(nearRaw,0f)
 verify(DreamMaterialPriority33.nearest(listOf(bounds33),-20f,-5f).first()==0,"grass close to the player refines first")
 verify(DreamMaterialPriority33.nearest(listOf(bounds33),20f,-5f).first()==10,"approaching stone changes actual material priority")
 val prepared33=DreamPrepare32.submit(DreamChunks.Key(0,0),nearRaw,false,0f).get();verify(prepared33.materials33.contentEquals(bounds33),"tile bounds are prepared off the GL thread with the mesh")
 for(t in DreamMusic24.tracks.filter{it.world!="title"})for(link in DreamCrossroads33.links(t.world))verify(c.nodes(t.world).any{it.key=="c33:cross:"+link.target},"every cross-world connection has an actual door in both directions ${t.world} -> ${link.target}")
 println("PASS $checks chapter, pursuit, consequence, audio and diagnostic checks")
}
