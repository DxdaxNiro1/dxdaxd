package com.dreamcore.game

import kotlin.math.*

/** An open motel courtyard above the clouds. All accessible floors share y = 0. */
object DreamCloudMap {
    const val ID = "dream_11"
    const val SPAWN_X = 0f
    const val SPAWN_Z = 32f

    private fun c(r:Float,g:Float,b:Float)=floatArrayOf(r,g,b)
    private val plaster=c(.85f,.68f,.77f)
    private val cream=c(.91f,.86f,.81f)
    private val lilac=c(.60f,.54f,.70f)
    private val brass=c(.72f,.61f,.49f)
    private val lamp=c(1f,.79f,.52f)
    private val glass=c(.43f,.53f,.66f)
    private val dark=c(.24f,.26f,.34f)

    private data class Solid(val x:Float,val y:Float,val z:Float,
                             val sx:Float,val sy:Float,val sz:Float,
                             val color:FloatArray,val kind:Float=0f)

    // Shared by mesh and collision. Windows and ceilings above head height do not block.
    private val solids:List<Solid> = buildList {
        fun box(x:Float,y:Float,z:Float,sx:Float,sy:Float,sz:Float,
                color:FloatArray,kind:Float=0f) { add(Solid(x,y,z,sx,sy,sz,color,kind)) }
        for(side in floatArrayOf(-1f,1f)) {
            for(i in 0 until 6) {
                val z=-30f+i*12f
                box(side*34.7f,2.2f,z,.25f,2.2f,5.5f,plaster)
                for(edge in floatArrayOf(-1f,1f)) {
                    box(side*30.5f,2.2f,z+edge*5.5f,4.2f,2.2f,.19f,plaster)
                    // The entry is really open: a 2.6 m break in the front wall.
                    box(side*26.3f,2.2f,z+edge*3.4f,.20f,2.2f,2.1f,cream)
                    box(side*23.5f,1.7f,z+edge*4.7f,.32f,1.7f,.32f,cream,26f)
                }
                // Room desk and a low bed, both have matching physical footprints.
                box(side*33.3f,.68f,z-3f,.72f,.68f,1.35f,lilac,26f)
                box(side*31.7f,.34f,z+3.5f,1.5f,.34f,.92f,cream,26f)
            }
            // Benches sit beside the broad cross-courtyard, outside all doorways.
            for(z in floatArrayOf(-25f,25f)) {
                box(side*13.2f,.56f,z,2.5f,.16f,.68f,lilac,26f)
                box(side*13.2f,1.1f,z-.62f,2.5f,.44f,.12f,lilac,26f)
                for(dx in floatArrayOf(-1.9f,1.9f))
                    box(side*13.2f+dx,.22f,z,.12f,.22f,.5f,brass,26f)
            }
        }
        for(z in floatArrayOf(-34f,34f)) for(side in floatArrayOf(-1f,1f)) {
            box(side*6f,.52f,z,1.02f,.52f,1.02f,cream,26f)
        }
        for(z in floatArrayOf(-40f,40f)) for(x in floatArrayOf(-2.9f,2.9f)) {
            box(x,2.45f,z,.38f,2.45f,.38f,cream,26f)
        }
    }

    private fun overCloudWell(x:Float,z:Float,margin:Float):Boolean =
        abs(x)>8f-margin && abs(x)<20f+margin && abs(z)<16f+margin

    fun canWalk(x:Float,z:Float):Boolean {
        if(x !in -34.3f..34.3f || z !in -43.3f..43.3f) return false
        if(overCloudWell(x,z,.43f)) return false
        for(s in solids) if(s.y-s.sy<2.1f && s.y+s.sy>.18f &&
            abs(x-s.x)<s.sx+.33f && abs(z-s.z)<s.sz+.33f) return false
        return true
    }

    fun portalAt(x:Float,z:Float):String? = when {
        abs(x)<2.3f && abs(z-40f)<1.8f -> DreamWorlds.STATION
        abs(x)<2.3f && abs(z+40f)<1.8f -> "dream_12"
        else -> null
    }

