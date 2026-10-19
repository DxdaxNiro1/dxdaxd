package com.dreamcore.game

object DreamMail {
    data class Address(val world:String,val x:Float,val z:Float,val name:String,val reply:String)
    val addresses=listOf(
        Address(DreamWorlds.AQUARIUM,-8f,-220f,"La casa que escucha el agua","Dentro solo hay una mesa: la respuesta dice que el mar también te escucha."),
        Address(DreamExpansion.CLOUD_HOTEL,0f,-160f,"Recepción de la madrugada","La carta era para quien dejó una luz encendida. Alguien guardó tu sitio."),
        Address(DreamHorizons.CELESTIAL,DreamHorizons.curve(-290f)+12f,-290f,"La puerta bajo el planeta","La última dirección estaba escrita desde tu refugio. El cartero ya sabía que volverías.")
    )
    fun active(story:DreamChronicle)= (0..2).firstOrNull{"letter:"+it in story.flags}
    fun target(story:DreamChronicle)=active(story)?.let{addresses[it]}
    fun accept(story:DreamChronicle):String {
        active(story)?.let{return "Todavía llevas la carta para: ${addresses[it].name}."}
        val next=(0..2).firstOrNull{"delivered:"+it !in story.flags}?:return "El cartero inclina la cabeza. Ya no quedan cartas para ti."
        story.flags.add("letter:"+next);return "El cartero entrega una carta: ${addresses[next].name}."
    }
    fun deliver(index:Int,story:DreamChronicle):Boolean {
        if(active(story)!=index)return false
        story.flags.remove("letter:"+index);story.flags.add("delivered:"+index);return true
    }
}
