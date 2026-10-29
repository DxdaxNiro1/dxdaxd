package com.dreamcore.game

import kotlin.math.*

/** Nine readable reference videos: seven new environments plus office/cinema/coast remodelling. */
object DreamHorror32 {
    const val ATRIUM="horror_atrium32";const val GARDEN="horror_garden32";const val WITNESSES="horror_road32"
    const val FOREST="horror_forest32";const val HERBARIUM="horror_screens32";const val HOUSE="horror_house32";const val POOL="horror_pool32"
    val ids=listOf(ATRIUM,GARDEN,WITNESSES,FOREST,HERBARIUM,HOUSE,POOL)
    val titles=listOf("El coro de las puertas torcidas","El jardín de las antenas","El camino de los testigos","El bosque del sol vacío","El herbario de las pantallas","La casa del hilo","La piscina del horizonte")
    fun contains(id:String)=id in ids
    fun title(id:String)=titles[ids.indexOf(id).coerceAtLeast(0)]
    fun indoor(id:String)=id==ATRIUM
    fun spawn(id:String)=DreamJourney.Point(0f,7f)
    fun height(id:String,x:Float,z:Float)=if(id==POOL&&abs(x)<20f&&z in -90f..-32f)-1.8f*((-z-32f)/6f).coerceIn(0f,1f)else 0f
    fun canWalk(id:String,x:Float,z:Float):Boolean{
        if(!contains(id)||!x.isFinite()||!z.isFinite())return false
        if(id==HOUSE){val island=hypot(x,z+91f)<21f;return ((abs(x)<3.5f&&z in -94f..16f)||island)&&!(abs(x)<5.5f&&z in -99.1f..-88.9f)}
        if(id==ATRIUM)return abs(x)<14.4f&&z in -176f..16f
        if(id==WITNESSES)return abs(x)<9f&&z in -183f..16f
        if(id==POOL)return abs(x)<43f&&z in -181f..16f
        return abs(x)<65f&&z in -181f..16f
    }
    fun portalAt(id:String,x:Float,z:Float):String?=if(hypot(x,z-13f)<2.6f)ids.getOrNull(ids.indexOf(id)-1)?:DreamPocket.RETURN else if(hypot(x,z+end(id))<2.6f)ids.getOrNull(ids.indexOf(id)+1)?:DreamWorlds.STATION else null
    fun end(id:String)=if(id==HOUSE)109f else 171f
    fun create(id:String):DreamWorlds.Scene{
        require(contains(id));val b=DreamMesh();val stone=floatArrayOf(.48f,.51f,.53f);val ivory=floatArrayOf(.83f,.78f,.66f);val grass=floatArrayOf(.26f,.43f,.20f);val dark=floatArrayOf(.10f,.12f,.14f)
        fun p(x:Float,y:Float,z:Float)=floatArrayOf(x,y,z)
        fun floor(x0:Int,x1:Int,z0:Int,z1:Int,material:Float=14f){for(x in x0 until x1 step 4)for(z in z0 until z1 step 4){fun q(a:Int,c:Int)=p(a.toFloat(),height(id,a.toFloat(),c.toFloat())-.04f,c.toFloat())
            b.quad(q(x,z),q(x,z+4),q(x+4,z+4),q(x+4,z),if(material==0f)grass else stone,material)}}
        fun pole(x:Float,z:Float){DreamSculpt.cylinder(b,x,0f,z,.085f,7.3f,dark,15f,8);DreamShapes.tube(b,listOf(p(x,7.2f,z),p(x*.75f,7.7f,z),p(x*.65f,7.5f,z)),.08f,dark,15f,8);b.ellipsoid(x*.65f,7.4f,z,.35f,.08f,.45f,floatArrayOf(.85f,.69f,.36f),5,10,24f)}
        fun tree(x:Float,z:Float,h:Float){DreamSculpt.cylinder(b,x,0f,z,.28f,h,dark,43f,7);for(i in 0..5){val a=i*2.4f;val y=h*(.35f+i*.10f);DreamShapes.tube(b,listOf(p(x,y,z),p(x+cos(a)*h*.16f,y+h*.15f,z+sin(a)*h*.16f),p(x+cos(a)*h*.30f,y+h*.20f,z+sin(a)*h*.30f)),.06f,dark,43f,6)};DreamShapes.plant(b,x,h*.65f,z,h*.5f)}
        fun palm(x:Float,z:Float){
            DreamSculpt.cylinder(b,x,0f,z,.30f,15f,dark,43f,8)
            for(i in 0..6){val a=i*2f*PI.toFloat()/7;val dx=cos(a);val dz=sin(a)
                DreamShapes.tube(b,listOf(p(x,15f,z),p(x+dx*3.8f,16.2f,z+dz*3.8f),p(x+dx*6f,13.8f,z+dz*6f)),.065f,dark,43f,5)
                fun point(u:Float,t:Float):FloatArray{val w=sin(t*PI.toFloat())*1.75f*(u*2f-1f);return p(x+dx*t*6f-dz*w,15f+sin(t*PI.toFloat())*2.1f-t*1.2f,z+dz*t*6f+dx*w)}
                val n=p(0f,1f,0f)
                for(j in 0..7){val t=j/8f;val q=(j+1)/8f;val aa=point(0f,t);val c=point(1f,t);val d=point(1f,q);val e=point(0f,q);val ua=p(0f,1f-t,4f);val uc=p(1f,1f-t,4f);val ud=p(1f,1f-q,4f);val ue=p(0f,1f-q,4f)
                    b.mappedTriangle(aa,c,d,ua,uc,ud,n,n,n,111f);b.mappedTriangle(aa,d,e,ua,ud,ue,n,n,n,111f)}
            }
        }
        fun arch(x:Float,z:Float,width:Float,h:Float){for(s in floatArrayOf(-1f,1f)){DreamSculpt.cylinder(b,x+s*width,0f,z,.3f,h*.65f,stone,26f,10);val list=(0..12).map{val a=it*PI.toFloat()/24;p(x+s*width*cos(a),h*.65f+h*.35f*sin(a),z)};DreamShapes.tube(b,list,.32f,stone,26f,8)}}
        when(id){
            ATRIUM->{floor(-16,16,-180,20,42f)
                for(side in floatArrayOf(-1f,1f))for(i in 0..11){val z=-i*15f;val x=side*15.5f;b.box(x,6.2f,z,.25f,6.2f,7.5f,ivory,0f)
                    // Each bay is a complete tilted door-frame, with open centre and a normal floor.
                    val angle=side*(if(i%3==0).20f else -.11f);val doorway=DreamMesh();for(s in floatArrayOf(-1f,1f))doorway.box(s*1.4f,2.2f,0f,.14f,2.2f,.18f,dark,43f);doorway.box(0f,4.4f,0f,1.55f,.14f,.18f,dark,43f)
                    val data=doorway.finish().vertices;for(k in data.indices step 10){val a=data[k];val h=data[k+1];val depth=data[k+2];data[k]=x-side*.30f+depth;data[k+1]=h*cos(angle)+a*sin(angle);data[k+2]=z+a*cos(angle)-h*sin(angle);val nx=data[k+6];val ny=data[k+7];val nz=data[k+8];data[k+6]=nz;data[k+7]=ny*cos(angle)+nx*sin(angle);data[k+8]=nx*cos(angle)-ny*sin(angle)}
                    for(k in data.indices step 30)b.mappedTriangle(data.copyOfRange(k,k+3),data.copyOfRange(k+10,k+13),data.copyOfRange(k+20,k+23),data.copyOfRange(k+3,k+6),data.copyOfRange(k+13,k+16),data.copyOfRange(k+23,k+26),data.copyOfRange(k+6,k+9),data.copyOfRange(k+16,k+19),data.copyOfRange(k+26,k+29),43f)
                }
                for(z in -170..0 step 17){arch(0f,z.toFloat(),14f,12f);DreamSculpt.pendant(b,0f,11.2f,z.toFloat())}
                b.box(0f,12.6f,-80f,16f,.2f,100f,dark,14f)
                for(z in -150..-30 step 30)for(s in floatArrayOf(-1f,1f))DreamSculpt.chair(b,s*11.5f,0f,z.toFloat())
            }
            GARDEN->{floor(-100,100,-216,36,0f);for(s in floatArrayOf(-1f,1f))for(i in 0..12){val x=s*(30f+(i%3)*11f);tree(x,-i*16f,8f+(i%4)*2f)}
                for(z in -168..0 step 14){b.box(0f,.005f,z.toFloat(),2f,.012f,5f,ivory,12f);if(z%28==0)pole(-9f,z.toFloat())}
                for(i in 0..5){val a=i*PI.toFloat()/3;DreamShapes.tube(b,listOf(p(cos(a)*18f,0f,-128f+sin(a)*18f),p(cos(a)*9f,30f,-128f+sin(a)*9f),p(0f,35f,-128f)),.09f,dark,15f,7)}
            }
            WITNESSES->{floor(-12,12,-188,20,7f);for(s in floatArrayOf(-1f,1f)){
                for(z in -176..0 step 16){pole(s*8.5f,z.toFloat());palm(s*14f,z-5f)}
                for(z in -180..0 step 24){DreamSculpt.rounded(b,s*24f,5f,z.toFloat(),7f,5f,9f,.3f,stone,14f);for(h in 0..2)b.box(s*16.8f,2f+h*3f,z.toFloat(),.03f,.7f,1.4f,ivory,34f)}
            };for(z in -180..10 step 9)b.box(0f,.01f,z.toFloat(),.07f,.015f,2f,ivory,26f)}
            FOREST->{floor(-88,88,-208,28,0f);for(s in floatArrayOf(-1f,1f))for(i in 0..25){tree(s*(13f+(i%5)*9f),-i*7.6f,13f+(i%6)*2f)}
                for(z in -176..0 step 8)b.box(0f,.005f,z.toFloat(),3.2f,.012f,3.8f,dark,17f)
                b.ellipsoid(0f,85f,-250f,43f,43f,12f,floatArrayOf(.73f,.17f,.055f),20,32,24f);b.ellipsoid(2f,86f,-235f,37f,37f,7f,floatArrayOf(.014f,.012f,.015f),20,32,121f)
                for(z in -168..-30 step 30){DreamSculpt.cylinder(b,-7f,0f,z.toFloat(),.6f,1.2f,stone,26f,12);b.ellipsoid(-7f,1.35f,z.toFloat(),.08f,.17f,.08f,floatArrayOf(.85f,.39f,.12f),4,6,24f)}
            }
            HERBARIUM->{floor(-88,88,-208,28,0f)
                for(i in 0..33){val x=(if(i%2==0)-1 else 1)*(10f+(i%6)*6f);val z=-5f-i*5f;val y=1f+(i%3)*.25f;DreamSculpt.rounded(b,x,y,z,1.05f,.83f,.70f,.19f,ivory,26f);b.box(x,y,z+.72f,.84f,.59f,.02f,dark,95f);b.box(x,.25f,z,.25f,.25f,.25f,dark,15f)
                    for(j in 0..3)DreamShapes.plant(b,x-3f+j*1.5f,0f,z-2.5f,1.2f)}
                for(z in -132..-54 step 13){b.box(33f,3f,z.toFloat(),3.4f,3f,6.2f,floatArrayOf(.64f,.58f,.49f),15f);for(s in floatArrayOf(-1f,1f)){b.box(33f+s*3.45f,3.3f,z.toFloat(),.035f,1.3f,4.8f,floatArrayOf(.17f,.27f,.30f),34f);for(i in 0..3)DreamShapes.plant(b,32f+s*2f,0f,z-4f+i*2f,2.5f)}}
                // A clock arcade, visible from the meadow rather than an unrelated hidden slideshow.
                for(i in 0..9){val z=-30f-i*10f;DreamSculpt.cylinder(b,-33f,0f,z,.11f,6f,ivory,15f,8);b.ellipsoid(-33f,5.3f,z,.9f,.9f,.12f,ivory,12,24,26f);b.box(-33f,5.45f,z+.13f,.025f,.44f,.025f,dark,15f);b.box(-32.8f,5.3f,z+.13f,.22f,.025f,.025f,dark,15f)}
            }
            HOUSE->{// Smooth bowl, a suspended central island, and a walkable service bridge.
                val radius=140f;for(ring in 0 until 28)for(i in 0 until 120){val a=i*2*PI.toFloat()/120;val c=(i+1)*2*PI.toFloat()/120;val r0=22f+ring*(radius-22f)/28;val r1=22f+(ring+1)*(radius-22f)/28
                    fun q(r:Float,t:Float):FloatArray{val h=-67f+67f*((r-22f)/(radius-22f)).pow(.56f);return p(cos(t)*r,h,-91f+sin(t)*r)}
                    b.quad(q(r0,a),q(r0,c),q(r1,c),q(r1,a),grass,0f)
                }
                // The bowl belongs to a continuous meadow, rather than ending at a floating disc.
                for(ring in 0 until 10)for(i in 0 until 96){val a=i*2f*PI.toFloat()/96;val c=(i+1)*2f*PI.toFloat()/96;val ra=140f+ring*40f;val rb=ra+40f
                    b.quad(p(cos(a)*ra,0f,-91f+sin(a)*ra),p(cos(c)*ra,0f,-91f+sin(c)*ra),p(cos(c)*rb,0f,-91f+sin(c)*rb),p(cos(a)*rb,0f,-91f+sin(a)*rb),grass,0f)}
                DreamSculpt.cylinder(b,0f,-7f,-91f,21f,7f,grass,0f,96)
                b.box(0f,-.20f,-37f,3.6f,.20f,55f,stone,15f)
                for(s in floatArrayOf(-1f,1f)){DreamShapes.tube(b,listOf(p(s*3.2f,1.2f,14f),p(s*3.2f,1.2f,-78f)),.045f,ivory,15f,8);for(z in -76..14 step 6)DreamSculpt.cylinder(b,s*3.2f,0f,z.toFloat(),.035f,1.2f,ivory,15f,6)}
                DreamSculpt.house(b,0f,0f,-94f,floatArrayOf(.80f,.54f,.62f),1.25f)
                DreamShapes.tube(b,listOf(p(0f,10f,-91f),p(0f,155f,-91f)),.065f,dark,15f,8)
                for(i in 0..9){val a=i*2.4f;DreamShapes.plant(b,cos(a)*15f,0f,-91f+sin(a)*15f,3.2f)}
            }
            POOL->{floor(-48,48,-188,20,9f);for(s in floatArrayOf(-1f,1f)){b.box(s*20.4f,-.7f,-61f,.25f,1.2f,29f,ivory,9f);for(z in -174..0 step 7){DreamSculpt.cylinder(b,s*44f,0f,z.toFloat(),.045f,1.1f,ivory,15f,6)};DreamShapes.tube(b,listOf(p(s*44f,1.1f,14f),p(s*44f,1.1f,-184f)),.04f,ivory,15f,8)}
                b.box(0f,-.1f,-64f,20f,.02f,25f,floatArrayOf(.22f,.48f,.51f),1f)
                for(i in 0..5)b.box(0f,-.15f-i*.30f,-32.5f-i,4f,.15f,.5f,ivory,9f)
                b.box(0f,-2f,-345f,700f,.1f,160f,floatArrayOf(.22f,.35f,.42f),1f)
                for(z in -164..-106 step 14){DreamSculpt.chair(b,-32f,0f,z.toFloat());pole(34f,z.toFloat())}
            }
        }
        for(z in floatArrayOf(13f,-end(id))){val y=height(id,0f,z);for(s in floatArrayOf(-1f,1f))b.box(s*1.65f,y+1.9f,z,.18f,1.9f,.18f,ivory,26f);b.box(0f,y+3.9f,z,1.85f,.15f,.18f,ivory,26f);b.box(0f,y+1.8f,z,1.45f,1.8f,.025f,floatArrayOf(.34f,.50f,.52f),if(z>0)29f else 28f)}
        return b.finish()
    }
}
