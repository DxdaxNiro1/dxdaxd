package com.dreamcore.game

import kotlin.math.*

/** Connected seeded room graph with loops. Steady lamps mark edges that approach the exit. */
object DreamBackrooms {
    const val ID="backrooms_01"
    const val N=7
    private var seed=Int.MIN_VALUE
    private val links=Array(N*N){mutableSetOf<Int>()}
    private val distance=IntArray(N*N)
    private var scene:DreamWorlds.Scene?=null
    fun configure(value:Int){if(value==seed)return;seed=value;scene=null
        links.forEach{it.clear()};val rng=java.util.Random(value.toLong()*31+93);val seen=BooleanArray(N*N);val stack=java.util.ArrayDeque<Int>();stack.add(3);seen[3]=true
        while(stack.isNotEmpty()){val a=stack.peekLast();val c=a%N;val r=a/N
            val choices=listOf(a-1,a+1,a-N,a+N).filter{b->b in links.indices && abs(b%N-c)+abs(b/N-r)==1 && !seen[b]}
            if(choices.isEmpty())stack.removeLast()else{val b=choices[rng.nextInt(choices.size)];links[a].add(b);links[b].add(a);seen[b]=true;stack.add(b)}
        }
        repeat(18){val a=rng.nextInt(N*N);val b=if(rng.nextBoolean())a+1 else a+N;if(b in links.indices&&abs(a%N-b%N)+abs(a/N-b/N)==1){links[a].add(b);links[b].add(a)}}
        distance.fill(1000);distance[48]=0;val q=java.util.ArrayDeque<Int>();q.add(48)
        while(q.isNotEmpty()){val a=q.removeFirst();for(b in links[a])if(distance[b]>distance[a]+1){distance[b]=distance[a]+1;q.add(b)}}
    }
    private fun ready(){if(seed==Int.MIN_VALUE)configure(7319)}
    fun cell(x:Float,z:Float):Int?{val c=floor((x+42f)/12f).toInt();val r=floor((6f-z)/12f).toInt();return if(c in 0 until N&&r in 0 until N)r*N+c else null}
    fun centre(a:Int)=DreamJourney.Point((a%N-3)*12f,-(a/N)*12f)
    fun neighbors(a:Int):Set<Int>{ready();return links[a].toSet()}
    fun remaining(a:Int):Int{ready();return distance[a]}
    fun canWalk(x:Float,z:Float):Boolean{
        ready();val a=cell(x,z)?:return false;val p=centre(a);val dx=x-p.x;val dz=z-p.z
        if(a%11==0&&a!=3&&abs(x-(p.x+3.8f))<1.32f&&abs(z-(p.z-3.8f))<1.02f)return false
        if(abs(dx)<5.45f&&abs(dz)<5.45f)return true
        if(abs(dx)>5.45f){val b=a+if(dx>0)1 else -1;if(b !in links[a]||abs(dz)>1.4f)return false}
        if(abs(dz)>5.45f){val b=a+if(dz>0)-N else N;if(b !in links[a]||abs(dx)>1.4f)return false}
        return true
    }
    fun guide(x:Float,z:Float):DreamJourney.Point?{ready();val a=cell(x,z)?:return null;return links[a].filter{distance[it]<distance[a]}.minByOrNull{distance[it]}?.let{val p=centre(a);val t=centre(it);DreamJourney.Point((p.x+t.x)*.5f,(p.z+t.z)*.5f)}}
    fun create():DreamWorlds.Scene{
        ready();scene?.let{return it};val b=DreamMesh();val yellow=floatArrayOf(.70f,.65f,.35f);val floor=floatArrayOf(.42f,.37f,.19f)
        for(a in links.indices){val p=centre(a)
            b.box(p.x,-.12f,p.z,6f,.12f,6f,floor,42f);b.box(p.x,3.65f,p.z,6f,.10f,6f,floatArrayOf(.69f,.68f,.50f),0f)
            b.box(p.x,3.52f,p.z,1.3f,.04f,.45f,floatArrayOf(.91f,.88f,.61f),24f)
            // Ceiling seams and curved column edges give close details without tiny floor geometry.
            for(s in floatArrayOf(-1f,1f))b.box(p.x+s*5.8f,3.51f,p.z,.025f,.025f,5.8f,floatArrayOf(.36f,.35f,.25f),15f)
            for((dx,dz)in listOf(1 to 0,0 to 1,-1 to 0,0 to -1)){
                val neighbor=a+dx+(-dz)*N
                val boundary=(dx<0&&a%N==0)||(dz>0&&a/N==0)
                if(dx<0&&!boundary || dz>0&&!boundary)continue
                val open=neighbor in links[a];val xx=p.x+dx*6f;val zz=p.z+dz*6f
                fun wall(shift:Float,half:Float){b.box(xx+(if(dx==0)shift else 0f),1.8f,zz+(if(dx!=0)shift else 0f),if(dx==0)half else .16f,1.8f,if(dx==0).16f else half,yellow,104f)
                    b.box(xx+(if(dx==0)shift else 0f),.13f,zz+(if(dx!=0)shift else 0f),if(dx==0)half else .22f,.13f,if(dx==0).22f else half,floatArrayOf(.55f,.51f,.29f),43f)}
                if(open){wall(-3.85f,2.15f);wall(3.85f,2.15f);b.box(xx,3.2f,zz,if(dx==0)1.7f else .16f,.4f,if(dx==0).16f else 1.7f,yellow,104f)
                    for(from in listOf(a,neighbor)){val near=centre(from);val to=if(from==a)neighbor else a;val stable=distance[to]<distance[from]
                        b.box((xx+near.x)*.5f,3.45f,(zz+near.z)*.5f,if(dx==0).85f else .25f,.035f,if(dx==0).25f else .85f,if(stable)floatArrayOf(.93f,.90f,.70f)else floatArrayOf(.41f,.45f,.36f),if(stable)24f else 106f)
                    }
                }else wall(0f,6f)
            }
            if(a%11==0&&a!=3){DreamSculpt.rounded(b,p.x+3.8f,.48f,p.z-3.8f,1f,.48f,.7f,.09f,floatArrayOf(.54f,.49f,.30f),42f)}
        }
        // A door out is always available at the entry; the far exit records the discovery.
        for(p in listOf(DreamJourney.Point(0f,4f),DreamJourney.Point(36f,-73f))){b.box(p.x,1.45f,p.z,1f,1.45f,.035f,floatArrayOf(.57f,.66f,.55f),29f);for(s in floatArrayOf(-1f,1f))b.box(p.x+s*1.2f,1.55f,p.z,.12f,1.55f,.12f,yellow,43f)}
        scene=b.finish();return scene!!
    }
}
