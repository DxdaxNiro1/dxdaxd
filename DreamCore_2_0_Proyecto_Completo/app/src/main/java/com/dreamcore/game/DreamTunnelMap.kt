package com.dreamcore.game

import kotlin.math.*

/**
 * 10 / Los baños de las horas violetas.
 *
 * Cinco salas conectadas, sin puertas invisibles: vestíbulo [z=26..54],
 * salón central [x=-18..18,z=-16..16], sala azul [z=-54..-26] y dos
 * galerías laterales [abs(x)=27..47,z=-11..11]. Los enlaces longitudinales
 * tienen 8 m de ancho; los laterales, 7.2 m. El suelo de juego es y=0.
 * Las láminas de agua están a -0.28 y sus vasos a -0.80: son decorativas,
 * someras y transitables. Todas las columnas y la fuente tienen colisión.
 */
object DreamTunnelMap {
    const val ID = "dream_10"
    const val SPAWN_X = 0f
    const val SPAWN_Z = 44f

    private fun rgb(r: Float, g: Float, b: Float) = floatArrayOf(r, g, b)
    private val ivory = rgb(.92f, .91f, .82f)
    private val lavender = rgb(.54f, .36f, .68f)
    private val lilac = rgb(.72f, .49f, .71f)
    private val cyan = rgb(.30f, .75f, .77f)
    private val plum = rgb(.38f, .28f, .53f)
    private val amber = rgb(.91f, .57f, .35f)
    private val water = rgb(.12f, .64f, .71f)
    private val lamp = rgb(1f, .83f, .57f)

    private data class Column(val x: Float, val z: Float, val h: Float)
    private val columns = buildList {
        for (z in floatArrayOf(33f, 46f, -33f, -46f)) {
            add(Column(-11.8f, z, 4.4f)); add(Column(11.8f, z, 4.4f))
        }
        for (x in floatArrayOf(-12.5f, 12.5f)) {
            add(Column(x, -10.5f, 5.5f)); add(Column(x, 10.5f, 5.5f))
        }
        for (x in floatArrayOf(-37f, 37f)) {
            add(Column(x, -7f, 4.1f)); add(Column(x, 7f, 4.1f))
        }
    }

    private fun inside(x: Float, z: Float): Boolean =
        (abs(x) <= 15f && abs(z - 40f) <= 14f) ||
        (abs(x) <= 15f && abs(z + 40f) <= 14f) ||
        (abs(x) <= 18f && abs(z) <= 16f) ||
        (abs(x) <= 4f && abs(z) <= 40f) ||
        (abs(x) <= 37f && abs(z) <= 3.6f) ||
        (abs(abs(x) - 37f) <= 10f && abs(z) <= 11f)

    fun canWalk(x: Float, z: Float): Boolean {
        if (!x.isFinite() || !z.isFinite()) return false
        // Sample the union, not each room separately: door seams stay open.
        val r = .34f
        if (!inside(x, z) || !inside(x-r,z) || !inside(x+r,z) ||
            !inside(x,z-r) || !inside(x,z+r) ||
            !inside(x-r*.7071f,z-r*.7071f) ||
            !inside(x+r*.7071f,z-r*.7071f) ||
            !inside(x-r*.7071f,z+r*.7071f) ||
            !inside(x+r*.7071f,z+r*.7071f)) return false
        if (hypot(x,z) < 2.35f) return false
        return columns.none { hypot(x-it.x,z-it.z) < .98f }
    }

    fun portalAt(x: Float, z: Float): String? = when {
        abs(x) < 1.65f && abs(z - 52f) < 1.15f -> DreamWorlds.STATION
        abs(x) < 1.65f && abs(z + 52f) < 1.15f -> "dream_11"
        else -> null
    }

    private fun pool(x: Float, z: Float): Boolean {
        val xx = abs(x)
        val zz = abs(z)
        if (zz >= 30f && zz <= 49f) return xx >= 4f && xx <= 11f
        if (zz >= 4f && zz <= 13f && xx >= 4f && xx <= 15f) return true
        if (abs(xx-37f) <= 7f && zz >= 2f && zz <= 8f) return true
        return false
    }

