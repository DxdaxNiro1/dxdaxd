package com.dreamcore.game

import kotlin.math.*

/** Median BVH over actual triangles: queries skip whole empty regions, no per-query allocations. */
class DreamSkySight(vertices:FloatArray) {
    private val kinds33:FloatArray
    private val triangles:FloatArray
    private val order:IntArray
    private data class Node(val b:DreamPerformance.Bounds,val start:Int,val end:Int,var left:Int=-1,var right:Int=-1)
    private val nodes=ArrayList<Node>()
    private val stack=IntArray(96)
    var testedTriangles=0;private set
    init {
        var count=0
        for(i in vertices.indices step 30)if(max(vertices[i+1],max(vertices[i+11],vertices[i+21]))>=.35f)count++
        triangles=FloatArray(count*9);kinds33=FloatArray(count);var cursor=0;var face33=0
        for(i in vertices.indices step 30){if(max(vertices[i+1],max(vertices[i+11],vertices[i+21]))<.35f)continue
            kinds33[face33++]=vertices[i+9];for(j in 0..20 step 10)for(k in 0..2)triangles[cursor++]=vertices[i+j+k]}
        order=IntArray(count){it}
        if(count>0)build(0,count)
    }
    /** Capsule projected against nearby static triangles. Sloping support floors retain their existing height solver. */
    fun bodyClear33(x:Float,feet:Float,z:Float,radius:Float=.27f):Boolean{
        if(nodes.isEmpty())return true
        var top=0;stack[top++]=0;val low=feet+.18f;val high=feet+1.48f;val rr=radius*radius
        fun edge(ax:Float,az:Float,bx:Float,bz:Float):Float{val dx=bx-ax;val dz=bz-az;val t=((x-ax)*dx+(z-az)*dz)/(dx*dx+dz*dz).coerceAtLeast(.000001f);val u=t.coerceIn(0f,1f);val px=x-ax-dx*u;val pz=z-az-dz*u;return px*px+pz*pz}
        while(top>0){val node=nodes[stack[--top]];val b=node.b;if(x+radius<b.x0||x-radius>b.x1||z+radius<b.z0||z-radius>b.z1||high<b.y0||low>b.y1)continue
            if(node.left>=0){stack[top++]=node.left;stack[top++]=node.right;continue}
            for(n in node.start until node.end){val index=order[n];val k=kinds33[index]
                if(!(k<.5f||k in 3.5f..19.49f||k in 24.5f..27.49f||k in 39.5f..44.49f||k in 111.5f..115.49f))continue
                val i=index*9;val ax=triangles[i];val ay=triangles[i+1];val az=triangles[i+2];val bx=triangles[i+3];val by=triangles[i+4];val bz=triangles[i+5];val cx=triangles[i+6];val cy=triangles[i+7];val cz=triangles[i+8]
                if(high<min(ay,min(by,cy))||low>max(ay,max(by,cy)))continue
                val ux=bx-ax;val uy=by-ay;val uz=bz-az;val vx=cx-ax;val vy=cy-ay;val vz=cz-az;val nx=uy*vz-uz*vy;val ny=uz*vx-ux*vz;val nz=ux*vy-uy*vx
                val norm=sqrt(nx*nx+ny*ny+nz*nz).coerceAtLeast(.0001f)
                if(abs(ny)/norm>.65f&&max(ay,max(by,cy))-min(ay,min(by,cy))>.12f)continue
                val area=(bx-ax)*(cz-az)-(bz-az)*(cx-ax)
                var inside=false
                if(abs(area)>.0001f){val a=((bx-x)*(cz-z)-(bz-z)*(cx-x))/area;val c=((cx-x)*(az-z)-(cz-z)*(ax-x))/area;inside=a>=0f&&c>=0f&&a+c<=1f}
                if(inside||min(edge(ax,az,bx,bz),min(edge(bx,bz,cx,cz),edge(cx,cz,ax,az)))<rr)return false
            }
        }
        return true
    }
    private fun centre(index:Int,axis:Int):Float {val i=index*9+axis;return (triangles[i]+triangles[i+3]+triangles[i+6])/3f}
    private fun build(start:Int,end:Int):Int {
        var x0=Float.POSITIVE_INFINITY;var y0=x0;var z0=x0;var x1=Float.NEGATIVE_INFINITY;var y1=x1;var z1=x1
        for(k in start until end){val i=order[k]*9;for(j in 0..6 step 3){x0=min(x0,triangles[i+j]);x1=max(x1,triangles[i+j]);y0=min(y0,triangles[i+j+1]);y1=max(y1,triangles[i+j+1]);z0=min(z0,triangles[i+j+2]);z1=max(z1,triangles[i+j+2])}}
        val index=nodes.size;val node=Node(DreamPerformance.Bounds(x0,y0,z0,x1,y1,z1),start,end);nodes.add(node)
        if(end-start>16){
            val axis=if(x1-x0>y1-y0 && x1-x0>z1-z0)0 else if(y1-y0>z1-z0)1 else 2
            val middle=(start+end)/2;var lo=start;var hi=end-1
            while(lo<hi){val pivot=centre(order[(lo+hi)/2],axis);var a=lo;var b=hi
                while(a<=b){while(centre(order[a],axis)<pivot)a++;while(centre(order[b],axis)>pivot)b--;if(a<=b){val t=order[a];order[a]=order[b];order[b]=t;a++;b--}}
                if(middle<=b)hi=b else if(middle>=a)lo=a else break
            }
            node.left=build(start,middle);node.right=build(middle,end)
        }
        return index
    }
    private fun box(b:DreamPerformance.Bounds,x:Float,y:Float,z:Float,dx:Float,dy:Float,dz:Float,limit:Float):Boolean {
        var lo=0f;var hi=limit
        for(axis in 0..2){
            val position=if(axis==0)x else if(axis==1)y else z
            val direction=if(axis==0)dx else if(axis==1)dy else dz
            val min=if(axis==0)b.x0 else if(axis==1)b.y0 else b.z0
            val max=if(axis==0)b.x1 else if(axis==1)b.y1 else b.z1
            if(abs(direction)<.0000001f){if(position<min-.0001f||position>max+.0001f)return false;continue}
            var t0=(min-position)/direction;var t1=(max-position)/direction
            if(t0>t1){val swap=t0;t0=t1;t1=swap};lo=kotlin.math.max(lo,t0);hi=kotlin.math.min(hi,t1)
            if(lo>hi+.0001f)return false
        }
        return true
    }
    fun clear(x:Float,y:Float,z:Float,dx:Float,dy:Float,dz:Float,limit:Float=2000f):Boolean {
        testedTriangles=0;if(nodes.isEmpty())return true
        var size=1;stack[0]=0
        while(size>0){val node=nodes[stack[--size]];if(!box(node.b,x,y,z,dx,dy,dz,limit))continue
            if(node.left>=0){stack[size++]=node.left;stack[size++]=node.right;continue}
            for(k in node.start until node.end){testedTriangles++;val i=order[k]*9
                val ax=triangles[i];val ay=triangles[i+1];val az=triangles[i+2]
                val ux=triangles[i+3]-ax;val uy=triangles[i+4]-ay;val uz=triangles[i+5]-az
                val vx=triangles[i+6]-ax;val vy=triangles[i+7]-ay;val vz=triangles[i+8]-az
                val px=dy*vz-dz*vy;val py=dz*vx-dx*vz;val pz=dx*vy-dy*vx
                val determinant=ux*px+uy*py+uz*pz;if(abs(determinant)<.000001f)continue
                val inverse=1f/determinant;val tx=x-ax;val ty=y-ay;val tz=z-az
                val u=(tx*px+ty*py+tz*pz)*inverse;if(u<0f||u>1f)continue
                val qx=ty*uz-tz*uy;val qy=tz*ux-tx*uz;val qz=tx*uy-ty*ux
                val v=(dx*qx+dy*qy+dz*qz)*inverse;if(v<0f||u+v>1f)continue
                val distance=(vx*qx+vy*qy+vz*qz)*inverse;if(distance>.18f&&distance<limit)return false
            }
        }
        return true
    }
}
