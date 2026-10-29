package com.dreamcore.game

/** Quality policy for effects; content and saved settings are never changed. */
object DreamGraphics26 {
    fun level(quality:Int,thermalCap:Int,powerMode:Int):Int {
        val chosen=quality.coerceIn(0,2)
        return if(powerMode==2 || thermalCap<=24)0 else if(thermalCap<=30)minOf(chosen,1)else chosen
    }
    fun cloudSteps(level:Int)=when(level){2->24f;1->16f;else->0f}
    fun cloudScale(world:String)=if(world==DreamReferences.HILL)1f/1200f else 1f/650f
    const val POOL_WATER=-.12f
    fun waterPlane(world:String):Float?=when(world){DreamHorizons36.SUNSET->-8f;DreamHorizons36.BLUE->0f;DreamWorlds.POOLS->POOL_WATER;DreamReferences.LAKE->.05f;DreamHorror32.POOL->-.10f;else->null}
}
