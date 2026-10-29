package com.dreamcore.game

import kotlin.math.abs
import kotlin.math.hypot

/** One ordered Dreamcore journey and an independent Dark Fantasy branch. */
object DreamJourney {
    const val DARK = "dark_01"
    val sequence = listOf(DreamWorlds.POOLS,DreamWorlds.CRATER,DreamExpansion.TUNNELS,
        DreamWorlds.GARDEN,DreamWorlds.MEADOW,
        DreamWorlds.AQUARIUM,DreamExpansion.CHESS,DreamExpansion.CLOUD_HOTEL,
        DreamExpansion.ROAD,DreamWaterpark.ID,DreamHorizons.CELESTIAL,DreamHorizons.CITY,DreamWorlds.HOUSE_CRATER)
    data class Point(val x:Float,val z:Float)
    data class Arrival(val x:Float,val z:Float,val yaw:Float)
    private val entries = listOf(Point(0f,7.6f),Point(-7.2f,24f),Point(0f,52f),
        Point(0f,30f),Point(-11f,27f),Point(0f,6f),Point(0f,27f),
        Point(0f,60f),Point(0f,40f),Point(0f,60f),Point(0f,6f))
    private val exits = listOf(Point(0f,-28.4f),Point(0f,DreamWorlds.CRATER_HOUSE_FRONT_Z),Point(0f,-52f),
        Point(0f,-184f),Point(0f,-132.3f),Point(0f,-103f),Point(0f,-187f),
        Point(0f,-60f),Point(0f,-40f),Point(0f,-66f),Point(0f,-75f))
    fun entry(id:String)=if(DreamReferences.contains(id))DreamReferences.entry(id)else if(DreamHorizons.contains(id))DreamHorizons.entry(id)else if(id==DreamWaterpark.ID)Point(DreamWaterpark.centre(90f),90f)else if(DreamLands.contains(id))DreamLands.entry(id)else entries[sequence.indexOf(id)]
    fun exit(id:String)=if(DreamReferences.contains(id))DreamReferences.exit(id)else if(DreamHorizons.contains(id))DreamHorizons.exit(id)else if(id==DreamWaterpark.ID)Point(DreamWaterpark.centre(-495f),-495f)else if(DreamLands.contains(id))DreamLands.exit(id)else exits[sequence.indexOf(id)]
    fun previous(id:String):String {
        if(DreamReferences.contains(id))return DreamReferences.previous(id)
        val i=sequence.indexOf(id);return if(i<=0)DreamWorlds.STATION else sequence[i-1]
    }
    fun next(id:String):String {
        if(DreamReferences.contains(id))return DreamReferences.next(id)
        val i=sequence.indexOf(id);return if(i<0 || i==sequence.lastIndex)DreamWorlds.STATION else sequence[i+1]
    }
    fun title(id:String):String=when(id) {
        DreamRainward34.ID -> DreamRainward34.TITLE
        DreamWorlds.STATION -> "El Umbral"
        DreamHorizons.DARK -> DreamHorizons.title(id)
        DARK -> "El reino de las lunas dormidas"
        DreamWaterpark.ID -> "El parque de las lágrimas suspendidas"
        else -> if(id==DreamArchive33.ID)"El depósito de las voces" else if(DreamHorror32.contains(id))DreamHorror32.title(id)else if(DreamWorld30.contains(id))DreamWorld30.title(id)else if(DreamVigilia27.contains(id))DreamVigilia27.title(id)else if(DreamRoutes23.contains(id))DreamRoutes23.title(id)else if(DreamNewDreams.contains(id))DreamNewDreams.title(id)else if(DreamReferences.contains(id))DreamReferences.title(id)else if(DreamVoyage.contains(id))DreamVoyage.title(id)else if(DreamHorizons.contains(id))DreamHorizons.title(id)else if(DreamPocket.contains(id))DreamPocket.title(id)else if(DreamLands.contains(id))DreamLands.title(id)else DreamExpansion.title(id)
    }
    fun hudTitle(id:String):String {
        val index=sequence.indexOf(id)
        return when {
            id==DreamWorlds.STATION -> "EL UMBRAL  /  Elige tu experiencia"
            DreamHorror32.contains(id) -> "TESTIGOS ${DreamHorror32.ids.indexOf(id)+1}/7 / ${title(id)}"
            DreamReferences.contains(id) -> "OTROS SUEÑOS ${DreamReferences.ids.indexOf(id)+1}/${DreamReferences.ids.size} / ${title(id)}"
            id==DARK -> "DARK FANTASY  /  Las lunas dormidas"
            index>=0 -> "DREAMCORE ${index+1}/${sequence.size}  /  ${title(id)}"
            else -> title(id)
        }
    }
    fun worldCode(id:String):Float=if(id==DreamRainward34.ID)63f else if(id==DreamArchive33.ID)62f else if(DreamHorror32.contains(id))55f+DreamHorror32.ids.indexOf(id)else if(DreamWorld30.contains(id))52f+DreamWorld30.ids.indexOf(id)else if(DreamVigilia27.contains(id))49f+DreamVigilia27.ids.indexOf(id)else if(DreamRoutes23.contains(id))43f+DreamRoutes23.ids.indexOf(id)else if(DreamNewDreams.contains(id))41f+DreamNewDreams.ids.indexOf(id)else if(DreamReferences.contains(id))30f+DreamReferences.ids.indexOf(id)else if(id in DreamVoyage.photos)worldCode(DreamVoyage.base(id))else when(id){DreamVoyage.REFUGE->22f;DreamVoyage.LETTER->23f;DreamGravity.ID->24f;DreamHorizons.CELESTIAL->17f;DreamHorizons.CITY->18f;DreamHorizons.DARK->19f;DreamHorizons.TOWER->20f;DreamHorizons.BED->21f;DARK->12f;DreamWaterpark.ID->13f;DreamPocket.LAUNDRY->14f;DreamPocket.TRAIN->15f;DreamPocket.DAWN->16f;else->DreamExpansion.worldCode(id)}
    fun surface(id:String,x:Float,z:Float):Float=if(DreamHorror32.contains(id))DreamHorror32.height(id,x,z)else if(DreamWorld30.contains(id))DreamWorld30.height(id,x,z)else if(DreamVigilia27.contains(id))DreamVigilia27.height(id,x,z)else if(DreamRoutes23.contains(id))DreamRoutes23.height(id,x,z)else if(DreamNewDreams.contains(id))DreamNewDreams.height(id,x,z)else if(DreamReferences.contains(id))DreamReferences.height(id,x,z)else if(DreamVoyage.contains(id))DreamVoyage.surface(id,x,z)else if(DreamHorizons.contains(id))DreamHorizons.height(id,x,z)else when(id) {
        DreamWorlds.STATION -> DreamWorlds.groundHeight(x,z)
        DreamWorlds.CRATER -> DreamWorlds.craterWalkSurface(x,z)
        DreamWorlds.MEADOW -> DreamLands.height(id,x,z)
        DreamWaterpark.ID -> DreamWaterpark.height(x,z)
        else -> 0f
    }
    fun arrival(target:String,from:String):Arrival {
        if(target==DreamRainward34.ID)return Arrival(0f,6f,0f)
        if(target==DreamWorlds.HOUSE_MEADOW)return Arrival(0f,4f,0f)
        if(target==DreamArchive33.ID)return Arrival(0f,6f,0f)
        if(DreamHorror32.contains(target)){if(DreamHorror32.ids.getOrNull(DreamHorror32.ids.indexOf(target)+1)==from)return Arrival(0f,-DreamHorror32.end(target)+6f,kotlin.math.PI.toFloat());val p=DreamHorror32.spawn(target);return Arrival(p.x,p.z,0f)}
        if(DreamWorld30.contains(target)){val p=DreamWorld30.spawn(target);return Arrival(p.x,p.z,0f)}
        if(target==DreamWorlds.STATION)return Arrival(0f,6f,0f)
        if(DreamVigilia27.contains(target)){val p=DreamVigilia27.spawn(target);return if(from==DreamVigilia27.DARK_FOUR && target==DreamVigilia27.DARK_THREE)Arrival(0f,-279f,kotlin.math.PI.toFloat())else Arrival(p.x,p.z,0f)}
        if(DreamRoutes23.contains(target)){val p=DreamRoutes23.spawn(target);return Arrival(p.x,p.z,0f)}
        if(DreamNewDreams.contains(target)){val p=DreamNewDreams.spawn(target);return Arrival(p.x,p.z,0f)}
        if(target==DARK)return if(from==DreamHorizons.DARK)Arrival(0f,-59f,kotlin.math.PI.toFloat())else Arrival(0f,36f,0f)
        if(target==DreamHorizons.DARK)return if(from==DreamVigilia27.DARK_THREE)Arrival(0f,-323f,kotlin.math.PI.toFloat())else Arrival(0f,24f,0f)
        if(DreamReferences.contains(target)){val backwards=from==DreamReferences.next(target);val a=if(backwards)DreamReferences.exit(target)else DreamReferences.entry(target);return Arrival(a.x,a.z+(if(backwards)5f else -5f),if(backwards)kotlin.math.PI.toFloat()else 0f)}
        if(DreamVoyage.contains(target)){val p=DreamVoyage.spawn(target);return Arrival(p.x,p.z,0f)}
        if(DreamPocket.contains(target))return Arrival(0f,2f,0f)
        val i=sequence.indexOf(target)
        require(i>=0)
        val backwards=from==next(target) && from!=DreamWorlds.STATION
        val p=if(backwards)exit(target)else entry(target)
        if(target==DreamWorlds.CRATER) {
            return if(backwards)Arrival(0f,DreamWorlds.CRATER_CENTER_Z+9.8f,0f)
                else Arrival(0f,26f,0f)
        }
        // Arrivals stay six metres inside the route, clear of the previous gate.
        return Arrival(p.x,p.z+(if(backwards)7f else -6f),
            if(backwards)kotlin.math.PI.toFloat() else 0f)
    }
    fun portalAt(id:String,x:Float,z:Float,feet:Float):String? {
        if(id==DreamRainward34.ID)return if(hypot(x,z-13f)<2.5f||hypot(x,z+209f)<2.5f)DreamPocket.RETURN else null
        if(id==DreamArchive33.ID)return if(hypot(x,z-13f)<2.6f)DreamPocket.RETURN else null
        if(DreamHorror32.contains(id))return DreamHorror32.portalAt(id,x,z)
        if(DreamWorld30.contains(id)){val p=DreamWorld30.spawn(id);return if(hypot(x-p.x,z-12.5f)<2.4f)DreamPocket.RETURN else null}
        if(id==DreamWorlds.STATION)return DreamHub35.portalAt(x,z)
        if(DreamVigilia27.contains(id))return DreamVigilia27.portalAt(id,x,z)
        if(DreamRoutes23.contains(id))return DreamRoutes23.portalAt(id,x,z)
        if(DreamNewDreams.contains(id))return DreamNewDreams.portalAt(id,x,z)
        if(id==DARK) {
            return if(hypot(x,z-44f)<2.7f)DreamWorlds.STATION else if(hypot(x,z+66f)<2.7f)DreamHorizons.DARK else null
        }
        if(id==DreamHorizons.DARK)return if(hypot(x,z-30f)<2.7f)DARK else if(hypot(x,z+330f)<2.7f)DreamVigilia27.DARK_THREE else null
        if(DreamReferences.contains(id))return DreamReferences.portalAt(id,x,z)
        if(DreamVoyage.contains(id)) {
            val p=DreamVoyage.gate(id)
            return if(hypot(x-p.x,z-p.z)<2.5f || (id==DreamGravity.ID&&hypot(x,z+150f)<2.5f))DreamPocket.RETURN else null
        }
        if(DreamPocket.contains(id))return if(hypot(x,z-8f)<2.5f)DreamPocket.RETURN else null
        if(id !in sequence)return null
        val a=entry(id);val b=exit(id)
        if(hypot(x-a.x,z-a.z)<2.7f && abs(feet-surface(id,a.x,a.z))<2.3f)return previous(id)
        if(hypot(x-b.x,z-b.z)<2.7f && abs(feet-surface(id,b.x,b.z))<2.3f)return next(id)
        return null
    }
    fun gateLabel(id:String,target:String)=when {
        id==DreamWorlds.STATION -> DreamHub35.gates.firstOrNull{it.target==target}?.let{it.sector.label+" · "+it.title}?:title(target)
        DreamHorror32.contains(target) -> "TESTIGOS · ${title(target)}"
        target==DreamBackrooms.ID -> "BACKROOMS · Las luces que no descansan"
        target==DreamVigilia27.SKY_ISLAND -> "SANTUARIO · El ojo entre nubes"
        target==DreamRoutes23.SKY -> "DESVÍOS · Los mundos con reglas"
        DreamReferences.contains(target) -> "Continuar: ${DreamReferences.title(target)}"
        target==DreamPocket.RETURN -> "Volver al lugar del que venías"
        id==DreamWorlds.STATION && target==DARK -> "DARK FANTASY"
        id==DreamWorlds.STATION -> "DREAMCORE: comenzar experiencia"
        target==previous(id) && id in sequence -> "Volver: ${title(target)}"
        target==DreamWorlds.STATION -> "Terminar y volver al Umbral"
        else -> "Continuar: ${title(target)}"
    }
    fun frameBlocked(id:String,x:Float,z:Float):Boolean {
        if(id !in sequence || id==DreamWorlds.CRATER)return false
        return listOf(entry(id),exit(id)).any { p ->
            abs(z-p.z)<.53f && abs(abs(x-p.x)-1.34f)<.53f
        }
    }
    fun canWalkHub(x:Float,z:Float)=DreamHub35.canWalk(x,z)
    private fun gate(b:DreamMesh,id:String,p:Point,forward:Boolean,large:Boolean=false) {
        val y=surface(id,p.x,p.z)
        val w=if(large)1.95f else 1.02f
        val h=if(large)4.4f else 2.85f
        val trim=if(forward)floatArrayOf(.89f,.80f,.65f)else floatArrayOf(.63f,.75f,.79f)
        val tint=if(id==DreamWorlds.STATION && p.x>0)floatArrayOf(.42f,.20f,.75f)
                 else if(forward)floatArrayOf(.65f,.87f,.86f)else floatArrayOf(.48f,.62f,.71f)
        for(offset in floatArrayOf(-.30f,.30f))
            b.box(p.x,y+h*.5f,p.z+offset,w,h*.5f,.03f,tint,if(forward)28f else 29f)
        for(s in floatArrayOf(-1f,1f))b.box(p.x+s*(w+.32f),y+h*.5f,p.z,.18f,h*.5f,.18f,trim,26f)
        b.box(p.x,y+h+.14f,p.z,w+.5f,.14f,.20f,trim,26f)
        b.box(p.x,y+.02f,p.z+1.1f,w+.65f,.025f,1.1f,trim,25f)
        if(large)for(i in 0..4)b.box(p.x-1.0f+i*.5f,y+h+.6f,p.z,.13f,.16f,.1f,tint,24f)
    }
    fun decorate(id:String,source:DreamWorlds.Scene):DreamWorlds.Scene {
        val data=source.vertices
        // Old portals become sealed architectural panels; only route gates glow.
        for(i in data.indices step 10)if(data[i+9] in 1.5f..3.5f) {
            data[i+3]=.35f;data[i+4]=.39f;data[i+5]=.43f;data[i+9]=0f
        }
        if(id==DreamHorizons.DARK) {
            val b=DreamMesh();gate(b,id,Point(0f,30f),false);gate(b,id,Point(0f,-330f),true)
            return DreamWorlds.Scene(data+b.finish().vertices)
        }
        if(id==DreamHorizons.TOWER){val b=DreamMesh();gate(b,id,Point(0f,8f),false);return DreamWorlds.Scene(data+b.finish().vertices)}
        if(id !in sequence)return source
        val b=DreamMesh();gate(b,id,entry(id),false);gate(b,id,exit(id),true)
        return DreamWorlds.Scene(data+b.finish().vertices)
    }
    fun createHub():DreamWorlds.Scene=DreamHub35.create()
}
