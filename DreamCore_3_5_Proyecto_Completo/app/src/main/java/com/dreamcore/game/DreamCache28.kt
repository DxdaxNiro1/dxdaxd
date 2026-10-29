package com.dreamcore.game

/** Inactive families must not pin old multi-megabyte meshes after a portal transition. */
object DreamCache28 {
    fun retain(id:String){
        if(!DreamLands.contains(id))DreamLands.releaseGeometry()
        if(!DreamReferences.contains(id))DreamReferences.releaseGeometry()
        if(!DreamHorizons.contains(id))DreamHorizons.releaseGeometry()
        if(!DreamNewDreams.contains(id))DreamNewDreams.releaseGeometry()
        if(!DreamRoutes23.contains(id))DreamRoutes23.releaseGeometry()
        if(!DreamVigilia27.contains(id))DreamVigilia27.releaseGeometry()
    }
}
