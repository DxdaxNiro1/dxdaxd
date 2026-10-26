package com.dreamcore.game

/** Single source of production map geometry, also exercised by the walking audit. */
object DreamSceneFactory33 {
    fun create(id:String,story:DreamChronicle):DreamWorlds.Scene=when {
            id==DreamArchive33.ID->DreamArchive33.create()
            DreamHorror32.contains(id)->DreamHorror32.create(id)
            DreamWorld30.contains(id)->DreamWorld30.create(id,story)
            DreamVigilia27.contains(id)->DreamVigilia27.create(id)
            DreamRoutes23.contains(id)->if(id==DreamMystery.HOUSE)DreamMystery.geometry(id,story)else DreamRoutes23.create(id)
            DreamNewDreams.contains(id)->DreamNewDreams.create(id)
            DreamReferences.contains(id)->DreamReferences.create(id)
            DreamVoyage.contains(id)->DreamVoyage.create(id,story)
            DreamHorizons.contains(id)->DreamHorizons.create(id)
            id==DreamWaterpark.ID->DreamWaterpark.create()
            DreamPocket.contains(id)->DreamPocket.create(id)
            DreamLands.contains(id)->DreamLands.create(id)
            id==DreamWorlds.STATION->DreamJourney.createHub()
            id==DreamWorlds.POOLS->DreamWorlds.createPools()
            id==DreamWorlds.CRATER->DreamWorlds.createCrater()
            id==DreamJourney.DARK->DreamDarkMap.create()
            else->error("Unknown world")
        }
}
