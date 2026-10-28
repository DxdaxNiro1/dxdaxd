package com.dreamcore.game

import kotlin.math.*

/** A deliberately authored encounter: ten-second warning, two aisles, two shelters and a permanent return. */
object DreamArchive33{
    const val ID="dream_archive33"
    private val racks=listOf(floatArrayOf(-5.5f,5.5f,-29f,-19f),floatArrayOf(-5.5f,5.5f,-61f,-49f),floatArrayOf(-5.5f,5.5f,-85f,-73f))
    private val points=listOf(0f to 8f,-12f to -12f,12f to -12f,-12f to -36f,12f to -36f,0f to -39f,-12f to -66f,12f to -66f,0f to -68f,-12f to -91f,12f to -91f,0f to -94f,0f to -110f)
    fun canWalk(x:Float,z:Float):Boolean=x.isFinite()&&z.isFinite()&&x in -17f..17f&&z in -126f..16f&&racks.none{x in (it[0]-.38f)..(it[1]+.38f)&&z in (it[2]-.38f)..(it[3]+.38f)}&&!(x in 7.6f..10.4f&&z in -49.3f..-47f)&&!(x in -10.95f..-7.05f&&z in -31.55f..-28.45f)
    fun lineClear(ax:Float,az:Float,bx:Float,bz:Float):Boolean{val n=(hypot(bx-ax,bz-az)/.45f).toInt().coerceAtLeast(1);for(i in 0..n){val t=i.toFloat()/n;if(!canWalk(ax+(bx-ax)*t,az+(bz-az)*t))return false};return true}
    private val edges=Array(points.size){a->points.indices.filter{it!=a&&lineClear(points[a].first,points[a].second,points[it].first,points[it].second)}.toIntArray()}
    fun advance(x:Float,z:Float,tx:Float,tz:Float,step:Float):Pair<Float,Float>{
        var target=tx to tz
        if(!lineClear(x,z,tx,tz)){
            val cost=FloatArray(points.size){Float.POSITIVE_INFINITY};val used=BooleanArray(points.size);val next=IntArray(points.size){-1}
            for(i in points.indices)if(lineClear(points[i].first,points[i].second,tx,tz))cost[i]=hypot(points[i].first-tx,points[i].second-tz)
            repeat(points.size){val at=points.indices.filter{!used[it]}.minByOrNull{cost[it]}?:return@repeat;if(!cost[at].isFinite())return@repeat;used[at]=true
                for(n in edges[at]){val d=cost[at]+hypot(points[n].first-points[at].first,points[n].second-points[at].second);if(d<cost[n]){cost[n]=d;next[n]=at}}}
            val at=points.indices.filter{cost[it].isFinite()&&lineClear(x,z,points[it].first,points[it].second)}.minByOrNull{cost[it]+hypot(points[it].first-x,points[it].second-z)}?:return x to z
            val chosen=if(hypot(points[at].first-x,points[at].second-z)<.45f&&next[at]>=0)next[at]else at
            target=points[chosen]
        }
        val dx=target.first-x;val dz=target.second-z;val distance=hypot(dx,dz);val move=min(step.coerceIn(0f,.5f),distance);val nx=x+dx/distance.coerceAtLeast(.001f)*move;val nz=z+dz/distance.coerceAtLeast(.001f)*move
        return if(canWalk(nx,nz))nx to nz else x to z
    }
    fun create():DreamWorlds.Scene{
        val b=DreamMesh();val wall=floatArrayOf(.47f,.43f,.35f);val steel=floatArrayOf(.22f,.24f,.25f)
        for(x in -20 until 20 step 4)for(z in -132 until 20 step 4)b.quad(floatArrayOf(x.toFloat(),-.04f,z.toFloat()),floatArrayOf(x.toFloat(),-.04f,z+4f),floatArrayOf(x+4f,-.04f,z+4f),floatArrayOf(x+4f,-.04f,z.toFloat()),wall,44f)
        for(s in floatArrayOf(-1f,1f)){b.box(s*18f,4f,-56f,.3f,4f,76f,wall,14f);for(z in -116..4 step 15){b.box(s*16f,5.5f,z.toFloat(),1f,.09f,.18f,floatArrayOf(.9f,.74f,.49f),24f)}}
        for(z in floatArrayOf(-132f,20f))b.box(0f,4f,z,18f,4f,.22f,wall,14f)
        b.box(0f,8.2f,-56f,18f,.2f,76f,steel,15f)
        for(r in racks){val x=(r[0]+r[1])*.5f;val z=(r[2]+r[3])*.5f;val sx=(r[1]-r[0])*.5f;val sz=(r[3]-r[2])*.5f
            b.box(x,2f,z,sx,2f,sz,steel,15f);for(y in 1..4)b.box(x,y.toFloat(),z,sx+.15f,.045f,sz+.15f,wall,15f)
            for(s in floatArrayOf(-1f,1f))for(i in 0..7)DreamSculpt.rounded(b,s*(sx+.05f),1f+(i%3)*.9f,z-sz+.6f+i*(sz*2-1.2f)/7,.16f,.32f,.40f,.025f,wall,43f)}
        for(z in floatArrayOf(13f,-124f)){for(s in floatArrayOf(-1f,1f))b.box(s*1.8f,1.9f,z,.18f,1.9f,.18f,wall,26f);b.box(0f,3.9f,z,2f,.14f,.2f,wall,26f);b.box(0f,1.8f,z,1.6f,1.8f,.025f,floatArrayOf(.31f,.53f,.49f),29f)}
        return b.finish()
    }
}
object DreamCrossroads33{
    data class Link(val target:String,val label:String)
    private val pairs=listOf(Triple(DreamExpansion.CLOUD_HOTEL,DreamReferences.OFFICE,"La puerta del turno ausente"),Triple(DreamReferences.HALL,DreamWorlds.MEADOW,"El corredor de la ventana encendida"),Triple(DreamWorld30.LIBRARY,DreamArchive33.ID,"Depósito de las voces"),Triple(DreamWorlds.POOLS,DreamPocket.LAUNDRY,"Servicio de las toallas"),Triple(DreamHorror32.GARDEN,DreamReferences.CINEMA,"La proyección de las antenas"),Triple(DreamVoyage.REFUGE,DreamWorld30.LIBRARY,"El pasillo de la archivista"))
    fun links(id:String)=pairs.mapNotNull{when(id){it.first->Link(it.second,it.third);it.second->Link(it.first,it.third);else->null}}
}
