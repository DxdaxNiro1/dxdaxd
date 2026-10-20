package com.dreamcore.game

import kotlin.math.*

/** Wet road, a vending-machine island, and a house at the end of an endless meadow. */
object DreamRoadMap {
    const val ID = "dream_12"
    const val SPAWN_X = 0f
    const val SPAWN_Z = 52f

    private fun c(r:Float,g:Float,b:Float)=floatArrayOf(r,g,b)
    private val ivory=c(.90f,.88f,.76f)
    private val white=c(.87f,.89f,.83f)
    private val pink=c(.79f,.54f,.64f)
    private val red=c(.62f,.25f,.30f)
    private val dark=c(.18f,.24f,.30f)
    private val grass=c(.30f,.44f,.28f)
    private val warm=c(1f,.82f,.52f)

    private data class Solid(val x:Float,val y:Float,val z:Float,
                             val sx:Float,val sy:Float,val sz:Float,
                             val color:FloatArray,val kind:Float=0f)

    private val solids:List<Solid> = buildList {
        fun box(x:Float,y:Float,z:Float,sx:Float,sy:Float,sz:Float,
                color:FloatArray,kind:Float=0f) { add(Solid(x,y,z,sx,sy,sz,color,kind)) }
        // The cottage has two genuinely open doors; the hallway crosses it end to end.
        for(x in floatArrayOf(-8f,8f)) box(x,3f,-48f,.20f,3f,8f,ivory)
        for(z in floatArrayOf(-40f,-56f)) for(side in floatArrayOf(-1f,1f))
            box(side*4.8f,3f,z,3.2f,3f,.20f,ivory)
        // Two side rooms, with physical 2.8 m openings off the central hallway.
        for(x in floatArrayOf(-2.3f,2.3f)) {
            box(x,2.6f,-42f,.12f,2.6f,2f,white)
            box(x,2.6f,-52.7f,.12f,2.6f,3.3f,white)
        }
        box(-5.9f,.48f,-52.3f,1.5f,.48f,2.25f,white,26f)
        box(-5.9f,1f,-54.3f,1.55f,1f,.15f,pink,26f)
        box(6.85f,.93f,-51.4f,.8f,.93f,3.0f,pink,26f)
        box(5.5f,.66f,-43.2f,1.2f,.13f,.9f,ivory,26f)
        for(x in floatArrayOf(4.6f,6.4f)) for(z in floatArrayOf(-43.8f,-42.6f))
            box(x,.26f,z,.08f,.26f,.08f,dark,26f)
        // Porch columns and seats remain away from the open doorway.
        for(x in floatArrayOf(-7.3f,7.3f)) for(z in floatArrayOf(-36.4f,-59.6f))
            box(x,2.85f,z,.18f,2.85f,.18f,white,26f)
        for(x in floatArrayOf(-5.2f,5.2f)) {
            box(x,.55f,-37.1f,1.12f,.18f,.55f,white,26f)
            box(x,1.10f,-37.57f,1.12f,.40f,.08f,white,26f)
        }
        // Vending machines, canopy legs, and a small bus shelter across the road.
        for(i in 0..2) box(12.6f+i*3.5f,1.8f,29f,1.35f,1.8f,1.1f,
            when(i){0->c(.38f,.63f,.78f);1->c(.84f,.72f,.38f);else->pink},26f)
        for(x in floatArrayOf(10f,22f)) for(z in floatArrayOf(25.8f,33.5f))
            box(x,2.7f,z,.12f,2.7f,.12f,white,26f)
        box(-20f,2.1f,18f,4.0f,2.1f,.14f,c(.52f,.65f,.68f),26f)
        for(x in floatArrayOf(-24f,-16f)) box(x,2.1f,20f,.13f,2.1f,2f,white,26f)
        box(-20f,.63f,18.8f,2.9f,.15f,.65f,white,26f)
        box(-20f,1.20f,18.27f,2.9f,.48f,.10f,white,26f)
        for(z in floatArrayOf(-66f,60f)) for(x in floatArrayOf(-3.1f,3.1f))
            box(x,2.65f,z,.3f,2.65f,.3f,white,26f)
        // Road lamps are collision objects, rather than thin pass-through props.
        for(i in 0 until 6) {
            val z=-26f+i*14f
            val x=if(i%2==0) -8.0f else 8.0f
            box(x,3.5f,z,.10f,3.5f,.10f,dark,26f)
        }
    }

