package com.dreamcore.game

import kotlin.math.*

/** Sculpted front/back volumes, recessed sockets, jaws and separate teeth/eyeballs/lids. */
object DreamMoonMesh27 {
    fun create(crescent:Boolean=false):DreamWorlds.Scene {
        val b=DreamMesh()
        fun stone(x:Float,y:Float):Float {
            fun bump(cx:Float,cy:Float,sx:Float,sy:Float,h:Float)=h*exp(-((x-cx)/sx).pow(2)-((y-cy)/sy).pow(2))
            var z=sqrt(max(.001f,1f-x*x-y*y))
            z+=bump(0f,.04f,.11f,.32f,.23f)+bump(0f,-.13f,.17f,.11f,.19f)
            for(a in floatArrayOf(-1f,1f))z+=bump(a*.34f,.20f,.21f,.13f,-.14f)+bump(a*.34f,.38f,.24f,.09f,.14f)+bump(a*.50f,-.12f,.23f,.23f,.1f)
            z+=.012f*sin(x*51f+y*21f)*sin(y*43f-x*17f)*(1f-x*x-y*y)
            return z
        }
        fun removed(x:Float,y:Float)=crescent && (x-.50f).pow(2)+(y-.04f).pow(2)<.76f*.76f
        fun mouth(x:Float,y:Float)=!crescent && abs(x)<.57f && abs(y-(-.40f+.30f*(x/.60f).pow(2)))<.14f*(1f-(x/.60f).pow(2))
        data class V(val p:FloatArray,val uv:FloatArray,val n:FloatArray)
        fun v(r:Float,a:Float,front:Boolean):V {
            val x=r*cos(a);val y=r*sin(a);val z=if(front)stone(x,y)else -sqrt(max(.001f,1f-x*x-y*y))
            val dx=if(front)(stone(x+.001f,y)-stone(x-.001f,y))/.002f else x/max(.03f,abs(z))
            val dy=if(front)(stone(x,y+.001f)-stone(x,y-.001f))/.002f else y/max(.03f,abs(z))
            val n=if(front)floatArrayOf(-dx,-dy,1f)else floatArrayOf(dx,dy,-1f);val l=sqrt(n.sumOf{(it*it).toDouble()}.toFloat());for(i in 0..2)n[i]/=l
            return V(floatArrayOf(x,y,z),floatArrayOf(.5f+x*.48f,.5f-y*.48f,0f),n)
        }
        fun tri(a:V,c:V,d:V,front:Boolean) {
            val x=(a.p[0]+c.p[0]+d.p[0])/3;val y=(a.p[1]+c.p[1]+d.p[1])/3
            if(removed(x,y)||(front&&mouth(x,y)))return
            if(front)b.mappedTriangle(a.p,c.p,d.p,a.uv,c.uv,d.uv,a.n,c.n,d.n,0f)
            else b.mappedTriangle(d.p,c.p,a.p,d.uv,c.uv,a.uv,d.n,c.n,a.n,0f)
        }
        val rings=26;val sides=72
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
        for(x in eyes){val y=.20f;val z=stone(x,y)+.055f
            b.ellipsoid(x,y,z,.105f,.060f,.045f,floatArrayOf(.10f,.12f,.17f),12,20,1f)
            b.ellipsoid(x,y,z+.046f,.018f,.022f,.010f,floatArrayOf(.22f,.53f,.98f),10,16,2f)
            b.ellipsoid(x,y,z+.057f,.006f,.012f,.004f,floatArrayOf(.002f,.003f,.008f),8,12,3f)
            // Two curved skin patches close smoothly from the rim to the socket centre.
            for(top in listOf(true,false))for(i in 0 until 24)for(j in 0 until 4){
                val side=if(top)1f else -1f
                fun p(xx:Float,v:Float):FloatArray{val edge=.080f*sqrt(max(0f,1f-xx*xx));return floatArrayOf(x+xx*.14f,y+side*edge*v,z+.080f+.009f*(1f-xx*xx))}
                val xx=-1f+i/12f;val next=xx+1f/12f;val color=floatArrayOf(x,y,z);val n=floatArrayOf(0f,0f,1f)
                val pa=p(xx,j/4f);val pc=p(next,j/4f);val pd=p(next,(j+1)/4f);val pe=p(xx,(j+1)/4f);val kind=if(top)4f else 5f
                b.mappedTriangle(pa,pc,pd,color,color,color,n,n,n,kind);b.mappedTriangle(pa,pd,pe,color,color,color,n,n,n,kind)
            }
        }
        if(!crescent){
            b.ellipsoid(0f,-.34f,.79f,.59f,.24f,.08f,floatArrayOf(.006f,.002f,.009f),12,28,7f)
            for(top in listOf(true,false))for(i in 0 until 13){val x=-.51f+i*.085f;val edge=-.40f+.30f*(x/.60f).pow(2);val y=edge+(if(top).11f else -.10f)*(1f-(x/.60f).pow(2))
                val length=.09f+.075f*(.5f+.5f*sin(i*2.8f));val tip=y+(if(top)-length else length)
                val z=stone(x,y)+.04f
                for(k in 0 until 6){val a=k*PI.toFloat()/3;val d=(k+1)*PI.toFloat()/3
                    b.triangle(floatArrayOf(x+.032f*cos(a),y,z+.032f*sin(a)),floatArrayOf(x+.032f*cos(d),y,z+.032f*sin(d)),floatArrayOf(x+.008f,tip,z+.035f),floatArrayOf(.86f,.75f,.47f),6f)}
            }
        }else{
            b.ellipsoid(-.40f,-.10f,.93f,.12f,.19f,.28f,floatArrayOf(.25f,.40f,.25f),12,18,8f)
            b.ellipsoid(-.46f,-.40f,.84f,.13f,.17f,.06f,floatArrayOf(.006f,.004f,.004f),10,16,7f)
        }
        return b.finish()
    }
}
