package com.dreamcore.game

import kotlin.math.*

object DreamTools {
    val names=listOf("none","lantern","compass","prism")
    fun equipped(story:DreamChronicle)=names.firstOrNull{"equip:"+it in story.flags}?:"none"
    fun equip(story:DreamChronicle,item:String):Boolean {
        if(item !in names || (item!="none" && "tool:"+item !in story.flags))return false
        story.flags.removeAll{it.startsWith("equip:")};story.flags.add("equip:"+item);return true
    }
    fun target(id:String,x:Float,z:Float,story:DreamChronicle):DreamJourney.Point? {
        DreamMail.target(story)?.let{if(it.world==id)return DreamJourney.Point(it.x,it.z)}
        DreamPhotography.targets(id).filter{"photo:"+it.key !in story.flags}.minByOrNull{hypot(x-it.x,z-it.z)}?.let{return DreamJourney.Point(it.x,it.z)}
        return if(id in DreamJourney.sequence)DreamJourney.exit(id)else if(DreamVoyage.contains(id))DreamVoyage.gate(id)else null
    }
    fun bearing(id:String,x:Float,z:Float,yaw:Float,story:DreamChronicle):String {
        val p=target(id,x,z,story)?:return "La aguja escucha el silencio"
        var angle=atan2(p.x-x,-(p.z-z))-yaw
        while(angle>PI)angle-=2f*PI.toFloat();while(angle< -PI)angle+=2f*PI.toFloat()
        val arrows=listOf("↑","↗","→","↘","↓","↙","←","↖")
        val index=((angle/(PI.toFloat()/4)).roundToInt()+8)%8
        return "Brújula ${arrows[index]} · ${hypot(p.x-x,p.z-z).toInt()} m hacia el sonido"
    }
}
