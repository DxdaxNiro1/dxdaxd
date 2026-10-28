package com.dreamcore.game
import kotlin.math.*

/** Original urban nightmare: rain street, red service hall and a supported route over a floating plaza. */
object DreamRainward34 {
    const val ID="dream_rainward34"
    const val TITLE="La ciudad que escucha llover"
    fun indoor(x:Float,z:Float)=z in -148f..-88f
    fun canWalk(x:Float,z:Float):Boolean{
        if(!x.isFinite()||!z.isFinite()||z !in -214f..17f)return false
        val width=if(z> -88f)11f else if(z> -148f)4.3f else 3.1f
        return abs(x)<width&&!(x in 6.4f..9.5f&&z in -45f..-40f)
    }
    fun create():DreamWorlds.Scene{
        val b=DreamMesh();val brick=floatArrayOf(.28f,.23f,.22f);val dark=floatArrayOf(.12f,.15f,.17f);val concrete=floatArrayOf(.30f,.32f,.34f);val red=floatArrayOf(.65f,.055f,.025f)
        fun box(x:Float,y:Float,z:Float,sx:Float,sy:Float,sz:Float,c:FloatArray,k:Float=14f)=b.box(x,y,z,sx,sy,sz,c,k)
        fun mortar(x:Float,y:Float,z:Float,sy:Float,sz:Float){b.quad(floatArrayOf(x,y-sy,z-sz),floatArrayOf(x,y+sy,z-sz),floatArrayOf(x,y+sy,z+sz),floatArrayOf(x,y-sy,z+sz),dark,15f)}
        for(z in -88 until 20 step 4){box(0f,-.12f,z.toFloat(),12f,.12f,2f,dark,93f);for(s in floatArrayOf(-1f,1f))box(s*9.7f,.05f,z.toFloat(),2f,.05f,2f,concrete,14f)}
        for(s in floatArrayOf(-1f,1f))for(i in 0..5){val z=3f-i*17f;val h=13f+(i%3)*3f
            box(s*18f,h/2,z,6f,h/2,8f,brick,14f)
            for(row in 1..(h*2).toInt()){val yy=row*.48f;mortar(s*11.88f,yy,z,.018f,7.9f);for(j in -5..5)mortar(s*11.87f,yy+.24f,z+j*1.4f+(row%2)*.7f,.22f,.018f)}
            for(y in 3..h.toInt()-1 step 3)for(j in -2..2){val xx=s*11.76f;box(xx,y.toFloat(),z+j*2.6f,.035f,1f,.72f,if((y+j+i)%11==0)floatArrayOf(.46f,.34f,.18f)else floatArrayOf(.018f,.024f,.033f),if((y+j+i)%11==0)136f else if(j==0&&y==6&&i%3==1)34f else 137f)}
            box(s*8f,2.8f,z,.06f,2.8f,.06f,dark,15f);box(s*8f,5.65f,z,.7f,.07f,.32f,floatArrayOf(.72f,.72f,.58f),24f)
            DreamShapes.tube(b,listOf(floatArrayOf(s*11f,10f,z),floatArrayOf(0f,8.8f,z-3f),floatArrayOf(-s*11f,10f,z)),.035f,dark,15f,6)
        }
        // Sheltered bus stop and a phone with a dark handset, clear of the walking centre.
        box(8f,1.1f,-42.5f,1.4f,1.1f,2.2f,dark,15f);box(8f,3.4f,-42.5f,2f,.1f,3f,concrete)
        box(-8f,1.7f,-66f,.65f,1.7f,.45f,brick);box(-8f,2.1f,-65.5f,.35f,.55f,.05f,dark,15f)
        for(z in -148 until -88 step 4){box(0f,-.08f,z.toFloat(),5f,.08f,2f,concrete,44f);for(s in floatArrayOf(-1f,1f))box(s*5f,3f,z.toFloat(),.22f,3f,2f,brick,14f)}
        box(0f,6.15f,-118f,5.2f,.15f,30f,dark,15f)
        for(z in -144..-92 step 10){box(0f,5.92f,z.toFloat(),.55f,.06f,.22f,red,136f);for(s in floatArrayOf(-1f,1f)){box(s*4.72f,1.7f,z.toFloat(),.04f,1.7f,.85f,dark,43f);box(s*4.65f,3.6f,z.toFloat(),.06f,.09f,.8f,red,136f)}}
        // The narrow centre is continuous. Side islands and hands remain scenery over the void.
        for(z in -216 until -148 step 4){box(0f,-.20f,z.toFloat(),3.6f,.2f,2f,dark,44f);for(s in floatArrayOf(-1f,1f))box(s*3.5f,.75f,z.toFloat(),.045f,.75f,.045f,red,136f)}
        for(s in floatArrayOf(-1f,1f))for(i in 0..4){val x=s*(9f+(i%2)*5f);val z=-157f-i*11f;box(x,-1.6f,z,3.5f,1.6f,3.5f,brick,14f);box(x,.04f,z,3.5f,.06f,3.5f,floatArrayOf(.065f,.012f,.014f),44f);for(edge in floatArrayOf(-1f,1f)){box(x+edge*3.5f,.13f,z,.025f,.025f,3.5f,red,136f);box(x,.13f,z+edge*3.5f,3.5f,.025f,.025f,red,136f)}
            b.ellipsoid(x,1.1f,z,1.15f,.55f,1.8f,concrete,10,16,14f)
            for(f in 0..4){val dx=(f-2)*.46f;DreamShapes.tube(b,listOf(floatArrayOf(x+dx,1f,z-.7f),floatArrayOf(x+dx,2.1f+f*.08f,z-1.7f),floatArrayOf(x+dx*.8f,2.4f,z-2.5f)),.19f,concrete,14f,8)}
        }
        for(z in floatArrayOf(13f,-209f)){for(s in floatArrayOf(-1f,1f))box(s*1.7f,1.9f,z,.16f,1.9f,.16f,concrete);box(0f,3.9f,z,1.9f,.16f,.18f,red,136f);box(0f,1.8f,z,1.5f,1.8f,.025f,floatArrayOf(.28f,.42f,.46f),29f)}
        return b.finish()
    }
}
