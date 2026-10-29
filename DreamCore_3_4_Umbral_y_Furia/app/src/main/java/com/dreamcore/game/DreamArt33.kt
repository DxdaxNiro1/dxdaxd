package com.dreamcore.game

/** Small calibrated deltas preserve the identity and readable exposure of every inherited world. */
object DreamArt33{
 private val neutral=floatArrayOf(0f,0f,0f,-.025f)
 private val interior=floatArrayOf(.035f,.008f,-.025f,-.045f)
 private val aquatic=floatArrayOf(-.015f,.008f,.035f,-.035f)
 private val archive=floatArrayOf(.03f,.012f,-.025f,-.075f)
 private val night=floatArrayOf(0f,-.006f,.025f,-.02f)
 private val water=setOf(DreamWorlds.POOLS,DreamWorlds.AQUARIUM,DreamHorror32.POOL,DreamReferences.LAKE,DreamReferences.CANAL,DreamWaterpark.ID)
 fun profile(id:String):FloatArray=when{
  id==DreamRainward34.ID->night
  id==DreamArchive33.ID->archive
  id in water->aquatic
  DreamVigilia27.dark(id)||id==DreamHorror32.FOREST||id==DreamHorror32.WITNESSES->night
  DreamPower.indoor(id)->interior
  else->neutral
 }
}
