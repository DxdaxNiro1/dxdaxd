package com.dreamcore.game
import java.io.File
import kotlin.math.*
fun main(){var checks=0;fun verify(v:Boolean,m:String){check(v){m};checks++}
 val o=File(System.getProperty("dreamcore.output","/tmp"));o.mkdirs()
 for(id in DreamHorizons36.ids){
  val a=DreamHorizons36.spawn(id);val h=DreamHorizons36.height(id,a.x,a.z)
  verify(DreamChronicle.validWorld(id)&&DreamMusic24.track(id)!=null,"registered map and music $id")
  verify(h.isFinite()&&DreamHorizons36.land(id,a.x,a.z)!=null,"spawn is supported $id")
  val state=DreamChronicle();state.flags.add("keep-decisions");state.checkpoint=DreamChronicle.Checkpoint(id,a.x,h+1.65f,a.z,0f,0f);verify(DreamChronicle.decode(DreamChronicle.encode(state))!!.checkpoint==state.checkpoint,"CRC save resume $id")
  val physical=DreamAirfall36();physical.reset(id,a.x,a.z,h);physical.step(id,0f,0f,.05f,true);verify(!physical.grounded&&physical.feet>h,"ordinary jump lifts from actual ground $id")
  repeat(70){physical.step(id,0f,0f,.02f,false)};verify(physical.grounded&&abs(physical.feet-h)<.02f,"ordinary jump lands without resetting $id")
  // Run straight over an actual edge. There is no coordinate wall in the air.
  var falling=false;var wind=false;var fullyClosed=false;var returned=false;var previousEyes=0f
  for(i in 0..1700){physical.step(id,12f,0f,.02f,false);falling=falling||physical.speedY< -8f;wind=wind||physical.wind>.3f;fullyClosed=fullyClosed||physical.eyes==1f
   if(physical.returned){verify(physical.eyes==1f,"position only resets behind closed eyelids $id");returned=true;break};previousEyes=physical.eyes}
  verify(falling&&wind&&fullyClosed&&returned,"walk off edge, feel wind and return $id")
  repeat(70){physical.step(id,0f,0f,.02f,false)};verify(physical.eyes==0f&&physical.grounded,"eyes reopen on solid land $id")
  verify(DreamWorlds.canWalk(id,900f,-900f),"no invisible perimeter wall $id")
  val broken=DreamRestore.repair(DreamChronicle.Checkpoint(id,800f,-50f,800f,0f,0f));verify(DreamHorizons36.land(id,broken.x,broken.z)!=null,"continuing a midair save is repaired $id")
  val mesh=DreamHorizons36.create(id);verify(mesh.vertices.size%30==0&&mesh.vertices.all{it.isFinite()}&&mesh.vertexCount<260000,"finite bounded complete scene $id ${mesh.vertexCount}")
  for(g in DreamHorizons36.gates(id)){verify(DreamHorizons36.land(id,g.x,g.z)!=null,"door supported $id");verify(DreamJourney.portalAt(id,g.x,g.z,DreamHorizons36.height(id,g.x,g.z))!=null,"door has actual destination $id")}
  verify(DreamPortalNames.gates(id).size>=2,"signposted return and next door $id")
 }
 val actual=DreamSkySight(DreamHorizons36.create(DreamHorizons36.SUNSET).vertices)
 for(s in DreamHorizons36.stairs){val a=DreamAirfall36();a.reset(DreamHorizons36.SUNSET,s.x,s.z,DreamHorizons36.base(s));val length=hypot(s.dx,s.dz);var landed=false
  repeat(4200){a.step(DreamHorizons36.SUNSET,s.dx/length*3f,s.dz/length*3f,.01f,false){x,y,z->actual.bodyClear33(x,y,z)};if(hypot(a.x-s.x-s.dx*s.steps,a.z-s.z-s.dz*s.steps)<1f)landed=true}
  verify(landed,"stair reaches raised door without teleport: ${a.x},${a.z},${a.feet}")
 }
 verify(DreamGraphics26.waterPlane(DreamHorizons36.BLUE)==0f&&DreamGraphics26.waterPlane(DreamHorizons36.SUNSET)==-8f,"true sea reflection levels")
 verify(DreamHorizons36.ids.all{!DreamShader28.volumeSky(DreamJourney.worldCode(it))},"new sky avoids legacy volume raymarching")
 println("PASS $checks new horizon and falling checks")
}
