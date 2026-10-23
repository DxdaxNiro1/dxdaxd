package com.dreamcore.game

import kotlin.math.*

/** Four deliberately motionless inhabitants. No damage, pursuit or combat hooks. */
object DreamGuardians27 {
    val restingPositions=listOf(DreamJourney.Point(-16f,-31f),DreamJourney.Point(26f,-92f),DreamJourney.Point(13f,-39f),DreamJourney.Point(-34f,-65f))
    private val bone=floatArrayOf(.78f,.77f,.71f)
    private val black=floatArrayOf(.035f,.033f,.043f)
    private val white=floatArrayOf(.94f,.92f,.88f)
    fun eye(b:DreamMesh,x:Float,y:Float,z:Float,r:Float){
        b.ellipsoid(x,y,z,r,r*.69f,r*.63f,bone,16,26,120f)
        val rings=12;val sides=48
        data class V(val p:FloatArray,val uv:FloatArray,val n:FloatArray)
        fun v(i:Int,j:Int):V{val a=j*2f*PI.toFloat()/sides;val t=i*.96f/rings;val u=t*cos(a);val w=t*sin(a);val d=sqrt(max(.001f,1f-t*t))
            return V(floatArrayOf(x+u*r,y+w*r*.69f,z+d*r*.65f),floatArrayOf(.5f+u*.49f,.5f-w*.49f,0f),floatArrayOf(u,w,d))}
        for(i in 0 until rings)for(j in 0 until sides){val a=v(i,j);val c=v(i+1,j);val d=v(i+1,j+1);val e=v(i,j+1)
            b.mappedTriangle(a.p,c.p,d.p,a.uv,c.uv,d.uv,a.n,c.n,d.n,117f)
            if(i>0)b.mappedTriangle(a.p,d.p,e.p,a.uv,d.uv,e.uv,a.n,d.n,e.n,117f)
        }
    }
    private fun feather(b:DreamMesh,start:FloatArray,end:FloatArray,width:Float){
        val dx=end[0]-start[0];val dy=end[1]-start[1];val length=hypot(dx,dy).coerceAtLeast(.01f);val rx=-dy/length;val ry=dx/length
        data class V(val p:FloatArray,val uv:FloatArray)
        fun v(t:Float,s:Float):V{val w=width*max(0f,sin(PI.toFloat()*t)).pow(.65f);val ridge=.1f*width*(1f-s*s)*sin(t*PI.toFloat())
            return V(floatArrayOf(start[0]+dx*t+rx*w*s,start[1]+dy*t+ry*w*s,start[2]+(end[2]-start[2])*t+ridge),floatArrayOf(.5f+s*.48f,t,0f))}
        val n=floatArrayOf(0f,0f,1f)
        for(i in 0 until 8)for(j in 0 until 4){val a=v(i/8f,-1f+j*.5f);val c=v((i+1)/8f,-1f+j*.5f);val d=v((i+1)/8f,-.5f+j*.5f);val e=v(i/8f,-.5f+j*.5f)
            b.mappedTriangle(a.p,c.p,d.p,a.uv,c.uv,d.uv,n,n,n,118f);b.mappedTriangle(a.p,d.p,e.p,a.uv,d.uv,e.uv,n,n,n,118f)}
        DreamShapes.tube(b,listOf(start,end),width*.025f,white,26f,5)
    }
    /** Six layered wings, halo and an eye with a sculpted wet cornea. Units scale in metres. */
    fun angel(b:DreamMesh,x:Float,y:Float,z:Float,scale:Float,folded:Boolean=false){
        for(layer in 0..2)for(side in floatArrayOf(-1f,1f)){
            val angle=if(folded)(-.75f+layer*.55f)else (-.63f+layer*.68f)
            for(row in 0..1)for(i in 0 until 17){val t=i/16f;val reach=(2.6f+2.1f*t)*scale;val a=angle+(.25f-t*.38f)
                val start=floatArrayOf(x+side*(.38f+t*1.3f)*scale,y+(layer-1)*.55f*scale,z-row*.16f*scale)
                val end=floatArrayOf(x+side*reach*cos(a),y+reach*sin(a)+(if(folded)-1.4f else .0f)*scale,z+(.15f+.55f*sin(t*PI.toFloat())-row*.16f)*scale)
                feather(b,start,end,(.15f+.13f*t)*scale)
            }
        }
        b.ellipsoid(x,y,z-.1f*scale,.85f*scale,1.25f*scale,.60f*scale,white,14,24,120f)
        eye(b,x,y+.20f*scale,z+.33f*scale,.73f*scale)
        if(scale>10f){
            // Three weathered ivory masks emerge above the central eye, among the plumage.
            for(k in -1..1){val xx=x+k*.62f*scale;val yy=y+(1.75f+if(k==0).15f else 0f)*scale
                b.ellipsoid(xx,yy,z-.1f*scale,.39f*scale,.63f*scale,.35f*scale,white,18,28,120f)
                b.ellipsoid(xx,yy-.24f*scale,z+.19f*scale,.24f*scale,.26f*scale,.20f*scale,bone,12,20,120f)
                for(side in floatArrayOf(-1f,1f)){
                    b.ellipsoid(xx+side*.135f*scale,yy+.11f*scale,z+.23f*scale,.105f*scale,.075f*scale,.08f*scale,floatArrayOf(.24f,.24f,.25f),10,16)
                    b.ellipsoid(xx+side*.135f*scale,yy+.19f*scale,z+.26f*scale,.14f*scale,.065f*scale,.10f*scale,white,10,16,120f)
                }
                b.ellipsoid(xx,yy-.035f*scale,z+.32f*scale,.07f*scale,.17f*scale,.16f*scale,white,12,18,120f)
                b.ellipsoid(xx,yy-.30f*scale,z+.36f*scale,.15f*scale,.025f*scale,.02f*scale,floatArrayOf(.29f,.26f,.27f),8,16)
            }
            for(i in -7..7)feather(b,floatArrayOf(x+i*.07f*scale,y-.8f*scale,z),floatArrayOf(x+i*.11f*scale,y-(2.1f+.7f*cos(i*.2f))*scale,z+.3f*scale),.15f*scale)
        }

        for(i in 0 until 12){val a=i*PI.toFloat()/6f;val p=floatArrayOf(x+cos(a)*1.16f*scale,y+.25f*scale+sin(a)*1.16f*scale,z-.08f*scale)
            val q=floatArrayOf(x+cos(a+.52f)*1.16f*scale,y+.25f*scale+sin(a+.52f)*1.16f*scale,z-.08f*scale)
            DreamShapes.tube(b,listOf(p,q),.045f*scale,bone,26f,7)
            if(i%2==0)eye(b,p[0],p[1],p[2],.105f*scale)
        }
    }
    fun field(b:DreamMesh){
        // The tall conical watcher sits with its knees drawn up, hands resting on long shins.
        run{val x=-16f;val z=-31f;val y=DreamNewDreams.hill(x,z)
            DreamSculpt.chair(b,x,y,z,upholstery=floatArrayOf(.24f,.19f,.19f))
            b.ellipsoid(x,y+2.9f,z,.45f,1.2f,.3f,black,14,20,121f)
            b.ellipsoid(x+.35f,y+4.2f,z+.25f,.37f,.48f,.31f,bone,14,20,120f)
            for(i in 0 until 18){val a=i*PI.toFloat()/9;val d=(i+1)*PI.toFloat()/9;b.triangle(floatArrayOf(x+.35f+.35f*cos(a),y+4.3f,z+.25f+.31f*sin(a)),floatArrayOf(x+.35f+.35f*cos(d),y+4.3f,z+.25f+.31f*sin(d)),floatArrayOf(x+1.25f,y+6.5f,z+.1f),black,121f)}
            for(side in floatArrayOf(-1f,1f)){
                val knee=floatArrayOf(x+side*1.9f,y+2.7f,z+1f);val ankle=floatArrayOf(x+side*2.7f,y+.18f,z+2.8f)
                DreamShapes.tube(b,listOf(floatArrayOf(x+side*.25f,y+1.9f,z),knee,ankle),.10f,black,121f,9)
                DreamShapes.tube(b,listOf(floatArrayOf(x+side*.4f,y+3.5f,z),floatArrayOf(x+side*1.1f,y+1.9f,z+1.1f),floatArrayOf(x+side*1.9f,y+1.5f,z+1.7f)),.075f,black,121f,9)
                for(f in 0..3)DreamShapes.tube(b,listOf(floatArrayOf(x+side*1.9f,y+1.5f,z+1.7f),floatArrayOf(x+side*(2f+f*.07f),y+1.1f,z+2f)),.025f,bone,120f,5)
                b.ellipsoid(x+.35f+side*.13f,y+4.23f,z+.54f,.095f,.12f,.04f,black,8,12,121f)
                b.ellipsoid(ankle[0],y+.12f,ankle[2]+.2f,.14f,.12f,.45f,black,8,12,121f)
            }
        }
        // One-eyed tree: hanging roots and the eye, rather than a ball canopy.
        run{val x=26f;val z=-92f;val y=DreamNewDreams.hill(x,z)
            for(i in 0..8){val a=i*2.399f;DreamShapes.tube(b,(0..9).map{j->val t=j/9f;floatArrayOf(x+cos(a)*(.5f+1.6f*t),y+t*12f,z+sin(a)*(.5f+1f*t))},.16f,floatArrayOf(.18f,.21f,.16f),119f,8)}
            eye(b,x,y+11f,z+1.1f,1.8f)
            for(i in 0..23){val a=i*2.399f;val r=1.3f+(i%4)*.2f;DreamShapes.tube(b,(0..12).map{j->val t=j/12f;floatArrayOf(x+cos(a)*r+.15f*sin(t*8+i),y+12f-t*(8f+(i%4)),z+sin(a)*r+.2f*sin(t*5f))},.035f,floatArrayOf(.13f,.19f,.14f),119f,5)}
        }
        // An eye-headed gentleman, seated with folded arms and actual lapels/tie.
        run{val x=13f;val z=-39f;val y=DreamNewDreams.hill(x,z)
            DreamSculpt.chair(b,x,y,z)
            b.ellipsoid(x,y+2.5f,z,.70f,.9f,.38f,black,14,20,121f)
            b.box(x,y+3.12f,z+.32f,.34f,.35f,.04f,white,42f)
            b.triangle(floatArrayOf(x-.1f,y+3.35f,z+.39f),floatArrayOf(x+.1f,y+3.35f,z+.39f),floatArrayOf(x,y+2.45f,z+.47f),floatArrayOf(.19f,.10f,.16f))
            eye(b,x,y+4.1f,z,.88f)
            for(side in floatArrayOf(-1f,1f)){
                b.triangle(floatArrayOf(x+side*.58f,y+3.2f,z+.3f),floatArrayOf(x+side*.15f,y+2.4f,z+.48f),floatArrayOf(x+side*.23f,y+3.25f,z+.44f),floatArrayOf(.13f,.13f,.15f),42f)
                DreamShapes.tube(b,listOf(floatArrayOf(x+side*.58f,y+3f,z),floatArrayOf(x+side*.8f,y+2.1f,z+.5f),floatArrayOf(x+side*.16f,y+2f,z+.9f)),.15f,black,121f,10)
                b.ellipsoid(x+side*.16f,y+2f,z+.92f,.21f,.10f,.15f,bone,10,16,120f)
                DreamShapes.tube(b,listOf(floatArrayOf(x+side*.3f,y+1.8f,z),floatArrayOf(x+side*.45f,y+1.7f,z+1.1f),floatArrayOf(x+side*.45f,y+.22f,z+1.4f)),.2f,black,121f,10)
                b.ellipsoid(x+side*.45f,y+.14f,z+1.65f,.24f,.15f,.5f,black,10,16,121f)
            }
        }
        run{val x=-34f;val z=-65f;val y=DreamNewDreams.hill(x,z)
            DreamSculpt.cylinder(b,x,y,z,2.9f,.5f,bone,26f,24);angel(b,x,y+3.1f,z,1.1f,true)
        }
    }
    fun blocked(x:Float,z:Float)=restingPositions.any{hypot(x-it.x,z-it.z)<(if(it.x==26f)2.7f else 3.2f)}
}
