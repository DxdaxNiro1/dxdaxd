package com.dreamcore.game

import kotlin.math.*

/** Reusable local-space models. No scene is rebuilt to move a chair or rotate a ride. */
object DreamProps30 {
    fun model(name:String):DreamWorlds.Scene {
        val b=DreamMesh();val ivory=DreamShapes.ivory;val rose=DreamShapes.rose;val wood=DreamSculpt.wood;val metal=floatArrayOf(.31f,.39f,.43f)
        when(name){
            "kit"->{b.box(0f,.85f,0f,.7f,.06f,.5f,wood,43f);DreamSculpt.rounded(b,0f,1.05f,0f,.32f,.13f,.22f,.025f,metal,26f);for(x in floatArrayOf(-.14f,.14f))b.ellipsoid(x,1.19f,0f,.095f,.012f,.095f,ivory,8,12,26f);b.box(.46f,.96f,0f,.035f,.035f,.18f,ivory,26f)}
            "tickets"->{b.box(0f,.9f,0f,.7f,.06f,.5f,wood,43f);for(i in 0..1)b.box(-.3f+i*.6f,.98f,0f,.23f,.012f,.36f,if(i==0)floatArrayOf(.34f,.54f,.76f)else rose,26f)}
            "book"->{b.box(0f,.85f,0f,.65f,.07f,.55f,wood,43f);b.box(0f,1f,0f,.40f,.06f,.35f,rose,43f);b.box(0f,1.07f,0f,.38f,.01f,.33f,ivory,26f);for(i in 0..5)b.box(0f,1.083f,-.24f+i*.09f,.29f,.002f,.008f,metal,26f)}
            "bell"->{DreamSculpt.cylinder(b,0f,0f,0f,.035f,1.7f,metal,26f,8);b.ellipsoid(0f,1.7f,0f,.26f,.30f,.26f,ivory,12,16,26f);b.ellipsoid(0f,1.44f,0f,.11f,.07f,.11f,wood,10,12,26f)}
            "clock"->{DreamSculpt.cylinder(b,0f,0f,0f,.09f,2f,wood,43f,10);b.ellipsoid(0f,2.4f,0f,.62f,.62f,.10f,ivory,16,24,26f);for(i in 0..11){val a=i*PI.toFloat()/6;b.ellipsoid(sin(a)*.48f,2.4f+cos(a)*.48f,.11f,.025f,.025f,.012f,metal,5,8,26f)}}
            "relic"->{b.ellipsoid(0f,.25f,0f,.18f,.27f,.18f,floatArrayOf(.46f,.72f,.76f),10,14,24f)}
            "chair","shadow"->DreamSculpt.chair(b,0f,0f,0f)
            "wardrobe"->{DreamSculpt.rounded(b,0f,1.8f,0f,1.15f,1.8f,.45f,.08f,wood,43f);for(s in floatArrayOf(-1f,1f)){b.box(s*.55f,1.8f,.47f,.50f,1.65f,.025f,ivory,43f);b.ellipsoid(s*.14f,1.7f,.53f,.05f,.05f,.04f,metal,5,8,26f)}}
            "piano"->{DreamSculpt.rounded(b,0f,1.35f,0f,1.65f,1.35f,.65f,.12f,wood,43f);b.box(0f,1.35f,.96f,1.65f,.08f,.48f,ivory,26f);for(i in 0..13){b.box(-1.5f+i*.23f,1.45f,1.0f,.10f,.025f,.4f,ivory,26f);if(i%7 !in listOf(2,6))b.box(-1.39f+i*.23f,1.52f,.74f,.055f,.06f,.22f,metal,26f)};DreamSculpt.chair(b,0f,0f,2f)}
            "station"->{for(s in floatArrayOf(-1f,1f)){DreamSculpt.cylinder(b,s*2f,0f,0f,.09f,3.6f,ivory,26f,10);b.box(s*2.5f,.05f,-1f,.07f,.05f,4f,metal,26f)};b.box(0f,3.35f,0f,2.5f,.36f,.13f,rose,26f);b.box(0f,.05f,-1f,1.9f,.03f,4f,ivory,42f)}
            "train"->{DreamSculpt.rounded(b,0f,1.8f,0f,1.8f,1.8f,8f,.4f,ivory,26f);for(s in floatArrayOf(-1f,1f))for(i in -3..3){b.box(s*1.84f,2.3f,i*2f,.02f,.65f,.7f,metal,34f);b.ellipsoid(s*1.55f,.30f,i*2f,.30f,.30f,.30f,metal,8,10,26f)}}
            "board"->{DreamSculpt.cylinder(b,0f,0f,0f,.08f,1.5f,metal,26f,8);b.box(0f,1.8f,0f,1.15f,.6f,.1f,ivory,26f);for(i in 0..3)b.box(0f,1.45f+i*.2f,.11f,.9f,.025f,.01f,metal,26f)}
            "window","window-frame"->{for(s in floatArrayOf(-1f,1f))b.box(s*1.4f,2.7f,0f,.08f,1.7f,.12f,ivory,26f);for(h in floatArrayOf(1f,4.4f))b.box(0f,h,0f,1.5f,.08f,.12f,ivory,26f);b.box(0f,2.7f,0f,.04f,1.7f,.1f,ivory,26f);if(name=="window")b.box(0f,2.7f,-.06f,1.32f,1.6f,.02f,metal,34f)}
            "window-open"->{b.box(0f,2.7f,-.1f,1.3f,1.6f,.02f,rose,134f)}
            "curtain"->{for(i in 0..17){val x=-1.6f+i*.19f;val z=cos(i*.9f)*.10f; b.quad(floatArrayOf(x,4.5f,z),floatArrayOf(x,1f,z),floatArrayOf(x+.19f,1f,z+.04f),floatArrayOf(x+.19f,4.5f,z+.04f),ivory,122f)}}
            "gate"->{for(s in floatArrayOf(-1f,1f))b.box(s*1.6f,1.8f,0f,.10f,1.8f,.12f,ivory,26f);b.box(0f,3.65f,0f,1.7f,.1f,.12f,ivory,26f)}
            "door"->{b.box(0f,1.8f,0f,1.5f,1.8f,.1f,wood,43f);b.ellipsoid(1.2f,1.6f,.15f,.08f,.08f,.05f,ivory,8,10,26f)}
            "model"->{b.box(0f,.9f,0f,1.9f,.12f,1.4f,wood,43f);for(s in floatArrayOf(-1f,1f))b.box(s*1.7f,.45f,0f,.09f,.45f,1.1f,wood,43f);b.box(0f,1.55f,0f,1.2f,.55f,.8f,ivory,26f);b.box(0f,1.55f,.82f,.35f,.50f,.02f,rose,43f);DreamShapes.tube(b,listOf(floatArrayOf(-1.4f,2.1f,0f),floatArrayOf(0f,2.9f,0f),floatArrayOf(1.4f,2.1f,0f)),.14f,rose,26f,8)}
            "projector"->{b.box(0f,.9f,0f,1.1f,.10f,.8f,wood,43f);DreamSculpt.rounded(b,0f,1.25f,0f,.55f,.25f,.4f,.08f,metal,26f);b.ellipsoid(0f,1.25f,-.43f,.20f,.20f,.10f,ivory,8,12,24f)}
            "bed"->{DreamSculpt.rounded(b,0f,.55f,0f,1.8f,.55f,3f,.20f,rose,122f);DreamSculpt.rounded(b,0f,1.15f,-2f,1.6f,.20f,.7f,.15f,ivory,122f)}
            "valve"->{DreamSculpt.cylinder(b,0f,0f,0f,.17f,1.7f,metal,26f,12);for(i in 0..15){val a=i*PI.toFloat()/8;val d=(i+1)*PI.toFloat()/8;DreamShapes.tube(b,listOf(floatArrayOf(cos(a)*.7f,1.7f+sin(a)*.7f,0f),floatArrayOf(cos(d)*.7f,1.7f+sin(d)*.7f,0f)),.07f,rose,26f,6)}}
            "water"->b.quad(floatArrayOf(-7f,0f,-7f),floatArrayOf(-7f,0f,7f),floatArrayOf(7f,0f,7f),floatArrayOf(7f,0f,-7f),floatArrayOf(.26f,.52f,.62f),1f)
            "carousel"->{DreamSculpt.cylinder(b,0f,0f,0f,.22f,6f,ivory,26f,16);for(i in 0..23){val a=i*PI.toFloat()/12;val c=(i+1)*PI.toFloat()/12;b.triangle(floatArrayOf(0f,7.2f,0f),floatArrayOf(cos(a)*8f,5.3f,sin(a)*8f),floatArrayOf(cos(c)*8f,5.3f,sin(c)*8f),if(i%2==0)rose else ivory,26f)}}
            "horse"->{b.ellipsoid(0f,1.7f,0f,.45f,.55f,1.0f,ivory,10,16,26f);b.ellipsoid(0f,2.45f,-.75f,.28f,.7f,.30f,ivory,10,14,26f);b.ellipsoid(0f,2.85f,-1.05f,.25f,.28f,.43f,ivory,10,14,26f);for(s in floatArrayOf(-1f,1f))for(z in floatArrayOf(-.65f,.65f))DreamShapes.tube(b,listOf(floatArrayOf(s*.3f,1.6f,z),floatArrayOf(s*.38f,.65f,z+.2f)),.11f,ivory,26f,8);DreamSculpt.cylinder(b,0f,0f,0f,.045f,5.3f,rose,26f,8)}
            "telescope"->{for(i in 0..2){val a=i*PI.toFloat()*2/3;DreamShapes.tube(b,listOf(floatArrayOf(0f,1.7f,0f),floatArrayOf(cos(a),0f,sin(a))),.05f,metal,26f,8)};DreamShapes.tube(b,listOf(floatArrayOf(0f,1.7f,.8f),floatArrayOf(0f,2.1f,-1f)),.28f,ivory,26f,16)}
            "barter"->{DreamSculpt.rounded(b,0f,2.8f,0f,.55f,2.8f,.38f,.18f,metal,26f);b.ellipsoid(0f,5.9f,0f,.42f,.65f,.4f,ivory,12,16,26f);DreamShapes.tube(b,listOf(floatArrayOf(.3f,4f,0f),floatArrayOf(1.2f,2.8f,1f),floatArrayOf(.6f,2.5f,1.7f)),.07f,ivory,26f,10)}
            "sky-sign"->{for(i in 0..31){val a=i*PI.toFloat()/16;val d=(i+1)*PI.toFloat()/16;DreamShapes.tube(b,listOf(floatArrayOf(cos(a)*.45f,sin(a)*.45f,0f),floatArrayOf(cos(d)*.45f,sin(d)*.45f,0f)),.025f,ivory,24f,6)}}
            else->{b.box(0f,.85f,0f,.7f,.08f,.45f,wood,43f);DreamSculpt.rounded(b,0f,1.02f,0f,.38f,.08f,.28f,.03f,rose,26f)}
        }
        val raw=b.finish().vertices
        if(name=="shadow")for(i in raw.indices step 10){raw[i+1]*=.012f;raw[i+3]=.03f;raw[i+4]=.035f;raw[i+5]=.045f;raw[i+9]=130f}
        return DreamWorlds.Scene(raw)
    }
    fun marks(play:DreamPlay30,id:String):DreamWorlds.Scene{
        val b=DreamMesh()
        for(m in play.marks())if(m.world==id){val c=if(m.chalk)floatArrayOf(.81f,.83f,.77f)else floatArrayOf(.20f,.24f,.22f)
            fun p(x:Float,z:Float)=floatArrayOf(m.x+x*cos(m.yaw)+z*sin(m.yaw),m.y+.025f,m.z-x*sin(m.yaw)+z*cos(m.yaw))
            if(m.chalk){b.triangle(p(0f,-.55f),p(-.23f,-.15f),p(.23f,-.15f),c,25f);b.quad(p(-.045f,-.15f),p(-.045f,.35f),p(.045f,.35f),p(.045f,-.15f),c,25f)}else for(s in floatArrayOf(-1f,1f))b.quad(p(s*.17f-.09f,-.17f),p(s*.17f-.09f,.17f),p(s*.17f+.09f,.17f),p(s*.17f+.09f,-.17f),c,130f)
        };return b.finish()
    }
    fun instances(id:String,play:DreamPlay30,x:Float,z:Float,draw:(String,Float,Float,Float,Float,Float,Float)->Unit){
        fun instance(name:String,px:Float,py:Float,pz:Float,angle:Float=0f,tilt:Float=0f,scale:Float=1f){
            if(hypot(px-x,pz-z)<=105f||name=="sky-sign")draw(name,px,py,pz,angle,tilt,scale)
        }
        for(n in play.nodes(id)){
            if(n.model.isEmpty()||n.model in listOf("marker","carousel"))continue
            if(n.key=="water-reward"&&play.number("water",2)>0)continue
            if(n.key=="piano-reward"&&!play.flag("piano:tune"))continue
            if(n.key=="perspective-door"&&!play.flag("perspective"))continue
            val p=play.pose(n,id)
            if(n.key=="hidden-door"&&!play.flag("hidden:wardrobe")){instance("door",p.x,p.y,p.z)}
            instance(if(n.key=="window"&&play.flag("window:$id"))"window-frame" else n.model,p.x,p.y,p.z)
            when(n.key){
                "train"->{val t=play.time%64f;val offset=when{t<7f->(7f-t)*(7f-t)*1.5f;t<36f->0f;else->(t-36f)*(t-36f)*.7f};if(offset<100f)instance("train",p.x-5f,p.y,p.z-offset)}
                "window"->{instance("curtain",p.x-2f,p.y,p.z+.2f);if(play.flag("window:$id")){instance("window-open",p.x,p.y,p.z+.02f);instance("window",p.x+1.4f,p.y,p.z,65f)}}
                "chair"->instance("shadow",p.x+.45f,p.y+.02f,p.z+.2f,0f,-play.shadowRise*82f,1.3f)
                "model"->instance("door",p.x,p.y+1.05f,p.z+.86f,if(play.flag("model:open"))80f else 0f,scale=.32f)
                "gate"->if(id==DreamWorld30.FAIR){val a=if(play.flag("model:open"))87f else 0f;val rad=a*PI.toFloat()/180;instance("door",p.x-5.25f+cos(rad)*5.25f,p.y,p.z-sin(rad)*5.25f,a,scale=3.5f)}
            }
        }
        if(id==DreamWorld30.LIBRARY){
            if(play.number("slide")>=1){instance("chair",25f,0f,-45f);instance("window-open",27f,0f,-54.5f)}
            if(play.number("slide")>=2)instance("water",26f,.12f,-47f,scale=.22f)
            if(play.number("slide")>=3)instance("bed",26f,0f,-48f,scale=.7f)
            if(play.flag("perspective"))instance("gate",0f,0f,-124f)
            if(play.flag("piano:tune"))instance("relic",-15f,0f,-23f)
        }
        if(id==DreamWorld30.FAIR){
            val a=play.carousel*180f/PI.toFloat();instance("carousel",0f,.55f,-69f,a)
            for(i in 0..5){val r=play.carousel+i*PI.toFloat()/3;instance("horse",cos(r)*5.3f,.6f+sin(play.time*1.4f+i)*.22f,-69f+sin(r)*5.3f,-a-i*60f)}
            if(play.number("water",2)>0)instance("water",-25f,-3f+play.number("water",2)*1.35f,-47f)
        }
        if(play.number("blink")>0&&play.number("blink")%2==1)play.nodes(id).firstOrNull{it.model=="station"||it.key=="chair"}?.let{val p=play.pose(it,id);instance("relic",p.x+2f,p.y+.2f,p.z-1f)}
        if(play.telescope){instance("sky-sign",x,38f,z-120f);if(play.flag("gift:glass"))instance("sky-sign",x+1.8f,38f,z-120f)}
        if(play.flag("gift:glass"))play.station(id)?.let{instance("relic",it.x+2f,.4f,it.z)}
    }

}
