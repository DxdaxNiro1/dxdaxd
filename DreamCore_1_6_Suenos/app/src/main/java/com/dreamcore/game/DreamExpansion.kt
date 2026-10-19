package com.dreamcore.game

import kotlin.math.abs
import kotlin.math.hypot

/** Navigation and visual presets for the four new, fully connected dreams. */
object DreamExpansion {
    const val CHESS = "dream_09"
    const val TUNNELS = "dream_10"
    const val CLOUD_HOTEL = "dream_11"
    const val ROAD = "dream_12"
    val ids = listOf(CHESS,TUNNELS,CLOUD_HOTEL,ROAD)

    fun contains(id: String) = id in ids
    fun worldCode(id: String): Float = ((id.substringAfter('_').toIntOrNull() ?: 1)-1).coerceIn(0,11).toFloat()
    fun title(id: String): String = when(id) {
        DreamWorlds.STATION -> "Estación de las tardes perdidas"
        DreamWorlds.POOLS -> "Piscinas del recuerdo"
        DreamWorlds.CRATER -> "El abismo de las casas perdidas"
        DreamWorlds.GARDEN -> "El jardín de la lluvia detenida"
        DreamWorlds.MEADOW -> "La pradera del último sol"
        DreamWorlds.AQUARIUM -> "El acuario de las horas"
        DreamWorlds.HOUSE_MEADOW -> "La casa de los pasillos largos"
        DreamWorlds.HOUSE_CRATER -> "La casa que mira hacia abajo"
        CHESS -> "Ajedrez del cielo inmóvil"
        TUNNELS -> "Túneles de agua y porcelana"
        CLOUD_HOTEL -> "Hotel sobre las nubes"
        ROAD -> "La carretera del último recuerdo"
        else -> "Un sueño por descubrir"
    }
    fun hudTitle(id: String) = "${id.substringAfter('_')}  /  ${title(id)}"

    fun spawn(id: String): Pair<Float,Float> = when(id) {
        CHESS -> Pair(DreamChessMap.SPAWN_X,DreamChessMap.SPAWN_Z)
        TUNNELS -> Pair(DreamTunnelMap.SPAWN_X,DreamTunnelMap.SPAWN_Z)
        CLOUD_HOTEL -> Pair(DreamCloudMap.SPAWN_X,DreamCloudMap.SPAWN_Z)
        ROAD -> Pair(DreamRoadMap.SPAWN_X,DreamRoadMap.SPAWN_Z)
        else -> error("Unknown expansion world: $id")
    }
    fun create(id: String): DreamWorlds.Scene = when(id) {
        CHESS -> DreamChessMap.create()
        TUNNELS -> DreamTunnelMap.create()
        CLOUD_HOTEL -> DreamCloudMap.create()
        ROAD -> DreamRoadMap.create()
        else -> error("Unknown expansion world: $id")
    }
    fun canWalk(id: String,x: Float,z: Float): Boolean = when(id) {
        CHESS -> DreamChessMap.canWalk(x,z)
        TUNNELS -> DreamTunnelMap.canWalk(x,z)
        CLOUD_HOTEL -> DreamCloudMap.canWalk(x,z)
        ROAD -> DreamRoadMap.canWalk(x,z)
        else -> false
    }
    fun portalAt(id: String,x: Float,z: Float): String? = when(id) {
        CHESS -> DreamChessMap.portalAt(x,z)
        TUNNELS -> DreamTunnelMap.portalAt(x,z)
        CLOUD_HOTEL -> DreamCloudMap.portalAt(x,z)
        ROAD -> DreamRoadMap.portalAt(x,z)
        else -> null
    }

    fun stationGateX(id: String) = when(id) {
        CHESS -> -30f; TUNNELS -> -10f; CLOUD_HOTEL -> 10f; ROAD -> 30f
        else -> 0f
    }
    const val STATION_GATE_Z = 28f
    fun stationPortalAt(x: Float,z: Float): String? = ids.firstOrNull {
        hypot(x-stationGateX(it),z-STATION_GATE_Z) < 2.9f
    }
    fun stationBlocked(x: Float,z: Float): Boolean = ids.any {
        abs(z-STATION_GATE_Z)<.7f && abs(abs(x-stationGateX(it))-2.45f)<.53f
    }

