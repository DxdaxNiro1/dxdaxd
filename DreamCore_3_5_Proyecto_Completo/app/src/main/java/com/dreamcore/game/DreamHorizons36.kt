package com.dreamcore.game
import kotlin.math.*

/** Authored, finite land in an open sky/sea. A missing support means falling, never a wall. */
object DreamHorizons36 {
    const val SUNSET="horizon_doors36"
    const val RED="horizon_crimson36"
    const val BLUE="horizon_blue36"
    val ids=listOf(SUNSET,RED,BLUE)
    fun contains(id:String)=id in ids
    fun title(id:String)=when(id){SUNSET->"Las puertas del último sol";RED->"Bajo el gigante carmesí";else->"La roca del océano infinito"}
    fun sea(id:String)=if(id==SUNSET)-8f else 0f
    fun spawn(id:String)=DreamJourney.Arrival(0f,if(id==RED)10f else if(id==BLUE)0f else 6f,0f)
    data class Stair(val x:Float,val z:Float,val dx:Float,val dz:Float,val rise:Float,val steps:Int,val width:Float=2f)
    val stairs=listOf(Stair(18f,-12f,.28f,-.72f,.38f,104),Stair(-24f,-18f,-.12f,-.90f,.33f,76,1.4f))
    private fun radial(x:Float,z:Float)=hypot(x,z+8f)
    private fun rim(id:String,a:Float)=if(id==BLUE)12.6f+sin(a*7f)*.65f else 30f+sin(a*5f)*2f+cos(a*9f)
    private val rockBases=listOf(Triple(-11f,-4f,3.4f),Triple(10f,3f,2.8f),Triple(3f,-15f,2.6f),Triple(-6f,13f,1.7f))
    private fun boulders(x:Float,z:Float):Float{var y=0f;for((a,b,h)in rockBases){val d=((x-a)*(x-a)+(z-b)*(z-b))/18f;y+=h*exp(-d)};return y}
    private fun islandTop(id:String,x:Float,z:Float)=if(id==BLUE)76f+sin(x*.38f)*.35f+cos(z*.29f)*.45f else 4f+sin(x*.19f)*.45f+cos(z*.14f)*.6f-(radial(x,z)/31f).coerceIn(0f,1f).pow(4)*8f+boulders(x,z)
    fun land(id:String,x:Float,z:Float):Float?{
        val r=radial(x,z);val a=atan2(x,z+8f)
        if(id==SUNSET){
            var result:Float?=if(r<rim(id,a))islandTop(id,x,z) else null
            for(s in stairs){val t=((x-s.x)*s.dx+(z-s.z)*s.dz)/(s.dx*s.dx+s.dz*s.dz);val side=abs((x-s.x)*s.dz-(z-s.z)*s.dx)/hypot(s.dx,s.dz)
                if(t in -.45f..(s.steps+2f)&&side<s.width){val y=base(s)+floor((t+.5f/abs(s.dz)).coerceAtLeast(0f)).coerceAtMost(s.steps.toFloat())*s.rise;result=maxOf(result?:-1000f,y)}}
            return result
        }
        if(id==BLUE){
            if(r<rim(id,a))return islandTop(id,x,z)
            for(i in 0..5){val xx=-40f+i*17f;val zz=-51f-(i%3)*14f;if(hypot(x-xx,z-zz)<4.2f)return 31f+(i%3)*9f}
            return null
        }
        if(abs(x)>95f||z !in -170f..37f)return null
        val edge=minOf(95f-abs(x),37f-z,z+170f)
        return 25f+sin(x*.045f+z*.018f)*8f+cos(z*.045f)*6f+sin(x*.22f)*cos(z*.21f)*.6f-maxOf(0f,9f-edge)*2.4f
    }
    fun base(s:Stair)=islandTop(SUNSET,s.x,s.z)
    fun height(id:String,x:Float,z:Float)=land(id,x,z)?:sea(id)-120f
    fun gates(id:String):List<DreamJourney.Point>{val a=spawn(id);return listOf(DreamJourney.Point(a.x+5f,a.z+2f),DreamJourney.Point(if(id==SUNSET)0f else if(id==RED)0f else -5f,if(id==SUNSET)-24f else if(id==RED)-145f else -12f))}
    fun portalAt(id:String,x:Float,z:Float,feet:Float):String?{val g=gates(id);if(hypot(x-g[0].x,z-g[0].z)<2f&&abs(feet-height(id,x,z))<2f)return DreamPocket.RETURN
        if(hypot(x-g[1].x,z-g[1].z)<2f&&abs(feet-height(id,x,z))<2f)return ids[(ids.indexOf(id)+1)%3]
        if(id==SUNSET)for((i,s)in stairs.withIndex()){if(hypot(x-s.x-s.dx*s.steps,z-s.z-s.dz*s.steps)<2f&&abs(feet-base(s)-s.rise*s.steps)<2f)return if(i==0)BLUE else RED}
        return null}
    fun create(id:String):DreamWorlds.Scene{
        val b=DreamMesh();val stone=floatArrayOf(.37f,.40f,.43f);val red=floatArrayOf(.27f,.065f,.045f);val grass=floatArrayOf(.30f,.40f,.17f)
        fun point(x:Float,z:Float)=floatArrayOf(x,height(id,x,z),z)
        // Exactly the sampled support surface: rendering and walking share the same equations.
        val step=if(id==RED)2f else 1f;val extent=if(id==RED)96 else if(id==SUNSET)34 else 15;val z0=if(id==RED)-171 else -8-extent;val z1=if(id==RED)38 else -8+extent
        if(id==RED){var x=-extent.toFloat();while(x<extent){var z=z0.toFloat();while(z<z1){
            val points=listOf(x to z,x to z+step,x+step to z+step,x+step to z)
            if(points.all{land(id,it.first,it.second)!=null}){b.quad(point(x,z),point(x,z+step),point(x+step,z+step),point(x+step,z),if(id==RED)red else grass,if(id==RED)6f else 4f)}
            z+=step};x+=step}}
        if(id!=RED){val segments=112;val rings=24
            fun p(i:Int,j:Int):FloatArray{val a=i*2f*PI.toFloat()/segments;val radius=rim(id,a)*j/rings;val xx=sin(a)*radius;val zz=-8f+cos(a)*radius;return floatArrayOf(xx,islandTop(id,xx,zz),zz)}
            for(i in 0 until segments){b.triangle(floatArrayOf(0f,islandTop(id,0f,-8f),-8f),p(i,1),p(i+1,1),grass,4f);for(j in 1 until rings)b.quad(p(i,j),p(i,j+1),p(i+1,j+1),p(i+1,j),grass,4f)}
        }
        fun crag(cx:Float,cz:Float,radius:Float,top:Float,bottom:Float,seed:Int){
            val n=if(radius>8)64 else 13;val rings=9
            for(j in 0 until rings)for(i in 0 until n){
                fun p(k:Int,h:Int):FloatArray{val angle=k*2f*PI.toFloat()/n;val t=h.toFloat()/rings;val main=id!=RED&&cx==0f&&cz== -8f;val rr=(if(main)rim(id,angle)else radius*(.80f+.15f*sin(angle*7f+seed)+.05f*cos(angle*13f)))*(if(id==RED)(1f-t).pow(.7f)*(.85f+.15f*sin(t*23f+angle*3f))+.015f else .67f+.33f*t);val xx=cx+sin(angle)*rr;val zz=cz+cos(angle)*rr;val cap=if(main)islandTop(id,xx,zz)else top;return floatArrayOf(xx,bottom+(cap-bottom)*t,zz)}
                b.quad(p(i,j),p(i+1,j),p(i+1,j+1),p(i,j+1),if(id==RED)red else stone,6f)
            }
        }
        if(id==BLUE){crag(0f,-8f,13.3f,76f,-14f,1);for(i in 0..5)crag(-40f+i*17f,-51f-(i%3)*14f,4.5f,31f+(i%3)*9f,-10f,i+3)}
        if(id==SUNSET)crag(0f,-8f,30f,-3f,-17f,0)
        if(id==RED){
            for(i in 0..33){val a=i*2.399f;val xx=cos(a)*(70f+i%5*5f);val zz=-75f+sin(a)*(95f+i%4*5f);val top=50f+(i%7)*13f;crag(xx,zz,8f+(i%4)*2.5f,top,-20f,i)}
            for(side in floatArrayOf(-1f,1f))for(i in 0..13){val xx=side*(14f+(i%3)*10f);val zz=18f-i*12f;crag(xx,zz,2.2f+(i%3)*1.3f,height(id,xx,zz)+3f+i%5,-12f,i)}
        }
        val rng=java.util.Random(3600L+ids.indexOf(id));val count=if(id==RED)1000 else if(id==SUNSET)1700 else 360
        repeat(count){i->val xx=(rng.nextFloat()-.5f)*(if(id==RED)160f else if(id==SUNSET)56f else 22f);val zz=(rng.nextFloat()-.5f)*(if(id==RED)190f else if(id==SUNSET)56f else 22f)+(if(id==RED)-65f else -8f);val h=land(id,xx,zz)
            if(h!=null){if(id!=RED){DreamBotany26.spray(b,xx,h+.015f,zz,.36f,.35f,i*.8f,if(i%17==0)7 else if(i%7==0)11 else 8);if(id==SUNSET&&i%6==0)DreamBotany26.spray(b,xx,h+.02f,zz,.40f,.42f,i*.7f,7)}else{
                val c=floatArrayOf(.34f+(i%4)*.05f,.035f,.028f);for(k in 0..3){val a=k*.8f+i;b.triangle(floatArrayOf(xx-.06f,h,zz),floatArrayOf(xx+.06f,h,zz),floatArrayOf(xx+cos(a)*.35f,h+.45f+k*.10f,zz+sin(a)*.35f),c,0f)}
            }}
        }
        fun door(xx:Float,y:Float,zz:Float,arch:Boolean=false){val ivory=floatArrayOf(.86f,.81f,.66f)
            for(side in floatArrayOf(-1f,1f))b.box(xx+side*1.32f,y+2.3f,zz,.20f,2.3f,.22f,ivory,26f)
            if(arch){for(i in 0 until 24){val a=i*PI.toFloat()/24;val d=(i+1)*PI.toFloat()/24;val p=floatArrayOf(xx+cos(a)*1.32f,y+4.6f+sin(a)*1.32f,zz);val q=floatArrayOf(xx+cos(d)*1.32f,y+4.6f+sin(d)*1.32f,zz);DreamShapes.tube(b,listOf(p,q),.24f,ivory,26f,7)}}else{b.box(xx,y+4.7f,zz,1.53f,.17f,.22f,ivory,26f);b.box(xx,y+2.3f,zz+.03f,1.05f,2.15f,.1f,floatArrayOf(.89f,.84f,.72f),8f);for(h in floatArrayOf(1.35f,3.4f))b.box(xx,y+h,zz+.15f,.81f,.57f,.04f,ivory,26f)}
        }
        if(id==SUNSET){for(s in stairs){for(i in 0..s.steps){val xx=s.x+s.dx*i;val zz=s.z+s.dz*i;val yy=base(s)+s.rise*i;b.box(xx,yy-.18f,zz,s.width,.18f,.5f,floatArrayOf(.83f,.77f,.65f),26f)};door(s.x+s.dx*s.steps,base(s)+s.rise*s.steps,s.z+s.dz*s.steps)}
            run{val ivory=floatArrayOf(.93f,.86f,.72f);for(side in floatArrayOf(-1f,1f))b.box(-53f+side*7f,16f,-95f,.7f,8f,.8f,ivory,26f);for(i in 0 until 40){val a=i*PI.toFloat()/40;val q=(i+1)*PI.toFloat()/40;DreamShapes.tube(b,listOf(floatArrayOf(-53f+cos(a)*7f,24f+sin(a)*7f,-95f),floatArrayOf(-53f+cos(q)*7f,24f+sin(q)*7f,-95f)),.75f,ivory,26f,9)}};door(62f,19f,-140f);door(-25f,24f,-165f);door(5f,-5f,-75f)
        }
        for(p in gates(id))door(p.x,height(id,p.x,p.z),p.z)
        if(id!=RED){
            // Flat horizon reaches the camera far plane; reflected environment also exists without an FBO.
            for(xx in -640 until 640 step 32)for(zz in -640 until 640 step 32)b.quad(floatArrayOf(xx.toFloat(),sea(id),zz.toFloat()),floatArrayOf(xx.toFloat(),sea(id),zz+32f),floatArrayOf(xx+32f,sea(id),zz+32f),floatArrayOf(xx+32f,sea(id),zz.toFloat()),floatArrayOf(.02f,.30f,.42f),138f)
        }
        return b.finish()
    }
}
