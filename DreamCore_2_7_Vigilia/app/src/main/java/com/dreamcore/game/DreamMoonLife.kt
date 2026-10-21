package com.dreamcore.game

import kotlin.math.*

/** Foreground-only lunar attention. Looking away for >2 seconds resets the 120s vigil. */
class DreamMoonLife {
    var time=0f;private set
    private var vigilClock=0.0
    val vigil get()=vigilClock.toFloat()
    var mainSmile=0f;private set
    var smallSmile=0f;private set
    var looking=false;private set
    var giantAge=-1f;private set
    var giantX=0f;private set
    var giantY=.5f;private set
    var giantZ=-.86f;private set
    var eyeX=0f;private set
    var eyeY=0f;private set
    var smallEyeX=0f;private set
    var smallEyeY=0f;private set
    private var away=0f
    private var active=false
    /** The first minute changes the blue faces; the independent giant reveal remains at 120s. */
    val bloodMoon get()=vigilClock>=60.0
    val redness get()=if(bloodMoon)1f else 0f
    val giantVisible get()=giantAge>=0f
    val giantReveal get()=if(giantAge<0f)0f else (giantAge/.32f).coerceIn(0f,1f)
    val giantRadius get()=if(!giantVisible)0f else .61f+.025f*sin(time*.18f)
    val mainBlink get()=blink(time,5.7f,.8f)
    val smallBlink get()=blink(time,6.3f,2.4f)
    val giantBlink get()=blink(time,4.9f,1.9f)
    private fun reset() {
        time=0f;vigilClock=0.0;away=0f;mainSmile=0f;smallSmile=0f
        looking=false;giantAge=-1f;eyeX=0f;eyeY=0f;smallEyeX=0f;smallEyeY=0f
    }
    fun step(inDark:Boolean,x:Float,y:Float,z:Float,yaw:Float,pitch:Float,dt:Float,skyVisible:Boolean=true):Boolean {
        if(!inDark){if(active)reset();active=false;return false}
        if(!active){reset();active=true}
        if(dt<=0f || !dt.isFinite())return false
        val step=dt.coerceAtMost(.25f)
        time+=step
        if(giantVisible)giantAge+=step
        val fx=sin(yaw)*cos(pitch);val fy=sin(pitch);val fz=-cos(yaw)*cos(pitch)
        val mainDot=fx*MAIN_X+fy*MAIN_Y+fz*MAIN_Z
        val smallDot=fx*SMALL_X+fy*SMALL_Y+fz*SMALL_Z
        val giantDot=fx*giantX+fy*giantY+fz*giantZ
        val mainLook=mainDot>.982f && skyVisible
        val smallLook=smallDot>.996f && skyVisible
        looking=mainLook || smallLook || (giantVisible && giantDot>.94f && skyVisible)
        if(looking) { vigilClock=(vigilClock+step.toDouble()).coerceAtMost(120.0);away=0f }
        else { away+=step;if(away>2f)vigilClock=0.0 }
        mainSmile=(mainSmile+step*(if(mainLook).024f else -.0025f)).coerceIn(0f,1f)
        smallSmile=(smallSmile+step*(if(smallLook).027f else -.0025f)).coerceIn(0f,1f)
        // Eyes aim at the player's moving position; bounded inside their sockets.
        eyeX=((x*.996f+z*.093f)/220f).coerceIn(-1f,1f)*.060f
        eyeY=((y-1.65f)*.08f-z*.0015f-x*.0002f).coerceIn(-1f,1f)*.032f
        smallEyeX=((x*.724f-z*.690f)/220f).coerceIn(-1f,1f)*.055f
        smallEyeY=((y-1.65f)*.08f+z*.0008f+x*.0008f).coerceIn(-1f,1f)*.030f
        if(vigil>=120f && !giantVisible) {
            giantAge=0f;giantX=fx;giantY=fy;giantZ=fz
            return true
        }
        return false
    }
    fun snapshot()=listOf(time,vigilClock,mainSmile,smallSmile,giantAge,giantX,giantY,giantZ,away,if(active)1f else 0f).joinToString(",")
    fun restore(text:String) {
        val n=text.split(',').mapNotNull{it.toFloatOrNull()};if(n.size!=10 || n.any{!it.isFinite()})return
        time=n[0].coerceIn(0f,1000000f);vigilClock=n[1].coerceIn(0f,120f).toDouble()
        mainSmile=n[2].coerceIn(0f,1f);smallSmile=n[3].coerceIn(0f,1f);giantAge=n[4].coerceIn(-1f,1000000f)
        giantX=n[5].coerceIn(-1f,1f);giantY=n[6].coerceIn(-1f,1f);giantZ=n[7].coerceIn(-1f,1f);away=n[8].coerceIn(0f,10f);active=n[9]>.5f
    }
    companion object {
        private val mainLength=sqrt(.08f*.08f+.50f*.50f+.86f*.86f)
        val MAIN_X=.08f/mainLength;val MAIN_Y=.50f/mainLength;val MAIN_Z=-.86f/mainLength
        private val smallLength=sqrt(.60f*.60f+.48f*.48f+.63f*.63f)
        val SMALL_X=-.60f/smallLength;val SMALL_Y=.48f/smallLength;val SMALL_Z=-.63f/smallLength
        fun blink(time:Float,period:Float,offset:Float):Float {
            val phase=((time+offset)%period+period)%period
            val close=exp(-((phase-.21f)/.10f).pow(2))
            val second=if((time/period).toInt()%4==2)exp(-((phase-.56f)/.09f).pow(2))*.85f else 0f
            return max(close,second).coerceIn(0f,1f)
        }
    }
}
