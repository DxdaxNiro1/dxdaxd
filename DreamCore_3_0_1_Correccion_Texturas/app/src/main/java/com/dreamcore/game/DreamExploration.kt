package com.dreamcore.game

import kotlin.math.*

/** Stateful local discoveries; no combat, jumpscares or network dependency. */
class DreamExploration(val found:MutableSet<String> = mutableSetOf()) {
    data class Door(val label:String,val x:Float,val z:Float,val toX:Float,val toZ:Float,val yaw:Float)
    val memoriesFound get()=found.count{it.endsWith(":memory") && !it.startsWith("memory:") && !it.startsWith("tower:")}
    var presenceX=22f;private set
    var presenceZ=-80f;private set
    var presenceAlpha=0f;private set
    var seenSeconds=0f;private set
    var corridorRevision=0;private set
    private var world=""
    private var gaze=0f
    private var cooldown=0f
    private var encounter=0
    private var corridorArmed=false
    private var previousZ=0f
    private var sinceChange=0f
    fun door(id:String,x:Float,z:Float):Door? {
        if(id==DreamWorlds.HOUSE_MEADOW)return when {
            hypot(x,z+247f)<2.7f->Door("Atravesar el reflejo",0f,-247f,-24f,-193f,PI.toFloat())
            hypot(x+24f,z+197f)<2.7f->Door("Volver al pasillo",-24f,-197f,0f,-242f,PI.toFloat())
            else->null
        }
        if(id!=DreamWorlds.MEADOW)return null
        return when {
            hypot(x+40f,z+48.1f)<2.7f->Door("Entrar: la piscina que no cabe en la casa",-40f,-48.1f,1600f,3f,0f)
            hypot(x-1600f,z-8f)<2.7f->Door("Salir al pueblo",1600f,8f,-40f,-43f,PI.toFloat())
            else->null
        }
    }
    fun step(id:String,x:Float,z:Float,yaw:Float,pitch:Float,dt:Float):List<String> {
        val messages=ArrayList<String>()
        if(world!=id) {
            world=id;gaze=0f;cooldown=0f;encounter=0;sinceChange=0f
            corridorArmed=false;previousZ=z;presenceAlpha=0f;seenSeconds=0f
            presenceX=22f;presenceZ=-80f
        }
        sinceChange+=dt;cooldown=(cooldown-dt).coerceAtLeast(0f)
        if(DreamLands.contains(id))for(s in DreamLands.secrets(id)) {
            val key="$id:${s.key}"
            if(hypot(x-s.x,z-s.z)<6f && found.add(key)) {
                messages+=s.text
                if(id==DreamWorlds.HOUSE_CRATER && s.key=="memory")found.add("${DreamWorlds.HOUSE_MEADOW}:memory")
            }
        }
        if(id==DreamWorlds.HOUSE_MEADOW) {
            if(z < -150f)corridorArmed=true
            if(corridorArmed && previousZ < -120f && z>=-120f) {
                corridorArmed=false;corridorRevision++;DreamLands.changeCorridor()
                messages+="Al volver, las ventanas están al otro lado. ¿Siempre fue así?"
            }
        }
        previousZ=z
        val eligible=id==DreamWorlds.GARDEN || id==DreamExpansion.ROAD
        if(eligible && x<1000f && encounter<1) {
            val dx=presenceX-x;val dz=presenceZ-z;val distance=hypot(dx,dz)
            val facing=(sin(yaw)*dx-cos(yaw)*dz)/max(distance,.001f)
            val looking=facing>.992f && abs(pitch)<.16f && distance in 16f..115f
            val visible=cooldown<=0f && sinceChange>18f && distance in 14f..150f
            presenceAlpha+=(if(visible)dt*.45f else -dt*.85f)
            presenceAlpha=presenceAlpha.coerceIn(0f,1f)
            var unobstructed=true
            if(looking || distance<14f)for(i in 1 until ceil(distance/.5f).toInt()) {
                val t=i*.5f/distance
                if(!DreamLands.canWalk(id,x+dx*t,z+dz*t)){unobstructed=false;break}
            }
            if(looking && visible && unobstructed) { gaze+=dt;seenSeconds+=dt } else gaze=(gaze-dt*.6f).coerceAtLeast(0f)
            if((gaze>1.7f || (distance<14f && unobstructed)) && cooldown<=0f) {
                cooldown=18f;gaze=0f;encounter++
                messages+="La figura inclina la cabeza. Cuando vuelves a buscarla, ya no está."
            }
            if(cooldown in 15f..16f && presenceAlpha<.01f) {
                presenceX=if(encounter%2==0)22f else -26f
                presenceZ=-80f-encounter*150f
            }
        } else presenceAlpha=(presenceAlpha-dt).coerceAtLeast(0f)
        return messages
    }
    companion object {
        fun figure():DreamWorlds.Scene {
            val b=DreamMesh();val c=floatArrayOf(.17f,.20f,.24f)
            b.ellipsoid(0f,2.5f,0f,.58f,1.35f,.32f,c,6,9,32f)
            b.ellipsoid(0f,4.1f,0f,.37f,.48f,.34f,c,6,9,32f)
            for(s in floatArrayOf(-1f,1f)) {
                b.box(s*.23f,.85f,0f,.14f,.85f,.14f,c,32f)
                b.box(s*.64f,2.25f,0f,.09f,1.0f,.10f,c,32f)
            }
            return b.finish()
        }
    }
}
