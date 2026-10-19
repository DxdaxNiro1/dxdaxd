package com.dreamcore.game

import kotlin.math.*

/** A walkable chess dream: original procedural models, no downloaded resources. */
object DreamChessMap {
    const val ID = "dream_09"
    const val SPAWN_X = 0f
    const val SPAWN_Z = 52f

    private data class Piece(val x:Float,val z:Float,val type:Int,val ivory:Boolean) {
        val radius:Float get()=if(type==0) 2.55f else 3.1f
    }
    // Types: pawn, rook, knight, bishop, queen, king. Keep the centre avenue open.
    private val pieces:List<Piece> = buildList {
        val back=intArrayOf(1,2,3,4,5,3,2,1)
        for(side in 0..1) for(i in 0..7) {
            val x=-56f+i*16f
            val sign=if(side==0) 1f else -1f
            add(Piece(x,40f*sign,back[i],side==0))
            add(Piece(x,24f*sign,0,side==0))
        }
    }

    fun canWalk(x:Float,z:Float):Boolean {
        if(x !in -69.4f..69.4f || z !in -69.4f..65.4f) return false
        for(p in pieces) if(hypot(x-p.x,z-p.z)<p.radius+.30f) return false
        // Colonnade columns, square garden beds, and gateway piers are physical.
        for(side in floatArrayOf(-1f,1f)) {
            for(cz in floatArrayOf(-48f,-16f,16f,48f)) {
                if(hypot(x-side*67f,z-cz)<1.12f) return false
            }
            for(cz in floatArrayOf(-32f,0f,32f)) {
                if(abs(x-side*66.5f)<2.22f && abs(z-cz)<3.42f) return false
            }
        }
        for(gz in floatArrayOf(-60f,60f)) for(gx in floatArrayOf(-3.4f,3.4f)) {
            if(abs(x-gx)<.86f && abs(z-gz)<.86f) return false
        }
        return true
    }

    fun portalAt(x:Float,z:Float):String? = when {
        abs(x)<2.35f && abs(z-60f)<2.4f -> DreamWorlds.STATION
        abs(x)<2.35f && abs(z+60f)<2.4f -> "dream_10"
        else -> null
    }

    private fun c(r:Float,g:Float,b:Float)=floatArrayOf(r,g,b)
    private val ivory=c(.91f,.88f,.81f)
    private val midnight=c(.20f,.24f,.32f)
    private val gold=c(.84f,.70f,.46f)
    private val rose=c(.80f,.59f,.69f)
    private val light=c(1f,.87f,.66f)
    private val whiteTile=c(.81f,.84f,.81f)
    private val darkTile=c(.27f,.32f,.38f)

    private fun tileColor(x:Float,z:Float):FloatArray {
        val ix=floor((x+64f)/16f).toInt()
        val iz=floor((z+64f)/16f).toInt()
        return if((ix+iz) and 1 == 0) whiteTile else darkTile
    }

