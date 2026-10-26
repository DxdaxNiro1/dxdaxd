package com.dreamcore.game
import android.content.Context
import android.content.res.Resources
import android.opengl.GLES20 as G
fun main(args:Array<String>){
 var checks=0;fun verify(v:Boolean,m:String){check(v){m};checks++}
 val context=Context(Resources(args.single()))
 fun pump(s:DreamTextureStream33,n:Int=600){repeat(n){s.step(if(it%7==0)DreamWorlds.POOLS else DreamWorlds.MEADOW,intArrayOf(it%32,10,0));Thread.sleep(2)}}
 G.reset();val low=DreamTextureStream33(context,0);verify(G.live.size==2&&G.live.values.all{it.width==512&&it.filled.all{q->q}},"complete valid fallback is immediate");pump(low,3);low.close();low.close();verify(G.live.isEmpty(),"idempotent close releases low-quality textures")
 for(q in 1..2){G.reset();G.limit=2048;val s=DreamTextureStream33(context,q);val original=s.maps;verify(G.live.values.all{it.width==512},"low detail first $q")
  var done=false;val start=System.nanoTime()
  while(!done&&(System.nanoTime()-start)<30_000_000_000L){val before=G.subs;s.step(if(G.subs%7==0)DreamWorlds.POOLS else DreamWorlds.MEADOW,intArrayOf((G.subs/2)%32,10,0));verify(G.subs-before<=8,"bounded tile work per frame $q")
   if(s.maps!=original)verify(G.live.getValue(s.maps.first).filled.all{it}&&G.live.getValue(s.maps.second).filled.all{it},"never bind an uninitialized atlas tile $q")
   done=G.subs>=128;Thread.sleep(2)
  }
  verify(done&&s.maps!=original&&G.live.size==2,"both refinement phases complete, old textures deleted $q")
  val expected=DreamSurfaceTextures.prepare(context,1024)
  for((id,source)in listOf(s.maps.first to expected.pixels,s.maps.second to expected.normals)){
   val texture=G.live.getValue(id);for(tile in 0..31)for(i in 0..7){val x=tile%4*256+(i*31+9)%256;val y=tile/4*256+(i*13+2)%256;val color=source[y*1024+x];for(channel in 0..3){val shift=intArrayOf(16,8,0,24)[channel];verify((texture.bytes[(y*1024+x)*4+channel].toInt()and 255)==((color ushr shift)and 255),"refinement retains numerical RGBA channels $q/$tile/$channel")}}
  };s.close();verify(G.live.isEmpty(),"all high-detail resources released $q")
 }
 for(phase in 0..1){G.reset();val s=DreamTextureStream33(context,1);val original=s.maps;if(phase==0)G.failAllocation=true else G.failSub=true;pump(s);verify(s.maps==original&&G.live.size==2,"driver rejection keeps the last valid pair $phase");s.close();verify(G.live.isEmpty(),"failed staging pair does not leak $phase")}
 G.reset();val mip=DreamTextureStream33(context,1);G.failMip=true;pump(mip,1200);verify(listOf(mip.maps.first,mip.maps.second).all{G.minFilters[it]==G.GL_LINEAR},"rejected mip generation retains complete linear filtering");mip.close();verify(G.live.isEmpty(),"mipmap fallback releases textures")
 G.reset();val partial=DreamTextureStream33(context,1);partial.close();pump(partial,3);verify(G.live.isEmpty(),"late worker completion cannot recreate a closed stream")
 G.reset();val lost=DreamTextureStream33(context,1);G.reset();val sentinel=IntArray(1);G.glGenTextures(1,sentinel,0);G.glBindTexture(G.GL_TEXTURE_2D,sentinel[0]);G.glTexImage2D(G.GL_TEXTURE_2D,0,G.GL_RGBA,8,16,0,G.GL_RGBA,G.GL_UNSIGNED_BYTE,null);lost.contextLost();lost.close();pump(lost,3);verify(G.live.containsKey(sentinel[0]),"context loss never deletes names owned by its replacement context")
 println("PASS $checks progressive atlas/driver fallback boundary checks; simulated GL contracts, real PNG data")
}