    fun canWalk(x:Float,z:Float):Boolean {
        if(x !in -59.2f..59.2f || z !in -79.2f..67.2f) return false
        // Farm fence flanks the house path, with openings at both garden ends.
        if(abs(abs(x)-11.8f)<.4f && z>-62f && z< -28f) return false
        for(s in solids) if(s.y-s.sy<2.1f && s.y+s.sy>.18f &&
            abs(x-s.x)<s.sx+.33f && abs(z-s.z)<s.sz+.33f) return false
        return true
    }

    fun portalAt(x:Float,z:Float):String? = when {
        abs(x)<2.5f && abs(z-60f)<1.9f -> DreamWorlds.STATION
        abs(x)<2.5f && abs(z+66f)<1.9f -> "dream_09"
        else -> null
    }

    private fun roof(b:DreamMesh) {
        fun v(x:Float,y:Float,z:Float)=floatArrayOf(x,y,z)
        // Gabled roof with a real silhouette; horizontal seams echo old clay roofing.
        for(side in floatArrayOf(-1f,1f)) {
            for(i in 0 until 15) {
                val x0=side*i*.62f;val x1=side*(i+1)*.62f
                val y0=10.3f-abs(x0)*.46f;val y1=10.3f-abs(x1)*.46f
                val color=if(i%2==0) red else c(.67f,.28f,.31f)
                if(side>0f)
                    b.quad(v(x0,y0,-58f),v(x0,y0,-38f),v(x1,y1,-38f),v(x1,y1,-58f),color,26f)
                else
                    b.quad(v(x1,y1,-58f),v(x1,y1,-38f),v(x0,y0,-38f),v(x0,y0,-58f),color,26f)
                for(j in 0 until 20) {
                    val z=-57.6f+j
                    b.box((x0+x1)*.5f,(y0+y1)*.5f+.025f,z,.30f,.035f,.028f,c(.74f,.35f,.36f),26f)
                }
            }
        }
        for(z in floatArrayOf(-40.05f,-55.95f)) {
            if(z> -48f) b.triangle(v(-8f,6f,z),v(8f,6f,z),v(0f,10.2f,z),ivory)
            else b.triangle(v(8f,6f,z),v(-8f,6f,z),v(0f,10.2f,z),ivory)
            b.box(0f,7.65f,z+.03f,.78f,.78f,.055f,dark,26f)
            b.box(0f,7.65f,z+.08f,.055f,.8f,.055f,white,26f)
            b.box(0f,7.65f,z+.08f,.8f,.055f,.055f,white,26f)
        }
        b.box(4.6f,9.0f,-51.5f,.72f,1.7f,.8f,c(.63f,.54f,.47f),26f)
        b.box(4.6f,10.8f,-51.5f,.85f,.12f,.93f,ivory,26f)
    }

