package com.dreamcore.game

import kotlin.math.*

/** 1.6: finite, explorable landscapes; distant silhouettes continue into the fog.
 * Geometry and collision share the same building footprints. Only the active land is cached. */
object DreamLands {
    val ids=listOf(DreamWorlds.GARDEN,DreamWorlds.MEADOW,DreamWorlds.HOUSE_MEADOW,
        DreamWorlds.AQUARIUM,DreamWorlds.HOUSE_CRATER,DreamExpansion.CHESS,
        DreamExpansion.TUNNELS,DreamExpansion.CLOUD_HOTEL,DreamExpansion.ROAD)
    fun contains(id:String)=id in ids
    data class Rect(val x:Float,val z:Float,val sx:Float,val sz:Float) {
        fun hits(px:Float,pz:Float,margin:Float=.32f)=abs(px-x)<sx+margin && abs(pz-z)<sz+margin
    }
    data class Mirror(val x:Float,val z:Float,val y:Float=3f,val half:Float=3f)
    data class Secret(val key:String,val x:Float,val z:Float,val text:String)
    private var cachedId=""
    private var cachedScene:DreamWorlds.Scene?=null
    private val obstacles=ArrayList<Rect>()
    private var collisionCells=emptyMap<DreamChunks.Key,List<Rect>>()
    private var corridorShift=false
    fun changeCorridor() { corridorShift=!corridorShift;cachedId="";cachedScene=null }
    fun title(id:String)=when(id) {
        DreamWorlds.MEADOW->"El pueblo de las ventanas despiertas"
        DreamWorlds.GARDEN->"Invernadero de las constelaciones"
        DreamWorlds.HOUSE_MEADOW->"La casa que recuerda otra puerta"
        DreamWorlds.AQUARIUM->"El mar dentro del cielo"
        DreamWorlds.HOUSE_CRATER->"La estación del último sueño"
        DreamExpansion.TUNNELS->"El valle de las cúpulas de agua"
        DreamExpansion.CHESS->"La llanura de los reyes dormidos"
        DreamExpansion.CLOUD_HOTEL->"El hotel de los horizontes"
        DreamExpansion.ROAD->"La carretera donde caen estrellas"
        else->DreamExpansion.title(id)
    }
    fun height(id:String,x:Float,z:Float):Float {
        if(id!=DreamWorlds.MEADOW || x>1000f)return 0f
        val blend=((abs(x)-10f)/85f).coerceIn(0f,1f)
        return sin(x*.012f)*sin(z*.009f)*3.8f*blend
    }
    fun entry(id:String)=when(id) {
        DreamWorlds.HOUSE_MEADOW,DreamWorlds.HOUSE_CRATER->DreamJourney.Point(0f,12f)
        else->DreamJourney.Point(0f,110f)
    }
    fun exit(id:String)=DreamJourney.Point(0f,if(id==DreamWorlds.HOUSE_MEADOW)-290f else if(id==DreamWorlds.HOUSE_CRATER)-260f else -550f)
    fun night(id:String)=id==DreamWorlds.GARDEN || id==DreamExpansion.ROAD || id==DreamWorlds.HOUSE_CRATER || id==DreamWorlds.AQUARIUM
    private fun villageInterior(x:Float,z:Float):Boolean {
        if(abs(x+40f)<5.5f && abs(z+55f)<6.5f)return true
        val hx=round(x/70f)*70f;val hz=round(z/70f)*70f
        val scale=1f+(abs(hx-hz).toInt()%3)*.15f
        return hx!=0f && abs(hx)<=280f && hz in -490f..70f &&
            abs(x-hx)<5.5f*scale && abs(z-hz)<6.5f*scale
    }
    fun indoor(id:String,x:Float,z:Float=0f)=id==DreamWorlds.HOUSE_MEADOW || id==DreamWorlds.HOUSE_CRATER ||
        x>1000f || (id==DreamWorlds.MEADOW && villageInterior(x,z))
    fun mirror(id:String):Mirror?=when(id) {
        DreamWorlds.MEADOW->Mirror(24f,-140f)
        DreamWorlds.HOUSE_MEADOW->Mirror(0f,-250f,3f,5f)
        DreamWorlds.GARDEN->Mirror(-25f,-220f)
        DreamExpansion.CLOUD_HOTEL->Mirror(0f,-360f,4f,5f)
        else->null
    }
    fun secrets(id:String)=listOf(
        Secret("bell",if(id==DreamExpansion.TUNNELS)-135f else if(id==DreamExpansion.CLOUD_HOTEL)-118f else -90f,if(id==DreamExpansion.TUNNELS)-145f else if(id==DreamExpansion.CLOUD_HOTEL)-190f else -175f,"Un sonido viene del jardín. Aquí el tiempo parece esperar."),
        Secret("window",if(id==DreamExpansion.TUNNELS)135f else 105f,-330f,"Esa ventana mira a un cielo que no pertenece a este lugar."),
        Secret("chair",if(id==DreamExpansion.TUNNELS)-150f else -135f,-440f,"La silla todavía está tibia. Alguien soñó aquí antes que tú.")
    ).let { if(id==DreamWorlds.HOUSE_MEADOW || id==DreamWorlds.HOUSE_CRATER)
        listOf(Secret("corridor",-24f,-195f,"Hay una puerta detrás del reflejo.")) else it } + DreamMemories.secret(id)
    fun zone(id:String,x:Float,z:Float)=when {
        x>1000f->"PISCINA_INTERIOR"
        indoor(id,x,z)->"CASA"
        abs(x)>65f->"JARDIN"
        z < -360f->"UMBRAL"
        else->"VIENTO"
    }
    fun material(id:String,x:Float,z:Float):String=when {
        x>1000f->"tile"
        indoor(id,x,z)->"wood"
        id==DreamExpansion.TUNNELS || id==DreamWorlds.AQUARIUM->"tile"
        id==DreamWorlds.MEADOW && abs(x)>7f && abs(x-round(x/70f)*70f)>4f->"grass"
        else->"stone"
    }
    fun canWalk(id:String,x:Float,z:Float):Boolean {
        ensure(id)
        if(id==DreamWorlds.MEADOW && x>1000f) {
            if(x !in 1502f..1698f || z !in -117f..10f)return false
        } else if(id==DreamWorlds.HOUSE_MEADOW || id==DreamWorlds.HOUSE_CRATER) {
            if(x !in -38f..38f || z !in -310f..22f)return false
        } else if(x !in -380f..380f || z !in -620f..155f)return false
        return collisionCells[DreamChunks.key(x,z)]?.none{it.hits(x,z)}?:true
    }
    private fun ensure(id:String) { if(cachedId!=id || cachedScene==null)create(id) }
    fun create(id:String):DreamWorlds.Scene {
        if(cachedId==id && cachedScene!=null)return cachedScene!!
        obstacles.clear()
        val b=DreamMesh()
        val green=floatArrayOf(.46f,.64f,.43f);val ivory=floatArrayOf(.84f,.83f,.76f)
        val rose=floatArrayOf(.83f,.61f,.72f);val blue=floatArrayOf(.48f,.68f,.76f)
        fun solid(x:Float,y:Float,z:Float,sx:Float,sy:Float,sz:Float,c:FloatArray,k:Float=26f) {
            b.box(x,y,z,sx,sy,sz,c,k);obstacles+=Rect(x,z,sx,sz)
        }
        fun floor(x:Float,z:Float,sx:Float,sz:Float,c:FloatArray,k:Float=25f,y:Float=-.04f) {
            val nx=ceil(sx*2f/24f).toInt().coerceAtLeast(1)
            val nz=ceil(sz*2f/24f).toInt().coerceAtLeast(1)
            for(ix in 0 until nx)for(iz in 0 until nz) {
                val x0=x-sx+ix*sx*2f/nx;val x1=x-sx+(ix+1)*sx*2f/nx
                val z0=z-sz+iz*sz*2f/nz;val z1=z-sz+(iz+1)*sz*2f/nz
                b.quad(floatArrayOf(x0,y,z0),floatArrayOf(x0,y,z1),
                    floatArrayOf(x1,y,z1),floatArrayOf(x1,y,z0),c,k)
            }
        }
        fun window(x:Float,y:Float,z:Float,sx:Float=1.1f) {
            b.box(x,y,z,sx,1.25f,.07f,blue,34f)
            for(s in floatArrayOf(-1f,1f))b.box(x+s*(sx+.12f),y,z,.10f,1.45f,.13f,ivory)
            b.box(x,y,z,.08f,1.3f,.10f,ivory)
        }
        fun house(x:Float,z:Float,scale:Float=1f,open:Boolean=false) {
            val w=5.8f*scale;val d=6.8f*scale;val h=4.8f*scale
            val tint=if((abs(x+z).toInt()%3)==0)rose else ivory
            if(!open)solid(x,h*.5f,z,w,h*.5f,d,tint,26f)
            else {
                solid(x-w,h*.5f,z,.24f,h*.5f,d,tint)
                solid(x+w,h*.5f,z,.24f,h*.5f,d,tint)
                solid(x,h*.5f,z-d,w,h*.5f,.24f,tint)
                for(s in floatArrayOf(-1f,1f))solid(x+s*(w*.5f+1f),h*.5f,z+d,(w-2f)*.5f,h*.5f,.24f,tint)
                b.box(x,h-.6f,z+d,1.9f,.6f,.25f,tint)
                floor(x,z,w,d,ivory)
            }
            val roof=floatArrayOf(.42f,.44f,.56f)
            b.triangle(floatArrayOf(x-w-.5f,h,z+d+.5f),floatArrayOf(x+w+.5f,h,z+d+.5f),floatArrayOf(x,h+4f*scale,z+d+.5f),roof)
            b.triangle(floatArrayOf(x+w+.5f,h,z-d-.5f),floatArrayOf(x-w-.5f,h,z-d-.5f),floatArrayOf(x,h+4f*scale,z-d-.5f),roof)
            b.quad(floatArrayOf(x-w-.5f,h,z-d-.5f),floatArrayOf(x-w-.5f,h,z+d+.5f),floatArrayOf(x,h+4f*scale,z+d+.5f),floatArrayOf(x,h+4f*scale,z-d-.5f),roof,25f)
            b.quad(floatArrayOf(x+w+.5f,h,z+d+.5f),floatArrayOf(x+w+.5f,h,z-d-.5f),floatArrayOf(x,h+4f*scale,z-d-.5f),floatArrayOf(x,h+4f*scale,z+d+.5f),roof,25f)
            window(x-w*.62f,h*.55f,z+d+.08f,.85f*scale)
            window(x+w*.62f,h*.55f,z+d+.08f,.85f*scale)
            if(!open)b.box(x,1.55f,z+d+.09f,1.1f,1.55f,.08f,rose,21f)
            else {
                window(x,2.7f,z-d+.30f,1.6f)
                solid(x-w*.55f,.65f,z-1.5f,1.05f,.65f,.9f,rose,25f)
                b.box(x-w*.55f,1.55f,z-1.5f,.12f,.25f,.12f,ivory,35f)
                b.box(x+w*.55f,.5f,z-d+1f,1.4f,.5f,.65f,blue,26f)
            }
        }
        fun tree(x:Float,z:Float,size:Float=1f) {
            solid(x,2.3f*size,z,.32f*size,2.3f*size,.32f*size,floatArrayOf(.38f,.31f,.29f),0f)
            b.ellipsoid(x,5.7f*size,z,2.8f*size,3.6f*size,2.5f*size,green,5,8,38f)
            // Soft static contact darkening, follows the same terrain.
            floor(x+1f,z+1f,3f*size,2.4f*size,floatArrayOf(.37f,.51f,.38f),0f,-.015f)
        }
        val interior=id==DreamWorlds.HOUSE_MEADOW || id==DreamWorlds.HOUSE_CRATER
        if(interior) {
            for(side in floatArrayOf(-1f,1f))solid(side*39f,3.5f,-144f,.3f,3.5f,166f,ivory)
            solid(0f,3.5f,-310f,39f,3.5f,.3f,rose)
            solid(0f,3.5f,22f,39f,3.5f,.3f,rose)
            for(z in -310 until 25 step 10)floor(0f,z+5f,40f,5f,floatArrayOf(.64f,.58f,.63f),25f)
            // Wide central nave; every sixty metres opens into rooms on both sides.
            for(z in -300 until 20 step 10) {
                val zz=z+5f
                for(s in floatArrayOf(-1f,1f)) {
                    val alcove=z%60 in -20..-10
                    if(!alcove)solid(s*12f,3.5f,zz,.25f,3.5f,5f,ivory)
                    else { solid(s*36f,3.5f,zz,.25f,3.5f,5f,rose);window(s*25f,3.3f,zz-4.8f,5f) }
                }
                b.box(0f,7.25f,zz,38f,.22f,5f,floatArrayOf(.54f,.53f,.65f),26f)
                b.box(0f,7f,zz,1.7f,.05f,2f,blue,35f)
            }
            for(i in 0..7) {
                val z=-25f-i*36f
                val s=if(corridorShift)-1f else 1f
                b.box(s*11.68f,3f,z,.045f,1.7f,2.3f,blue,34f)
                for(edge in floatArrayOf(-1f,1f))b.box(s*11.60f,3f,z+edge*2.4f,.12f,1.8f,.10f,ivory)
                b.box(-s*11.6f,3f,z,.06f,1.9f,1.1f,rose,21f)
            }
            if(id==DreamWorlds.HOUSE_MEADOW)b.box(-24f,1.7f,-198.5f,1.1f,1.7f,.06f,rose,34f)
            if(id==DreamWorlds.HOUSE_CRATER) {
                // A departure hall under a star window; the last gate ends the journey.
                for(z in -235..-25 step 30)for(s in floatArrayOf(-1f,1f)) {
                    solid(s*8f,.55f,z.toFloat(),1.7f,.55f,.6f,rose)
                    b.box(s*8f,1.7f,z+.55f,1.7f,.65f,.10f,ivory)
                }
                b.box(0f,6.95f,-240f,7f,.03f,12f,blue,34f)
            }
        } else {
            // 760 x 775 m traversable area; tile-sized meshes support chunk residency.
            for(x in -400 until 400 step 20)for(z in -640 until 180 step 20) {
                val checker=id==DreamExpansion.CHESS
                val c=when {
                    checker->if(((x/20+z/20) and 1)==0)ivory else floatArrayOf(.25f,.30f,.39f)
                    id==DreamWorlds.MEADOW->green
                    id==DreamWorlds.GARDEN->floatArrayOf(.30f,.42f,.42f)
                    id==DreamExpansion.ROAD->floatArrayOf(.29f,.33f,.46f)
                    else->floatArrayOf(.70f,.73f,.79f)
                }
                floor(x+10f,z+10f,10f,10f,c,if(checker)25f else 0f)
            }
            // Pale route, scattered edge lamps: a legible direction amid free exploration.
            for(z in -600..140 step 10) {
                floor(0f,z.toFloat(),5.5f,5f,if(id==DreamExpansion.ROAD)floatArrayOf(.28f,.30f,.37f)else ivory,25f,-.012f)
                if(z%40==0)for(s in floatArrayOf(-1f,1f)) {
                    b.box(s*7f,.7f,z.toFloat(),.12f,.7f,.12f,ivory)
                    b.ellipsoid(s*7f,1.5f,z.toFloat(),.24f,.24f,.24f,rose,4,6,35f)
                }
            }
            when(id) {
                DreamWorlds.MEADOW->{
                    for(x in -280..280 step 70)for(z in -490..70 step 70) {
                        if(x==0)continue
                        val xx=x.toFloat();val zz=z.toFloat()
                        house(xx,zz,1f+((abs(x-z)%3)*.15f),true)
                        for(q in -2..2)floor(xx+q*2f,zz+11f,1f,4f,ivory,25f,-.01f)
                        tree(xx+16f,zz+10f,.9f)
                        // Garden hedges have real collision, with a broad front opening.
                        for(s in floatArrayOf(-1f,1f))solid(xx+s*13f,.7f,zz, .6f,.7f,12f,green,0f)
                    }
                    for(z in -500..100 step 70)floor(0f,z.toFloat(),380f,2.4f,ivory,26f,-.008f)
                    // Empty playground: recognizable scale amid the endless-looking streets.
                    for(side in floatArrayOf(-1f,1f))solid(70f+side*6f,4f,-105f,.17f,4f,.17f,rose)
                    b.box(70f,8f,-105f,6.3f,.18f,.18f,ivory)
                    for(x in floatArrayOf(67f,73f)) {
                        for(s in floatArrayOf(-1f,1f))b.box(x+s*.65f,4.8f,-105f,.035f,3f,.035f,ivory,39f)
                        b.box(x,1.75f,-105f,.85f,.10f,.5f,rose,39f)
                    }
                    for(x in -280..280 step 70)if(x!=0)for(z in -490..70 step 70) {
                        for(side in floatArrayOf(-1f,1f))b.ellipsoid(x+side*9f,1f,z+7f,1.7f,1.2f,1.7f,green,3,6)
                        b.box(x.toFloat(),.8f,z+11f,1.8f,.12f,.6f,ivory,26f)
                    }
                    house(-40f,-55f,1f,true)
                    b.box(-40f,1.6f,-48.1f,1.35f,1.6f,.05f,blue,34f)
                    // Optional impossible interior, spatially remote, loaded only inside.
                    floor(1600f,-54f,100f,66f,ivory,26f)
                    for(s in floatArrayOf(-1f,1f))solid(1600f+s*100f,12f,-54f,.4f,12f,66f,rose)
                    solid(1600f,12f,-120f,100f,12f,.4f,ivory)
                    b.box(1600f,24f,-54f,100f,.3f,66f,ivory,26f)
                    for(x in 1520..1680 step 20)for(z in -110..-10 step 20) {
                        if(abs(x-1600)<40)continue
                        solid(x.toFloat(),12f,z.toFloat(),.65f,12f,.65f,ivory)
                    }
                    floor(1600f,-58f,32f,43f,blue,1f,.045f)
                    obstacles+=Rect(1600f,-58f,32f,43f)
                    b.box(1600f,22f,-54f,40f,.05f,52f,blue,34f)
                    b.box(1600f,1.8f,8f,1.3f,1.8f,.06f,rose,34f)
                }
                DreamWorlds.GARDEN->{
                    for(x in -240..240 step 80)for(z in -480..0 step 80) {
                        if(x==0)continue
                        for(s in floatArrayOf(-1f,1f)) {
                            for(end in floatArrayOf(-24f,24f))solid(x+s*21f,12f,z+end,.32f,12f,.32f,ivory)
                            for(y in floatArrayOf(8f,16f,23.5f))b.box(x+s*21f,y,z.toFloat(),.22f,.20f,24f,ivory)
                        }
                        b.box(x.toFloat(),24f,z.toFloat(),21f,.14f,24f,blue,34f)
                        for(k in -2..2)tree(x+k*7f,z.toFloat(),1.2f)
                    }
                    for(i in 0..12)b.ellipsoid(sin(i*2.4f)*180f,45f+i%3*12f,-40f-i*42f,3f,3f,3f,rose,7,10,36f)
                }
                DreamExpansion.TUNNELS->{
                    for(z in -500..60 step 80) {
                        for(s in floatArrayOf(-1f,1f)) {
                            val x=s*65f
                            floor(x,z.toFloat(),45f,28f,blue,1f,.03f);obstacles+=Rect(x,z.toFloat(),45f,28f)
                            for(k in 0..8) {
                                val a=k*PI.toFloat()/8
                                b.box(x+cos(a)*48f,sin(a)*33f,z.toFloat(),.75f,.75f,30f,ivory,26f)
                            }
                        }
                        for(s in floatArrayOf(-1f,1f))solid(s*13f,10f,z.toFloat(),.7f,10f,.7f,rose)
                        b.box(0f,20f,z.toFloat(),14f,.65f,5f,blue,26f)
                    }
                    for(x in -250..250 step 125)if(x!=0)for(z in -500..0 step 100)house(x.toFloat(),z.toFloat(),1.5f)
                }
                DreamWorlds.AQUARIUM->{
                    // Water suspended overhead: the whole avenue passes under a sea.
                    floor(0f,-235f,320f,380f,floatArrayOf(.22f,.47f,.59f),1f,32f)
                    for(x in -240..240 step 80)if(x!=0)for(z in -500..0 step 80) {
                        solid(x.toFloat(),16f,z.toFloat(),1f,16f,1f,blue)
                        b.ellipsoid(x+15f,24f,z-12f,8f,2.8f,3f,rose,5,8,36f)
                        b.ellipsoid(x-13f,20f,z+14f,2.5f,2.5f,2.5f,ivory,6,8,36f)
                    }
                }
                DreamExpansion.CHESS->{
                    for(x in -280..280 step 70)if(x!=0)for(z in -490..70 step 70) {
                        val tint=if(((x/70+z/70) and 1)==0)ivory else floatArrayOf(.28f,.31f,.40f)
                        solid(x.toFloat(),1f,z.toFloat(),6f,1f,6f,tint,25f)
                        b.ellipsoid(x.toFloat(),7f,z.toFloat(),3.2f,6f,3.2f,tint,7,12,25f)
                        b.ellipsoid(x.toFloat(),14f,z.toFloat(),4.7f,4.7f,4.7f,tint,7,12,25f)
                        if(z%140==0)for(k in -1..1)b.box(x+k*3f,18f,z.toFloat(),.9f,1.8f,1.4f,tint,25f)
                    }
                }
                DreamExpansion.CLOUD_HOTEL->{
                    for(x in -240..240 step 80)if(x!=0)for(z in -480..80 step 80) {
                        solid(x.toFloat(),18f,z.toFloat(),20f,18f,17f,ivory)
                        for(y in 5..32 step 7)for(k in -2..2)window(x+k*6f,y.toFloat(),z+17.1f,1.8f)
                        b.box(x.toFloat(),37f,z.toFloat(),24f,.7f,21f,rose,25f)
                    }
                    for(z in -500..60 step 80) {
                        for(s in floatArrayOf(-1f,1f))solid(s*19f,12f,z.toFloat(),.6f,12f,.6f,ivory)
                        b.box(0f,24f,z.toFloat(),20f,.6f,4f,rose,26f)
                    }
                }
                DreamExpansion.ROAD->{
                    for(z in -580..100 step 55) {
                        floor(-18f,z.toFloat(),5f,20f,blue,1f,.015f)
                        for(s in floatArrayOf(-1f,1f)) {
                            solid(s*12f,5f,z.toFloat(),.12f,5f,.12f,ivory,0f)
                            b.box(s*10f,10f,z.toFloat(),2f,.08f,.4f,rose,35f)
                        }
                    }
                    for(x in -240..240 step 80)if(x!=0)for(z in -480..0 step 80) {
                        house(x.toFloat(),z.toFloat(),if(z%160==0)2f else 1f)
                        tree(x+25f,z-20f,1.4f)
                    }
                }
            }
            for(secret in secrets(id)) {
                // Discoverable chair, distant window and an abandoned bell pavilion.
                val x=secret.x;val z=secret.z
                b.box(x,.85f,z,1f,.12f,1f,rose,25f)
                b.box(x,1.6f,z-.9f,1f,.7f,.10f,ivory,26f)
                for(s in floatArrayOf(-1f,1f))for(q in floatArrayOf(-1f,1f))b.box(x+s*.8f,.4f,z+q*.8f,.08f,.4f,.08f,ivory)
                window(x,5f,z-3f,2.6f)
                b.ellipsoid(x,8.5f,z-3f,.6f,.7f,.6f,rose,5,8,36f)
            }
        }
        mirror(id)?.let { m->
            b.box(m.x,m.y,m.z,m.half,m.y,.045f,ivory,31f)
            for(s in floatArrayOf(-1f,1f))solid(m.x+s*(m.half+.18f),m.y,m.z,.16f,m.y+.2f,.18f,rose)
            b.box(m.x,m.y*2f+.18f,m.z,m.half+.3f,.15f,.20f,ivory)
            obstacles+=Rect(m.x,m.z,m.half,.12f)
        }
        val encounter=DreamMemories.create(id)
        obstacles.addAll(encounter.blocks)
        val cells=HashMap<DreamChunks.Key,MutableList<Rect>>()
        for(rect in obstacles) {
            val lo=DreamChunks.key(rect.x-rect.sx-.33f,rect.z-rect.sz-.33f)
            val hi=DreamChunks.key(rect.x+rect.sx+.33f,rect.z+rect.sz+.33f)
            for(cx in lo.x..hi.x)for(cz in lo.z..hi.z)cells.getOrPut(DreamChunks.Key(cx,cz)){ArrayList()}.add(rect)
        }
        collisionCells=cells
        val mesh=b.finish().vertices+encounter.scene.vertices
        if(id==DreamWorlds.MEADOW)for(i in mesh.indices step 10)mesh[i+1]+=height(id,mesh[i],mesh[i+2])
        cachedScene=DreamWorlds.Scene(mesh);cachedId=id
        return cachedScene!!
    }
}
