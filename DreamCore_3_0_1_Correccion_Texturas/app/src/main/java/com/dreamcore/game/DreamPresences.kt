package com.dreamcore.game

import kotlin.math.*

/** Authored feather, eyelid, fabric and tapered-tail geometry; no damage or chase state. */
object DreamPresences {
    private val ivory=floatArrayOf(.78f,.78f,.70f)
    private val cloth=floatArrayOf(.41f,.53f,.65f)
    private val dark=floatArrayOf(.12f,.10f,.13f)
    // A convex oval with atlas UVs: positive Z faces the viewer.
    fun face(b:DreamMesh,x:Float,y:Float,z:Float,rx:Float,ry:Float,tile:Int,kind:Float){
        val cols=32;val rings=7
        fun p(a:Float,r:Float)=floatArrayOf(x+cos(a)*rx*r,y+sin(a)*ry*r,z+.16f*(1f-r*r))
        fun uv(a:Float,r:Float)=floatArrayOf((tile%2)*.5f+.25f+cos(a)*r*(if(tile==2).17f else .237f),(if(tile<2).225f else .745f)-sin(a)*r*(if(tile<2).205f else .215f),0f)
        fun n(a:Float,r:Float):FloatArray{val v=floatArrayOf(cos(a)*r*.16f/rx,sin(a)*r*.16f/ry,1f);val len=sqrt(v.sumOf{(it*it).toDouble()}.toFloat());return v.map{it/len}.toFloatArray()}
        for(j in 0 until rings)for(i in 0 until cols){val a=i*2f*PI.toFloat()/cols;val d=(i+1)*2f*PI.toFloat()/cols;val r0=j.toFloat()/rings;val r1=(j+1f)/rings
            b.mappedTriangle(p(a,r0),p(a,r1),p(d,r1),uv(a,r0),uv(a,r1),uv(d,r1),n(a,r0),n(a,r1),n(d,r1),kind)
            if(j>0)b.mappedTriangle(p(a,r0),p(d,r1),p(d,r0),uv(a,r0),uv(d,r1),uv(d,r0),n(a,r0),n(d,r1),n(d,r0),kind)
        }
    }
    fun contact(b:DreamMesh,radius:Float){
        val a=floatArrayOf(-radius,.035f,-radius);val d=floatArrayOf(radius,.035f,-radius);val e=floatArrayOf(radius,.035f,radius);val f=floatArrayOf(-radius,.035f,radius)
        val ua=floatArrayOf(0f,0f,0f);val ud=floatArrayOf(1f,0f,0f);val ue=floatArrayOf(1f,1f,0f);val uf=floatArrayOf(0f,1f,0f);val n=floatArrayOf(0f,1f,0f)
        b.mappedTriangle(a,e,d,ua,ue,ud,n,n,n,99f);b.mappedTriangle(a,f,e,ua,uf,ue,n,n,n,99f)
    }
    fun figure(role:String):DreamWorlds.Scene{
        val b=DreamMesh();contact(b,1.7f);val mourner=role=="mourner";val flower=role=="guide"
        val c=if(mourner)floatArrayOf(.29f,.31f,.37f)else cloth
        // Fluted bell-shaped dress, asymmetrical hem and narrow shoulders.
        for(j in 0 until 10)for(i in 0 until 40){
            fun p(r:Int,k:Int):FloatArray{val a=k*2f*PI.toFloat()/40;val t=r/10f;val radius=.40f+(1f-t).pow(1.7f)*.95f+.08f*sin(a*18f)*(1f-t*.7f);return floatArrayOf(cos(a)*radius,.18f+t*3.25f+.09f*sin(a*7f)*(1f-t),sin(a)*radius*.62f)}
            b.quad(p(j,i),p(j,i+1),p(j+1,i+1),p(j+1,i),c,32f)
        }
        b.ellipsoid(0f,3.45f,0f,.55f,.31f,.27f,ivory,9,16,32f)
        for(s in floatArrayOf(-1f,1f)){
            DreamShapes.tube(b,listOf(floatArrayOf(s*.50f,3.25f,0f),floatArrayOf(s*.86f,2.45f,.08f),floatArrayOf(s*.99f,1.30f,.24f)),.14f,c,32f,10)
            b.ellipsoid(s*1.01f,1.22f,.25f,.17f,.34f,.12f,ivory,8,12,32f)
            for(i in 0..3)DreamShapes.tube(b,listOf(floatArrayOf(s*1.01f+(i-1.5f)*.07f,1.07f,.27f),floatArrayOf(s*1.05f+(i-1.5f)*.08f,.72f,.34f),floatArrayOf(s*1.04f+(i-1.5f)*.08f,.66f,.30f)),.027f,ivory,32f,6)
            // Sleeve eyes are smaller, with their own fleshy rim.
            b.ellipsoid(s*.88f,2.2f,.19f,.28f,.34f,.21f,ivory,8,14,32f)
            face(b,s*.88f,2.2f,.39f,.22f,.16f,1,97f)
        }
        val headY=if(mourner)5.2f else 4.35f
        DreamShapes.tube(b,listOf(floatArrayOf(0f,3.55f,0f),floatArrayOf(.08f,headY-.40f,0f)),.12f,ivory,32f,10)
        b.ellipsoid(0f,headY,0f,.79f,.63f,.30f,ivory,14,24,32f)
        if(flower){
            for(i in 0 until 18){val a=i*2f*PI.toFloat()/18;DreamShapes.tube(b,listOf(floatArrayOf(cos(a)*.52f,headY+sin(a)*.50f,0f),floatArrayOf(cos(a)*1.05f,headY+sin(a)*1.05f,-.05f),floatArrayOf(cos(a)*1.22f,headY+sin(a)*1.22f,-.12f)),.13f,ivory,32f,9)}
        }else{
            for(s in floatArrayOf(-1f,1f))for(i in 0 until 15){val t=i/14f
                DreamShapes.tube(b,listOf(floatArrayOf(s*.50f,headY+.42f,0f),floatArrayOf(s*(.83f+t*.66f),headY+.73f-t*.17f,-.05f),floatArrayOf(s*(.75f+t*1.35f),headY+.72f-t*.95f,-.03f),floatArrayOf(s*(.7f+t*1.2f),headY+.59f-t*1.12f,.03f)),.095f,ivory,32f,7)
            }
        }
        if(!flower)for(s in floatArrayOf(-1f,1f)){
            val n=floatArrayOf(0f,0f,1f)
            for(i in 0 until 10){val t=i/10f;val d=(i+1)/10f
                fun p(q:Float,h:Float)=floatArrayOf(s*(.58f+q*1.55f),headY+.55f-q*q*.75f-h*(.40f+q*.35f),.14f)
                fun uv(q:Float,h:Float)=floatArrayOf(.52f+q*.45f,.54f+h*.41f,0f)
                b.mappedTriangle(p(t,0f),p(d,0f),p(d,1f),uv(t,0f),uv(d,0f),uv(d,1f),n,n,n,97.2f)
                b.mappedTriangle(p(t,0f),p(d,1f),p(t,1f),uv(t,0f),uv(d,1f),uv(t,1f),n,n,n,97.2f)
            }
        }
        face(b,0f,headY,.30f,.71f,.48f,0,97f)
        if(mourner)for(s in floatArrayOf(-1f,1f))DreamShapes.tube(b,listOf(floatArrayOf(s*.40f,headY-.30f,.31f),floatArrayOf(s*.43f,headY-.96f,.26f),floatArrayOf(s*.34f,headY-1.6f,.35f)),.035f,floatArrayOf(.18f,.20f,.27f),32f,7)
        for(i in 0..4)b.ellipsoid(0f,3.15f-i*.23f,.31f,.035f,.035f,.025f,ivory,5,7,32f)
        return b.finish()
    }
    fun friendly():DreamWorlds.Scene{
        val b=DreamMesh();contact(b,1.1f);val red=floatArrayOf(.43f,.10f,.13f)
        // A genuinely tapered curved tail, swept from large neck to a tiny tip.
        val steps=48;val sides=10
        fun centre(t:Float)=floatArrayOf(sin(t*7f)*(.15f+t*.8f),3.7f-t*3.3f,-.15f-t*.75f)
        for(j in 0 until steps)for(i in 0 until sides){fun p(jj:Int,ii:Int):FloatArray{val t=jj.toFloat()/steps;val c=centre(t);val a=ii*2f*PI.toFloat()/sides;val r=.20f*(1f-t).pow(.8f)+.012f;return floatArrayOf(c[0]+cos(a)*r,c[1],c[2]+sin(a)*r)};b.quad(p(j,i),p(j,i+1),p(j+1,i+1),p(j+1,i),dark,98f)}
        b.ellipsoid(0f,4.22f,0f,.63f,.88f,.46f,red,16,24,98f)
        face(b,0f,4.25f,.39f,.57f,.80f,2,98.1f)
        return b.finish()
    }
}
