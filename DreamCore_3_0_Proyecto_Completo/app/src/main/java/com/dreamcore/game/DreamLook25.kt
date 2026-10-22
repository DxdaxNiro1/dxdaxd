package com.dreamcore.game

/** Shared lighting intent. Immutable profiles avoid allocating data every frame. */
object DreamLook25 {
    data class Light(val ambient:Float,val direct:Float,val night:Float)
    private val daylight=Light(.50f,.68f,0f)
    private val interior=Light(.48f,.08f,.15f)
    private val dimField=Light(.14f,.18f,1f)
    private val moonlight=Light(.24f,.40f,1f)

    fun light(world:String):Light {
        val id=if(world in DreamVoyage.photos)DreamVoyage.base(world)else world
        if(id==DreamNewDreams.EYES)return dimField
        if(DreamPower.indoor(id))return interior
        return if(DreamLands.night(id)||DreamVigilia27.dark(id)||id==DreamHorizons.BED)
            moonlight else daylight
    }

    // Keep the existing low/balanced GPU budgets. Extra resolution is opt-in.
    fun atlasSize(quality:Int)=if(quality>=2)2048 else if(quality<=0)512 else 1024
}
