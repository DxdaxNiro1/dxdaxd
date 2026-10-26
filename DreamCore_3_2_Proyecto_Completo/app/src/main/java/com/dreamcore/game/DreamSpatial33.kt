package com.dreamcore.game
import kotlin.math.*
object DreamSpatial33{
 private val notes33=(0..6).map{"piano$it"}.toSet()+(0..3).map{"tone$it"}
 data class Mix(val pan:Float,val gain:Float,val blocked:Boolean,val above:Boolean,val below:Boolean,val caption:String)
 fun mix(x:Float,y:Float,z:Float,yaw:Float,sx:Float,sy:Float,sz:Float,blocked:Boolean):Mix{
  val dx=sx-x;val dy=sy-y;val dz=sz-z;val horizontal=hypot(dx,dz);val distance=sqrt(dx*dx+dy*dy+dz*dz)
  val pan=((cos(yaw)*dx+sin(yaw)*dz)/horizontal.coerceAtLeast(1f)).coerceIn(-.85f,.85f)
  val gain=(.24f/(1f+distance*.035f+distance*distance*.0008f)*(if(blocked).48f else 1f)).coerceIn(.005f,.24f)
  val above=dy>2.8f;val below=dy< -2.8f
  return Mix(pan,gain,blocked,above,below,(if(pan< -.3f)"← " else if(pan>.3f)"→ " else "")+when{above&&blocked->"Un sonido amortiguado en el piso superior";below->"Un sonido debajo de ti";blocked->"Un sonido al otro lado de la pared";above->"Un sonido sobre tu cabeza";else->"Una presencia suena cerca"})
 }
 fun sound(base:String,mix:Mix):String=if(mix.blocked)when{base in notes33->base+"Wall33";base.startsWith("piano")->"pianoWall33";base.startsWith("train")->"trainWall33";base=="whale"->"whaleWall33";base=="wood"->"woodWall33";else->"roomWall33"}else base
}
