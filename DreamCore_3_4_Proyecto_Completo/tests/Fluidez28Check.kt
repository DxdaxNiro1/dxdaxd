package com.dreamcore.game

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

fun main(){
 var checks=0;fun verify(ok:Boolean,message:String){check(ok){message};checks++}
 val root=File(System.getProperty("dreamcore.baseline","/workspace/dreamcore-validation/build/27"));val out=File(System.getProperty("dreamcore.output","/tmp"));val dir=File(out,"indexed");dir.mkdirs()
 val report=StringBuilder("world\traw_bytes\tpacked_bytes\traw_vertices\tunique_vertices\tshadow_vertices\tpages\tencode_ms\n")
 val chunksReport=StringBuilder("world\traw_bytes\tpacked_bytes\traw_vertices\tunique_vertices\tshadow_vertices\tchunks\n")
 var original=0L;var packed=0L;var inputVertices=0L;var outputVertices=0L
 fun dump(file:File,float:FloatArray){val b=ByteBuffer.allocate(float.size*4).order(ByteOrder.LITTLE_ENDIAN);b.asFloatBuffer().put(float);file.writeBytes(b.array())}
 fun dump(file:File,short:ShortArray){val b=ByteBuffer.allocate(short.size*2).order(ByteOrder.LITTLE_ENDIAN);b.asShortBuffer().put(short);file.writeBytes(b.array())}
 for(line in File(root,"catalog_maps.tsv").readLines()){
  val id=line.substringBefore('\t');dir.listFiles()?.filter{it.name.startsWith(id+"_")}?.forEach{check(it.delete())};val bytes=File(root,"$id.f32").readBytes();val data=FloatArray(bytes.size/4);ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(data)
  var cb=0L;var cp=0L;var cv=0L;var cu=0L;var cs=0L
  val chunks=DreamChunks.split(DreamWorlds.Scene(data))
  for((_,v)in chunks){val batch=DreamPerformance.batch(v);val c=DreamIndexed28.encode(batch.data,batch.opaque,DreamPower.shadowNeeded(id))
   verify(DreamIndexed28.expand(c).contentEquals(batch.data),"actual chunk stream remains bit exact $id")
   cb+=v.size*4;cp+=c.bytes;cv+=v.size/10;cu+=c.uniqueVertices;cs+=c.pages.sumOf{it.shadow.size}
  }
  chunksReport.append("$id\t$cb\t$cp\t$cv\t$cu\t$cs\t${chunks.size}\n")
  val ordered=DreamPerformance.batch(data);val start=System.nanoTime();val mesh=DreamIndexed28.encode(ordered.data,ordered.opaque);val elapsed=(System.nanoTime()-start)/1e6
  val expanded=DreamIndexed28.expand(mesh);verify(ordered.data.contentEquals(expanded),"all original vertex bits, triangle order, UV and normals retained $id")
  var shadow=0;var before=0
  for((n,p)in mesh.pages.withIndex()){
   verify(p.vertices.size/10<=DreamIndexed28.LIMIT,"GLES2 ushort range");verify(p.indices.size%3==0&&p.opaqueIndices%3==0&&p.shadow.size%3==0,"triangle boundaries")
   verify(p.indices.all{(it.toInt()and 65535)<p.vertices.size/10},"valid local indices")
   shadow+=p.shadow.size;before+=p.opaqueIndices
   dump(File(dir,"${id}_${n}.f32"),p.vertices);dump(File(dir,"${id}_${n}.u16"),p.indices);dump(File(dir,"${id}_${n}.shadow.u16"),p.shadow);val layers=DreamSurface32.partition(p);dump(File(dir,"${id}_${n}.surface.u16"),layers.first);dump(File(dir,"${id}_${n}.fx.u16"),layers.second)
  }
  verify(before==ordered.opaque,"opaque phase boundary exact")
  val expected=(ordered.data.indices step 30).count{DreamIndexed28.castsShadow(ordered.data[it+9])}*3
  verify(expected==shadow,"all original shadow casters remain $id")
  report.append("$id\t${bytes.size}\t${mesh.bytes}\t${data.size/10}\t${mesh.uniqueVertices}\t$shadow\t${mesh.pages.size}\t$elapsed\n")
  original+=bytes.size;packed+=mesh.bytes;inputVertices+=data.size/10;outputVertices+=mesh.uniqueVertices
  println("INDEXED $id ${bytes.size} -> ${mesh.bytes}, ${mesh.pages.size} pages");System.out.flush()
 }
 File(out,"index_stats.tsv").writeText(report.toString());File(out,"chunk_stats.tsv").writeText(chunksReport.toString())
 for(name in listOf("moon_round27","moon_crescent27")){
  val bytes=File(root,"$name.f32").readBytes();val data=FloatArray(bytes.size/4);ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(data)
  val mesh=DreamIndexed28.encode(data,includeShadow=false);verify(DreamIndexed28.expand(mesh).contentEquals(data),"animated lunar vertex stream remains exact")
  for((n,p)in mesh.pages.withIndex()){dump(File(dir,"${name}_${n}.f32"),p.vertices);dump(File(dir,"${name}_${n}.u16"),p.indices)}
 }
 // Page overflow and signed-zero seams remain exact, even on drivers without uint indices.
 val stress=FloatArray(70002*10){i->if(i%10==9)0f else i.toFloat()};val large=DreamIndexed28.encode(stress)
 verify(large.pages.size>=2,"more than 65535 unique vertices splits safely");verify(DreamIndexed28.expand(large).contentEquals(stress),"no overflow corruption")
 verify(!DreamShader28.volumeSky(49f)&&!DreamShader28.volumeSky(50f),"no unused volume pass in new dark maps")
 for(code in 0..63){
  val sources=listOf(DreamShaders.vertexShader,DreamShaders.fragmentShader,DreamShaders.skyVertexShader,DreamShaders.skyFragmentShader,DreamLegacyShaders.vertexShader,DreamLegacyShaders.fragmentShader,DreamLegacyShaders.skyVertexShader,DreamLegacyShaders.skyFragmentShader)
  File(out,"surface_vertex_${code}.glsl").writeText(DreamShader28.specialize(DreamSurface32.vertex(DreamShaders.vertexShader),code.toFloat()))
  File(out,"surface_${code}.glsl").writeText(DreamShader28.specialize(DreamAtmosphere32.grade(DreamSurface32.fragment(DreamShaders.fragmentShader)),code.toFloat()))
  for((n,s)in sources.withIndex()){val changed=DreamShader28.specialize(if(n%4 in listOf(1,3))DreamAtmosphere32.grade(s)else s,code.toFloat());verify(!changed.contains("uniform mediump float uWorld;"),"world uniform specialized $code");File(out,"shader_${code}_$n.glsl").writeText(changed)}
 }
 val bounds=listOf(DreamPerformance.Bounds(-1f,-1f,-1f,1f,1f,1f),DreamPerformance.Bounds(10f,10f,10f,11f,11f,11f))
 val matrix=FloatArray(16);for(i in 0..3)matrix[i*5]=1f
 val visible=DreamVisibility28<Int>();val keys=listOf(0,1)
 val first=visible.select(matrix,1,keys){bounds[it]}.toList();val count=visible.tests
 verify(first==keys.filter{DreamPerformance.visible(bounds[it],matrix)},"frustum order/result unchanged")
 verify(visible.select(matrix,1,keys){bounds[it]}.toList()==first && visible.tests==count,"no duplicate cull work in transparent phase/idle view")
 visible.select(matrix,2,keys){bounds[it]};verify(visible.tests>count,"resident revision invalidates selection")
 verify(DreamPerformance.sphereVisible(0f,0f,0f,.5f,matrix),"visible celestial sphere retained")
 verify(!DreamPerformance.sphereVisible(20f,0f,0f,.5f,matrix),"offscreen celestial rejected")
 verify(DreamPerformance.sphereVisible(1.2f,0f,0f,.5f,matrix),"partially visible sphere never removed")
 val random=java.util.Random(2801)
 for(code in listOf(1f,10f,17f,24f,33f,35f,45f,47f,51f))repeat(100){
  val yaw=random.nextFloat()*6.28f;val pitch=(random.nextFloat()-.5f)*3f;val roll=random.nextFloat()*6.28f;val y=random.nextFloat()*900f-300f
  val fy=sin(pitch);val ry=cos(pitch)*sin(roll);val uy=cos(pitch)*cos(roll);val aspect=1.9f
  val may=DreamShader28.cloudMayIntersect(code,y,fy,ry,uy,aspect)
  for(ix in -4..4)for(iy in -4..4){
   val ray=fy+.649408f*(ry*ix/4f*aspect+uy*iy/4f)
   if(abs(ray)>.00001f){val sea=code in listOf(10f,33f,45f,47f,51f)&&ray<0f;val base=if(sea)-135f else 85f;val height=if(sea)110f else 105f
    val a=(base-y)/ray;val b=(base+height-y)/ray
    if(max(a,b)>max(0f,min(a,b)))verify(may,"camera cloud bound never removes an intersecting ray")
   }
  }
 }
 verify(packed<original,"overall geometry memory reduced without deleted content")
 println("BYTES $original -> $packed; VERTICES $inputVertices -> $outputVertices")
 println("PASS $checks optimization checks")
}