    /** Original station plus a gallery of four visibly different gateways. */
    fun stationScene(): DreamWorlds.Scene {
        val base=DreamWorlds.createStation()
        val b=DreamMesh()
        val ivory=floatArrayOf(.91f,.90f,.87f)
        val tints=arrayOf(floatArrayOf(.37f,.43f,.56f),floatArrayOf(.55f,.78f,.83f),
            floatArrayOf(.91f,.66f,.77f),floatArrayOf(.76f,.65f,.91f))
        for((index,id) in ids.withIndex()) {
            val x=stationGateX(id);val z=STATION_GATE_Z
            val y=DreamWorlds.groundHeight(x,z)
            for(side in floatArrayOf(-1f,1f)) {
                b.box(x+side*2.45f,y+2.7f,z,.29f,2.7f,.45f,ivory,26f)
                b.ellipsoid(x+side*2.45f,y+5.6f,z,.34f,.34f,.34f,tints[index],7,10,24f)
            }
            b.box(x,y+5.25f,z,2.7f,.24f,.47f,tints[index],26f)
            b.box(x,y+2.35f,z,1.98f,2.35f,.06f,tints[index],if(index==0)24f else 3f)
            // Small icon above each door: chess crown, tunnel, hotel windows, road.
            if(index==0) {
                for(k in -1..1)b.box(x+k*.50f,y+6.10f,z,.13f,.40f,.16f,ivory,25f)
                b.box(x,y+5.86f,z,.8f,.12f,.18f,ivory,25f)
            } else if(index==1) {
                b.ring(x,y+6.06f,z,.64f,.14f,ivory)
            } else if(index==2) {
                for(k in -1..1)b.box(x+k*.48f,y+6.1f,z,.12f,.26f,.10f,ivory,24f)
            } else {
                for(k in -1..1)b.box(x,y+5.9f+k*.25f,z,.10f,.07f,.10f,ivory,24f)
            }
            // Approaching path follows the same terrain as the player's feet.
            for(k in 0..10) {
                val pz=12f+k*1.5f
                b.box(x,DreamWorlds.groundHeight(x,pz)-.02f,pz,1.1f,.025f,.55f,ivory,8f)
            }
        }
        val extra=b.finish().vertices
        return DreamWorlds.Scene(base.vertices+extra)
    }

    fun skyTop(id: String): FloatArray = when(id) {
        CHESS -> floatArrayOf(.23f,.43f,.70f)
        TUNNELS -> floatArrayOf(.35f,.48f,.68f)
        CLOUD_HOTEL -> floatArrayOf(.42f,.43f,.70f)
        ROAD -> floatArrayOf(.36f,.34f,.67f)
        else -> floatArrayOf(.34f,.46f,.75f)
    }
    fun skyHorizon(id: String): FloatArray = when(id) {
        CHESS -> floatArrayOf(.91f,.84f,.77f)
        TUNNELS -> floatArrayOf(.77f,.85f,.89f)
        CLOUD_HOTEL -> floatArrayOf(.98f,.73f,.80f)
        ROAD -> floatArrayOf(.98f,.67f,.77f)
        else -> floatArrayOf(.94f,.80f,.86f)
    }
    fun fogColor(id: String): FloatArray = when(id) {
        CHESS -> floatArrayOf(.72f,.78f,.85f)
        TUNNELS -> floatArrayOf(.48f,.62f,.69f)
        CLOUD_HOTEL -> floatArrayOf(.79f,.65f,.78f)
        ROAD -> floatArrayOf(.79f,.62f,.79f)
        else -> floatArrayOf(.48f,.54f,.69f)
    }
    fun fogNear(id: String) = if(id==TUNNELS) 38f else 100f
    fun fogFar(id: String) = if(id==TUNNELS) 160f else 340f
}
