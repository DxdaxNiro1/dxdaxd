package com.dreamcore.game

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

fun main(){
    var checks=0;fun verify(value:Boolean,label:String){check(value){label};checks++}
    val out=File(System.getProperty("dreamcore.output","/tmp"));out.mkdirs()
    fun mesh(name:String,scene:DreamWorlds.Scene){
        val v=scene.vertices;verify(v.size%30==0&&v.all{it.isFinite()},"finite layout $name")
        verify(v.size/30<180000,"bounded geometry $name")
        for(i in v.indices step 10){val n=sqrt(v[i+6].pow(2)+v[i+7].pow(2)+v[i+8].pow(2));check(abs(n-1f)<.03f){"normal $name $i $n"}}
        val b=ByteBuffer.allocate(v.size*4).order(ByteOrder.LITTLE_ENDIAN);b.asFloatBuffer().put(v);File(out,"$name.f32").writeBytes(b.array())
        println("$name: ${v.size/30} triangles; ${DreamChunks.split(scene).size} chunks")
    }
    val session=DreamSession(false)
    verify(session.atTitle&&!session.maySave&&!session.canContinue,"first launch cannot save or continue imaginary progress")
    verify(!session.continueGame()&&session.atTitle,"disabled continue does not enter")
    session.newGame();verify(session.started&&!session.atTitle&&session.maySave,"new journey begins")
    session.home();verify(session.atTitle&&session.canContinue,"return to title can resume current unsaved session")
    verify(session.continueGame()&&!session.atTitle,"resume live session")
    val existing=DreamSession(true);verify(existing.atTitle&&!existing.maySave&&existing.canContinue,"existing save untouched on title")
    verify(existing.continueGame()&&existing.maySave,"continue permits subsequent save")
    for(id in DreamNewDreams.ids){
        val p=DreamNewDreams.spawn(id);verify(DreamWorlds.canWalk(id,p.x,p.z),"safe arrival $id")
        val y=DreamJourney.surface(id,p.x,p.z);verify(y.isFinite(),"finite floor $id")
        val story=DreamChronicle();story.checkpoint=DreamChronicle.Checkpoint(id,p.x,y+1.65f,p.z,.2f,0f)
        story.returnPoint=DreamChronicle.Checkpoint(DreamWorlds.MEADOW,0f,1.65f,0f,0f,0f)
        val decoded=DreamChronicle.decode(DreamChronicle.encode(story))!!
        verify(decoded.checkpoint==story.checkpoint&&decoded.returnPoint==story.returnPoint,"resume + safe return $id")
        for(g in DreamNewDreams.gates(id)){verify(DreamWorlds.canWalk(id,g.x,g.z),"reachable gate $id $g");verify(DreamJourney.portalAt(id,g.x,g.z,DreamJourney.surface(id,g.x,g.z))==DreamPocket.RETURN,"return portal $id")}
        verify(!DreamWorlds.canWalk(id,1000f,1000f),"outside bounded $id")
        mesh(id,DreamNewDreams.create(id))
    }
    // The slide must have continuous support throughout its full descent, including deck junctions.
    var previous=72f
    for(i in 0..1000){val z=-20f+i*.2f;val x=DreamNewDreams.slideX(z);val y=DreamJourney.surface(DreamNewDreams.ISLAND,x,z)
        verify(DreamWorlds.canWalk(DreamNewDreams.ISLAND,x,z),"slide traversable $z")
        verify(y<=previous+.001f&&previous-y<.12f,"smooth monotonic slide $z $y $previous");previous=y
        for(side in floatArrayOf(-1f,1f)){verify(DreamWorlds.canWalk(DreamNewDreams.ISLAND,x+side*1.85f,z),"safe slide edge");verify(!DreamWorlds.canWalk(DreamNewDreams.ISLAND,x+side*3f,z),"slide sides protected")}
    }
    verify(abs(previous-1f)<.001f,"slide reaches deck")
    verify(!DreamWorlds.canWalk(DreamNewDreams.ISLAND,0f,-56f),"house wall solid")
    for(x in -160..160 step 20)for(z in -180..40 step 20)if(DreamWorlds.canWalk(DreamNewDreams.EYES,x.toFloat(),z.toFloat()))verify(abs(DreamNewDreams.hill(x.toFloat(),z.toFloat()))<4f,"meadow hills bounded")
    for(role in listOf("curious","mourner","guide","postman"))mesh("entity_$role",DreamShapes.presence(role))
    mesh("entity_friendly",DreamPresences.friendly())
    val face=DreamMesh();DreamPresences.face(face,0f,0f,0f,1f,1f,0,97f)
    val fv=face.finish().vertices
    verify((fv.indices step 30).any{i->abs(fv[i])<.0001f && abs(fv[i+1])<.0001f && abs((fv[i+10]-fv[i])*(fv[i+21]-fv[i+1])-(fv[i+11]-fv[i+1])*(fv[i+20]-fv[i]))>.001f},"eye centre has a nondegenerate triangle; no pupil hole")
    val state=DreamChronicle();val actors=DreamActors()
    repeat(6000){actors.step(DreamNewDreams.EYES,0f,-45f,0f,.02f,state)}
    verify(actors.actors.size==1&&actors.actors.single().role=="friendly","only one friendly entity in preview")
    val friendly=actors.actors.single();verify(hypot(friendly.x,friendly.z+45f)>=8.98f,"friendly keeps personal distance")
    repeat(100){actors.step(DreamWorlds.MEADOW,0f,100f,0f,.02f,state)}
    verify(actors.actors.size==2&&actors.actors.count{it.role!="postman"}==1,"one observer and existing story postman")
    verify(actors.actors.all{hypot(it.x,it.z-100f)>100f},"no presence immediately beside meadow spawn")
    for(id in listOf(DreamWorlds.GARDEN,DreamExpansion.CHESS,DreamExpansion.TUNNELS,DreamExpansion.ROAD)){mesh(id,DreamLands.create(id));val a=DreamJourney.arrival(id,DreamJourney.previous(id));verify(DreamWorlds.canWalk(id,a.x,a.z),"remodelled arrival $id")}
    val obstructed=DreamChronicle.Checkpoint(DreamWorlds.GARDEN,98f,1.65f,-240f,.2f,.1f)
    verify(!DreamWorlds.canWalk(obstructed.world,obstructed.x,obstructed.z),"new pillar occupies formerly empty saved position")
    val repaired=DreamRestore.repair(obstructed)
    verify(DreamWorlds.canWalk(repaired.world,repaired.x,repaired.z)&&hypot(repaired.x-obstructed.x,repaired.z-obstructed.z)<2f,"old save repaired nearby")
    verify(repaired.yaw==obstructed.yaw&&repaired.pitch==obstructed.pitch,"repair preserves look direction")
    val safe=DreamChronicle.Checkpoint(DreamWorlds.GARDEN,0f,1.65f,30f,0f,0f)
    verify(DreamRestore.repair(safe)==safe,"unobstructed save remains exact")
    val hub=DreamPortalNames.gates(DreamWorlds.STATION);verify(hub.size==10&&hub.map{it.name}.distinct().size==10,"hub named routes distinct")
    for(id in DreamJourney.sequence+DreamReferences.ids+DreamNewDreams.ids){val labels=DreamPortalNames.gates(id);verify(labels.isNotEmpty()&&labels.all{it.name.isNotBlank()},"all route portals named $id")}
    // A real perspective matrix pointed down -Z, projected labels stay within HUD safe area.
    val f=1f/tan(66f*PI.toFloat()/360f);val matrix=floatArrayOf(f/1.78f,0f,0f,0f,0f,f,0f,0f,0f,0f,-1.0001f,-1f,0f,0f,-.2f,0f)
    val visible=DreamPortalNames.project(DreamWorlds.STATION,floatArrayOf(0f,0f,0f),matrix)
    verify(visible.isNotEmpty()&&visible.size<=3&&visible.all{it.x in 0f..1f&&it.y in 0f..1f},"projection and bounded labels")
    verify(DreamPortalNames.project(DreamWorlds.STATION,floatArrayOf(0f,0f,0f),matrix){_,_,_,_->false}.isEmpty(),"labels hidden behind walls")
    verify(DreamPortalNames.project(DreamWorlds.STATION,floatArrayOf(0f,0f,0f),FloatArray(16)).isEmpty(),"labels behind camera rejected")
    println("PASS $checks checks")
}
