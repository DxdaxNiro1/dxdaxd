package com.dreamcore.game

import kotlin.math.*

/** Small optional dreams with one unambiguous return door. */
object DreamPocket {
    const val LAUNDRY="pocket_laundry"
    const val TRAIN="pocket_train"
    const val DAWN="pocket_dawn"
    const val RETURN="__return__"
    val ids=listOf(LAUNDRY,TRAIN,DAWN,DreamHorizons.TOWER,DreamHorizons.BED)
    fun contains(id:String)=DreamNewDreams.contains(id) || DreamReferences.contains(id) || DreamVoyage.contains(id) || id in ids
    fun title(id:String)=if(DreamHorizons.contains(id))DreamHorizons.title(id)else when(id){LAUNDRY->"Lavandería entre nubes";TRAIN->"El tren bajo el agua";else->"El amanecer que no termina"}
    fun canWalk(id:String,x:Float,z:Float):Boolean {
        if(id==TRAIN) {
            if(abs(x)>3.6f || z !in -99f..10f)return false
            if(abs(x)>1.7f && z< -5f && abs((z+4f)%8f)<2.1f)return false
            return true
        }
        if(x !in -23f..23f || z !in -54f..10f)return false
        if(id==LAUNDRY && abs(x)>12.3f && z< -5f)return false
        if(id==DAWN && abs(x)>15f && z in -40f..-10f)return false
        return true
    }
    fun create(id:String):DreamWorlds.Scene {
        val b=DreamMesh();val cream=floatArrayOf(.82f,.79f,.69f);val rose=floatArrayOf(.70f,.53f,.60f)
        val width=if(id==TRAIN)4f else 24f;val length=if(id==TRAIN)100f else 55f;val height=if(id==TRAIN)3.7f else 8f
        for(x in -width.toInt() until width.toInt() step 2)for(z in -length.toInt() until 12 step 4)
            b.quad(floatArrayOf(x.toFloat(),-.04f,z.toFloat()),floatArrayOf(x.toFloat(),-.04f,z+4f),floatArrayOf(x+2f,-.04f,z+4f),floatArrayOf(x+2f,-.04f,z.toFloat()),cream,if(id==TRAIN)43f else 42f)
        b.box(0f,height+.15f,-length*.5f,width,.15f,length*.5f+12f,cream,26f)
        b.box(0f,height*.5f,-length,width,height*.5f,.18f,rose,26f)
        for(s in floatArrayOf(-1f,1f)) {
            b.box(s*width,height*.5f,-length*.5f,.18f,height*.5f,length*.5f+12f,cream,26f)
            if(id==TRAIN) {
                for(z in -94..-6 step 8) {
                    b.box(s*3.25f,.55f,z.toFloat(),.65f,.55f,1.65f,rose,42f)
                    b.box(s*3.74f,2.4f,z.toFloat(),.03f,.72f,2.4f,floatArrayOf(.16f,.43f,.52f),34f)
                }
            } else if(id==LAUNDRY) {
                for(z in -48..-8 step 8) {
                    b.box(s*17f,1.6f,z.toFloat(),3.5f,1.6f,2.2f,cream,26f)
                    b.ellipsoid(s*13.4f,1.7f,z.toFloat(),.10f,1.12f,1.12f,floatArrayOf(.24f,.34f,.40f),8,16,25f)
                    b.box(s*16f,3.3f,z.toFloat(),1f,.05f,.5f,rose,24f)
                }
                b.box(0f,4.3f,-54.7f,17f,3.1f,.04f,cream,34f)
            } else {
                b.box(s*19f,.75f,-25f,3f,.75f,14f,rose,43f)
                b.box(0f,4.3f,-54.7f,20f,3.5f,.04f,floatArrayOf(.97f,.72f,.49f),34f)
            }
        }
        for(s in floatArrayOf(-1f,1f))b.box(s*1.7f,1.7f,8f,.2f,1.7f,.2f,cream,26f)
        b.box(0f,3.55f,8f,1.9f,.15f,.2f,cream,26f)
        b.box(0f,1.5f,8.12f,1.45f,1.5f,.03f,floatArrayOf(.55f,.76f,.77f),29f)
        return b.finish()
    }
}
