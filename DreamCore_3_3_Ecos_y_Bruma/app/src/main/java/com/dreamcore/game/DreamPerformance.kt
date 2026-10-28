package com.dreamcore.game

import kotlin.math.*

/** Work scheduling and conservative culling, independent of Android/device timing. */
object DreamPerformance {
    class Pacer {
        private var next=0L
        fun reset(){next=0L}
        fun due(now:Long,fps:Int):Boolean {
            val interval=1_000_000_000L/fps.coerceIn(15,60)
            if(now<next)return false
            next=if(next==0L || now-next>interval*2)now+interval else next+interval
            return true
        }
    }
    data class Bounds(val x0:Float,val y0:Float,val z0:Float,val x1:Float,val y1:Float,val z1:Float)
    fun bounds(data:FloatArray):Bounds {
        var x0=Float.POSITIVE_INFINITY;var y0=x0;var z0=x0
        var x1=Float.NEGATIVE_INFINITY;var y1=x1;var z1=x1
        for(i in data.indices step 10){x0=min(x0,data[i]);x1=max(x1,data[i]);y0=min(y0,data[i+1]);y1=max(y1,data[i+1]);z0=min(z0,data[i+2]);z1=max(z1,data[i+2])}
        return Bounds(x0,y0,z0,x1,y1,z1)
    }
    fun visible(b:Bounds,m:FloatArray,margin:Float=12f):Boolean {
        // Positive support vertex of each homogeneous frustum plane. Includes animation margin.
        for(axis in 0..2)for(side in 0..1){
            val sign=if(side==0)-1 else 1
            val a=m[3]+sign*m[axis];val c=m[7]+sign*m[axis+4];val d=m[11]+sign*m[axis+8];val e=m[15]+sign*m[axis+12]
            val x=if(a>=0)b.x1+margin else b.x0-margin
            val y=if(c>=0)b.y1+margin else b.y0-margin
            val z=if(d>=0)b.z1+margin else b.z0-margin
            if(a*x+c*y+d*z+e<0f)return false
        }
        return true
    }
    /** Conservative celestial/actor bounds: no allocations and no GPU submission outside the frustum. */
    fun sphereVisible(x:Float,y:Float,z:Float,radius:Float,m:FloatArray):Boolean {
        for(axis in 0..2)for(side in 0..1){
            val sign=if(side==0)-1f else 1f
            val a=m[3]+sign*m[axis];val b=m[7]+sign*m[axis+4];val c=m[11]+sign*m[axis+8];val d=m[15]+sign*m[axis+12]
            if(a*x+b*y+c*z+d < -radius*sqrt(a*a+b*b+c*c))return false
        }
        return true
    }
    fun translucent(kind:Float)=kind in 49.5f..50.5f || kind in 51.5f..54.5f || kind in 67.5f..70.5f || kind in 91.5f..92.5f || kind in 104.5f..105.5f || kind in 115.5f..116.5f
    data class Batch(val data:FloatArray,val opaque:Int)
    fun batch(source:FloatArray):Batch {
        val out=FloatArray(source.size);var write=0
        for(i in source.indices step 30)if(!translucent(source[i+9])){source.copyInto(out,write,i,i+30);write+=30}
        val count=write/10
        for(i in source.indices step 30)if(translucent(source[i+9])){source.copyInto(out,write,i,i+30);write+=30}
        return Batch(out,count)
    }
}
