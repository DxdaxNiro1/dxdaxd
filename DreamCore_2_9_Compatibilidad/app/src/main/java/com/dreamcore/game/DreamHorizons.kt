package com.dreamcore.game

import kotlin.math.*

/** Distinct worlds, each built from the same footprints used by collision. */
object DreamHorizons {
    fun releaseGeometry(){cached="";mesh=null}
    const val CELESTIAL="dream_14"
    const val CITY="dream_15"
    const val DARK="dark_02"
    const val TOWER="pocket_tower"
    const val BED="pocket_cloudbed"
    val ids=listOf(CELESTIAL,CITY,DARK,TOWER,BED)
    data class Rect(val x:Float,val z:Float,val sx:Float,val sz:Float){fun hits(px:Float,pz:Float)=abs(px-x)<sx+.33f && abs(pz-z)<sz+.33f}
    private var cached="";private var mesh:DreamWorlds.Scene?=null
    private val blocks=ArrayList<Rect>()
    fun contains(id:String)=id in ids
    fun title(id:String)=when(id){CELESTIAL->"El tobogán de los planetas";CITY->"La ciudad del último recreo";DARK->"El jardín de las coronas vacías";TOWER->"La torre que viste desde lejos";else->"Una cama al borde del universo"}
    fun curve(z:Float)=48f*sin((z-85f)*.012f)
    fun height(id:String,x:Float,z:Float):Float=when(id){CELESTIAL->8f+2.6f*sin(z*.008f);TOWER->if(z< -35f)min(22f,(-z-35f)*.20f)else 0f;else->0f}
    fun entry(id:String)=when(id){CELESTIAL->DreamJourney.Point(curve(90f),90f);DARK->DreamJourney.Point(0f,30f);else->DreamJourney.Point(0f,90f)}
    fun exit(id:String)=when(id){CELESTIAL->DreamJourney.Point(curve(-480f),-480f);DARK->DreamJourney.Point(0f,-330f);else->DreamJourney.Point(0f,-480f)}
    fun indoor(id:String,x:Float,z:Float)=when(id){BED,TOWER->true;CITY->(abs(x)<44f && z in -210f..-120f)||(x in -200f..-80f && z in -370f..-260f);else->false}
    fun canWalk(id:String,x:Float,z:Float):Boolean {
        create(id)
        val supported=when(id){
            CELESTIAL->z in -495f..104f && (abs(x-curve(z))<6.5f || listOf(65f,-115f,-290f,-465f).any{hypot(x-curve(it),z-it)<24f})
            CITY->x in -235f..235f && z in -510f..110f
            DARK->z in -347f..45f && (abs(x)<10f || listOf(-40f,-150f,-285f).any{hypot(x,z-it)<48f})
            TOWER->abs(x)<10.5f && z in -151f..11f
            BED->x in -8.5f..8.5f && z in -21f..11f
            else->false
        }
        return supported && blocks.none{it.hits(x,z)}
    }
    fun create(id:String):DreamWorlds.Scene {
        if(cached==id && mesh!=null)return mesh!!
        blocks.clear();val b=DreamMesh();val c=DreamShapes.ivory;val rose=DreamShapes.rose;val blue=floatArrayOf(.31f,.52f,.65f)
        fun solid(x:Float,y:Float,z:Float,sx:Float,sy:Float,sz:Float,tint:FloatArray=c,k:Float=26f){b.box(x,y,z,sx,sy,sz,tint,k);blocks.add(Rect(x,z,sx,sz))}
        fun floor(x:Float,z:Float,sx:Float,sz:Float,k:Float=25f,tint:FloatArray=c) {
            val nx=ceil(sx/12).toInt();val nz=ceil(sz/12).toInt()
            for(i in 0 until nx)for(j in 0 until nz){val ax=x-sx+i*2*sx/nx;val bx=x-sx+(i+1)*2*sx/nx;val az=z-sz+j*2*sz/nz;val bz=z-sz+(j+1)*2*sz/nz
                fun p(xx:Float,zz:Float)=floatArrayOf(xx,height(id,xx,zz)-.04f,zz)
                b.quad(p(ax,az),p(ax,bz),p(bx,bz),p(bx,az),tint,k)}
        }
        fun room(x:Float,z:Float,sx:Float,sz:Float,h:Float=6f) {
            solid(x-sx,h*.5f,z,.25f,h*.5f,sz);solid(x+sx,h*.5f,z,.25f,h*.5f,sz)
            if(id==CITY && x==0f){solid(-10f,h*.5f,z-sz,34f,h*.5f,.25f);solid(38f,h*.5f,z-sz,6f,h*.5f,.25f)}else solid(x,h*.5f,z-sz,sx,h*.5f,.25f)
            for(s in floatArrayOf(-1f,1f))solid(x+s*(sx*.5f+2f),h*.5f,z+sz,(sx-4f)*.5f,h*.5f,.25f)
            b.box(x,h+.2f,z,sx,.2f,sz,c,26f)
        }
        when(id) {
            CELESTIAL->{
                for(z in -494 until 104 step 2){val a=z.toFloat();val d=z+2f
                    fun p(zz:Float,dx:Float)=floatArrayOf(curve(zz)+dx,height(id,0f,zz),zz)
                    b.quad(p(a,-7f),p(a,7f),p(d,7f),p(d,-7f),rose,51f)
                    // Raised curved sides suggest an enormous open slide, without forcing camera motion.
                    for(s in floatArrayOf(-1f,1f))for(j in 0..5){val t=j/6f;val u=(j+1)/6f
                        fun q(zz:Float,v:Float)=floatArrayOf(curve(zz)+s*(7f+2f*sin(v*PI.toFloat()*.5f)),height(id,0f,zz)+2.2f*(1f-cos(v*PI.toFloat()*.5f)),zz)
                        b.quad(q(a,t),q(d,t),q(d,u),q(a,u),rose,51f)}
                }
                for(z in listOf(65f,-115f,-290f,-465f)){val cx=curve(z)
                    for(i in 0..63){val a=i*PI.toFloat()/32;val d=(i+1)*PI.toFloat()/32
                        fun p(aa:Float)=floatArrayOf(cx+25f*cos(aa),height(id,0f,z+25f*sin(aa)),z+25f*sin(aa))
                        b.triangle(floatArrayOf(cx,height(id,cx,z),z),p(d),p(a),c,25f)}
                    DreamShapes.chair(b,cx+14f,height(id,cx,z),z,c)
                    DreamShapes.plant(b,cx-14f,height(id,cx,z),z,1.8f)
                }
                for(i in 0..55){val z=105f-i*11.8f;val x=curve(z)+sin(i*2.399f)*65f
                    b.ellipsoid(x,-8f-(i%4),z,20f+(i%3)*8f,7f,18f,c,10,18,56f)}
                // A distant horizon is also a real accessible optional world.
                towerSilhouette(b,140f,-16f,-430f,.9f)
            }
            CITY->{
                floor(0f,-200f,240f,315f,4f,floatArrayOf(.43f,.59f,.43f))
                for(z in -500..100 step 12)floor(0f,z.toFloat(),6f,6f,16f,floatArrayOf(.49f,.47f,.46f))
                floor(-105f,-230f,105f,5f,26f);floor(105f,-300f,105f,5f,26f)
                // School: broad entrance, three real classrooms with distinct desk arrangements.
                room(0f,-170f,44f,45f,8f)
                for(x in floatArrayOf(-27f,0f,27f))for(z in -192..-142 step 12){
                    b.box(x,.85f,z.toFloat(),2.2f,.12f,1.1f,c,43f)
                    for(s in floatArrayOf(-1f,1f))b.box(x+s*1.8f,.4f,z.toFloat(),.07f,.4f,.75f,rose,40f)
                    DreamShapes.chair(b,x,0f,z+3f)
                    blocks.add(Rect(x,z.toFloat(),2.5f,1.3f))
                }
                b.box(0f,3.7f,-214.6f,15f,2.2f,.08f,floatArrayOf(.20f,.32f,.29f),43f)
                for(s in floatArrayOf(-1f,1f))for(z in -198..-140 step 18)b.box(s*43.7f,4.3f,z.toFloat(),.035f,2.1f,5f,blue,34f)
                // Mall: open atrium and shop fronts, not another corridor.
                room(-140f,-315f,60f,55f,15f)
                floor(-140f,-315f,59f,54f,25f)
                for(x in floatArrayOf(-180f,-100f))for(z in -350..-280 step 35){
                    b.box(x,4f,z.toFloat(),12f,4f,.08f,blue,34f)
                    DreamShapes.plant(b,x,0f,z+6f,2.4f)
                }
                for(i in 0..7){val a=i*PI.toFloat()/4;DreamShapes.chair(b,-140f+18f*cos(a),0f,-315f+18f*sin(a))}
                b.box(-140f,14.7f,-315f,37f,.03f,38f,blue,34f)
                // Isolated lit court, net and markings.
                floor(135f,-315f,42f,30f,26f,floatArrayOf(.39f,.58f,.62f))
                for(s in floatArrayOf(-1f,1f)){b.box(135f+s*41f,.02f,-315f,.12f,.015f,29f,c);b.box(135f,.02f,-315f+s*29f,41f,.015f,.12f,c)
                    solid(135f+s*43f,5f,-315f,.18f,5f,.18f,rose,40f);b.box(135f+s*42f,10f,-315f,2.5f,.10f,2.5f,c,24f)}
                for(i in -12..12)b.box(135f+i*3.3f,1.5f,-315f,.018f,1.5f,.018f,c,40f)
                for(i in 0..5)b.box(135f,.4f+i*.45f,-315f,41f,.015f,.015f,c,40f)
                // A party that has been prepared, with no guests.
                floor(-140f,-435f,24f,20f,26f)
                b.box(-140f,1f,-435f,13f,.16f,2.5f,c,43f);blocks.add(Rect(-140f,-435f,13f,2.5f))
                for(i in -4..4){DreamShapes.chair(b,-140f+i*2.7f,0f,-430f);DreamShapes.cup(b,-140f+i*2.7f,1.2f,-435f)}
                for(i in 0..18)b.ellipsoid(-160f+i*2.2f,4f+sin(i*.6f),-440f,.3f,.4f,.3f,rose,7,12,37f)
                towerSilhouette(b,195f,0f,-465f,1f)
            }
            DARK->{
                for(z in -345 until 45 step 8)floor(0f,z+4f,11f,4f,7f,floatArrayOf(.27f,.30f,.36f))
                for(z in listOf(-40f,-150f,-285f)){
                    for(i in 0..63){val a=i*PI.toFloat()/32;val d=(i+1)*PI.toFloat()/32;b.triangle(floatArrayOf(0f,-.04f,z),floatArrayOf(49f*cos(d),-.04f,z+49f*sin(d)),floatArrayOf(49f*cos(a),-.04f,z+49f*sin(a)),c,7f)}
                    for(i in 0..11){val a=i*PI.toFloat()/6;val x=38f*cos(a);val zz=z+38f*sin(a)
                        if(abs(x)<12f)continue
                        solid(x,6f,zz,.65f,6f,.65f,floatArrayOf(.32f,.33f,.37f),7f)
                        DreamShapes.tube(b,(0..16).map{val t=it/16f*PI.toFloat();floatArrayOf(x+8f*cos(t),12f+8f*sin(t),zz)},.35f,c,7f,10)
                    }
                    for(s in floatArrayOf(-1f,1f)){b.ellipsoid(s*22f,2.5f,z,4f,2.5f,4f,DreamShapes.dark,14,20,7f);DreamShapes.face(b,s*23f,13f,z-15f,.38f)}
                }
                for(i in 0..30){val a=i*2.399f;val x=cos(a)*(70f+i%4*18f);val z=-i*12f
                    DreamShapes.tube(b,listOf(floatArrayOf(x,-12f,z),floatArrayOf(x+3f,24f,z),floatArrayOf(x-4f,45f,z+7f)),.8f,DreamShapes.dark,7f,10)
                    for(s in floatArrayOf(-1f,1f))DreamShapes.tube(b,listOf(floatArrayOf(x+2f,24f,z),floatArrayOf(x+s*14f,37f,z+3f),floatArrayOf(x+s*18f,42f,z+5f)),.35f,DreamShapes.dark,7f,8)
                }
                for(x in -210 until 210 step 30)for(z in -390 until 90 step 30)b.quad(floatArrayOf(x.toFloat(),-12f,z.toFloat()),floatArrayOf(x.toFloat(),-12f,z+30f),floatArrayOf(x+30f,-12f,z+30f),floatArrayOf(x+30f,-12f,z.toFloat()),floatArrayOf(.10f,.15f,.23f),1f)
            }
            TOWER->{
                floor(0f,-70f,12f,82f,25f)
                for(s in floatArrayOf(-1f,1f)){solid(s*12f,22f,-70f,.25f,28f,82f,c,7f)
                    for(z in -140..0 step 28)b.box(s*11.7f,height(id,0f,z.toFloat())+4f,z.toFloat(),.03f,3f,7f,blue,34f)}
                solid(0f,28f,-153f,12f,28f,.3f,c,7f)
                for(z in -140..-40 step 5){val y=height(id,0f,z.toFloat());b.box(0f,y-.17f,z.toFloat(),10.4f,.2f,.25f,rose,57f)}
                for(i in 0..3){val z=-15f-i*37f;val y=height(id,0f,z)
                    b.box(-8f,y+3f,z,2f,2f,.06f,blue,34f);DreamShapes.chair(b,7f,y,z)
                    // Four miniature memories of real visited places, rather than repeated empty windows.
                    b.box(-7f,y+1f,z+3f,2.4f,1f,2.1f,c,43f)
                    when(i){
                        0->{b.box(-7f,y+2.5f,z+3f,1.5f,.6f,1.3f,c,26f);b.triangle(floatArrayOf(-8.7f,y+3.1f,z+4.4f),floatArrayOf(-5.3f,y+3.1f,z+4.4f),floatArrayOf(-7f,y+4.3f,z+4.4f),rose,43f);b.box(-7f,y+2.4f,z+4.32f,.35f,.55f,.03f,blue,34f)}
                        1->{b.box(-7f,y+2.05f,z+3f,2f,.03f,1.8f,blue,1f);for(side in floatArrayOf(-1f,1f))b.box(-7f+side*1.5f,y+3f,z+3f,.12f,1f,.12f,c,7f);DreamShapes.tube(b,(0..16).map{val a=it*PI.toFloat()/16;floatArrayOf(-7f+1.6f*cos(a),y+4f+sin(a),z+3f)},.09f,c,7f,7)}
                        2->{b.box(-7f,y+3f,z+3f,1.5f,1f,1.2f,c);for(k in -1..1)for(h in 0..2)b.box(-7f+k*.8f,y+2.4f+h*.6f,z+4.23f,.22f,.18f,.025f,blue,34f)}
                        else->DreamShapes.tube(b,(0..30).map{val t=it/30f;floatArrayOf(-7f+sin(t*7f)*1.5f,y+2.4f+t*.5f,z+1.5f+t*3f)},.17f,rose,51f,10)
                    }
                    b.ellipsoid(-8f,y+3f,z+.12f,1.1f,1.1f,1.1f,rose,14,20,55f+i*.1f)}
                b.box(0f,24.8f,-140f,3f,2.2f,.05f,blue,34f)
            }
            BED->{
                floor(0f,-5f,9f,17f,43f)
                solid(-9f,3.7f,-5f,.2f,3.7f,17f,c,0f)
                solid(9f,3.7f,-5f,.2f,3.7f,17f,c,0f)
                b.box(0f,7.4f,-5f,9f,.2f,17f,c,0f)
                DreamShapes.bed(b,-3f,0f,-12f);blocks.add(Rect(-3f,-12f,2.6f,3.5f))
                b.box(-8.75f,3.7f,-12f,.05f,3.3f,4f,rose,58f)
                for(i in 0..24){val a=i*2.399f;b.ellipsoid(sin(a)*9f,-.5f+sin(i*.5f)*.25f,-5f+cos(a)*17f,3.4f,1.0f,3.4f,c,12,20,56f)}
                b.box(0f,3.4f,8f,1.5f,3.4f,.03f,blue,29f)
            }
        }
        mesh=b.finish();cached=id;return mesh!!
    }
    fun towerSilhouette(b:DreamMesh,x:Float,y:Float,z:Float,s:Float=1f){
        b.box(x,y+34*s,z,5*s,34*s,5*s,DreamShapes.ivory,7f)
        for(i in 0..6){b.box(x,y+7*s+i*8*s,z,6*s,.35f*s,6*s,DreamShapes.rose,43f);b.box(x,y+8*s+i*8*s,z+5.1f*s,1.3f*s,2*s,.05f, DreamShapes.ivory,24f)}
        b.ellipsoid(x,y+72*s,z,7*s,4*s,7*s,DreamShapes.rose,12,20,25f)
    }
}