    private fun quadUp(b: DreamMesh, x0: Float, z0: Float, x1: Float, z1: Float,
                       y: Float, c: FloatArray, kind: Float) {
        b.quad(floatArrayOf(x0,y,z0),floatArrayOf(x0,y,z1),
            floatArrayOf(x1,y,z1),floatArrayOf(x1,y,z0),c,kind)
    }

    private fun blend(a: FloatArray, c: FloatArray, t: Float): FloatArray =
        FloatArray(3) { a[it] * (1f-t) + c[it]*t }

    private fun floors(b: DreamMesh) {
        // One-metre modules keep exact basin rims, smooth broad light pools,
        // and a modest vertex budget. The shader supplies the fine grout.
        for (xi in -47 until 47) for (zi in -54 until 54) {
            val x = xi+.5f; val z = zi+.5f
            if (!inside(x,z)) continue
            val wet = pool(x,z)
            val checker = if ((xi+zi) and 1 == 0) .02f else 0f
            var shadow = 1f
            for (c in columns) {
                val d = hypot(x-c.x-.9f,z-c.z-1.3f)
                shadow -= .13f*exp(-d*d/6f)
            }
            shadow -= .10f*exp(-hypot(x,z).pow(2)/10f)
            val zone = when {
                z > 26f -> blend(ivory,lilac,.11f)
                z < -26f -> blend(ivory,cyan,.14f)
                x < -26f -> blend(ivory,amber,.14f)
                x > 26f -> blend(ivory,lavender,.12f)
                else -> ivory
            }
            val color = if (wet) rgb(.36f,.70f,.73f) else
                FloatArray(3) { (zone[it]+checker)*shadow.coerceIn(.67f,1f) }
            quadUp(b,xi.toFloat(),zi.toFloat(),xi+1f,zi+1f,
                if (wet) -.80f else -.045f,color,26f)
            if (wet) {
                quadUp(b,xi.toFloat(),zi.toFloat(),xi+1f,zi+1f,-.28f,
                    blend(water,cyan,.10f+.04f*sin(x*.7f+z*.3f)),1f)
                // Vertical tiled pool edges, with a thin ivory coping lip.
                for (d in 0..3) {
                    val dx = if(d==0) -1 else if(d==1) 1 else 0
                    val dz = if(d==2) -1 else if(d==3) 1 else 0
                    if (pool(x+dx,z+dz)) continue
                    if (dx != 0) {
                        val edge = x+dx*.5f
                        b.quad(floatArrayOf(edge,-.80f,z-.5f),
                            floatArrayOf(edge,-.045f,z-.5f),
                            floatArrayOf(edge,-.045f,z+.5f),
                            floatArrayOf(edge,-.80f,z+.5f),cyan,26f)
                        quadUp(b,edge-.075f,z-.5f,edge+.075f,z+.5f,-.005f,ivory,25f)
                    } else {
                        val edge = z+dz*.5f
                        b.quad(floatArrayOf(x-.5f,-.80f,edge),
                            floatArrayOf(x+.5f,-.80f,edge),
                            floatArrayOf(x+.5f,-.045f,edge),
                            floatArrayOf(x-.5f,-.045f,edge),cyan,26f)
                        quadUp(b,x-.5f,edge-.075f,x+.5f,edge+.075f,-.005f,ivory,25f)
                    }
                }
            }
        }
    }

