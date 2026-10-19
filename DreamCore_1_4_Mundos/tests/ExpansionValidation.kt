package com.dreamcore.game

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.*

/** Runs the actual expansion navigation and actual generated vertices on JVM. */
fun main(args: Array<String>) {
    var checks=0
    val normalFailures=mutableListOf<String>()
    fun verify(ok:Boolean,message:String){check(ok){message};checks++}
    val output=Paths.get(args.firstOrNull() ?: "build/gameplay-validation")
    Files.createDirectories(output)
    fun export(name:String,scene:DreamWorlds.Scene) {
        verify(scene.vertices.size%30==0,"$name incomplete triangles")
        verify(scene.vertices.all{it.isFinite()},"$name nonfinite vertex data")
        var zeroNormals=0;var badNormals=0
        for(i in scene.vertices.indices step 10) {
            val len=sqrt(scene.vertices[i+6].pow(2)+scene.vertices[i+7].pow(2)+scene.vertices[i+8].pow(2))
            if(len<.01f)zeroNormals++
            else if(abs(len-1f)>.005f)badNormals++
        }
        if(zeroNormals!=0)normalFailures.add("$name $zeroNormals zero normals")
        if(badNormals!=0)normalFailures.add("$name $badNormals nonunit normals")
        val bytes=ByteBuffer.allocate(scene.vertices.size*4).order(ByteOrder.LITTLE_ENDIAN)
        bytes.asFloatBuffer().put(scene.vertices)
        Files.write(output.resolve("${name}14.f32"),bytes.array())
        println("GEOMETRY $name ${scene.vertexCount/3} triangles ${bytes.capacity()} bytes")
    }
    val home=DreamWorlds.STATION
    val onward=listOf(DreamExpansion.TUNNELS,DreamExpansion.CLOUD_HOTEL,DreamExpansion.ROAD,DreamExpansion.CHESS)
    val portalZ=listOf(60f,52f,40f,60f)
    val endZ=listOf(-60f,-52f,-40f,-66f)
    val minX=-100f;val minZ=-110f;val step=.5f
    val width=401;val height=441
    for((worldIndex,id) in DreamExpansion.ids.withIndex()) {
        val (sx,sz)=DreamExpansion.spawn(id)
        verify(DreamExpansion.canWalk(id,sx,sz),"$id spawn blocked")
        verify(!DreamExpansion.canWalk(id,10000f,10000f),"$id must bound movement")
        verify(!DreamExpansion.canWalk(id,Float.NaN,0f),"$id NaN accepted")
        verify(!DreamExpansion.canWalk(id,0f,Float.POSITIVE_INFINITY),"$id Infinity accepted")
        verify(DreamExpansion.portalAt(id,0f,portalZ[worldIndex])==home,"$id home portal mismatch")
        verify(DreamExpansion.portalAt(id,0f,endZ[worldIndex])==onward[worldIndex],"$id onward portal mismatch")
        verify(DreamExpansion.portalAt(id,sx,sz)==null,"$id spawn auto-overlaps portal")
        val startX=((sx-minX)/step).roundToInt();val startZ=((sz-minZ)/step).roundToInt()
        val visited=BooleanArray(width*height);val queue=IntArray(width*height)
        var read=0;var write=0;val start=startZ*width+startX;queue[write++]=start;visited[start]=true
        var reachedHome=false;var reachedNext=false;var maxDistance=0f
        while(read<write) {
            val node=queue[read++];val xi=node%width;val zi=node/width
            val x=minX+xi*step;val z=minZ+zi*step
            when(DreamExpansion.portalAt(id,x,z)) {
                home->reachedHome=true
                onward[worldIndex]->reachedNext=true
            }
            // A home==onward final map must still reach both distinct door regions.
            if(id==DreamExpansion.ROAD && abs(x)<2f && abs(z-endZ[worldIndex])<1f)reachedNext=true
            maxDistance=max(maxDistance,hypot(x-sx,z-sz))
            for((dx,dz) in arrayOf(1 to 0,-1 to 0,0 to 1,0 to -1)) {
                val nx=xi+dx;val nz=zi+dz
                if(nx !in 0 until width || nz !in 0 until height)continue
                val next=nz*width+nx
                if(visited[next])continue
                val xx=minX+nx*step;val zz=minZ+nz*step
                if(!DreamExpansion.canWalk(id,xx,zz) || !DreamExpansion.canWalk(id,(x+xx)*.5f,(z+zz)*.5f))continue
                visited[next]=true;queue[write++]=next
            }
        }
        verify(reachedHome,"$id return portal unreachable")
        verify(reachedNext,"$id onward portal unreachable")
        verify(maxDistance>30f,"$id navigation unexpectedly tiny")
        println("NAVIGATION $id spawn($sx,$sz) reachable=$write cells home=$reachedHome onward=$reachedNext")
        val scene=DreamExpansion.create(id)
        export(id,scene)
        val samples=buildList {
            add(sx to sz)
            add(0f to portalZ[worldIndex]);add(0f to endZ[worldIndex])
            for(k in 0 until minOf(write,128)) {
                val node=queue[(k.toLong()*write/128).toInt().coerceAtMost(write-1)]
                add((minX+(node%width)*step) to (minZ+(node/width)*step))
            }
        }
        Files.write(output.resolve("${id}14_walk_samples.csv"),
            samples.map{"${it.first},${it.second}"})
    }
    // Station gallery must be reachable from the original saved spawn.
    val visited=BooleanArray(width*height);val queue=IntArray(width*height);var read=0;var write=0
    val startX=((0f-minX)/step).roundToInt();val startZ=((6f-minZ)/step).roundToInt()
    queue[write++]=startZ*width+startX;visited[queue[0]]=true
    val found=mutableSetOf<String>()
    while(read<write) {
        val node=queue[read++];val xi=node%width;val zi=node/width
        val x=minX+xi*step;val z=minZ+zi*step
        DreamExpansion.stationPortalAt(x,z)?.let{found.add(it)}
        for((dx,dz) in arrayOf(1 to 0,-1 to 0,0 to 1,0 to -1)) {
            val nx=xi+dx;val nz=zi+dz;if(nx !in 0 until width || nz !in 0 until height)continue
            val next=nz*width+nx;if(visited[next])continue
            val xx=minX+nx*step;val zz=minZ+nz*step
            if(!DreamWorlds.canWalk(home,xx,zz) || !DreamWorlds.canWalk(home,(x+xx)*.5f,(z+zz)*.5f))continue
            visited[next]=true;queue[write++]=next
        }
    }
    verify(found.containsAll(DreamExpansion.ids),"Station has unreachable expansion gates: ${DreamExpansion.ids-found}")
    println("NAVIGATION station: $write cells, all four expansion portals reachable")
    export("station",DreamExpansion.stationScene())
    export("crater",DreamWorlds.createCrater())
    verify(normalFailures.isEmpty(),"Mesh normal failures: $normalFailures")
    println("PASS $checks expansion checks")
}
