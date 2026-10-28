package com.dreamcore.game
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*
fun main(){var checks=0;fun verify(v:Boolean,m:String){check(v){m};checks++}
 val worlds=DreamMusic24.tracks.filter{it.world!=DreamMusic24.TITLE};val categories=DreamCatalogue34.Group.values().flatMap{DreamCatalogue34.worlds(it)}
 verify(categories.size==worlds.size&&categories.map{it.world}.toSet()==worlds.map{it.world}.toSet(),"every world appears exactly once in a travel/music category")
 verify(DreamCatalogue34.group(DreamBackrooms.ID)==DreamCatalogue34.Group.BACKROOMS,"Backrooms is separate")
 verify(DreamVigilia27.ids.filter{DreamVigilia27.dark(it)}.all{DreamCatalogue34.group(it)==DreamCatalogue34.Group.DARK},"lunar worlds remain Dark Fantasy")
 verify(DreamCatalogue34.group(DreamRainward34.ID)==DreamCatalogue34.Group.TERROR,"new urban nightmare is classified")
 verify(DreamJourney.worldCode(DreamRainward34.ID)==63f,"unique new shader world identity")
 verify(DreamJourney.portalAt(DreamRainward34.ID,0f,13f,0f)==DreamPocket.RETURN&&DreamJourney.portalAt(DreamRainward34.ID,0f,-209f,0f)==DreamPocket.RETURN,"entry and exit both return safely")
 val start=DreamJourney.arrival(DreamRainward34.ID,DreamWorlds.STATION);verify(DreamRainward34.canWalk(start.x,start.z),"supported arrival")
 val mesh=DreamRainward34.create();verify(mesh.vertices.all{it.isFinite()}&&mesh.vertexCount<180000,"finite bounded urban geometry ${mesh.vertexCount}")
 val sight=DreamSkySight(mesh.vertices);for(z in 6 downTo -209){verify(DreamRainward34.canWalk(0f,z.toFloat())&&sight.bodyClear33(0f,0f,z.toFloat()),"continuous supported centre route $z")}
 val rain=DreamAtmosphere.particles().vertices;val bytes=ByteBuffer.allocate(rain.size*4).order(ByteOrder.LITTLE_ENDIAN);bytes.asFloatBuffer().put(rain);File(System.getProperty("dreamcore.output","/tmp"),"rain34.f32").writeBytes(bytes.array())
 val c=DreamChronicle();c.checkpoint=DreamChronicle.Checkpoint(DreamRainward34.ID,0f,1.65f,-70f,0f,0f);verify(DreamChronicle.decode(DreamChronicle.encode(c))!!.checkpoint==c.checkpoint,"new world resumes in original save schema")
 val p=DreamEntities32.Presence("tenant",0f,0f,-4f,.75f);val sound=DreamAcoustics34();var near=DreamAcoustics34.Frame();repeat(80){near=sound.sample(DreamRainward34.ID,0f,1.65f,0f,0f,.1f,listOf(p))};verify(near.tension>.6f,"breathing rises smoothly near a creature")
 var voices=0;repeat(140){val f=sound.sample(DreamRainward34.ID,0f,1.65f,0f,0f,.1f,listOf(p));if(f.voice.isNotBlank())voices++};verify(voices in 2..6,"nearby voice is sparse and bounded")
 repeat(120){near=sound.sample(DreamRainward34.ID,0f,1.65f,90f,0f,.1f,listOf(p))};verify(near.tension<.01f,"breathing settles after moving away")
 val blocked=DreamAcoustics34();repeat(80){near=blocked.sample(DreamRainward34.ID,0f,1.65f,0f,0f,.1f,listOf(p)){_,_,_->true}};verify(near.tension<.5f,"wall occlusion reduces perceived threat")
 repeat(100){near=blocked.sample(DreamNewDreams.EYES,0f,1.65f,0f,0f,.1f,listOf(p))};verify(near.tension==0f&&near.voice.isBlank(),"eye preview remains peaceful")
 verify(DreamWeather34.at(DreamRainward34.ID,0f,-30f,5f).rain>.5f&&DreamWeather34.at(DreamRainward34.ID,0f,-110f,5f).rain==0f,"rain stops under the corridor roof")
 verify(DreamWeather34.at(DreamVoyage.REFUGE,0f,0f,1f).wind==0f,"no outdoor weather in refuge")
 verify(DreamEntities32.models.all{DreamCatalogue34.creatureName(it)!="Presencia"},"all entities receive readable names")
 verify(DreamCatalogue34.soundFamily("antler")!=DreamCatalogue34.soundFamily("water-eye")&&DreamCatalogue34.soundFamily("archivist").isEmpty(),"voices match morphology, friendly Alma is excluded")
 File(System.getProperty("dreamcore.output","/tmp"),"ecos34_summary.txt").writeText("63 worlds / 64 compositions / 5 mutually exclusive categories / 6 creature sound families\n")
 verify(DreamCatalogue34.entries.distinct().size==DreamCatalogue34.entries.size,"modern and inherited presences have unique catalogue entries")
 for(entry in DreamCatalogue34.entries)verify(DreamCatalogue34.creatureName(entry)!="Presencia"&&DreamCatalogue34.locations(entry).isNotEmpty(),"every catalogue presence has a readable name and real habitat $entry")
 val returnState=DreamChronicle();returnState.flags.add("kept-a-clue");repeat(20){val point=DreamChronicle.Checkpoint(DreamRainward34.ID,0f,1.65f,-it.toFloat(),0f,0f);DreamCatalogue34.rememberMenuReturn(returnState,point);verify(returnState.returnStack.isEmpty()&&returnState.returnPoint==point&&"kept-a-clue" in returnState.flags,"menu travel keeps one usable return without filling portal stack or erasing progress")}
 println("PASS $checks catalogue, weather, proximity audio and urban map checks")}
