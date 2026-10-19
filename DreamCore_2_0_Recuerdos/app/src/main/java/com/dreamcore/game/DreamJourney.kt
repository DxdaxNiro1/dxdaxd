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
    fun entry(id:String)=if(DreamHorizons.contains(id))DreamHorizons.entry(id)else if(id==DreamWaterpark.ID)Point(DreamWaterpark.centre(90f),90f)else if(DreamLands.contains(id))DreamLands.entry(id)else entries[sequence.indexOf(id)]
    fun exit(id:String)=if(DreamHorizons.contains(id))DreamHorizons.exit(id)else if(id==DreamWaterpark.ID)Point(DreamWaterpark.centre(-495f),-495f)else if(DreamLands.contains(id))DreamLands.exit(id)else exits[sequence.indexOf(id)]
    fun previous(id:String):String {
        val i=sequence.indexOf(id);return if(i<=0)DreamWorlds.STATION else sequence[i-1]
    }
    fun next(id:String):String {
        val i=sequence.indexOf(id);return if(i<0 || i==sequence.lastIndex)DreamWorlds.STATION else sequence[i+1]
    }
    fun title(id:String):String=when(id) {
        DreamWorlds.STATION -> "El Umbral: Dreamcore y Dark Fantasy"
        DreamHorizons.DARK -> DreamHorizons.title(id)
        DARK -> "El reino de las lunas dormidas"
        DreamWaterpark.ID -> "El parque de las lágrimas suspendidas"
        else -> if(DreamVoyage.contains(id))DreamVoyage.title(id)else if(DreamHorizons.contains(id))DreamHorizons.title(id)else if(DreamPocket.contains(id))DreamPocket.title(id)else if(DreamLands.contains(id))DreamLands.title(id)else DreamExpansion.title(id)
    }
    fun hudTitle(id:String):String {
        val index=sequence.indexOf(id)
        return when {
            id==DreamWorlds.STATION -> "EL UMBRAL  /  Elige tu experiencia"
            id==DARK -> "DARK FANTASY  /  Las lunas dormidas"
            index>=0 -> "DREAMCORE ${index+1}/${sequence.size}  /  ${title(id)}"
            else -> title(id)
        }
    }
    fun worldCode(id:String):Float=if(id in DreamVoyage.photos)worldCode(DreamVoyage.base(id))else when(id){DreamVoyage.REFUGE->22f;DreamVoyage.LETTER->23f;DreamGravity.ID->24f;DreamHorizons.CELESTIAL->17f;DreamHorizons.CITY->18f;DreamHorizons.DARK->19f;DreamHorizons.TOWER->20f;DreamHorizons.BED->21f;DARK->12f;DreamWaterpark.ID->13f;DreamPocket.LAUNDRY->14f;DreamPocket.TRAIN->15f;DreamPocket.DAWN->16f;else->DreamExpansion.worldCode(id)}
    fun surface(id:String,x:Float,z:Float):Float=if(DreamVoyage.contains(id))DreamVoyage.surface(id,x,z)else if(DreamHorizons.contains(id))DreamHorizons.height(id,x,z)else when(id) {
        DreamWorlds.STATION -> DreamWorlds.groundHeight(x,z)
        DreamWorlds.CRATER -> DreamWorlds.craterWalkSurface(x,z)
        DreamWorlds.MEADOW -> DreamLands.height(id,x,z)
        DreamWaterpark.ID -> DreamWaterpark.height(x,z)
        else -> 0f
    }
    fun arrival(target:String,from:String):Arrival {
        if(target==DreamWorlds.STATION)return Arrival(0f,6f,0f)
        if(target==DARK)return if(from==DreamHorizons.DARK)Arrival(0f,-59f,kotlin.math.PI.toFloat())else Arrival(0f,36f,0f)
        if(target==DreamHorizons.DARK)return Arrival(0f,24f,0f)
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
        if(id==DreamWorlds.STATION) {
            if(hypot(x+8f,z+16f)<2.7f)return sequence.first()
            if(hypot(x-8f,z+16f)<2.7f)return DARK
            return null
        }
        if(id==DARK) {
            return if(hypot(x,z-44f)<2.7f)DreamWorlds.STATION else if(hypot(x,z+66f)<2.7f)DreamHorizons.DARK else null
        }
        if(id==DreamHorizons.DARK)return if(hypot(x,z-30f)<2.7f)DARK else if(hypot(x,z+330f)<2.7f)DreamWorlds.STATION else null
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
    fun canWalkHub(x:Float,z:Float):Boolean {
        if(x !in -23f..23f || z !in -27f..22f)return false
        if(abs(abs(x)-18f)<.82f && listOf(-21f,-4f,13f).any{abs(z-it)<.82f})return false
        for(gx in floatArrayOf(-8f,8f)) {
            if(abs(z+16f)<.8f && abs(abs(x-gx)-2.3f)<.55f)return false
        }
        return true
    }
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
    fun createHub():DreamWorlds.Scene {
        val b=DreamMesh();val stone=floatArrayOf(.77f,.75f,.83f)
        for(x in -24 until 24 step 3)for(z in -28 until 23 step 3) {
            fun p(xx:Float,zz:Float)=floatArrayOf(xx,DreamWorlds.groundHeight(xx,zz)-.04f,zz)
            b.quad(p(x.toFloat(),z.toFloat()),p(x.toFloat(),z+3f),p(x+3f,z+3f),p(x+3f,z.toFloat()),stone,25f)
        }
        gate(b,DreamWorlds.STATION,Point(-8f,-16f),true,true)
        gate(b,DreamWorlds.STATION,Point(8f,-16f),true,true)
        val rose=floatArrayOf(.85f,.65f,.77f);val dark=floatArrayOf(.24f,.24f,.38f)
        for(x in floatArrayOf(-18f,18f))for(z in floatArrayOf(-21f,-4f,13f)) {
            b.box(x,1.8f,z,.45f,1.8f,.45f,if(x<0)rose else dark,26f)
            b.ellipsoid(x,3.9f,z,.46f,.46f,.46f,if(x<0)rose else dark,8,12,24f)
        }
        for(k in 0 until 12) {
            val z=2f-k*1.35f
            for(x in floatArrayOf(-8f,8f))b.box(x,DreamWorlds.groundHeight(x,z)+.01f,z,1.3f,.025f,.36f,if(x<0)rose else dark,25f)
        }
        return b.finish()
    }
}
