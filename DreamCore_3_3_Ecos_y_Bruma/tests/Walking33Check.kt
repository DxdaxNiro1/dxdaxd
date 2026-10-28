package com.dreamcore.game
import kotlin.math.*
import java.io.File
fun main(){
 val rows=ArrayList<String>();var checks=0
 for(t in DreamMusic24.tracks.filter{it.world!="title"}){
  val id=t.world;val story=DreamChronicle();val c=DreamChapter33(story);DreamLands.configure(story.layout(id));DreamCraterLayout.seed=story.universeSeed
  val scene=DreamSceneFactory33.create(id,story);val sight=DreamSkySight(scene.vertices+DreamObjects.geometry(id,story).vertices)
  val a=if(id==DreamWorlds.HOUSE_MEADOW)DreamJourney.Arrival(0f,4f,0f)else DreamJourney.arrival(id,DreamWorlds.STATION)
  fun ok(x:Float,z:Float)=DreamWorlds.canWalk(id,x,z)&&c.furnitureClear(id,x,z,0f)&&(id==DreamWorlds.CRATER||id==DreamGravity.ID||sight.bodyClear33(x,DreamJourney.surface(id,x,z),z))
  var sx=a.x;var sz=a.z
  if(!ok(sx,sz))loop@for(r in 1..10)for(i in 0..23){val angle=i*PI.toFloat()/12;val x=a.x+cos(angle)*r*.45f;val z=a.z+sin(angle)*r*.45f;if(ok(x,z)){sx=x;sz=z;break@loop}}
  for(n in c.nodes(id)){
   if(id==DreamWorlds.CRATER||id==DreamGravity.ID){rows.add("$id\t${n.key}\tspecialized radial/fall regression\t0");continue}
   data class Cell(val x:Int,val z:Int,val cost:Float,val rank:Float)
   val queue=java.util.PriorityQueue<Cell>(compareBy{it.rank});val seen=HashMap<Long,Float>();val step=.75f
   fun key(x:Int,z:Int)=(x.toLong() shl 32) xor (z.toLong() and 0xffffffffL)
   queue.add(Cell(0,0,0f,hypot(sx-n.x,sz-n.z)));seen[key(0,0)]=0f;var found=false;var cells=0
   while(queue.isNotEmpty()&&cells<100000){val q=queue.remove();val x=sx+q.x*step;val z=sz+q.z*step;cells++;if(hypot(x-n.x,z-n.z)<n.range-.15f){found=true;break};val h=DreamJourney.surface(id,x,z)
    for(d in arrayOf(1 to 0,-1 to 0,0 to 1,0 to -1)){val nx=q.x+d.first;val nz=q.z+d.second;val k=key(nx,nz);val cost=q.cost+step;if((seen[k]?:Float.POSITIVE_INFINITY)<=cost)continue;val xx=sx+nx*step;val zz=sz+nz*step
      if(ok(xx,zz)&&abs(DreamJourney.surface(id,xx,zz)-h)<.65f&&ok((x+xx)/2,(z+zz)/2)){seen[k]=cost;queue.add(Cell(nx,nz,cost,cost+hypot(xx-n.x,zz-n.z)))}
    }
   }
   val row="$id\t${n.key}\t${if(found)"OK" else "BLOCKED@${n.x},${n.z}"}\t$cells";check(found){row};rows.add(row);checks++
  }
 }
 File(System.getProperty("dreamcore.output","/tmp"),"walking_audit33.tsv").writeText(rows.joinToString("\n")+"\n");println("PASS $checks approaches")
}