    /** Curved ceramic vault; axisX rotates the local longitudinal z axis. */
    private fun vault(b: DreamMesh, cx: Float, cz: Float, radius: Float,
                      halfLength: Float, spring: Float, rise: Float,
                      base: FloatArray, axisX: Boolean = false,
                      arcs: Int = 48, lengths: Int = 16) {
        fun p(across: Float, y: Float, along: Float): FloatArray =
            if (axisX) floatArrayOf(cx+along,y,cz+across)
            else floatArrayOf(cx+across,y,cz+along)
        for (j in 0 until lengths) {
            val z0 = -halfLength + 2f*halfLength*j/lengths
            val z1 = -halfLength + 2f*halfLength*(j+1)/lengths
            for (i in 0 until arcs) {
                val a0 = PI.toFloat()*i/arcs
                val a1 = PI.toFloat()*(i+1)/arcs
                val xx0 = -radius*cos(a0); val xx1 = -radius*cos(a1)
                val y0 = spring+rise*sin(a0); val y1 = spring+rise*sin(a1)
                val stripe = (i in arcs/5..arcs/5+2) ||
                    (i in arcs*4/5-3..arcs*4/5-1)
                val central = abs(i-arcs/2) < 2
                val c = when {
                    stripe -> blend(cyan,ivory,.16f)
                    central -> blend(base,ivory,.24f)
                    else -> blend(base,ivory, .06f + .025f*sin(j*1.7f+i*.62f))
                }
                // Inward-facing normals receive the interior lighting.
                if (axisX) b.quad(p(xx0,y0,z1),p(xx1,y1,z1),p(xx1,y1,z0),p(xx0,y0,z0),c,26f)
                else b.quad(p(xx1,y1,z0),p(xx1,y1,z1),p(xx0,y0,z1),p(xx0,y0,z0),c,26f)
            }
        }
    }

    // Ceramic end wall, optionally pierced by a real arched doorway.
    private fun endWall(b: DreamMesh, cx: Float, cz: Float, half: Float,
                        spring: Float, rise: Float, color: FloatArray,
                        opening: Float = 0f, doorSpring: Float = 2.2f,
                        axisX: Boolean = false, inward: Float = 1f) {
        val steps = (half*4).roundToInt()
        fun p(across: Float, y: Float): FloatArray =
            if (axisX) floatArrayOf(cx,y,cz+across) else floatArrayOf(cx+across,y,cz)
        fun roof(x: Float) = spring + rise*sqrt((1f-(x/half).pow(2)).coerceAtLeast(0f))
        fun bottom(x: Float): Float = if(opening>0f && abs(x)<opening)
            doorSpring + opening*sqrt((1f-(x/opening).pow(2)).coerceAtLeast(0f)) else -.045f
        for (i in 0 until steps) {
            val x0 = -half+2*half*i/steps
            val x1 = -half+2*half*(i+1)/steps
            // Extra vertices precisely at doorway sides avoid a diagonal
            // triangle jutting into the traversable part of the aperture.
            val cuts = listOf(x0,x1,-opening,opening).filter { it>=x0 && it<=x1 }.distinct().sorted()
            for (k in 0 until cuts.size-1) {
                val a=cuts[k]; val d=cuts[k+1]
                // Float equality distinguishes -0f from 0f. A closed wall
                // inserts both at its centre; never emit a zero-width strip.
                if (d-a < .0001f) continue
                val mid=(a+d)*.5f
                val ba=if(abs(mid)<opening) bottom(a.coerceIn(-opening+.0001f,opening-.0001f)) else -.045f
                val bd=if(abs(mid)<opening) bottom(d.coerceIn(-opening+.0001f,opening-.0001f)) else -.045f
                val q0=p(a,ba); val q1=p(d,bd); val q2=p(d,roof(d)); val q3=p(a,roof(a))
                if (inward>0f) b.quad(q0,q1,q2,q3,color,26f) else b.quad(q3,q2,q1,q0,color,26f)
            }
        }
    }

    private fun cylinder(b: DreamMesh,x:Float,y:Float,z:Float,r:Float,h:Float,
                         color:FloatArray,kind:Float=26f,segments:Int=24) {
        for (i in 0 until segments) {
            val a=2f*PI.toFloat()*i/segments
            val d=2f*PI.toFloat()*(i+1)/segments
            val p0=floatArrayOf(x+r*cos(a),y,z+r*sin(a))
            val p1=floatArrayOf(x+r*cos(d),y,z+r*sin(d))
            val p2=floatArrayOf(p1[0],y+h,p1[2])
            val p3=floatArrayOf(p0[0],y+h,p0[2])
            b.quad(p0,p3,p2,p1,color,kind)
            b.triangle(floatArrayOf(x,y+h,z),p2,p3,color,kind)
        }
    }

