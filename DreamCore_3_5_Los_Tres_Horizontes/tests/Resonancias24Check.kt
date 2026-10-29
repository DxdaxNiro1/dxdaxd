package com.dreamcore.game

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun main(){
    var checks=0
    fun verify(ok:Boolean,label:String){check(ok){label};checks++}
    val expected=(listOf(DreamWorlds.STATION,DreamWorlds.POOLS,DreamWorlds.CRATER,DreamWorlds.HOUSE_MEADOW)+DreamJourney.sequence+listOf(DreamJourney.DARK)+DreamHorizons.ids+DreamPocket.ids+DreamVoyage.ids+DreamReferences.ids+DreamNewDreams.ids+DreamRoutes23.ids+DreamVigilia27.ids+DreamWorld30.ids+DreamHorror32.ids+DreamArchive33.ID+DreamRainward34.ID+DreamHorizons36.ids).toSet()
    val tracks=DreamMusic24.tracks
    verify(expected.size==66,"inventory includes all 61 actual scenes")
    verify(tracks.size==67,"one title and one score per scene")
    verify(tracks.map{it.world}.toSet()==expected+DreamMusic24.TITLE,"no missing or invented scene")
    for(selector in listOf<(DreamMusic24.Track)->String>({it.world},{it.title},{it.resource},{it.cover}))verify(tracks.map(selector).distinct().size==67,"unique score identity")
    verify(DreamMusic24.track("missing")==null,"unknown world has no arbitrary music")
    val out=File(System.getProperty("dreamcore.output","/tmp"));out.mkdirs()
    val story=DreamChronicle();story.universeSeed=3271
    DreamBackrooms.configure(story.universeSeed);DreamMystery.configure(story.universeSeed);DreamMystery.sync(story)
    val metadata=StringBuilder()
    for(track in tracks){
        verify(DreamMusic24.track(track.world)==track,"lookup exact")
        verify(track.seconds in 100..160,"long-form ambient duration")
        verify(track.resource.matches(Regex("music_score_\\d{2}_(24|27|30|32|33|34|36)")),"safe Android resource")
        if(track.world==DreamMusic24.TITLE)continue
        val id=track.world
        verify(DreamChronicle.validWorld(id),"save still recognizes scene $id")
        verify(track.map==DreamJourney.title(id),"album uses current map title $id")
        metadata.append(id).append('\t').append(DreamJourney.title(id)).append('\t').append(DreamJourney.worldCode(id)).append('\n')
        val scene=DreamSceneFactory33.create(id,story)
        val geometry=DreamJourney.decorate(id,scene).vertices+DreamObjects.geometry(id,story).vertices+DreamEvents.geometry(id).vertices+DreamRules23.geometry(id).vertices+(if(id==DreamWorlds.MEADOW)DreamMystery.geometry(id,story).vertices else floatArrayOf())+DreamPhotography.geometry(id).vertices+DreamWorldMotion.create(id).vertices
        verify(geometry.size%30==0&&geometry.all{it.isFinite()},"actual cover mesh valid $id")
        val bytes=ByteBuffer.allocate(geometry.size*4).order(ByteOrder.LITTLE_ENDIAN);bytes.asFloatBuffer().put(geometry)
        File(out,"$id.f32").writeBytes(bytes.array())
        verify(DreamRouteHelp24.hint(id).isNotBlank(),"hint available")
    }
    File(out,"catalog_maps.tsv").writeText(metadata.toString())
    // Audition metadata cannot mutate progression; all scene checkpoints still round-trip.
    for(id in expected){
        val saved=DreamChronicle();saved.checkpoint=DreamChronicle.Checkpoint(id,1f,1.65f,2f,.4f,.1f)
        val loaded=DreamChronicle.decode(DreamChronicle.encode(saved))!!.checkpoint
        if(id==DreamWorlds.HOUSE_MEADOW)verify(loaded.world==DreamWorlds.HOUSE_CRATER,"old consolidated house keeps its existing migration")
        else verify(loaded==saved.checkpoint,"checkpoint preserved $id")
    }
    println("PASS $checks checks")
}
