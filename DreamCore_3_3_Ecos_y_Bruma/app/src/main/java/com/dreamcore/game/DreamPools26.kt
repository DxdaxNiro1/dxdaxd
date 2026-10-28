package com.dreamcore.game

import kotlin.math.*

/** The same first room and portals, with recessed basins and open skylights. */
object DreamPools26 {
    fun create():DreamWorlds.Scene{
        val b=DreamMesh();val cream=floatArrayOf(.88f,.87f,.80f)
        val wall=floatArrayOf(.86f,.75f,.81f);val rose=floatArrayOf(.82f,.61f,.67f)
        val tile=floatArrayOf(.58f,.79f,.78f);val water=floatArrayOf(.34f,.66f,.69f)
        // Separate deck pieces leave the basin bottoms visible below the water line.
        b.box(0f,-.20f,4.5f,15.5f,.20f,9.5f,cream,10f)
        b.box(0f,-.20f,-32.5f,15.5f,.20f,3.5f,cream,10f)
        b.box(0f,-.20f,-17f,1.78f,.20f,12f,cream,10f)
        for(s in floatArrayOf(-1f,1f)){
            b.box(s*12.25f,-.20f,-17f,3.25f,.20f,12f,cream,10f)
            val x=s*5.45f
            b.box(x,-1.55f,-17f,3.58f,.12f,12.0f,tile,9f)
            for(e in floatArrayOf(-1f,1f)){
                b.box(x+e*3.54f,-.75f,-17f,.05f,.73f,12f,tile,9f)
                DreamSculpt.rounded(b,x+e*3.63f,.07f,-17f,.18f,.13f,12.2f,.11f,cream,114f)
                b.box(x,-.75f,-17f+e*12f,3.55f,.73f,.06f,tile,9f)
                DreamSculpt.rounded(b,x,.07f,-17f+e*12.16f,3.80f,.13f,.18f,.11f,cream,114f)
            }
            // A thin surface, not a solid colored cuboid. Its plane matches the reflection camera.
            b.quad(floatArrayOf(x-3.48f,DreamGraphics26.POOL_WATER,-5f),floatArrayOf(x+3.48f,DreamGraphics26.POOL_WATER,-5f),floatArrayOf(x+3.48f,DreamGraphics26.POOL_WATER,-29f),floatArrayOf(x-3.48f,DreamGraphics26.POOL_WATER,-29f),water,116f)
            b.box(s*15.5f,5f,-11f,.32f,5.4f,25f,wall,11f)
            b.box(s*15.12f,.20f,-11f,.14f,.20f,25f,cream,114f)
            for(z in floatArrayOf(-4f,-12f,-20f,-28f)){
                b.box(s*15.12f,5.3f,z,.035f,2.1f,1.5f,floatArrayOf(.43f,.61f,.67f),34f)
                for(dz in floatArrayOf(-1.62f,1.62f))b.box(s*15.05f,5.3f,z+dz,.10f,2.25f,.12f,cream,115f)
                b.box(s*15f,3f,z,.22f,.12f,1.8f,cream,114f)
            }
        }
        b.box(0f,5f,-36f,15.5f,5.4f,.32f,wall,11f)
        b.box(0f,.20f,-35.6f,15f,.20f,.14f,cream,114f)
        // Roof slabs deliberately leave three real openings to the sky.
        for(s in floatArrayOf(-1f,1f))b.box(s*10.5f,10.25f,-11f,5f,.16f,25f,cream,10f)
        var front=14f
        for(z in floatArrayOf(-8f,-20f,-31f)){
            val edge=z+2.35f
            b.box(0f,10.25f,(front+edge)*.5f,5.5f,.16f,(front-edge)*.5f,cream,10f)
            for(s in floatArrayOf(-1f,1f)){
                b.box(s*5.45f,10.12f,z,.13f,.26f,2.5f,cream,114f)
                b.box(0f,10.12f,z+s*2.35f,5.5f,.26f,.13f,cream,114f)
            }
            for(x in floatArrayOf(-3.7f,-1.85f,0f,1.85f,3.7f))b.box(x,10.42f,z,.035f,.045f,2.35f,cream,15f)
            front=z-2.35f
        }
        b.box(0f,10.25f,(front-36f)*.5f,5.5f,.16f,(front+36f)*.5f,cream,10f)
        for(z in floatArrayOf(-4f,-12f,-20f,-28f)){
            for(x in floatArrayOf(-11.5f,11.5f)){
                DreamSculpt.cylinder(b,x,.08f,z,.34f,8.7f,cream,26f,16)
                DreamSculpt.cylinder(b,x,.08f,z,.53f,.20f,cream,114f,16)
                DreamSculpt.cylinder(b,x,8.65f,z,.53f,.22f,rose,114f,16)
                b.ellipsoid(x,8.25f,z,.23f,.23f,.23f,floatArrayOf(.98f,.90f,.72f),8,12,35f)
            }
            val path=(0..32).map{val a=it*PI.toFloat()/32;floatArrayOf(14.6f*cos(a),3.4f+6f*sin(a),z)}
            DreamShapes.tube(b,path,.17f,cream,26f,8)
        }
        DreamSculpt.rounded(b,0f,.10f,-32f,3.3f,.12f,2.3f,.15f,cream,114f)
        DreamSculpt.cylinder(b,0f,.22f,-32f,.60f,1.45f,rose,26f,20)
        b.ellipsoid(0f,2.65f,-32f,1.02f,1.02f,1.02f,tile,16,24,15f)
        for(s in floatArrayOf(-1f,1f)){
            b.box(s*14.92f,2.6f,-33f,.04f,2.5f,2f,tile,51f)
            for(i in 0..4)b.box(s*14.84f,2.8f,-34.6f+i*.8f,.07f,2.3f,.025f,cream,50f)
        }
        fun portal(x:Float,z:Float,k:Float,c:FloatArray){
            b.box(x,1.72f,z,1.05f,1.72f,.09f,c,k)
            for(s in floatArrayOf(-1f,1f))DreamSculpt.rounded(b,x+s*1.23f,1.85f,z,.15f,1.88f,.22f,.08f,cream,114f)
            DreamSculpt.rounded(b,x,3.75f,z,1.40f,.16f,.22f,.08f,cream,114f)
        }
        portal(0f,7.6f,2f,floatArrayOf(.30f,.27f,.57f))
        portal(10f,6.5f,3f,floatArrayOf(.54f,.83f,.91f))
        DreamSculpt.rounded(b,-4f,.72f,4.8f,1.55f,.15f,.45f,.12f,rose,43f)
        DreamSculpt.rounded(b,-4f,1.2f,5.2f,1.55f,.45f,.13f,.10f,rose,43f)
        for(x in floatArrayOf(-5.2f,-2.8f))DreamSculpt.cylinder(b,x,.02f,4.8f,.10f,.55f,cream,15f,8)
        return b.finish()
    }
}
