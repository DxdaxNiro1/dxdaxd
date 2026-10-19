package com.dreamcore.game

import kotlin.math.*

class DreamAtmosphere {
    var type=0;private set
    var strength=0f;private set
    var anchorX=0f;private set
    var anchorZ=0f;private set
    private var world=""
    private var time=0f
    private var cycle=-1
    fun step(id:String,x:Float,z:Float,dt:Float,visits:Int):String? {
        if(id!=world){world=id;time=0f;cycle=-1;type=0;strength=0f}
        val enabled=DreamLands.contains(id) && !DreamLands.indoor(id,x,z) || id==DreamWaterpark.ID || id==DreamJourney.DARK
        if(!enabled){strength=(strength-dt*.2f).coerceAtLeast(0f);return null}
        time+=dt
        val offset=(abs(id.hashCode()%22)).toFloat()
        val phase=(time+offset)%145f
        val nextCycle=((time+offset)/145f).toInt()
        var notice:String?=null
        if(phase>=55f && phase<92f) {
            if(cycle!=nextCycle) {
                cycle=nextCycle;anchorX=x;anchorZ=z
                type=if(id==DreamJourney.DARK || DreamLands.night(id))4 else (abs(id.hashCode()+visits+cycle)%3)+1
                notice=when(type){1->"Por un momento, la lluvia decide volver al cielo.";2->"La sombra de una nube se ha detenido sobre ti.";3->"Hay casas al otro lado del cielo.";else->"Las estrellas se apagan lentamente. El cielo sigue ahí."}
            }
            val fade=min((phase-55f)/5f,(92f-phase)/7f).coerceIn(0f,1f)
            strength=fade
        } else strength=(strength-dt*.12f).coerceAtLeast(0f)
        return notice
    }
    companion object {
        fun particles():DreamWorlds.Scene {val b=DreamMesh();for(i in 0 until 110){val x=sin(i*2.399f)*16f;val z=cos(i*1.71f)*16f;val y=(i*.71f)%16f;b.box(x,y,z,.012f,.19f,.012f,floatArrayOf(.60f,.73f,.84f),52f)};return b.finish()}
        fun skyHouses():DreamWorlds.Scene {val b=DreamMesh();for(i in -1..1){val x=i*38f;val y=65f+abs(i)*12f;val z=-70f-abs(i)*30f;b.box(x,y,z,7f,5f,7f,floatArrayOf(.73f,.75f,.78f),53f);b.triangle(floatArrayOf(x-8f,y-5f,z+7f),floatArrayOf(x+8f,y-5f,z+7f),floatArrayOf(x,y-11f,z+7f),floatArrayOf(.46f,.47f,.56f),53f)};return b.finish()}
    }
}
