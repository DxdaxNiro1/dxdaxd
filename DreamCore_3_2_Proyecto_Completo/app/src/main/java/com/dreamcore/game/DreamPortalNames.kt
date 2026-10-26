package com.dreamcore.game

import kotlin.math.*

/** Screen-space labels: shared route registry, occluded by actual geometry, no text textures. */
object DreamPortalNames {
    data class Gate(val x:Float,val y:Float,val z:Float,val name:String)
    data class Label(val x:Float,val y:Float,val name:String,val alpha:Float,val anchorY:Float=y)
    fun gates(id:String):List<Gate> = routeGates(id)+DreamObjects.items(id).filter{it.kind=="door" || it.kind=="tower" || it.kind=="transition" || it.key=="mystery:visit" || it.key in listOf("rule:cloud","rule:city","rule:field","rule:board")}.map{
        Gate(it.x,DreamJourney.surface(id,it.x,it.z)+3.8f,it.z-1.5f,when{it.kind=="investigation"->"La casa del timbre";it.kind=="rule"->DreamRules23.label(it.key);it.kind=="transition"->if(it.key.startsWith("lift"))"ASCENSOR · La ciudad bajo la lluvia" else "AGUA · Jardines de cristal";else->DreamObjects.doorTitle(it.key)})
    }
    private fun routeGates(id:String):List<Gate>{
        fun gate(x:Float,z:Float,name:String)=Gate(x,DreamJourney.surface(id,x,z)+3.8f,z,name)
        if(id==DreamArchive33.ID)return listOf(gate(0f,13f,"VOLVER · la puerta anterior"))
        if(DreamHorror32.contains(id)){val next=DreamHorror32.ids.getOrNull(DreamHorror32.ids.indexOf(id)+1);val previous=DreamHorror32.ids.getOrNull(DreamHorror32.ids.indexOf(id)-1);return listOf(gate(0f,13f,if(previous==null)"VOLVER · lugar anterior" else "VOLVER · "+DreamHorror32.title(previous)),gate(0f,-DreamHorror32.end(id),if(next==null)"VOLVER · El Umbral" else "SEGUIR · "+DreamHorror32.title(next)))}
        if(DreamWorld30.contains(id)){val p=DreamWorld30.spawn(id);return listOf(gate(p.x,12.5f,"VOLVER · lugar anterior"))}
        if(id==DreamWorlds.STATION)return listOf(gate(20f,-24f,"TESTIGOS · 7 lugares de terror"),gate(-12f,-2f,"EL ÚLTIMO TREN"),gate(12f,-3f,"BIBLIOTECA"),gate(-8f,-16f,"DREAMCORE · 13 mundos"),gate(8f,-16f,"DARK FANTASY · 4 mundos"),gate(0f,-24f,"LUGARES IMPOSIBLES · 11 mundos"),gate(-14f,8f,"BACKROOMS · Las luces"),gate(14f,8f,"DESVÍOS · Otros caminos"),gate(0f,16f,"SANTUARIO · El ojo entre nubes"))
        if(DreamVigilia27.contains(id))return DreamVigilia27.gates(id).map{p->val target=DreamVigilia27.portalAt(id,p.x,p.z)!!;gate(p.x,p.z,if(target==DreamPocket.RETURN)"VOLVER · lugar anterior" else DreamJourney.title(target))}
        if(DreamRoutes23.contains(id)){val p=DreamRoutes23.gate(id);return listOf(gate(p.x,p.z,"VOLVER · lugar anterior"))}
        if(DreamNewDreams.contains(id))return DreamNewDreams.gates(id).map{gate(it.x,it.z,"VOLVER · lugar anterior")}
        if(id==DreamJourney.DARK)return listOf(gate(0f,44f,"VOLVER · El Umbral"),gate(0f,-66f,"SEGUIR · La catedral de las lunas"))
        if(id==DreamHorizons.DARK)return listOf(gate(0f,30f,"VOLVER · Las lunas dormidas"),gate(0f,-330f,"SEGUIR · El pueblo de la luna escarlata"))
        if(DreamReferences.contains(id)||id in DreamJourney.sequence){val a=DreamJourney.entry(id);val b=DreamJourney.exit(id)
            fun destination(target:String)=if(target==DreamPocket.RETURN)"lugar anterior" else DreamJourney.title(target)
            return listOf(gate(a.x,a.z,"VOLVER · "+destination(DreamJourney.previous(id))),gate(b.x,b.z,"SEGUIR · "+destination(DreamJourney.next(id))))
        }
        if(DreamVoyage.contains(id)){val p=DreamVoyage.gate(id);return listOf(gate(p.x,p.z,"VOLVER · lugar anterior"))}
        if(DreamPocket.contains(id))return listOf(gate(0f,8f,"VOLVER · lugar anterior"))
        return emptyList()
    }
    fun project(id:String,eye:FloatArray,m:FloatArray,clear:(Float,Float,Float,Float)->Boolean={_,_,_,_->true}):List<Label>{
        val candidates=gates(id).mapNotNull{g->
            val dx=g.x-eye[0];val dy=g.y-eye[1];val dz=g.z-eye[2];val distance=sqrt(dx*dx+dy*dy+dz*dz)
            if(distance !in 2f..85f)return@mapNotNull null
            val w=m[3]*g.x+m[7]*g.y+m[11]*g.z+m[15];if(w<=.1f)return@mapNotNull null
            val x=(m[0]*g.x+m[4]*g.y+m[8]*g.z+m[12])/w
            val y=(m[1]*g.x+m[5]*g.y+m[9]*g.z+m[13])/w
            val depth=(m[2]*g.x+m[6]*g.y+m[10]*g.z+m[14])/w
            if(abs(x)>.88f || abs(y)>.72f || depth !in -1f..1f || !clear(dx/distance,dy/distance,dz/distance,(distance-.8f).coerceAtLeast(.1f)))return@mapNotNull null
            distance to Label(x*.5f+.5f,.5f-y*.5f,g.name,((90f-distance)/25f).coerceIn(.25f,1f))
        }.sortedBy{it.first}
        val result=ArrayList<Label>()
        for((_,label)in candidates){
            var y=label.y
            repeat(4){if(result.any{abs(it.x-label.x)<.27f && abs(it.y-y)<.075f})y-=.085f}
            if(y<.13f)continue
            result.add(label.copy(y=y));if(result.size==3)break
        }
        return result
    }
}
