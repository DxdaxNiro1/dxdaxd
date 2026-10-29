package com.dreamcore.game

import android.content.Context
import android.opengl.GLES20 as G
import java.nio.ByteBuffer
import java.util.concurrent.*

/** Valid low-detail images remain visible while bounded CPU tiles and GL sub-uploads refine nearby materials. */
class DreamTextureStream33(context:Context,quality:Int){
    private val max=IntArray(1).also{G.glGetIntegerv(G.GL_MAX_TEXTURE_SIZE,it,0)}[0]
    private val size=minOf(DreamLook25.atlasSize(quality),max/2).coerceAtLeast(256)
    private var coarse:DreamSurfaceTextures.Data?=DreamSurfaceTextures.prepare(context,minOf(512,size))
    var maps=DreamSurfaceTextures.upload(coarse!!);private set
    private var target:IntArray?=null
    private val data:Future<DreamSurfaceTextures.Data>?=if(size>coarse!!.size)worker.submit<DreamSurfaceTextures.Data>{DreamSurfaceTextures.prepare(context.applicationContext,size)}else null
    private var high:DreamSurfaceTextures.Data?=null
    private data class Tile(val index:Int,val a:ByteBuffer,val n:ByteBuffer)
    private val spare=ConcurrentLinkedQueue<Pair<ByteBuffer,ByteBuffer>>()
    private val pending=java.util.ArrayDeque<Future<Tile>>()
    private val order=java.util.ArrayDeque<Int>();private var phase=0;private var finished=0;private var closed=false;private var world=""
        init {if(data!=null)for(i in 0..31)order.add(i)else coarse=null}
    val refining get()=!closed&&data!=null&&phase!=2
    private var lastPriority33=IntArray(0)
    fun step(id:String,near:IntArray=IntArray(0)){
        if(closed||data==null||phase==2)return
        if(!data.isDone)return
        if(high==null){high=try{data.get()}catch(_:Exception){closePending();phase=2;coarse=null;return}}
        if(target==null){DreamGl29.drain();val ids=IntArray(2);G.glGenTextures(2,ids,0);for(t in ids){G.glBindTexture(G.GL_TEXTURE_2D,t);for(p in intArrayOf(G.GL_TEXTURE_MIN_FILTER,G.GL_TEXTURE_MAG_FILTER))G.glTexParameteri(G.GL_TEXTURE_2D,p,G.GL_LINEAR);for(p in intArrayOf(G.GL_TEXTURE_WRAP_S,G.GL_TEXTURE_WRAP_T))G.glTexParameteri(G.GL_TEXTURE_2D,p,G.GL_CLAMP_TO_EDGE);G.glTexImage2D(G.GL_TEXTURE_2D,0,G.GL_RGBA,size,size*2,0,G.GL_RGBA,G.GL_UNSIGNED_BYTE,null)};target=ids;if(G.glGetError()!=G.GL_NO_ERROR){fallback();return}}
        if(phase==1&&(world!=id||!lastPriority33.contentEquals(near))){world=id;lastPriority33=near;val fallback=if(DreamPower.indoor(id))listOf(1,9,8,10,6,15,20,21,22)else listOf(0,13,2,3,14,16,17,20,21,22,28,29,30,31);val remaining=order.toList();order.clear();for(k in near.toList()+fallback+remaining)if(k in remaining&&!order.contains(k))order.add(k)}
        val source=if(phase==0)coarse!! else high!!
        while(pending.size<4&&order.isNotEmpty()){val index=order.removeFirst();pending.add(worker.submit<Tile>{tile(source,index,size,spare.poll())})}
        var count=0
        while(pending.isNotEmpty()&&pending.first().isDone&&count<(if(phase==0)4 else 1)){
            val t=try{pending.removeFirst().get()}catch(_:Exception){fallback();return}
            DreamGl29.drain();val w=size/4;for((unit,bytes)in listOf(0 to t.a,1 to t.n)){G.glBindTexture(G.GL_TEXTURE_2D,target!![unit]);bytes.position(0);G.glTexSubImage2D(G.GL_TEXTURE_2D,0,t.index%4*w,t.index/4*w,w,w,G.GL_RGBA,G.GL_UNSIGNED_BYTE,bytes)};if(G.glGetError()!=G.GL_NO_ERROR){fallback();return};spare.add(t.a to t.n);finished++;count++
        }
        if(phase==0&&finished==32){for(t in target!!){G.glBindTexture(G.GL_TEXTURE_2D,t);G.glTexParameteri(G.GL_TEXTURE_2D,G.GL_TEXTURE_MIN_FILTER,G.GL_LINEAR_MIPMAP_LINEAR);DreamTexture29.mipmaps()};G.glDeleteTextures(2,intArrayOf(maps.first,maps.second),0);maps=target!![0] to target!![1];coarse=null;phase=1;finished=0;for(i in 0..31)order.add(i);world="";for(i in 0..2)DreamSurfaceTextures.carpetMean[i]=high!!.mean[i]}
        else if(phase==1&&count>0&&(finished%2==0||finished==32)){for(t in target!!){G.glBindTexture(G.GL_TEXTURE_2D,t);DreamTexture29.mipmaps()};if(finished==32){phase=2;high=null}}
    }
    private fun fallback(){closePending();val ids=target;if(ids!=null&&maps.first!=ids[0]){G.glDeleteTextures(2,ids,0);target=null};phase=2;coarse=null;high=null;spare.clear()}
    private fun closePending(){for(f in pending)f.cancel(true);pending.clear();order.clear()}
    /** The previous context owns the old names; cancel CPU work without deleting names in the new one. */
    fun contextLost(){closed=true;data?.cancel(true);closePending();target=null;coarse=null;high=null;spare.clear()}
    fun close(){if(closed)return;closed=true;data?.cancel(true);closePending();val ids=target;if(ids!=null&&maps.first!=ids[0])G.glDeleteTextures(2,ids,0);G.glDeleteTextures(2,intArrayOf(maps.first,maps.second),0);coarse=null;high=null;spare.clear()}
    companion object{
        private val worker=Executors.newSingleThreadExecutor{r->Thread(r,"dream-textures").apply{isDaemon=true;priority=Thread.NORM_PRIORITY-1}}
        private fun tile(source:DreamSurfaceTextures.Data,index:Int,size:Int,reuse:Pair<ByteBuffer,ByteBuffer>?):Tile{
            val w=size/4;val sw=source.size/4;val a=reuse?.first?:ByteBuffer.allocateDirect(w*w*4);val n=reuse?.second?:ByteBuffer.allocateDirect(w*w*4);a.clear();n.clear()
            fun put(out:ByteBuffer,c:Int){out.put((c shr 16).toByte());out.put((c shr 8).toByte());out.put(c.toByte());out.put((c ushr 24).toByte())}
            for(y in 0 until w)for(x in 0 until w){val at=(index/4*sw+y*sw/w)*source.size+index%4*sw+x*sw/w;put(a,source.pixels[at]);put(n,source.normals[at])}
            a.position(0);n.position(0);return Tile(index,a,n)
        }
    }
}
