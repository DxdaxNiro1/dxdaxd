package com.dreamcore.game

import kotlin.math.*

/** Eleven authored spaces from the supplied references. One scene and its shared collision grid at a time. */
object DreamReferences {
    fun releaseGeometry(){cached="";mesh=null}
    const val VILLAGE="echo_01";const val CINEMA="echo_02";const val VALLEY="echo_03"
    const val LAKE="echo_04";const val CANAL="echo_05";const val HILL="echo_06"
    const val GAS="echo_07";const val SUBURB="echo_08";const val HALL="echo_09"
    const val ATRIUM="echo_10";const val OFFICE="echo_11"
    val ids=listOf(VILLAGE,CINEMA,VALLEY,LAKE,CANAL,HILL,GAS,SUBURB,HALL,ATRIUM,OFFICE)
    fun contains(id:String)=id in ids
    fun title(id:String)=when(id){VILLAGE->"El barrio que se inclinó";CINEMA->"El cine sobre la Tierra";VALLEY->"Las duchas del valle infinito";LAKE->"La casa reflejada entre nubes";CANAL->"El canal de los hongos dormidos";HILL->"La colina de la nube inmensa";GAS->"La última gasolinera del atardecer";SUBURB->"El barrio de las sombras de colores";HALL->"El pasillo de las sillas vacías";ATRIUM->"El edificio de los pisos sin nadie";else->"La oficina dentro de las nubes"}
    fun entry(id:String)=DreamJourney.Point(if(id==CANAL)20f else 0f,8f)
    fun endZ(id:String)=when(id){CINEMA->-140f;VALLEY->-435f;LAKE->-210f;HALL->-165f;ATRIUM->-205f;OFFICE->-34f;else->-255f}
    fun exit(id:String)=DreamJourney.Point(if(id==CANAL)20f else 0f,endZ(id))
    fun previous(id:String)=ids.indexOf(id).let{if(it<=0)DreamPocket.RETURN else ids[it-1]}
    fun next(id:String)=ids.indexOf(id).let{if(it>=ids.lastIndex)DreamPocket.RETURN else ids[it+1]}
    fun indoor(id:String)=id in listOf(CINEMA,HALL,ATRIUM,OFFICE)
    fun centre(z:Float)=sin(z*.015f)*7f
    fun height(id:String,x:Float,z:Float):Float=when(id){CINEMA->-((-z-30f)*.035f).coerceIn(0f,3.85f);HILL->sin(z*.016f)*2.5f+sin(x*.014f)*.7f;ATRIUM->((-z-50f)/10f).coerceIn(0f,8f);else->0f}
    data class Rect(val x:Float,val z:Float,val sx:Float,val sz:Float){fun hit(px:Float,pz:Float)=abs(px-x)<sx+.28f&&abs(pz-z)<sz+.28f}
    data class Light(val x:Float,val y:Float,val z:Float,val radius:Float,val warm:Float)
    private var cached="";private var mesh:DreamWorlds.Scene?=null
    private val blocks=ArrayList<Rect>();private var cells=emptyMap<DreamChunks.Key,List<Rect>>()
    private val lights=ArrayList<Light>()
    fun canWalk(id:String,x:Float,z:Float):Boolean {
        if(!contains(id))return false
        if(cached!=id)create(id)
        val end=endZ(id)-8f
        if(z !in end..17f)return false
        val allowed=when(id){
            CINEMA->abs(x)<31f || (x in 30f..61f && z in -25f..-15f)
            VALLEY->{val dx=x-centre(z);abs(dx)<10f || listOf(-90f,-210f,-320f).any{hypot(dx-17f,z-it)<12f}}
            CANAL->abs(x) in 9f..34f || listOf(-8f,-140f,-240f).any{abs(z-it)<3f&&abs(x)<34f}
            HALL->abs(x)<3.35f
            ATRIUM->abs(x)<16.6f && !(abs(x)<7.4f && z in -175f..-55f && abs(z+112f)>3.2f)
            LAKE->hypot(x,z+130f)<92f || (abs(x)<3.7f && z> -50f)
            OFFICE->abs(x)<9.2f
            else->abs(x)<105f
        }
        if(!allowed)return false
        if(id==LAKE&&hypot(x,z+130f)<31f&&hypot(x,z+130f)>11f&&!(abs(x)<2.1f&&z> -130f))return false
        if(id==VALLEY&&listOf(-90f,-210f,-320f).any{hypot(x-centre(z)-17f,z-it)<7.8f})return false
        val px=if(id==CANAL)20f else 0f
        if((abs(z-8f)<.48f||abs(z-endZ(id))<.48f)&&abs(abs(x-px)-1.7f)<.30f)return false
        return cells[DreamChunks.key(x,z)]?.none{it.hit(x,z)}?:true
    }
    fun portalAt(id:String,x:Float,z:Float):String? {val a=entry(id);val e=exit(id);return if(hypot(x-a.x,z-a.z)<2.2f)previous(id)else if(hypot(x-e.x,z-e.z)<2.2f)next(id)else null}
    fun create(id:String):DreamWorlds.Scene {
        require(contains(id));if(cached==id&&mesh!=null)return mesh!!
        val b=DreamMesh();blocks.clear();lights.clear()
        val cream=DreamSculpt.ivory;val rose=floatArrayOf(.91f,.59f,.66f);val green=floatArrayOf(.32f,.53f,.28f)
        fun blocked(x:Float,z:Float,sx:Float,sz:Float){blocks.add(Rect(x,z,sx,sz))}
        fun floor(x0:Float,x1:Float,z0:Float,z1:Float,c:FloatArray,k:Float=44f,step:Float=4f,lift:Float=0f){
            val nx=ceil((x1-x0)/step).toInt();val nz=ceil((z1-z0)/step).toInt()
            for(i in 0 until nx)for(j in 0 until nz){val x=x0+(x1-x0)*i/nx;val xx=x0+(x1-x0)*(i+1)/nx;val z=z0+(z1-z0)*j/nz;val zz=z0+(z1-z0)*(j+1)/nz
                if(id==LAKE && hypot((x+xx)*.5f,(z+zz)*.5f+130f)>96f && !(abs((x+xx)*.5f)<4f && (z+zz)*.5f> -50f))continue
                if(id==ATRIUM&&abs((x+xx)*.5f)<7.2f&&(z+zz)*.5f in -175f..-55f&&abs((z+zz)*.5f+112f)>3.2f)continue
                fun p(a:Float,d:Float)=floatArrayOf(a,height(id,a,d)+lift,d)
                b.quad(p(x,z),p(x,zz),p(xx,zz),p(xx,z),c,k)
            }
        }
        fun wall(x:Float,y:Float,z:Float,sx:Float,sy:Float,sz:Float,c:FloatArray=cream,k:Float=0f){DreamSculpt.rounded(b,x,y,z,sx,sy,sz,min(.12f,min(sx,min(sy,sz))*.6f),c,k);blocked(x,z,sx,sz)}
        fun light(x:Float,y:Float,z:Float,r:Float=8f,warm:Float=1f){lights.add(Light(x,y,z,r,warm));DreamSculpt.pendant(b,x,y,z)}
        fun house(x:Float,z:Float,c:FloatArray,scale:Float=1f,tilt:Float=0f){DreamSculpt.house(b,x,height(id,x,z),z,c,scale,tilt);blocked(x,z,4.6f*scale+abs(sin(tilt))*8f*scale,4.4f*scale)}
        fun cloud(x:Float,y:Float,z:Float,s:Float){for(i in 0..2){val xx=x+(i-1)*s*.26f;val yy=y+sin(i*2f)*s*.12f;val zz=z-i*s*.15f
            val a=floatArrayOf(xx-s,yy-s*.55f,zz);val c=floatArrayOf(xx+s,yy-s*.55f,zz);val d=floatArrayOf(xx+s,yy+s*.55f,zz);val e=floatArrayOf(xx-s,yy+s*.55f,zz);val n=floatArrayOf(0f,0f,1f)
            b.mappedTriangle(a,c,d,floatArrayOf(0f,0f,1f),floatArrayOf(1f,0f,1f),floatArrayOf(1f,1f,1f),n,n,n,92f)
            b.mappedTriangle(a,d,e,floatArrayOf(0f,0f,1f),floatArrayOf(1f,1f,1f),floatArrayOf(0f,1f,1f),n,n,n,92f)
        }}
        when(id){
            VILLAGE,SUBURB->{
                floor(-160f,160f,-310f,35f,if(id==VILLAGE)floatArrayOf(.96f,.97f,.96f)else green,if(id==VILLAGE)44f else 4f,10f)
                floor(-7f,7f,-285f,25f,if(id==VILLAGE)cream else floatArrayOf(.25f,.28f,.31f),if(id==VILLAGE)44f else 93f,6f,.02f)
                val colors=listOf(rose,floatArrayOf(.88f,.75f,.26f),floatArrayOf(.35f,.70f,.78f),floatArrayOf(.59f,.77f,.47f),cream)
                for(row in 0..7)for(side in floatArrayOf(-1f,1f)){
                    val z=-24f-row*32f;val x=side*(if(id==VILLAGE)24f else 26f)
                    house(x,z,if(id==VILLAGE&&row%3==0)cream else colors[(row+(if(side>0)2 else 0))%5],if(row%3==0)1.25f else 1f,if(id==VILLAGE)sin(row*2.4f+side)*.17f else 0f)
                    if(id==SUBURB){floor(side*15f-5f,side*15f+5f,z-7f,z+10f,cream,44f,4f,.03f);DreamShapes.plant(b,x+side*8f,0f,z+3f,1.8f)}
                    DreamSculpt.rounded(b,side*9f,.14f,z,1.5f,.14f,16f,.12f,cream,44f)
                }
                if(id==VILLAGE)for(i in 0..2)house(53f,-80f-i*6f,colors[i],.8f+i*.2f,.1f*(i-1))
            }
            CINEMA->{
                floor(-33f,33f,-150f,20f,floatArrayOf(.18f,.31f,.35f),42f,2f)
                for(s in floatArrayOf(-1f,1f)){
                    if(s<0f)wall(s*33f,5f,-65f,.3f,9f,85f,rose)
                    for(z in 0 downTo -140 step 14){DreamSculpt.cylinder(b,s*28f,height(id,s*28f,z.toFloat()),z.toFloat(),.48f,8f,cream,15f,20);light(s*28f,height(id,s*28f,z.toFloat())+7f,z.toFloat(),10f);blocked(s*28f,z.toFloat(),.48f,.48f)}
                }
                // Open orbital gallery at the right, with glazing rails and Earth beyond.
                for(z in 0 downTo -140 step 10)b.box(32.5f,1f+height(id,32.5f,z.toFloat()),z.toFloat(),.08f,.08f,5f,cream,15f)
                for(row in 0..13)for(col in -7..7){if(abs(col)<=1)continue
                    val z=-32f-row*6.5f;val x=col*2.15f;val y=height(id,x,z)
                    DreamSculpt.chair(b,x,y,z,true);blocked(x,z,.70f,.66f)
                    if(col== -2||col==2)b.ellipsoid(x,y+.18f,z+1f,.12f,.13f,.12f,floatArrayOf(.99f,.60f,.19f),5,8,24f)
                }
                val z=-148f;val y=height(id,0f,z)+8.5f
                DreamSculpt.rounded(b,0f,y,z,20f,8f,.3f,.2f,cream,15f)
                val n=floatArrayOf(0f,0f,1f);val a=floatArrayOf(-19f,y-7f,z+.35f);val c=floatArrayOf(19f,y-7f,z+.35f);val d=floatArrayOf(19f,y+7f,z+.35f);val e=floatArrayOf(-19f,y+7f,z+.35f)
                b.mappedTriangle(a,c,d,floatArrayOf(0f,0f,1f),floatArrayOf(1f,0f,1f),floatArrayOf(1f,1f,1f),n,n,n,94f)
            b.mappedTriangle(a,d,e,floatArrayOf(0f,0f,1f),floatArrayOf(1f,1f,1f),floatArrayOf(0f,1f,1f),n,n,n,94f)
                floor(33f,61f,-25f,-15f,cream,25f,3f)
                DreamSculpt.cylinder(b,0f,-185f,-70f,2.8f,179f,cream,15f,28)
                for(i in 0 until 80){val a=i*2f*PI.toFloat()/80f;val d=(i+1)*2f*PI.toFloat()/80f
                    fun p(r:Float,t:Float,y:Float)=floatArrayOf(r*cos(t),y,-70f+r*sin(t))
                    b.quad(p(38f,a,-4.3f),p(38f,d,-4.3f),p(43f,d,-4.3f),p(43f,a,-4.3f),rose,15f)
                    b.quad(p(43f,a,-7f),p(43f,d,-7f),p(43f,d,-4.3f),p(43f,a,-4.3f),cream,15f)
                }
                // Arched niches and gently curved balcony over the planet.
                for(z0 in -10 downTo -130 step 20)DreamShapes.tube(b,(0..18).map{val a=it*PI.toFloat()/18;floatArrayOf(-31.7f,3f+3.6f*sin(a),z0+3.5f*cos(a))},.13f,cream,15f,8)
            }
            VALLEY->{
                floor(-45f,45f,-455f,25f,green,4f,6f)
                for(z0 in -450 until 20 step 4){val z=z0.toFloat();val x=centre(z);val next=centre(z+4f);b.quad(floatArrayOf(x-3f,.025f,z),floatArrayOf(next-3f,.025f,z+4f),floatArrayOf(next+3f,.025f,z+4f),floatArrayOf(x+3f,.025f,z),floatArrayOf(.60f,.65f,.63f),44f)}
                // Curved, smoothly normaled moss cliffs; no stacks of rectangular blocks.
                for(side in floatArrayOf(-1f,1f))for(j in 0 until 105)for(k in 0 until 16){
                    fun p(u:Float,v:Float):FloatArray {val z=20f-u*4.5f;val y=v*4.8f;val bend=18f+22f*sin(y/76.8f*PI.toFloat())+4f*sin(z*.016f+y*.022f);return floatArrayOf(centre(z)+side*bend,y,z)}
                    fun normal(u:Float,v:Float):FloatArray{val a=p(u+.02f,v);val c=p(u-.02f,v);val d=p(u,v+.02f);val e=p(u,v-.02f);val ux=a[0]-c[0];val uy=a[1]-c[1];val uz=a[2]-c[2];val vx=d[0]-e[0];val vy=d[1]-e[1];val vz=d[2]-e[2];val n=floatArrayOf(uy*vz-uz*vy,uz*vx-ux*vz,ux*vy-uy*vx);val len=sqrt(n.sumOf{(it*it).toDouble()}.toFloat()).coerceAtLeast(.001f);for(i in 0..2)n[i]=n[i]/len*side;return n}
                    val u=j.toFloat();val v=k.toFloat();val a=p(u,v);val c=p(u+1,v);val d=p(u+1,v+1);val e=p(u,v+1);val na=normal(u,v);val nc=normal(u+1,v);val nd=normal(u+1,v+1);val ne=normal(u,v+1)
                    b.mappedTriangle(a,c,d,green,green,green,na,nc,nd,90f)
            b.mappedTriangle(a,d,e,green,green,green,na,nd,ne,90f)
                }
                for(i in 0..24){val z=-15f-i*16f;val x=centre(z)-8f
                    DreamShapes.tube(b,listOf(floatArrayOf(x,0f,z),floatArrayOf(x,3.4f,z),floatArrayOf(x+.5f,3.7f,z),floatArrayOf(x+1.15f,3.7f,z)),.075f,floatArrayOf(.54f,.57f,.54f),15f,10)
                    b.ellipsoid(x+1.15f,3.58f,z,.25f,.06f,.25f,cream,6,12,15f)
                    for(k in 0..2)b.box(x+1.15f+(k-1)*.13f,1.8f,z,.02f,1.7f,.03f,floatArrayOf(.58f,.77f,.80f),50f)
                    if(i%4==0){DreamSculpt.chair(b,centre(z)+7f,0f,z);blocked(centre(z)+7f,z,.75f,.75f)}
                }
                for(z in listOf(-90f,-210f,-320f)){val x=centre(z)+17f
                    for(i in 0 until 64){val a=i*2f*PI.toFloat()/64f;val d=(i+1)*2f*PI.toFloat()/64f;fun p(r:Float,t:Float,y:Float)=floatArrayOf(x+r*cos(t),y,z+r*sin(t))
            b.quad(p(8f,a,.08f),p(8f,d,.08f),p(10.5f,d,.08f),p(10.5f,a,.08f),cream,44f)
            b.triangle(floatArrayOf(x,-.08f,z),p(8f,a,-.08f),p(8f,d,-.08f),floatArrayOf(.22f,.61f,.68f),1f)}
                }
                for(centreZ in floatArrayOf(-360f,-390f,-420f))for(j in 0 until 48)for(k in 0..3){
                    val a=j*2f*PI.toFloat()/48f;val d=(j+1)*2f*PI.toFloat()/48f;val r=8f+k*3.5f;val rr=r+3.5f
                    fun p(radius:Float,t:Float)=floatArrayOf(centre(centreZ)+radius*cos(t),17f+3f*(1f-radius*radius/484f),centreZ+radius*sin(t))
                    b.quad(p(r,a),p(rr,a),p(rr,d),p(r,d),green,90f)
                }
                // A grotto canopy with three real apertures and falling water at the end.
                for(i in 0..3){val z=-345f-i*23f;DreamShapes.tube(b,(0..30).map{val a=it*PI.toFloat()/30;floatArrayOf(centre(z)+23f*cos(a),5f+17f*sin(a),z)},2f,green,90f,12);for(s in floatArrayOf(-1f,1f))b.box(centre(z)+s*18f,9f,z,.15f,9f,1f,floatArrayOf(.55f,.73f,.72f),50f)}
            }
            LAKE->{
                floor(-150f,150f,-300f,30f,floatArrayOf(.43f,.50f,.37f),4f,8f)
                for(i in 0 until 96){val a=i*2f*PI.toFloat()/96;val d=(i+1)*2f*PI.toFloat()/96;fun p(r:Float,t:Float,y:Float)=floatArrayOf(r*cos(t),y,-130f+r*sin(t))
            b.quad(p(11f,a,.05f),p(11f,d,.05f),p(32f,d,.05f),p(32f,a,.05f),floatArrayOf(.43f,.64f,.73f),1f)
            b.quad(p(32f,a,.07f),p(32f,d,.07f),p(35f,d,.07f),p(35f,a,.07f),cream,14f)}
                floor(-4f,4f,-50f,18f,cream,44f,2f,.03f)
                for(i in 0 until 80)for(j in 0 until 6){val a=i*2f*PI.toFloat()/80f;val d=(i+1)*2f*PI.toFloat()/80f
                    fun p(t:Float,k:Float):FloatArray {val r=96f-k*8f;return floatArrayOf(r*cos(t),-k*6f,-130f+r*sin(t))}
                    b.quad(p(a,j.toFloat()),p(a,j+1f),p(d,j+1f),p(d,j.toFloat()),floatArrayOf(.40f,.43f,.36f),14f)
                }
                house(0f,-130f,floatArrayOf(.78f,.86f,.85f),1.5f)
                floor(-2f,2f,-119f,-94f,cream,43f,2f,.14f)
                for(i in 0..8)cloud((i-4)*28f,20f+sin(i.toFloat())*8f,-240f-i*6f,45f)
            }
            CANAL->{
                floor(-40f,40f,-280f,22f,rose,26f,4f)
                floor(-8f,8f,-280f,22f,floatArrayOf(.34f,.72f,.80f),1f,5f,.06f)
                for(z in listOf(-8f,-140f,-240f))floor(-34f,34f,z-3f,z+3f,cream,25f,3f,.15f)
                for(i in 0..14)for(s in floatArrayOf(-1f,1f)){val z=-12f-i*18f;val x=s*(23f+5f*sin(i*1.7f))
                    DreamSculpt.cylinder(b,x,0f,z,1.2f,6f,cream,0f,16)
            b.ellipsoid(x,6f,z,5.4f,2.8f,5.4f,if(i%2==0)floatArrayOf(.46f,.69f,.47f)else rose,14,28,0f)
                    for(k in 0..5){val a=k*2.399f
            b.ellipsoid(x+cos(a)*3.4f,8.1f-abs(sin(a))*.7f,z+sin(a)*3.4f,.6f,.13f,.7f,cream,5,9,15f)}
                    blocked(x,z,1.4f,1.4f)
                    if(i%2==0){DreamSculpt.cylinder(b,s*11f,0f,z,.07f,4.3f,cream,15f,10)
            b.ellipsoid(s*11f,4.5f,z,.35f,.35f,.35f,cream,8,12,24f)}
                }
            }
            HILL->{
                floor(-170f,170f,-300f,30f,green,4f,4f)
                house(0f,-150f,rose,1.5f);floor(-2f,2f,-125f,18f,floatArrayOf(.56f,.65f,.39f),4f,3f,.02f)
                cloud(-20f,78f,-230f,115f);cloud(90f,65f,-265f,95f)
                for(i in 0..90){val x=sin(i*2.399f)*80f;val z=10f-i*3f;DreamShapes.tube(b,listOf(floatArrayOf(x,height(id,x,z),z),floatArrayOf(x+.2f,height(id,x,z)+.7f,z)),.025f,floatArrayOf(.52f,.61f,.32f),38f,4)}
            }
            GAS->{
                floor(-150f,150f,-300f,30f,floatArrayOf(.47f,.43f,.28f),4f,10f)
                floor(-7f,7f,-285f,20f,floatArrayOf(.25f,.28f,.32f),7f,5f,.01f)
                floor(-24f,24f,-115f,-35f,floatArrayOf(.62f,.65f,.61f),93f,3f,.03f)
                DreamSculpt.rounded(b,0f,5.8f,-78f,22f,.40f,31f,.38f,cream,15f)
                for(s in floatArrayOf(-1f,1f))for(z in floatArrayOf(-53f,-103f)){DreamSculpt.cylinder(b,s*20f,0f,z,.35f,5.6f,cream,15f,20);blocked(s*20f,z,.4f,.4f)}
                for(x in floatArrayOf(-7f,7f))for(z in floatArrayOf(-60f,-93f)){
                    DreamSculpt.rounded(b,x,1.1f,z,.65f,1.1f,.5f,.16f,cream,15f);DreamSculpt.rounded(b,x,2.35f,z,.8f,.22f,.58f,.14f,floatArrayOf(.36f,.68f,.70f),24f)
                    b.box(x,1.6f,z+.51f,.43f,.25f,.015f,floatArrayOf(.06f,.10f,.10f),95f)
                    DreamShapes.tube(b,(0..18).map{val a=it*PI.toFloat()/18;floatArrayOf(x+.8f+.25f*sin(a),1.8f-1.3f*sin(a),z+.2f*cos(a))},.055f,floatArrayOf(.13f,.14f,.15f),15f,8);blocked(x,z,.8f,.65f);lights.add(Light(x,4.8f,z,13f,.7f))
                }
                house(44f,-80f,cream,1.4f)
            }
            HALL->{
                floor(-3.6f,3.6f,-176f,18f,DreamSculpt.teal,42f,1.5f)
                wall(-3.65f,2.3f,-79f,.15f,2.3f,97f);wall(3.65f,2.3f,-79f,.15f,2.3f,97f)
                wall(0f,2.3f,18f,3.8f,2.3f,.15f);wall(0f,2.3f,-176f,3.8f,2.3f,.15f)
                b.box(0f,4.65f,-79f,3.8f,.12f,98f,cream,0f)
                for(i in 0..31){val z=-2f-i*5f
                    DreamSculpt.chair(b,-2.45f,0f,z);blocked(-2.45f,z,.7f,.75f)
                    // Angled bays catch broad white light, as in the supplied corridor.
                    b.quad(floatArrayOf(-3.48f,1.9f,z-3.8f),floatArrayOf(-3.48f,4.55f,z-3.8f),floatArrayOf(-1.9f,4.55f,z-6f),floatArrayOf(-3.48f,1.9f,z-6f),cream,0f)
                    b.box(-3.47f,2.6f,z-5f,.025f,1.7f,1f,floatArrayOf(.96f,.97f,.94f),96f)
                    DreamSculpt.rounded(b,3.48f,1.45f,z-4f,.06f,1.45f,.68f,.06f,floatArrayOf(.44f,.37f,.28f),43f)
                    light(0f,4.55f,z-4f,7f);lights.add(Light(-3.3f,2.4f,z-5f,5f,0f))
                    if(i%2==0)b.box(3.45f,2.4f,z,.03f,.55f,.42f,woodColor(),43f)
                }
            }
            ATRIUM->{
                floor(-18f,18f,-220f,18f,floatArrayOf(.83f,.82f,.78f),44f,2f)
                wall(-18f,11f,-100f,.35f,16f,120f);wall(18f,11f,-100f,.35f,16f,120f)
                wall(0f,12f,18f,18f,15f,.3f)
                // Bottom of the dark central void and floating upper corridors.
                b.box(0f,-15f,-115f,7.5f,.3f,62f,floatArrayOf(.07f,.07f,.08f),44f)
                for(s in floatArrayOf(-1f,1f))for(z0 in -60 downTo -170 step 4){val z=z0.toFloat();val y=height(id,s*7.6f,z)
                    if(abs(z+112f)>5f){b.box(s*7.6f,y+.65f,z,.13f,.65f,2f,cream,0f);blocked(s*7.6f,z,.13f,2f)}
                }
                for(level in 0..2){val y=level*8f
                    for(z in -12 downTo -200 step 16){DreamSculpt.rounded(b,17.55f,y+1.6f,z.toFloat(),.08f,1.6f,.7f,.06f,floatArrayOf(.23f,.22f,.21f),43f);light(-15.5f,y+5.5f,z.toFloat(),9f,.45f)}
                    if(level>1)for(s in floatArrayOf(-1f,1f)){b.box(s*13f,y,-118f,4.8f,.25f,61f,cream,44f)
            b.box(s*8.3f,y+1f,-118f,.15f,1f,61f,cream,0f)}
                }
                for(z in floatArrayOf(-45f,-112f,-185f)){val y=height(id,0f,z)
            b.box(0f,y-.7f,z,17.5f,.18f,3.1f,cream,44f)
            b.box(0f,y+4.1f,z,16f,.12f,4f,cream,0f)}
            }
            OFFICE->{
                floor(-10f,10f,-43f,18f,floatArrayOf(.22f,.30f,.36f),42f,2f)
                for(s in floatArrayOf(-1f,1f))wall(s*10f,1.9f,-12f,.25f,1.9f,31f)
                b.box(0f,3.9f,-12f,10f,.15f,31f,cream,91f)
                wall(0f,1.9f,18f,10f,1.9f,.25f)
                // Actual opening, with clouds behind the glazing and inside the room.
                wall(-8f,1.9f,-42f,2f,1.9f,.2f);wall(8f,1.9f,-42f,2f,1.9f,.2f)
                b.box(0f,.5f,-42f,6f,.5f,.15f,cream,0f)
            b.box(0f,3.6f,-42f,6f,.3f,.15f,cream,0f)
                for(x in -6..6 step 6)b.box(x.toFloat(),2.1f,-41.8f,.06f,1.5f,.10f,cream,15f)
                for(s in floatArrayOf(-1f,1f))for(i in 0..3){val x=s*6.5f;val z=-4f-i*9.5f
                    DreamSculpt.rounded(b,x,.84f,z,2.6f,.12f,1.1f,.10f,floatArrayOf(.58f,.49f,.34f),43f)
                    for(t in floatArrayOf(-1f,1f))DreamSculpt.cylinder(b,x+t*2.2f,0f,z,.055f,.8f,cream,15f,8)
                    DreamSculpt.computer(b,x,1.08f,z-.15f);DreamSculpt.chair(b,x,0f,z+2.3f);blocked(x,z,2.6f,1.2f);blocked(x,z+2.3f,.75f,.7f)
                    light(s*4.5f,3.7f,z,10f,.3f)
                }
                for(i in 0..5)cloud((i-2.5f)*12f,3f+i*1.1f,-54f-i*8f,22f)
                cloud(-5f,1.2f,-23f,3.7f);cloud(6f,.9f,-39f,3f)
                for(i in 0..11)floor(-1.1f,1.1f,-41f+i*4.6f,-37.6f+i*4.6f,floatArrayOf(.50f,.38f,.31f),42f,1f,.014f)
            }
        }
        for((p,forward)in listOf(entry(id) to false,exit(id) to true)){
            val y=height(id,p.x,p.z)
            for(s in floatArrayOf(-1f,1f))DreamSculpt.rounded(b,p.x+s*1.7f,y+1.7f,p.z,.13f,1.7f,.15f,.08f,cream,15f)
            b.box(p.x,y+3.5f,p.z,1.85f,.10f,.15f,cream,15f)
            b.box(p.x,y+1.65f,p.z,1.5f,1.65f,.025f,if(forward)floatArrayOf(.58f,.81f,.77f)else floatArrayOf(.64f,.68f,.81f),if(forward)28f else 29f)
        }
        val vertices=b.finish().vertices
        // Local light and contact darkening are baked once into mesh colors, not dozens of GPU lights per pixel.
        if(indoor(id)||id==GAS)for(i in vertices.indices step 10){
            if(vertices[i+9] in 91.5f..95.5f)continue
            var warm=0f;var cool=0f
            for(l in lights){val dx=(vertices[i]-l.x)/l.radius;val dy=(vertices[i+1]-l.y)/l.radius;val dz=(vertices[i+2]-l.z)/l.radius;val t=(1f-dx*dx-dy*dy-dz*dz).coerceAtLeast(0f);warm+=t*t*l.warm;cool+=t*t*(1f-l.warm)}
            val base=if(id==ATRIUM).43f else .67f
            vertices[i+3]*=(base+.30f*warm+.25f*cool).coerceAtMost(1.35f);vertices[i+4]*=(base+.23f*warm+.27f*cool).coerceAtMost(1.3f);vertices[i+5]*=(base+.12f*warm+.30f*cool).coerceAtMost(1.25f)
        }
        for(i in vertices.indices step 10){
            if(vertices[i+7]<.65f || abs(vertices[i+1]-height(id,vertices[i],vertices[i+2]))>.20f)continue
            var shade=1f
            for(r in blocks){val dx=(abs(vertices[i]-r.x)-r.sx).coerceAtLeast(0f);val dz=(abs(vertices[i+2]-r.z)-r.sz).coerceAtLeast(0f);val d=dx*dx+dz*dz;if(d<2.25f)shade=min(shade,.76f+.24f*(d/2.25f))}
            vertices[i+3]*=shade;vertices[i+4]*=shade;vertices[i+5]*=shade
        }
        val index=HashMap<DreamChunks.Key,MutableList<Rect>>()
        for(r in blocks){val a=DreamChunks.key(r.x-r.sx-1,r.z-r.sz-1);val d=DreamChunks.key(r.x+r.sx+1,r.z+r.sz+1);for(x in a.x..d.x)for(z in a.z..d.z)index.getOrPut(DreamChunks.Key(x,z)){ArrayList()}.add(r)}
        cells=index;mesh=DreamWorlds.Scene(vertices);cached=id;return mesh!!
    }
    private fun woodColor()=floatArrayOf(.37f,.26f,.19f)
}
