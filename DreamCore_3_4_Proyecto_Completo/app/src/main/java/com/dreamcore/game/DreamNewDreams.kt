package com.dreamcore.game

import kotlin.math.*

/** Optional, self-contained dreams. The field contains quiet observers that react when their space is invaded. */
object DreamNewDreams {
    fun releaseGeometry(){cachedId="";cached=null}
    const val EYES="preview_eyes"
    const val ISLAND="dream_rose_island"
    val ids=listOf(EYES,ISLAND)
    fun contains(id:String)=id in ids
    fun title(id:String)=if(id==EYES)"La pradera que te mira" else "La casa rosa sobre el océano"
    fun spawn(id:String)=if(id==EYES)DreamJourney.Point(0f,10f)else DreamJourney.Point(0f,-29f)
    fun gates(id:String)=if(id==EYES)listOf(DreamJourney.Point(0f,17f))else listOf(DreamJourney.Point(-14f,-32f),DreamJourney.Point(0f,188f))
    fun hill(x:Float,z:Float)=2.3f*sin(x*.027f)*cos(z*.021f)+1.1f*sin((x+z)*.041f)
    fun slideX(z:Float)=sin(((z+20f)/200f).coerceIn(0f,1f)*PI.toFloat()*2f)*5f
    fun slideHeight(z:Float):Float{val t=((z+20f)/200f).coerceIn(0f,1f);return 72f-71f*t*t*(3f-2f*t)}
    fun height(id:String,x:Float,z:Float):Float=if(id==EYES)hill(x,z)else if(z>=180f)1f else if(z>=-20f)slideHeight(z)+1.55f*(1f-sqrt(1f-((x-slideX(z))/2.25f).coerceIn(-1f,1f).pow(2)))else 72f
    fun canWalk(id:String,x:Float,z:Float):Boolean {
        if(id==EYES)return hypot(x,z+80f)<190f && !DreamGuardians27.blocked(x,z)
        if(z in -20f..180f)return abs(x-slideX(z))<1.95f
        if(z in 180f..194f)return abs(x)<7f
        if(hypot(x,z+45f)>28.8f)return false
        if(abs(x)<6.8f && abs(z+56f)<6.3f)return false
        if(abs(abs(x)-17f)<2.2f && abs(z+49f)<2.2f)return false
        return true
    }
    fun portalAt(id:String,x:Float,z:Float)=if(gates(id).any{hypot(x-it.x,z-it.z)<2.5f})DreamPocket.RETURN else null
    private var cachedId="";private var cached:DreamWorlds.Scene?=null
    fun create(id:String):DreamWorlds.Scene{
        if(cachedId==id)return cached!!
        val b=DreamMesh();val grass=floatArrayOf(.49f,.67f,.35f);val pink=floatArrayOf(.89f,.50f,.63f);val cream=DreamShapes.ivory
        if(id==EYES){
            for(x in -240 until 240 step 6)for(z in -320 until 160 step 6){fun p(xx:Float,zz:Float)=floatArrayOf(xx,hill(xx,zz)-.04f,zz)
                b.quad(p(x.toFloat(),z.toFloat()),p(x.toFloat(),z+6f),p(x+6f,z+6f),p(x+6f,z.toFloat()),grass,4f)}
            DreamSculpt.chair(b,-16f,hill(-16f,-31f),-31f)
            DreamSculpt.chair(b,13f,hill(13f,-39f),-39f)
            // An empty chair remains as a point of reference.
            DreamSculpt.chair(b,-10f,hill(-10f,-35f),-35f)

        }else{
            // A smooth organic pillar, with layered grass at its rim and a tiled sea below.
            val sides=96;val rings=12
            for(j in 0 until rings)for(i in 0 until sides){val a=i*2f*PI.toFloat()/sides;val d=(i+1)*2f*PI.toFloat()/sides
                fun p(t:Float,r:Int):FloatArray{val y=-8f+r*80f/rings;val radius=30f+(1f-r.toFloat()/rings)*5f+.35f*sin(t*7f+r*.2f);return floatArrayOf(cos(t)*radius,y,-45f+sin(t)*radius)}
                b.quad(p(a,j),p(d,j),p(d,j+1),p(a,j+1),grass,4f)
            }
            for(i in 0 until sides){val a=i*2f*PI.toFloat()/sides;val d=(i+1)*2f*PI.toFloat()/sides;b.triangle(floatArrayOf(0f,72f,-45f),floatArrayOf(cos(d)*30f,72f,-45f+sin(d)*30f),floatArrayOf(cos(a)*30f,72f,-45f+sin(a)*30f),grass,4f)}
            DreamSculpt.house(b,0f,72f,-56f,pink,1.35f)
            for(s in floatArrayOf(-1f,1f)){DreamSculpt.cylinder(b,s*17f,72f,-49f,.55f,5f,floatArrayOf(.31f,.23f,.18f),43f,12)
                for(i in 0..4){val a=i*2.399f;b.ellipsoid(s*17f+cos(a)*1.7f,78f+sin(i.toFloat()),-49f+sin(a)*1.7f,2.4f,2.5f,2.4f,grass,9,16,38f)}}
            for(x in -560 until 560 step 40)for(z in -560 until 560 step 40)b.quad(floatArrayOf(x.toFloat(),-.12f,z.toFloat()),floatArrayOf(x.toFloat(),-.12f,z+40f),floatArrayOf(x+40f,-.12f,z+40f),floatArrayOf(x+40f,-.12f,z.toFloat()),floatArrayOf(.17f,.56f,.63f),1f)
            for(j in 0 until 200){val z0=-20f+j;val z1=z0+1f
                fun p(z:Float,a:Float):FloatArray{val d=sin(a)*2.25f;return floatArrayOf(slideX(z)+d,slideHeight(z)+1.55f*(1f-cos(a)),z)}
                for(k in 0 until 12){val a=-1.45f+k*2.9f/12f;val d=a+2.9f/12f;b.quad(p(z0,a),p(z1,a),p(z1,d),p(z0,d),pink,15f)}
                if(j%2==0)for(s in floatArrayOf(-1f,1f))DreamShapes.tube(b,listOf(p(z0,s*1.45f),p(z1,s*1.45f),p(z1+1f,s*1.45f)),.09f,cream,15f,7)
            }
            DreamSculpt.rounded(b,0f,.65f,187f,8f,.35f,8f,.2f,pink,15f)
            for(s in floatArrayOf(-1f,1f))DreamShapes.tube(b,listOf(floatArrayOf(s*7.5f,2.4f,180f),floatArrayOf(s*7.5f,2.4f,194f)),.09f,cream,15f,8)
        }
        for(p in gates(id)){val y=height(id,p.x,p.z);for(s in floatArrayOf(-1f,1f))DreamSculpt.rounded(b,p.x+s*1.7f,y+1.7f,p.z,.18f,1.7f,.18f,.09f,cream,26f);b.box(p.x,y+3.5f,p.z,1.9f,.14f,.18f,cream,26f);b.box(p.x,y+1.6f,p.z,1.4f,1.6f,.03f,pink,29f)}
        cachedId=id;cached=b.finish();return cached!!
    }
}
