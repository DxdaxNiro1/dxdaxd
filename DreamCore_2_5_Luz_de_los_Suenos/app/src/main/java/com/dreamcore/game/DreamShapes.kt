package com.dreamcore.game

import kotlin.math.*

/** Shared sculptural pieces. Real curved meshes rather than textured rectangular panels. */
object DreamShapes {
    val ivory=floatArrayOf(.76f,.74f,.69f)
    val rose=floatArrayOf(.68f,.42f,.49f)
    val dark=floatArrayOf(.12f,.15f,.18f)
    fun tube(b:DreamMesh,path:List<FloatArray>,radius:Float,c:FloatArray,kind:Float=40f,sides:Int=14) {
        fun section(i:Int,a:Float):FloatArray {
            val p=path[i];val prev=path[max(0,i-1)];val next=path[min(path.lastIndex,i+1)]
            var dx=next[0]-prev[0];var dy=next[1]-prev[1];var dz=next[2]-prev[2]
            val length=sqrt(dx*dx+dy*dy+dz*dz).coerceAtLeast(.001f);dx/=length;dy/=length;dz/=length
            var rx=-dz;var ry=0f;var rz=dx
            var r=sqrt(rx*rx+rz*rz)
            if(r<.01f){rx=1f;rz=0f;r=1f}
            rx/=r;rz/=r
            val ux=dy*rz-dz*ry;val uy=dz*rx-dx*rz;val uz=dx*ry-dy*rx
            return floatArrayOf(p[0]+radius*(rx*cos(a)+ux*sin(a)),p[1]+radius*(ry*cos(a)+uy*sin(a)),p[2]+radius*(rz*cos(a)+uz*sin(a)))
        }
        for(i in 0 until path.lastIndex)for(j in 0 until sides){val a=j*2f*PI.toFloat()/sides;val d=(j+1)*2f*PI.toFloat()/sides;b.quad(section(i,a),section(i+1,a),section(i+1,d),section(i,d),c,kind)}
    }
    fun chair(b:DreamMesh,x:Float,y:Float,z:Float,c:FloatArray=rose) {
        DreamSculpt.chair(b,x,y,z,upholstery=c)
    }
    fun bed(b:DreamMesh,x:Float,y:Float,z:Float) {
        b.box(x,y+.45f,z,2.2f,.45f,3.2f,ivory,43f)
        b.ellipsoid(x,y+1.05f,z,2.3f,.38f,3.3f,ivory,10,24,42f)
        for(s in floatArrayOf(-1f,1f))b.ellipsoid(x+s*1.05f,y+1.52f,z-2.1f,.95f,.26f,.6f,ivory,9,16,42f)
        // Multiple broad folds, not a single flat cover.
        for(i in 0..8)b.ellipsoid(x-1.9f+i*.47f,y+1.35f+.06f*sin(i*2f),z+.45f,.38f,.17f,2.1f,rose,7,16,42f)
        b.box(x,y+1.5f,z-3.1f,2.5f,1.4f,.18f,rose,43f)
    }
    fun plant(b:DreamMesh,x:Float,y:Float,z:Float,scale:Float=1f) {
        b.ellipsoid(x,y+.4f,z,.52f,.45f,.52f,rose,7,12,26f)
        for(i in 0..6){val a=i*2.399f;val dx=cos(a)*scale;val dz=sin(a)*scale
            tube(b,listOf(floatArrayOf(x,y+.4f,z),floatArrayOf(x+dx*.4f,y+1.2f*scale,z+dz*.4f),floatArrayOf(x+dx,y+1.65f*scale,z+dz)),.035f,floatArrayOf(.30f,.38f,.24f),0f,6)
            b.ellipsoid(x+dx,y+1.65f*scale,z+dz,.28f*scale,.12f*scale,.55f*scale,floatArrayOf(.32f,.46f,.32f),5,8,38f)}
    }
    fun cup(b:DreamMesh,x:Float,y:Float,z:Float,kind:Float=26f) {
        b.ellipsoid(x,y+.15f,z,.15f,.19f,.15f,ivory,7,12,kind)
        b.ellipsoid(x,y+.32f,z,.125f,.015f,.125f,floatArrayOf(.20f,.14f,.10f),4,12,kind)
        tube(b,(0..12).map{val a=it*PI.toFloat()/6;floatArrayOf(x+.18f+.12f*cos(a),y+.17f+.12f*sin(a),z)},.025f,ivory,kind,6)
    }
    fun face(b:DreamMesh,x:Float,y:Float,z:Float,scale:Float=1f) {
        // A continuous sculpted relief, with independent UV coordinates and smooth analytic normals.
        b.ellipsoid(x,y,z-2f*scale,12f*scale,18f*scale,9f*scale,ivory,24,36,44f)
        fun depth(u:Float,v:Float):Float {
            fun bump(cx:Float,cy:Float,sx:Float,sy:Float,h:Float)=h*exp(-((u-cx)/sx).pow(2)-((v-cy)/sy).pow(2))
            var d=-2f+11f*sqrt(max(.001f,1f-u*u-v*v))
            d+=bump(0f,.10f,.13f,.38f,2.6f)+bump(0f,-.18f,.18f,.13f,4.3f)
            for(side in floatArrayOf(-1f,1f)){
                d+=bump(side*.35f,.32f,.22f,.12f,-1.6f)
                d+=bump(side*.35f,.48f,.26f,.10f,1.1f)
                d+=bump(side*.48f,-.12f,.25f,.27f,1.5f)
            }
            d+=bump(0f,-.51f,.34f,.045f,.65f)+bump(0f,-.63f,.34f,.055f,.65f)-bump(0f,-.57f,.33f,.045f,1.0f)+bump(0f,-.77f,.30f,.12f,1.1f)
            return d
        }
        data class Point(val p:FloatArray,val uv:FloatArray,val n:FloatArray)
        fun point(i:Int,j:Int):Point {
            val a=j*2f*PI.toFloat()/96;val r=i*.98f/48
            val u=r*cos(a);val v=r*sin(a)
            val dx=(depth(u+.002f,v)-depth(u-.002f,v))/.004f/12f
            val dy=(depth(u,v+.002f)-depth(u,v-.002f))/.004f/18f
            val len=sqrt(dx*dx+dy*dy+1f)
            return Point(floatArrayOf(x+u*12f*scale,y+v*18f*scale,z+depth(u,v)*scale),floatArrayOf(u*.5f+.5f,v*.5f+.5f,.5f),floatArrayOf(-dx/len,-dy/len,1f/len))
        }
        val grid=Array(49){i->Array(97){j->point(i,j)}}
        fun triangle(a:Point,c:Point,d:Point){b.mappedTriangle(a.p,c.p,d.p,a.uv,c.uv,d.uv,a.n,c.n,d.n,49f)}
        for(i in 0 until 48)for(j in 0 until 96){val a=grid[i][j];val c=grid[i+1][j];val d=grid[i+1][j+1];val e=grid[i][j+1]
            triangle(a,c,d);if(i>0)triangle(a,d,e)
        }
        for(side in floatArrayOf(-1f,1f)){
            val originU=side*.35f;val originV=.32f;val start=depth(originU,originV)+.18f
            tube(b,(0..45).map{val t=it/45f;floatArrayOf(x+(side*4.2f+.10f*sin(t*11))*scale,y+(5.76f-t*43f)*scale,z+(start+3f*t)*scale)},.20f*scale,floatArrayOf(.39f,.65f,.70f),50f,12)
        }
    }
    fun presence(role:String):DreamWorlds.Scene {
        if(role!="postman")return DreamPresences.figure(role)
        val b=DreamMesh();val c=floatArrayOf(.16f,.18f,.20f)
        val tall=if(role=="mourner")1.30f else 1f
        b.ellipsoid(0f,2.5f*tall,0f,.69f,1.4f*tall,.36f,c,14,20,32f)
        b.ellipsoid(0f,4.15f*tall,0f,.40f,.62f,.34f,floatArrayOf(.53f,.54f,.50f),14,20,32f)
        for(s in floatArrayOf(-1f,1f)) {
            tube(b,listOf(floatArrayOf(s*.55f,3.2f*tall,0f),floatArrayOf(s*.91f,2.1f*tall,.15f),floatArrayOf(s*.76f,.92f*tall,.34f)),.11f,c,32f,10)
            b.ellipsoid(s*.76f,.82f*tall,.35f,.16f,.32f,.09f,floatArrayOf(.42f,.44f,.42f),9,12,32f)
            for(i in 0..3)b.ellipsoid(s*.76f+(i-1.5f)*.055f,.52f*tall,.36f,.025f,.20f,.03f,c,5,7,32f)
            tube(b,listOf(floatArrayOf(s*.25f,1.6f*tall,0f),floatArrayOf(s*.27f,.8f*tall,-.05f),floatArrayOf(s*.25f,.12f,.15f)),.13f,c,32f,10)
            // Deep sockets, pinprick eyes and extended collar; no round toy-like face.
            if(role!="postman")b.ellipsoid(s*.15f,4.26f*tall,.30f,.10f,.08f,.055f,dark,7,10,32f)
            if(role!="postman")b.ellipsoid(s*.15f,4.25f*tall,.35f,.017f,.015f,.008f,ivory,5,7,32f)
        }
        if(role!="postman")b.ellipsoid(0f,3.98f*tall,.34f,.11f,.045f,.03f,dark,7,10,32f)
        if(role!="postman")b.ellipsoid(0f,4.13f*tall,.35f,.045f,.11f,.08f,c,7,10,32f)
        if(role=="postman"){
            b.box(.73f,1.8f,.22f,.44f,.52f,.25f,floatArrayOf(.26f,.23f,.20f),32f)
            tube(b,listOf(floatArrayOf(-.4f,3.5f,.1f),floatArrayOf(.1f,2.8f,.45f),floatArrayOf(.73f,2.2f,.35f)),.045f,ivory,32f,7)
            b.box(.73f,2.03f,.49f,.31f,.16f,.025f,ivory,32f)
        }
        if(role=="mourner")for(i in 0..11){val a=i*PI.toFloat()/6; tube(b,listOf(floatArrayOf(.5f*cos(a),5.1f,.4f*sin(a)),floatArrayOf(.9f*cos(a),5.8f,.8f*sin(a)),floatArrayOf(1.0f*cos(a),6.5f,1.1f*sin(a))),.045f,c,32f,7)}
        return b.finish()
    }
}