    private fun arch(b:DreamMesh,x:Float,z:Float,side:Float) {
        // Smooth masonry arch, in a vertical plane perpendicular to the room entry.
        for(i in 0 until 16) {
            val a=(PI*i/16).toFloat();val n=(PI*(i+1)/16).toFloat()
            fun p(angle:Float,r:Float,depth:Float)=
                floatArrayOf(x+depth,2.9f+r*sin(angle),z+r*cos(angle))
            for(depth in floatArrayOf(-.22f,.22f)) {
                b.quad(p(a,4.6f,depth),p(n,4.6f,depth),p(n,4.3f,depth),p(a,4.3f,depth),cream,26f)
            }
            b.quad(p(a,4.3f,-.22f),p(n,4.3f,-.22f),p(n,4.3f,.22f),p(a,4.3f,.22f),cream,26f)
            b.quad(p(a,4.6f,.22f),p(n,4.6f,.22f),p(n,4.6f,-.22f),p(a,4.6f,-.22f),plaster,26f)
        }
        // A little brass pendant inside each arcade creates a warm rhythm.
        b.box(x-side*.75f,4.0f,z,.03f,.65f,.03f,brass,26f)
        b.ellipsoid(x-side*.75f,3.28f,z,.25f,.34f,.25f,lamp,5,8,24f)
    }