    /** Smooth analytic normals give the turned ceramic silhouettes a soft sheen. */
    private class TurnedMesh {
        private var data=FloatArray(65536)
        private var used=0
        private fun add(v:Float) {
            if(used==data.size) data=data.copyOf(data.size*2)
            data[used++]=v
        }
        fun lathe(x:Float,y:Float,z:Float,profile:Array<FloatArray>,color:FloatArray,
                  scale:Float=1f,sectors:Int=20,kind:Float=26f) {
            fun vertex(level:Int,angle:Float) {
                val p=profile[level]
                val prev=profile[(level-1).coerceAtLeast(0)]
                val next=profile[(level+1).coerceAtMost(profile.lastIndex)]
                val dy=next[0]-prev[0]
                val dr=next[1]-prev[1]
                val length=hypot(dy,dr).coerceAtLeast(.0001f)
                val co=cos(angle);val si=sin(angle)
                add(x+p[1]*scale*co);add(y+p[0]*scale);add(z+p[1]*scale*si)
                color.forEach{add(it)}
                add(dy/length*co);add(-dr/length);add(dy/length*si);add(kind)
            }
            for(j in 0 until profile.lastIndex) for(i in 0 until sectors) {
                val a=(2.0*PI*i/sectors).toFloat()
                val n=(2.0*PI*(i+1)/sectors).toFloat()
                if(profile[j+1][1]>.0001f) {
                    vertex(j,a);vertex(j+1,a);vertex(j+1,n)
                }
                if(profile[j][1]>.0001f) {
                    vertex(j,a);vertex(j+1,n);vertex(j,n)
                }
            }
        }
        fun softShadow(x:Float,z:Float,sx:Float,sz:Float,base:FloatArray) {
            fun vertex(r:Float,a:Float) {
                val smooth=r*r*(3f-2f*r)
                val shade=.57f+.43f*smooth
                add(x+sx*r*cos(a));add(.045f);add(z+sz*r*sin(a))
                base.forEach{add(it*shade)}
                add(0f);add(1f);add(0f);add(25f)
            }
            val rings=4;val sectors=16
            for(j in 0 until rings) for(i in 0 until sectors) {
                val r0=j/rings.toFloat();val r1=(j+1f)/rings
                val a=(2.0*PI*i/sectors).toFloat();val n=(2.0*PI*(i+1)/sectors).toFloat()
                if(j>0) {vertex(r0,a);vertex(r0,n);vertex(r1,n)}
                vertex(r0,a);vertex(r1,n);vertex(r1,a)
            }
        }
        fun finish():FloatArray=data.copyOf(used)
    }

    private fun profile(vararg yRadius:Float):Array<FloatArray> =
        Array(yRadius.size/2){floatArrayOf(yRadius[it*2],yRadius[it*2+1])}

    private fun baseProfile(radius:Float,height:Float):Array<FloatArray> = profile(
        0f,0f, 0f,radius*.83f, .12f,radius, .48f,radius,
        .83f,radius*.77f,
        1.32f,radius*.57f, 1.52f,radius*.54f, height,radius*.34f
    )

    /** Extrude a concave horse silhouette; ear clipping keeps muzzle and ears crisp. */
    private fun knight(b:DreamMesh,x:Float,y:Float,z:Float,color:FloatArray,ivorySide:Boolean) {
        val poly=arrayOf(
            floatArrayOf(-1.08f,0f),floatArrayOf(1.06f,0f),floatArrayOf(.83f,1.6f),
            floatArrayOf(1.24f,2.55f),floatArrayOf(2.08f,2.99f),floatArrayOf(2.13f,3.58f),
            floatArrayOf(1.65f,3.91f),floatArrayOf(.98f,4.31f),floatArrayOf(.52f,4.98f),
            floatArrayOf(.26f,5.93f),floatArrayOf(-.17f,5.56f),floatArrayOf(-.65f,5.98f),
            floatArrayOf(-.87f,5.08f),floatArrayOf(-1.23f,4.45f),
            floatArrayOf(-1.54f,3.05f),floatArrayOf(-1.61f,1.53f)
        )
        val sc=1.23f
        fun v(i:Int,depth:Float)=floatArrayOf(x+poly[i][0]*sc,y+poly[i][1]*sc,z+depth)
        fun cross(a:FloatArray,bb:FloatArray,cc:FloatArray):Float =
            (bb[0]-a[0])*(cc[1]-a[1])-(bb[1]-a[1])*(cc[0]-a[0])
        fun contains(p:FloatArray,a:FloatArray,bb:FloatArray,cc:FloatArray):Boolean =
            cross(a,bb,p)>=-.00001f && cross(bb,cc,p)>=-.00001f && cross(cc,a,p)>=-.00001f
        val remaining=poly.indices.toMutableList()
        while(remaining.size>3) {
            var found=false
            for(k in remaining.indices) {
                val ai=remaining[(k+remaining.size-1)%remaining.size]
                val bi=remaining[k];val ci=remaining[(k+1)%remaining.size]
                if(cross(poly[ai],poly[bi],poly[ci])<=.00001f) continue
                if(remaining.any{it!=ai && it!=bi && it!=ci && contains(poly[it],poly[ai],poly[bi],poly[ci])}) continue
                b.triangle(v(ai,.88f),v(bi,.88f),v(ci,.88f),color,26f)
                b.triangle(v(ci,-.88f),v(bi,-.88f),v(ai,-.88f),color,26f)
                remaining.removeAt(k);found=true;break
            }
            check(found){"Invalid knight silhouette"}
        }
        b.triangle(v(remaining[0],.88f),v(remaining[1],.88f),v(remaining[2],.88f),color,26f)
        b.triangle(v(remaining[2],-.88f),v(remaining[1],-.88f),v(remaining[0],-.88f),color,26f)
        for(i in poly.indices) {
            val j=(i+1)%poly.size
            b.quad(v(i,-.88f),v(j,-.88f),v(j,.88f),v(i,.88f),color,26f)
        }
        val eye=if(ivorySide) midnight else light
        for(d in floatArrayOf(-.91f,.91f)) {
            b.ellipsoid(x+.72f,y+4.65f*sc,z+d,.16f,.16f,.055f,eye,5,8,26f)
        }
        // The separate mane ridge makes its profile legible from the opposite side.
        for(i in 0 until 5) {
            b.box(x-1.65f+i*.10f,y+1.5f+i*.75f,z,.18f,.34f,.98f,gold,26f)
        }
    }

