package com.dreamcore.game

/** Frame cadence and CPU submission are kept separately; no claim of GPU timing or silicon temperature. */
class DreamMetrics33 {
    data class Sample(val world:String,val frames:Int,val meanMs:Double,val p95Ms:Double,val janks:Int,val maxMs:Double,val cpuMeanMs:Double)
    private class Counter {var count=0;var total=0.0;var cpu=0.0;var worst=0.0;var janks=0;val histogram=IntArray(251)}
    private var current="";private val recent=DoubleArray(120);private val recentCpu=DoubleArray(120);private var recentCount=0;private var recentAt=0
    private val worlds=LinkedHashMap<String,Counter>();private var running=true
    private var elapsed=0L
    @Synchronized fun reset(){worlds.clear();elapsed=0;running=true;current="";recentCount=0;recentAt=0}
    @Synchronized fun stop(){running=false}
    @Synchronized fun frame(world:String,intervalNanos:Long,cpuNanos:Long,fps:Int){
        if(!running||intervalNanos<=0||!DreamChronicle.validWorld(world))return
        val c=worlds.getOrPut(world){Counter()};val ms=intervalNanos/1e6;if(current!=world){current=world;recentCount=0;recentAt=0};recent[recentAt]=ms;recentCpu[recentAt]=cpuNanos.coerceAtLeast(0)/1e6;recentAt=(recentAt+1)%recent.size;recentCount=minOf(recentCount+1,recent.size);c.count++;c.total+=ms;c.cpu+=cpuNanos.coerceAtLeast(0)/1e6;c.worst=maxOf(c.worst,ms);if(ms>1500.0/fps.coerceIn(15,60))c.janks++
        c.histogram[ms.toInt().coerceIn(0,250)]++;elapsed+=intervalNanos
    }
    @Synchronized fun samples():List<Sample> = worlds.map{(id,c)->var at=0;var p95=0;for(i in c.histogram.indices){at+=c.histogram[i];if(at>=kotlin.math.ceil(c.count*.95).toInt()){p95=i;break}};Sample(id,c.count,c.total/c.count,p95.toDouble(),c.janks,c.worst,c.cpu/c.count)}
    @Synchronized fun seconds()=elapsed/1e9
    @Synchronized fun hud():String{val c=worlds[current]?:return "Midiendo…";val count=recentCount.coerceAtLeast(1);val ms=recent.take(count).sum()/count;val cpu=recentCpu.take(count).sum()/count;return "${(1000.0/ms).toInt()} FPS · ${c.janks} tirones · CPU ${"%.1f".format(java.util.Locale.US,cpu)} ms"}
    @Synchronized fun json(device:String,batteryC:Float?,thermal:String,heapBytes:Long,nativeBytes:Long,graphics:String):String{
        fun q(s:String)=buildString{append('"');for(c in s)when(c){'"'->append("\\\"");'\\'->append("\\\\");'\n'->append("\\n");'\r'->append("\\r");'\t'->append("\\t");else->if(c.code<32)append("\\u%04x".format(c.code))else append(c)};append('"')}
        fun n(d:Double)=String.format(java.util.Locale.US,"%.3f",d)
        val rows=samples().joinToString(","){s->"{\"world\":${q(s.world)},\"frames\":${s.frames},\"frame_mean_ms\":${n(s.meanMs)},\"frame_p95_bucket_ms\":${s.p95Ms.toInt()},\"janks\":${s.janks},\"frame_max_ms\":${n(s.maxMs)},\"cpu_submission_mean_ms\":${n(s.cpuMeanMs)}}"}
        return "{\"version\":\"3.4\",\"device\":${q(device)},\"seconds\":${n(seconds())},\"battery_temperature_c\":${batteryC?.takeIf{it.isFinite()}?.let{n(it.toDouble())}?:"null"},\"thermal_status\":${q(thermal)},\"java_heap_bytes\":$heapBytes,\"native_heap_bytes\":$nativeBytes,\"graphics\":${q(graphics)},\"gpu_time_measured\":false,\"soc_temperature_measured\":false,\"samples\":[$rows]}"
    }
}
