package com.dreamcore.game

import kotlin.math.*

/** Sculpted front/back volumes, recessed sockets, jaws and separate teeth/eyeballs/lids. */
object DreamMoonMesh27 {
    fun create(crescent:Boolean=false):DreamWorlds.Scene {
        val b=DreamMesh()
        fun stone(x:Float,y:Float):Float {
            fun bump(cx:Float,cy:Float,sx:Float,sy:Float,h:Float)=h*exp(-((x-cx)/sx).pow(2)-((y-cy)/sy).pow(2))
            var z=sqrt(max(.001f,1f-x*x-y*y))
            z+=bump(0f,.05f,.10f,.27f,.13f)+bump(0f,-.12f,.13f,.10f,.10f)
            for(a in floatArrayOf(-1f,1f))z+=bump(a*.34f,.20f,.18f,.12f,-.12f)+bump(a*.34f,.35f,.23f,.07f,.075f)+bump(a*.48f,-.12f,.20f,.23f,.055f)
            z+=.012f*sin(x*51f+y*21f)*sin(y*43f-x*17f)*(1f-x*x-y*y)
            return z
        }
        fun removed(x:Float,y:Float)=crescent && (x-.50f).pow(2)+(y-.04f).pow(2)<.76f*.76f
        fun mouthField(x:Float,y:Float):Float {val q=x/.49f;val centre=-.40f+.20f*q*q;return max(abs(q)-1f,abs(y-centre)-.105f*max(0f,1f-q*q))}
        fun eyeField(x:Float,y:Float):Float {val cx=if(crescent)-.46f else if(x<0f)-.34f else .34f;return ((x-cx)/.113f).pow(2)+((y-.20f)/.057f).pow(2)-1f}
        data class V(val p:FloatArray,val uv:FloatArray,val n:FloatArray)
        fun v(r:Float,a:Float,front:Boolean):V {
            val x=r*cos(a);val y=r*sin(a);val z=if(front)stone(x,y)else -sqrt(max(.001f,1f-x*x-y*y))
            val dx=if(front)(stone(x+.001f,y)-stone(x-.001f,y))/.002f else x/max(.03f,abs(z))
            val dy=if(front)(stone(x,y+.001f)-stone(x,y-.001f))/.002f else y/max(.03f,abs(z))
            val n=if(front)floatArrayOf(-dx,-dy,1f)else floatArrayOf(dx,dy,-1f);val l=sqrt(n.sumOf{(it*it).toDouble()}.toFloat());for(i in 0..2)n[i]/=l
            return V(floatArrayOf(x,y,z),floatArrayOf(.5f+x*.48f,.5f-y*.48f,if(front)0f else 1f),n)
        }
        fun blend(a:V,c:V,t:Float)=V(FloatArray(3){a.p[it]+(c.p[it]-a.p[it])*t},FloatArray(3){a.uv[it]+(c.uv[it]-a.uv[it])*t},FloatArray(3){a.n[it]+(c.n[it]-a.n[it])*t})
        fun tri(a:V,c:V,d:V,front:Boolean) {
            // Clip against the continuous socket/grin boundary; no jagged centroid-only gaps.
            var polygon=listOf(a,c,d)
            val fields=ArrayList<(V)->Float>()
            if(crescent)fields.add{v->(v.p[0]-.50f).pow(2)+(v.p[1]-.04f).pow(2)-.76f*.76f}
            if(front){if(!crescent)fields.add{v->mouthField(v.p[0],v.p[1])};fields.add{v->eyeField(v.p[0],v.p[1])}}
            for(field in fields){val next=ArrayList<V>();if(polygon.isEmpty())return
                for(i in polygon.indices){val p=polygon[i];val q=polygon[(i+1)%polygon.size];val fp=field(p);val fq=field(q)
                    if(fp>=0f)next.add(p)
                    if((fp>=0f)!=(fq>=0f))next.add(blend(p,q,(fp/(fp-fq)).coerceIn(0f,1f)))
                };polygon=next
            }
            for(i in 1 until polygon.size-1){val p=polygon[0];val q=polygon[i];val r=polygon[i+1]
                if(front)b.mappedTriangle(p.p,q.p,r.p,p.uv,q.uv,r.uv,p.n,q.n,r.n,0f)
                else b.mappedTriangle(r.p,q.p,p.p,r.uv,q.uv,p.uv,r.n,q.n,p.n,0f)
            }
        }
        val rings=40;val sides=96
        for(front in listOf(true,false))for(i in 0 until rings)for(j in 0 until sides){
            val a=v(i.toFloat()/rings,j*2f*PI.toFloat()/sides,front);val c=v((i+1f)/rings,j*2f*PI.toFloat()/sides,front)
            val d=v((i+1f)/rings,(j+1f)*2f*PI.toFloat()/sides,front);val e=v(i.toFloat()/rings,(j+1f)*2f*PI.toFloat()/sides,front)
            tri(a,c,d,front);if(i>0)tri(a,d,e,front)
        }
        // The crescent has a closed, curved interior rim instead of a black painted disc.
        if(crescent)for(i in 0 until 96){val a=i*2f*PI.toFloat()/96;val d=(i+1)*2f*PI.toFloat()/96
            fun p(t:Float,side:Float):FloatArray{val x=.50f+.76f*cos(t);val y=.04f+.76f*sin(t);return floatArrayOf(x,y,side*sqrt(max(.001f,1f-x*x-y*y)))}
            val pa=p(a,1f);val pd=p(d,1f);if(pa[0]*pa[0]+pa[1]*pa[1]<.99f && pd[0]*pd[0]+pd[1]*pd[1]<.99f)b.quad(pa,p(a,-1f),p(d,-1f),pd,floatArrayOf(.21f,.44f,.21f),8f)
        }
        val eyes=if(crescent)floatArrayOf(-.46f)else floatArrayOf(-.34f,.34f)
        for(x in eyes){val y=.20f;val z=stone(x,y)+.018f
            b.ellipsoid(x,y,z,.108f,.054f,.050f,floatArrayOf(.10f,.12f,.17f),12,20,1f)
            b.ellipsoid(x,y,z+.050f,.022f,.023f,.010f,floatArrayOf(.22f,.53f,.98f),10,16,2f)
            b.ellipsoid(x,y,z+.061f,.008f,.014f,.004f,floatArrayOf(.002f,.003f,.008f),8,12,3f)
            // Two curved skin patches close smoothly from the rim to the socket centre.
            for(top in listOf(true,false))for(i in 0 until 24)for(j in 0 until 4){
                val side=if(top)1f else -1f
                fun p(xx:Float,v:Float):FloatArray{val edge=.080f*sqrt(max(0f,1f-xx*xx));return floatArrayOf(x+xx*.14f,y+side*edge*v,z+.063f+.009f*(1f-xx*xx))}
                val xx=-1f+i/12f;val next=xx+1f/12f;val color=floatArrayOf(x,y,z);val n=floatArrayOf(0f,0f,1f)
                val pa=p(xx,j/4f);val pc=p(next,j/4f);val pd=p(next,(j+1)/4f);val pe=p(xx,(j+1)/4f);val kind=if(top)4f else 5f
                b.mappedTriangle(pa,pc,pd,color,color,color,n,n,n,kind);b.mappedTriangle(pa,pd,pe,color,color,color,n,n,n,kind)
            }
        }
        if(!crescent){
            b.ellipsoid(0f,-.34f,.77f,.50f,.19f,.09f,floatArrayOf(.006f,.002f,.009f),16,32,7f)
            // Irregular rounded incisors: oval lofts, with two restrained canines.
            for(top in listOf(true,false))for(i in 0 until 15){
                val x=-.44f+i*.063f;val q=x/.49f;val edge=-.40f+.20f*q*q
                val y=edge+(if(top).083f else -.076f)*(1f-q*q)
                val length=(.048f+.019f*(.5f+.5f*sin(i*2.8f)))*(1f-.32f*abs(q))+(if(i==2||i==12).025f else 0f)
                val direction=if(top)-1f else 1f;val z=stone(x,y)+.006f;val width=.026f*(1f-.2f*abs(q))
                fun p(r:Int,j:Int):FloatArray {val t=r/4f;val a=j*PI.toFloat()/4;val taper=1f-.60f*t*t;return floatArrayOf(x+cos(a)*width*taper,y+direction*length*t,z+.020f+sin(a)*.022f*taper)}
                val color=floatArrayOf(.73f+.08f*sin(i.toFloat()),.67f,.49f)
                for(r in 0..3)for(j in 0..7)b.quad(p(r,j),p(r+1,j),p(r+1,j+1),p(r,j+1),color,6f)
                for(j in 0..7)b.triangle(p(4,j),floatArrayOf(x,y+direction*length*1.06f,z+.020f),p(4,j+1),color,6f)
            }
        }else{
            b.ellipsoid(-.40f,-.10f,.93f,.12f,.19f,.28f,floatArrayOf(.25f,.40f,.25f),12,18,8f)
            b.ellipsoid(-.46f,-.40f,.84f,.13f,.17f,.06f,floatArrayOf(.006f,.004f,.004f),10,16,7f)
        }
        return b.finish()
    }
}
