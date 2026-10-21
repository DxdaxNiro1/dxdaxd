package com.dreamcore.game

import kotlin.math.*

/** Distinct overhead mobiles, above every walkable route. Physics meshes stay stationary. */
object DreamWorldMotion {
    fun create(id:String):DreamWorlds.Scene {
        val b=DreamMesh()
        if(id !in DreamJourney.sequence)return b.finish()
        val index=DreamJourney.sequence.indexOf(id)
        val p=if(id==DreamWorlds.CRATER)DreamJourney.Point(0f,-102f)else DreamJourney.exit(id)
        val y=if(id==DreamWorlds.CRATER)38f else DreamJourney.surface(id,p.x,p.z)+48f
        val c=floatArrayOf(.55f+(index%3)*.10f,.65f-(index%4)*.06f,.65f+(index%2)*.12f)
        for(i in 0 until 3+index%4){val a=i*2.399f+index;val x=p.x+cos(a)*(14f+index);val z=p.z+sin(a)*18f
            when(index%4){
                0->b.ellipsoid(x,y+i*2f,z,1.6f,.25f,5f,c,8,16,81f)
                1->{b.box(x,y+i,z,3f,.15f,3f,c,81f);b.box(x,y+i+1.5f,z,1f,1.4f,1f,c,82f)}
                2->{for(s in floatArrayOf(-1f,1f))b.box(x+s*2f,y+i,z,.15f,3f,.2f,c,81f);b.box(x,y+i+3f,z,2.2f,.15f,.2f,c,81f)}
                else->b.ellipsoid(x,y+i*3,z,2.8f,2.8f,2.8f,c,10,16,55f+(index%4)*.1f)
            }
        }
        return b.finish()
    }
}
