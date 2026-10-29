package com.dreamcore.game

import kotlin.math.*

/** Atlas tile bounds are prepared beside indexing, then ranked by distance without scanning vertices per frame. */
object DreamMaterialPriority33 {
    fun tile(v:FloatArray,i:Int,code:Float):Int {
        val k=v[i+9]
        return when {
            k>110.5f&&k<111.5f->(20+floor(v[i+5]+.5f).toInt()).coerceIn(20,31)
            k<.5f->if(v[i+4]>v[i+3]*1.18f&&v[i+4]>v[i+5]*1.12f)0 else 1
            k>3.5f&&k<19.5f->if(code>29.5f&&k>14.5f&&k<15.5f)15 else floor(k-4f+.5f).toInt()
            k>24.5f&&k<25.5f->7
            k>25.5f&&k<26.5f->if(code in 11.5f..12.5f||code in 18.5f..19.5f||code in 48.5f..50.5f)14 else 6
            k>26.5f&&k<27.5f->2
            k>39.5f&&k<44.5f->when {k>43.5f->10;k>42.5f->8;k>41.5f->9;else->4+floor(k-40f+.5f).toInt()}
            k>89.5f&&k<90.5f->0
            k>90.5f&&k<91.5f->1
            k>92.5f&&k<93.5f->3
            k>103.5f&&k<104.5f->1
            k>107.5f&&k<108.5f->0
            k>109.5f&&k<110.5f->15
            k>111.5f&&k<115.5f->16+floor(k-112f+.5f).toInt()
            k>121.5f&&k<122.5f->9
            else->-1
        }
    }
    fun bounds(vertices:FloatArray,code:Float):FloatArray {
        val out=FloatArray(128){i->if(i%4<2)Float.POSITIVE_INFINITY else Float.NEGATIVE_INFINITY}
        for(i in vertices.indices step 10){val tile=tile(vertices,i,code);if(tile<0)continue;val at=tile*4;val x=vertices[i];val z=vertices[i+2];out[at]=min(out[at],x);out[at+1]=min(out[at+1],z);out[at+2]=max(out[at+2],x);out[at+3]=max(out[at+3],z)}
        return out
    }
    fun nearest(chunks:Collection<FloatArray>,x:Float,z:Float):IntArray {
        val distance=FloatArray(32){Float.POSITIVE_INFINITY}
        for(b in chunks)for(tile in 0..31){val i=tile*4;if(!b[i].isFinite())continue;val dx=max(max(b[i]-x,x-b[i+2]),0f);val dz=max(max(b[i+1]-z,z-b[i+3]),0f);distance[tile]=min(distance[tile],dx*dx+dz*dz)}
        return distance.indices.filter{distance[it].isFinite()}.sortedBy{distance[it]}.toIntArray()
    }
}
