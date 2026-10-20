package com.dreamcore.game

import java.net.URLDecoder
import java.net.URLEncoder
import java.util.zip.CRC32
import kotlin.math.*

/** Persistent choices and consequences, independent of Android and of rendering. */
class DreamChronicle {
    data class Checkpoint(val world:String,val x:Float,val y:Float,val z:Float,val yaw:Float,val pitch:Float)
    val found=mutableSetOf<String>()
    val flags=mutableSetOf<String>()
    val calls=mutableSetOf<String>()
    val symbols=mutableSetOf<String>()
    val visits=mutableMapOf<String,Int>()
    val endings=mutableSetOf<String>()
    var checkpoint=Checkpoint(DreamWorlds.STATION,0f,1.65f,6f,0f,0f)
    var returnPoint:Checkpoint?=null
    val returnStack=mutableListOf<Checkpoint>()
    fun pushReturn(c:Checkpoint):Boolean {
        if(returnStack.size>=7)return false
        returnPoint?.let{returnStack.add(it)};returnPoint=c;return true
    }
    fun popReturn():Checkpoint? {val out=returnPoint;returnPoint=if(returnStack.isEmpty())null else returnStack.removeAt(returnStack.lastIndex);return out}
    var moon=""
    var universeSeed=java.util.Random().nextInt(8000)+1000
    val memories get()=found.count{it.endsWith(":memory") && !it.startsWith("memory:") && !it.startsWith("tower:")}
    fun enter(id:String){visits[id]=(visits[id]?:0)+1}
    fun toggle(key:String):Boolean {if(!flags.add(key)){flags.remove(key);return false};return true}
    fun layout(id:String)=if((visits[id]?:0)>1 && ((visits[id]?:0)%2==0))1 else 0
    fun answer(id:String):String {
        calls.add(id)
        return when {
            "lamp:meadow" in flags -> "La voz dice: dejaste una lámpara encendida. Su luz te espera en la otra casa."
            "dream_10:bell" in found -> "La voz repite la campana que encontraste bajo el agua. No has imaginado ese sonido."
            memories>=3 -> "La voz cuenta ${memories} recuerdos. Dice que el último billete también puede devolverse."
            else -> "Una voz lejana dice: busca el ojo, la espiral, la casa y el sol. La melodía sabe el camino."
        }
    }
    fun ending():String=when {
        "choice:awaken" in flags && memories>=9 && calls.size>=3 && symbols.size>=4 -> "DESPERTAR"
        "choice:stay" in flags || (calls.size>=3 && memories<5) -> "PERMANECER"
        else -> "EL_ECO"
    }
    fun choose(awaken:Boolean) {flags.remove("choice:awaken");flags.remove("choice:stay");flags.add(if(awaken)"choice:awaken" else "choice:stay")}
    companion object {
        fun validWorld(id:String)=id==DreamWorlds.STATION || id==DreamJourney.DARK || id==DreamHorizons.DARK || id==DreamWorlds.HOUSE_MEADOW || id in DreamJourney.sequence || DreamPocket.contains(id)
        private fun esc(s:String)=URLEncoder.encode(s,"UTF-8")
        private fun unesc(s:String)=URLDecoder.decode(s,"UTF-8")
        private fun checkText(c:Checkpoint)=listOf(c.world,c.x,c.y,c.z,c.yaw,c.pitch).joinToString(",")
        private fun point(text:String):Checkpoint? {
            val f=text.split(',');if(f.size!=6 || !validWorld(f[0]))return null
            val n=f.drop(1).map{it.toFloatOrNull()?:return null}
            if(n.any{!it.isFinite()} || abs(n[0])>2000 || abs(n[1])>500 || abs(n[2])>2000 || abs(n[3])>100000 || abs(n[4])>1.5)return null
            return Checkpoint(f[0],n[0],n[1],n[2],n[3],n[4])
        }
        fun encode(s:DreamChronicle):String {
            fun set(v:Set<String>)=v.sorted().joinToString(";"){esc(it)}
            val body=listOf("schema=22","universe="+s.universeSeed,"point="+esc(checkText(s.checkpoint)),"return="+esc(s.returnPoint?.let{checkText(it)}?:""),
                "stack="+s.returnStack.joinToString(";"){esc(checkText(it))},"found="+set(s.found),"flags="+set(s.flags),"calls="+set(s.calls),"symbols="+set(s.symbols),"endings="+set(s.endings),
                "visits="+s.visits.toSortedMap().entries.joinToString(";"){esc(it.key)+":"+it.value},"moon="+esc(s.moon)).joinToString("\n")
            val crc=CRC32();crc.update(body.toByteArray(Charsets.UTF_8))
            return body+"\ncrc="+java.lang.Long.toHexString(crc.value)
        }
        fun decode(text:String?):DreamChronicle? {
            if(text==null || text.length>100000)return null
            return try {
                val index=text.lastIndexOf("\ncrc=");if(index<0)return null
                val body=text.substring(0,index);val crc=CRC32();crc.update(body.toByteArray(Charsets.UTF_8))
                if(java.lang.Long.toHexString(crc.value)!=text.substring(index+5))return null
                val rows=body.lines().associate {val i=it.indexOf('=');if(i<1)return null;it.substring(0,i) to it.substring(i+1)}
                if(rows["schema"] !in listOf("18","19","20","21","22"))return null
                val out=DreamChronicle();out.checkpoint=point(unesc(rows["point"]?:""))?:return null
                out.returnPoint=point(unesc(rows["return"]?:""))
                rows["stack"]?.split(';')?.filter{it.isNotEmpty()}?.take(7)?.forEach{out.returnStack.add(point(unesc(it))?:return null)}
                fun read(key:String,target:MutableSet<String>){rows[key]?.split(';')?.filter{it.isNotEmpty()}?.take(1000)?.forEach{target.add(unesc(it))}}
                read("found",out.found);read("flags",out.flags);read("calls",out.calls);read("symbols",out.symbols);read("endings",out.endings)
                rows["visits"]?.split(';')?.filter{it.isNotEmpty()}?.forEach{val i=it.lastIndexOf(':');if(i>0){val id=unesc(it.substring(0,i));val count=it.substring(i+1).toIntOrNull();if(validWorld(id)&&count!=null)out.visits[id]=count.coerceIn(0,100000)}}
                out.moon=unesc(rows["moon"]?:"")
                out.universeSeed=rows["universe"]?.toIntOrNull()?.coerceIn(1,9999)?:7319
                fun migrate(c:Checkpoint):Checkpoint {
                    if(c.world==DreamWorlds.HOUSE_MEADOW)return Checkpoint(DreamWorlds.HOUSE_CRATER,0f,1.65f,12f,0f,0f)
                    if(rows["schema"]=="18" && c.world==DreamWaterpark.ID){val a=DreamJourney.arrival(c.world,DreamJourney.previous(c.world));return Checkpoint(c.world,a.x,DreamJourney.surface(c.world,a.x,a.z)+1.65f,a.z,a.yaw,0f)}
                    return c
                }
                out.checkpoint=migrate(out.checkpoint);out.returnPoint=out.returnPoint?.let{migrate(it)}
                if(DreamPocket.contains(out.checkpoint.world) && out.returnPoint==null)out.returnPoint=Checkpoint(DreamWorlds.STATION,0f,1.65f,6f,0f,0f)
                out
            } catch(_:Exception){null}
        }
        fun recover(current:String?,previous:String?)=decode(current)?:decode(previous)?:DreamChronicle()
    }
}
