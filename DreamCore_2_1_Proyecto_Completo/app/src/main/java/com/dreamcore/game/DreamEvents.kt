package com.dreamcore.game

import kotlin.math.*

/** Quiet authored encounters. Clocks advance only with foreground simulation. */
class DreamEvents {
    var time=0f;private set
    var busAge=-1f;private set
    val busVisible get()=busAge in 0f..23f
    val busZ get()=when{busAge<8f->-395f+busAge*18f;busAge<15f->-251f;else->-251f-(busAge-15f)*22f}
    val busX=24f
    var transform=0f;private set
    private var world="";private var companionStep=1.9f;private var piano=7f;private var noticedCup=false
    fun step(id:String,x:Float,z:Float,yaw:Float,moving:Boolean,dt:Float,story:DreamChronicle):List<String> {
        val out=ArrayList<String>()
        if(id!=world){world=id;time=0f;busAge=-1f;companionStep=1.9f;piano=7f;noticedCup=false;transform=0f}
        time+=dt
        if(id==DreamWorlds.MEADOW && "bus:departed" !in story.flags && hypot(x-35f,z+250f)<85f && busAge<0f)busAge=0f
        if(busAge>=0f){busAge+=dt;if(busAge>=23f && story.flags.add("bus:departed"))out.add("La parada vuelve a estar vacía. El autobús se ha llevado a alguien.")}
        if((id==DreamWorlds.MEADOW || id==DreamHorizons.CITY) && moving){companionStep-=dt;if(companionStep<0f){companionStep=1.9f;out.add("@companion")}}
        if(id==DreamHorizons.CITY && hypot(x+140f,z+435f)<23f && story.flags.add("discovery:party"))out.add("La fiesta está preparada. Hay un sitio para ti, pero nadie ha empezado a comer.")
        if(id==DreamPocket.DAWN){
            if(hypot(x+6f,z+20f)<8f)noticedCup=true
            if(noticedCup && cos(yaw)<.1f && story.flags.add("companion:cup"))out.add("Al volver la vista, hay otra taza. Todavía sale vapor.")
        }
        if(id==DreamHorizons.CITY && z< -140f && story.flags.add("companion:door"))out.add("Escuchas un picaporte. Alguien ha dejado abierta la puerta de atrás.")
        val distance=when(id){DreamHorizons.CITY->hypot(x+140f,z+315f);DreamWorlds.HOUSE_CRATER->hypot(x-28f,z+150f);else->1000f}
        piano-=dt
        if(distance<6f){if(story.flags.add("piano:"+id))out.add("La habitación está vacía. Aquí ya no suena ningún piano.")}
        else if(distance<65f && piano<0f && "piano:"+id !in story.flags){piano=12f;out.add("@piano")}
        if(id==DreamHorizons.TOWER || id==DreamHorizons.BED)transform=(transform+dt*.15f).coerceAtMost(1f)
        return out
    }
    companion object {
        fun geometry(id:String):DreamWorlds.Scene {
            val b=DreamMesh()
            if(id==DreamPocket.DAWN){b.box(-6f,1f,-20f,2f,.16f,1.1f,DreamShapes.ivory,43f);DreamShapes.cup(b,-6.6f,1.2f,-20f);DreamShapes.cup(b,-5.4f,1.2f,-20f,66f)}
            if(id==DreamHorizons.CITY){b.box(28f,3f,-214.65f,3.5f,3f,.10f,DreamShapes.rose,65f)}
            if(id==DreamWorlds.MEADOW || id==DreamWorlds.AQUARIUM || id==DreamExpansion.CLOUD_HOTEL)DreamHorizons.towerSilhouette(b,220f,0f,-490f,.9f)
            if(id==DreamHorizons.CITY){
                for(i in 0..5)b.ellipsoid(-140f+i*1.4f,8f+sin(i.toFloat()),-315f,4f,2f,3f,DreamShapes.ivory,10,18,56f)
                for(i in 0..45)b.box(sin(i*2.399f)*8f,(i*.73f)%12f,84f+cos(i*1.71f)*8f,.014f,.23f,.014f,DreamShapes.ivory,69f)
            }
            if(id==DreamHorizons.BED)for(i in 0..55)b.box(-3f+sin(i*2.399f)*2f,(i*.71f)%7f,-12f+cos(i*1.71f)*2.8f,.012f,.18f,.012f,DreamShapes.ivory,69f)
            if(id==DreamWorlds.HOUSE_CRATER)for(i in 0..85)b.ellipsoid(sin(i*2.399f)*25f,(i*.79f)%12f,-130f+cos(i*1.71f)*45f,.035f,.035f,.035f,DreamShapes.ivory,4,6,68f)
            if(id==DreamHorizons.TOWER)for(s in floatArrayOf(-1f,1f))b.box(s*11.6f,5f,-70f,.025f,5f,12f,floatArrayOf(.32f,.58f,.63f),70f)
            return b.finish()
        }
        fun bus():DreamWorlds.Scene {
            val b=DreamMesh();val pale=DreamShapes.ivory;val rose=DreamShapes.rose
            b.box(0f,2f,0f,2.6f,1.55f,7f,pale,67f)
            b.box(0f,2.7f,7.1f,2.3f,.7f,.04f,floatArrayOf(.24f,.35f,.42f),67f)
            for(s in floatArrayOf(-1f,1f))for(z in -5..5 step 2){b.box(s*2.65f,2.6f,z.toFloat(),.035f,.65f,.8f,rose,67f)}
            for(s in floatArrayOf(-1f,1f))for(z in floatArrayOf(-4.3f,4.3f))b.ellipsoid(s*2.6f,.65f,z,.36f,.65f,.65f,DreamShapes.dark,10,14,67f)
            return b.finish()
        }
    }
}
