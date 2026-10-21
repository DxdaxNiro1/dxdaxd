package com.dreamcore.game

import kotlin.math.*

/** Original dark-fantasy realm: moonlit causeway, drowned gardens and ruined nave. */
object DreamDarkMap {
    private data class Solid(val x:Float,val z:Float,val rx:Float,val rz:Float)
    private val stone=floatArrayOf(.31f,.34f,.40f)
    private val trim=floatArrayOf(.52f,.53f,.60f)
    private val moss=floatArrayOf(.18f,.30f,.27f)
    private val violet=floatArrayOf(.51f,.35f,.87f)
    private val solidSpecs=buildList {
        add(Solid(-15f,-34f,11f,.65f));add(Solid(15f,-34f,11f,.65f))
        add(Solid(-26f,-52f,.65f,18f));add(Solid(26f,-52f,.65f,18f))
        add(Solid(0f,-70f,26f,.65f))
        for(z in floatArrayOf(44f,-66f))for(x in floatArrayOf(-2.5f,2.5f))add(Solid(x,z,.65f,.65f))
        for(z in floatArrayOf(-44f,-62f))for(x in floatArrayOf(-21f,21f))add(Solid(x,z,4.2f,4.2f))
        for(z in floatArrayOf(29f,9f,-15f,-48f))for(x in floatArrayOf(-7.8f,7.8f))add(Solid(x,z,.8f,.8f))
        for(z in floatArrayOf(36f,17f,-7f,-29f))for(x in floatArrayOf(-45f,45f))add(Solid(x,z,1.2f,1.2f))
    }
    private fun inLake(x:Float,z:Float)=abs(x)>12f && abs(x)<44f && z> -20f && z<16f
    fun canWalk(x:Float,z:Float):Boolean {
        if(x !in -51f..51f || z !in -69f..49f)return false
        if(abs(x) in 11.6f..44.4f && z in -20.4f..16.4f && abs(z-8f)>2.0f && abs(z+16f)>2.0f)return false
        for(i in 0 until 76) {
            val side=if(i%2==0)-1f else 1f
            val tx=side*(33f+(i%5)*4.3f);val tz=45f-(i/2)*3.3f
            if(tz < -31f && abs(tx)<40f)continue
            if(hypot(x-tx,z-tz)<.65f)return false
        }
        for(s in solidSpecs)if(abs(x-s.x)<s.rx+.30f && abs(z-s.z)<s.rz+.30f)return false
        return true
    }
    private fun cylinder(b:DreamMesh,x:Float,y:Float,z:Float,r:Float,height:Float,color:FloatArray,kind:Float=26f,n:Int=16) {
        for(i in 0 until n) {
            val a=2f*PI.toFloat()*i/n;val d=2f*PI.toFloat()*(i+1)/n
            fun p(t:Float,yy:Float)=floatArrayOf(x+r*cos(t),yy,z+r*sin(t))
            b.quad(p(a,y),p(a,y+height),p(d,y+height),p(d,y),color,kind)
            b.triangle(floatArrayOf(x,y+height,z),p(d,y+height),p(a,y+height),color,kind)
        }
    }
    private fun cone(b:DreamMesh,x:Float,y:Float,z:Float,r:Float,height:Float,color:FloatArray,n:Int=12) {
        for(i in 0 until n) {
            val a=2f*PI.toFloat()*i/n;val d=2f*PI.toFloat()*(i+1)/n
            b.triangle(floatArrayOf(x+r*cos(a),y,z+r*sin(a)),floatArrayOf(x,y+height,z),
                floatArrayOf(x+r*cos(d),y,z+r*sin(d)),color,26f)
        }
    }
    private fun arch(b:DreamMesh,cx:Float,z:Float,half:Float,spring:Float,rise:Float,color:FloatArray,kind:Float=26f) {
        for(side in floatArrayOf(-1f,1f))b.box(cx+side*(half+.28f),spring*.5f,z,.28f,spring*.5f,.55f,color,kind)
        for(i in 0 until 36) {
            val a=i/36f;val d=(i+1)/36f
            fun p(t:Float,outer:Boolean):FloatArray {
                val x=-half+2f*half*t
                val y=spring+rise*(1f-abs(x/half)).pow(.72f)
                return floatArrayOf(cx+x,y+(if(outer).42f else 0f),z)
            }
            b.quad(p(a,false),p(d,false),p(d,true),p(a,true),color,kind)
        }
    }
    private fun statue(b:DreamMesh,x:Float,z:Float,scale:Float=1f) {
        b.box(x,.26f,z,.78f*scale,.26f,.78f*scale,stone,26f)
        cone(b,x,.52f,z,.68f*scale,2.5f*scale,floatArrayOf(.23f,.17f,.31f))
        b.ellipsoid(x,3.10f*scale,z,.43f*scale,.55f*scale,.39f*scale,trim,7,10,26f)
        b.ellipsoid(x,3.13f*scale,z+.31f*scale,.27f*scale,.33f*scale,.08f,floatArrayOf(.025f,.025f,.05f),6,8)
        b.box(x+.58f*scale,2.1f*scale,z+.20f,.055f,1.65f*scale,.055f,trim,26f)
        b.ellipsoid(x+.58f*scale,3.9f*scale,z+.20f,.23f,.29f,.23f,violet,7,10,30f)
    }
    private fun portal(b:DreamMesh,z:Float) {
        arch(b,0f,z,1.8f,2.6f,2.2f,trim)
        b.box(0f,1.35f,z+.12f,1.75f,1.35f,.035f,violet,30f)
        for(i in 0 until 24) {
            val a=PI.toFloat()*i/24f;val d=PI.toFloat()*(i+1)/24f
            b.triangle(floatArrayOf(0f,2.7f,z+.12f),
                floatArrayOf(1.75f*cos(a),2.7f+1.9f*sin(a),z+.12f),
                floatArrayOf(1.75f*cos(d),2.7f+1.9f*sin(d),z+.12f),violet,30f)
        }
        for(s in floatArrayOf(-1f,1f)) {
            b.box(s*2.5f,.3f,z, .65f,.30f,.65f,stone,26f)
            cylinder(b,s*2.5f,.6f,z,.28f,2.7f,trim)
            b.ellipsoid(s*2.5f,3.5f,z,.35f,.35f,.35f,violet,8,12,30f)
        }
    }
    fun create():DreamWorlds.Scene {
        val b=DreamMesh()
        // The terrain drops below both ponds; bridges retain the walker's y=0 floor.
        for(x in -56 until 56 step 4)for(z in -76 until 52 step 4) {
            val cx=x+2f;val cz=z+2f
            val y=if(inLake(cx,cz))-.82f else -.04f
            val c=if(abs(cx)<6f || cz< -28f)stone else moss
            b.quad(floatArrayOf(x.toFloat(),y,z.toFloat()),floatArrayOf(x.toFloat(),y,z+4f),
                floatArrayOf(x+4f,y,z+4f),floatArrayOf(x+4f,y,z.toFloat()),c,if(abs(cx)<6f)25f else 26f)
        }
        for(s in floatArrayOf(-1f,1f)) {
            val left=min(s*12f,s*44f);val right=max(s*12f,s*44f)
            b.quad(floatArrayOf(left,-.28f,-20f),floatArrayOf(left,-.28f,16f),
                floatArrayOf(right,-.28f,16f),floatArrayOf(right,-.28f,-20f),floatArrayOf(.12f,.21f,.28f),1f)
            for(z in floatArrayOf(8f,-16f))b.box(s*28f,-.03f,z,16.1f,.035f,2.2f,stone,25f)
            for(z in -19..15 step 4) {
                if(abs(z+1.8f-8f)<4.4f || abs(z+1.8f+16f)<4.4f)continue
                b.box(s*11.8f,.36f,z.toFloat(),.12f,.4f,.12f,trim,26f)
                b.box(s*11.8f,.63f,z+1.8f,.06f,.06f,2f,trim,26f)
            }
        }
        // Ruined cathedral: actual open doorway and navigable central aisle.
        for(x in floatArrayOf(-15f,15f))b.box(x,8f,-34f,11f,8f,.65f,stone,26f)
        for(x in floatArrayOf(-26f,26f))b.box(x,8.5f,-52f,.65f,8.5f,18f,stone,26f)
        b.box(0f,8f,-70f,26f,8f,.65f,stone,26f)
        arch(b,0f,-33.25f,4f,7f,8.5f,trim)
        for(x in floatArrayOf(-21f,21f))for(z in floatArrayOf(-44f,-62f)) {
            cylinder(b,x,0f,z,4.2f,25f,stone,26f,18)
            cylinder(b,x,24.3f,z,4.6f,1.1f,trim,26f,18)
            cone(b,x,25.4f,z,4.6f,17f,floatArrayOf(.16f,.20f,.27f),18)
            b.ellipsoid(x,43.1f,z,.45f,.75f,.45f,violet,7,10,30f)
            for(level in 0..3)for(side in floatArrayOf(-1f,1f)) {
                b.box(x+side*1.4f,5f+level*5f,z+4.18f,.38f,1.4f,.04f,violet,30f)
            }
        }
        for(s in floatArrayOf(-1f,1f))for(k in 0 until 9) {
            val z=-35f-k*4f
            b.box(s*25.0f,8.8f,z,.23f,8.8f,.44f,trim,26f)
            cone(b,s*25.0f,17.6f,z,.60f,3.0f,trim)
        }
        for(s in floatArrayOf(-1f,1f))for(k in 0 until 5) {
            val x=s*(6f+k*3.2f)
            arch(b,x,-33.15f,1.0f,8.4f,3f,moss)
        }
        // Static wardens and braziers; no enemy AI is implied by these sculptures.
        for(z in floatArrayOf(29f,9f,-15f,-48f))for(x in floatArrayOf(-7.8f,7.8f))statue(b,x,z)
        for(z in floatArrayOf(36f,17f,-7f,-29f))for(x in floatArrayOf(-45f,45f)) {
            b.box(x,3f,z,.7f,3f,.7f,stone,26f)
            b.ellipsoid(x,6.9f,z,.55f,.55f,.55f,violet,8,12,30f)
        }
        // Dense peripheral pines and twisted bare trees, kept outside the path.
        for(i in 0 until 76) {
            val side=if(i%2==0)-1f else 1f
            val x=side*(33f+(i%5)*4.3f);val z=45f-(i/2)*3.3f
            if(z< -31f && abs(x)<40f)continue
            val h=7f+(i%5)*1.5f
            b.box(x,h*.35f,z,.23f,h*.35f,.23f,floatArrayOf(.16f,.14f,.19f),26f)
            for(j in 0..2)cone(b,x,h*.30f+j*h*.20f,z,2.6f-j*.52f,h*.55f,floatArrayOf(.08f,.17f,.17f),9)
        }
        for(i in 0 until 11) {
            val x=-95f+i*19f
            cone(b,x,-9f,-155f,18f,70f+(i%4)*19f,floatArrayOf(.09f,.12f,.20f),7)
        }
        // Rune stones and a low altar make the far exit unmistakable.
        cylinder(b,0f,-.05f,-61f,3.3f,.10f,trim,25f,32)
        for(i in 0 until 12) {
            val a=2f*PI.toFloat()*i/12f
            b.box(3f*cos(a),.12f,-61f+3f*sin(a),.13f,.02f,.22f,violet,30f)
        }
        for(x in floatArrayOf(-52f,52f))b.box(x,.42f,-12f,.45f,.42f,64f,stone,26f)
        b.box(0f,.42f,50f,52f,.42f,.45f,stone,26f)
        portal(b,44f);portal(b,-66f)
        return b.finish()
    }
}