    fun create():DreamWorlds.Scene {
        val b=DreamMesh()
        val turned=TurnedMesh()
        // The chess pattern continues into the mist beyond the playable balustrade.
        // A board square is 16m wide; pieces tower over a human first-person camera.
        b.box(0f,-.46f,0f,257f,.44f,257f,c(.17f,.22f,.28f),25f)
        for(ix in -12 until 12) for(iz in -12 until 12) {
            val x=ix*16f;val z=iz*16f
            val color=tileColor(x+8f,z+8f)
            b.quad(floatArrayOf(x+.025f,0f,z+.025f),floatArrayOf(x+.025f,0f,z+15.975f),
                floatArrayOf(x+15.975f,0f,z+15.975f),floatArrayOf(x+15.975f,0f,z+.025f),color,25f)
        }
        // Broad inset brass/chalk frame, flush to the walking surface.
        for(sign in floatArrayOf(-1f,1f)) {
            b.box(sign*64.25f,.018f,0f,.18f,.018f,64.4f,gold,26f)
            b.box(0f,.018f,sign*64.25f,64.4f,.018f,.18f,gold,26f)
            b.box(sign*70f,.58f,-2f,.28f,.58f,68f,ivory,26f)
            b.box(sign*70f,1.23f,-2f,.39f,.07f,68f,gold,26f)
        }
        b.box(0f,.58f,-70f,70f,.58f,.28f,ivory,26f)
        b.box(-37f,.58f,66f,33f,.58f,.28f,ivory,26f)
        b.box(37f,.58f,66f,33f,.58f,.28f,ivory,26f)

        fun shadow(p:Piece) {
            val size=if(p.type==0) .78f else 1f
            turned.softShadow(p.x+1.05f,p.z+1.1f,4.6f*size,5.5f*size,tileColor(p.x,p.z))
        }

        fun buildPiece(p:Piece) {
            val color=if(p.ivory) ivory else midnight
            val height=when(p.type){0->4.7f;1->6.9f;2->3.1f;3->8.4f;4->10.5f;else->11.6f}
            val radius=if(p.type==0) 2.5f else 3f
            turned.lathe(p.x,.07f,p.z,baseProfile(radius,height),color)
            // Two fine gold collars read as crafted ceramic even at ground level.
            turned.lathe(p.x,.07f,p.z,profile(.65f,radius*.92f,.75f,radius*.87f,
                .82f,radius*.77f),gold)
            when(p.type) {
                0 -> {
                    turned.lathe(p.x,.07f,p.z,profile(4.6f,.9f,4.85f,1.45f,5.13f,1.45f,
                        5.35f,.92f),color)
                    b.ellipsoid(p.x,6.25f,p.z,1.56f,1.61f,1.56f,color,7,12,26f)
                }
                1 -> {
                    turned.lathe(p.x,.07f,p.z,profile(6.7f,1.02f,7.1f,2.35f,7.55f,2.6f,
                        8.3f,2.6f,8.42f,2.45f,8.42f,0f),color)
                    for(i in 0 until 8) {
                        val a=(2.0*PI*i/8).toFloat()
                        b.box(p.x+2.07f*cos(a),9.0f,p.z+2.07f*sin(a),.52f,.68f,.52f,color,26f)
                    }
                }
                2 -> knight(b,p.x,3.17f,p.z,color,p.ivory)
                3 -> {
                    turned.lathe(p.x,.07f,p.z,profile(8.15f,1.02f,8.5f,1.85f,8.9f,1.85f,
                        9.2f,1.08f,9.7f,1.5f,10.6f,1.85f,11.6f,1.2f,12.8f,.14f,12.9f,0f),color)
                    // A diagonal dark incision marks the traditional bishop mitre.
                    b.quad(floatArrayOf(p.x-.77f,11.91f,p.z+1.39f),
                        floatArrayOf(p.x-.57f,12.04f,p.z+1.39f),
                        floatArrayOf(p.x+.90f,10.12f,p.z+1.65f),
                        floatArrayOf(p.x+.68f,9.98f,p.z+1.65f),midnight)
                    b.ellipsoid(p.x,13.1f,p.z,.26f,.29f,.26f,gold,6,8,26f)
                }
                4 -> {
                    turned.lathe(p.x,.07f,p.z,profile(10.3f,1.02f,10.6f,2.1f,11f,2.2f,
                        11.3f,1.7f,12.7f,2.05f,12.95f,1.8f,12.95f,0f),color)
                    for(i in 0 until 9) {
                        val a=(2.0*PI*i/9).toFloat()
                        b.ellipsoid(p.x+2.04f*cos(a),13.33f,p.z+2.04f*sin(a),
                            .34f,.49f,.34f,gold,5,8,26f)
                    }
                    b.ellipsoid(p.x,13.25f,p.z,.81f,.87f,.81f,color,8,12,26f)
                }
                5 -> {
                    turned.lathe(p.x,.07f,p.z,profile(11.4f,1.02f,11.7f,2.15f,12.13f,2.2f,
                        12.4f,1.8f,12.8f,1.8f,13.02f,1.2f,13.3f,1.2f,13.3f,0f),color)
                    b.box(p.x,14.8f,p.z,.38f,1.65f,.38f,gold,26f)
                    b.box(p.x,15.15f,p.z,1.48f,.35f,.38f,gold,26f)
                }
            }
        }
        pieces.forEach{shadow(it);buildPiece(it)}

        fun arch(cx:Float,cz:Float,rx:Float,base:Float,rise:Float,depth:Float,color:FloatArray) {
            val count=24
            for(i in 0 until count) {
                val a=(PI*i/count).toFloat();val n=(PI*(i+1)/count).toFloat()
                fun q(angle:Float,extra:Float,z:Float)=floatArrayOf(
                    cx+(rx+extra)*cos(angle),base+(rise+extra)*sin(angle),z)
                val front=cz+depth;val back=cz-depth
                b.quad(q(a,0f,front),q(a,.48f,front),q(n,.48f,front),q(n,0f,front),color,26f)
                b.quad(q(n,0f,back),q(n,.48f,back),q(a,.48f,back),q(a,0f,back),color,26f)
                b.quad(q(a,0f,back),q(a,0f,front),q(n,0f,front),q(n,0f,back),color,26f)
                b.quad(q(n,.48f,back),q(n,.48f,front),q(a,.48f,front),q(a,.48f,back),color,26f)
            }
        }
        fun gateway(z:Float,returnHome:Boolean) {
            val tone=if(returnHome) c(.42f,.74f,.77f) else c(.69f,.57f,.85f)
            for(x in floatArrayOf(-3.4f,3.4f)) {
                b.box(x,3.05f,z,.52f,3.05f,.62f,ivory,26f)
                b.box(x,.31f,z,.74f,.31f,.81f,gold,26f)
                b.box(x,6.18f,z,.73f,.23f,.80f,gold,26f)
                b.ellipsoid(x,6.72f,z,.38f,.40f,.38f,light,6,8,24f)
            }
            arch(0f,z,3.4f,6.35f,3.1f,.62f,ivory)
            b.box(0f,2.8f,z,2.53f,2.8f,.07f,tone,3f)
            b.box(0f,.035f,z,2.65f,.025f,2.5f,ivory,25f)
            b.box(0f,10.14f,z,1.05f,.18f,.30f,gold,26f)
            b.ellipsoid(0f,10.75f,z,.43f,.50f,.43f,light,8,12,24f)
        }
        gateway(60f,true);gateway(-60f,false)

        // Lavender colonnades and small impossible gardens flank the chessboard.
        for(side in floatArrayOf(-1f,1f)) {
            val x=side*67f
            for(z in floatArrayOf(-48f,-16f,16f,48f)) {
                turned.lathe(x,.03f,z,profile(0f,0f,0f,1.02f,.38f,1.02f,.55f,.74f,
                    16f,.66f,16.3f,1.05f,16.65f,1.05f,16.65f,0f),ivory,sectors=20)
                b.ellipsoid(x,17.2f,z,.47f,.55f,.47f,light,6,8,24f)
            }
            b.box(x,17f,0f,1.2f,.25f,49.1f,rose,26f)
            b.box(x,17.46f,0f,1.4f,.18f,49.4f,ivory,26f)
            for(z in floatArrayOf(-32f,0f,32f)) {
                b.box(side*66.5f,.53f,z,1.9f,.53f,3.1f,rose,26f)
                b.box(side*66.5f,1.1f,z,2.02f,.08f,3.22f,ivory,26f)
                b.box(side*66.5f,1.19f,z,1.78f,.025f,2.98f,c(.38f,.55f,.49f))
                for(k in 0 until 3) {
                    val tz=z+(k-1)*1.8f
                    b.ellipsoid(side*66.5f,2.0f,tz,.91f,.95f,.91f,c(.56f,.70f,.61f),5,8,12f)
                    b.ellipsoid(side*66.5f,2.65f,tz,.23f,.28f,.23f,light,4,6,24f)
                }
            }
        }
        // Brass arrow-like inlays show a route through the armies without HUD clutter.
        for(i in 0 until 13) {
            val z=48f-i*8f
            b.box(0f,.025f,z,.13f,.012f,1f,gold,26f)
        }
        // Far floating pawns and lavender clouds suggest a board beyond gravity.
        for(i in 0 until 6) {
            val sign=if(i%2==0) -1f else 1f
            val x=sign*(100f+(i%3)*22f);val z=-100f+(i/2)*75f
            val y=18f+(i%3)*8f
            turned.lathe(x,y,z,profile(0f,0f,0f,3.8f,.6f,4.2f,1.1f,3.2f,
                2f,2f,7.7f,1f,8.3f,2.4f,8.8f,2.4f,9f,0f),
                if(i%2==0) ivory else c(.50f,.45f,.64f),sectors=16)
            b.ellipsoid(x,y+10.7f,z,2.6f,2.6f,2.6f,ivory,7,10,26f)
            b.ellipsoid(x,52f+(i%2)*14f,z,29f,7f,22f,c(.91f,.84f,.93f),6,10,20f)
        }
        val detail=b.finish().vertices
        val smooth=turned.finish()
        return DreamWorlds.Scene(detail+smooth)
    }
}
