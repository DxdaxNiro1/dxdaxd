package com.dreamcore.game
import kotlin.math.*

object DreamWeather34 {
    data class Mix(val wind:Float,val rain:Float)
    fun at(id:String,x:Float,z:Float,time:Float):Mix{
        val sheltered=if(id==DreamRainward34.ID)DreamRainward34.indoor(x,z) else DreamPower.indoor(id)||DreamReferences.indoor(id)||DreamVoyage.indoor(id,x,z)||DreamLands.indoor(id,x,z)
        if(sheltered)return Mix(0f,0f)
        val gust=.78f+.22f*sin(time*.17f+(id.hashCode()%13))
        val rain=when(id){DreamRainward34.ID->.72f;DreamHorror32.FOREST->.30f;DreamHorror32.WITNESSES->.17f;else->0f}
        val wind=when{ id==DreamRainward34.ID->.48f;DreamVigilia27.dark(id)||DreamHorror32.contains(id)->.40f;id==DreamWorlds.STATION->.08f;else->.16f }
        return Mix(wind*gust,rain)
    }
}

/** Ten-Hz bounded mixer state. Proximity follows actual moving placements and respects occlusion. */
class DreamAcoustics34 {
    data class Frame(val world:String="",val tension:Float=0f,val wind:Float=0f,val rain:Float=0f,val voice:String="",val pan:Float=0f,val gain:Float=0f,val caption:String="",val fall:Float=0f)
    private var world="";private var time=0f;private var pressure=0f;private var cue=1.4f
    fun sample(id:String,x:Float,y:Float,z:Float,yaw:Float,dt:Float,presences:List<DreamEntities32.Presence>,blocked:(Float,Float,Float)->Boolean={_,_,_->false}):Frame{
        if(world!=id){world=id;time=0f;pressure=0f;cue=1.4f}
        val d=dt.coerceIn(0f,.3f);time+=d;cue-=d
        val weather=DreamWeather34.at(id,x,z,time)
        var nearest:DreamEntities32.Presence?=null;var distance=Float.POSITIVE_INFINITY
        for(p in presences){if(DreamCatalogue34.soundFamily(p.model).isEmpty())continue
            val centreY=DreamJourney.surface(id,p.x,p.z)+p.y+minOf(2f,p.scale*2f)
            val gap=(sqrt((x-p.x).pow(2)+(z-p.z).pow(2)+(y-centreY).pow(2))-p.scale*.65f).coerceAtLeast(0f)
            if(gap<distance){distance=gap;nearest=p}}
        val n=nearest;val obscured=n!=null&&distance<28f&&blocked(n.x,DreamJourney.surface(id,n.x,n.z)+n.y+1.6f,n.z)
        val target=((18f-distance)/16f).coerceIn(0f,1f)*(if(obscured).45f else 1f)
        pressure+=(target-pressure)*(1f-exp(-d*if(target>pressure)1.5f else .75f))
        var voice="";var pan=0f;var gain=0f;var caption=""
        if(n!=null&&distance<25f&&cue<=0f){voice="presence_"+DreamCatalogue34.soundFamily(n.model)+"34";cue=6.2f+(1f-pressure)*3f
            val mix=DreamSpatial33.mix(x,y,z,yaw,n.x,DreamJourney.surface(id,n.x,n.z)+n.y+1.6f,n.z,obscured);pan=mix.pan;gain=mix.gain*(.45f+.55f*target)*(if(obscured).60f else 1f)
            caption=(if(pan<-.3f)"← " else if(pan>.3f)"→ " else "")+DreamCatalogue34.creatureName(n.model)+if(obscured)" · sonido detrás del muro" else " · respira cerca"}
        return Frame(id,pressure.coerceIn(0f,1f),weather.wind,weather.rain,voice,pan,gain,caption)
    }
}
