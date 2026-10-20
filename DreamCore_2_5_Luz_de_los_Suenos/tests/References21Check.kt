package com.dreamcore.game

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

fun main(){
    var checks=0;fun verify(v:Boolean,name:String){check(v){name};checks++}
    val out=File(System.getProperty("dreamcore.output","/tmp"));out.mkdirs()
    val report=StringBuilder()
    fun route(id:String,tx:Float,tz:Float):Boolean{
        val a=DreamJourney.arrival(id,"test");val queue=java.util.ArrayDeque<Pair<Int,Int>>();val seen=HashSet<Pair<Int,Int>>();queue.add(a.x.roundToInt() to a.z.roundToInt())
        while(queue.isNotEmpty()&&seen.size<180000){val q=queue.removeFirst();if(!seen.add(q))continue;val x=q.first.toFloat();val z=q.second.toFloat();if(!DreamWorlds.canWalk(id,x,z))continue
            if(hypot(x-tx,z-tz)<1.2f)return true
            for((dx,dz)in listOf(1 to 0,-1 to 0,0 to 1,0 to -1)){val n=q.first+dx to q.second+dz;if(n !in seen)queue.add(n)}
        };return false
    }
    for((index,id)in DreamReferences.ids.withIndex()){
        val scene=DreamReferences.create(id);val vertices=scene.vertices
        verify(vertices.size%30==0 && vertices.all{it.isFinite()},"finite triangle layout $id")
        var invalid=0
        for(i in vertices.indices step 10){val n=sqrt(vertices[i+6].pow(2)+vertices[i+7].pow(2)+vertices[i+8].pow(2));if(abs(n-1f)>.02f)invalid++}
        verify(invalid==0,"unit smooth/flat normals $id: $invalid")
        verify(vertices.size/30<160000,"bounded triangle budget $id: ${vertices.size/30}")
        val bytes=ByteBuffer.allocate(vertices.size*4).order(ByteOrder.LITTLE_ENDIAN);bytes.asFloatBuffer().put(vertices);File(out,"$id.f32").writeBytes(bytes.array())
        report.append("$id ${DreamReferences.title(id)}: ${vertices.size/30} triangles, ${DreamChunks.split(scene).size} chunks\n")
        verify(DreamChronicle.validWorld(id),"valid saved world $id")
        verify(DreamJourney.worldCode(id)==30f+index,"unique sky $id")
        val arrival=DreamJourney.arrival(id,"test");val back=DreamJourney.arrival(id,DreamReferences.next(id))
        verify(DreamWorlds.canWalk(id,arrival.x,arrival.z),"safe forward arrival $id")
        verify(DreamWorlds.canWalk(id,back.x,back.z),"safe backward arrival $id")
        val a=DreamJourney.entry(id);val e=DreamJourney.exit(id)
        verify(DreamJourney.portalAt(id,a.x,a.z,DreamJourney.surface(id,a.x,a.z))==DreamReferences.previous(id),"back portal $id")
        verify(DreamJourney.portalAt(id,e.x,e.z,DreamJourney.surface(id,e.x,e.z))==DreamReferences.next(id),"forward portal $id")
        verify(route(id,e.x,e.z),"connected collision-safe walking route $id")
        verify(!DreamWorlds.canWalk(id,10000f,0f),"outside world blocked $id")
        val state=DreamChronicle();state.checkpoint=DreamChronicle.Checkpoint(id,arrival.x,DreamJourney.surface(id,arrival.x,arrival.z)+1.65f,arrival.z,0f,0f)
        state.returnPoint=DreamChronicle.Checkpoint(DreamVoyage.REFUGE,0f,1.65f,2f,0f,0f)
        val loaded=DreamChronicle.decode(DreamChronicle.encode(state))!!
        verify(loaded.checkpoint==state.checkpoint&&loaded.returnPoint==state.returnPoint,"new world resumes and can return $id")
        val batches=DreamChunks.split(scene)
        var total=0
        for((_,data)in batches){val batch=DreamPerformance.batch(data);total+=batch.data.size;verify(batch.opaque in 0..data.size/10,"opaque/transparent range");verify(batch.data.size==data.size,"no geometry lost")}
        verify(total==vertices.size,"all new-world triangles resident in some chunk")
    }
    verify(DreamJourney.sequence.size==13,"old journey retained")
    verify(DreamJourney.portalAt(DreamWorlds.STATION,0f,-24f,0f)==DreamReferences.VILLAGE,"physical hub entrance")
    verify(DreamObjects.use(DreamObjects.items(DreamVoyage.REFUGE).first{it.key=="door:echoes"},DreamChronicle()).pocket==DreamReferences.VILLAGE,"refuge entrance")
    verify(!DreamWorlds.canWalk(DreamReferences.ATRIUM,0f,-80f),"atrium pit blocked")
    verify(DreamWorlds.canWalk(DreamReferences.ATRIUM,0f,-112f),"atrium bridge supported")
    verify(route(DreamReferences.CINEMA,56f,-20f),"orbital exterior gallery reachable")
    for(z in -130..-50){val a=DreamReferences.height(DreamReferences.ATRIUM,13f,z.toFloat());val b=DreamReferences.height(DreamReferences.ATRIUM,13f,z+1f);verify(abs(a-b)<=.101f,"atrium ramp continuity");verify(a in 0f..8f,"atrium height bounded")}
    for(z in -430..0 step 5){val x=DreamReferences.centre(z.toFloat());verify(DreamWorlds.canWalk(DreamReferences.VALLEY,x,z.toFloat()),"valley path follows actual curve")}
    val shape=DreamMesh();DreamSculpt.rounded(shape,0f,0f,0f,1f,2f,3f,.25f,DreamSculpt.ivory)
    val data=shape.finish().vertices;verify((data.indices step 10).any{abs(data[it+6]) in .1f.. .9f && abs(data[it+7])>.1f},"bevels have actual rounded smooth normals")
    val column=DreamMesh();DreamSculpt.cylinder(column,0f,0f,0f,1f,3f,DreamSculpt.ivory)
    val col=column.finish().vertices
    val top=(col.indices step 30).filter{col[it+1]==3f&&col[it+11]==3f&&col[it+21]==3f}
    verify(top.isNotEmpty() && top.all{col[it+7]>.99f},"column caps face upwards into the light")
    File(out,"reference_geometry.txt").writeText(report.toString())
    println("PASS $checks · Eleven references: traversal, meshes, saves and galleries")
}
