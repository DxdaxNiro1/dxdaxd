package com.dreamcore.game

import kotlin.math.*

/** Story objects, memory remnants and shelters are actual reusable models in the scene. */
object DreamChapterProps33 {
    fun model(name:String):DreamWorlds.Scene{
        val b=DreamMesh();val cream=floatArrayOf(.78f,.73f,.61f);val wood=floatArrayOf(.36f,.24f,.14f);val dark=floatArrayOf(.12f,.15f,.18f)
        when(name){
            "c33-table"->{b.box(0f,1.15f,0f,1.65f,.12f,1.15f,wood,43f);for(x in floatArrayOf(-1.45f,1.45f))for(z in floatArrayOf(-.95f,.95f))b.box(x,.55f,z,.06f,.55f,.06f,wood,43f)}
            "c33-wardrobe"->{for(side in floatArrayOf(-1f,1f))b.box(side*1.25f,1.8f,0f,.05f,1.8f,.65f,wood,43f);b.box(0f,1.8f,-.65f,1.3f,1.8f,.05f,wood,43f);b.box(0f,3.6f,0f,1.3f,.06f,.65f,wood,43f);b.box(0f,.04f,0f,1.3f,.04f,.65f,wood,43f);for(s in floatArrayOf(-1f,1f)){b.box(s*.64f,.91f,.68f,.60f,.87f,.04f,wood,43f);b.box(s*.64f,2.76f,.68f,.60f,.80f,.04f,wood,43f);b.ellipsoid(s*.12f,1.8f,.76f,.045f,.08f,.04f,cream,5,8,15f)};b.box(0f,2.2f,.73f,.035f,.35f,.025f,dark,15f)}
            "c33-door"->{for(s in floatArrayOf(-1f,1f))DreamSculpt.cylinder(b,s*.95f,0f,0f,.10f,3.1f,cream,26f,10);DreamShapes.tube(b,(0..16).map{val a=it*PI.toFloat()/16;floatArrayOf(cos(a)*.95f,3.1f+sin(a)*.75f,0f)},.11f,cream,26f,8);b.box(0f,1.55f,0f,.82f,1.55f,.025f,floatArrayOf(.26f,.38f,.43f),29f)}
            "c33-clock"->{DreamSculpt.cylinder(b,0f,0f,0f,.32f,1.2f,wood,43f,12);b.ellipsoid(0f,1.8f,0f,.62f,.7f,.18f,cream,12,24,26f);b.box(0f,1.98f,.20f,.025f,.35f,.025f,dark,15f);b.box(.18f,1.8f,.20f,.19f,.02f,.02f,dark,15f)}
            "c33-cylinder"->{DreamSculpt.cylinder(b,0f,.70f,0f,.22f,.65f,cream,15f,16);b.box(0f,.66f,0f,.60f,.07f,.55f,wood,43f);for(s in floatArrayOf(-1f,1f))b.box(s*.47f,.3f,0f,.055f,.30f,.38f,wood,43f)}
            "c33-cup"->{b.box(0f,.78f,0f,.7f,.07f,.55f,wood,43f);DreamShapes.cup(b,0f,.85f,0f);b.box(.4f,.88f,.12f,.18f,.015f,.14f,cream,26f)}
            "c33-memory"->{for(i in 0..5)b.ellipsoid((if(i%2==0)-.16f else .16f),.012f,-i*.44f,.10f,.012f,.19f,floatArrayOf(.46f,.32f,.15f),4,8,130f);DreamShapes.tube(b,listOf(floatArrayOf(-.8f,0f,-2f),floatArrayOf(-.8f,2.8f,-2f),floatArrayOf(.8f,2.8f,-2f),floatArrayOf(.8f,0f,-2f)),.045f,floatArrayOf(.70f,.51f,.25f),26f,8)}
            "c33-lit-window"->{b.box(0f,2.64f,0f,.85f,1.25f,.035f,floatArrayOf(1f,.70f,.27f),24f)}
            "c33-lamp"->{DreamSculpt.cylinder(b,0f,0f,0f,.035f,2.1f,wood,43f,8);b.ellipsoid(0f,2.15f,0f,.36f,.16f,.36f,cream,8,16,24f)}
            else->{b.box(0f,.65f,0f,.43f,.65f,.42f,wood,43f);b.box(0f,1.34f,0f,.35f,.018f,.27f,cream,26f);for(i in 0..3)b.box(-.2f+i*.12f,1.36f,0f,.015f,.008f,.18f,dark,15f)}
        }
        return b.finish()
    }
    fun instances(id:String,chapter:DreamChapter33,play:DreamPlay30,emit:(String,Float,Float,Float,Float,Float,Float)->Unit){
        for(n in chapter.nodes(id)){
            val y=DreamJourney.surface(id,n.x,n.z)
            if(n.key=="c33:escape")continue // Existing physical frame at the end is the exit.
            val tilt=if(chapter.giant>.1f)sin(play.time*7f+n.x)*chapter.giant*1.5f else 0f
            emit(n.model,n.x,y,n.z,0f,tilt,if(n.model=="c33-door").78f else 1f)
            if(n.key=="c33:examine"){val pulse=(play.time%4f);if(pulse<.20f||pulse in 1f..1.2f||pulse in 1.5f..1.7f)emit("c33-lamp",n.x+.7f,y,n.z,0f,0f,.38f)}
            if(chapter.past&&n.key=="c33:resolve")emit("c33-memory",n.x-1.2f,y+.01f,n.z,0f,0f,1f)
        }
        if(id==DreamWorlds.MEADOW&&chapter.flag("lamp-returned")){val y=DreamJourney.surface(id,66.4f,-62.7f);emit("c33-lit-window",66.4f,y,-62.7f,0f,0f,1f);emit("c33-lamp",66.4f,y,-65f,0f,0f,1f)}
        if(id==DreamVoyage.REFUGE&&chapter.flag("visitor-seen")){emit("c33-cup",if(chapter.flag("visitor-welcome"))3f else -6f,0f,-15f,0f,0f,1f);emit("c33-clock",3.8f,0f,-15f,0f,0f,.25f)}
        if(id==DreamPocket.TRAIN&&chapter.trainEvent>=3)emit("c33-door",0f,3.4f,-18f,0f,180f,.45f)
        if(id==DreamPocket.TRAIN&&chapter.trainEvent==1)emit("c33-wardrobe",0f,0f,-36f,0f,0f,2.8f)
    }
    fun presences(id:String,c:DreamChapter33):List<DreamEntities32.Presence>{
        val list=ArrayList<DreamEntities32.Presence>()
        if(id==DreamArchive33.ID&&c.chaseState in 1..3)list.add(DreamEntities32.Presence("antler",c.pursuerX,0f,c.pursuerZ,.85f))
        if(id==DreamArchive33.ID)list.add(DreamEntities32.Presence("archivist",-5f,0f,-109f,.85f))
        if(id==DreamVoyage.REFUGE&&c.flag("visitor-welcome")&&(!c.flag("visitor-moved")||c.flag("escaped")))list.add(DreamEntities32.Presence("archivist",-5f,0f,-6.5f,.83f))
        if(id==DreamWorld30.LIBRARY&&c.flag("visitor-moved")&&!c.flag("escaped"))list.add(DreamEntities32.Presence("archivist",9f,0f,-38f,.9f))
        if(id==DreamWorlds.MEADOW||id==DreamReferences.VILLAGE)list.add(DreamEntities32.Presence("star",c.trackerX,0f,c.trackerZ,1f))
        if(id==DreamPocket.TRAIN&&c.trainEvent>=2)list.add(DreamEntities32.Presence("archivist",-2.6f,-.6f,-20f,.75f))
        if(c.giant>.05f)list.add(DreamEntities32.Presence("gold",40f,25f,-190f,14f,phase=c.giant*8f))
        if(id==DreamWorlds.STATION&&c.value("ending")=="ALMA RECUERDA")list.add(DreamEntities32.Presence("archivist",-5f,0f,-8f,1f))
        return list
    }
}
