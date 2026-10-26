package com.dreamcore.game

import android.opengl.GLES20 as G
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Ushort pages work on GLES2 without requiring OES_element_index_uint. */
class DreamGpuMesh28(mesh:DreamIndexed28.Mesh) {
    private data class Page(val vertex:Int,val index:Int,val shadow:Int,val first:Int,val count:Int,val shadowCount:Int,val surface:Int,val effects:Int,val surfaceCount:Int,val effectsCount:Int)
    private val pages=ArrayList<Page>()
    init {for(p in mesh.pages){
        val ids=IntArray(3);G.glGenBuffers(if(p.shadow.isEmpty())2 else 3,ids,0)
        uploadVertices(ids[0],p.vertices)
        uploadIndices(ids[1],p.indices);if(p.shadow.isNotEmpty())uploadIndices(ids[2],p.shadow)
        val layers=DreamSurface32.partition(p)
        val extra=IntArray(2);G.glGenBuffers(2,extra,0)
        uploadIndices(extra[0],layers.first);uploadIndices(extra[1],layers.second)
        pages.add(Page(ids[0],ids[1],ids[2],p.opaqueIndices,p.indices.size,p.shadow.size,extra[0],extra[1],layers.first.size,layers.second.size))
    };G.glBindBuffer(G.GL_ARRAY_BUFFER,0);G.glBindBuffer(G.GL_ELEMENT_ARRAY_BUFFER,0)}
    val handle get()=pages.firstOrNull()?.vertex?:0
    fun draw(attributes:IntArray,first:Int=0,count:Int=Int.MAX_VALUE,shadow:Boolean=false){
        var base=0
        for(p in pages){val start=if(shadow)0 else maxOf(first-base,0);val end=if(shadow)p.shadowCount else minOf(p.count,if(count==Int.MAX_VALUE)Int.MAX_VALUE else first+count-base)
            if(end>start){
                G.glBindBuffer(G.GL_ARRAY_BUFFER,p.vertex);G.glBindBuffer(G.GL_ELEMENT_ARRAY_BUFFER,if(shadow)p.shadow else p.index)
                for(i in 0..3)if(attributes[i]>=0)G.glVertexAttribPointer(attributes[i],if(i==3)1 else 3,G.GL_FLOAT,false,40,i*12)
                G.glDrawElements(G.GL_TRIANGLES,end-start,G.GL_UNSIGNED_SHORT,start*2)
            };base+=p.count
        }
    }
    fun drawOpaque(attributes:IntArray,surface:Boolean){
        for(p in pages){val n=if(surface)p.surfaceCount else p.effectsCount;if(n==0)continue
            G.glBindBuffer(G.GL_ARRAY_BUFFER,p.vertex);G.glBindBuffer(G.GL_ELEMENT_ARRAY_BUFFER,if(surface)p.surface else p.effects)
            for(i in 0..3)if(attributes[i]>=0)G.glVertexAttribPointer(attributes[i],if(i==3)1 else 3,G.GL_FLOAT,false,40,i*12)
            G.glDrawElements(G.GL_TRIANGLES,n,G.GL_UNSIGNED_SHORT,0)
        }
    }
    fun dispose(){for(p in pages)G.glDeleteBuffers(5,intArrayOf(p.vertex,p.index,p.shadow,p.surface,p.effects),0);pages.clear()}
    companion object {
        private var staging=ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
        internal val stagingBytes get()=staging.capacity()
        private fun stage(bytes:Int):ByteBuffer {
            if(staging.capacity()<bytes){val capacity=Integer.highestOneBit((bytes-1).coerceAtLeast(1)) shl 1;staging=ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder())}
            staging.clear();staging.limit(bytes);return staging
        }
        @Synchronized private fun uploadVertices(id:Int,data:FloatArray){
            val b=stage(data.size*4).asFloatBuffer();b.put(data).position(0)
            G.glBindBuffer(G.GL_ARRAY_BUFFER,id);G.glBufferData(G.GL_ARRAY_BUFFER,data.size*4,b,G.GL_STATIC_DRAW)
        }
        @Synchronized private fun uploadIndices(id:Int,data:ShortArray){
            val b=stage(data.size*2).asShortBuffer();b.put(data).position(0)
            G.glBindBuffer(G.GL_ELEMENT_ARRAY_BUFFER,id);G.glBufferData(G.GL_ELEMENT_ARRAY_BUFFER,data.size*2,b,G.GL_STATIC_DRAW)
        }
    }

}
