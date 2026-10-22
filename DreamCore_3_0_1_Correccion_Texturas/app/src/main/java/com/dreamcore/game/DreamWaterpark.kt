package com.dreamcore.game

import kotlin.math.*

/** Expanded raised waterpark: walking mesh and collision follow the same routes. */
object DreamWaterpark {
    const val ID="dream_13"
    fun centre(z:Float)=42f*sin((z-90f)*.014f)
    fun height(x:Float,z:Float)=6f+2f*sin(z*.011f)
    val islands=floatArrayOf(80f,-60f,-200f,-340f,-480f)
    fun canWalk(x:Float,z:Float):Boolean {
        if(z !in -513f..101f)return false
        val bridge=abs(x-centre(z))<6f
        val island=islands.any{hypot(x-centre(it),z-it)<25f}
        val branch=z in -171f..-157f && x in -108f..centre(-164f)
        val duckIsland=hypot(x+108f,z+164f)<24f
        if(!(bridge||island||branch||duckIsland))return false
        if(hypot(x+108f,z+164f)<13f)return false
        if(islands.any{zz->abs(abs(x-centre(zz))-16f)<3.0f && abs(z-(zz+6f))<1.0f})return false
        return true
    }
    fun create():DreamWorlds.Scene {
        val b=DreamMesh();val tile=floatArrayOf(.44f,.67f,.65f);val rust=floatArrayOf(.55f,.39f,.28f);val pale=DreamShapes.ivory
        fun p(x:Float,z:Float)=floatArrayOf(x,height(x,z),z)
        for(z in -512 until 100 step 2){val a=z.toFloat();val d=z+2f
            b.quad(p(centre(a)-6.7f,a),p(centre(a)+6.7f,a),p(centre(d)+6.7f,d),p(centre(d)-6.7f,d),tile,41f)
            for(s in floatArrayOf(-1f,1f)){
                DreamShapes.tube(b,listOf(floatArrayOf(centre(a)+s*6.9f,height(0f,a)+1.3f,a),floatArrayOf(centre(d)+s*6.9f,height(0f,d)+1.3f,d)),.075f,rust,40f,7)
                if(z%8==0)b.box(centre(a)+s*6.9f,height(0f,a)+.65f,a,.07f,.65f,.07f,rust,40f)
            }}
        for(z in islands){val cx=centre(z)
            for(i in 0..63){val a=i*2f*PI.toFloat()/64;val d=(i+1)*2f*PI.toFloat()/64
                b.triangle(p(cx,z),p(cx+26f*cos(d),z+26f*sin(d)),p(cx+26f*cos(a),z+26f*sin(a)),tile,41f)}
            for(s in floatArrayOf(-1f,1f)){b.box(cx+s*16f,height(cx,z)+.8f,z+6f,2.7f,.18f,.65f,pale,43f);for(t in floatArrayOf(-1f,1f))b.box(cx+s*16f+t*2f,height(cx,z)+.3f,z+6f,.12f,.4f,.45f,rust,40f)}
        }
        for(x in -108 until 43 step 4)b.quad(p(x.toFloat(),-171f),p(x+4f,-171f),p(x+4f,-157f),p(x.toFloat(),-157f),tile,41f)
        for(i in 0..47){val a=i*PI.toFloat()/24;val d=(i+1)*PI.toFloat()/24;b.triangle(p(-108f,-164f),p(-108f+25f*cos(d),-164f+25f*sin(d)),p(-108f+25f*cos(a),-164f+25f*sin(a)),tile,41f)}
        val ochre=floatArrayOf(.71f,.53f,.24f)
        b.ellipsoid(-108f,13f,-164f,12f,9f,10f,ochre,20,32,51f)
        b.ellipsoid(-108f,25f,-160f,7f,8f,6.5f,ochre,20,32,51f)
        b.ellipsoid(-108f,23f,-152.8f,5.2f,1.4f,3.4f,rust,12,20,51f)
        for(s in floatArrayOf(-1f,1f)){b.ellipsoid(-108f+s*3.5f,27.3f,-154.6f,.8f,.8f,.45f,DreamShapes.dark,12,16);b.ellipsoid(-108f+s*9f,13f,-164f,3f,6f,7f,ochre,14,24,51f)}
        for((x,z)in listOf(80f to -110f,-80f to -340f,90f to -485f)) {
            DreamShapes.face(b,x,31f,z,1.4f)
            b.ellipsoid(x,-8f,z,26f,4f,24f,rust,10,24,44f)
        }
        for(side in floatArrayOf(-1f,1f))for(lane in 0..2){
            val path=(0..120).map{val t=it/120f;floatArrayOf(side*(128f+24f*sin(t*PI.toFloat()*5+lane)),23f+lane*7f+14f*cos(t*PI.toFloat()*3),90f-t*620f)}
            DreamShapes.tube(b,path,2.1f+lane*.25f,if(lane%2==0)rust else tile,if(lane%2==0)40f else 41f,18)
        }
        for(x in -180..180 step 30)for(z in -540..100 step 65)if(abs(x)>65){b.box(x.toFloat(),21f,z.toFloat(),.5f,38f,.5f,rust,40f);b.box(x.toFloat(),50f,z.toFloat(),.7f,.7f,12f,rust,40f)}
        for(x in -220 until 220 step 20)for(z in -560 until 140 step 20)b.quad(floatArrayOf(x.toFloat(),-16f,z.toFloat()),floatArrayOf(x.toFloat(),-16f,z+20f),floatArrayOf(x+20f,-16f,z+20f),floatArrayOf(x+20f,-16f,z.toFloat()),floatArrayOf(.13f,.30f,.32f),1f)
        return b.finish()
    }
}
