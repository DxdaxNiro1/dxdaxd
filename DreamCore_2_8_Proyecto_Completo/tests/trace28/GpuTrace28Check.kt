package com.dreamcore.game

import android.opengl.GLES20 as G

fun main(){
 var checks=0;fun verify(ok:Boolean,label:String){check(ok){label};checks++}
 val data=FloatArray(70002*10){i->if(i%10==9)(if(i/30%3==0)50f else 0f)else i.toFloat()}
 val mesh=DreamIndexed28.encode(data);val gpu=DreamGpuMesh28(mesh)
 for((first,count)in listOf(0 to 3,65520 to 90,69999 to 3,0 to 70002)){
  G.output.clear();gpu.draw(intArrayOf(2,4,6,8),first,count)
  verify(G.output.toFloatArray().contentEquals(data.copyOfRange(first*10,(first+count)*10)),"actual GLES boundary replays exact cross-page range")
 }
 G.output.clear();gpu.draw(intArrayOf(2,-1,-1,8),shadow=true)
 val expected=ArrayList<Float>();for(i in data.indices step 30)if(data[i+9]==0f)for(j in 0..29)expected.add(data[i+j])
 verify(G.output.toFloatArray().contentEquals(expected.toFloatArray()),"shadow GL range keeps every caster, omits discarded surfaces")
 verify(DreamGpuMesh28.stagingBytes<=4*1024*1024,"native upload staging is bounded and reused")
 gpu.dispose();verify(G.live.isEmpty(),"all page VBO/EBO handles released")
 val noShadow=DreamGpuMesh28(DreamIndexed28.encode(data,includeShadow=false));G.output.clear();noShadow.draw(intArrayOf(2,-1,-1,8),shadow=true);verify(G.output.isEmpty(),"no useless shadow submission for an indoor/actor mesh");noShadow.dispose();verify(G.live.isEmpty(),"empty shadow stream has no buffer leak")
 var id=100;var compiles=0;val programs=DreamPrograms28{v,f->check("uniform mediump float uWorld;" !in v&&"const mediump float uWorld" in f);compiles++;id++}
 fun scene(code:Float)=programs.world(code,DreamShaders.vertexShader,DreamShaders.fragmentShader,DreamShaders.skyVertexShader,DreamShaders.skyFragmentShader)
 val first=scene(1f);scene(2f);verify(scene(1f)==first&&compiles==4,"world-pair cache reuses programs")
 scene(3f);verify(G.deletedPrograms.size==2,"least recently used pair released")
 verify(scene(1f)==first,"active/recent pair survives eviction")
 println("PASS $checks GPU lifecycle checks")
}
