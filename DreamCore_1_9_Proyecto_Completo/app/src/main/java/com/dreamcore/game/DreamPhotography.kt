package com.dreamcore.game

import kotlin.math.*

/** Four actual sightline photographs unlock the distant tower. */
object DreamPhotography {
    data class Target(val key:String,val x:Float,val y:Float,val z:Float)
    fun targets(id:String)=when(id){
        DreamWorlds.MEADOW->listOf(Target("casa",24f,4f,-136f))
        DreamWorlds.AQUARIUM->listOf(Target("mar",8f,24f,-190f))
        DreamExpansion.CLOUD_HOTEL->listOf(Target("ventana",0f,8f,-355f))
        DreamHorizons.CELESTIAL->listOf(Target("planeta",DreamHorizons.curve(-290f),20f,-325f))
        else->emptyList()
    }
    fun photograph(id:String,x:Float,y:Float,z:Float,yaw:Float,pitch:Float,story:DreamChronicle,clear:(Target,Float)->Boolean={_,_->true}):List<String> {
        val discovered=ArrayList<String>();val fx=sin(yaw)*cos(pitch);val fy=sin(pitch);val fz=-cos(yaw)*cos(pitch)
        for(t in targets(id)) {
            val dx=t.x-x;val dy=t.y-y;val dz=t.z-z;val distance=sqrt(dx*dx+dy*dy+dz*dz)
            if(distance in 2f..120f && (fx*dx+fy*dy+fz*dz)/distance>.95f && clear(t,distance) && story.flags.add("photo:"+t.key))discovered.add(t.key)
        }
        return discovered
    }
    fun count(story:DreamChronicle)=story.flags.count{it.startsWith("photo:")}
    fun unlocked(story:DreamChronicle)=count(story)>=4
    fun geometry(id:String):DreamWorlds.Scene {
        val b=DreamMesh()
        for(t in targets(id)){
            b.ellipsoid(t.x,t.y,t.z,.7f,1.2f,.25f,DreamShapes.ivory,12,18,60f)
            for(s in floatArrayOf(-1f,1f))b.box(t.x+s*.9f,t.y+.3f,t.z,.45f,.06f,.06f,DreamShapes.ivory,60f)
        }
        return b.finish()
    }
}
