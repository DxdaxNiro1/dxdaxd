package com.dreamcore.game

import kotlin.math.*

/** Lathed ceramic chess pieces with actual collars and differentiated crowns. */
object DreamRenewal {
    fun chess(b:DreamMesh,x:Float,z:Float,role:Int,c:FloatArray){
        val profile=listOf(0f to 5.7f,.7f to 6f,1.3f to 5.8f,1.8f to 4.3f,2.2f to 4.2f,2.6f to 3.2f,3.1f to 3f,5f to 2.35f,9f to 1.75f,11f to 1.9f,11.8f to 2.8f,12.3f to 3.7f,13f to 3.7f,13.5f to 2.8f)
        val sides=32
        for(j in 0 until profile.lastIndex)for(i in 0 until sides){val a=i*2f*PI.toFloat()/sides;val d=(i+1)*2f*PI.toFloat()/sides
            fun p(k:Int,t:Float)=floatArrayOf(x+cos(t)*profile[k].second,profile[k].first,z+sin(t)*profile[k].second)
            fun n(k:Int,t:Float):FloatArray{val lo=(k-1).coerceAtLeast(0);val hi=(k+1).coerceAtMost(profile.lastIndex);val dy=profile[hi].first-profile[lo].first;val dr=profile[hi].second-profile[lo].second;val len=sqrt(dy*dy+dr*dr);return floatArrayOf(cos(t)*dy/len,-dr/len,sin(t)*dy/len)}
            b.mappedTriangle(p(j,a),p(j,d),p(j+1,d),c,c,c,n(j,a),n(j,d),n(j+1,d),25f)
            b.mappedTriangle(p(j,a),p(j+1,d),p(j+1,a),c,c,c,n(j,a),n(j+1,d),n(j+1,a),25f)
        }
        when(role){
            0->b.ellipsoid(x,15.6f,z,2.7f,2.7f,2.7f,c,14,24,25f)
            1->{DreamSculpt.cylinder(b,x,13f,z,3.4f,4f,c,25f,32);for(i in 0..5){val a=i*PI.toFloat()/3;DreamSculpt.rounded(b,x+cos(a)*2.6f,18f,z+sin(a)*2.6f,.75f,1.25f,.75f,.2f,c,25f)}}
            2->{b.ellipsoid(x,16.8f,z,2.6f,4.1f,2.6f,c,16,24,25f);DreamSculpt.cylinder(b,x,20.5f,z,.48f,1.3f,c,25f,12);b.ellipsoid(x,22.2f,z,.75f,.75f,.75f,c,9,14,25f)}
            else->{DreamSculpt.cylinder(b,x,13f,z,2.3f,4.6f,c,25f,24);DreamSculpt.rounded(b,x,19f,z,.65f,2.4f,.65f,.22f,c,25f);DreamSculpt.rounded(b,x,19.4f,z,2f,.60f,.65f,.22f,c,25f)}
        }
    }
}