    /** A portal is close to its back wall, with an arch and an unobstructed approach. */
    private fun gate(b: DreamMesh,z:Float,back:Boolean) {
        val frame = if(back) amber else cyan
        for (x in floatArrayOf(-1.75f,1.75f)) {
            b.box(x,1.5f,z,.16f,1.5f,.18f,ivory,25f)
            b.box(x,1.5f,z+.02f,.055f,1.36f,.20f,frame,24f)
        }
        val steps=36
        for (i in 0 until steps) {
            val a=PI.toFloat()*i/steps
            val d=PI.toFloat()*(i+1)/steps
            b.quad(floatArrayOf(1.6f*cos(a),3f+1.6f*sin(a),z),
                floatArrayOf(1.94f*cos(a),3f+1.94f*sin(a),z),
                floatArrayOf(1.94f*cos(d),3f+1.94f*sin(d),z),
                floatArrayOf(1.6f*cos(d),3f+1.6f*sin(d),z),ivory,25f)
            b.triangle(floatArrayOf(0f,3f,z-.02f),
                floatArrayOf(1.59f*cos(a),3f+1.59f*sin(a),z-.02f),
                floatArrayOf(1.59f*cos(d),3f+1.59f*sin(d),z-.02f),frame,3f)
        }
        b.box(0f,1.48f,z-.015f,1.59f,1.5f,.04f,frame,3f)
        b.box(0f,.02f,z+.5f,2.2f,.065f,1.1f,ivory,25f)
        // Two reflective stepping markers lead directly to each return gate.
        for(i in 1..3) b.box(0f,.005f,z+(if(back) -1 else 1)*i*1.1f,
            .85f,.025f,.13f,frame,24f)
    }

    private fun roomShell(b:DreamMesh,z:Float,halfX:Float,halfZ:Float,
                          spring:Float,rise:Float,color:FloatArray,
                          lowerOpen:Boolean,upperOpen:Boolean) {
        vault(b,0f,z,halfX,halfZ,spring,rise,color,lengths=if(halfZ>15f) 20 else 16)
        endWall(b,0f,z-halfZ,halfX,spring,rise,color,if(lowerOpen)4f else 0f,inward=1f)
        endWall(b,0f,z+halfZ,halfX,spring,rise,color,if(upperOpen)4f else 0f,inward=-1f)
        for (sign in floatArrayOf(-1f,1f)) {
            val x=sign*halfX
            if (z == 0f) {
                // Real side arch for the two lateral galleries.
                endWall(b,x,0f,halfZ,5.5f,0f,color,3.6f,1.5f,axisX=true,inward=sign)
            } else b.box(x,spring*.5f,z,.16f,spring*.5f,halfZ,color,26f)
            if(z==0f) {
                for(zz in floatArrayOf(-9.8f,9.8f))
                    b.box(x-sign*.05f,.18f,zz,.18f,.18f,6.2f,ivory,25f)
            } else b.box(x-sign*.05f,.18f,z,.18f,.18f,halfZ,ivory,25f)
            // Dado height gives a crisp band above the rippling water.
            if(z != 0f) b.box(x-sign*.17f,1.12f,z,.035f,.075f,halfZ,cyan,26f)
        }
    }

