package com.dreamcore.game

import kotlin.math.floor

/** CPU chunks for one scene. GPU residency is managed on the GL thread. */
object DreamChunks {
    const val SIZE=64f
    data class Key(val x:Int,val z:Int)
    fun key(x:Float,z:Float)=Key(floor(x/SIZE).toInt(),floor(z/SIZE).toInt())
    fun nearby(key:Key,x:Float,z:Float):Boolean {
        val dx=(key.x+.5f)*SIZE-x;val dz=(key.z+.5f)*SIZE-z
        return dx*dx+dz*dz<400f*400f
    }
    fun split(scene:DreamWorlds.Scene):Map<Key,FloatArray> {
        val data=scene.vertices
        val counts=HashMap<Key,Int>()
        fun triangleKey(i:Int)=key((data[i]+data[i+10]+data[i+20])/3f,
            (data[i+2]+data[i+12]+data[i+22])/3f)
        for(i in data.indices step 30) {
            if(data[i+9] in 19.5f..20.5f)continue
            val k=triangleKey(i);counts[k]=(counts[k]?:0)+30
        }
        val output=counts.mapValues{FloatArray(it.value)}
        val offset=HashMap<Key,Int>()
        for(i in data.indices step 30) {
            if(data[i+9] in 19.5f..20.5f)continue
            val k=triangleKey(i);val cursor=offset[k]?:0
            data.copyInto(output.getValue(k),cursor,i,i+30);offset[k]=cursor+30
        }
        return output
    }
}
