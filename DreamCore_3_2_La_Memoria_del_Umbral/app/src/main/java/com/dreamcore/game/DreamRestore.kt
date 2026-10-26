package com.dreamcore.game

import kotlin.math.*

/** Remodelled props may occupy a formerly empty saved position. Move to nearby supported ground. */
object DreamRestore {
    private val changed=setOf(DreamWorlds.MEADOW,DreamWorlds.GARDEN,DreamExpansion.CHESS,DreamExpansion.TUNNELS,DreamExpansion.ROAD)+DreamNewDreams.ids+DreamRoutes23.ids+DreamVigilia27.ids+DreamWorld30.ids+DreamHorror32.ids+DreamArchive33.ID
    fun repair(c:DreamChronicle.Checkpoint):DreamChronicle.Checkpoint {
        if(c.world !in changed || DreamWorlds.canWalk(c.world,c.x,c.z))return c
        for(r in 1..20)for(i in 0 until 24){val a=i*2f*PI.toFloat()/24;val x=c.x+cos(a)*r*.65f;val z=c.z+sin(a)*r*.65f
            if(DreamWorlds.canWalk(c.world,x,z)){val y=DreamJourney.surface(c.world,x,z)+1.65f;if(abs(y-c.y)<4f)return c.copy(x=x,y=y,z=z)}
        }
        val p=DreamJourney.arrival(c.world,DreamWorlds.STATION)
        return c.copy(x=p.x,y=DreamJourney.surface(c.world,p.x,p.z)+1.65f,z=p.z,yaw=p.yaw)
    }
}
