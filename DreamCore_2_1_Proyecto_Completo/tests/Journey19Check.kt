package com.dreamcore.game

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.*

fun main() {
    var checks=0
    fun verify(v:Boolean,msg:String){check(v){msg};checks++}
    val output=Paths.get(System.getProperty("dreamcore.output","build/validation19"));Files.createDirectories(output)
    fun scene(id:String)=when {
        DreamHorizons.contains(id)->DreamHorizons.create(id)
        id==DreamWaterpark.ID->DreamWaterpark.create()
        DreamPocket.contains(id)->DreamPocket.create(id)
        DreamLands.contains(id)->DreamLands.create(id)
        id==DreamWorlds.STATION->DreamJourney.createHub()
        id==DreamWorlds.POOLS->DreamWorlds.createPools()
        id==DreamWorlds.CRATER->DreamWorlds.createCrater()
        id==DreamJourney.DARK->DreamDarkMap.create()
        else->error("unknown id")
    }

    for(id in listOf(DreamWorlds.STATION)+DreamJourney.sequence+DreamJourney.DARK) {
        val s=DreamJourney.decorate(id,scene(id));val d=s.vertices+DreamObjects.geometry(id,DreamChronicle()).vertices
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
    verify(DreamJourney.sequence.distinct().size==13,"all original Dreamcore worlds once")
    for((i,id)in DreamJourney.sequence.withIndex()) {
        val prev=DreamJourney.previous(id);val next=DreamJourney.next(id)
        verify(prev==(if(i==0)DreamWorlds.STATION else DreamJourney.sequence[i-1]),"previous sequence")
        verify(next==(if(i==DreamJourney.sequence.lastIndex)DreamWorlds.STATION else DreamJourney.sequence[i+1]),"next sequence")
        val a=DreamJourney.entry(id);val b=DreamJourney.exit(id)
        verify(DreamJourney.portalAt(id,a.x,a.z,DreamJourney.surface(id,a.x,a.z))==prev,"$id entry mapping")
        verify(DreamJourney.portalAt(id,b.x,b.z,DreamJourney.surface(id,b.x,b.z))==next,"$id exit mapping")
        for(from in listOf(prev,next)) {
            val s=DreamJourney.arrival(id,from)
            verify(DreamWorlds.canWalk(id,s.x,s.z),"$id arrival blocked from $from at $s")
            verify(DreamJourney.portalAt(id,s.x,s.z,DreamJourney.surface(id,s.x,s.z))==null,"$id spawn overlaps portal")
        }
        if(id==DreamWorlds.CRATER)continue
        val sx=if(DreamLands.contains(id))380 else if(id==DreamHorizons.CITY)235 else if(id==DreamWaterpark.ID)180 else if(id==DreamHorizons.CELESTIAL)120 else 65
        val z0=if(DreamLands.contains(id))-620 else if(id==DreamHorizons.CITY)-510 else if(id==DreamWaterpark.ID)-514 else if(id==DreamHorizons.CELESTIAL)-495 else -205
        val z1=if(DreamLands.contains(id))155 else if(id==DreamHorizons.CITY)110 else if(id==DreamWaterpark.ID || id==DreamHorizons.CELESTIAL)105 else 70
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
        if(DreamLands.contains(id))for(secret in DreamLands.secrets(id)) {
            val ix=(secret.x+sx).roundToInt();val iz=(secret.z-z0).roundToInt()
            verify(visited[iz*width+ix],"$id secret not reachable: ${secret.key}")
        }
        for(item in DreamObjects.items(id)) {
            val ix=(item.x+sx).roundToInt();val iz=(item.z-z0).roundToInt()
            verify(visited[iz*width+ix],"$id object not reachable: ${item.key} at ${item.x},${item.z}")
        }
        println("ROUTE $id: both neighbors and secrets reachable ($write cells)")
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
    verify(DreamJourney.portalAt(dark,0f,-66f,0f)==DreamHorizons.DARK,"Dark finale returns to hub")
    verify(!DreamDarkMap.canWalk(22f,0f),"dark lake blocks unsupported walking")
    verify(DreamDarkMap.canWalk(22f,8f),"dark bridge crosses lake")
    val figure=DreamExploration.figure().vertices
    val figureBytes=ByteBuffer.allocate(figure.size*4).order(ByteOrder.LITTLE_ENDIAN)
    figureBytes.asFloatBuffer().put(figure);Files.write(output.resolve("figure.f32"),figureBytes.array())
    verify(DreamLands.indoor(DreamWorlds.MEADOW,70f,-70f),"village houses have interior ambience")
    verify(DreamLands.material(DreamWorlds.MEADOW,70f,-70f)=="wood","wooden steps inside village houses")
    verify(DreamLands.zone(DreamWorlds.MEADOW,1600f,3f)=="PISCINA_INTERIOR","pool has its own sound layer")
    verify(DreamLands.material(DreamWorlds.MEADOW,1600f,3f)=="tile","pool rim has tile steps")
    for(x in -350..350 step 35)verify(abs(DreamJourney.surface(DreamWorlds.MEADOW,x.toFloat(),-250f)-DreamLands.height(DreamWorlds.MEADOW,x.toFloat(),-250f))<.001f,"meadow player follows mesh height")
    val experience=DreamExploration()
    val front=experience.door(DreamWorlds.MEADOW,-40f,-46.5f)!!
    verify(front.toX>1000f,"house opens impossible room")
    verify(DreamWorlds.canWalk(DreamWorlds.MEADOW,front.toX,front.toZ),"pool arrival is walkable")
    val reflection=experience.door(DreamWorlds.HOUSE_MEADOW,0f,-247f)!!
    verify(DreamWorlds.canWalk(DreamWorlds.HOUSE_MEADOW,reflection.toX,reflection.toZ),"hidden mirror room is accessible")
    val reflectionReturn=experience.door(DreamWorlds.HOUSE_MEADOW,-24f,-197f)!!
    verify(DreamWorlds.canWalk(DreamWorlds.HOUSE_MEADOW,reflectionReturn.toX,reflectionReturn.toZ),"hidden mirror room has a safe return")
    val back=experience.door(DreamWorlds.MEADOW,1600f,7f)!!
    verify(DreamWorlds.canWalk(DreamWorlds.MEADOW,back.toX,back.toZ),"house return is walkable")
    verify(!DreamWorlds.canWalk(DreamWorlds.MEADOW,1600f,-50f),"pool blocks walking on deep water")
    for(z in -110..6)verify(DreamWorlds.canWalk(DreamWorlds.MEADOW,1650f,z.toFloat()),"pool rim escape path")
    val gaze=DreamExploration()
    val yaw=atan2(22f,80f)
    var messages=0
    repeat(600){messages+=gaze.step(DreamWorlds.MEADOW,0f,0f,yaw,0f,1f/60f).size}
    verify(messages==1,"gaze reacts once, no repeated trigger: $messages")
    verify(gaze.presenceAlpha<.01f,"presence fades away after looking")
    val occluded=DreamExploration()
    var blockedNotices=0
    repeat(600){blockedNotices+=occluded.step(DreamWorlds.MEADOW,70f,0f,atan2(-48f,80f),0f,1f/60f).size}
    verify(blockedNotices==0 && occluded.seenSeconds==0f,"presence does not react through a house wall")
    val e=DreamExploration()
    e.step(DreamWorlds.HOUSE_MEADOW,0f,-100f,0f,0f,.016f)
    e.step(DreamWorlds.HOUSE_MEADOW,0f,-170f,0f,0f,.016f)
    val change=e.step(DreamWorlds.HOUSE_MEADOW,0f,-110f,0f,0f,.016f)
    verify(change.size==1 && e.corridorRevision==1,"corridor changes on return")
    for(z in -200..0)verify(DreamWorlds.canWalk(DreamWorlds.HOUSE_MEADOW,0f,z.toFloat()),"corridor revision leaves exit free")
    val secrets=DreamExploration()
    val one=secrets.step(DreamWorlds.MEADOW,-90f,-175f,0f,0f,.016f)
    val two=secrets.step(DreamWorlds.MEADOW,-90f,-175f,0f,0f,.016f)
    verify(one.size==1 && two.isEmpty(),"discovery persists within session")
    val meadow=DreamJourney.decorate(DreamWorlds.MEADOW,DreamLands.create(DreamWorlds.MEADOW))
    val chunks=DreamChunks.split(meadow)
    verify(chunks.values.sumOf{it.size}==meadow.vertices.size,"chunk split loses no triangles")
    val villageChunks=chunks.keys.filter{DreamChunks.nearby(it,0f,0f)}
    val poolChunks=chunks.keys.filter{DreamChunks.nearby(it,1600f,0f)}
    verify(villageChunks.none{it in poolChunks},"village and impossible room are separately resident")
    verify(villageChunks.size<chunks.size,"distant world is not fully drawn")
    for(id in DreamLands.ids) {
        val mirror=DreamLands.mirror(id)?:continue
        verify(!DreamLands.canWalk(id,mirror.x,mirror.z),"mirror has collision")
        verify(DreamLands.canWalk(id,mirror.x,mirror.z+3f),"mirror viewing position accessible")
    }
    println("PASS $checks journey / geometry / exploration / chunks / dark checks")
}
