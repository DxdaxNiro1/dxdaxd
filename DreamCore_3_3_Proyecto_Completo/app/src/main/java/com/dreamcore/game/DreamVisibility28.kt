package com.dreamcore.game

/** Preserve draw order and reuse the exact conservative frustum result across scene phases. */
class DreamVisibility28<K> {
    private val previous=FloatArray(16){Float.NaN}
    private var generation=-1
    private val visible=ArrayList<K>()
    var tests=0;private set
    fun select(matrix:FloatArray,revision:Int,keys:Iterable<K>,bounds:(K)->DreamPerformance.Bounds):List<K>{
        if(generation==revision && previous.contentEquals(matrix))return visible
        generation=revision;matrix.copyInto(previous);visible.clear()
        for(key in keys){tests++;if(DreamPerformance.visible(bounds(key),matrix))visible.add(key)}
        return visible
    }
}
