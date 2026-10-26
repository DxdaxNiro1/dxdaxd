package com.dreamcore.game

import kotlin.math.*

/** Authored 3-D silhouettes: cloth lofts, sculpted masks, individual curved feathers and skeletal hands. */
object DreamEntities32 {
    data class Presence(val model:String,val x:Float,val y:Float,val z:Float,val scale:Float=1f,val yaw:Float=0f,val phase:Float=0f)
    val models=listOf("choir","mask","witness","crimson","antler","gold","star","observatory","mushroom","water-eye","archivist")
    private val catalogue=HashMap<String,List<Presence>>()
    fun placements(id:String):List<Presence> = catalogue.getOrPut(id){authored(id)}
    private fun authored(id:String):List<Presence> = when(id){
        DreamHorror32.ATRIUM->listOf(Presence("choir",-7f,2f,-39f,1.4f),Presence("choir",8f,5f,-55f,1f,phase=2f))
        DreamHorror32.GARDEN->listOf(Presence("observatory",0f,0f,-128f,2.7f),Presence("star",8f,0f,-52f,1.25f))
        DreamHorror32.WITNESSES->listOf(Presence("mask",0f,1f,-43f,1.3f),Presence("witness",0f,0f,-131f,4.8f,phase=4f))
        DreamHorror32.FOREST->listOf(Presence("crimson",-16f,3f,-65f,2f),Presence("antler",14f,0f,-137f,2.4f))
        DreamHorror32.HERBARIUM->listOf(Presence("mushroom",12f,0f,-48f,3f),Presence("mushroom",-17f,0f,-93f,2.3f,phase=3f))
        DreamHorror32.HOUSE->listOf(Presence("gold",-62f,-13f,-88f,5f))
        DreamHorror32.POOL->listOf(Presence("water-eye",-14f,-1.5f,-69f,2.5f),Presence("water-eye",24f,-1f,-127f,4f,phase=3f))
        DreamNewDreams.EYES->listOf(Presence("star",-30f,0f,-114f,1.9f))
        DreamReferences.HALL->listOf(Presence("mask",-1.2f,.3f,-133f,.9f))
        DreamReferences.ATRIUM->listOf(Presence("choir",11f,6f,-151f,1.4f))
        DreamJourney.DARK->listOf(Presence("antler",-39f,0f,-41f,2.6f))
        DreamHorizons.DARK->listOf(Presence("crimson",17f,7f,-165f,2.6f))
        DreamVigilia27.DARK_THREE->listOf(Presence("gold",-17f,5f,-154f,2.5f))
        DreamReferences.OFFICE->listOf(Presence("star",12f,0f,-34f,1.1f))
        DreamReferences.CINEMA->listOf(Presence("choir",-12f,0f,-69f,1.15f))
        DreamReferences.LAKE->listOf(Presence("water-eye",39f,-2f,-140f,6f))
        else->emptyList()
    }
    fun model(name:String,lod:Int=0):DreamWorlds.Scene{
        require(name in models)
        val b=DreamMesh();val pi=PI.toFloat();val segments=if(lod==0)40 else 20
        fun v(x:Float,y:Float,z:Float)=floatArrayOf(x,y,z)
        fun unit(p:FloatArray):FloatArray{val d=sqrt(p.sumOf{(it*it).toDouble()}).toFloat().coerceAtLeast(.0001f);return FloatArray(3){p[it]/d}}
        fun patch(n:Int,m:Int,tile:Int,part:Float=0f,uv:((Float,Float)->FloatArray)?=null,point:(Float,Float)->FloatArray){
            fun normal(u:Float,w:Float):FloatArray{val a=point(u+.0005f,w);val c=point(u-.0005f,w);val d=point(u,w+.0005f);val e=point(u,w-.0005f);val x=FloatArray(3){a[it]-c[it]};val y=FloatArray(3){d[it]-e[it]};return unit(v(x[1]*y[2]-x[2]*y[1],x[2]*y[0]-x[0]*y[2],x[0]*y[1]-x[1]*y[0]))}
            fun tri(a:FloatArray,c:FloatArray,d:FloatArray){val p=point(a[0],a[1]);val q=point(c[0],c[1]);val r=point(d[0],d[1]);b.mappedTriangle(p,q,r,uv?.invoke(a[0],a[1])?:v(a[0],a[1],tile.toFloat()),uv?.invoke(c[0],c[1])?:v(c[0],c[1],tile.toFloat()),uv?.invoke(d[0],d[1])?:v(d[0],d[1],tile.toFloat()),normal(a[0],a[1]),normal(c[0],c[1]),normal(d[0],d[1]),part)}
            for(i in 0 until n)for(j in 0 until m){val a=v(i.toFloat()/n,j.toFloat()/m,0f);val c=v((i+1f)/n,j.toFloat()/m,0f);val d=v((i+1f)/n,(j+1f)/m,0f);val e=v(i.toFloat()/n,(j+1f)/m,0f);tri(a,c,d);tri(a,d,e)}
        }
        fun sphere(x:Float,y:Float,z:Float,rx:Float,ry:Float,rz:Float,tile:Int,part:Float=0f){val n=if(rx<.36f)minOf(segments,20)else segments;patch(n,n/2,tile,part){u,t->val a=u*2*pi;val h=t*pi;v(x+rx*sin(h)*sin(a),y+ry*cos(h),z+rz*sin(h)*cos(a))}}
        fun loft(y0:Float,height:Float,width:(Float)->Float,depth:Float,tile:Int,part:Float=0f,offset:Float=0f){patch(segments,if(lod==0)18 else 10,tile,part){u,t->val a=u*2*pi;val w=width(t);val folds=(sin(a*17f+t*1.1f)*.045f+sin(a*9f-t*.7f)*.035f)*(1f-t)*w;v(offset+sin(a)*(w+folds),y0+t*height,cos(a)*(w*depth+folds)+.10f*sin(t*pi))}}
        fun tube(points:List<FloatArray>,radius:Float,tile:Int){for(i in 0 until points.lastIndex){val a=points[i];val c=points[i+1];patch(8,1,tile){u,t->val q=u*2*pi;v(a[0]+(c[0]-a[0])*t+radius*cos(q),a[1]+(c[1]-a[1])*t,a[2]+(c[2]-a[2])*t+radius*sin(q))}}}
        fun eye(x:Float,y:Float,z:Float,r:Float){sphere(x,y,z,r,r*.82f,r*.6f,0);patch(if(r<.5f)segments/4 else segments/2,if(r<.5f)4 else 8,3,4f){u,t->val a=(u-.5f)*pi*.85f;val h=(t-.5f)*pi*.62f;v(x+sin(a)*cos(h)*r*.89f,y+sin(h)*r*.72f,z+cos(a)*cos(h)*r*.67f)}}
        fun face(tile:Int,y:Float=4.8f){
            // Front-facing relief: cheekbones, recessed orbits, a projecting nose and lips.
            patch(segments,segments/2,tile,8f,uv={u,t->v(.5f+cos(u*2*pi)*t*.49f,.5f-sin(u*2*pi)*t*.49f,tile.toFloat())}){u,t->val x=cos(u*2*pi)*t*.54f;val h=sin(u*2*pi)*t*.74f;val ellipse=max(0f,1f-x*x/ .34f-h*h/.75f)
                fun g(cx:Float,cy:Float,sx:Float,sy:Float)=exp(-((x-cx)/sx).pow(2)-((h-cy)/sy).pow(2))
                val relief=.19f*sqrt(ellipse)+.27f*g(0f,-.03f,.105f,.22f)-.10f*(g(-.20f,.17f,.13f,.10f)+g(.20f,.17f,.13f,.10f))+.055f*g(0f,-.34f,.23f,.065f)
                v(x*(.78f+.22f*sqrt(ellipse)),y+h,.16f+relief)
            }
            sphere(0f,y,-.05f,.51f,.73f,.36f,if(tile==15)0 else 12,8f)
            for(side in floatArrayOf(-1f,1f))eye(side*.19f,y+.18f,.37f,.095f)
        }
        fun wing(side:Float,black:Boolean=false,gold:Boolean=false,eyes:Boolean=false){val tile=if(gold)14 else if(black)13 else 2;val part=if(side<0f)2f else 3f
            val rows=if(lod==0)3 else 2;val feathers=if(lod==0)19 else 11
            for(row in 0 until rows)for(i in 0 until feathers){val q=i.toFloat()/(feathers-1);val rootX=side*(.36f+q*2.25f);val rootY=3.65f+sin(q*pi*.78f)*1.3f-row*.22f;val length=.48f+q*1.05f+row*.15f;val spread=.13f+q*.10f
                patch(4,if(lod==0)8 else 4,tile,part){u,t->val w=sin(t*pi).coerceAtLeast(0f).pow(.55f)*spread;v(rootX+side*(t*(.25f+q*.68f)+(u-.5f)*w*2f),rootY-t*length,.04f+row*.09f+sin(t*pi)*.13f-((u-.5f)*(u-.5f))*.20f)}
                if(eyes&&i%4==1&&row==1)eye(rootX+side*.22f,rootY-.37f,.28f,.17f)
            }
        }
        fun robe(tile:Int=0){loft(.04f,3.9f,{t->.46f+.62f*(1f-t).pow(2)+.19f*exp(-((t-.80f)*8).pow(2))},.70f,tile,1f)
            for(s in floatArrayOf(-1f,1f)){patch(segments/2,12,tile,1f){u,t->val a=u*2*pi;val r=.19f+.22f*(1f-t);v(s*(.52f+.53f*(1f-t))+r*sin(a),2.25f+t*1.4f,r*cos(a))}
                for(f in 0..4){val x=s*(1.02f+(f-2)*.055f);tube(listOf(v(x,2.30f,.08f),v(x+s*.05f,2.02f,.12f),v(x+s*.02f,1.88f+(f%2)*.05f,.15f)),.024f,12)}
            }
        }
        fun halo(){patch(48,6,14){u,t->val a=u*2*pi;val h=t*2*pi;v(cos(a)*(.70f+.035f*cos(h)),5.92f+.035f*sin(h),sin(a)*(.70f+.035f*cos(h)))}}
        when(name){
            "water-eye"->eye(0f,1.4f,0f,1.6f)
            "observatory"->{sphere(0f,9.3f,0f,3.9f,3.6f,3.5f,6);eye(0f,9.4f,3.05f,2.1f);for(s in floatArrayOf(-1f,1f))for(d in floatArrayOf(-1f,1f)){val x=s*2.7f;val z=d*2.4f;tube(listOf(v(x,0f,z),v(x*.8f,8f,z*.8f)),.09f,6);for(h in 0..3){tube(listOf(v(x,h*2f,z),v(-x,(h+1)*2f,z)),.055f,6)}}}
            "mushroom"->{loft(0f,2.8f,{t->.26f+.13f*sin(t*pi)},.8f,0);patch(segments,16,7){u,t->val a=u*2*pi;val r=t*1.8f;v(sin(a)*r,2.65f+1.2f*cos(t*pi*.5f),cos(a)*r)};for(i in 0..4){val a=i*2*pi/5;eye(sin(a)*1.15f,2.94f,cos(a)*1.15f,.32f)}}
            "archivist"->{robe(4);face(1);b.ellipsoid(.28f,3.55f,.39f,.11f,.11f,.035f,floatArrayOf(.4f,.6f,14f),5,10,0f)}
            "star"->{loft(1.1f,2.4f,{t->.32f+.36f*sin(t*pi*.8f)},.52f,9);for(s in floatArrayOf(-1f,1f)){loft(0f,1.25f,{.17f},.8f,9,offset=s*.25f);tube(listOf(v(s*.62f,3.2f,0f),v(s*.83f,2.3f,.04f),v(s*.82f,1.2f,.05f)),.16f,9)}
                // Five lobes loft out of a thick sculpted star, rather than a five-point billboard.
                patch(segments*2,12,8){u,t->val a=u*2*pi+pi*.5f;val radius=(.65f+.48f*(.5f+.5f*cos(5f*(a-pi*.5f))))*t;v(cos(a)*radius,4.35f+sin(a)*radius,.19f+.26f*(1f-t*t))};eye(0f,4.4f,.40f,.42f)
                patch(1,1,0){u,t->v((u-.5f)*.38f,3.55f-t*.65f,.38f)};patch(1,1,4){u,t->v((u-.5f)*(.045f+.09f*t),3.48f-t*.67f,.40f)}
            }
            else->{val dark=name=="crimson"||name=="antler";robe(if(dark)4 else 0);face(if(name=="mask")15 else if(name=="witness"||dark)12 else 1)
                if(name=="crimson"){
                    for(s in floatArrayOf(-1f,1f)){for(i in 0..4){val a=i*pi*.20f;tube(listOf(v(s*.45f,3.8f,0f),v(s*(1.1f+cos(a)*1.5f),4.7f-sin(a)*2.4f,-.12f)),.035f,4)}
                        patch(18,12,5,if(s<0)2f else 3f){u,t->val a=u*pi*.8f;val r=.3f+t*(2.7f+.20f*sin(u*pi*8));v(s*(.45f+r*cos(a*.70f)),3.8f+r*.60f-sin(a)*r,-.10f+.14f*sin(t*pi))}
                        tube(listOf(v(s*.32f,5.35f,0f),v(s*.63f,5.8f,0f),v(s*.51f,6.2f,.12f)),.10f,4)
                    }
                }else{wing(-1f,name=="antler",name=="gold",name=="witness");wing(1f,name=="antler",name=="gold",name=="witness")}
                if(name=="choir"||name=="gold")halo()
                if(name=="antler")for(s in floatArrayOf(-1f,1f)){tube(listOf(v(s*.31f,5.2f,-.1f),v(s*.70f,5.9f,0f),v(s*1.2f,6.5f,-.1f)),.06f,4);for(i in 0..2)tube(listOf(v(s*(.60f+i*.15f),5.8f+i*.2f,0f),v(s*(.9f+i*.20f),6.35f+i*.25f,.1f)),.035f,4)}
                if(name=="witness"){for(i in 0..4){sphere((i%2-.5f)*.38f,1.25f+i*.43f,.58f,.23f,.31f,.19f,12,6f)};patch(16,16,5,6f){u,t->v((u-.5f)*.62f,.7f+t*2.8f,.57f+.1f*sin(t*pi))}}
            }
        }
        return b.finish()
    }
}
