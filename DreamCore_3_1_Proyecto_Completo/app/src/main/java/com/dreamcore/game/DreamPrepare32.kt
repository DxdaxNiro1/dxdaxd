package com.dreamcore.game

import java.util.concurrent.Executors
import java.util.concurrent.Future

/** Pure immutable mesh work only; Android/GL uploads stay on the render thread. One bounded worker. */
object DreamPrepare32 {
    data class Ready(val key:DreamChunks.Key,val bounds:DreamPerformance.Bounds,val opaque:Int,val mesh:DreamIndexed28.Mesh)
    private val worker=Executors.newSingleThreadExecutor{r->Thread(r,"dream-mesh").apply{isDaemon=true;priority=Thread.NORM_PRIORITY-1}}
    fun submit(key:DreamChunks.Key,raw:FloatArray,shadow:Boolean):Future<Ready> =worker.submit<Ready>{
        val batch=DreamPerformance.batch(raw)
        Ready(key,DreamPerformance.bounds(raw),batch.opaque,DreamIndexed28.encode(batch.data,batch.opaque,shadow))
    }
}
