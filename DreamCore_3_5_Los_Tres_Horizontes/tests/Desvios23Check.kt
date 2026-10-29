package com.dreamcore.game

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

fun main(){
    var checks=0;fun verify(v:Boolean,name:String){check(v){name};checks++}
    val out=File(System.getProperty("dreamcore.output","/tmp"));out.mkdirs()
    fun dump(name:String,s:DreamWorlds.Scene){val data=s.vertices;verify(data.size%30==0&&data.all{it.isFinite()},"finite triangle layout $name");verify(data.size/30<180000,"bounded geometry $name ${data.size/30}")
        var bad=0;for(i in data.indices step 10){val len=sqrt(data[i+6].pow(2)+data[i+7].pow(2)+data[i+8].pow(2));if(abs(len-1f)>.03f)bad++};verify(bad==0,"normalized mesh $name $bad")
        val bytes=ByteBuffer.allocate(data.size*4).order(ByteOrder.LITTLE_ENDIAN);bytes.asFloatBuffer().put(data);File(out,"$name.f32").writeBytes(bytes.array());println("$name ${data.size/30} triangles ${DreamChunks.split(s).size} chunks")}
    for(seed in listOf(1000,1001,1002)){
        for(path in listOf("power","hush","photo","tune")){
            val s=DreamChronicle();s.universeSeed=seed;DreamMystery.configure(seed);DreamMystery.sync(s)
            verify(!DreamMystery.unlocked(s),"locked initially")
            when(path){"photo"->DreamMystery.record(s,"photo");"tune"->{DreamMystery.use("mystery:listen",s);DreamMystery.use("mystery:tune",s)};else->DreamMystery.use("mystery:$path",s)}
            DreamMystery.sync(s);verify(DreamMystery.unlocked(s),"independent $path solution without phone gate")
            verify(DreamMystery.use("mystery:visit",s).pocket==DreamMystery.HOUSE,"all solutions enter same house")
            DreamMystery.use("mystery:friend",s);DreamMystery.use("mystery:answer",s);DreamMystery.use("mystery:stay",s)
            verify(DreamMystery.has(s,"stay")&&!DreamMystery.has(s,"leave"),"choice exclusive")
            val loaded=DreamChronicle.decode(DreamChronicle.encode(s))!!
            verify(DreamMystery.unlocked(loaded)&&DreamMystery.has(loaded,"answer"),"solution survives restart")
            verify(DreamMystery.use("mystery:friend",loaded).text.contains("reconoce"),"character remembers choice after reload")
            DreamMystery.use("mystery:leave",loaded);verify(DreamMystery.has(loaded,"leave")&&!DreamMystery.has(loaded,"stay"),"can change own decision")
            verify(DreamMystery.diary(loaded).contains("descolgado")&&DreamMystery.diary(loaded).contains("tranquila"),"journal keeps historical discoveries after changing decision")
        }
        val state=DreamChronicle();state.universeSeed=seed;DreamMystery.configure(seed);val c=DreamMystery.circuit()
        verify(DreamObjects.items(DreamWorlds.MEADOW).first{it.key=="mystery:power"}.x==c.x,"new seed invalidates object cache")
        DreamMystery.sync(state);verify(DreamMystery.blocked(70f,-61.5f),"closed facade blocks physical entry")
        for(i in DreamMystery.items(DreamWorlds.MEADOW))verify(DreamWorlds.canWalk(DreamWorlds.MEADOW,i.x,i.z),"quest interaction has accessible floor ${i.key} $seed")
        DreamMystery.use("mystery:power",state);verify(DreamMystery.observer(state),"light attracts attention")
        DreamMystery.use("mystery:hush",state);verify(!DreamMystery.observer(state),"silencing withdraws attention")
        verify(DreamMystery.diary(state).contains("iluminó"),"old power discovery remains in diary")
        val repaired=DreamRestore.repair(DreamChronicle.Checkpoint(DreamWorlds.MEADOW,70f,1.65f,-61.5f,0f,0f));verify(DreamWorlds.canWalk(repaired.world,repaired.x,repaired.z),"old checkpoint repaired around locked door")
    }
    val locations=(1000..1002).map{DreamMystery.configure(it);DreamMystery.circuit()};verify(locations.distinct().size==3,"actual clue location varies between seed variants")
    DreamMystery.sync(DreamChronicle().apply{DreamMystery.record(this,"photo")})
    val targetY=DreamJourney.surface(DreamWorlds.MEADOW,70f,-60f)+3f;val eyeY=1.65f;val targetPitch=atan2(targetY-eyeY,25f)
    verify(DreamMystery.photoCandidate(DreamWorlds.MEADOW,70f,eyeY,-35f,0f,targetPitch){_,_,_,_->true},"photo requires correct view")
    verify(!DreamMystery.photoCandidate(DreamWorlds.MEADOW,70f,eyeY,-35f,PI.toFloat(),targetPitch){_,_,_,_->true},"looking away does not solve")
    verify(!DreamMystery.photoCandidate(DreamWorlds.MEADOW,70f,eyeY,-35f,0f,targetPitch){_,_,_,_->false},"occluded camera cannot solve")
    val photos=DreamChronicle();photos.flags.addAll(listOf("photo:casa","photo:mar","photo:ventana","photo:not_a_clue"));verify(!DreamPhotography.unlocked(photos),"unrelated photos cannot unlock old tower")
    val signatures=HashSet<String>()
    for(seed in 1000..1020){DreamBackrooms.configure(seed);signatures.add((0..48).joinToString{DreamBackrooms.neighbors(it).sorted().joinToString("-")})
        for(a in 0..48){verify(DreamBackrooms.remaining(a)<1000,"every room reaches exit $seed $a");val p=DreamBackrooms.centre(a);verify(DreamBackrooms.canWalk(p.x,p.z),"room centre free")
            for(b in DreamBackrooms.neighbors(a)){verify(a in DreamBackrooms.neighbors(b),"two-way graph link");val q=DreamBackrooms.centre(b);for(j in 0..24){val t=j/24f;verify(DreamBackrooms.canWalk(p.x+(q.x-p.x)*t,p.z+(q.z-p.z)*t),"door passage graph matches collision $seed $a $b $j")}}
            if(a!=48)verify(DreamBackrooms.neighbors(a).any{DreamBackrooms.remaining(it)<DreamBackrooms.remaining(a)},"steady-lamp path always advances")
        }
    };verify(signatures.size>15,"different coherent layouts across seeds")
    DreamBackrooms.configure(3271);DreamMystery.configure(3271)
    for(id in DreamRoutes23.ids){
        val p=DreamRoutes23.spawn(id);val g=DreamRoutes23.gate(id);verify(DreamChronicle.validWorld(id),"new world valid save")
        verify(DreamWorlds.canWalk(id,p.x,p.z),"safe new spawn $id");verify(DreamWorlds.canWalk(id,g.x,g.z),"safe return portal $id")
        verify(DreamJourney.portalAt(id,g.x,g.z,DreamJourney.surface(id,g.x,g.z))==DreamPocket.RETURN,"return gate works $id")
        for(i in DreamRules23.items(id)+DreamMystery.items(id))verify(DreamWorlds.canWalk(id,i.x,i.z),"new interaction reachable floor $id ${i.key}")
        val targets=(DreamRules23.items(id)+DreamMystery.items(id)).map{DreamJourney.Point(it.x,it.z)}+g
        val reached=BooleanArray(targets.size);val seen=HashSet<Pair<Int,Int>>();val queue=java.util.ArrayDeque<Pair<Int,Int>>();queue.add(p.x.roundToInt() to p.z.roundToInt())
        while(queue.isNotEmpty()&&seen.size<250000){val q=queue.removeFirst();if(!seen.add(q)||!DreamWorlds.canWalk(id,q.first.toFloat(),q.second.toFloat()))continue
            for(i in targets.indices)if(hypot(q.first-targets[i].x,q.second-targets[i].z)<1.5f)reached[i]=true
            if(reached.all{it})break
            for((dx,dz)in listOf(1 to 0,-1 to 0,0 to 1,0 to -1)){val n=q.first+dx to q.second+dz;if(n !in seen)queue.add(n)}
        }
        verify(reached.all{it},"all new interactions and return door connect to spawn $id ${reached.toList()}")
        val s=DreamChronicle();s.checkpoint=DreamChronicle.Checkpoint(id,p.x,DreamJourney.surface(id,p.x,p.z)+1.65f,p.z,0f,0f);s.returnPoint=DreamChronicle.Checkpoint(DreamWorlds.MEADOW,0f,1.65f,30f,0f,0f)
        val loaded=DreamChronicle.decode(DreamChronicle.encode(s))!!;verify(loaded.checkpoint==s.checkpoint&&loaded.returnPoint==s.returnPoint,"new world and origin survive restart")
        val mesh=if(id==DreamMystery.HOUSE)DreamMystery.geometry(id,s)else DreamRoutes23.create(id);dump(id,DreamWorlds.Scene(mesh.vertices+DreamObjects.geometry(id,s).vertices+DreamRules23.geometry(id).vertices))
    }
    val ringing=DreamRules23();val unanswered=DreamChronicle();var rings=0
    repeat(400){if(ringing.step(DreamWorlds.MEADOW,8f,65f,0f,false,.05f,unanswered)?.startsWith("@ring:")==true)rings++}
    verify(rings in 1..2,"nearby unanswered phone gives sparse audio cues")
    DreamMystery.record(unanswered,"call");rings=0
    repeat(400){if(ringing.step(DreamWorlds.MEADOW,8f,65f,0f,false,.05f,unanswered)?.startsWith("@ring:")==true)rings++}
    verify(rings==0,"answering stops repeated ring cues")
    val rules=DreamRules23();val state=DreamChronicle()
    repeat(200){rules.step(DreamRoutes23.SKY,50f,-145f,.5f,true,.05f,state)};verify(!DreamMystery.has(state,"sky"),"moving never completes stillness rule")
    repeat(121){rules.step(DreamRoutes23.SKY,50f,-145f,.5f,false,.05f,state)};verify(DreamMystery.has(state,"sky"),"observing in light completes rule")
    val city=DreamRules23();repeat(400){city.step(DreamRoutes23.CITY,24f,-230f,0f,false,.05f,state)};verify(DreamMystery.has(state,"city"),"waiting for actual train stop discovers passage")
    verify(DreamRules23.use("rule:board",state).pocket==DreamRoutes23.EDGE,"train links to fissures")
    verify(DreamRules23.use("rule:window",state).warp==DreamJourney.Point(0f,-190f),"window changes space")
    for(status in 0..6){val cap=DreamPower.cap(status);verify(cap in 24..60,"thermal cap valid");if(status>=3)verify(cap==24,"severe caps scheduling")}
    verify(DreamPower.scale(0,60)==1f,"automatic keeps full resolution when cool")
    verify(DreamPower.scale(0,30)<1f&&DreamPower.scale(0,24)<DreamPower.scale(0,30),"progressive heat response")
    verify(DreamPower.scale(1,24)==1f,"explicit native resolution respected")
    verify(DreamPower.fps(60,60,2)==24&&DreamPower.fps(60,24,1)==24,"fresh and thermal frame targets")
    verify(!DreamPower.shadowNeeded(DreamBackrooms.ID)&&!DreamPower.shadowNeeded(DreamVoyage.REFUGE)&&DreamPower.shadowNeeded(DreamWorlds.MEADOW),"only irrelevant indoor shadow passes omitted")
    verify(DreamPower.mirrorInterval(true,30,24)>DreamPower.mirrorInterval(true,30,60),"hot mirrors refreshed less often")
    val rng=java.util.Random(23)
    for(id in listOf(DreamWorlds.MEADOW,DreamMystery.HOUSE,DreamBackrooms.ID,DreamRoutes23.CITY))repeat(300){
        val x=rng.nextFloat()*180f-90f;val z=rng.nextFloat()*330f-300f;val feet=DreamJourney.surface(id,x,z)
        val oracle=DreamObjects.items(id).filter{hypot(it.x-x,it.z-z)<2.5f&&abs(feet-DreamJourney.surface(id,it.x,it.z))<2.4f}.minByOrNull{hypot(it.x-x,it.z-z)}
        verify(DreamObjects.nearest(id,x,z,feet)==oracle,"allocation-free nearest matches distance oracle")
    }
    val st=DreamChronicle();val village=DreamWorlds.Scene(DreamLands.create(DreamWorlds.MEADOW).vertices+DreamObjects.geometry(DreamWorlds.MEADOW,st).vertices+DreamMystery.geometry(DreamWorlds.MEADOW,st).vertices);dump("village_investigation",village)
    println("PASS $checks checks")
}