    fun create(): DreamWorlds.Scene {
        val b=DreamMesh()
        floors(b)
        roomShell(b,40f,15f,14f,4.4f,6.8f,lilac,true,false)
        roomShell(b,0f,18f,16f,5.5f,8.8f,lavender,true,true)
        roomShell(b,-40f,15f,14f,4.4f,6.8f,plum,false,true)

        for (z in floatArrayOf(-21f,21f)) {
            vault(b,0f,z,4f,5f,2.2f,4f,if(z>0)lilac else plum,arcs=40,lengths=8)
            for (x in floatArrayOf(-4f,4f)) {
                b.box(x,1.1f,z,.14f,1.1f,5f,cyan,26f)
                b.box(x,.15f,z,.2f,.15f,5f,ivory,25f)
            }
            // Repeated circular thresholds are actual annuli, not closed discs.
            for (dz in floatArrayOf(-4.5f,0f,4.5f)) {
                for(i in 0 until 40) {
                    val a=PI.toFloat()*i/40f; val d=PI.toFloat()*(i+1)/40f
                    b.quad(floatArrayOf(-4f*cos(a),2.2f+4f*sin(a),z+dz),
                        floatArrayOf(-3.88f*cos(a),2.2f+3.88f*sin(a),z+dz),
                        floatArrayOf(-3.88f*cos(d),2.2f+3.88f*sin(d),z+dz),
                        floatArrayOf(-4f*cos(d),2.2f+4f*sin(d),z+dz),ivory,25f)
                }
                b.ellipsoid(0f,5.98f,z+dz,.20f,.12f,.20f,lamp,5,12,24f)
            }
        }

        for (sign in floatArrayOf(-1f,1f)) {
            val c=if(sign<0f)amber else cyan
            vault(b,sign*22.5f,0f,3.6f,4.5f,1.5f,3.6f,c,true,40,7)
            for (z in floatArrayOf(-3.6f,3.6f))
                b.box(sign*22.5f,.75f,z,4.5f,.75f,.14f,c,26f)
            vault(b,sign*37f,0f,11f,10f,4.1f,6.5f,c,true,48,14)
            endWall(b,sign*27f,0f,11f,4.1f,6.5f,c,3.6f,1.5f,true,inward=-sign)
            endWall(b,sign*47f,0f,11f,4.1f,6.5f,c,axisX=true,inward=sign)
            for (z in floatArrayOf(-11f,11f)) {
                b.box(sign*37f,2.05f,z,10f,2.05f,.16f,c,26f)
                b.box(sign*37f,1.12f,z-sign*.05f,10f,.10f,.18f,ivory,25f)
            }
            for (x in floatArrayOf(30f,37f,44f)) {
                b.ellipsoid(sign*x,9.9f,0f,.28f,.14f,.28f,lamp,5,12,24f)
                // Tall, warm recesses at both ends of the color galleries.
                for(z in floatArrayOf(-10.76f,10.76f)) {
                    b.box(sign*x,2.05f,z,.7f,1.65f,.05f,ivory,25f)
                    b.box(sign*x,2.1f,z-sign*.015f,.52f,1.35f,.065f,
                        if(sign<0)rgb(1f,.73f,.45f) else rgb(.42f,.89f,.90f),24f)
                }
            }
        }

        for ((i,c) in columns.withIndex()) {
            val tone=if(i<8) amber else if(i<12) ivory else lavender
            cylinder(b,c.x,-.04f,c.z,.68f,.21f,ivory,25f)
            cylinder(b,c.x,.17f,c.z,.55f,c.h-.35f,tone)
            cylinder(b,c.x,c.h-.18f,c.z,.72f,.21f,ivory,25f)
            cylinder(b,c.x,1.18f,c.z,.568f,.12f,cyan)
            // A sconce faces the open centre instead of hiding inside a pillar.
            val sx=c.x-sign(c.x)*.54f
            b.ellipsoid(sx,2.8f,c.z,.12f,.28f,.19f,lamp,5,10,24f)
        }

        // The low fountain leaves every corridor connected around its rim.
        cylinder(b,0f,-.03f,0f,2.02f,.30f,ivory,25f,48)
        cylinder(b,0f,.27f,0f,1.85f,.14f,cyan,26f,48)
        quadUp(b,-1.35f,-1.35f,1.35f,1.35f,.415f,water,1f)
        cylinder(b,0f,.42f,0f,.40f,1.25f,ivory,25f)
        cylinder(b,0f,1.54f,0f,1.12f,.15f,lilac,26f,40)
        b.ellipsoid(0f,1.82f,0f,.48f,.46f,.48f,lamp,8,20,24f)

        // Pendant domes make the large ceiling readable from eye level.
        for(z in floatArrayOf(-46f,-35f,35f,46f)) {
            b.ellipsoid(0f,10.6f,z,.38f,.22f,.38f,lamp,6,16,24f)
            b.box(0f,10.98f,z,.035f,.26f,.035f,ivory,25f)
        }
        for(z in floatArrayOf(-10f,0f,10f)) {
            b.ellipsoid(0f,13.65f,z,.50f,.24f,.50f,lamp,6,16,24f)
            b.box(0f,14.01f,z,.045f,.22f,.045f,ivory,25f)
        }
        gate(b,52f,true)
        gate(b,-52f,false)
        return b.finish()
    }
}
