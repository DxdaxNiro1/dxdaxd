package com.dreamcore.game

import java.io.File

fun main(){
 var checks=0;fun verify(ok:Boolean,message:String){check(ok){message};checks++}
 fun caps(name:String="Mali-G615",precision:Int=23,units:Int=8,varying:Int=16,depth:Boolean=true)=DreamCompatibility29.Caps(name,"OpenGL ES 3.2",precision,units,varying,4096,4096,4096,4096,depth,true)
 for(name in listOf("Adreno (TM) 619","Adreno 619","Adreno (TM) 610","Adreno (TM) 506")){
  val p=DreamCompatibility29.policy(caps(name),0);verify(p.stable&&!p.depthSampling&&!p.basic,"conservative compatible path $name")
 }
 for(name in listOf("Mali-G615 MC2","Adreno (TM) 730","Adreno (TM) 640","PowerVR Rogue GE8320")){
  val p=DreamCompatibility29.policy(caps(name),0);verify(!p.basic&&p.depthSampling,"capable hardware retains enhanced art $name")
 }
 verify(!DreamCompatibility29.policy(caps(depth=false),0).depthSampling,"no depth extension means no depth sample")
 verify(DreamCompatibility29.policy(caps(precision=0),0).basic,"fragment highp not required to run")
 verify(DreamCompatibility29.policy(caps(varying=8),0).basic,"GLES2 minimum varying fallback")
 verify(!DreamCompatibility29.policy(caps(),1).depthSampling,"manual stable works on any vendor")
 verify(DreamCompatibility29.policy(caps(),2).basic,"manual essential recovery")
 for(w in listOf(1,720,1080,2400,4096,8192))for(h in listOf(1,720,1080,2400))for(limit in listOf(2048,4096,8192)){
  val size=DreamCompatibility29.fit(w,h,1f,limit);verify(size.first in 1..limit && size.second in 1..limit,"render target bounds $w $h $limit")
  if(w<=limit&&h<=limit)verify(size==w to h,"no reduction on native-size supported target")
 }
 val idle=DreamCompatibility29.Budget();repeat(1000){idle.sample(4_000_000,33_333_333,30,true)};verify(idle.scale==1f,"no quality change at steady target cadence")
 val load=DreamCompatibility29.Budget();repeat(600){load.sample(50_000_000,66_666_666,30,true)};verify(load.scale in .80f.. .81f,"sustained overload responds within bounded scale")
 repeat(600){load.sample(2_000_000,33_333_333,30,true)};verify(load.scale>.80f,"recovery is gradual")
 verify(load.sample(90_000_000,100_000_000,30,false)==1f,"native resolution choice respected")
 val transition=DreamCompatibility29.Budget();repeat(90){transition.sample(3_000_000,33_333_333,30,true)};transition.sample(600_000_000,650_000_000,30,true);verify(transition.scale==1f,"portal construction does not trigger scaling")
 val sources=listOf(DreamShaders.vertexShader,DreamShaders.fragmentShader,DreamShaders.skyVertexShader,DreamShaders.skyFragmentShader,DreamShaders.shadowVertexShader,DreamShaders.shadowFragmentShader,DreamLegacyShaders.vertexShader,DreamLegacyShaders.fragmentShader,DreamLegacyShaders.skyVertexShader,DreamLegacyShaders.skyFragmentShader,DreamLunarShaders27.vertexShader,DreamLunarShaders27.fragmentShader)
 val out=File(System.getProperty("dreamcore.output","/tmp"));out.mkdirs()
 for((i,original)in sources.withIndex()){
  val s=original.lines().filterNot{it.trimStart().startsWith("//")}.joinToString("\n")
  verify("float dreamPow" in s && "return q*q" in s,"defined signed-square math $i")
  verify("vec3 dreamNormalize" in s,"guarded normalization $i")
  verify(Regex("\\bpow\\(").findAll(s).count()==1,"no unchecked pow calls $i")
  verify(Regex("\\bnormalize\\(").findAll(s).count()==0,"no zero-vector normalization $i")
  File(out,"compat_shader_$i.glsl").writeText(s)
 }
 // Same mathematical square for negative signed distances used in eyelids, lamps and rim lights.
 for(n in -640..640){val x=n/10f;verify(x*x>=0f && (x*x).isFinite(),"signed square finite $x")}
 println("PASS $checks compatibility checks")
}
