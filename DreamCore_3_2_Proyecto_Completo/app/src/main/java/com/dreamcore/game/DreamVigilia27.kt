package com.dreamcore.game

import kotlin.math.*

/** Two independent, traversable dark landscapes and a slide-free suspended sanctuary. */
object DreamVigilia27 {
    fun releaseGeometry(){cachedId="";cached=null}
    const val DARK_THREE="dark_03"
    const val DARK_FOUR="dark_04"
    const val SKY_ISLAND="dream_sanctuary_clouds"
    val ids=listOf(DARK_THREE,DARK_FOUR,SKY_ISLAND)
    fun contains(id:String)=id in ids
    fun dark(id:String)=id==DreamJourney.DARK || id==DreamHorizons.DARK || id==DARK_THREE || id==DARK_FOUR
    private val lunarPairs=arrayOf(0 to 6,5 to 3,1 to 4,2 to 7)
    fun moons(id:String)=lunarPairs[when(id){DreamJourney.DARK->0;DreamHorizons.DARK->1;DARK_THREE->2;else->3}]
    fun title(id:String)=when(id){DARK_THREE->"El pueblo de la luna escarlata";DARK_FOUR->"El castillo de la sonrisa pálida";else->"El santuario del ojo entre nubes"}
    fun height(id:String,x:Float,z:Float)=if(id==SKY_ISLAND)72f+.20f*sin(x*.13f)*sin((z+45f)*.12f)else if(abs(x)<6f)0f else .35f*sin(x*.06f)*sin(z*.04f)
    fun spawn(id:String)=if(id==SKY_ISLAND)DreamJourney.Point(0f,-12f)else DreamJourney.Point(0f,20f)
    fun gates(id:String)=if(id==SKY_ISLAND)listOf(DreamJourney.Point(0f,-8f))else listOf(DreamJourney.Point(0f,28f),DreamJourney.Point(0f,-286f))
    fun portalAt(id:String,x:Float,z:Float):String?{
        val g=gates(id);if(hypot(x-g[0].x,z-g[0].z)<2.5f)return when(id){DARK_THREE->DreamHorizons.DARK;DARK_FOUR->DARK_THREE;else->DreamPocket.RETURN}
        if(g.size>1 && hypot(x-g[1].x,z-g[1].z)<2.5f)return if(id==DARK_THREE)DARK_FOUR else DreamWorlds.STATION
        return null
    }
    fun canWalk(id:String,x:Float,z:Float):Boolean{
        if(id==SKY_ISLAND){if(hypot(x,(z+45f)*.94f)>35f)return false
            if(abs(abs(x)-20f)<1f && abs(z+52f)<1f)return false
            // Open doorway and furnished interior; walls remain solid.
            if(z in -67f..-47f && abs(abs(x)-8.4f)<.7f)return false
            if(abs(z+67f)<.7f && abs(x)<8.8f)return false
            if(abs(z+47f)<.7f && abs(x) in 1.8f..8.8f)return false
            return !(abs(x-5f)<2.2f && abs(z+61f)<2.1f)
        }
        if(x !in -105f..105f || z !in -300f..36f)return false
        if(abs(x)<8f)return true
        if(id==DARK_THREE)return !(listOf(-34f,34f).any{abs(x-it)<9f} && (-3 downTo -243 step 48).any{abs(z-it)<8f})
        return !(abs(abs(x)-34f)<4f && z in -246f..-204f)
    }
    private var cachedId="";private var cached:DreamWorlds.Scene?=null
    fun create(id:String):DreamWorlds.Scene{
        if(cachedId==id)return cached!!
        val b=DreamMesh();val stone=floatArrayOf(.31f,.34f,.41f);val snow=floatArrayOf(.66f,.70f,.79f);val grass=floatArrayOf(.46f,.63f,.31f)
        if(id==SKY_ISLAND){
            val n=96
            for(i in 0 until n){val a=i*2f*PI.toFloat()/n;val d=(i+1)*2f*PI.toFloat()/n
                fun p(t:Float,r:Float,y:Float)=floatArrayOf(cos(t)*r,y,-45f+sin(t)*r/ .94f)
                val pa=p(a,37f,72f);val pd=p(d,37f,72f);pa[1]=height(id,pa[0],pa[2]);pd[1]=height(id,pd[0],pd[2])
                b.triangle(floatArrayOf(0f,72f,-45f),pd,pa,grass,4f)
                b.quad(pa,p(a,28f,63f),p(d,28f,63f),pd,grass,4f)
                b.triangle(p(a,28f,63f),floatArrayOf(0f,44f,-45f),p(d,28f,63f),stone,44f)
            }
            val pink=floatArrayOf(.91f,.64f,.71f);val cream=floatArrayOf(.93f,.86f,.83f)
            for(side in floatArrayOf(-1f,1f)){b.box(side*8.4f,77f,-57f,.35f,5f,10f,pink,0f);b.box(side*5.3f,77f,-47f,3.3f,5f,.35f,pink,0f)}
            b.box(0f,77f,-67f,8.7f,5f,.35f,pink,0f);b.box(0f,81f,-47f,1.8f,1f,.35f,pink,0f)
            b.box(0f,72.08f,-57f,8f,.08f,10f,cream,43f)
            val roof=floatArrayOf(.85f,.45f,.56f)
            for(side in floatArrayOf(-1f,1f)){
                val pa=floatArrayOf(side*9.2f,82f,-46f);val pc=floatArrayOf(side*9.2f,82f,-68f);val pd=floatArrayOf(0f,88f,-68f);val pe=floatArrayOf(0f,88f,-46f)
                if(side>0)b.quad(pa,pc,pd,pe,roof,112f)else b.quad(pe,pd,pc,pa,roof,112f)
            }
            for(side in floatArrayOf(-1f,1f)){
                val x=side*5.1f
                DreamSculpt.rounded(b,x,77f,-46.55f,1.35f,1.85f,.08f,.16f,cream,26f)
                b.box(x,77f,-46.43f,1.12f,1.61f,.025f,floatArrayOf(.40f,.68f,.79f),34f)
                b.box(x,77f,-46.36f,.055f,1.65f,.045f,cream,26f);b.box(x,77f,-46.36f,1.14f,.055f,.045f,cream,26f)
            }
            b.ellipsoid(0f,83.4f,-46.6f,1.1f,1.1f,.08f,cream,12,24,26f)
            b.ellipsoid(0f,83.4f,-46.49f,.88f,.88f,.03f,floatArrayOf(.44f,.69f,.77f),12,24,34f)
            b.box(0f,83.4f,-46.43f,.055f,.89f,.04f,cream,26f);b.box(0f,83.4f,-46.43f,.89f,.055f,.04f,cream,26f)
            b.box(-5f,85f,-60f,.6f,3.1f,.65f,pink,0f)
            for(zz in floatArrayOf(-47f,-67f))b.triangle(floatArrayOf(-8.8f,82f,zz),floatArrayOf(8.8f,82f,zz),floatArrayOf(0f,88f,zz),pink,0f)
            for(side in floatArrayOf(-1f,1f))for(zz in floatArrayOf(-53f,-61f)){b.box(side*8.82f,78f,zz,.025f,1.5f,1.2f,floatArrayOf(.48f,.72f,.82f),31f);b.box(side*8.85f,78f,zz,.04f,1.55f,.07f,cream,26f)}
            DreamShapes.bed(b,5f,72.15f,-61f);DreamSculpt.chair(b,-5f,72.15f,-61f);DreamSculpt.chair(b,-4f,72.15f,-52f)
            DreamSculpt.rounded(b,-4f,73.5f,-56f,1.6f,.12f,1.1f,.15f,cream,26f);DreamShapes.cup(b,-4f,73.65f,-56f)
            for(i in 0..8){val z=-14f-i*3.8f;b.ellipsoid(sin(i*.6f)*1.2f,height(id,0f,z)+.025f,z,1.7f,.04f,1.4f,cream,5,12,25f)}
            for(i in 0..66){val a=i*2.399f;val rr=19f+(i%9)*1.5f;val x=cos(a)*rr;val z=-45f+sin(a)*rr;val y=height(id,x,z)
                if(i<6)DreamShapes.plant(b,x,y,z,.45f+(i%3)*.1f)
                else{DreamShapes.tube(b,listOf(floatArrayOf(x,y,z),floatArrayOf(x+.1f,y+.65f,z)),.025f,grass,0f,5)
                    for(k in 0..4){val a=k*2f*PI.toFloat()/5;b.ellipsoid(x+cos(a)*.15f,y+.66f,z+sin(a)*.15f,.14f,.05f,.14f,cream,4,8,26f)}
                }}
            for(side in floatArrayOf(-1f,1f)){
                val x=side*20f;DreamBotany26.tree(b,x,height(id,x,-52f),-52f,1.4f,27+(side*3).toInt())
                DreamBotany26.shrub(b,side*11f,height(id,side*11f,-50f),-50f,1.3f,7)
            }
            for(i in 0 until 420){val a=i*2.399f;val rr=sqrt((i+.5f)/420f)*34f;val x=cos(a)*rr;val z=-45f+sin(a)*rr
                if(!(abs(x)<10f&&z in -69f..-44f) && !(abs(x)<2.7f&&z> -48f))DreamBotany26.tuft(b,x,height(id,x,z),z,i+2700)
            }
            // The observer is above and well outside the playable island. Its wings span ~150m.
            DreamGuardians27.angel(b,52f,222f,-245f,19f)
        }else{
            for(x in -150 until 150 step 6)for(z in -330 until 66 step 6){fun p(xx:Float,zz:Float)=floatArrayOf(xx,height(id,xx,zz)-.025f,zz)
                b.quad(p(x.toFloat(),z.toFloat()),p(x.toFloat(),z+6f),p(x+6f,z+6f),p(x+6f,z.toFloat()),if(id==DARK_THREE)snow else stone,if(id==DARK_THREE)44f else 25f)}
            for(i in 0 until 77){val a=i*2.399f;val x=cos(a)*(43f+(i%7)*12f);val z=28f-(i*17%315);val y=height(id,x,z)
                DreamSculpt.cylinder(b,x,y,z,.4f,10f,floatArrayOf(.16f,.13f,.15f),43f,9)
                if(id==DARK_THREE)for(j in 0..4)b.ellipsoid(x,y+6f+j*1.7f,z,4.4f-j*.75f,2f,4.4f-j*.75f,if(j%2==0)snow else floatArrayOf(.12f,.18f,.19f),6,12,38f)
                else for(side in floatArrayOf(-1f,1f))DreamShapes.tube(b,listOf(floatArrayOf(x,y+5f,z),floatArrayOf(x+side*2f,y+9f,z+.8f),floatArrayOf(x+side*3f,y+12f,z+.4f)),.15f,stone,43f,7)
            }
            if(id==DARK_THREE){for(x in floatArrayOf(-34f,34f))for(z in -3 downTo -243 step 48){DreamSculpt.house(b,x,0f,z.toFloat(),floatArrayOf(.36f,.25f,.28f),1.45f)
                    b.box(x,2.3f,z+8.1f,.8f,1f,.04f,floatArrayOf(1f,.23f,.06f),24f)}
                DreamSculpt.house(b,-62f,0f,-236f,floatArrayOf(.43f,.32f,.36f),2f)
                // Chapel entrance stays open on the central path.
                for(side in floatArrayOf(-1f,1f))DreamSculpt.cylinder(b,side*14f,0f,-243f,2.3f,24f,stone,44f,14)
            }else{
                // Broken towers and arches surround an accessible drowned courtyard.
                for(x in floatArrayOf(-34f,34f))for(z in floatArrayOf(-210f,-240f)){
                    DreamSculpt.cylinder(b,x,0f,z,3.6f,31f,stone,44f,20)
                    for(i in 0..7){val a=i*PI.toFloat()/4;b.box(x+cos(a)*3.5f,32.2f,z+sin(a)*3.5f,.6f,1.2f,.6f,stone,44f)}
                }
                for(side in floatArrayOf(-1f,1f))DreamShapes.tube(b,(0..32).map{val a=it*PI.toFloat()/32;floatArrayOf(side*17f+cos(a)*14f,18f+sin(a)*15f,-232f)},.9f,stone,44f,10)
                for(side in floatArrayOf(-1f,1f))for(i in 0..8){val z=-60f-i*20f;b.ellipsoid(side*24f,-.035f,z,14f,.025f,8f,floatArrayOf(.12f,.21f,.29f),5,24,1f)}
                for(i in 0..15){val x=if(i%2==0)-12f else 12f;val z=-20f-i*16f;b.box(x,1.4f,z,.45f,1.4f,.45f,stone,44f);b.ellipsoid(x,3f,z,.19f,.30f,.19f,floatArrayOf(.78f,.19f,.12f),7,10,24f)}
            }
            for(z in 24 downTo -282 step 9)b.ellipsoid(0f,.015f,z.toFloat(),4.8f,.025f,4.2f,stone,4,12,25f)
        }
        for(p in gates(id)){val y=height(id,p.x,p.z);for(side in floatArrayOf(-1f,1f))DreamSculpt.rounded(b,p.x+side*1.8f,y+1.8f,p.z,.2f,1.8f,.2f,.1f,stone,26f)
            b.box(p.x,y+3.7f,p.z,2.0f,.12f,.2f,stone,26f);b.box(p.x,y+1.7f,p.z,1.5f,1.7f,.025f,floatArrayOf(.66f,.39f,.73f),29f)}
        cachedId=id;cached=b.finish();return cached!!
    }
}
