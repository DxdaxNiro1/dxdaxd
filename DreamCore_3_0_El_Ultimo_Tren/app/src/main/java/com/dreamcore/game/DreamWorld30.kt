package com.dreamcore.game

import kotlin.math.*

/** Three optional train destinations. Geometry is static; interactive props are rendered independently. */
object DreamWorld30 {
    const val LIBRARY="dream_library30"
    const val FAIR="dream_fair30"
    const val SECRET="dream_station30"
    val ids=listOf(LIBRARY,FAIR,SECRET)
    fun contains(id:String)=id in ids
    fun title(id:String)=when(id){LIBRARY->"Biblioteca de lugares olvidados";FAIR->"La feria detenida";else->"El andén sin nombre"}
    fun spawn(id:String)=if(id==LIBRARY)DreamJourney.Point(0f,8f)else if(id==FAIR)DreamJourney.Point(-22f,5f)else DreamJourney.Point(0f,8f)
    fun height(id:String,x:Float,z:Float):Float{
        if(id==FAIR&&x in -32f..-18f&&z in -54f..-34f)return if(z> -40f)-( -34f-z)*.5f else -3f
        return 0f
    }
    fun canWalk(id:String,x:Float,z:Float):Boolean=when(id){
        LIBRARY->{if((-120..0 step 15).any{hypot(abs(x)-8f,z-it)<.48f})false else if(x !in -29f..29f||z !in -129f..12f)false else if(z in -88f..-24f&&abs(x) in 21.5f..25f&&!(x>0f&&z in -51f..-43f))false else if(z in -76.2f..-75.8f&&x in 12f..18f&&abs(x-15f)>1.3f)false else if(z in -88f..-76f&&(abs(x-12f)<.22f||abs(x-18f)<.22f))false else if(z in -88.2f..-87.8f&&x in 12f..18f)false else if(z in -115f..-100f&&abs(x)>10f&&abs(x)<12f)false else true}
        FAIR->x in -68f..68f&&z in -174f..20f&&!(z in -96f..-94f&&abs(x)>6f)&&!(hypot(x,z+69f)<8.5f)
        SECRET->x in -23f..23f&&z in -87f..12f
        else->false
    }
    fun create(id:String,story:DreamChronicle):DreamWorlds.Scene{
        val b=DreamMesh();val cream=DreamShapes.ivory;val rose=DreamShapes.rose;val blue=floatArrayOf(.25f,.40f,.54f)
        fun floor(x0:Int,x1:Int,z0:Int,z1:Int,step:Int=4,k:Float=42f){
            fun p(a:Int,c:Int)=floatArrayOf(a.toFloat(),height(id,a.toFloat(),c.toFloat())-.04f,c.toFloat())
            for(x in x0 until x1 step step)for(z in z0 until z1 step step)b.quad(p(x,z),p(x,z+step),p(x+step,z+step),p(x+step,z),cream,k)
        }
        if(id==LIBRARY){
            floor(-30,30,-132,16)
            for(s in floatArrayOf(-1f,1f)){
                b.box(s*30f,5f,-59f,.22f,5f,73f,cream,0f)
                // A shallow colonnade gives long sight lines without a dense forest of props.
                for(z in -120..0 step 15){DreamSculpt.cylinder(b,s*8f,0f,z.toFloat(),.24f,7f,cream,26f,12);DreamShapes.tube(b,listOf(floatArrayOf(s*8f,6.8f,z.toFloat()),floatArrayOf(s*4f,9.2f,z.toFloat()),floatArrayOf(0f,9.8f,z.toFloat())),.17f,cream,26f,10)}
                for(z in -88..-24 step 8){
                    if(s>0f&&z in -48..-40)continue
                    DreamSculpt.rounded(b,s*23f,2.5f,z.toFloat(),1.4f,2.5f,3.2f,.10f,DreamSculpt.wood,43f)
                    for(row in 0..3){b.box(s*21.45f,.65f+row*1.2f,z.toFloat(),.12f,.065f,3.25f,cream,26f)
                        for(i in 0..8){val tint=floatArrayOf(.26f+.045f*(i%4),.25f+.04f*((i+row)%5),.29f+.035f*(row%3));b.box(s*21.20f,.99f+row*1.2f,z-2.6f+i*.63f,.20f,.31f,.20f,tint,43f)}}
                }
                for(z in -110..0 step 22)DreamSculpt.pendant(b,s*5f,9f,z.toFloat())
                for(z in -118..-5 step 26){b.box(s*29.7f,5f,z.toFloat(),.025f,2.4f,4f,blue,34f);for(h in floatArrayOf(2.6f,7.4f))b.box(s*29.5f,h,z.toFloat(),.12f,.1f,4.15f,cream,26f)}
            }
            b.box(0f,10.2f,-59f,30f,.20f,73f,cream,0f);b.box(0f,5f,-132f,30f,5f,.2f,cream,0f)
            // A projection alcove, with no fictitious wall across its playable entrance.
            b.box(26f,3.2f,-55f,3f,3.2f,.12f,cream,0f);b.box(26f,3.2f,-40f,3f,3.2f,.12f,cream,0f)
            b.box(29f,3.2f,-47.5f,.12f,3.2f,7.5f,cream,0f)
            for(z in -126..-112 step 7)for(s in floatArrayOf(-1f,1f))DreamSculpt.chair(b,s*18f,0f,z.toFloat())
            // A real room behind the movable wardrobe, with a reachable entrance and a clue table.
            for(x in floatArrayOf(12f,18f))b.box(x,2f,-82f,.18f,2f,6f,cream,0f)
            for(x in floatArrayOf(12.65f,17.35f))b.box(x,2f,-76f,.65f,2f,.18f,cream,0f)
            b.box(15f,2f,-88f,3f,2f,.18f,cream,0f);b.box(15f,4.1f,-82f,3f,.12f,6f,cream,26f)
            DreamSculpt.chair(b,14f,0f,-85f);b.box(16f,.8f,-85f,.7f,.08f,.8f,rose,43f)
            // Actual anamorphic frame: three depths share rays from the viewing tile.
            val eye=floatArrayOf(0f,1.65f,-94f)
            val corners=listOf(floatArrayOf(-2f,0f,-124f),floatArrayOf(2f,0f,-124f),floatArrayOf(2f,4f,-124f),floatArrayOf(-2f,4f,-124f))
            for(i in 0..3){val t=.40f+i*.15f;val a=corners[i];val d=corners[(i+1)%4];val p=FloatArray(3){eye[it]+(a[it]-eye[it])*t};val q=FloatArray(3){eye[it]+(d[it]-eye[it])*t};DreamShapes.tube(b,listOf(p,q),.07f,floatArrayOf(.78f,.68f,.39f),25f,10)}
            DreamSculpt.cylinder(b,0f,0f,-94f,.65f,.015f,floatArrayOf(.77f,.69f,.50f),25f,32)
        }else if(id==FAIR){
            for(x in -72 until 72 step 4)for(z in -176 until 24 step 4){
                val k=if(abs(x)<6||abs(z+20)<4)42f else 0f
                val c=if(k==42f)floatArrayOf(.77f,.67f,.69f)else floatArrayOf(.32f,.47f,.27f)
                fun p(xx:Int,zz:Int)=floatArrayOf(xx.toFloat(),height(id,xx.toFloat(),zz.toFloat())-.06f,zz.toFloat())
                b.quad(p(x,z),p(x,z+4),p(x+4,z+4),p(x+4,z),c,k)
            }
            for(s in floatArrayOf(-1f,1f)){
                for(z in -156..0 step 26){val x=s*46f;DreamSculpt.rounded(b,x,2.4f,z.toFloat(),5.5f,2.4f,4f,.16f,if(z%52==0)rose else cream,0f)
                    b.box(x,3f,z+4.06f,3.4f,1.1f,.025f,blue,95f);DreamShapes.tube(b,listOf(floatArrayOf(x-6f,4.7f,z+4.8f),floatArrayOf(x,7f,z+4.8f),floatArrayOf(x+6f,4.7f,z+4.8f)),.18f,cream,26f,10)
                    for(i in -3..3)b.ellipsoid(x+i*1.3f,4.9f,z+4.6f,.10f,.10f,.10f,floatArrayOf(.94f,.76f,.39f),4,8,24f)
                }
                for(z in -160..10 step 18)DreamShapes.plant(b,s*61f,0f,z.toFloat(),2.8f)
            }
            // Curved carousel foundation; the moving canopy and horses are instanced by the prop renderer.
            DreamSculpt.cylinder(b,0f,0f,-69f,8.5f,.55f,rose,26f,48)
            for(s in floatArrayOf(-1f,1f)){b.box(s*36f,1.5f,-95f,30f,1.5f,.15f,cream,26f);for(x in 7..66 step 3)DreamSculpt.cylinder(b,s*x,0f,-95f,.055f,3.5f,rose,43f,6)}
            // Water chamber, ramp and fixed steps remain aligned with collision height.
            for(s in floatArrayOf(-1f,1f))b.box(-25f+s*7.25f,-1.4f,-47f,.18f,1.6f,7.2f,cream,0f)
            b.box(-25f,-1.4f,-54.2f,7.2f,1.6f,.18f,cream,0f)
            for(i in 0..5)b.box(-25f,-.25f-i*.5f,-34.5f-i,3.5f,.25f,.5f,cream,26f)
            for(i in 0..7){val a=i*2f*PI.toFloat()/8;DreamSculpt.cylinder(b,15f*cos(a),0f,-133f+15f*sin(a),.18f,5.5f,cream,26f,10)}
        }else{
            val pale=story.flags.any{it=="v30:secret-variant=2"}
            floor(-24,24,-88,16,4,if(pale)42f else 25f)
            for(s in floatArrayOf(-1f,1f))for(z in -80..0 step 12){DreamSculpt.cylinder(b,s*20f,0f,z.toFloat(),.38f,12f,cream,26f,16);if(pale)DreamSculpt.chair(b,s*15f,0f,z.toFloat())}
            if(pale){b.box(0f,12.2f,-38f,24f,.2f,52f,cream,26f);b.box(0f,6f,-88f,24f,6f,.2f,cream,0f)}
            else {for(i in 0..5){val a=i*PI.toFloat()/3;b.ellipsoid(55f*cos(a),35f+i*8f,-90f+25f*sin(a),5f+i,5f+i,5f+i,blue,14,24,55f+(i%4)*.1f)}}
        }
        val p=spawn(id);b.box(p.x,1.7f,12.5f,1.4f,1.7f,.04f,floatArrayOf(.40f,.68f,.72f),29f)
        return b.finish()
    }
}
