package com.dreamcore.game

import kotlin.math.*

object DreamVoyage {
    const val REFUGE="pocket_refuge"
    const val HOUSE="photo_house"
    const val SEA="photo_sea"
    const val WINDOW="photo_window"
    const val PLANET="photo_planet"
    const val LETTER="pocket_letter"
    val photos=listOf(HOUSE,SEA,WINDOW,PLANET)
    val ids=listOf(REFUGE,HOUSE,SEA,WINDOW,PLANET,LETTER,DreamGravity.ID)
    fun contains(id:String)=id in ids
    fun base(id:String)=when(id){HOUSE->DreamWorlds.MEADOW;SEA->DreamWorlds.AQUARIUM;WINDOW->DreamExpansion.CLOUD_HOTEL;PLANET->DreamHorizons.CELESTIAL;else->id}
    fun photoWorld(source:String)=when(source){DreamWorlds.MEADOW->HOUSE;DreamWorlds.AQUARIUM->SEA;DreamExpansion.CLOUD_HOTEL->WINDOW;DreamHorizons.CELESTIAL->PLANET;else->null}
    fun title(id:String)=when(id){REFUGE->"La habitación que te espera";HOUSE->"Fotografía: la casa sin nadie";SEA->"Fotografía: el mar inmóvil";WINDOW->"Fotografía: las ventanas de ayer";PLANET->"Fotografía: el planeta detenido";LETTER->"La casa del destinatario ausente";else->"El suelo que se volvió cielo"}
    fun spawn(id:String)=when(id){HOUSE->DreamJourney.Point(24f,-120f);SEA->DreamJourney.Point(8f,-160f);WINDOW->DreamJourney.Point(0f,-328f);PLANET->DreamJourney.Point(DreamHorizons.curve(-285f),-285f);else->DreamJourney.Point(0f,2f)}
    fun gate(id:String)=spawn(id).let{DreamJourney.Point(it.x,it.z+6f)}
    fun surface(id:String,x:Float,z:Float)=if(id in photos)DreamJourney.surface(base(id),x,z)else 0f
    fun indoor(id:String,x:Float,z:Float)=id==REFUGE || id==LETTER || (id in photos && (DreamLands.indoor(base(id),x,z)||DreamHorizons.indoor(base(id),x,z)))
    fun canWalk(id:String,x:Float,z:Float):Boolean=when(id){
        DreamGravity.ID->DreamGravity.canWalk(x,z)
        REFUGE->x in -13.3f..13.3f && z in -24f..10f && !(abs(x)<7.4f&&abs(z+10f)<1.5f) && !(x in -11.5f..-6.2f&&z in -21f..-14f)
        LETTER->x in -17f..17f && z in -41f..10f && !(abs(x)<4f&&z in -29f..-24f)
        else->if(id in photos)DreamWorlds.canWalk(base(id),x,z)else false
    }
    fun create(id:String,story:DreamChronicle):DreamWorlds.Scene {
        if(id==DreamGravity.ID)return DreamGravity.create()
        val b=DreamMesh();val cream=DreamShapes.ivory;val rose=DreamShapes.rose
        if(id in photos){
            val source=if(base(id)==DreamHorizons.CELESTIAL)DreamHorizons.create(base(id))else DreamLands.create(base(id))
            val p=spawn(id);val y=surface(id,p.x,p.z)
            // A single changed landmark in each still photograph, plus a clear way back.
            b.ellipsoid(p.x+6f,y+3.6f,p.z-8f,.65f,1.4f,.30f,cream,12,18,84f)
            for(i in 0..8)b.ellipsoid(p.x+sin(i*2.399f)*6f,y+2f+i*.35f,p.z-9f+cos(i.toFloat())*3f,.08f,.08f,.08f,rose,4,7,24f)
            return DreamWorlds.Scene(source.vertices+DreamObjects.geometry(base(id),story).vertices+b.finish().vertices+returnGate(id))
        }
        val width=if(id==REFUGE)14f else 18f;val back=if(id==REFUGE)-25f else -42f
        for(x in -width.toInt() until width.toInt() step 2)for(z in back.toInt() until 12 step 4)b.quad(floatArrayOf(x.toFloat(),-.04f,z.toFloat()),floatArrayOf(x.toFloat(),-.04f,z+4f),floatArrayOf(x+2f,-.04f,z+4f),floatArrayOf(x+2f,-.04f,z.toFloat()),cream,43f)
        for(s in floatArrayOf(-1f,1f))b.box(s*width,3.5f,(back+12f)*.5f,.2f,3.5f,(12f-back)*.5f,cream,0f)
        b.box(0f,3.5f,back,width,3.5f,.2f,cream,0f)
        b.box(0f,3.5f,12f,width,3.5f,.2f,cream,0f)
        b.box(0f,7.2f,(back+12f)*.5f,width,.2f,(12f-back)*.5f,cream,0f)
        if(id==REFUGE){
            b.box(0f,1.1f,-10f,7f,.2f,1.1f,rose,43f)
            for(x in floatArrayOf(-5f,5f))b.box(x,.55f,-10f,.12f,.55f,.7f,cream,40f)
            DreamShapes.bed(b,-9f,0f,-17.5f)
            b.box(13.75f,3.7f,-12f,.025f,2.5f,6f,floatArrayOf(.25f,.34f,.48f),34f)
            for(i in 0..3){val x=-9f+i*6f;val z=-24.7f;val y=4.5f
                b.box(x,y,z,2.6f,1.8f,.12f,rose,43f)
                val n=floatArrayOf(0f,0f,1f)
                val a=floatArrayOf(x-2.35f,y-1.55f,z+.14f);val c=floatArrayOf(x+2.35f,y-1.55f,z+.14f);val d=floatArrayOf(x+2.35f,y+1.55f,z+.14f);val e=floatArrayOf(x-2.35f,y+1.55f,z+.14f)
                val ua=floatArrayOf(0f,1f,0f);val uc=floatArrayOf(1f,1f,0f);val ud=floatArrayOf(1f,0f,0f);val ue=floatArrayOf(0f,0f,0f)
                b.mappedTriangle(a,c,d,ua,uc,ud,n,n,n,78f+i*.1f);b.mappedTriangle(a,d,e,ua,ud,ue,n,n,n,78f+i*.1f)
            }
            if(story.memories>=3){b.ellipsoid(-13.65f,4.6f,-5f,.10f,1.1f,1.1f,cream,12,20,25f);b.box(-13.50f,4.7f,-5f,.03f,.06f,.7f,rose)}
            if(story.calls.isNotEmpty()){b.box(8f,.9f,-20f,1.5f,.8f,.7f,rose,43f);b.box(8f,1.9f,-20f,.7f,.10f,.25f,cream,40f)}
            DreamShapes.cup(b,2f,1.35f,-10f)
            if((story.visits[id]?:0)>1){DreamShapes.cup(b,3f,1.35f,-10f);DreamShapes.chair(b,3f,0f,-13f);for(i in 0..5)b.ellipsoid(9f+(i%2)*.4f,.015f,-6f-i*.5f,.13f,.01f,.24f,rose,3,6,54f)}
            DreamShapes.plant(b,10f,0f,-4f,1.5f)
        }else{
            b.box(0f,1.1f,-27f,3.7f,.18f,2f,rose,43f)
            for(i in -2..2){b.box(i*1.1f,1.35f,-27f,.4f,.025f,.6f,cream,43f);DreamShapes.chair(b,i*1.3f,0f,-32f)}
            b.box(0f,4f,-41.7f,8f,2.5f,.03f,floatArrayOf(.55f,.65f,.75f),34f)
            for(i in 0..4)DreamShapes.plant(b,-14f+i*7f,0f,-39f,1.8f)
        }
        return DreamWorlds.Scene(b.finish().vertices+returnGate(id))
    }
    private fun returnGate(id:String):FloatArray {
        val b=DreamMesh();val p=gate(id);val y=surface(id,p.x,p.z)
        b.box(p.x,y+1.6f,p.z,1.5f,1.6f,.035f,floatArrayOf(.56f,.77f,.78f),29f)
        for(s in floatArrayOf(-1f,1f))b.box(p.x+s*1.75f,y+1.7f,p.z,.15f,1.7f,.15f,DreamShapes.ivory,26f)
        return b.finish().vertices
    }
}
