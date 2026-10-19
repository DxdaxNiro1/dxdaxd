package com.dreamcore.game

import kotlin.math.*

/** Real-triangle sky visibility, sampled at 4 Hz only while inside Dark Fantasy. */
class DreamSkySight(vertices:FloatArray) {
    private val triangles:FloatArray
    init {
        val kept=ArrayList<Float>()
        for(i in vertices.indices step 30) {
            if(max(vertices[i+1],max(vertices[i+11],vertices[i+21]))<.35f)continue
            for(j in intArrayOf(0,10,20))for(k in 0..2)kept.add(vertices[i+j+k])
        }
        triangles=kept.toFloatArray()
    }
    fun clear(x:Float,y:Float,z:Float,dx:Float,dy:Float,dz:Float):Boolean {
        for(i in triangles.indices step 9) {
            val ax=triangles[i];val ay=triangles[i+1];val az=triangles[i+2]
            val ux=triangles[i+3]-ax;val uy=triangles[i+4]-ay;val uz=triangles[i+5]-az
            val vx=triangles[i+6]-ax;val vy=triangles[i+7]-ay;val vz=triangles[i+8]-az
            val px=dy*vz-dz*vy;val py=dz*vx-dx*vz;val pz=dx*vy-dy*vx
            val determinant=ux*px+uy*py+uz*pz
            if(abs(determinant)<.000001f)continue
            val inverse=1f/determinant
            val tx=x-ax;val ty=y-ay;val tz=z-az
            val u=(tx*px+ty*py+tz*pz)*inverse
            if(u<0f || u>1f)continue
            val qx=ty*uz-tz*uy;val qy=tz*ux-tx*uz;val qz=tx*uy-ty*ux
            val v=(dx*qx+dy*qy+dz*qz)*inverse
            if(v<0f || u+v>1f)continue
            val distance=(vx*qx+vy*qy+vz*qz)*inverse
            if(distance>.18f && distance<2000f)return false
        }
        return true
    }
}
