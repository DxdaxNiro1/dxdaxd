package com.dreamcore.game

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

fun main(){
    var checks=0
    fun verify(value:Boolean,message:String){check(value){message};checks++}
    fun near(a:Float,b:Float)=abs(a-b)<.002f
    val story=DreamChronicle();val root=DreamChronicle.Checkpoint(DreamWorlds.AQUARIUM,8f,1.65f,-160f,0f,0f)
    val home=DreamChronicle.Checkpoint(DreamVoyage.REFUGE,0f,1.65f,2f,0f,0f)
    val inside=DreamChronicle.Checkpoint(DreamVoyage.HOUSE,24f,1.65f,-120f,0f,0f)
    verify(story.pushReturn(root),"first return");verify(story.pushReturn(home),"nested return")
    story.checkpoint=inside;val loaded=DreamChronicle.decode(DreamChronicle.encode(story))!!
    verify(loaded.popReturn()==home,"restore inner return after restart");verify(loaded.popReturn()==root,"restore outer return");verify(loaded.popReturn()==null,"return stack exhausted")
    while(story.popReturn()!=null){}
    for(i in 0..7)verify(story.pushReturn(root),"bounded stack accepts level $i")
    verify(!story.pushReturn(home),"overfull stack rejects without losing return")
    verify(DreamChronicle.decode(DreamChronicle.encode(story))!!.returnStack.size==7,"bounded stack persisted")
    verify(!DreamTools.equip(story,"lantern"),"cannot equip missing item")
    for(tool in DreamTools.names.drop(1)){story.flags.add("tool:$tool");verify(DreamTools.equip(story,tool),"equip $tool");verify(DreamTools.equipped(story)==tool,"exclusive tool $tool")}
    verify(!DreamTools.equip(story,"invalid"),"invalid item")
    for(i in 0..2){DreamMail.accept(story);verify(DreamMail.active(story)==i,"letter order");verify(!DreamMail.deliver((i+1)%3,story),"wrong recipient");verify(DreamMail.active(story)==i,"wrong recipient retains letter");verify(DreamMail.deliver(i,story),"recipient accepts");verify(DreamChronicle.decode(DreamChronicle.encode(story))!!.flags.contains("delivered:$i"),"delivery persisted")}
    DreamMail.accept(story);verify(DreamMail.active(story)==null,"finite three letters")
    for(schema in listOf("18","19","20")){
        val old=DreamChronicle();old.checkpoint=DreamChronicle.Checkpoint(DreamWorlds.MEADOW,12f,1.65f,-15f,.5f,.2f);old.found.add("dream_05:memory");old.flags.add("lamp:meadow");old.universeSeed=3271
        val body=DreamChronicle.encode(old).substringBefore("\ncrc=").lines().filter{!it.startsWith("stack=")}.joinToString("\n").replace("schema=21","schema=$schema")
        val crc=java.util.zip.CRC32();crc.update(body.toByteArray(Charsets.UTF_8));val restored=DreamChronicle.decode(body+"\ncrc="+java.lang.Long.toHexString(crc.value))!!
        verify(restored.checkpoint==old.checkpoint && restored.flags==old.flags && restored.found==old.found,"real schema $schema row migration")
        verify(restored.returnStack.isEmpty() && restored.universeSeed==3271,"old schema no invented nesting/seed")
    }
    for(layout in 0..1){DreamLands.configure(layout)
        for(id in listOf(DreamWorlds.MEADOW,DreamWorlds.AQUARIUM,DreamExpansion.CLOUD_HOTEL,DreamHorizons.CELESTIAL))for(item in DreamObjects.items(id).filter{it.kind in listOf("postman","hidden","mailbox")}){
            verify((-4..4).any{dx->(-4..4).any{dz->hypot(dx*.5f,dz*.5f)<2.4f && DreamWorlds.canWalk(id,item.x+dx*.5f,item.z+dz*.5f)}},"new world object reachable in layout $layout ${item.key}")
        }
    }
    DreamLands.configure(0)
    val seed=DreamCraterLayout.seed
    val first=(0..23).map{DreamCraterLayout.side(it)}
    DreamCraterLayout.seed=seed+1;val second=(0..23).map{DreamCraterLayout.side(it)}
    verify(first!=second,"different saved universe changes abyss")
    DreamCraterLayout.seed=seed;verify(first==(0..23).map{DreamCraterLayout.side(it)},"seed determinism")
    for(h in first){verify(h.radius in 84f..89.5f && h.base in -161f..-14f && h.scale in 1.1f..1.75f,"house safe bounds")}
    for(z in -157..10){val depth=z.toFloat();val a=DreamGravity.point(0f,depth);val b=DreamGravity.point(0f,depth+.001f);verify(sqrt((a[1]-b[1]).pow(2)+(a[2]-b[2]).pow(2))<.002f,"continuous gravity path")
        val axes=listOf(DreamGravity.vector(depth,1f,0f,0f),DreamGravity.vector(depth,0f,1f,0f),DreamGravity.vector(depth,0f,0f,1f))
        for(v in axes)verify(near(sqrt(v.sumOf{(it*it).toDouble()}.toFloat()),1f),"unit gravity camera axis")
        verify(near(axes[1].zip(axes[2]).sumOf{(it.first*it.second).toDouble()}.toFloat(),0f),"orthogonal gravity camera")
        verify(DreamWorlds.canWalk(DreamGravity.ID,0f,depth),"gravity whole path walkable")
    }
    val end=DreamGravity.vector(-150f,0f,1f,0f);verify(end[2]>.999f && abs(end[1])<.001f,"wall normal actually turned 90 degrees")
    for(id in DreamVoyage.ids){
        verify(DreamChronicle.validWorld(id),"save accepts $id")
        val spawn=DreamVoyage.spawn(id);val gate=DreamVoyage.gate(id)
        verify(DreamWorlds.canWalk(id,spawn.x,spawn.z),"spawn traversable $id at $spawn")
        verify(DreamJourney.portalAt(id,gate.x,gate.z,DreamJourney.surface(id,gate.x,gate.z))==DreamPocket.RETURN,"return gateway $id")
        val state=DreamChronicle();state.checkpoint=DreamChronicle.Checkpoint(id,spawn.x,DreamJourney.surface(id,spawn.x,spawn.z)+1.65f,spawn.z,0f,0f)
        verify(DreamChronicle.decode(DreamChronicle.encode(state))!!.checkpoint.world==id,"new checkpoint roundtrip $id")
    }
    // Reach the tools and wall door without crossing the refuge table/bed.
    fun reachable(id:String,tx:Float,tz:Float):Boolean {
        val step=.5f;val start=DreamVoyage.spawn(id);val seen=HashSet<Pair<Int,Int>>();val queue=java.util.ArrayDeque<Pair<Int,Int>>();queue.add((start.x/step).roundToInt() to (start.z/step).roundToInt())
        while(queue.isNotEmpty()&&seen.size<100000){val q=queue.removeFirst();if(!seen.add(q))continue;val x=q.first*step;val z=q.second*step;if(!DreamWorlds.canWalk(id,x,z))continue
            if(hypot(x-tx,z-tz)<1.2f)return true
            for((dx,dz)in listOf(1 to 0,-1 to 0,0 to 1,0 to -1)){val n=q.first+dx to q.second+dz;if(n !in seen)queue.add(n)}
        };return false
    }
    for(tool in DreamObjects.items(DreamVoyage.REFUGE))verify(reachable(DreamVoyage.REFUGE,tool.x,tool.z),"refuge item reachable ${tool.key}")
    verify(reachable(DreamVoyage.LETTER,0f,-22f),"letter reply reachable")
    for(fps in listOf(24,30,60)){val pacer=DreamPerformance.Pacer();var frames=0;for(i in 0 until 1200)if(pacer.due(i*1_000_000_000L/120,fps))frames++
        verify(frames in fps*10-1..fps*10+1,"$fps FPS pacing, got $frames")
        verify(pacer.due(100_000_000_000,fps),"pacer catches up after pause");verify(!pacer.due(100_000_000_000,fps),"no catch-up burst")
    }
    val identity=FloatArray(16){if(it%5==0)1f else 0f}
    verify(DreamPerformance.visible(DreamPerformance.Bounds(-.5f,-.5f,-.5f,.5f,.5f,.5f),identity,0f),"inside frustum")
    verify(!DreamPerformance.visible(DreamPerformance.Bounds(2f,-.5f,-.5f,3f,.5f,.5f),identity,0f),"outside frustum")
    verify(DreamPerformance.visible(DreamPerformance.Bounds(-5f,-5f,-5f,5f,5f,5f),identity,0f),"camera inside huge chunk")
    var submitted=0;var resident=0
    val matrix=FloatArray(16);val f=1f/tan(33f*PI.toFloat()/180f);val far=520f;val near=.18f
    matrix[0]=f/(16f/9f);matrix[5]=f;matrix[10]=(far+near)/(near-far);matrix[11]=-1f;matrix[13]=-1.65f*f;matrix[14]=2f*far*near/(near-far)-matrix[10]*21f;matrix[15]=21f
    val boxes=java.util.Random(20)
    for(i in 0 until 1000){val x=boxes.nextFloat()*1200f-600f;val y=boxes.nextFloat()*120f-50f;val z=boxes.nextFloat()*1300f-650f;val r=boxes.nextFloat()*45f+.1f
        val bounds=DreamPerformance.Bounds(x-r,y-r,z-r,x+r,y+r,z+r)
        val clip=ArrayList<FloatArray>()
        for(xx in floatArrayOf(bounds.x0,bounds.x1))for(yy in floatArrayOf(bounds.y0,bounds.y1))for(zz in floatArrayOf(bounds.z0,bounds.z1))clip.add(FloatArray(4){row->matrix[row]*xx+matrix[row+4]*yy+matrix[row+8]*zz+matrix[row+12]})
        val rejected=(0..2).any{axis->clip.all{it[axis]< -it[3]}||clip.all{it[axis]>it[3]}}
        verify(DreamPerformance.visible(bounds,matrix,0f)==!rejected,"frustum matches eight-corner reference $i")
    }
    for((key,data)in DreamChunks.split(DreamLands.create(DreamWorlds.MEADOW)))if(DreamChunks.nearby(key,0f,21f)){resident++;if(DreamPerformance.visible(DreamPerformance.bounds(data),matrix))submitted++}
    verify(submitted in 1 until resident,"frustum reduces resident draw submissions $submitted/$resident")
    val output=File(System.getProperty("dreamcore.output","/tmp"));output.mkdirs()
    fun dump(id:String,data:FloatArray){val bytes=ByteBuffer.allocate(data.size*4).order(ByteOrder.LITTLE_ENDIAN);for(f in data)bytes.putFloat(f);File(output,"$id.f32").writeBytes(bytes.array())}
    for(id in DreamVoyage.ids+listOf(DreamWorlds.POOLS,DreamWorlds.CRATER)){
        val geometry=when(id){DreamWorlds.POOLS->DreamWorlds.createPools();DreamWorlds.CRATER->DreamWorlds.createCrater();else->DreamVoyage.create(id,DreamChronicle())}
        verify(geometry.vertices.all{it.isFinite()},"finite mesh $id")
        dump(id,geometry.vertices+DreamObjects.geometry(id,DreamChronicle()).vertices)
        for((_,data)in DreamChunks.split(geometry)){val batch=DreamPerformance.batch(data);verify(batch.data.size==data.size,"batch preserves all vertices")
            val before=(data.indices step 30).map{data.copyOfRange(it,it+30).contentHashCode()}.sorted();val after=(batch.data.indices step 30).map{batch.data.copyOfRange(it,it+30).contentHashCode()}.sorted();verify(before==after,"batch preserves triangles/materials")
        }
    }
    val world=DreamLands.create(DreamWorlds.MEADOW).vertices
    val random=java.util.Random(7319);val sight=DreamSkySight(world);var bruteCount=0L;var bvhCount=0L
    fun brute(x:Float,y:Float,z:Float,dx:Float,dy:Float,dz:Float,limit:Float):Boolean {
        for(i in world.indices step 30){if(max(world[i+1],max(world[i+11],world[i+21]))<.35f)continue;bruteCount++
            val ax=world[i];val ay=world[i+1];val az=world[i+2]
            val ux=world[i+10]-ax;val uy=world[i+11]-ay;val uz=world[i+12]-az;val vx=world[i+20]-ax;val vy=world[i+21]-ay;val vz=world[i+22]-az
            val px=dy*vz-dz*vy;val py=dz*vx-dx*vz;val pz=dx*vy-dy*vx;val det=ux*px+uy*py+uz*pz;if(abs(det)<.000001f)continue
            val inv=1f/det;val tx=x-ax;val ty=y-ay;val tz=z-az;val u=(tx*px+ty*py+tz*pz)*inv;if(u<0f||u>1f)continue
            val qx=ty*uz-tz*uy;val qy=tz*ux-tx*uz;val qz=tx*uy-ty*ux;val v=(dx*qx+dy*qy+dz*qz)*inv;if(v<0f||u+v>1f)continue
            val t=(vx*qx+vy*qy+vz*qz)*inv;if(t>.18f&&t<limit)return false
        };return true
    }
    for(i in 0 until 1200){val x=random.nextFloat()*650f-325f;val z=random.nextFloat()*730f-700f;val y=random.nextFloat()*35f+.5f
        val yaw=random.nextFloat()*6.283f;val pitch=random.nextFloat()*2f-1f;val dx=sin(yaw)*cos(pitch);val dy=sin(pitch);val dz=-cos(yaw)*cos(pitch);val limit=if(i%2==0)2000f else random.nextFloat()*180f+1f
        verify(sight.clear(x,y,z,dx,dy,dz,limit)==brute(x,y,z,dx,dy,dz,limit),"BVH matches real triangles ray $i");bvhCount+=sight.testedTriangles
    }
    verify(bvhCount*10<bruteCount,"BVH saves over 90% triangle checks: $bvhCount/$bruteCount")
    for(t in 0..600){val c=DreamCreature.pose(t.toFloat());verify(c.y in 21f..25f && abs(c.x)<=61f && c.z in -353f..-127f,"creature stays below sea")}
    dump("creature",DreamCreature.create().vertices);dump("postman",DreamShapes.presence("postman").vertices)
    File(output,"performance.txt").writeText("Resident world batches: $resident, visible submissions: $submitted.\nReal triangle-ray comparisons: BVH=$bvhCount, previous linear scan=$bruteCount; saved ${100.0*(1-bvhCount.toDouble()/bruteCount)}%.\nFrame scheduling tested at 24, 30 and 60 FPS. No device FPS/temperature measurements.\n")
    println("PASS $checks · Voyage 2.0 · BVH $bvhCount/$bruteCount")
}
