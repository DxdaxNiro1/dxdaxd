package com.dreamcore.game

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

fun main(){
 var checks=0;fun verify(ok:Boolean,label:String){check(ok){label};checks++}
 val out=File(System.getProperty("dreamcore.output","/tmp"));out.mkdirs()
 fun dump(name:String,v:FloatArray){val b=ByteBuffer.allocate(v.size*4).order(ByteOrder.LITTLE_ENDIAN);b.asFloatBuffer().put(v);File(out,"$name.f32").writeBytes(b.array())}
 val story=DreamChronicle();val p=DreamPlay30(story)
 fun use(key:String,id:String=DreamVoyage.REFUGE,x:Float=0f,z:Float=0f)=p.use(key,id,x,DreamJourney.surface(id,x,z)+1.65f,z,0f)
 fun step(id:String,seconds:Float,x:Float=0f,z:Float=0f,yaw:Float=0f,pitch:Float=0f,moving:Boolean=false):List<DreamPlay30.Effect>{val a=ArrayList<DreamPlay30.Effect>();repeat((seconds*30).toInt()){p.step(id,x,DreamJourney.surface(id,x,z)+1.65f,z,yaw,pitch,moving,1f/30)?.let{a.add(it)}};return a}
 for(id in DreamWorld30.ids){val a=DreamJourney.arrival(id,DreamWorlds.STATION);verify(DreamWorlds.canWalk(id,a.x,a.z),"safe arrival $id");verify(DreamJourney.portalAt(id,a.x,a.z,0f)==null,"no spawn in portal $id");verify(DreamChronicle.validWorld(id),"save registry $id");verify(DreamMusic24.track(id)!=null,"unique score $id")}
 verify(use("chalk").text.contains("refugio"),"kit required")
 use("kit");verify(p.flag("kit"),"kit pickup")
 use("ticket:1");use("ticket:2");verify(p.number("ticket")==1,"one destination chosen per journey")
 p.enter(DreamPocket.TRAIN);verify(use("ride:${DreamWorld30.SECRET}",DreamPocket.TRAIN).text.contains("parte"),"secret route boards")
 val travel=step(DreamPocket.TRAIN,12.2f);verify(travel.any{it.target==DreamWorld30.SECRET},"arrival after actual timed trip");verify(p.flag("ticket:spent"),"ticket consumed")
 p.enter(DreamWorld30.SECRET);use("secret-reward",DreamWorld30.SECRET);verify(p.flag("secret:seen:1"),"secret reward")
 use("sleep");verify(p.number("sleep")==1&&!p.flag("ticket:spent")&&p.number("ticket")==0,"sleep reopens choice without losing discovery")
 verify(p.phase(DreamWorlds.MEADOW)==1&&p.phase(DreamHorizons.CITY)==3,"sleep changes time");verify(p.phase(DreamJourney.DARK)==0,"dark mood preserved")
 step(DreamVoyage.REFUGE,3.5f);use("ticket:2");p.enter(DreamPocket.TRAIN);use("ride:${DreamWorld30.SECRET}",DreamPocket.TRAIN);verify(step(DreamPocket.TRAIN,12.3f).any{it.target==DreamWorld30.SECRET},"second route after sleep");verify(p.number("secret-variant")==2,"different secret station")
 p.enter(DreamVoyage.REFUGE);use("chalk",x=3f,z=-4f);verify(p.marks().any{it.chalk&&it.x==3f},"persistent chalk position")
 repeat(3){use("eyes");step(DreamVoyage.REFUGE,3.4f)};verify(p.number("blink")==3&&!p.closing&&p.eyes==0f,"voluntary blink ends");verify(p.marks().count{it.chalk}==2,"displaced chalk after blink")
 use("record");for(s in p.drainSounds())p.observeSound(s.name,s.pan,s.inside,s.gain);p.observeSound("piano2",-.3f,true,.12f);step(DreamVoyage.REFUGE,12.2f);verify(p.value("record").contains("piano2"),"recorder captures virtual audio")
 p.enter(DreamWorld30.LIBRARY);use("play",DreamWorld30.LIBRARY);step(DreamWorld30.LIBRARY,4f);verify(p.flag("sound:response")&&p.drainSounds().any{it.name.startsWith("piano")},"foreign sound causes spatial response")
 for(n in listOf(0,4,0,1))use("piano:$n",DreamWorld30.LIBRARY);verify(p.flag("piano:tune"),"playable melody opens reward")
 verify(p.nearest(DreamWorld30.LIBRARY,-15f,-23f)?.key=="piano-reward","piano reward reachable")
 verify(!p.clearPath(DreamWorld30.LIBRARY,21f,-47f),"projection room initially sealed")
 use("slide:3",DreamWorld30.LIBRARY);verify(p.number("slide")==3&&p.clearPath(DreamWorld30.LIBRARY,21f,-47f),"projection creates enterable space")
 verify(!p.clearPath(DreamWorld30.FAIR,0f,-95f),"full-size gate initially shut");use("model",DreamWorld30.LIBRARY);verify(p.clearPath(DreamWorld30.FAIR,0f,-95f),"miniature opens actual gate")
 verify(!p.clearPath(DreamWorld30.FAIR,-25f,-48f),"water covers lower room");use("water:0",DreamWorld30.FAIR);verify(p.clearPath(DreamWorld30.FAIR,-25f,-48f),"drain reveals stairs");verify(DreamJourney.surface(DreamWorld30.FAIR,-25f,-48f)==-3f,"real lowered floor")
 use("water-reward",DreamWorld30.FAIR);verify(p.flag("water:relic"),"basin reward")
 p.enter(DreamVoyage.REFUGE);use("chair");step(DreamVoyage.REFUGE,.4f,12f,-10.75f);use("chair");val chair=p.nodes(DreamVoyage.REFUGE).first{it.key=="chair"};val pose=p.pose(chair,DreamVoyage.REFUGE)
 verify(hypot(pose.x-12f,pose.z+12f)<.1f,"chair can be moved");verify(p.extraFloor(DreamVoyage.REFUGE,pose.x,pose.z)>1f,"chair supports player height")
 use("window");verify(p.flag("window:${DreamVoyage.REFUGE}"),"chair permits high window")
 step(DreamVoyage.REFUGE,4f,0f,0f,PI.toFloat());verify(p.shadowRise>.5f,"shadow moves independently when unobserved")
 p.enter(DreamWorld30.LIBRARY);use("wardrobe",DreamWorld30.LIBRARY);step(DreamWorld30.LIBRARY,.4f,8f,-70f);use("release",DreamWorld30.LIBRARY);use("hidden-door",DreamWorld30.LIBRARY);verify(p.flag("hidden:wardrobe"),"moving wardrobe reveals secret")
 step(DreamWorld30.LIBRARY,2.5f,0f,-94f,0f,.012f);verify(p.flag("perspective"),"aligned frame opens perspective doorway");verify(use("perspective-door",DreamWorld30.LIBRARY).target==DreamPocket.DAWN,"perspective door leads somewhere")
 p.enter(DreamWorld30.FAIR);val angle=p.carousel;step(DreamWorld30.FAIR,4f,0f,-50f,PI.toFloat());verify(p.carousel>angle,"unseen carousel moves");val before=p.carousel;step(DreamWorld30.FAIR,4f,0f,-50f,0f);val settled=p.carousel;step(DreamWorld30.FAIR,2f,0f,-50f,0f);verify(abs(p.carousel-settled)<.001f&&settled>=before,"watched carousel settles")
 use("telescope",DreamWorld30.FAIR);step(DreamWorld30.FAIR,2.5f,12f,-133f,0f,.30f);verify(p.telescope&&p.flag("sky:sign"),"telescope reveals fine sky detail")
 use("trade:record",DreamWorld30.FAIR);verify(p.flag("gift:glass")&&p.value("record").isEmpty(),"barter consumes offered recording with persistent consequence")
 p.enter(DreamWorlds.MEADOW);repeat(120){p.step(DreamWorlds.MEADOW,it*.03f,1.65f,0f,0f,0f,true,1f/30)};verify(p.mimic.x< -18f,"mimic keeps distance with delayed pose")
 p.enter(DreamWorld30.LIBRARY);step(DreamWorld30.LIBRARY,20f);verify(p.darkAdapt>.99f,"gradual dark adaptation");step(DreamWorlds.STATION,3f);verify(p.darkAdapt<.01f,"exterior adaptation recovers")
 p.toggleLink("tren","agua");p.annotate("agua","Mi hipótesis");verify(p.links().contains("agua:tren")&&p.notes().contains("Mi hipótesis"),"visual journal links and notes")
 repeat(100){use("chalk",x=it.toFloat(),z=-4f)};verify(p.marks().size==72,"marks remain bounded")
 // Sweep the authored maps on a 1 m grid. Every interaction has a reachable approach.
 for(id in DreamWorld30.ids){val start=DreamWorld30.spawn(id);val queue=ArrayDeque<Pair<Int,Int>>();val visited=HashSet<Pair<Int,Int>>();val first=start.x.roundToInt() to start.z.roundToInt();queue.add(first);visited.add(first)
    while(queue.isNotEmpty()){val (x,z)=queue.removeFirst();for((dx,dz)in listOf(1 to 0,-1 to 0,0 to 1,0 to -1)){val q=x+dx to z+dz;if(q !in visited&&DreamWorlds.canWalk(id,q.first.toFloat(),q.second.toFloat())&&p.clearPath(id,q.first.toFloat(),q.second.toFloat())){visited.add(q);queue.add(q)}}}
    for(n in p.nodes(id)){val pos=p.pose(n,id);verify(visited.any{hypot(it.first-pos.x,it.second-pos.z)<n.range},"reachable interaction $id ${n.key}")}
 }
 val restored=DreamChronicle.decode(DreamChronicle.encode(story))!!;val loaded=DreamPlay30(restored);verify(loaded.marks()==p.marks()&&loaded.links()==p.links()&&loaded.notes()==p.notes(),"CRC save roundtrip includes game actions")
 verify(loaded.flag("model:open")&&loaded.number("water",2)==0&&loaded.flag("secret:seen:1"),"cross-world decisions survive restart")
 for(id in DreamWorld30.ids){val scene=DreamWorld30.create(id,story).vertices;verify(scene.all{it.isFinite()}&&scene.size%30==0,"finite geometry $id");dump("variant30_"+id,scene)}
 for(name in listOf("kit","tickets","book","bell","clock","relic","marker","chair","shadow","wardrobe","piano","station","train","board","window","window-frame","window-open","curtain","gate","door","model","projector","bed","valve","water","carousel","horse","telescope","barter","sky-sign")){val v=DreamProps30.model(name).vertices;verify(v.isNotEmpty()&&v.size%30==0&&v.all{it.isFinite()},"model $name");dump("prop30_$name",v)}
 dump("marks30",DreamProps30.marks(p,DreamVoyage.REFUGE).vertices)
 for(id in listOf(DreamWorld30.LIBRARY,DreamWorld30.FAIR,DreamWorld30.SECRET,DreamVoyage.REFUGE,DreamWorlds.STATION,DreamPocket.TRAIN)){
    val args=ArrayList<String>();val pos=if(id==DreamWorld30.FAIR)DreamJourney.Point(0f,-70f)else if(id==DreamWorld30.LIBRARY)DreamJourney.Point(0f,-52f)else DreamJourney.Point(0f,0f)
    DreamProps30.instances(id,p,pos.x,pos.z){name,x,y,z,angle,tilt,scale->args.add("{\"name\":\"$name\",\"position\":[$x,$y,$z],\"angle\":$angle,\"tilt\":$tilt,\"scale\":$scale}")}
    File(out,"instances30_$id.json").writeText("["+args.joinToString(",")+"]")
 }
 println("PASS $checks playable systems checks")
}
