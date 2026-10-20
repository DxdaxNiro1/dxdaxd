package com.dreamcore.game

import kotlin.math.*

/** Rounded surfaces carry independent smooth normals; the result is ordinary GLES2 triangles. */
object DreamSculpt {
    val ivory=floatArrayOf(.90f,.88f,.82f)
    val wood=floatArrayOf(.43f,.27f,.17f)
    val teal=floatArrayOf(.24f,.47f,.43f)
    fun rounded(b:DreamMesh,x:Float,y:Float,z:Float,sx:Float,sy:Float,sz:Float,r:Float,c:FloatArray,k:Float=0f){
        val half=floatArrayOf(sx,sy,sz);val radius=min(r,min(sx,min(sy,sz))*.85f)
        for(axis in 0..2)for(sign in floatArrayOf(-1f,1f)){
            val u=(axis+1)%3;val v=(axis+2)%3
            fun values(h:Float)=floatArrayOf(-h,-h+radius,h-radius,h)
            val us=values(half[u]);val vs=values(half[v])
            data class P(val p:FloatArray,val n:FloatArray)
            fun point(i:Int,j:Int):P{val p=FloatArray(3);p[axis]=sign*half[axis];p[u]=us[i];p[v]=vs[j]
                val core=FloatArray(3){p[it].coerceIn(-half[it]+radius,half[it]-radius)};val n=FloatArray(3){p[it]-core[it]};val len=sqrt(n.sumOf{(it*it).toDouble()}.toFloat()).coerceAtLeast(.00001f)
                for(a in 0..2){n[a]/=len;p[a]=core[a]+n[a]*radius};p[0]+=x;p[1]+=y;p[2]+=z;return P(p,n)}
            fun tri(a:P,d:P,e:P){b.mappedTriangle(a.p,d.p,e.p,c,c,c,a.n,d.n,e.n,k)}
            for(i in 0..2)for(j in 0..2){val a=point(i,j);val d=point(i+1,j);val e=point(i+1,j+1);val f=point(i,j+1);if(sign>0){tri(a,d,e);tri(a,e,f)}else{tri(a,e,d);tri(a,f,e)}}
        }
    }
    fun cylinder(b:DreamMesh,x:Float,y:Float,z:Float,r:Float,h:Float,c:FloatArray,k:Float=15f,sides:Int=16){
        for(i in 0 until sides){val a=i*2f*PI.toFloat()/sides;val d=(i+1)*2f*PI.toFloat()/sides
            val na=floatArrayOf(cos(a),0f,sin(a));val nd=floatArrayOf(cos(d),0f,sin(d))
            fun p(t:Float,yy:Float)=floatArrayOf(x+r*cos(t),yy,z+r*sin(t))
            b.mappedTriangle(p(a,y),p(d,y),p(d,y+h),c,c,c,na,nd,nd,k)
            b.mappedTriangle(p(a,y),p(d,y+h),p(a,y+h),c,c,c,na,nd,na,k)
            b.triangle(floatArrayOf(x,y+h,z),p(d,y+h),p(a,y+h),c,k)
        }
    }
    fun chair(b:DreamMesh,x:Float,y:Float,z:Float,cinema:Boolean=false,upholstery:FloatArray?=null){
        val cloth=upholstery?:if(cinema)floatArrayOf(.61f,.27f,.33f)else teal
        rounded(b,x,y+.60f,z,.56f,.13f,.51f,.11f,cloth,42f)
        // Bowed upholstered back, following a cylinder rather than a flat rectangle.
        for(i in 0 until 10){val a=-.65f+i*.13f;val d=a+.13f
            fun p(t:Float,h:Float)=floatArrayOf(x+sin(t)*.9f,y+h-(if(h>1f).15f*(abs(t)/.65f).pow(3)else 0f),z-.48f+(1f-cos(t))*.55f)
            b.quad(p(a,.73f),p(d,.73f),p(d,1.42f),p(a,1.42f),cloth,42f)
            b.quad(p(d,1.42f),p(d,.73f),p(a,.73f),p(a,1.42f),wood,43f)
        }
        for(s in floatArrayOf(-1f,1f)){
            if(!cinema)for(t in floatArrayOf(-1f,1f))DreamShapes.tube(b,listOf(floatArrayOf(x+s*.45f,y+.55f,z+t*.4f),floatArrayOf(x+s*.5f,y+.05f,z+t*.46f)),.045f,wood,43f,6)
            else cylinder(b,x+s*.33f,y,z-.05f,.08f,.5f,wood,40f,8)
            DreamShapes.tube(b,listOf(floatArrayOf(x+s*.60f,y+.72f,z+.32f),floatArrayOf(x+s*.60f,y+1f,z+.26f),floatArrayOf(x+s*.60f,y+1f,z-.39f)),.055f,wood,43f,7)
        }
    }
    fun pendant(b:DreamMesh,x:Float,y:Float,z:Float){
        cylinder(b,x,y-.8f,z,.035f,.8f,wood,40f,8)
        b.ellipsoid(x,y-.9f,z,.40f,.23f,.40f,ivory,9,20,24f)
        for(i in 0..2){val a=i*2f*PI.toFloat()/3f;DreamShapes.tube(b,listOf(floatArrayOf(x,y-.52f,z),floatArrayOf(x+cos(a)*.44f,y-.50f,z+sin(a)*.44f),floatArrayOf(x+cos(a)*.48f,y-.68f,z+sin(a)*.48f),floatArrayOf(x+cos(a)*.31f,y-.83f,z+sin(a)*.31f)),.025f,wood,40f,8)}
    }
    fun computer(b:DreamMesh,x:Float,y:Float,z:Float){
        rounded(b,x,y+.42f,z,.56f,.45f,.48f,.10f,ivory,0f)
        rounded(b,x,y+.44f,z+.49f,.47f,.35f,.025f,.025f,floatArrayOf(.06f,.07f,.08f),95f)
        b.ellipsoid(x,y-.05f,z,.42f,.08f,.32f,ivory,5,12,15f)
        rounded(b,x,y-.05f,z+.94f,.58f,.035f,.22f,.025f,ivory,15f)
        for(i in -4..4)b.box(x+i*.11f,y,z+.95f,.035f,.012f,.12f,floatArrayOf(.48f,.48f,.44f),15f)
        b.ellipsoid(x+.84f,y-.04f,z+.83f,.13f,.065f,.20f,ivory,6,10,15f)
    }
    fun house(b:DreamMesh,x:Float,y:Float,z:Float,c:FloatArray,scale:Float=1f,tilt:Float=0f){
        val m=DreamMesh();val w=4.3f*scale;val d=4f*scale;val h=5.1f*scale
        rounded(m,0f,h*.5f,0f,w,h*.5f,d,.14f,c,0f)
        fun p(xx:Float,yy:Float,zz:Float)=floatArrayOf(xx,yy,zz)
        val roof=floatArrayOf(c[0]*.65f,c[1]*.65f,c[2]*.65f)
        for(s in floatArrayOf(-1f,1f))m.quad(p(s*(w+.35f),h,d+.4f),p(0f,h+2.6f*scale,d+.4f),p(0f,h+2.6f*scale,-d-.4f),p(s*(w+.35f),h,-d-.4f),roof,43f)
        for(s in floatArrayOf(-1f,1f))m.triangle(p(-w,h,s*d),p(w,h,s*d),p(0f,h+2.6f*scale,s*d),c,0f)
        rounded(m,0f,1.3f*scale,d+.03f,.68f*scale,1.3f*scale,.10f,.08f,ivory,43f)
        for(xx in floatArrayOf(-2.5f,2.5f))for(yy in floatArrayOf(1.8f,4f)){
            rounded(m,xx*scale,yy*scale,d+.04f,.55f*scale,.65f*scale,.12f,.07f,ivory,15f)
            m.box(xx*scale,yy*scale,d+.18f,.42f*scale,.53f*scale,.012f,floatArrayOf(.28f,.43f,.50f),34f)
        }
        val data=m.finish().vertices
        for(i in data.indices step 10){val xx=data[i];val yy=data[i+1];data[i]=x+xx*cos(tilt)-yy*sin(tilt);data[i+1]=y+xx*sin(tilt)+yy*cos(tilt);data[i+2]+=z
            val nx=data[i+6];val ny=data[i+7];data[i+6]=nx*cos(tilt)-ny*sin(tilt);data[i+7]=nx*sin(tilt)+ny*cos(tilt)}
        // Preserve authored normals when appending.
        for(i in data.indices step 30){fun a(j:Int,k:Int)=data.copyOfRange(i+j+k,i+j+k+3)
            b.mappedTriangle(a(0,0),a(10,0),a(20,0),a(0,3),a(10,3),a(20,3),a(0,6),a(10,6),a(20,6),data[i+9])}
    }
}
