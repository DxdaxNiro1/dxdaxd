package com.dreamcore.game

import kotlin.math.*

/** Local walking coordinates bend continuously onto an actual vertical world surface. */
object DreamGravity {
    const val ID="pocket_gravity"
    const val RADIUS=30f
    val bendLength=RADIUS*PI.toFloat()/2f
    fun angle(z:Float)=((-z-25f)/RADIUS).coerceIn(0f,PI.toFloat()/2f)
    fun point(x:Float,z:Float,up:Float=0f):FloatArray {
        val s=-z-25f;val a=angle(z)
        val y=when{ s<0f->0f;s<=bendLength->RADIUS*(1f-cos(a));else->RADIUS+s-bendLength }
        val depth=when{s<0f->z;s<=bendLength->-25f-RADIUS*sin(a);else->-25f-RADIUS}
        return floatArrayOf(x,y+up*cos(a),depth+up*sin(a))
    }
    fun vector(z:Float,x:Float,y:Float,depth:Float):FloatArray {val a=angle(z);return floatArrayOf(x,y*cos(a)-depth*sin(a),y*sin(a)+depth*cos(a))}
    fun canWalk(x:Float,z:Float)=abs(x)<6.3f && z in -158f..11f
    fun create():DreamWorlds.Scene {
        val b=DreamMesh();val c=DreamShapes.ivory;val rose=DreamShapes.rose
        for(z in -160 until 12){
            fun p(x:Float,depth:Float,up:Float=0f)=point(x,depth,up)
            b.quad(p(-7f,z.toFloat()),p(-7f,z+1f),p(7f,z+1f),p(7f,z.toFloat()),c,25f)
            for(s in floatArrayOf(-1f,1f)){
                b.quad(p(s*7f,z.toFloat()),p(s*7f,z+1f),p(s*7f,z+1f,1.2f),p(s*7f,z.toFloat(),1.2f),rose,26f)
                if(z%12==0){val q=p(s*5.6f,z.toFloat(),.25f);b.ellipsoid(q[0],q[1],q[2],.22f,.22f,.22f,rose,6,9,24f)}
            }
        }
        // A recognizable floor/city remains below when the player walks up the wall.
        for(x in -70..70 step 20)for(z in -110..-20 step 25){if(abs(x)<12)continue
            b.box(x.toFloat(),3f,z.toFloat(),4f,3f,5f,c,26f)
            b.triangle(floatArrayOf(x-5f,6f,z+5f),floatArrayOf(x+5f,6f,z+5f),floatArrayOf(x.toFloat(),10f,z+5f),rose,43f)}
        for(depth in floatArrayOf(8f,-150f)){
            // Portal panels lie in the player's local vertical plane at each end.
            fun p(x:Float,u:Float)=point(x,depth,u)
            b.quad(p(-1.5f,0f),p(1.5f,0f),p(1.5f,3f),p(-1.5f,3f),floatArrayOf(.55f,.73f,.75f),29f)
        }
        return b.finish()
    }
}
