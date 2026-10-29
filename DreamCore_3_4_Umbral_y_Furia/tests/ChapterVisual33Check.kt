package com.dreamcore.game
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
fun main(){
 var checks=0;fun verify(v:Boolean,m:String){check(v){m};checks++}
 val out=File(System.getProperty("dreamcore.output","/tmp"));out.mkdirs()
 fun dump(name:String,v:FloatArray){val b=ByteBuffer.allocate(v.size*4).order(ByteOrder.LITTLE_ENDIAN);b.asFloatBuffer().put(v);File(out,"$name.f32").writeBytes(b.array())}
 val names=listOf("c33-letter","c33-reliquary","c33-clock","c33-cylinder","c33-cup","c33-table","c33-wardrobe","c33-memory","c33-door","c33-lamp","c33-lit-window")
 for(name in names){val v=DreamChapterProps33.model(name).vertices;verify(v.isNotEmpty()&&v.size%30==0&&v.all{it.isFinite()},"finite real story model $name");dump("prop30_$name",v)}
 fun scenario(key:String,id:String,setup:(DreamChapter33,DreamPlay30)->Unit){val s=DreamChronicle();val c=DreamChapter33(s);val p=DreamPlay30(s);c.use("c33:goal:1",DreamWorlds.STATION,0f,6f,p);c.enter(id);setup(c,p)
  val props=ArrayList<String>();DreamProps30.instances(id,p,0f,0f){name,x,y,z,angle,tilt,scale->props.add("{\"name\":\"$name\",\"position\":[$x,$y,$z],\"angle\":$angle,\"tilt\":$tilt,\"scale\":$scale}")};DreamChapterProps33.instances(id,c,p){name,x,y,z,angle,tilt,scale->props.add("{\"name\":\"$name\",\"position\":[$x,$y,$z],\"angle\":$angle,\"tilt\":$tilt,\"scale\":$scale}")}
  verify(props.isNotEmpty(),"visible story actions $key");File(out,"instances33_$key.json").writeText(props.joinToString(",","[","]"))
  val actors=DreamEntities32.placements(id)+DreamChapterProps33.presences(id,c);val rows=actors.joinToString(","){q->"{\"name\":\"${q.model}\",\"position\":[${q.x},${DreamJourney.surface(id,q.x,q.z)+q.y},${q.z}],\"scale\":${q.scale},\"yaw\":${q.yaw},\"phase\":${q.phase}}"};File(out,"entities33_$key.json").writeText("[$rows]")
  val profile=DreamArt33.profile(id);File(out,"state33_$key.json").writeText("{\"art\":[${profile.joinToString(",")}],\"chapter\":[${if(c.past)1 else 0},${c.giant},${if(c.flag("lamp-returned"))1 else 0},${if(c.completed)1 else 0}]}")
 }
 scenario("umbral",DreamWorlds.STATION){_,_->}
 scenario("archivo",DreamArchive33.ID){c,p->c.use("c33:prepared",DreamArchive33.ID,0f,0f,p);c.use("c33:echo",DreamArchive33.ID,0f,-88f,p)}
 scenario("recuerdo",DreamVoyage.REFUGE){c,p->c.use("c33:welcome",DreamVoyage.REFUGE,0f,0f,p);c.use("c33:memory",DreamVoyage.REFUGE,0f,0f,p)}
 scenario("ventana",DreamWorlds.MEADOW){c,_->c.mark("lamp-returned");c.mark("visitor-moved")}
 scenario("biblioteca",DreamWorld30.LIBRARY){c,_->c.mark("visitor-moved")}
 scenario("tren",DreamPocket.TRAIN){c,p->p.use("ride:${DreamWorld30.LIBRARY}",DreamPocket.TRAIN,0f,1.65f,0f,0f);repeat(90){p.step(DreamPocket.TRAIN,0f,1.65f,0f,0f,0f,false,.1f);c.tick(DreamPocket.TRAIN,0f,1.65f,0f,0f,0f,.1f,p)}}
 scenario("gigante",DreamWorlds.AQUARIUM){c,p->repeat(180){c.tick(DreamWorlds.AQUARIUM,0f,1.65f,-60f,0f,0f,.1f,p)};verify(c.giant>.1f,"giant changes actual surroundings")}
 for(t in DreamMusic24.tracks.filter{it.world!="title"})scenario("map_"+t.world,t.world){_,_->}
 val story=DreamChronicle();val c=DreamChapter33(story);val p=DreamPlay30(story)
 for(t in DreamMusic24.tracks.filter{it.world!="title"}){c.mark("read:${t.world}");c.mark("resolved:${t.world}");c.set("choice:${t.world}",DreamStories33.get(t.world).answer);c.mark("past:${t.world}")}
 verify(DreamChronicle.decode(DreamChronicle.encode(story))!=null,"all 62 authored resolutions fit existing bounded save")
 File(out,"profiles33.json").writeText(DreamMusic24.tracks.filter{it.world!="title"}.joinToString(",","{","}"){t->"\"${DreamJourney.worldCode(t.world).toInt()}\":[${DreamArt33.profile(t.world).joinToString(",")}]"})
 val m=DreamMetrics33();m.frame(DreamWorlds.STATION,33_333_333,1_000_000,30);File(out,"diagnostic_fixture33.json").writeText(m.json("quote\" slash\\ tab\t control\u0001",null,"unavailable",1,2,"GLES2"))
 println("PASS $checks visual story/export and full-save checks")
}
