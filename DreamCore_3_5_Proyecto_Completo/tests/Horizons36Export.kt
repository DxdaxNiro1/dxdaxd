package com.dreamcore.game
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
fun main(){
 val o=File(System.getProperty("dreamcore.output","/tmp"));val indexed=File(o,"indexed");indexed.mkdirs()
 fun floats(f:File,v:FloatArray){val b=ByteBuffer.allocate(v.size*4).order(ByteOrder.LITTLE_ENDIAN);b.asFloatBuffer().put(v);f.writeBytes(b.array())}
 fun shorts(f:File,v:ShortArray){val b=ByteBuffer.allocate(v.size*2).order(ByteOrder.LITTLE_ENDIAN);b.asShortBuffer().put(v);f.writeBytes(b.array())}
 for(id in DreamHorizons36.ids){val scene=DreamSceneFactory33.create(id,DreamChronicle());val raw=scene.vertices+DreamObjects.geometry(id,DreamChronicle()).vertices+DreamEvents.geometry(id).vertices+DreamRules23.geometry(id).vertices+DreamPhotography.geometry(id).vertices+DreamWorldMotion.create(id).vertices
  floats(File(o,"$id.f32"),raw);indexed.listFiles()?.filter{it.name.startsWith(id+"_")}?.forEach{it.delete()};val ordered=DreamPerformance.batch(raw);val mesh=DreamIndexed28.encode(ordered.data,ordered.opaque)
  check(DreamIndexed28.expand(mesh).contentEquals(ordered.data))
  for((i,p)in mesh.pages.withIndex()){val name="${id}_$i";floats(File(indexed,"$name.f32"),p.vertices);shorts(File(indexed,"$name.u16"),p.indices);shorts(File(indexed,"$name.shadow.u16"),p.shadow);val layers=DreamSurface32.partition(p);shorts(File(indexed,"$name.surface.u16"),layers.first);shorts(File(indexed,"$name.fx.u16"),layers.second)}
  println("$id ${scene.vertexCount} vertices; exact indexed mesh exported")
 }
 for(code in 0..66){val sources=listOf(DreamShaders.vertexShader,DreamShaders.fragmentShader,DreamShaders.skyVertexShader,DreamShaders.skyFragmentShader,DreamLegacyShaders.vertexShader,DreamLegacyShaders.fragmentShader,DreamLegacyShaders.skyVertexShader,DreamLegacyShaders.skyFragmentShader)
  for((i,s)in sources.withIndex())File(o,"shader_${code}_$i.glsl").writeText(DreamShader28.specialize(if(i%4 in listOf(1,3))DreamAtmosphere32.grade(s)else s,code.toFloat()))
  File(o,"surface_vertex_$code.glsl").writeText(DreamShader28.specialize(DreamSurface32.vertex(DreamShaders.vertexShader),code.toFloat()));File(o,"surface_$code.glsl").writeText(DreamShader28.specialize(DreamAtmosphere32.grade(DreamSurface32.fragment(DreamShaders.fragmentShader)),code.toFloat()))
 }
}
