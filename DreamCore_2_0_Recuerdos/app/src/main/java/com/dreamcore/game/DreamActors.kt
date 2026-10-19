package com.dreamcore.game

import kotlin.math.*

class DreamActors {
    data class Actor(val role:String,var x:Float,var z:Float,var alpha:Float=0f,var age:Float=0f)
    val actors=ArrayList<Actor>()
    var soundX=0f;private set
    var soundZ=0f;private set
    var traceX=0f;private set
    var traceZ=0f;private set
    var traceLife=0f;private set
    private var world=""
    private var chime=10f
    private var shifted=false
    fun step(id:String,x:Float,z:Float,yaw:Float,dt:Float,story:DreamChronicle):List<String> {
        val notices=ArrayList<String>()
        if(id!=world) {
            world=id;actors.clear();chime=10f;shifted=false;traceLife=0f
            when(id) {
                DreamHorizons.DARK->{actors.add(Actor("mourner",24f,-125f));actors.add(Actor("mourner",-26f,-270f))}
                DreamHorizons.CITY->actors.add(Actor("window",37f,-170f))
                DreamWorlds.AQUARIUM->actors.add(Actor("mourner",40f,-200f))
                DreamWorlds.MEADOW->{actors.add(Actor("curious",x+12f,z-22f));actors.add(Actor("postman",-26f,-181.5f))}
                DreamWaterpark.ID,DreamExpansion.TUNNELS->actors.add(Actor("guide",if(id==DreamWaterpark.ID)DreamWaterpark.centre(z-18f)else 0f,z-18f))
                DreamWorlds.HOUSE_MEADOW->actors.add(Actor("window",10.5f,z-35f))
                DreamExpansion.CLOUD_HOTEL->actors.add(Actor("window",80f,-62.7f))
            }
        }
        traceLife=(traceLife-dt).coerceAtLeast(0f);chime-=dt
        for(a in actors) {
            a.age+=dt
            val distance=hypot(a.x-x,a.z-z)
            val dot=(sin(yaw)*(a.x-x)-cos(yaw)*(a.z-z))/distance.coerceAtLeast(.01f)
            val seen=dot>.95f && distance<100f
            a.alpha=(a.alpha+dt*.4f).coerceAtMost(if(distance<180f)1f else 0f)
            if(a.role=="curious" && !seen && distance>10f) {
                val nx=a.x+(x-a.x)/max(distance,.1f)*dt*1.7f;val nz=a.z+(z-a.z)/max(distance,.1f)*dt*1.7f
                if(DreamWorlds.canWalk(id,nx,nz)){a.x=nx;a.z=nz}
            }
            if(a.role=="mourner") {
                // An unnaturally tall presence that keeps its distance, never attacks.
                if(!seen && distance>24f && distance<100f){val nx=a.x+(x-a.x)/distance*dt*.6f;val nz=a.z+(z-a.z)/distance*dt*.6f;if(DreamWorlds.canWalk(id,nx,nz)){a.x=nx;a.z=nz}}
            }
            if(a.role=="guide") {
                val targetZ=max(DreamJourney.exit(id).z+9f,z-20f)
                val targetX=if(id==DreamWaterpark.ID)DreamWaterpark.centre(targetZ)else 0f
                val d=hypot(targetX-a.x,targetZ-a.z).coerceAtLeast(.1f)
                val nx=a.x+(targetX-a.x)/d*dt*3f;val nz=a.z+(targetZ-a.z)/d*dt*3f
                if(DreamWorlds.canWalk(id,nx,nz)){a.x=nx;a.z=nz}
                if(chime<=0f && distance<65f){soundX=a.x;soundZ=a.z;notices.add("@guide");chime=14f}
            }
            if(a.role=="window" && seen)shifted=true
            if(a.role=="window" && shifted && !seen && a.age>3f) {
                traceX=if(id==DreamWorlds.HOUSE_MEADOW)8f else 54f;traceZ=a.z;traceLife=25f
                a.x=if(id==DreamWorlds.HOUSE_MEADOW)-10.5f else if(id==DreamHorizons.CITY)-37f else -80f
                a.z=if(id==DreamWorlds.HOUSE_MEADOW)max(-240f,z-45f)else -142.7f
                a.age=0f;shifted=false
            }
            if(distance<45f && story.flags.add("met:"+a.role))notices.add(when(a.role){"curious"->"La figura se acerca cuando no la miras. Se detiene para dejarte espacio.";"mourner"->"La figura es demasiado alta. No avanza mientras la miras.";"guide"->"Alguien delante de ti marca el camino con un sonido.";"postman"->"El cartero espera junto al camino. Lleva una carta sin sello.";else->"Había alguien en esa ventana. En el suelo han quedado sus huellas."})
        }
        return notices
    }
    companion object {
        fun head():DreamWorlds.Scene {
            val source=DreamShapes.presence("curious").vertices;val selected=ArrayList<Float>()
            for(i in source.indices step 30)if(source[i+1]>3.5f && source[i+11]>3.5f && source[i+21]>3.5f)for(j in i until i+30)selected.add(source[j])
            return DreamWorlds.Scene(selected.toFloatArray())
        }
        fun traces():DreamWorlds.Scene {val b=DreamMesh();for(i in 0..5)b.ellipsoid((if(i%2==0)-.2f else .2f),.025f,-i*.55f,.13f,.015f,.26f,floatArrayOf(.23f,.26f,.24f),3,7,54f);return b.finish()}
    }
}
