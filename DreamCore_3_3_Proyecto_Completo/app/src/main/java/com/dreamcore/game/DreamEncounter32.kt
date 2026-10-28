package com.dreamcore.game

import kotlin.math.*

/** Sparse encounters; no damage, save reset or attacks in the eye meadow. */
class DreamEncounter32 {
    private var world="";private var seen=false;private var wait=0f
    private var step=0f
    var approach:Float=0f;private set
    fun enter(id:String){if(world==id)return;world=id;seen=false;wait=0f;step=0f;approach=0f}
    fun tick(id:String,x:Float,z:Float,yaw:Float,dt:Float,story:DreamChronicle):String?{
        enter(id);val first=DreamEntities32.placements(id).firstOrNull()?:return null
        val dx=first.x-x;val dz=first.z+approach-z;val d=hypot(dx,dz).coerceAtLeast(.1f)
        val watching=(sin(yaw)*dx-cos(yaw)*dz)/d>.76f
        if(id==DreamHorror32.WITNESSES&&!watching&&d>22f){wait+=dt.coerceIn(0f,.1f);if(wait>3.5f&&approach<14f){approach+=.30f*dt.coerceIn(0f,.1f)}}else wait=0f
        if(!seen&&watching&&d<60f){step+=dt.coerceIn(0f,.1f);if(step>2.2f){seen=true;val key="v32:witness:"+id;val repeat=key in story.flags;story.flags.add(key);return if(repeat)"La figura sigue en el lugar que recuerdas. Su cabeza vuelve a orientarse hacia ti." else "La figura ha cambiado la dirección de la mirada. El diario conserva este encuentro."}}
        return null
    }
}
