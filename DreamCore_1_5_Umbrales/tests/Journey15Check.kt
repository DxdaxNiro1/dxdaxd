package com.dreamcore.game

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.*

fun main() {
    var checks=0
    fun verify(v:Boolean,msg:String){check(v){msg};checks++}
    val output=Paths.get("build/journey-validation");Files.createDirectories(output)
    fun scene(id:String)=when(id) {
        DreamWorlds.STATION->DreamJourney.createHub()
        DreamWorlds.POOLS->DreamWorlds.createPools()
        DreamWorlds.CRATER->DreamWorlds.createCrater()
        DreamWorlds.GARDEN->DreamWorlds.createGarden()
        DreamWorlds.MEADOW->DreamWorlds.createMeadow()
        DreamWorlds.AQUARIUM->DreamWorlds.createAquarium()
        DreamWorlds.HOUSE_MEADOW->DreamWorlds.createHouseMeadow()
        DreamWorlds.HOUSE_CRATER->DreamWorlds.createHouseCrater()
        DreamJourney.DARK->DreamDarkMap.create()
        else->DreamExpansion.create(id)
    }
    for(id in listOf(DreamWorlds.STATION)+DreamJourney.sequence+DreamJourney.DARK) {
        val s=DreamJourney.decorate(id,scene(id));val d=s.vertices
        verify(d.size%30==0,"$id incomplete triangles")
        verify(d.all{it.isFinite()},"$id invalid float")
        var bad=0
        for(i in d.indices step 10) {
            val n=sqrt(d[i+6]*d[i+6]+d[i+7]*d[i+7]+d[i+8]*d[i+8])
            if(abs(n-1f)>.01f)bad++
        }
        verify(bad==0,"$id degenerate normals $bad")
        val data=ByteBuffer.allocate(d.size*4).order(ByteOrder.LITTLE_ENDIAN);data.asFloatBuffer().put(d)
        Files.write(output.resolve("$id.f32"),data.array())
        println("MESH $id ${d.size/30} triangles")
    }
    verify(DreamJourney.sequence.distinct().size==11,"all original Dreamcore worlds once")
    for((i,id)in DreamJourney.sequence.withIndex()) {
        val prev=DreamJourney.previous(id);val next=DreamJourney.next(id)
        verify(prev==(if(i==0)DreamWorlds.STATION else DreamJourney.sequence[i-1]),"previous sequence")
        verify(next==(if(i==10)DreamWorlds.STATION else DreamJourney.sequence[i+1]),"next sequence")
        val a=DreamJourney.entry(id);val b=DreamJourney.exit(id)
        verify(DreamJourney.portalAt(id,a.x,a.z,DreamJourney.surface(id,a.x,a.z))==prev,"$id entry mapping")
        verify(DreamJourney.portalAt(id,b.x,b.z,DreamJourney.surface(id,b.x,b.z))==next,"$id exit mapping")
        for(from in listOf(prev,next)) {
            val s=DreamJourney.arrival(id,from)
            verify(DreamWorlds.canWalk(id,s.x,s.z),"$id arrival blocked from $from at $s")
            verify(DreamJourney.portalAt(id,s.x,s.z,DreamJourney.surface(id,s.x,s.z))==null,"$id spawn overlaps portal")
        }
        if(id==DreamWorlds.CRATER)continue
        val sx=if(id==DreamWorlds.MEADOW)210 else 65
        val z0=if(id==DreamWorlds.MEADOW)-235 else -205
        val z1=if(id==DreamWorlds.MEADOW)180 else 70
        val width=sx*2+1;val height=z1-z0+1
        val start=DreamJourney.arrival(id,prev)
        val startX=(start.x+sx).roundToInt();val startZ=(start.z-z0).roundToInt()
        val visited=BooleanArray(width*height);val queue=IntArray(width*height)
        var read=0;var write=0
        val index=startZ*width+startX;queue[write++]=index;visited[index]=true
        var reachedNext=false;var reachedPrev=false
        while(read<write) {
            val node=queue[read++];val xi=node%width;val zi=node/width
            val x=(xi-sx).toFloat();val z=(zi+z0).toFloat()
            val portal=DreamJourney.portalAt(id,x,z,DreamJourney.surface(id,x,z))
            if(portal==next)reachedNext=true
            if(portal==prev)reachedPrev=true
            for((dx,dz)in arrayOf(1 to 0,-1 to 0,0 to 1,0 to -1)) {
                val nx=xi+dx;val nz=zi+dz
                if(nx !in 0 until width||nz !in 0 until height)continue
                val k=nz*width+nx;if(visited[k])continue
                val xx=(nx-sx).toFloat();val zz=(nz+z0).toFloat()
                if(!DreamWorlds.canWalk(id,xx,zz)||!DreamWorlds.canWalk(id,(xx+x)/2,(zz+z)/2))continue
                visited[k]=true;queue[write++]=k
            }
        }
        verify(reachedNext&&reachedPrev,"$id route disconnected prev=$reachedPrev next=$reachedNext")
        println("ROUTE $id: both neighbors reachable ($write cells)")
    }
    verify(DreamJourney.portalAt(DreamWorlds.STATION,-8f,-16f,0f)==DreamWorlds.POOLS,"hub Dreamcore gate")
    verify(DreamJourney.portalAt(DreamWorlds.STATION,8f,-16f,0f)==DreamJourney.DARK,"hub Dark gate")
    val p=DreamCraterPhysics();val c=DreamWorlds.CRATER_CENTER_Z
    p.reset(0f,c+91.4f,0f)
    repeat(240){p.step(1f/60,0f,0f,false)}
    verify(p.grounded&&abs(p.feetY+7.5f)<.01f,"grass ledge supports fall: ${p.feetY}")
    repeat(600){p.step(1f/60,0f,-6.1f,false)}
    verify(p.feetY< -170f,"can leave ledge and reach bottom")
    val backwards=DreamJourney.arrival(DreamWorlds.CRATER,DreamExpansion.TUNNELS)
    verify(DreamJourney.surface(DreamWorlds.CRATER,backwards.x,backwards.z)==DreamWorlds.CRATER_ISLAND_Y,"backtracking spawns on island")
    verify(DreamJourney.portalAt(DreamWorlds.CRATER,0f,DreamWorlds.CRATER_HOUSE_FRONT_Z,0f)==null,"no crater portal while overhead")
    // Dark route: central nave is reachable and opens only the independent hub.
    val dark=DreamJourney.DARK
    for(z in -66..44)verify(DreamWorlds.canWalk(dark,0f,z.toFloat()),"dark central causeway blocked at $z")
    verify(DreamJourney.portalAt(dark,0f,-66f,0f)==DreamWorlds.STATION,"Dark finale returns to hub")
    verify(!DreamDarkMap.canWalk(22f,0f),"dark lake blocks unsupported walking")
    verify(DreamDarkMap.canWalk(22f,8f),"dark bridge crosses lake")
    println("PASS $checks journey / geometry / shelf / dark checks")
}
