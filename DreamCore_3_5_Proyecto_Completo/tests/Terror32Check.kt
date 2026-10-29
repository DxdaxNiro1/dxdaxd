package com.dreamcore.game
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*
fun main(){
 var checks=0;fun verify(v:Boolean,n:String){check(v){n};checks++}
 val out=File(System.getProperty("dreamcore.output","/tmp"));out.mkdirs()
 fun dump(name:String,data:FloatArray){val bytes=ByteBuffer.allocate(data.size*4).order(ByteOrder.LITTLE_ENDIAN);bytes.asFloatBuffer().put(data);File(out,name+".f32").writeBytes(bytes.array())}
 fun path(id:String):Boolean{val queue=java.util.ArrayDeque<Pair<Int,Int>>();val seen=HashSet<Pair<Int,Int>>();queue.add(0 to 7)
  while(queue.isNotEmpty()){val q=queue.removeFirst();if(!seen.add(q))continue;val x=q.first.toFloat();val z=q.second.toFloat();if(!DreamWorlds.canWalk(id,x,z))continue
   if(hypot(x,z+DreamHorror32.end(id))<1.2f)return true
   val h=DreamJourney.surface(id,x,z)
   for((dx,dz)in listOf(1 to 0,-1 to 0,0 to 1,0 to -1)){val next=q.first+dx to q.second+dz;if(next !in seen&&abs(DreamJourney.surface(id,next.first.toFloat(),next.second.toFloat())-h)<.6f)queue.add(next)}
  };return false}
 for((index,id)in DreamHorror32.ids.withIndex()){
  verify(path(id),"complete supported walking route $id")
  verify(DreamJourney.worldCode(id)==55f+index,"unique shader identity $id")
  verify(DreamJourney.portalAt(id,0f,13f,0f)==(DreamHorror32.ids.getOrNull(index-1)?:DreamPocket.RETURN),"return portal $id")
  verify(DreamJourney.portalAt(id,0f,-DreamHorror32.end(id),0f)==(DreamHorror32.ids.getOrNull(index+1)?:DreamWorlds.STATION),"named exit destination $id")
  val back=DreamJourney.arrival(id,DreamHorror32.ids.getOrNull(index+1)?:DreamWorlds.STATION);verify(DreamWorlds.canWalk(id,back.x,back.z),"safe backwards arrival $id")
  verify(DreamPortalNames.gates(id).size==2,"both physical gates labelled $id")
  val mesh=DreamHorror32.create(id);verify(mesh.vertices.all{it.isFinite()}&&mesh.vertices.size%30==0,"finite geometry $id")
  verify(mesh.vertexCount<350000,"scene bounded independently of device $id: ${mesh.vertexCount}")
  val state=DreamChronicle();state.checkpoint=DreamChronicle.Checkpoint(id,0f,1.65f,7f,0f,0f);verify(DreamChronicle.decode(DreamChronicle.encode(state))!!.checkpoint==state.checkpoint,"checkpoint roundtrip $id")
 }
 val report=StringBuilder("model\tlod\tvertices\tbytes\n")
 for(name in DreamEntities32.models){var high=0
  for(lod in 0..1){val raw=DreamEntities32.model(name,lod).vertices
   verify(raw.isNotEmpty()&&raw.size%30==0&&raw.all{it.isFinite()},"finite modeled body $name/$lod")
   verify(raw.size/10<150000,"bounded entity $name/$lod ${raw.size/10}")
   verify((raw.indices step 10).all{raw[it+3] in 0f..1f&&raw[it+4] in 0f..1f&&raw[it+5] in 0f..15f},"atlas coordinates valid $name/$lod")
   val bounds=DreamPerformance.bounds(raw);verify(bounds.y1-bounds.y0>1f&&bounds.z1-bounds.z0>.25f,"volumetric silhouette $name/$lod")
   val indexed=DreamIndexed28.encode(raw,includeShadow=false);verify(DreamIndexed28.expand(indexed).contentEquals(raw),"entity detail survives ushort indexing $name/$lod")
   if(lod==0)high=raw.size else verify(raw.size<high,"distant mesh cheaper $name")
   report.append("$name\t$lod\t${raw.size/10}\t${indexed.bytes}\n");dump("entity32_${name}_$lod",raw)
  }
 }
 File(out,"entity32_stats.tsv").writeText(report.toString());File(out,"entity32_vertex.glsl").writeText(DreamEntityShaders32.vertex);File(out,"entity32_fragment.glsl").writeText(DreamEntityShaders32.fragment)
 for(t in DreamMusic24.tracks.filter{it.world!="title"}){
  val list=DreamEntities32.placements(t.world);verify(list.size<=(if(t.world==DreamNewDreams.EYES)5 else 2),"sparse authored placement ${t.world}")
  val rows=list.joinToString(","){p->"{\"name\":\"${p.model}\",\"position\":[${p.x},${DreamJourney.surface(t.world,p.x,p.z)+p.y},${p.z}],\"scale\":${p.scale},\"yaw\":${p.yaw},\"phase\":${p.phase}}"}
  File(out,"entities32_${t.world}.json").writeText("[$rows]")
 }
 val s=DreamChronicle();val fear=DreamEncounter32()
 repeat(400){fear.tick(DreamHorror32.WITNESSES,0f,0f,PI.toFloat(),.1f,s)}
 verify(fear.approach>0f&&fear.approach<=14.1f,"masked entity approaches only while unwatched")
 val before=fear.approach;repeat(50){fear.tick(DreamHorror32.WITNESSES,0f,0f,0f,.1f,s)}
 verify(fear.approach==before,"looking stops distant approach")
 verify(s.flags.contains("v32:witness:"+DreamHorror32.WITNESSES),"encounter remembered")
 verify(DreamMystery.diary(s).contains(DreamHorror32.title(DreamHorror32.WITNESSES)),"encounter reaches visible diary")
 val eye=DreamEncounter32();repeat(1000){eye.tick(DreamNewDreams.EYES,0f,-80f,PI.toFloat(),.1f,s)};verify(eye.approach==0f,"eye meadow has no pursuit or attack")
 // Actual worker returns exactly the same data without touching GL, including opaque boundary.
 val sample=DreamHorror32.create(DreamHorror32.ATRIUM).vertices;val key=DreamChunks.Key(0,0);val ready=DreamPrepare32.submit(key,sample,true).get();val ordered=DreamPerformance.batch(sample)
 verify(DreamIndexed28.expand(ready.mesh).contentEquals(ordered.data)&&ready.opaque==ordered.opaque,"asynchronous worker preserves all vertex bits and phases")
 for(page in ready.mesh.pages){val layers=DreamSurface32.partition(page);verify(layers.first.size+layers.second.size==page.opaqueIndices,"partition conserves every opaque index")}
 // Independent small scenes make loss of flowers, glass, cloudy ceiling and oil sheen visible in GLES comparisons.
 val indexedDir=File(out,"indexed");indexedDir.mkdirs()
 fun shorts(name:String,a:ShortArray){val bytes=ByteBuffer.allocate(a.size*2).order(ByteOrder.LITTLE_ENDIAN);bytes.asShortBuffer().put(a);File(indexedDir,name).writeBytes(bytes.array())}
 for(k in listOf(91,93,108,110)){
  val b=DreamMesh();if(k==110)b.box(0f,3f,-9f,6f,3f,.1f,floatArrayOf(.53f,.58f,.60f),k.toFloat())else b.box(0f,-.1f,-8f,6f,.1f,8f,if(k==108)floatArrayOf(.3f,.48f,.22f)else floatArrayOf(.6f,.6f,.6f),k.toFloat())
  val raw=b.finish().vertices;dump("feature32_$k",raw);val mesh=DreamIndexed28.encode(raw)
  for((n,page)in mesh.pages.withIndex()){val name="feature32_${k}_$n";dump("indexed/"+name,page.vertices);shorts(name+".u16",page.indices);shorts(name+".shadow.u16",page.shadow);val layers=DreamSurface32.partition(page);shorts(name+".surface.u16",layers.first);shorts(name+".fx.u16",layers.second);verify(layers.first.size==page.indices.size,"special material follows compact stream $k")}
 }
 println("PASS $checks horror/render scheduling checks")
}
