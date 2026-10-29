package com.dreamcore.game

/** Every map has exactly one home; category labels are shared by travel, music and the HUD. */
object DreamCatalogue34 {
    enum class Group(val title:String,val detail:String){
        DREAMCORE("Dreamcore","Paisajes, ciudades y lugares imposibles"),
        BACKROOMS("Backrooms y espacios liminales","Habitaciones, pasillos y piscinas vacías"),
        DARK("Dark Fantasy","Reinos nocturnos y lunas vivas"),
        TERROR("Terror y pesadillas","Testigos, archivo y ciudad bajo la lluvia"),
        REFUGE("Refugio y recuerdos","Descanso, fotografías y viajes personales")
    }
    fun group(id:String):Group=when{
        id==DreamRainward34.ID||id==DreamArchive33.ID||DreamHorror32.contains(id)->Group.TERROR
        DreamVigilia27.dark(id)->Group.DARK
        id==DreamVoyage.REFUGE||id==DreamPocket.TRAIN||id==DreamWorld30.LIBRARY||id in DreamVoyage.photos||id==DreamVoyage.LETTER||id==DreamGravity.ID->Group.REFUGE
        id==DreamBackrooms.ID||id==DreamWorlds.POOLS||id==DreamWorlds.HOUSE_MEADOW||id==DreamReferences.HALL||id==DreamReferences.ATRIUM||id==DreamReferences.OFFICE||id==DreamPocket.LAUNDRY||id==DreamReferences.CINEMA->Group.BACKROOMS
        else->Group.DREAMCORE
    }
    fun worlds(category:Group)=DreamMusic24.tracks.filter{it.world!=DreamMusic24.TITLE&&group(it.world)==category}.sortedWith(compareBy<DreamMusic24.Track>{if(it.world==DreamWorlds.STATION)0 else 1}.thenBy{it.map})
    fun soundFamily(model:String)=when(model){"choir","gold","mourner","seraph"->"choir";"mask","witness","tenant"->"throat";"antler","cone"->"bone";"crimson"->"rasp";"star","observatory","curious","window"->"whisper";"mushroom","water-eye","root-eye","gentleman"->"wet";else->""}
    fun creatureName(model:String)=when(model){"choir"->"Coro de las puertas";"mask"->"El enmascarado";"witness"->"Testigo de los ojos";"crimson"->"Presencia carmesí";"antler"->"Custodio de astas";"gold"->"Guardián dorado";"star"->"Visitante estrellado";"observatory"->"Observatorio ocular";"mushroom"->"Habitante del herbario";"water-eye"->"Ojo del agua";"archivist"->"Alma, la archivista";"tenant"->"El inquilino del turno vacío";"curious"->"El observador del camino";"mourner"->"La figura del duelo";"window"->"La figura de la ventana";"friendly"->"Compañero de la pradera";"guide"->"El guía";"postman"->"El cartero";"cone"->"El vigilante del cono";"root-eye"->"El ojo de las raíces";"gentleman"->"El caballero ocular";"seraph"->"El serafín en reposo";"moons"->"Las lunas vivas";else->"Presencia"}
    fun rememberMenuReturn(story:DreamChronicle,from:DreamChronicle.Checkpoint){story.returnStack.clear();story.returnPoint=from}
    val entries get()=DreamEntities32.models+listOf("friendly","guide","postman","moons")
    fun locations(model:String):List<String>{
        val extra=when(model){"archivist"->listOf(DreamArchive33.ID,DreamVoyage.REFUGE,DreamWorld30.LIBRARY,DreamPocket.TRAIN);"antler"->listOf(DreamArchive33.ID);"star"->listOf(DreamWorlds.MEADOW,DreamReferences.VILLAGE);"gold"->listOf(DreamWorlds.AQUARIUM,DreamVigilia27.SKY_ISLAND,DreamHorror32.POOL);"curious","postman"->listOf(DreamWorlds.MEADOW);"mourner"->listOf(DreamHorizons.DARK,DreamWorlds.AQUARIUM);"window"->listOf(DreamHorizons.CITY,DreamBackrooms.ID,DreamWorlds.HOUSE_MEADOW,DreamExpansion.CLOUD_HOTEL);"friendly"->listOf(DreamNewDreams.EYES);"guide"->listOf(DreamWaterpark.ID,DreamExpansion.TUNNELS);"moons"->listOf(DreamJourney.DARK,DreamHorizons.DARK,DreamVigilia27.DARK_THREE,DreamVigilia27.DARK_FOUR);else->emptyList()}
        return (DreamMusic24.tracks.filter{it.world!=DreamMusic24.TITLE&&DreamEntities32.placements(it.world).any{p->p.model==model}}.map{it.world}+extra).distinct().map{DreamJourney.title(it)}
    }

}
