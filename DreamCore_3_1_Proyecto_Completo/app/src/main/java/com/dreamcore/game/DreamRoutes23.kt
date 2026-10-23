package com.dreamcore.game

import kotlin.math.*

/** Each reference has its own explorable space; discoveries can connect them in a loop. */
object DreamRoutes23 {
    fun releaseGeometry(){cachedId="";cached=null}
    const val SKY="dream_sky_openings"
    const val CLOUD="dream_cloud_castle"
    const val CITY="dream_flower_city"
    const val EDGE="dream_grass_fissures"
    val ids=listOf(DreamBackrooms.ID,SKY,CLOUD,CITY,EDGE,DreamMystery.HOUSE)
    fun connected(id:String)=id in listOf(SKY,CLOUD,CITY,EDGE)
    fun contains(id:String)=id in ids
    fun title(id:String)=when(id){DreamBackrooms.ID->"Backrooms: las luces que no descansan";SKY->"Las aberturas del cielo";CLOUD->"Los corredores del castillo de nubes";CITY->"La ciudad al otro lado de las flores";EDGE->"La pradera de las grietas";else->"La casa del teléfono desconectado"}
    fun spawn(id:String)=if(id==EDGE)DreamJourney.Point(-30f,5f)else if(id==DreamBackrooms.ID)DreamJourney.Point(0f,0f)else DreamJourney.Point(0f,2f)
    fun gate(id:String)=if(id==EDGE)DreamJourney.Point(-30f,12f)else if(id==DreamBackrooms.ID)DreamJourney.Point(0f,4f)else DreamJourney.Point(0f,8f)
    fun height(id:String,x:Float,z:Float)=if(id==SKY)sin(x*.025f)*sin(z*.018f)*1.5f else 0f
    fun crack(z:Float)=sin(z*.026f)*10f
    private val towers=buildList{for(s in listOf(-1f,1f))for(i in 0..3)for(j in 0..2)add(DreamLands.Rect(s*(72f+j*44f),-75f-i*82f,10f+(i+j)%3*3f,10f+(i*3+j)%3*3f))}
    fun canWalk(id:String,x:Float,z:Float):Boolean=when(id){
        DreamBackrooms.ID->DreamBackrooms.canWalk(x,z)
        DreamMystery.HOUSE->x in -8.4f..8.4f&&z in -26.4f..10f&&!(abs(x)<4.3f&&abs(z+15.5f)<1.4f)
        SKY->x in -160f..160f&&z in -250f..25f
        CITY->x in -190f..190f&&z in -360f..30f&&towers.none{it.hits(x,z)}&&!(abs(x+42f)<11f&&abs(z+88f)<15f)&&!(abs(x-32f)<3.6f&&z in -360f..-200f)&&!(abs(abs(x)-17f)<1.2f&&abs(z+30f)<4f)
        CLOUD->{val hall=abs(x)<11.4f&&z in -159f..10f;val bridge=abs(x)<3.2f&&z in -185f..-145f;val court=hypot(x,z+214f)<33f&&!(abs(abs(x)-24f)<5f&&z in -250f..-196f);val room=abs(x)<40f&&(abs(z+50f)<8f||abs(z+100f)<8f);(hall||bridge||court||room)&&!(abs(x+34f)<3f&&(abs(z+50f)<4f||abs(z+100f)<4f))}
        EDGE->x in -110f..110f&&z in -215f..25f&&(abs(x-crack(z))>7f||abs(z+50f)<3f||abs(z+145f)<3f)&&!(abs(x+35f)<6.4f&&abs(z+155f)<6f)
        else->false
    }
    fun portalAt(id:String,x:Float,z:Float):String? {val g=gate(id);if(hypot(x-g.x,z-g.z)<2.4f)return DreamPocket.RETURN;if(id==DreamBackrooms.ID&&hypot(x-36f,z+73f)<2.3f)return DreamPocket.RETURN;return null}
    private var cachedId="";private var cached:DreamWorlds.Scene?=null
    fun create(id:String):DreamWorlds.Scene {
        if(id==DreamBackrooms.ID)return DreamBackrooms.create()
        if(id==DreamMystery.HOUSE)return DreamMystery.geometry(id,DreamChronicle())
        if(id==cachedId)return cached!!
        val b=DreamMesh();val ivory=DreamShapes.ivory;val grass=floatArrayOf(.47f,.61f,.27f);val pink=DreamShapes.rose
        fun floor(x0:Float,x1:Float,z0:Float,z1:Float,c:FloatArray,k:Float=4f,step:Float=8f){var x=x0;while(x<x1){var z=z0;while(z<z1){val xx=min(x+step,x1);val zz=min(z+step,z1);fun p(a:Float,d:Float)=floatArrayOf(a,height(id,a,d)+(if(k==26f).025f else if(k==25f&&id==EDGE).015f else -.04f),d)
            b.quad(p(x,z),p(x,zz),p(xx,zz),p(xx,z),c,k);z=zz};x=min(x+step,x1)}}
        fun cloud(x:Float,y:Float,z:Float,r:Float){val n=floatArrayOf(0f,1f,0f);val a=floatArrayOf(x-r,y,z-r);val d=floatArrayOf(x+r,y,z-r);val e=floatArrayOf(x+r,y,z+r);val f=floatArrayOf(x-r,y,z+r);val ua=floatArrayOf(0f,0f,0f);val ud=floatArrayOf(1f,0f,0f);val ue=floatArrayOf(1f,1f,0f);val uf=floatArrayOf(0f,1f,0f)
            b.mappedTriangle(a,e,d,ua,ue,ud,n,n,n,92f);b.mappedTriangle(a,f,e,ua,uf,ue,n,n,n,92f)}
        fun puff(x:Float,y:Float,z:Float,rx:Float,ry:Float){
            for(a in listOf(0f,PI.toFloat()*.5f)){val dx=cos(a)*rx;val dz=sin(a)*rx;val n=floatArrayOf(-sin(a),0f,cos(a));val p0=floatArrayOf(x-dx,y-ry,z-dz);val p1=floatArrayOf(x+dx,y-ry,z+dz);val p2=floatArrayOf(x+dx,y+ry,z+dz);val p3=floatArrayOf(x-dx,y+ry,z-dz)
                val a0=floatArrayOf(0f,1f,0f);val a1=floatArrayOf(1f,1f,0f);val a2=floatArrayOf(1f,0f,0f);val a3=floatArrayOf(0f,0f,0f)
                b.mappedTriangle(p0,p1,p2,a0,a1,a2,n,n,n,92f);b.mappedTriangle(p0,p2,p3,a0,a2,a3,n,n,n,92f)}
        }
        when(id){
            SKY->{floor(-180f,180f,-280f,40f,floatArrayOf(.18f,.29f,.22f))
                for((i,p)in listOf(-65f to -85f,50f to -145f,10f to -215f).withIndex()){
                    val y=height(id,p.first,p.second)
                    for(j in 0..2){val a=j*PI.toFloat()/3;val dx=cos(a)*18f;val dz=sin(a)*18f;val n=floatArrayOf(-sin(a),0f,cos(a));val a0=floatArrayOf(p.first-dx,y,p.second-dz);val a1=floatArrayOf(p.first+dx,y,p.second+dz);val a2=floatArrayOf(p.first+dx*.65f,y+160f,p.second+dz*.65f);val a3=floatArrayOf(p.first-dx*.65f,y+160f,p.second-dz*.65f)
                        b.mappedTriangle(a0,a1,a2,floatArrayOf(0f,0f,0f),floatArrayOf(1f,0f,0f),floatArrayOf(1f,1f,0f),n,n,n,105f);b.mappedTriangle(a0,a2,a3,floatArrayOf(0f,0f,0f),floatArrayOf(1f,1f,0f),floatArrayOf(0f,1f,0f),n,n,n,105f)}
                    b.ellipsoid(p.first,y+.01f,p.second,19f,.02f,15f,floatArrayOf(.81f,.79f,.32f),4,24,24f)
                    for(j in 0..4)cloud(p.first+(j-2)*20f,y+155f+sin(j.toFloat())*8f,p.second,29f)
                }
                for(s in floatArrayOf(-1f,1f)){DreamSculpt.cylinder(b,s*90f,0f,-130f,.2f,25f,ivory,40f,8);DreamShapes.tube(b,listOf(floatArrayOf(s*90f,25f,-130f),floatArrayOf(s*80f,17f,-65f),floatArrayOf(s*90f,25f,10f)),.04f,ivory,40f,5)}
            }
            CLOUD->{
                // Collision carries the corridor; the cloud sea remains visible beneath the feet.
                for(z in -150..0 step 10){for(s in floatArrayOf(-1f,1f)){b.box(s*12f,3f,z.toFloat(),.22f,3f,.2f,ivory,0f);b.box(s*12f,5.9f,z-4.7f,.22f,.18f,4.7f,ivory,0f);if(z !in listOf(-50,-100))b.box(s*12f,2.8f,z-4.7f,.025f,2.5f,4.4f,floatArrayOf(.45f,.62f,.69f),34f)}
                    b.box(0f,6f,z.toFloat(),12f,.15f,.22f,ivory,0f);cloud(0f,.18f,z.toFloat(),13f);cloud(0f,6.1f,z.toFloat(),15f);for(x in listOf(-8f,0f,8f))puff(x,-.1f,z.toFloat(),5.5f,1.3f)}
                for(z in listOf(-50f,-100f)){floor(-40f,40f,z-8f,z+8f,ivory,25f);for(x in listOf(-38f,38f)){if(x<0f)DreamShapes.bed(b,-34f,0f,z);b.box(x,4.8f,z-7.7f,2.5f,3.2f,.08f,ivory,34f);cloud(x,.3f,z,17f)}}
                floor(-3.5f,3.5f,-185f,-150f,ivory,25f);for(i in 0 until 64){val a=i*2f*PI.toFloat()/64;val d=(i+1)*2f*PI.toFloat()/64;b.triangle(floatArrayOf(0f,-.04f,-214f),floatArrayOf(cos(d)*35f,-.04f,-214f+sin(d)*35f),floatArrayOf(cos(a)*35f,-.04f,-214f+sin(a)*35f),ivory,25f)}
                for(x in listOf(-24f,24f))for(z in listOf(-203f,-243f)){DreamSculpt.cylinder(b,x,0f,z,4.4f,22f,ivory,0f,24)
                    for(j in 0 until 24){val a=j*2f*PI.toFloat()/24;val d=(j+1)*2f*PI.toFloat()/24;b.triangle(floatArrayOf(x+cos(a)*5.2f,22f,z+sin(a)*5.2f),floatArrayOf(x+cos(d)*5.2f,22f,z+sin(d)*5.2f),floatArrayOf(x,34f,z),floatArrayOf(.57f,.63f,.71f),15f)}
                    b.box(x,12f,z+4.4f,.7f,2.6f,.04f,floatArrayOf(.29f,.43f,.56f),34f)
                }
                for(i in 0..12){val x=sin(i*2.399f)*65f;val z=-195f+cos(i*2.399f)*60f;cloud(x,-4f-i%3*3f,z,38f);puff(x,-5f,z,26f,8f)}
                b.box(0f,12f,-245f,19f,12f,.7f,ivory,0f);b.box(0f,8f,-244.2f,4f,7f,.06f,floatArrayOf(.38f,.55f,.68f),34f)
            }
            CITY->{floor(-230f,230f,-400f,60f,floatArrayOf(.68f,.72f,.24f),108f)
                for((i,t)in towers.withIndex()){val h=50f+(i*37%85);DreamSculpt.rounded(b,t.x,h*.5f,t.z,t.sx,h*.5f,t.sz,.6f,floatArrayOf(.30f,.42f,.57f),110f)
                    for(y in 4 until h.toInt() step 6){b.box(t.x,y.toFloat(),t.z+t.sz+.03f,t.sx-.5f,.06f,.04f,ivory,15f);for(x in -2..2)b.box(t.x+x*t.sx/3f,y.toFloat(),t.z+t.sz+.08f,.035f,2.5f,.03f,ivory,15f)}
                    if(i%3==0)DreamShapes.plant(b,t.x-t.sx-4f,0f,t.z,3f)}
                for(s in floatArrayOf(-1f,1f))DreamSculpt.rounded(b,s*17f,15f,-30f,1f,15f,4f,.3f,ivory,0f)
                DreamSculpt.rounded(b,0f,30f,-30f,18f,1f,4f,.3f,ivory,0f)
                for(i in 0..10)DreamSculpt.rounded(b,-17f+i*3.4f,.12f,18f,.8f,.12f,2f,.1f,ivory,25f)
                floor(-55f,-29f,-106f,-70f,ivory,26f);b.box(-42f,.02f,-88f,10f,.03f,14f,floatArrayOf(.18f,.56f,.70f),1f)
                for(x in floatArrayOf(-43f,-41f))DreamShapes.tube(b,listOf(floatArrayOf(x,.3f,-72f),floatArrayOf(x,1.5f,-72.2f),floatArrayOf(x,1.5f,-74f),floatArrayOf(x,-1f,-74f)),.055f,ivory,15f,9)
                // Moving train: a single material handles the complete rigid model.
                for(j in 0..2){val z=-242f-j*18f;DreamSculpt.rounded(b,32f,2.2f,z,2.8f,1.9f,8.4f,.55f,floatArrayOf(.57f,.72f,.72f),109f)
                    for(s in floatArrayOf(-1f,1f)){for(k in -3..3)b.box(32f+s*2.84f,2.8f,z+k*2f,.03f,.65f,.68f,floatArrayOf(.12f,.26f,.39f),109f)
                        for(k in floatArrayOf(-5.5f,5.5f))b.ellipsoid(32f+s*2.6f,.6f,z+k,.45f,.6f,.6f,DreamShapes.dark,7,12,109f)}}
                for(z in -320..-200 step 8){b.box(32f,.02f,z.toFloat(),3.2f,.015f,.18f,floatArrayOf(.39f,.40f,.32f),40f);for(x in floatArrayOf(30.5f,33.5f))b.box(x,.09f,z.toFloat(),.06f,.07f,4f,ivory,15f)}
                for(s in floatArrayOf(-1f,1f)){
                    DreamShapes.tube(b,listOf(floatArrayOf(32f+s*3.65f,1.15f,-359f),floatArrayOf(32f+s*3.65f,1.15f,-200f)),.055f,ivory,15f,7)
                    for(z in -356..-204 step 8)DreamSculpt.cylinder(b,32f+s*3.65f,0f,z.toFloat(),.055f,1.2f,ivory,15f,7)
                }
                for(i in 0..39){val x=sin(i*2.399f)*22f;val z=-48f-i*5.4f;val y=.35f+(i%4)*.07f
                    DreamShapes.tube(b,listOf(floatArrayOf(x,0f,z),floatArrayOf(x+.03f,y,z)),.018f,grass,38f,5)
                    val c=if(i%4==0)floatArrayOf(.51f,.41f,.76f)else if(i%3==0)ivory else floatArrayOf(.91f,.77f,.25f)
                    for(k in 0..5){val a=k*PI.toFloat()/3;val d=(k+1)*PI.toFloat()/3;b.triangle(floatArrayOf(x,y+.03f,z),floatArrayOf(x+cos(a)*.19f,y,z+sin(a)*.19f),floatArrayOf(x+cos(d)*.19f,y,z+sin(d)*.19f),c,38f)}
                }
                DreamSculpt.chair(b,24f,0f,-230f)
                for(i in 0..24){val a=i*2.399f;DreamShapes.plant(b,cos(a)*35f,0f,-140f+sin(a)*80f,1.1f)}
            }
            EDGE->{for(s in floatArrayOf(-1f,1f))for(z in -236 until 40 step 4)for(k in 0 until 32){
                    fun p(row:Float,col:Int):FloatArray{val edge=crack(row)+s*7f;val x=edge+(s*128f-edge)*col/32f;return floatArrayOf(x,-.04f,row)}
                    b.quad(p(z.toFloat(),k),p(z+4f,k),p(z+4f,k+1),p(z.toFloat(),k+1),grass,4f)
                }
                for(s in floatArrayOf(-1f,1f))for(z in -236 until 40 step 4){val x0=crack(z.toFloat())+s*7f;val x1=crack(z+4f)+s*7f;b.quad(floatArrayOf(x0,0f,z.toFloat()),floatArrayOf(x1,0f,z+4f),floatArrayOf(x1,-165f,z+4f),floatArrayOf(x0,-165f,z.toFloat()),grass,90f)}
                for(z in listOf(-50f,-145f)){floor(-22f,22f,z-3f,z+3f,ivory,25f);for(s in floatArrayOf(-1f,1f))DreamShapes.tube(b,listOf(floatArrayOf(-22f,1.2f,z+s*3f),floatArrayOf(22f,1.2f,z+s*3f)),.07f,pink,15f,8)}
                for(z in -205 until 15 step 2){val x=crack(z.toFloat())+20f;for(k in -1..1){val c=if(((z/2+k)and 1)==0)pink else ivory;floor(x+k*1.2f,x+(k+1)*1.2f,z.toFloat(),z+2f,c,26f)}}
                DreamSculpt.house(b,-35f,0f,-155f,pink,1.3f)
                DreamSculpt.cylinder(b,24f,0f,-190f,.18f,7f,ivory,43f,12);b.ellipsoid(24f,8f,-190f,2f,2f,.18f,ivory,14,28,15f)
                for(s in floatArrayOf(-1f,1f))b.ellipsoid(24f+s*.65f,8.5f,-189.78f,.20f,.30f,.05f,pink,7,12,15f)
                DreamShapes.tube(b,(0..20).map{val a=PI.toFloat()*.1f+it*PI.toFloat()*.8f/20;floatArrayOf(24f+cos(a)*1.3f,8f-sin(a)*1.1f,-189.76f)},.08f,pink,15f,8)
                for(i in 0..20)cloud(sin(i*2.399f)*110f,-25f-(i%4)*20f,-110f+cos(i*1.7f)*150f,45f)
            }
        }
        val g=gate(id);val y=height(id,g.x,g.z);b.box(g.x,y+1.6f,g.z,1.35f,1.6f,.035f,pink,29f);for(s in floatArrayOf(-1f,1f))b.box(g.x+s*1.6f,y+1.7f,g.z,.15f,1.7f,.15f,ivory,26f)
        cachedId=id;cached=b.finish();return cached!!
    }
}
