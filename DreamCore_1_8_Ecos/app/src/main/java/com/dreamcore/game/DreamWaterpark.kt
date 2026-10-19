package com.dreamcore.game

import kotlin.math.*

/** An original industrial water dream inspired by the user's video. Raised routes have support. */
object DreamWaterpark {
    const val ID="dream_13"
    fun centre(z:Float)=24f*sin((z-70f)*.025f)
    fun height(x:Float,z:Float)=3f+1.4f*sin(z*.018f)
    val islands=floatArrayOf(72f,-30f,-120f,-215f)
    fun canWalk(x:Float,z:Float):Boolean {
        if(z !in -239f..91f)return false
        val bridge=abs(x-centre(z))<5.0f
        val island=islands.any{hypot(x-centre(it),z-it)<17f}
        val duckBridge=z in -45f..-35f && x in -65f..centre(-40f)
        val duckIsland=hypot(x+55f,z+40f)<18f
        if(!(bridge||island||duckBridge||duckIsland))return false
        if(islands.any{zz->floatArrayOf(-1f,1f).any{s->abs(x-(centre(zz)+s*11f))<2.5f && abs(z-(zz+4f))<.9f}})return false
        if(hypot(x+55f,z+40f)<11.8f)return false // duck statue base
        return true
    }
    fun create():DreamWorlds.Scene {
        val b=DreamMesh();val tile=floatArrayOf(.49f,.67f,.66f)
        val metal=floatArrayOf(.57f,.47f,.30f);val ivory=floatArrayOf(.65f,.67f,.63f)
        fun p(x:Float,z:Float)=floatArrayOf(x,height(x,z),z)
        fun beam(ax:Float,ay:Float,az:Float,bx:Float,by:Float,bz:Float,w:Float,c:FloatArray,k:Float=40f) {
            val dx=bx-ax;val dz=bz-az;val len=hypot(dx,dz).coerceAtLeast(.001f)
            val nx=-dz/len*w;val nz=dx/len*w
            b.quad(floatArrayOf(ax+nx,ay,az+nz),floatArrayOf(bx+nx,by,bz+nz),floatArrayOf(bx-nx,by,bz-nz),floatArrayOf(ax-nx,ay,az-nz),c,k)
            b.quad(floatArrayOf(ax,ay-w,az),floatArrayOf(bx,by-w,bz),floatArrayOf(bx,by+w,bz),floatArrayOf(ax,ay+w,az),c,k)
        }
        for(z in -238 until 90 step 2) {
            val a=z.toFloat();val d=z+2f
            b.quad(p(centre(a)-5.5f,a),p(centre(a)+5.5f,a),p(centre(d)+5.5f,d),p(centre(d)-5.5f,d),tile,41f)
            for(s in floatArrayOf(-1f,1f)) {
                val x0=centre(a)+s*5.6f;val x1=centre(d)+s*5.6f
                beam(x0,height(x0,a)+1.25f,a,x1,height(x1,d)+1.25f,d,.08f,metal)
                if(z%6==0)b.box(x0,height(x0,a)+.65f,a,.08f,.65f,.08f,metal,40f)
            }
        }
        for(z in islands) {
            val cx=centre(z)
            for(i in 0 until 64) {
                val a=i*2f*PI.toFloat()/64;val d=(i+1)*2f*PI.toFloat()/64
                b.triangle(p(cx,z),p(cx+18f*cos(d),z+18f*sin(d)),p(cx+18f*cos(a),z+18f*sin(a)),tile,41f)
                if(abs(cos(a))>.36f) {
                    val x=cx+18f*cos(a);val zz=z+18f*sin(a)
                    b.box(x,height(x,zz)+.65f,zz,.08f,.65f,.08f,metal,40f)
                }
            }
            for(s in floatArrayOf(-1f,1f))b.box(cx+s*11f,height(cx,z)+1f,z+4f,2.2f,.35f,.55f,ivory,40f)
        }
        // A horizontal branch approaches an enormous aged ceramic duck.
        for(x in -72 until 24 step 4) {
            b.quad(p(x.toFloat(),-45f),p(x+4f,-45f),p(x+4f,-35f),p(x.toFloat(),-35f),tile,41f)
        }
        for(i in 0 until 48) {
            val a=i*2f*PI.toFloat()/48;val d=(i+1)*2f*PI.toFloat()/48
            b.triangle(p(-55f,-40f),p(-55f+19f*cos(d),-40f+19f*sin(d)),p(-55f+19f*cos(a),-40f+19f*sin(a)),tile,41f)
        }
        val ochre=floatArrayOf(.64f,.48f,.22f)
        b.ellipsoid(-55f,10f,-40f,11f,8f,9f,ochre,12,20,51f)
        b.ellipsoid(-55f,20f,-37f,6.5f,7f,6f,ochre,12,20,51f)
        b.box(-55f,18.5f,-30.2f,4.1f,1.2f,2.2f,floatArrayOf(.53f,.29f,.12f),40f)
        for(s in floatArrayOf(-1f,1f))b.ellipsoid(-55f+s*3.2f,22f,-31.6f,.75f,.75f,.42f,floatArrayOf(.04f,.05f,.045f),7,10)
        // Giant sculptural faces and falling curtains of water.
        for((x,z)in listOf(65f to -75f,-68f to -180f)) {
            b.ellipsoid(x,22f,z,16f,25f,14f,ivory,14,24,44f)
            b.box(x,22f,z+13f,15.8f,23f,.12f,ivory,49f)
            for(s in floatArrayOf(-1f,1f))b.box(x+s*5f,6f,z+14f,.7f,18f,.04f,tile,50f)
            b.box(x,-6f,z,20f,5f,18f,metal,40f)
        }
        // Long elevated slide tubes, decorative and clearly outside the walking routes.
        for(side in floatArrayOf(-1f,1f)) {
            fun centrePoint(t:Float)=floatArrayOf(side*(45f+20f*sin(t*PI.toFloat()*3)),22f+13f*cos(t*PI.toFloat()*2),65f-t*300f)
            for(i in 0 until 100) {
                val a=centrePoint(i/100f);val d=centrePoint((i+1)/100f)
                for(j in 0 until 12) {
                    val aa=j*2f*PI.toFloat()/12;val dd=(j+1)*2f*PI.toFloat()/12
                    fun q(c:FloatArray,v:Float)=floatArrayOf(c[0]+2.4f*cos(v),c[1]+2.4f*sin(v),c[2])
                    b.quad(q(a,aa),q(d,aa),q(d,dd),q(a,dd),if(side<0)metal else tile,if(side<0)40f else 41f)
                }
            }
        }
        for(x in -110..110 step 22)for(z in -260..100 step 45) {
            if(abs(x)<30)continue
            b.box(x.toFloat(),18f,z.toFloat(),.65f,35f,.65f,ivory,40f)
            b.box(x.toFloat(),-11f,z.toFloat(),4f,.1f,4f,tile,1f)
        }
        for(x in -140 until 140 step 20)for(z in -280 until 120 step 20)
            b.quad(floatArrayOf(x.toFloat(),-13f,z.toFloat()),floatArrayOf(x.toFloat(),-13f,z+20f),floatArrayOf(x+20f,-13f,z+20f),floatArrayOf(x+20f,-13f,z.toFloat()),floatArrayOf(.12f,.29f,.32f),1f)
        return b.finish()
    }
}
