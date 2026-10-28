package com.dreamcore.game

import kotlin.math.*

/** Bounded temporal poses are also used for delayed reflections. */
class DreamMotion33{
 data class Pose(val time:Float,val x:Float,val y:Float,val z:Float,val yaw:Float)
 private val history=HashMap<String,java.util.ArrayDeque<Pose>>()
 private var world=""
 fun clear(id:String){if(world!=id){history.clear();world=id}}
 fun pose(key:String,time:Float,x:Float,y:Float,z:Float,target:Float,mirror:Boolean):Pose{
  val h=history.getOrPut(key){java.util.ArrayDeque()}
  if(mirror)return h.lastOrNull{it.time<=time-.65f}?:Pose(time-.65f,x,y,z,target)
  val last=h.lastOrNull();var yaw=target
  if(last!=null){var delta=target-last.yaw;while(delta>PI)delta-=2*PI.toFloat();while(delta< -PI)delta+=2*PI.toFloat();yaw=last.yaw+delta*(1f-exp(-(time-last.time).coerceIn(0f,.1f)*1.6f))}
  val p=Pose(time,x,y,z,yaw);if(last==null||time-last.time>=.08f){h.add(p);while(h.size>24)h.removeFirst()};return p
 }
 companion object{
  fun blink(time:Float,phase:Float)=exp(-((time+phase)%6.7f-.22f).pow(2)/.006f)
  fun gestures(name:String,time:Float,phase:Float):FloatArray{
   val speed=when(name){"choir"->.42f;"mask"->.15f;"crimson"->.62f;"antler"->.22f;"gold"->.31f;"star"->.18f;else->.38f}
   return floatArrayOf(sin(time*speed+phase)*.10f,sin(time*.23f+phase)*.045f,.5f+.5f*sin(time*.19f+phase),blink(time,phase))
  }
 }
}
