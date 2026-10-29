package com.dreamcore.game
import java.io.File
import kotlin.math.*
fun main(){var count=0;fun verify(v:Boolean,m:String){check(v){m};count++}
 val world=DreamRainward34.ID
 verify(DreamFatal35.styles.size==18&&DreamFatal35.styles.values.map{it.id}.distinct().size==18,"18 unique encounter styles")
 verify(DreamFatal35.styles.keys.all{it in DreamEntities32.models},"all attacks correspond to real models")
 for((name,style)in DreamFatal35.styles){
  val p=DreamEntities32.Presence(name,0f,0f,-4f);val fatal=DreamFatal35();fatal.reset(world,0f)
  var frame=DreamFatal35.Frame();var cries=0;var hold=false;var looking=false;var blood=false
  repeat(180){frame=fatal.step(world,0f,1.65f,-2f,.05f,listOf(p));if(frame.scream.isNotEmpty()){cries++;verify(frame.scream=="attack_${style.sound}35","matching cry $name")};hold=hold||frame.held;looking=looking||(frame.held&&frame.progress in .28f.. .45f);blood=blood||frame.blood>.5f}
  verify(hold&&looking&&blood&&frame.dead&&cries==1,"full grasp/look/rage/death sequence with one cry $name")
  verify(frame.fade==1f&&frame.blood==1f,"death ends in stable fade $name")
  fatal.reset(world,0f);repeat(30){frame=fatal.step(world,0f,1.65f,-2f,.05f,listOf(p)){_,_,_->false}};verify(!frame.held&&frame.warning==0f,"no capture through occlusion $name")
  fatal.reset(world,0f);repeat(30){frame=fatal.step(world,0f,50f,-2f,.05f,listOf(p))};verify(!frame.held,"floor separation respected $name")
  fatal.reset(world,0f);repeat(10){frame=fatal.step(world,0f,1.65f,-2f,.05f,listOf(p))};verify(!frame.held&&frame.warning>0f,"warning gives time to retreat $name")
  repeat(100){frame=fatal.step(world,0f,1.65f,20f,.05f,listOf(p))};verify(!frame.held&&frame.warning==0f,"retreat cancels warning $name")
  fatal.reset(world,10f);repeat(100){frame=fatal.step(world,0f,1.65f,-2f,.05f,listOf(p))};verify(!frame.held,"respawn grace $name")
  fatal.reset(world,0f);repeat(40){frame=fatal.step(world,0f,1.65f,-2f,.05f,listOf(p))};frame=fatal.step(DreamWorlds.STATION,0f,1.65f,6f,.05f,listOf(p));verify(!frame.held&&!frame.dead,"world switch clears encounter $name")
 }
 for(model in listOf("archivist","friendly","guide","postman","moons"))verify(!DreamFatal35.hostile(world,model),"friendly/scenery excluded $model")
 verify(DreamEntities32.models.all{!DreamFatal35.hostile(DreamVoyage.REFUGE,it)&&!DreamFatal35.hostile(DreamWorlds.STATION,it)},"safe refuge and hub")
 val hub=DreamHub35.create();val sight=DreamSkySight(hub.vertices);verify(hub.vertexCount<40000&&hub.vertices.all{it.isFinite()},"bounded finite hub")
 verify(DreamHub35.sectors.map{it.rgb}.distinct().size==5,"five distinct colours")
 verify(DreamHub35.gates.size==10&&DreamHub35.gates.map{it.target}.distinct().size==10,"ten unique named destinations")
 val queue=java.util.ArrayDeque<Pair<Int,Int>>();val visited=HashSet<Pair<Int,Int>>();val reachable=HashSet<Pair<Int,Int>>();queue.add(0 to 6)
 while(queue.isNotEmpty()){
  val p=queue.removeFirst();if(!visited.add(p))continue
  val x=p.first.toFloat();val z=p.second.toFloat();if(!DreamHub35.canWalk(x,z)||!sight.bodyClear33(x,DreamJourney.surface(DreamWorlds.STATION,x,z),z))continue
  reachable.add(p)
  for((dx,dz)in listOf(1 to 0,-1 to 0,0 to 1,0 to -1)){val n=p.first+dx to p.second+dz;if(n !in visited&&n.first in -43..43&&n.second in -45..19)queue.add(n)}
 }
 for(g in DreamHub35.gates){
  verify((g.x.toInt() to (g.z+2).toInt())in reachable,"reachable approach ${g.title}")
  verify(DreamPortalNames.gates(DreamWorlds.STATION).any{abs(it.x-g.x)<.1f&&abs(it.z-g.z)<.1f&&it.name==g.label&&it.colour==g.sector.rgb},"geometry/sign/colour agreement ${g.title}")
  if(g.node.isEmpty())verify(DreamJourney.portalAt(DreamWorlds.STATION,g.x,g.z,0f)==g.target,"trigger/destination agreement ${g.title}")
  else verify(DreamPlay30(DreamChronicle()).nodes(DreamWorlds.STATION).any{it.key==g.node&&it.x==g.x&&it.z==g.z},"train/library node agreement")
 }
 // Warning, freezing and death must not edit persistent story progress.
 val story=DreamChronicle();story.flags.add("remember-this");story.symbols.add("ESPIRAL");val saved=DreamChronicle.encode(story);val simulation=DreamFatal35();repeat(200){simulation.step(world,0f,1.65f,-2f,.05f,listOf(DreamEntities32.Presence("tenant",0f,0f,-4f)))};verify(DreamChronicle.encode(story)==saved,"encounter leaves existing progress untouched")
 val out=File(System.getProperty("dreamcore.output","/tmp"));File(out,"attack35_styles.json").writeText(DreamFatal35.styles.entries.joinToString(",","[","]"){(name,s)->"{\"name\":\"$name\",\"style\":${s.id},\"duration\":${s.duration}}"})
 println("PASS $count hub organisation and fatal encounter checks")
}