    fun create():DreamWorlds.Scene {
        val b=DreamMesh()
        b.box(0f,-.24f,-6f,260f,.23f,260f,grass)
        // The wet asphalt is geometry over the meadow, with a broad uninterrupted road.
        for(i in -40 until 35) {
            val z=i*2f
            b.quad(floatArrayOf(-5.4f,.012f,z),floatArrayOf(-5.4f,.012f,z+2f),
                floatArrayOf(5.4f,.012f,z+2f),floatArrayOf(5.4f,.012f,z),c(.30f,.33f,.41f),25f)
            for(side in floatArrayOf(-1f,1f)) {
                b.box(side*5.15f,.02f,z+1f,.075f,.008f,.98f,ivory,25f)
                b.box(side*6.5f,-.01f,z+1f,.95f,.025f,1f,c(.55f,.56f,.56f),25f)
            }
            if(i%4==0 && z> -36f) b.box(0f,.025f,z+1f,.08f,.008f,.83f,c(.82f,.71f,.44f),25f)
        }
        // The vending island and shelter have a wet apron at the same walking height.
        b.box(16f,-.005f,30f,8f,.025f,7.5f,c(.48f,.46f,.55f),25f)
        b.box(-20f,-.005f,20f,5f,.025f,5f,c(.47f,.49f,.53f),25f)
        for(i in 0..3) b.box(11f+i*3.2f,.025f,35f,.045f,.01f,1.3f,ivory,25f)
        // Terrace and interior are flush; no invisible steps or gravity overrides.
        b.box(0f,-.03f,-48f,9.2f,.04f,12f,ivory,25f)
        for(ix in -8 until 8) for(iz in -56 until -40) {
            val color=if((ix+iz) and 1==0)c(.78f,.77f,.71f) else c(.84f,.82f,.74f)
            b.quad(floatArrayOf(ix+.02f,.025f,iz+.02f),floatArrayOf(ix+.02f,.025f,iz+.98f),
                floatArrayOf(ix+.98f,.025f,iz+.98f),floatArrayOf(ix+.98f,.025f,iz+.02f),color,25f)
        }
        solids.forEach{b.box(it.x,it.y,it.z,it.sx,it.sy,it.sz,it.color,it.kind)}
        roof(b)
        b.box(0f,5.85f,-48f,7.8f,.10f,7.8f,white)
        for(z in floatArrayOf(-36f,-60f)) {
            b.box(0f,5.8f,z,9.0f,.12f,1.85f,pink,26f)
            b.box(0f,5.48f,z,8.6f,.04f,.07f,warm,24f)
            b.box(0f,3.2f,if(z> -40f)-39.75f else -56.25f,1.65f,.12f,.08f,white,26f)
        }
        // Painted timber siding with separate reveals, plus lit windows and muntins.
        for(i in 0 until 23) {
            val y=.22f+i*.25f
            for(side in floatArrayOf(-1f,1f)) {
                b.box(side*8.24f,y,-48f,.025f,.024f,8.15f,c(.72f,.72f,.64f))
                for(z in floatArrayOf(-39.76f,-56.24f))
                    b.box(side*4.9f,y,z,3.15f,.024f,.025f,c(.72f,.72f,.64f))
            }
        }
        fun window(x:Float,z:Float,onSide:Boolean) {
            if(onSide) {
                b.box(x,3.1f,z,.04f,1.2f,1.35f,c(.73f,.66f,.46f),24f)
                for(d in floatArrayOf(-1f,1f)) {
                    b.box(x*1.005f,3.1f+d*1.26f,z,.07f,.07f,1.43f,white,26f)
                    b.box(x*1.005f,3.1f,z+d*1.4f,.07f,1.25f,.07f,white,26f)
                }
                b.box(x*1.008f,3.1f,z,.08f,1.2f,.045f,white,26f)
                b.box(x*1.008f,3.1f,z,.08f,.045f,1.35f,white,26f)
            } else {
                b.box(x,3.1f,z,1.2f,1.2f,.04f,c(.73f,.66f,.46f),24f)
                for(d in floatArrayOf(-1f,1f)) {
                    b.box(x+d*1.26f,3.1f,z+.04f,.07f,1.27f,.07f,white,26f)
                    b.box(x,3.1f+d*1.26f,z+.04f,1.27f,.07f,.07f,white,26f)
                }
                b.box(x,3.1f,z+.06f,.045f,1.2f,.08f,white,26f)
                b.box(x,3.1f,z+.06f,1.2f,.045f,.08f,white,26f)
            }
        }
        for(side in floatArrayOf(-1f,1f)) {
            for(z in floatArrayOf(-43.5f,-48f,-52.5f)) window(side*8.26f,z,true)
            window(side*4.8f,-39.73f,false)
            window(side*4.8f,-56.27f,false)
        }
        // Warm household objects: duvet, lamp, framed picture, plates and kitchen sink.
        b.box(-5.9f,1f,-52.1f,1.4f,.07f,2.0f,c(.70f,.59f,.66f),26f)
        b.box(-5.9f,1.14f,-53.6f,1.16f,.09f,.45f,white,26f)
        b.box(6.85f,1.91f,-51.4f,.82f,.05f,3.02f,ivory,25f)
        b.box(6.85f,1.98f,-53f,.54f,.015f,.65f,dark,26f)
        b.box(7.15f,2.2f,-53f,.045f,.22f,.045f,white,26f)
        b.ellipsoid(5.5f,.84f,-43.2f,.38f,.035f,.38f,white,6,14,26f)
        for(x in floatArrayOf(-5.3f,5.3f)) {
            b.box(x,4.8f,-48f,.025f,.55f,.025f,dark,26f)
            b.ellipsoid(x,4.1f,-48f,.45f,.32f,.45f,warm,7,12,24f)
        }
        // The three vending machines are original procedural meshes, each differently lit.
        for(i in 0..2) {
            val x=12.6f+i*3.5f
            val light=when(i){0->c(.56f,.82f,1f);1->c(1f,.87f,.52f);else->c(1f,.61f,.79f)}
            b.box(x,3.22f,30.13f,1.1f,.19f,.045f,light,24f)
            b.box(x-.30f,1.96f,30.13f,.71f,.88f,.045f,dark,26f)
            for(row in 0..3) for(col in 0..3) {
                val bottle=when((row+col+i)%3){0->pink;1->c(.46f,.71f,.67f);else->ivory}
                b.box(x-.86f+col*.37f,1.32f+row*.43f,30.2f,.10f,.13f,.04f,bottle,26f)
                b.box(x-.86f+col*.37f,1.48f+row*.43f,30.2f,.055f,.035f,.045f,white,26f)
            }
            for(row in 0..5) b.box(x+.91f,2.75f-row*.25f,30.19f,.09f,.06f,.035f,light,24f)
            b.box(x,.52f,30.14f,.8f,.17f,.045f,dark,26f)
            for(row in 0..4) b.box(x,.32f+row*.055f,30.19f,.62f,.012f,.03f,white,26f)
            b.box(x,-.005f,31f,1.35f,.015f,1.1f,c(light[0]*.6f,light[1]*.6f,light[2]*.6f),25f)
        }
        b.box(16f,5.45f,29.6f,6.5f,.12f,4.4f,white,26f)
        for(x in floatArrayOf(11f,21f)) b.box(x,5.22f,29.6f,.055f,.035f,3.7f,warm,24f)
        b.box(-20f,4.3f,20f,4.4f,.14f,2.5f,pink,26f)
        b.box(-20f,4.10f,20f,2.7f,.035f,.10f,warm,24f)
        b.box(-23f,2.5f,18.2f,.56f,.85f,.04f,ivory,26f)
        for(i in 0..5) b.box(-23f,3.05f-i*.21f,18.26f,.4f,.025f,.03f,dark,26f)

        // The white fence leads the eye toward the solitary house, matching the reference.
        fun fenceX(x:Float,z0:Float,z1:Float) {
            for(y in floatArrayOf(.62f,1.25f)) b.box(x,y,(z0+z1)*.5f,.065f,.085f,(z1-z0)*.5f,white,26f)
            val n=((z1-z0)/3f).toInt()
            for(i in 0..n) b.box(x,.77f,z0+(z1-z0)*i/n,.11f,.77f,.11f,white,26f)
        }
        for(x in floatArrayOf(-11.8f,11.8f)) fenceX(x,-62f,-28f)
        for(x in floatArrayOf(-59.8f,59.8f)) fenceX(x,-79.8f,67.8f)
        for(z in floatArrayOf(-79.8f,67.8f)) {
            for(side in floatArrayOf(-1f,1f)) {
                b.box(side*31.8f,.65f,z,28f,.08f,.07f,white,26f)
                b.box(side*31.8f,1.25f,z,28f,.08f,.07f,white,26f)
                for(i in 0..18) b.box(side*(4f+i*3f),.77f,z,.11f,.77f,.11f,white,26f)
            }
        }
        for(i in 0 until 6) {
            val z=-26f+i*14f;val x=if(i%2==0)-8f else 8f
            b.box(x*.72f,6.9f,z,abs(x)*.28f,.08f,.08f,dark,26f)
            b.box(x*.46f,6.79f,z,.36f,.045f,.22f,warm,24f)
        }
        // Sparse tufts: deterministic variation, kept off the road and the cottage plot.
        for(ix in -24..24) for(iz in -30..25) {
            val seed=((ix*73856093) xor (iz*19349663)) and 0xffff
            if(seed%4!=0) continue
            val x=ix*2.4f+sin(seed.toFloat())*.8f
            val z=iz*2.7f+cos(seed.toFloat())*.8f
            if(abs(x)<13f || (x>8f && x<25f && z>22f && z<39f) ||
                (x< -14f && x> -26f && z>14f && z<26f)) continue
            val color=c(.35f+(seed%7)*.012f,.46f+(seed%9)*.008f,.29f)
            for(j in 0 until 3) {
                val a=j*2.094f;val dx=cos(a)*.22f;val dz=sin(a)*.22f
                val height=.26f+(seed%11)*.035f
                b.triangle(floatArrayOf(x-dx,.01f,z-dz),floatArrayOf(x+dx,.01f,z+dz),
                    floatArrayOf(x+.16f*sin(seed.toFloat()),height,z+.08f),color)
            }
        }
        for(z in floatArrayOf(-66f,60f)) {
            b.box(0f,5.3f,z,3.55f,.15f,.36f,white,26f)
            b.box(0f,5.05f,z,2.7f,.04f,.1f,warm,24f)
            b.box(0f,2.4f,z,2.7f,2.38f,.045f,
                if(z>0f)c(.41f,.76f,.73f) else c(.72f,.64f,.85f),3f)
        }
        return b.finish()
    }
}
