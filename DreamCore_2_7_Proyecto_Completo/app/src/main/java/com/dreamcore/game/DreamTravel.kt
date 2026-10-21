package com.dreamcore.game

import kotlin.math.*

/** Faster travel with a bounded collision sweep, independent of frame rate. */
object DreamTravel {
    const val WALK=4.2f
    const val RUN=10.4f
    const val CRUISE=12.4f
    fun speed(sprinting:Boolean,heldSeconds:Float,indoors:Boolean):Float {
        if(!sprinting)return WALK
        if(indoors)return 8.2f
        return RUN+(CRUISE-RUN)*((heldSeconds-1.2f)/1.6f).coerceIn(0f,1f)
    }
    fun move(id:String,x:Float,z:Float,vx:Float,vz:Float,dt:Float):Pair<Float,Float> {
        if(dt<=0f || !dt.isFinite())return x to z
        val step=dt.coerceAtMost(.1f)
        val count=ceil(hypot(vx,vz)*step/.20f).toInt().coerceIn(1,64)
        val dx=vx*step/count;val dz=vz*step/count
        var xx=x;var zz=z
        repeat(count) {
            if(DreamWorlds.canWalk(id,xx+dx,zz))xx+=dx
            if(DreamWorlds.canWalk(id,xx,zz+dz))zz+=dz
        }
        return xx to zz
    }
}