    fun create():DreamWorlds.Scene {
        val b=DreamMesh()
        // Actual open cloud wells. No opaque cloud spheres or invisible walkable slabs.
        b.box(0f,-.28f,0f,8f,.28f,44f,cream,26f)
        for(side in floatArrayOf(-1f,1f)) {
            b.box(side*27.5f,-.28f,0f,7.5f,.28f,44f,cream,26f)
            b.box(0f,-.28f,side*30f,20f,.28f,14f,cream,26f)
        }
        // Polished ceramic squares, with real spaces between them as fine grout.
        for(ix in -17 until 17) for(iz in -22 until 22) {
            val x=ix*2f;val z=iz*2f
            if(overCloudWell(x+1f,z+1f,0f)) continue
            val tile=if((ix+iz) and 1==0) c(.85f,.77f,.80f) else c(.79f,.73f,.79f)
            b.quad(floatArrayOf(x+.025f,.012f,z+.025f),floatArrayOf(x+.025f,.012f,z+1.975f),
                floatArrayOf(x+1.975f,.012f,z+1.975f),floatArrayOf(x+1.975f,.012f,z+.025f),tile,25f)
        }
        // Borders emphasize the suspended courtyard rather than hiding the drop.
        fun railX(x:Float,z0:Float,z1:Float) {
            b.box(x,1.05f,(z0+z1)*.5f,.10f,.08f,(z1-z0)*.5f,brass,26f)
            b.box(x,.38f,(z0+z1)*.5f,.075f,.045f,(z1-z0)*.5f,cream,26f)
            val steps=((z1-z0)/1.5f).toInt()
            for(i in 0..steps) b.box(x,.54f,z0+(z1-z0)*i/steps,.055f,.54f,.055f,cream,26f)
        }
        fun railZ(z:Float,x0:Float,x1:Float) {
            b.box((x0+x1)*.5f,1.05f,z,(x1-x0)*.5f,.08f,.1f,brass,26f)
            b.box((x0+x1)*.5f,.38f,z,(x1-x0)*.5f,.045f,.075f,cream,26f)
            val steps=((x1-x0)/1.5f).toInt()
            for(i in 0..steps) b.box(x0+(x1-x0)*i/steps,.54f,z,.055f,.54f,.055f,cream,26f)
        }
        for(side in floatArrayOf(-1f,1f)) {
            railX(side*7.95f,-16f,16f);railX(side*20.05f,-16f,16f)
            railZ(-16.05f,min(side*8f,side*20f),max(side*8f,side*20f))
            railZ(16.05f,min(side*8f,side*20f),max(side*8f,side*20f))
            railX(side*34.7f,-43.7f,43.7f)
            // Outer edge end rails, with an honest opening at each portal avenue.
            railZ(side*43.7f,-34.7f,-3.2f);railZ(side*43.7f,3.2f,34.7f)
        }
        solids.forEach{b.box(it.x,it.y,it.z,it.sx,it.sy,it.sz,it.color,it.kind)}

        for(side in floatArrayOf(-1f,1f)) for(i in 0 until 6) {
            val z=-30f+i*12f
            arch(b,side*23.5f,z,side)
            b.box(side*30.5f,4.55f,z,4.5f,.15f,5.7f,cream,26f)
            // Lintel and jambs frame an open doorway rather than painting a false door.
            b.box(side*26.05f,3.25f,z,.24f,1.15f,1.28f,plaster)
            for(d in floatArrayOf(-1f,1f))
                b.box(side*26.04f,1.44f,z+d*1.28f,.25f,1.44f,.075f,brass,26f)
            // Large back window and its curtain rails, visible from within every room.
            b.box(side*34.42f,2.3f,z,.045f,1.15f,2.1f,glass,26f)
            for(d in floatArrayOf(-1f,1f)) {
                b.box(side*34.34f,2.3f,z+d*2.1f,.06f,1.22f,.07f,cream,26f)
                b.box(side*34.33f,2.3f+d*1.15f,z,.06f,.07f,2.1f,cream,26f)
            }
            b.box(side*34.31f,2.3f,z,.07f,1.15f,.045f,cream,26f)
            b.box(side*33.25f,1.42f,z-3f,.43f,.065f,.9f,brass,26f)
            b.ellipsoid(side*33.25f,1.64f,z-3f,.18f,.23f,.18f,lamp,5,8,24f)
            b.box(side*31.7f,.72f,z+3.5f,1.35f,.07f,.82f,c(.79f,.65f,.76f),26f)
            b.box(side*32.65f,.86f,z+3.5f,.35f,.09f,.70f,cream,26f)
            // Vertical wainscoting gives rooms detail without noisy surface textures.
            for(j in 0 until 9) {
                b.box(side*34.41f,.58f,z-4.8f+j*1.2f,.055f,.5f,.025f,brass,26f)
            }
            // Decorative upper stories are beyond jumping range, visibly not reachable.
            for(level in 1..2) {
                val y=level*4.65f
                b.box(side*30.5f,y+2.2f,z,4.3f,2.2f,5.5f,plaster)
                b.box(side*25.15f,y+.04f,z,1.3f,.14f,5.5f,cream,26f)
                b.box(side*23.84f,y+1.1f,z,.06f,.055f,5.5f,brass,26f)
                b.box(side*23.84f,y+.30f,z,.05f,.045f,5.5f,cream,26f)
                for(j in 0..7) b.box(side*23.84f,y+.65f,z-5f+j*(10f/7f),.035f,.47f,.035f,cream,26f)
                for(wz in floatArrayOf(-2.7f,2.7f)) {
                    val wc=if((i+level) % 3==0) lamp else c(.60f,.49f,.67f)
                    b.box(side*26.14f,y+2.25f,z+wz,.05f,1.05f,1.45f,wc,if((i+level)%3==0)24f else 26f)
                    for(edge in floatArrayOf(-1f,1f)) {
                        b.box(side*26.05f,y+2.25f+edge*1.08f,z+wz,.07f,.07f,1.5f,cream,26f)
                        b.box(side*26.05f,y+2.25f,z+wz+edge*1.5f,.07f,1.08f,.06f,cream,26f)
                    }
                    b.box(side*26.01f,y+2.25f,z+wz,.08f,1.05f,.035f,cream,26f)
                }
                b.box(side*25.7f,y+3.72f,z,.5f,.09f,5.6f,lilac,26f)
                b.box(side*25.12f,y+3.6f,z,.075f,.025f,5.2f,lamp,24f)
            }
        }
        // Fragrant garden planters, on solid terrace edges rather than in a cloud void.
        for(z in floatArrayOf(-34f,34f)) for(side in floatArrayOf(-1f,1f)) {
            b.box(side*6f,1.08f,z,.88f,.06f,.88f,c(.27f,.31f,.28f))
            for(j in 0..4) {
                val a=j*1.2566f
                b.ellipsoid(side*6f+.44f*cos(a),1.30f,z+.44f*sin(a),.46f,.38f,.46f,
                    c(.41f,.51f,.43f),6,8)
                b.ellipsoid(side*6f+.44f*cos(a),1.62f,z+.44f*sin(a),.14f,.11f,.14f,
                    c(.89f,.69f,.78f),4,6,26f)
            }
        }
        for(z in floatArrayOf(-40f,40f)) {
            b.box(0f,4.85f,z,3.3f,.22f,.42f,cream,26f)
            b.box(0f,4.54f,z,2.35f,.055f,.30f,lamp,24f)
            val color=if(z>0f)c(.42f,.80f,.79f) else c(.93f,.64f,.77f)
            b.box(0f,2.25f,z,2.25f,2.23f,.04f,color,3f)
            for(dx in floatArrayOf(-2.9f,2.9f)) {
                b.box(dx,2.7f,z+.45f,.20f,.42f,.08f,lamp,24f)
            }
        }
        return b.finish()
    }
}
